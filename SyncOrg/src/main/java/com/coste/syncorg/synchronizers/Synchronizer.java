package com.coste.syncorg.synchronizers;

import android.content.ContentResolver;
import android.content.Context;
import android.text.TextUtils;

import com.coste.syncorg.R;
import com.coste.syncorg.gui.SynchronizerNotification;
import com.coste.syncorg.gui.SynchronizerNotificationCompat;
import com.coste.syncorg.orgdata.OrgFileOld;
import com.coste.syncorg.orgdata.OrgFileImporter;
import com.coste.syncorg.util.OrgUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.cert.CertificateException;
import java.util.HashSet;

/**
 * The base class of all the synchronizers.
 * The singleton instance of the class can be retreived using getInstance()
 * This class implements many of the operations that need to be done on
 * synching.
 * <p/>
 */
public abstract class Synchronizer {
    public static final String SYNC_UPDATE = "com.coste.syncorg.Synchronizer.action.SYNC_UPDATE";
    public static final String SYNC_DONE = "sync_done";
    public static final String SYNC_START = "sync_start";
    public static final String SYNC_PROGRESS_UPDATE = "progress_update";
    public static final String SYNC_SHOW_TOAST = "showToast";
    static private boolean syncRunning = false;
    private static boolean syncEnabled = true;
    protected Context context;
    private ContentResolver resolver;
    private OrgFileImporter importer;
    private SynchronizerNotificationCompat notify;


    protected Synchronizer(Context context, OrgFileImporter importer) {
        this.context = context;
        this.resolver = context.getContentResolver();
        this.importer = importer;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
            this.notify = new SynchronizerNotification(context);
        else
            this.notify = new SynchronizerNotificationCompat(context);
    }

    public void setSyncEnabled(boolean syncEnabled) {
        Synchronizer.syncEnabled = syncEnabled;
    }

    /**
     * Instanciate a synchronizer and start a synchronization if not already running
     */
    public void runSynchronize() {
        Thread syncThread = new Thread() {
            public void run() {
                syncRunning = true;
                Synchronizer syncer = Synchronizer.this;
                try {
                    syncer.execute();
                    syncer.postSynchronize();
                } catch (Exception e) {
                    syncer.notify.errorNotification(e.getMessage());
                } finally {
                    syncRunning = false;
                }
            }
        };

        if (syncEnabled && !syncRunning) {
            syncThread.start();
        }
    }

    /**
     * Return true if the user has to enter its credentials when the app starts
     * eg. SSHSynchonizer by password returns yes
     *
     * @return
     */
    public boolean isCredentialsRequired() {
        return false;
    }


    /**
     * @return List of files that where changed.
     */
    private void execute() {
        if (!isConfigured()) {
            notify.errorNotification("Sync not configured");
            return;
        }

        try {
            announceStartSync();

            isConnectable();

            SyncResult pulledFiles = synchronize();

            for (String filename : pulledFiles.deletedFiles) {
                OrgFileOld orgFile = new OrgFileOld(filename, resolver);
                orgFile.removeFile(context, true); // FIXME: trigger dao here
            }

            HashSet<String> modifiedFiles = pulledFiles.newFiles;
            modifiedFiles.addAll(pulledFiles.changedFiles);
            for (String filename : modifiedFiles) {
                if (filename.startsWith(".")) continue; // no hidden files
                File file = new File(filename);
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                    importer.parseFile(file.getAbsolutePath(), bufferedReader, context);
                }
            }

            announceSyncDone();
        } catch (Exception e) {
            showErrorNotification(e);
            e.printStackTrace();
        }
    }

    private void announceStartSync() {
        notify.setupNotification();
        OrgUtils.announceSyncStart(context);
    }

    private void announceProgressUpdate(int progress, String message) {
        if (message != null && !TextUtils.isEmpty(message))
            notify.updateNotification(progress, message);
        else
            notify.updateNotification(progress);
        OrgUtils.announceSyncUpdateProgress(progress, context);
    }

    private void announceProgressDownload(String filename, int fileIndex, int totalFiles) {
        int progress = 0;
        if (totalFiles > 0)
            progress = (100 / totalFiles) * fileIndex;
        String message = context.getString(R.string.downloading) + " " + filename;
        announceProgressUpdate(progress, message);
    }

    private void showErrorNotification(Exception exception) {
        notify.finalizeNotification();

        String errorMessage = "";
        if (CertificateException.class.isInstance(exception)) {
            errorMessage = "Certificate Error occured during sync: "
                    + exception.getLocalizedMessage();
        } else {
            errorMessage = "Error: " + exception.getLocalizedMessage();
        }

        notify.errorNotification(errorMessage);
    }

    private void announceSyncDone() {
        announceProgressUpdate(100, "Done synchronizing");
        notify.finalizeNotification();
        OrgUtils.announceSyncDone(context);
    }

    public abstract String getAbsoluteFilesDir();

    /**
     * Delete all files from the synchronized repository
     * except repository configuration files
     *
     * @param context
     */
    public void clearRepository(Context context) {
        File dir = new File(getAbsoluteFilesDir());
        for (File file : dir.listFiles()) {
            file.delete();
        }
    }


    /**
     * Called before running the synchronizer to ensure that it's configuration
     * is in a valid state.
     */
    abstract boolean isConfigured();

    /**
     * Called before running the synchronizer to ensure it can connect.
     */
    abstract public boolean isConnectable() throws Exception;


    abstract SyncResult synchronize();


    /**
     * Use this to disconnect from any services and cleanup.
     */
    public abstract void postSynchronize();

    /**
     * Synchronize a new file
     *
     * @param filename
     */
    abstract public void addFile(String filename);


}
