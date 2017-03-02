package com.coste.syncorg.synchronizers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.coste.syncorg.services.PermissionManager;
import com.coste.syncorg.util.OrgUtils;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.eclipse.jgit.util.FS;

import java.io.File;

public class SSHSynchronizer extends Synchronizer {
    private final String LT = "SyncOrg";
    AuthData authData;
    String absoluteFileDir;
    private Session session;

    private ExternalSynchronizer dirSynchronizer;

    public SSHSynchronizer(Context context) {
        super(context);
        this.context = context;
        authData = AuthData.getInstance(context);
        SharedPreferences appSettings = PreferenceManager
                .getDefaultSharedPreferences(context);
        this.absoluteFileDir = appSettings.getString("syncFolder",
                context.getFilesDir() + "/" + JGitWrapper.GIT_DIR);


        if (PermissionManager.permissionGranted(context) == false) return;

        this.dirSynchronizer = new ExternalSynchronizer(context, getAbsoluteFilesDir());

        File dir = new File(getAbsoluteFilesDir());
        if (!dir.exists()) {
            dir.mkdir();
        }

    }

    @Override
    public String getAbsoluteFilesDir() {
        return absoluteFileDir;
    }

    @Override
    public boolean isConfigured() {
        return !(authData.getPath().equals("")
                || authData.getUser().equals("")
                || authData.getHost().equals("")
                || authData.getPassword().equals("")
                && AuthData.getPublicKey(context).equals(""));
    }

    public void connect() {
        try {
            SshSessionFactory sshSessionFactory = new SshSessionFactory(context);
            JSch jSch = sshSessionFactory.createDefaultJSch(FS.detect());


            session = jSch.getSession(
                    authData.getUser(),
                    authData.getHost(),
                    authData.getPort());

            session.setPassword(AuthData.getInstance(context).getPassword());

            // TODO: find a way to check for host key
//            jSch.setKnownHosts("/storage/sdcard0/Download/known_hosts");
            session.setConfig("StrictHostKeyChecking", "no");

            session.connect();
            session.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
        }

    }

    public SyncResult synchronize() {
        if (PermissionManager.permissionGranted(context) == false) return new SyncResult();

        if (isCredentialsRequired()) return new SyncResult();

        SyncResult dirRefreshResult = this.dirSynchronizer.synchronize();

        String folder = Synchronizer.getSynchronizer(context).getAbsoluteFilesDir();
        SyncResult pullResult = JGitWrapper.pull(context, folder);

        new JGitWrapper.PushTask(context).execute();

        pullResult.changedFiles.addAll(dirRefreshResult.changedFiles);
        pullResult.deletedFiles.addAll(dirRefreshResult.deletedFiles);
        pullResult.newFiles.addAll(dirRefreshResult.newFiles);

        Log.d("SyncOrg/SSHSynchronizer", pullResult.changedFiles.toString());

        return pullResult;
    }

    /**
     * Except if authentication by Public Key, the user has to enter his password
     *
     * @return
     */
    public boolean isCredentialsRequired() {
        return false;
    }

    @Override
    public void postSynchronize() {
        if (this.session != null)
            this.session.disconnect();
    }

    @Override
    public void _addFile(String filename) {
        JGitWrapper.add(filename, context);
    }

    @Override
    public boolean isConnectable() throws Exception {
        if (!OrgUtils.isNetworkOnline(context)) return false;

        this.connect();
        return true;
    }

    @Override
    public void clearRepository(Context context) {
        File dir = new File(getAbsoluteFilesDir());
        for (File file : dir.listFiles()) {
            if (file.getName().equals(".git")) continue;
            file.delete();
        }
    }
}
