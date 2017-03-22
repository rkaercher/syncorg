package com.coste.syncorg.dao;

import com.coste.syncorg.orgdata.OrgDatabase;
import com.coste.syncorg.orgdata.OrgFileNew;
import com.yahoo.squidb.data.TableModel;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OrgFileDao {

    private OrgDatabase db;

    @Inject
    OrgFileDao(OrgDatabase db) {
        this.db = db;
    }

    public boolean save(OrgFileNew orgFile) {
        db.persist(getOrgFileEntity(orgFile));
        return true;
    }

    private TableModel getOrgFileEntity(OrgFileNew orgFile) {
        return null;
    }
}
