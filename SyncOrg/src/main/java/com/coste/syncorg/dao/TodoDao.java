package com.coste.syncorg.dao;

import com.coste.syncorg.orgdata.OrgDatabase;
import com.coste.syncorg.orgdata.table.TodoTypeEntity;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.sql.Query;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TodoDao {

    private OrgDatabase db;

    @Inject
    TodoDao(OrgDatabase db) {
        this.db = db;
    }

    public TodoTypeEntity save(TodoTypeEntity entity) {
        if (db.upsert(entity)) {
            return entity;
        }
        return null;
    }

    public Map<String, Long> getTodoIdMappings() {
        Map<String, Long> result = new HashMap<>();
        SquidCursor<TodoTypeEntity> cursor = db.query(TodoTypeEntity.class, Query.select(TodoTypeEntity.ID, TodoTypeEntity.KEYWORD));
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            result.put(cursor.get(TodoTypeEntity.KEYWORD), cursor.get(TodoTypeEntity.ID));
            cursor.moveToNext();
        }

        return result;
    }
}
