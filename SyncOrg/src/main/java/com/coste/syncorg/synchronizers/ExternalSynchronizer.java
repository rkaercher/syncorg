package com.coste.syncorg.synchronizers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.coste.syncorg.orgdata.OrgFileOld;
import com.coste.syncorg.orgdata.OrgFileImporter;
import com.coste.syncorg.services.PermissionManager;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A synchronizer that keeps track of the synchronization state of a standard folder
 * This is done by keeping track of the timestamp of last modification for each file in the folder.
 * Every time the synchronizer is called it will check whether timestamps kept in DB match those of
 * the files. It will then parse new files and externally modified files
 */
@Singleton
public class ExternalSynchronizer extends Synchronizer {
    private String syncFolder;

    @Inject
    public ExternalSynchronizer(Context context, OrgFileImporter importer) {
        super(context, importer);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        syncFolder = preferences.getString("repoPath", "null");
        File dir = new File(getAbsoluteFilesDir());

        if (!PermissionManager.permissionGranted(context)) return;

        if (!dir.exists()) {
            createSyncFolder();
        }
    }


    private void createSyncFolder() {
        File dir = new File(getAbsoluteFilesDir());
        dir.mkdir();
    }

    @Override
    public String getAbsoluteFilesDir() {
        return syncFolder;
    }

    public boolean isConfigured() {
        return true;
    }

    public void putRemoteFile(String filename, String contents) {
    }

    public BufferedReader getRemoteFile(String filename) {
        return null;
    }

    @Override
    public SyncResult synchronize() {
        SyncResult result = new SyncResult();
        if (!PermissionManager.permissionGranted(context)) return result;

        ArrayList<File> files = getFilesRecursively(new File(getAbsoluteFilesDir()));

        HashMap<String, Long> times_modified = OrgFileOld.getLastModifiedTimes(context);
        result.deletedFiles = new TreeSet<>(times_modified.keySet());

        for (File f : files) {
            result.deletedFiles.remove(f.getAbsolutePath());
            Long timeInDB = times_modified.get(f.getAbsolutePath());
            if (timeInDB == null || f.lastModified() != timeInDB) {
                result.changedFiles.add(f.getAbsolutePath());
            }
        }

        return result;
    }

    private ArrayList<File> getFilesRecursively(File dir) {
        ArrayList<File> result = new ArrayList<>();
        if (dir == null || dir.listFiles() == null) return result;

        for (File f : dir.listFiles()) {
            // Skip hidden files
            if (f.getName().startsWith(".")) continue;
            if (f.isDirectory()) {
                result.addAll(getFilesRecursively(f));
            } else {
                result.add(f);
            }
        }

        return result;
    }


    @Override
    public void postSynchronize() {
    }

    @Override
    public void addFile(String filename) {

    }

    @Override
    public boolean isConnectable() {
        return true;
    }
}