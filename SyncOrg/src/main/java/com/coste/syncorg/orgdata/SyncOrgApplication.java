package com.coste.syncorg.orgdata;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.coste.syncorg.util.ApplicationDiComponent;
import com.coste.syncorg.util.DaggerApplicationDiComponent;

import javax.inject.Inject;

public class SyncOrgApplication extends Application {

    protected ApplicationDiComponent diComponent;

    private static SyncOrgApplication instance;
    SharedPreferences sharedPreferences;

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        OrgDatabase.startDB(this);

        diComponent = DaggerApplicationDiComponent.builder().build();
       // diComponent.inject(this);

        instance = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        OrgFileParser.startParser(this);
    }

    public ApplicationDiComponent getDiComponent() {
        return diComponent;
    }

}
