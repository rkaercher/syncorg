package com.coste.syncorg.orgdata;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.coste.syncorg.util.di.ApplicationDiComponent;
import com.coste.syncorg.util.di.ApplicationModule;
import com.coste.syncorg.util.di.DaggerApplicationDiComponent;

import net.danlew.android.joda.JodaTimeAndroid;

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
        JodaTimeAndroid.init(this);

        OrgDatabase.instantiateDb(this);

        diComponent = DaggerApplicationDiComponent.builder().applicationModule(new ApplicationModule(this)).build();

        instance = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public ApplicationDiComponent getDiComponent() {
        return diComponent;
    }

}
