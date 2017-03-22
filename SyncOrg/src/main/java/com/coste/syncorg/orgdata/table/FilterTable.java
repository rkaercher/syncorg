package com.coste.syncorg.orgdata.table;


import com.coste.syncorg.orgdata.NodeFilter;
import com.yahoo.squidb.annotations.PrimaryKey;
import com.yahoo.squidb.annotations.TableModelSpec;

@TableModelSpec(className = "FilterEntity", tableName = "filter")
public class FilterTable {
    @PrimaryKey
    Long id;
    String name;
    String comment;
    NodeFilter.FilterType type;
}