package com.coste.syncorg.orgdata.table;

import com.coste.syncorg.orgdata.TimestampType;
import com.yahoo.squidb.annotations.TableModelSpec;
import com.yahoo.squidb.annotations.tables.ColumnName;
import com.yahoo.squidb.annotations.tables.constraints.ConstraintSql;
import com.yahoo.squidb.annotations.tables.constraints.PrimaryKey;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

@TableModelSpec(className = "TimestampEntity", tableName = "timestamp")
@ConstraintSql("FOREIGN KEY (nodeId) REFERENCES node(_id)")
class TimestampSpec {
    @PrimaryKey
    @ColumnName("_id")
    Long id;

    Long nodeId;

    LocalDate startDate;
    LocalTime startTime;
    LocalDate endDate;
    LocalTime endTime;

    boolean inActive;

    TimestampType type;
}
