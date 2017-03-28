package com.coste.syncorg.orgdata.table;


import com.coste.syncorg.orgdata.NodeFilter;

import com.yahoo.squidb.annotations.TableModelSpec;
import com.yahoo.squidb.annotations.tables.ColumnName;
import com.yahoo.squidb.annotations.tables.constraints.PrimaryKey;

@TableModelSpec(className = "FilterEntity", tableName = "filter")
public class FilterTable {
    @PrimaryKey
    @ColumnName("_id")
    Long id;
    String name;
    String comment;
    NodeFilter.FilterType type;
}
