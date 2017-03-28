package com.coste.syncorg.dao;

import com.coste.syncorg.orgdata.OrgDatabase;
import com.coste.syncorg.orgdata.table.TimestampEntity;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TimestampDao {
    private OrgDatabase db;

    @Inject
    TimestampDao(OrgDatabase db) {
        this.db = db;
    }

    public TimestampEntity save(TimestampEntity entity) {
        if (this.db.persist(entity)) {
            return entity;
        }
        return null;
    }
}
