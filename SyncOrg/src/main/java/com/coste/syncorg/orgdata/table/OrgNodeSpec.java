package com.coste.syncorg.orgdata.table;

import com.yahoo.squidb.annotations.ModelMethod;
import com.yahoo.squidb.annotations.PrimaryKey;
import com.yahoo.squidb.annotations.TableModelSpec;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

@TableModelSpec(className = "OrgNodeEntity", tableName = "node")
public class OrgNodeSpec {
    @PrimaryKey
    Long id;
    Long parentId;
    long fileId;
    int level;
    //TODO: priority
    //TODO: tagsInherited
    String payload;
    String displayName;
    int positionInFile;
    LocalDateTime scheduledStartDateTime;
    LocalTime scheduledEndTime;
    boolean hasScheduledEndTime;
    LocalDate deadlineDateTime;
    boolean hasDeadlineTime;

//TODO: put in BO
//    private TodoType todo;
//    private List<OrgTag> tags = new ArrayList<>();
//
//    @ModelMethod
//    public List<OrgTag> getTags() {
//        return tags;
//    }
//
//    @ModelMethod
//    public void setTags(List<OrgTag> tags) {
//        this.tags = tags;
//    }
//
//    @ModelMethod
//    public TodoType getTodo() {
//        return todo;
//    }
//
//    @ModelMethod
//    public void setTodo(TodoType todo) {
//        this.todo = todo;
//    }
}
