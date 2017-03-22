package com.coste.syncorg.orgdata.table;

import com.yahoo.squidb.annotations.PrimaryKey;
import com.yahoo.squidb.annotations.TableModelSpec;

@TableModelSpec(className = "TodoTypeEntity", tableName = "todo_type")
public class TodoTypeSpec {
    @PrimaryKey
    Long id;
    String keyword;
    boolean inactive;
}