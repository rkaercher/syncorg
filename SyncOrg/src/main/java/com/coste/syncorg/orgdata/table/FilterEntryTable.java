package com.coste.syncorg.orgdata.table;


import com.yahoo.squidb.annotations.PrimaryKey;
import com.yahoo.squidb.annotations.TableModelSpec;

@TableModelSpec(className = "FilterEntryEntity", tableName = "filter_entry", tableConstraint = "FOREIGN KEY (filterId) REFERENCES filter(_id)")
public class FilterEntryTable  {
    @PrimaryKey
    Long id;
    String fileId;
    String filterId;
}
