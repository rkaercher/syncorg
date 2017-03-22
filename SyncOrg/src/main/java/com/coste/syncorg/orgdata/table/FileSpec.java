package com.coste.syncorg.orgdata.table;

import com.yahoo.squidb.annotations.PrimaryKey;
import com.yahoo.squidb.annotations.TableModelSpec;

import org.joda.time.DateTime;

@TableModelSpec(className = "FileEntity", tableName = "file", tableConstraint = "UNIQUE (fileName) ON CONFLICT IGNORE")
public class FileSpec {
    @PrimaryKey
    Long id;
    Long rootNodeId;
    String fileName;
    String displayName;
    String comment;
    DateTime created;
    DateTime lastModified;
}
