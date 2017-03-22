package com.coste.syncorg.util.di;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.coste.syncorg.orgdata.OrgDatabase;
import com.coste.syncorg.orgdata.OrgFileImporter;
import com.coste.syncorg.synchronizers.ExternalSynchronizer;
import com.coste.syncorg.synchronizers.SSHSynchronizer;
import com.coste.syncorg.synchronizers.Synchronizer;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private Context appContext;

    public ApplicationModule(Context appContext) {
        this.appContext = appContext;
    }

    @Provides
    Context provideContext() {
        return this.appContext;
    }

    @Provides
    OrgDatabase provideOrgDatabase() {
        return OrgDatabase.getInstance();
    }


    @Provides
    OrgFileImporter provideOrgFileImporter() {
        return new OrgFileImporter(appContext);
    }

    @Provides
    Synchronizer provideSynchronizer(SSHSynchronizer sshSynchronizer, ExternalSynchronizer externalSynchronizer) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String syncSource = preferences.getString("syncSource", "");
        switch (syncSource) {
            case "sdcard":
//                return new SDCardSynchronizer(appContext);
                return null;
            case "scp":
                return sshSynchronizer;
            default:
                return externalSynchronizer;
        }
    }


//    @Provides
//    static FilterDao provideFilterDao(OrgDatabase db) {
//        return new FilterDao(db);
//    }
}
