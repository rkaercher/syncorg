package com.coste.syncorg.orgdata.table;

import com.yahoo.squidb.annotations.TableModelSpec;
import com.yahoo.squidb.annotations.tables.ColumnName;
import com.yahoo.squidb.annotations.tables.UpsertKey;
import com.yahoo.squidb.annotations.tables.constraints.NotNull;
import com.yahoo.squidb.annotations.tables.constraints.PrimaryKey;
import com.yahoo.squidb.annotations.tables.constraints.Unique;

@TableModelSpec(className = "TodoTypeEntity", tableName = "todo_type")
class TodoTypeSpec {
    @PrimaryKey
    @ColumnName("_id")
    Long id;
    @UpsertKey
    @NotNull
    @Unique
    String keyword;
    boolean inactive;
}