package com.coste.syncorg.dao;


import android.text.TextUtils;

import com.coste.syncorg.orgdata.NodeFilter;
import com.coste.syncorg.orgdata.OrgDatabase;
import com.coste.syncorg.orgdata.table.FilterEntity;
import com.coste.syncorg.orgdata.table.FilterEntryEntity;

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
        db.beginTransaction();
        try {

            FilterEntity filterEntity = null;
            if (filter.getFilterId() != null) {
                filterEntity = db.fetchByCriterion(FilterEntity.class, FilterEntity.ID.is(filter.getFilterId()));
                db.deleteWhere(FilterEntryEntity.class, FilterEntryEntity.FILTER_ID.is(filter.getFilterId()));
            }
            if (filterEntity == null) {
                filterEntity = new FilterEntity();
            }

            filterEntity.setName(filter.getName());
            //TODO implement
//        filterEntity.setComment()
            db.persist(filterEntity);

            for (Long fileId : filter.getIncludedNodeIds()) {
                db.persist(new FilterEntryEntity().
                        setFileId(fileId).
                        setFilterId(filterEntity.getId()));
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }


        return true;
    }


    public NodeFilter loadFilter(NodeFilter.FilterType type) {
//        Cursor cursor = db.getReadableDatabase().query(FilterTable.name, new String[]{FilterTable.FIELD_ID, FilterTable.FIELD_NAME}, and(fieldSelection(FilterTable.FIELD_TYPE), fieldSelection(FilterTable.FIELD_NAME)), new String[]{String.valueOf(type.ordinal()), "DEFAULT"}, null, null, null);
//        if (cursor.getCount() == 0) {
//            return new NodeFilter(type);
//        }
//
//        Set<Long> includedIds = new HashSet<>();
//        cursor.moveToNext();
//        Integer filterId = cursor.getInt(0);
//        cursor.close();
//
//        cursor = db.getReadableDatabase().query(FilterEntrySpec.name, new String[]{FilterEntrySpec.FIELD_FILE_ID}, fieldSelection(FilterEntrySpec.FIELD_FILTER_ID), new String[]{filterId.toString()}, null, null, null);
//
//        if (cursor.getCount() > 0) {
//            while (!cursor.isLast()) {
//                cursor.moveToNext();
//                includedIds.add(cursor.getLong(0));
//            }
//        }
//        cursor.close();
//
//        return new NodeFilter(filterId, includedIds, NodeFilter.DEFAULT, type);
        return null;
    }

    protected String and(String... selections) {
        return TextUtils.join(" AND ", selections);
    }

    protected String fieldSelection(String field) {
        return String.format("%s=?", field);
    }
}
