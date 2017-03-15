package com.coste.syncorg.util.di;


import com.coste.syncorg.dao.FilterDao;
import com.coste.syncorg.orgdata.OrgDatabase;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    @Provides
    static OrgDatabase provideOrgDatabase() {
        return OrgDatabase.getInstance();
    }

//    @Provides
//    static FilterDao provideFilterDao(OrgDatabase db) {
//        return new FilterDao(db);
//    }
}
