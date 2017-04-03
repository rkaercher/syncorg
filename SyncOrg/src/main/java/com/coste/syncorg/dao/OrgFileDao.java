package com.coste.syncorg.dao;

import com.coste.syncorg.orgdata.OrgDatabase;
import com.coste.syncorg.orgdata.OrgFileNew;
import com.coste.syncorg.orgdata.table.FileEntity;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.data.TableModel;
import com.yahoo.squidb.sql.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OrgFileDao {

    private OrgDatabase db;

    @Inject
    OrgFileDao(OrgDatabase db) {
        this.db = db;
    }

    public FileEntity save(FileEntity orgFile) {
        //// TODO: 3/26/17 use upsert here
      Query query =  Query.select(FileEntity.ID).from(FileEntity.TABLE).where(FileEntity.FILE_PATH.is(orgFile.getFilePath()));
        SquidCursor<FileEntity> cursor = db.query(FileEntity.class, query);
        if (cursor.moveToFirst()) {
            orgFile.setId(cursor.get(FileEntity.ID));
        }
        db.persist(orgFile);
        return orgFile;
    }

    public List<FileEntity> getFiles() {
        List<FileEntity> result = new ArrayList<>();
        SquidCursor<FileEntity> cursor = db.query(FileEntity.class, Query.select(FileEntity.PROPERTIES));
        for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
            result.add(new FileEntity(cursor));
        }
        return result;
    }

}
