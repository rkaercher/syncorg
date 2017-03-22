package com.coste.syncorg.synchronizers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.coste.syncorg.orgdata.OrgFileImporter;
import com.coste.syncorg.services.PermissionManager;
import com.coste.syncorg.util.OrgUtils;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.eclipse.jgit.util.FS;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SSHSynchronizer extends Synchronizer {
    private final String LT = "SyncOrg";
    private AuthData authData;
    private String absoluteFileDir;
    private Session session;

    private JGitWrapper gitWrapper;


    @Inject
    public SSHSynchronizer(Context context, JGitWrapper gitWrapper, OrgFileImporter importer) {
        super(context, importer);
        this.gitWrapper = gitWrapper;
        this.context = context;
        authData = AuthData.getInstance(context);
        SharedPreferences appSettings = PreferenceManager
                .getDefaultSharedPreferences(context);
        this.absoluteFileDir = appSettings.getString("repoPath",
                context.getFilesDir() + "/" + JGitWrapper.GIT_DIR);


        if (!PermissionManager.permissionGranted(context)) return;

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

    private void connect() {
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

    private HashSet<String> makeAbsolute(String folder, Set<String> relativePaths) {
        HashSet<String> result = new HashSet<>();
        for (String filePath : relativePaths) {
            result.add(folder + "/" + filePath);
        }
        return result;
    }

    private SyncResult withAbsolutePaths(String folder, SyncResult relativePathsResult) {
        SyncResult result = new SyncResult();
        result.newFiles = makeAbsolute(folder, relativePathsResult.newFiles);
        result.changedFiles = makeAbsolute(folder, relativePathsResult.changedFiles);
        result.deletedFiles = makeAbsolute(folder, relativePathsResult.deletedFiles);
        result.setState(relativePathsResult.state);
        return result;
    }

    public SyncResult synchronize() {
        if (!PermissionManager.permissionGranted(context)) return new SyncResult();

        if (isCredentialsRequired()) return new SyncResult();
        String folder = getAbsoluteFilesDir();
        SyncResult pullResult = withAbsolutePaths(folder, gitWrapper.pull(context, folder));

        gitWrapper.createPushTask(context, getAbsoluteFilesDir()).execute();
        return pullResult;
    }

    /**
     * Except if authentication by Public Key, the user has to enter his password
     *
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
    public void addFile(String filename) {
        gitWrapper.add(filename, context, getAbsoluteFilesDir());
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
