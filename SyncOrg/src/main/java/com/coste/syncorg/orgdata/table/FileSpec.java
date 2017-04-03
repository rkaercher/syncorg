package com.coste.syncorg.orgdata.table;

import com.yahoo.squidb.annotations.tables.ColumnName;
import com.yahoo.squidb.annotations.tables.UpsertKey;
import com.yahoo.squidb.annotations.tables.constraints.ConstraintSql;
import com.yahoo.squidb.annotations.tables.constraints.NotNull;
import com.yahoo.squidb.annotations.tables.constraints.PrimaryKey;
import com.yahoo.squidb.annotations.TableModelSpec;
import com.yahoo.squidb.annotations.tables.constraints.Unique;

import org.joda.time.DateTime;

@TableModelSpec(className = "FileEntity", tableName = "file")
@ConstraintSql("UNIQUE (filePath) ON CONFLICT IGNORE")
class FileSpec {
    @PrimaryKey
    @ColumnName("_id")
    Long id;
    Long rootNodeId;
    @UpsertKey
    @NotNull
    @Unique
    String filePath;
    String displayName;
    String comment;
    DateTime created;
    DateTime lastModified;
}
