package com.coste.syncorg.orgdata.table;


public class FilterEntryTable  {

    public static final String name = "filter_entry";

    public static final String FIELD_ID = "_id";
    public static final String FIELD_FILE_ID = "file_id";
    public static final String FIELD_FILTER_ID = "filter_id";

    public static String getCreateDDL() {
        return "CREATE TABLE IF NOT EXISTS filter_entry ("
                + "_id integer primary key autoincrement,"
                + "file_id integer NOT NULL,"
                + "filter_id integer,"
                + "FOREIGN KEY (filter_id) REFERENCES filter(_id));";
    }
}
