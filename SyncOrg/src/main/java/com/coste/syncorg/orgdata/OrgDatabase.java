package com.coste.syncorg.orgdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.coste.syncorg.orgdata.table.*;
import com.yahoo.squidb.android.AndroidOpenHelper;
import com.yahoo.squidb.data.ISQLiteDatabase;
import com.yahoo.squidb.data.ISQLiteOpenHelper;
import com.yahoo.squidb.data.SquidDatabase;
import com.yahoo.squidb.sql.Table;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import static com.coste.syncorg.orgdata.OrgDatabase.Tables.ALL_TABLES;

@Singleton
public class OrgDatabase extends SquidDatabase {
    private static final String DATABASE_NAME = "SyncOrg.db";
    static final int DATABASE_VERSION = 8;
    private static OrgDatabase mInstance = null;

    private SQLiteStatement orgdataInsertStatement;
    private SQLiteStatement addPayloadStatement;
    private SQLiteStatement addTimestampsStatement;
    private Context context;

    public OrgDatabase(Context context) {

//        super(context, DATABASE_NAME, null, DATABASE_VERSION);

//        orgdataInsertStatement = getWritableDatabase()
//                .compileStatement("INSERT INTO " + Tables.ORGDATA + " ("
//                        + OrgData.NAME + ", "
//                        + OrgData.TODO + ", "
//                        + OrgData.PRIORITY + ", "
//                        + OrgData.PARENT_ID + ", "
//                        + OrgData.FILE_ID + ", "
//                        + OrgData.TAGS + ", "
//                        + OrgData.TAGS_INHERITED + ", "
//                        + OrgData.LEVEL + ", "
//                        + OrgData.POSITION + ") "
//                        + "VALUES (?,?,?,?,?,?,?,?,?)");
//
//        addPayloadStatement = getWritableDatabase()
//                .compileStatement("UPDATE " + Tables.ORGDATA + " SET payload=? WHERE _id=?");
//        addTimestampsStatement = getWritableDatabase()
//                .compileStatement("INSERT INTO " + Tables.TIMESTAMPS + " (timestamp, file_id, node_id, type, all_day) VALUES (?,?,?,?,?) ");
        this.context = context;
    }



    static void instantiateDb(Context context) {
        mInstance = new OrgDatabase(context);
    }

    public static OrgDatabase getInstance() {
        return mInstance;
    }

    public void onCreate(ISQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS files ("
                + "_id integer primary key autoincrement,"
                + "node_id integer,"
                + "filename text,"
                + "displayName text,"
                + "comment text,"
                + "time_modified integer default 0,"
                + "UNIQUE(filename) ON CONFLICT IGNORE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS todos("
                + "_id integer primary key autoincrement,"
                + "todogroup integer,"
                + "displayName text,"
                + "isdone integer default 0,"
                + "UNIQUE(todogroup, displayName) ON CONFLICT IGNORE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS priorities("
                + "_id integer primary key autoincrement,"
                + "displayName text)");
        db.execSQL("CREATE TABLE IF NOT EXISTS tags("
                + "_id integer primary key autoincrement,"
                + "taggroup integer,"
                + "displayName text)");
        db.execSQL("CREATE TABLE IF NOT EXISTS orgdata ("
                + "_id integer primary key autoincrement,"
                + "parent_id integer default -1,"
                + "file_id integer,"
                + "level integer default 0,"
                + "priority text,"
                + "todo text,"
                + "tags text,"
                + "tags_inherited text,"
                + "payload text,"
                + "displayName text,"
                + "positionInParent integer,"
                + "scheduled integer default -1,"
                + "scheduled_date_only integer default 0,"
                + "deadline integer default -1,"
                + "deadline_date_only integer default 0)");
        db.execSQL("CREATE TABLE IF NOT EXISTS timestamps ("
                + "file_id integer,"
                + "timestamp ,"
                + "type integer,"
                + "node_id integer,"
                + "all_day integer)");


        ContentValues values = new ContentValues();
        values.put("_id", "0");
        values.put("todogroup", "0");
        values.put("displayName", "TODO");
        values.put("isdone", "0");
    //    db.insert("todos", null, values);

        values.put("_id", "1");
        values.put("todogroup", "0");
        values.put("displayName", "DONE");
        values.put("isdone", "1");
      //  db.insert("todos", null, values);

    }

    @Override
    protected boolean onUpgrade(ISQLiteDatabase db, int oldVersion, int newVersion) {
        switch (newVersion) {
            case 6:
                db.execSQL("ALTER TABLE " + Tables.FILES + " ADD time_modified integer default 0");
                break;
            case 7:
                for (String table : ALL_TABLES) {
                   // db.delete(table, null, null);
                }
           //     onCreate(db);
                break;
            case 8:

        }
        return true;
    }

    long fastInsertNode(OrgNode node) {
        orgdataInsertStatement.bindString(1, node.displayName);
        orgdataInsertStatement.bindString(2, node.todo);
        orgdataInsertStatement.bindString(3, node.priority);
        orgdataInsertStatement.bindLong(4, node.parentId);
        orgdataInsertStatement.bindLong(5, node.fileId);
        orgdataInsertStatement.bindString(6, node.tags);
        orgdataInsertStatement.bindString(7, node.tags_inherited);
        orgdataInsertStatement.bindLong(8, node.level);
        orgdataInsertStatement.bindLong(9, node.positionInParent);

        return orgdataInsertStatement.executeInsert();
    }

    void fastInsertTimestamp(Long id, Long fileId, final HashMap<OrgNodeTimeDate.TYPE, OrgNodeTimeDate> timestamps) {
        for (Map.Entry<OrgNodeTimeDate.TYPE, OrgNodeTimeDate> entry : timestamps.entrySet()) {
            OrgNodeTimeDate timeDate = entry.getValue();
            if (timeDate.getEpochTime() < 0) continue;

            addTimestampsStatement.bindLong(1, timeDate.getEpochTime());
            addTimestampsStatement.bindLong(2, fileId);
            addTimestampsStatement.bindLong(3, id);
            addTimestampsStatement.bindLong(4, entry.getKey().ordinal());
            addTimestampsStatement.bindLong(5, timeDate.isAllDay());
            addTimestampsStatement.executeInsert();
        }
    }

    void fastInsertNodePayload(Long id, final String payload) {

        addPayloadStatement.bindString(1, payload);
        addPayloadStatement.bindLong(2, id);
        addPayloadStatement.execute();
    }

    @Override
    public String getName() {
        return DATABASE_NAME;
    }

    @Override
    protected int getVersion() {
        return DATABASE_VERSION;
    }

    @Override
    protected Table[] getTables() {
        return new Table[] {FilterEntity.TABLE, FilterEntryEntity.TABLE, FileEntity.TABLE, OrgNodeEntity.TABLE, TagEntity.TABLE, TaggedByEntity.TABLE, TimestampEntity.TABLE, TodoTypeEntity.TABLE};
    }

    @Override
    protected ISQLiteOpenHelper createOpenHelper(String databaseName, OpenHelperDelegate delegate, int version) {
        return new AndroidOpenHelper(this.context, databaseName,delegate, version);
    }

    public SQLiteDatabase getReadableDatabase() {
        return (SQLiteDatabase) this.getDatabase().getWrappedObject();
    }

    public SQLiteDatabase getWritableDatabase() {
        return (SQLiteDatabase) this.getDatabase().getWrappedObject();
    }

    public interface Tables {
        String TIMESTAMPS = "timestamps";
        String FILES = "files";
        String PRIORITIES = "priorities";
        String TAGS = "tags";
        String TODOS = "todos";
        String ORGDATA = "orgdata";

        String[] ALL_TABLES = {TIMESTAMPS, FILES, PRIORITIES, TAGS, TODOS, ORGDATA};
    }

}
