package com.coste.syncorg.orgdata.table;

import com.yahoo.squidb.annotations.TableModelSpec;
import com.yahoo.squidb.annotations.tables.ColumnName;
import com.yahoo.squidb.annotations.tables.UpsertKey;
import com.yahoo.squidb.annotations.tables.constraints.NotNull;
import com.yahoo.squidb.annotations.tables.constraints.PrimaryKey;
import com.yahoo.squidb.annotations.tables.constraints.Unique;

@TableModelSpec(className = "TagEntity", tableName = "tag")
class TagSpec {
    @PrimaryKey
    @ColumnName("_id")
    Long id;
    @UpsertKey
    @NotNull
    @Unique
    String name;
}
