package com.coste.syncorg.orgdata.table;

import com.yahoo.squidb.annotations.TableModelSpec;
import com.yahoo.squidb.annotations.tables.ColumnName;
import com.yahoo.squidb.annotations.tables.constraints.PrimaryKey;

@TableModelSpec(className = "TaggedByEntity", tableName = "tagged_by")
public class TaggedBySpec {

    @PrimaryKey
    @ColumnName("_id")
    Long id;

    Long tagId;
    Long nodeId;

    boolean inherited;

}

