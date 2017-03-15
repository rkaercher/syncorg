package com.coste.syncorg.orgdata.table;



public class FilterTable {

    public static final String name = "filter";

    public static final String FIELD_ID = "_id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_COMMENT = "comment";
    public static final String FIELD_TYPE = "type";

    public static String getCreateDDL() {
        return "CREATE TABLE IF NOT EXISTS filter ("
                + "_id integer primary key autoincrement,"
                + "name text,"
                + "comment text,"
                + "type integer,"
                + "UNIQUE(name, type) ON CONFLICT IGNORE);";
    }

}
