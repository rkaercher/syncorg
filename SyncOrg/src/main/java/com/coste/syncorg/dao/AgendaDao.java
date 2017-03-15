package com.coste.syncorg.dao;


import com.coste.syncorg.orgdata.OrgDatabase;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AgendaDao {

    private OrgDatabase db;

    @Inject
    public AgendaDao(OrgDatabase db) {
        this.db = db;
    }
}
