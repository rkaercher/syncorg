package com.coste.syncorg.dao;


import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.coste.syncorg.orgdata.NodeFilter;
import com.coste.syncorg.orgdata.OrgDatabase;
import com.coste.syncorg.orgdata.table.FilterEntryTable;
import com.coste.syncorg.orgdata.table.FilterTable;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FilterDao {

    private OrgDatabase db;

    @Inject
    FilterDao(OrgDatabase db) {
        this.db = db;
    }

    public boolean saveFilter(NodeFilter filter) {
        ContentValues values = new ContentValues();
        values.put(FilterTable.FIELD_NAME, filter.getName());
        values.put(FilterTable.FIELD_TYPE, filter.getType().ordinal());

        long executionResult = -1;

        final long filterId;

        try {
            db.getWritableDatabase().beginTransaction();
            if (filter.getFilterId() != null) {
                filterId = filter.getFilterId();
                values.put(FilterTable.FIELD_ID, filter.getFilterId());
                executionResult = db.getWritableDatabase().update(FilterTable.name, values, fieldSelection(FilterTable.FIELD_ID), new String[]{filter.getFilterId().toString()});
                db.getWritableDatabase().delete(FilterEntryTable.name, fieldSelection(FilterEntryTable.FIELD_FILTER_ID), new String[]{filter.getFilterId().toString()});
            } else {
                filterId = db.getWritableDatabase().insert(FilterTable.name, null, values);
            }
            saveFilterFileIds(filter, filterId);
            db.getWritableDatabase().setTransactionSuccessful();
        } finally {
            db.getWritableDatabase().endTransaction();
        }

        return executionResult > -1; // TODO make this meaningful
    }

    private void saveFilterFileIds(NodeFilter filter, long filterId) {
        ContentValues values = new ContentValues(2);
        values.put(FilterEntryTable.FIELD_FILTER_ID, filterId);

        for (Long id : filter.getIncludedNodeIds()) {
            values.put(FilterEntryTable.FIELD_FILE_ID, id);
            db.getWritableDatabase().insert(FilterEntryTable.name, null, values);
        }
    }

    public NodeFilter loadFilter(NodeFilter.FilterType type) {
        Cursor cursor = db.getReadableDatabase().query(FilterTable.name, new String[]{FilterTable.FIELD_ID, FilterTable.FIELD_NAME}, and(fieldSelection(FilterTable.FIELD_TYPE), fieldSelection(FilterTable.FIELD_NAME)), new String[]{String.valueOf(type.ordinal()), "DEFAULT"}, null, null, null);
        if (cursor.getCount() == 0) {
            return new NodeFilter(type);
        }

        Set<Long> includedIds = new HashSet<>();
        cursor.moveToNext();
        Integer filterId = cursor.getInt(0);
        cursor.close();

        cursor = db.getReadableDatabase().query(FilterEntryTable.name, new String[]{FilterEntryTable.FIELD_FILE_ID}, fieldSelection(FilterEntryTable.FIELD_FILTER_ID), new String[]{filterId.toString()}, null, null, null);

        while (!cursor.isLast()) {
            cursor.moveToNext();
            includedIds.add(cursor.getLong(0));
        }
        cursor.close();

        return new NodeFilter(filterId, includedIds, NodeFilter.DEFAULT, type);
    }

    protected String and(String... selections) {
        return TextUtils.join(" AND ", selections);
    }

    protected String fieldSelection(String field) {
        return String.format("%s=?", field);
    }
}
