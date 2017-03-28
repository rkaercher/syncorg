package com.coste.syncorg.orgdata.table;

import com.yahoo.squidb.annotations.TableModelSpec;
import com.yahoo.squidb.annotations.tables.ColumnName;
import com.yahoo.squidb.annotations.tables.constraints.PrimaryKey;

@TableModelSpec(className = "OrgNodeEntity", tableName = "node")
class OrgNodeSpec {
    @PrimaryKey
    @ColumnName("_id")
    Long id;
    Long parentId;
    long fileId;
    int level;
    //TODO: priority
    //TODO: tagsInherited
    String payload;
    String displayName;
    int positionInParent;
    String comment;

    Long todoId;
}
