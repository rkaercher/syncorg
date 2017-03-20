package com.coste.syncorg.orgdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.coste.syncorg.orgdata.OrgContract.Timestamps;
import com.coste.syncorg.orgdata.OrgDatabase.Tables;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrgNodeTimeDate {
    private static final String timestampPattern = "<((\\d{4})-(\\d{1,2})-(\\d{1,2}))(?:[^\\d]*)"
            + "((\\d{1,2})\\:(\\d{2}))?(-((\\d{1,2})\\:(\\d{2})))?[^>]*>";
    private static final Map<TYPE, Pattern> patterns;

    static {
        Map<TYPE, Pattern> tmpMap = new HashMap<>();
        tmpMap.put(TYPE.Deadline, Pattern.compile("DEADLINE:\\s*" + timestampPattern));
        tmpMap.put(TYPE.Scheduled, Pattern.compile("SCHEDULED:\\s*" + timestampPattern));
        patterns = Collections.unmodifiableMap(tmpMap);
    }

    private TYPE type = TYPE.Timestamp;

    private int endTimeOfDay = -1;
    private int endMinute = -1;
    int matchStart = -1, matchEnd = -1;

    private LocalDate date = null;
    private LocalTime time = null;

    OrgNodeTimeDate(TYPE type) {
        this.type = type;
        this.date = LocalDate.now();
    }

    OrgNodeTimeDate(TYPE type, String line) {
        this.type = type;
        parseDate(line);
    }

    public OrgNodeTimeDate(long epochTimeInSec) {
        setEpochTime(epochTimeInSec, false);
    }

    /**
     * OrgNodeTimeDate ctor from the database
     *
     * @param type
     * @param nodeId The OrgNode ID associated with this timestamp
     */
    OrgNodeTimeDate(TYPE type, long nodeId) {

        String todoQuery = "SELECT " +
                OrgContract.formatColumns(
                        Tables.TIMESTAMPS,
                        Timestamps.DEFAULT_COLUMNS) +
                " FROM " + Tables.TIMESTAMPS +
                " WHERE " + Timestamps.NODE_ID + " = " + nodeId +
                "   AND " + Timestamps.TYPE + " = " + type.ordinal() +
                " ORDER BY " + Timestamps.TIMESTAMP;

        this.type = type;

        Cursor cursor = OrgDatabase.getInstance().getReadableDatabase().rawQuery(todoQuery, null);
        set(cursor);
    }

    private static String typeToFormatted(TYPE type) {
        switch (type) {
            case Scheduled:
                return "SCHEDULED: ";
            case Deadline:
                return "DEADLINE: ";
            case Timestamp:
                return "";
            default:
                return "";
        }
    }

    private static String formatDate(TYPE type, String timestamp) {
        if (TextUtils.isEmpty(timestamp))
            return "";
        else {
            return OrgNodeTimeDate.typeToFormatted(type) + "<" + timestamp + ">";
        }
    }

    static Pattern getTimestampMatcher(OrgNodeTimeDate.TYPE type) {
        final String timestampPattern = "<([^>]+)(\\d\\d:\\d\\d)>"; // + "(?:\\s*--\\s*<([^>]+)>)?"; for ranged date
//		final String timestampLookbehind = "\\s*(?<!(?:SCHEDULED:|DEADLINE:)\\s?)";

//		String pattern;
//		if(type == OrgNodeTimeDate.TYPE.Timestamp)
//			pattern = timestampLookbehind + "(" + timestampPattern + ")";
//		else

        String pattern = "\\s*(" + OrgNodeTimeDate.typeToFormatted(type) + "\\s*" + timestampPattern + ")";

        return Pattern.compile(pattern);
    }

    static public void deleteTimestamp(Context context, long nodeId, String where) {
        Uri uri = OrgContract.Timestamps.buildIdUri(nodeId);
        context.getContentResolver().delete(uri, where, null);
    }

    public void set(Cursor cursor) {

        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.isBeforeFirst() || cursor.isAfterLast())
                cursor.moveToFirst();

            long epochTime = cursor.getLong(cursor.getColumnIndexOrThrow(Timestamps.TIMESTAMP));

            boolean allDay = cursor.getLong(cursor.getColumnIndexOrThrow(Timestamps.ALL_DAY)) == 1;

            setEpochTime(epochTime, allDay);
        }
    }

    private void setEpochTime(long epochTimeInSec, boolean allDay) {
        long epochTimeMillisec = epochTimeInSec * 1000L;
        date = new LocalDate(epochTimeMillisec);
        if (!allDay) {
            time = new LocalTime(epochTimeMillisec);
        }
    }

    private void parseDate(String line) {
        if (line == null)
            return;

        if (patterns.get(type) == null) return;
        Matcher propm = patterns.get(type).matcher(line);
        if (propm.find()) {
            matchStart = propm.start();
            matchEnd = propm.end();
            try {
                int year = Integer.parseInt(propm.group(2));
                int monthOfYear = Integer.parseInt(propm.group(3));
                int dayOfMonth = Integer.parseInt(propm.group(4));

                date = new LocalDate(year, monthOfYear, dayOfMonth);

                if (propm.group(6) != null && propm.group(7) != null) {
                    int startHour = Integer.parseInt(propm.group(6));
                    int startMinute = Integer.parseInt(propm.group(7));
                    time = new LocalTime(startHour, startMinute);
                }

                endTimeOfDay = Integer.parseInt(propm.group(10));
                endMinute = Integer.parseInt(propm.group(11));
            } catch (NumberFormatException e) {
            }
        }
    }

    public String getDateString() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        return fmt.print(date);
    }

    public String getTimeString() {
        if (null == time) return "";
        DateTimeFormatter fmt = DateTimeFormat.forPattern("hh:mm");
        return fmt.print(time);
    }

    private String getTimeDateString() {
        String dateString = getDateString();
        if (time == null) return dateString;
        String timeString = getTimeString();
        return dateString + " " + timeString;

    }

    /**
     * Return the number of seconds elapsed since the start of 1st January 1970
     *
     * @return
     */
    public long getEpochTime() {
        if (time != null) {
            return date.toDateTime(time).toDate().getTime() / 1000L;
        }
        return date.toDate().getTime() / 1000L;
    }

    /**
     * Check if event is all day long
     */
    long isAllDay() {
        return (time == null) ? 1 : 0;
    }

    /**
     * Format the string according to the parameter isDate
     *
     * @param isDate: date formating or time formating
     */
    public String toString(boolean isDate) {
        if (isDate) return getDateString();
        return getTimeString();
    }

    String toFormatedString() {
        return formatDate(type, getTimeDateString());
    }

    public void update(Context context, long nodeId, long fileId) {
        deleteTimestamp(context, nodeId, Timestamps.TYPE + "=" + type.ordinal());
        if (getEpochTime() < 0) return;
        context.getContentResolver().
                insert(
                        OrgContract.Timestamps.buildIdUri(nodeId),
                        getContentValues(nodeId, fileId));
    }

    private ContentValues getContentValues(long nodeId, long fileId) {
        ContentValues values = new ContentValues();
        values.put(Timestamps.ALL_DAY, isAllDay());
        values.put(Timestamps.TIMESTAMP, getEpochTime());
        values.put(Timestamps.TYPE, type.ordinal());
        values.put(Timestamps.NODE_ID, nodeId);
        values.put(Timestamps.FILE_ID, fileId);
        return values;
    }

    /**
     * Check if this was at least a day before date
     */
    private boolean isBefore(OrgNodeTimeDate date) {
        return this.date.isBefore(date.getDate());
    }

    /**
     * Check the crrent date is between start date (excluded) and to date(excluded)
     * WARNING, can also be between end (first) and start (at the end) !!
     *
     * @param start: the starting date
     * @param end:   the ending date
     */
    public boolean isBetween(OrgNodeTimeDate start, OrgNodeTimeDate end) {
        return
                start.isBefore(this) && this.isBefore(end)
                        || end.isBefore(this) && this.isBefore(start);
    }

    public TYPE getType() {
        return type;
    };

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public enum TYPE {
        Scheduled,
        Deadline,
        Timestamp,
        InactiveTimestamp
    }


}
