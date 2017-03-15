package com.coste.syncorg.util;


import android.app.Activity;

import com.coste.syncorg.AgendaFragment;
import com.coste.syncorg.gui.filter.FilterActivity;
import com.coste.syncorg.util.di.ApplicationModule;
import com.coste.syncorg.orgdata.SyncOrgApplication;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationDiComponent {
    //void inject(SyncOrgApplication application);

    void inject(FilterActivity activity);

    void inject(AgendaFragment fragment);
}
