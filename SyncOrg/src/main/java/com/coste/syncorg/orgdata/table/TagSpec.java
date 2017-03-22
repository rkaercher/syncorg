package com.coste.syncorg.orgdata.table;

import com.yahoo.squidb.annotations.PrimaryKey;
import com.yahoo.squidb.annotations.TableModelSpec;

@TableModelSpec(className = "TagEntity", tableName = "tag")
public class TagSpec {
    @PrimaryKey
    Long id;
    String name;
}
