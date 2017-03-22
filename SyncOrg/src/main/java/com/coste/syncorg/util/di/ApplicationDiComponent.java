package com.coste.syncorg.util.di;


import android.app.Activity;

import com.coste.syncorg.AgendaFragment;
import com.coste.syncorg.MainActivity;
import com.coste.syncorg.OrgNodeDetailFragment;
import com.coste.syncorg.gui.FileDecryptionActivity;
import com.coste.syncorg.gui.filter.FilterActivity;
import com.coste.syncorg.gui.outline.ConflictResolverActivity;
import com.coste.syncorg.gui.outline.MainAdapter;
import com.coste.syncorg.gui.wizard.wizards.NoSyncWizard;
import com.coste.syncorg.gui.wizard.wizards.SSHWizard;
import com.coste.syncorg.util.di.ApplicationModule;
import com.coste.syncorg.orgdata.SyncOrgApplication;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationDiComponent {
    void inject(FilterActivity activity);

    void inject(AgendaFragment fragment);

    void inject(OrgNodeDetailFragment fragment);

    void inject(FileDecryptionActivity fileDecryptionActivity);

    void inject(SSHWizard sshWizard);

    void inject(MainActivity mainActivity);

    void inject(NoSyncWizard noSyncWizard);

    void inject(ConflictResolverActivity conflictResolverActivity);

    void inject(MainAdapter mainAdapter);
}
