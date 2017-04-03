package com.coste.syncorg.orgdata;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.coste.syncorg.orgdata.OrgContract.OrgData;
import com.coste.syncorg.util.OrgFileNotFoundException;
import com.coste.syncorg.util.OrgNodeNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class OrgNode {

    long id = -1;
    long parentId = -1;
    long fileId = -1;

    long level = 0; // The headline level
    String priority = ""; // The priority tag
    String todo = "";    // The TODO state
    String tags = "";
    String tags_inherited = "";
    String displayName = "";
    // The ordering of the same level siblings
    int positionInParent = 0;
    private OrgNodeTimeDate deadline, scheduled;
    // The payload is a string containing the raw string corresponding to this mode
    private String payload = "";
    private OrgNodePayload orgNodePayload = null;

    public OrgNode() {
    }

    public OrgNode(OrgNode node) {
        this.level = node.level;
        this.priority = node.priority;
        this.todo = node.todo;
        this.tags = node.tags;
        this.tags_inherited = node.tags_inherited;
        this.displayName = node.displayName;
        this.positionInParent = node.positionInParent;
        this.scheduled = node.scheduled;
        this.deadline = node.deadline;
        setPayload(node.getPayload());
    }

    public OrgNode(long id, long parentId, long fileId, long level, String priority, String todo, String tags, String tags_inherited, String displayName, int positionInParent, String payload) {
        this.id = id;
        this.parentId = parentId;
        this.fileId = fileId;
        this.level = level;
        this.priority = priority;
        this.todo = todo;
        this.tags = tags;
        this.tags_inherited = tags_inherited;
        this.displayName = displayName;
        this.positionInParent = positionInParent;
        this.payload = payload;
    }

    public OrgNode(long id, ContentResolver resolver) throws OrgNodeNotFoundException {
        Cursor cursor = resolver.query(OrgData.buildIdUri(id),
                OrgData.DEFAULT_COLUMNS, null, null, null);
        if (cursor == null)
            throw new OrgNodeNotFoundException("Node with id \"" + id + "\" not found");

        if (!cursor.moveToFirst()) {
            cursor.close();
            throw new OrgNodeNotFoundException("Node with id \"" + id + "\" not found");
        }
      //  set(cursor);

        cursor.close();
    }


    public static boolean hasChildren(long node_id, ContentResolver resolver) {
        try {
            OrgNode node = new OrgNode(node_id, resolver);
            return node.hasChildren(resolver);
        } catch (OrgNodeNotFoundException e) {
        }

        return false;
    }

    private void setTimestamps() {
        deadline = new OrgNodeTimeDate(OrgNodeTimeDate.TYPE.Deadline, id);
        scheduled = new OrgNodeTimeDate(OrgNodeTimeDate.TYPE.Scheduled, id);
    }


    public String getFilename(ContentResolver resolver) {
        try {
            OrgFileOld file = new OrgFileOld(fileId, resolver);
            return file.filename;
        } catch (OrgFileNotFoundException e) {
            return "";
        }
    }

    public OrgFileOld getOrgFile(ContentResolver resolver) throws OrgFileNotFoundException {
        return new OrgFileOld(fileId, resolver);
    }

    public void setFilename(String filename, ContentResolver resolver) throws OrgFileNotFoundException {
        OrgFileOld file = new OrgFileOld(filename, resolver);
        this.fileId = file.nodeId;
    }

    private void preparePayload() {
        if (this.orgNodePayload == null)
            this.orgNodePayload = new OrgNodePayload(this.payload);
    }

    public void write(Context context) {
        if (id < 0)
            addNode(context);
        else
            updateNode(context);
    }

    /**
     * Generate the family tree with all descendants nodes and starting at the current one
     *
     * @return the ArrayList<OrgNode> containing all nodes
     */
    private ArrayList<OrgNode> getDescandants(ContentResolver resolver) {
        ArrayList<OrgNode> result = new ArrayList<OrgNode>();
        result.add(this);
        for (OrgNode child : getChildren(resolver)) result.addAll(child.getDescandants(resolver));
        return result;
    }

    private int updateNode(Context context) {
        // TODO: 30.03.17 refactor
        if (scheduled != null) {
            scheduled.update(context, id, fileId);
        }
        if (deadline != null) {
            deadline.update(context, id, fileId);
        }
      //  return context.getContentResolver().update(OrgData.buildIdUri(id), getContentValues(), null, null);
        return 0;
    }

    public boolean isHabit() {
        preparePayload();
        return orgNodePayload.getProperty("STYLE").equals("habit");
    }



    /**
     * This will split up the tag string that it got from the tag entry in the
     * database. The leading and trailing : are stripped out from the tags by
     * the parser. A double colon (::) means that the tags before it are
     * inherited.
     */
    public ArrayList<String> getTags() {
        ArrayList<String> result = new ArrayList<String>();

        if (tags == null)
            return result;

        String[] split = tags.split("\\:");

        Collections.addAll(result, split);

        if (tags.endsWith(":"))
            result.add("");

        return result;
    }

    ArrayList<OrgNode> getChildren(ContentResolver resolver) {
        return OrgProviderUtils.getOrgNodeChildren(id, resolver);
    }

    private ArrayList<String> getChildrenStringArray(ContentResolver resolver) {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<OrgNode> children = getChildren(resolver);

        for (OrgNode node : children)
            result.add(node.displayName);

        return result;
    }

    public OrgNode getChild(String name, ContentResolver resolver) throws OrgNodeNotFoundException {
        ArrayList<OrgNode> children = getChildren(resolver);

        for (OrgNode child : children) {
            if (child.displayName.equals(name))
                return child;
        }
        throw new OrgNodeNotFoundException("Couln't find child of node "
                + this.displayName + " with displayName " + name);
    }

    public boolean hasChildren(ContentResolver resolver) {
        Cursor childCursor = resolver.query(OrgData.buildChildrenUri(id),
                OrgData.DEFAULT_COLUMNS, null, null, null);

        int childCount = childCursor.getCount();
        childCursor.close();

        return childCount > 0;
    }

    private OrgNode getParent(ContentResolver resolver) throws OrgNodeNotFoundException {
        Cursor cursor = resolver.query(OrgData.buildIdUri(this.parentId),
                OrgData.DEFAULT_COLUMNS, null, null, null);
//        OrgNode parent = new OrgNode(cursor);
//        if (cursor != null) cursor.close();
//        return parent;
        return null;
        // TODO: 30.03.17 refactor
    }


    public ArrayList<String> getSiblingsStringArray(ContentResolver resolver) {
        try {
            OrgNode parent = getParent(resolver);
            return parent.getChildrenStringArray(resolver);
        } catch (OrgNodeNotFoundException e) {
            throw new IllegalArgumentException("Couldn't get parent for node " + displayName);
        }
    }

    private ArrayList<OrgNode> getSiblings(ContentResolver resolver) {
        try {
            OrgNode parent = getParent(resolver);
            return parent.getChildren(resolver);
        } catch (OrgNodeNotFoundException e) {
            throw new IllegalArgumentException("Couldn't get parent for node " + displayName);
        }
    }

    public void shiftNextSiblingNodes(Context context) {
        for (OrgNode sibling : getSiblings(context.getContentResolver())) {
            if (sibling.positionInParent >= positionInParent && sibling.id != this.id) {
                ++sibling.positionInParent;
                sibling.updateNode(context);
            }
        }
    }

    public OrgNodeTimeDate getDeadline() {
        return deadline;
    }

    public OrgNodeTimeDate getScheduled() {
        return scheduled;
    }

    /**
     * if scheduled and deadline are defined returns the number of seconds between them
     * else return -1
     */
    public long getRangeInSec() {
        long scheduled = this.scheduled.getEpochTime();
        if (scheduled < 0) return -1;
        long deadline = this.deadline.getEpochTime();
        if (deadline < 0) return -1;
        return Math.abs(deadline - scheduled);
    }

    public String getCleanedPayload() {
        preparePayload();
        return this.orgNodePayload.getCleanedPayload();
    }

    public String getPayload() {
        preparePayload();
        return this.orgNodePayload.get();
    }

    public void setPayload(String payload) {
        this.orgNodePayload = null;
        this.payload = payload;
    }

    public HashMap getPropertiesPayload() {
        preparePayload();
        return this.orgNodePayload.getPropertiesPayload();
    }

    public void addDate(OrgNodeTimeDate date) {
        this.orgNodePayload.insertOrReplaceDate(date);
        switch (date.getType()) {
            case Deadline:
                deadline = date;
                break;
            case Scheduled:
                scheduled = date;
                break;
        }
    }

    public OrgNodePayload getOrgNodePayload() {
        preparePayload();
        return this.orgNodePayload;
    }

    /**
     * Construct the correct level of indentation by appending one space per level.
     *
     * @param c: The characted used for the padding
     * @return
     */
    private String getLevelPadding(char c) {
        String result = "";
        if (level > 0) for (int i = 0; i < level; i++) result += c;
        result += " ";
        return result;
    }

    /**
     * Build the the plain text string corresponding to this node
     *
     * @return the node in plain text
     */
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(getLevelPadding('*'));

        if (!TextUtils.isEmpty(todo))
            result.append(todo).append(" ");

        if (!TextUtils.isEmpty(priority))
            result.append("[#" + priority + "] ");

        result.append(displayName);

        if (scheduled != null || deadline != null) {
            result.append("\n" + getLevelPadding(' '));
            if (scheduled != null) {
                result.append(scheduled.toFormatedString());
                if (deadline != null) result.append(" ");
            }
            if (deadline != null) result.append(deadline.toFormatedString());
        }

        if (tags != null && !TextUtils.isEmpty(tags))
            result.append(" ").append(":").append(tags).append(":");

        if (payload != null && !TextUtils.isEmpty(payload)) {
            for (String payload_line : payload.split("\\r?\\n")) {
                result.append("\n");
                result.append(payload_line);
            }
        }

        return result.toString();
    }


    public boolean equals(OrgNode node) {
        return displayName.equals(node.displayName) && tags.equals(node.tags)
                && priority.equals(node.priority) && todo.equals(node.todo)
                && payload.equals(node.payload);
    }

    /**
     * Delete this node and rewrite the file on disk
     */
    public void deleteNode(Context context) {
        context.getContentResolver().delete(OrgData.buildIdUri(id), null, null);
        OrgFileOld.updateFile(this, context);
    }

    /**
     * Add this node and rewrite the file on disk
     */
    private long addNode(Context context) {
        //TODO refactor

//        Uri uri = context.getContentResolver().insert(OrgData.CONTENT_URI, getContentValues());
//        this.id = Long.parseLong(OrgData.getId(uri));
//        if (scheduled != null) {
//            scheduled.update(context, id, fileId);
//        }
//        if (deadline != null) {
//            deadline.update(context, id, fileId);
//        }
//        OrgFileOld.updateFile(this, context);
        return id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = level;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = todo;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags_inherited() {
        return tags_inherited;
    }

    public void setTags_inherited(String tags_inherited) {
        this.tags_inherited = tags_inherited;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getPositionInParent() {
        return positionInParent;
    }

    public void setPositionInParent(int positionInParent) {
        this.positionInParent = positionInParent;
    }
}
