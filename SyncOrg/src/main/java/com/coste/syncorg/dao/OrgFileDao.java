package com.coste.syncorg.dao;

import com.coste.syncorg.orgdata.OrgDatabase;
import com.coste.syncorg.orgdata.OrgFileNew;
import com.coste.syncorg.orgdata.table.FileEntity;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.data.TableModel;
import com.yahoo.squidb.sql.Query;

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


}
