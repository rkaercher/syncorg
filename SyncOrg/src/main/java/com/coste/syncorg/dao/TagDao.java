package com.coste.syncorg.dao;

import com.coste.syncorg.orgdata.OrgDatabase;
import com.coste.syncorg.orgdata.table.TagEntity;
import com.coste.syncorg.orgdata.table.TaggedByEntity;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TagDao {

    private OrgDatabase db;

    @Inject
    TagDao(OrgDatabase db) {
        this.db = db;
    }

    public Long getIdForTag(String tag) {
        TagEntity tagEntity = new TagEntity().
                setName(tag);
        db.upsert(tagEntity);
        return tagEntity.getId();
    }

    public boolean tagNodeWith(long nodeId, String tag, boolean isInherited) {
        TaggedByEntity taggedByEntity = new TaggedByEntity().
                setNodeId(nodeId).
                setTagId(getIdForTag(tag)).
                setIsInherited(isInherited);
        return db.persist(taggedByEntity);
    }

    public int deleteTagsForNode(long nodeId) {
        return db.deleteWhere(TaggedByEntity.class, TaggedByEntity.NODE_ID.is(nodeId));
    }
}
