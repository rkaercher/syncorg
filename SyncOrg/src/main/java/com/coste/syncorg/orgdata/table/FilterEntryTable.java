package com.coste.syncorg.orgdata.table;


import com.yahoo.squidb.annotations.tables.ColumnName;
import com.yahoo.squidb.annotations.tables.constraints.ConstraintSql;
import com.yahoo.squidb.annotations.tables.constraints.PrimaryKey;
import com.yahoo.squidb.annotations.TableModelSpec;

@TableModelSpec(className = "FilterEntryEntity", tableName = "filter_entry")
@ConstraintSql("FOREIGN KEY (filterId) REFERENCES filter(_id)")
public class FilterEntryTable  {
    @PrimaryKey
    @ColumnName("_id")
    Long id;
    Long fileId;
    Long filterId;
}
