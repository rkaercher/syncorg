package com.coste.syncorg.dao;


import android.database.Cursor;

import com.coste.syncorg.orgdata.OrgDatabase;
import com.coste.syncorg.orgdata.OrgNode;
import com.coste.syncorg.orgdata.table.OrgNodeEntity;
import com.coste.syncorg.orgdata.table.TodoTypeEntity;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.sql.Property;
import com.yahoo.squidb.sql.Query;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OrgNodeDao {
    private OrgDatabase db;

    @Inject
    OrgNodeDao(OrgDatabase db) {
        this.db = db;
    }

    public OrgNodeEntity save(OrgNodeEntity entity) {
        if (this.db.persist(entity)) {
            return entity;
        }
        return null;
    }

    public OrgNode findById(long nodeId) {

        List<Property<?>> properties = OrgNodeEntity.PROPERTIES;
        properties.add(TodoTypeEntity.KEYWORD);
        SquidCursor<OrgNodeEntity> cursor = db.query(OrgNodeEntity.class, Query.select(properties).leftJoin(TodoTypeEntity.TABLE, OrgNodeEntity.TODO_ID.eq(TodoTypeEntity.ID)));


        if (cursor.moveToFirst()) {
            OrgNodeEntity entity = new OrgNodeEntity();
            entity.readPropertiesFromCursor(cursor);
            String tags = "";
            String tagsInherited = "";
            String todoKeyword = entity.get(TodoTypeEntity.KEYWORD);
            if (todoKeyword == null) {
                todoKeyword = "";
            }
            OrgNode result = new OrgNode(entity.getId(), entity.getParentId(), entity.getFileId(), entity.getLevel(), "#A", todoKeyword, tags, tagsInherited, entity.getDisplayName(), entity.getPositionInParent(), entity.getPayload());
            return result;
        }
        return null;
    }

    public List<OrgNode> findTodoNodes() {
//        String t1 = OrgDatabase.Tables.ORGDATA;
//        String t2 = OrgDatabase.Tables.TODOS;
//        String t3 = FilterTable.name;
//        String t4 = FilterEntrySpec.name;
//        String sql = "Select " + OrgContract.formatColumns(t1, OrgContract.OrgData.DEFAULT_COLUMNS)
//                + " FROM " + t1 + "," + t2 + "," + t3 + "," + t4 + " WHERE " + t1 + ".TODO=" + t2 + "."
//                + OrgContract.Todos.NAME + " AND " + t2 + "." + OrgContract.Todos.ISDONE + "=0 AND "
//                + t3 + ".type=" + NodeFilter.FilterType.TODO.ordinal() + " AND " + t3 + "._id="
//                + t4 + ".filter_id AND " + t1 + ".file_id=" + t4 + ".file_id";
//
//        Cursor cursor = db.getReadableDatabase().rawQuery(sql, null);
//
//        List<OrgNode> result = new ArrayList<>(cursor.getCount());
//        if (cursor.getCount() > 0) {
//            while (!cursor.isLast()) {
//                cursor.moveToNext();
//                result.add(nodeFromRow(cursor));
//            }
//        }
//        cursor.close();
//FIXME impl1
        return null;
    }

    private OrgNode nodeFromRow(Cursor cursor) {
        int idx = 0;
        Long id = cursor.getLong(idx++);
        String name = cursor.getString(idx++);
        String todo = cursor.getString(idx++);
        String tags = cursor.getString(idx++);
        String tagsInherited = cursor.getString(idx++);
        Long parentId = cursor.getLong(idx++);
        String payload = cursor.getString(idx++);
        Long level = cursor.getLong(idx++);
        String priority = cursor.getString(idx++);
        Long fileId = cursor.getLong(idx++);
        Integer position = cursor.getInt(idx);

        return new OrgNode(id, parentId, fileId, level, priority, todo, tags, tagsInherited, name, position, payload);
    }
}
