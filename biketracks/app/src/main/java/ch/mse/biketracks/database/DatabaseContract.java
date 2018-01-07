package ch.mse.biketracks.database;

import android.provider.BaseColumns;

/**
 * This class represent the whole schema of the database
 * Each tablea is an inner class
 */
public final class DatabaseContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DatabaseContract() {}

    /* COMMON ATTRIBUTES */
    static final String COMMON_KEY_ID = "id";

    /* CONTACT */
    public static class ContactEntry implements BaseColumns {
        static final String TABLE_NAME = "contact";
        static final String COLUMN_NAME_ID = COMMON_KEY_ID;
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_NUMBER = "number";

        public static final String SQL_CREATE_TABLE_CONTACT =
                "CREATE TABLE " + ContactEntry.TABLE_NAME + " (" +
                        ContactEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                        ContactEntry.COLUMN_NAME_NAME + " TEXT," +
                        ContactEntry.COLUMN_NAME_NUMBER + " TEXT);";

        public static final String SQL_DROP_TABLE_CONTACT =
                "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    }

    /* TRACK */
    public static class TrackEntry implements BaseColumns {
        static final String TABLE_NAME = "track";
        static final String COLUMN_NAME_ID = COMMON_KEY_ID;
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_DISTANCE = "distance_m";
        static final String COLUMN_NAME_CLIMB = "climb_m";
        static final String COLUMN_NAME_DESCENT = "descent_m";
        static final String COLUMN_NAME_SPEED = "speed";
        static final String COLUMN_NAME_TYPE = "type";
        static final String COLUMN_NAME_DATE = "date";
        static final String COLUMN_NAME_DURATION = "duration_s";
        static final String COLUMN_NAME_IMAGE = "image";

        public static final String SQL_CREATE_TABLE_TRACK =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_NAME + " TEXT," +
                        COLUMN_NAME_DISTANCE + " INTEGER," +
                        COLUMN_NAME_CLIMB + " INTEGER," +
                        COLUMN_NAME_SPEED + " FLOAT," +
                        COLUMN_NAME_DESCENT + " INTEGER," +
                        COLUMN_NAME_TYPE + " TEXT," +
                        COLUMN_NAME_DATE + " INTEGER," +
                        COLUMN_NAME_DURATION + " INTEGER," +
                        COLUMN_NAME_IMAGE + " BLOB);";

        public static final String SQL_DROP_TABLE_TRACK =
                "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    }

    /* POINT */
    public static class PointEntry implements BaseColumns {
        static final String TABLE_NAME = "point";
        static final String COLUMN_NAME_ID = COMMON_KEY_ID;
        static final String COLUMN_NAME_LAT = "lat";
        static final String COLUMN_NAME_LNG = "lng";
        static final String COLUMN_NAME_ELEV = "elev";
        static final String COLUMN_NAME_TIME = "time";
        static final String COLUMN_NAME_DURATION = "duration_s";
        static final String COLUMN_NAME_TRACK_ID = "track_id";

        public static final String SQL_CREATE_TABLE_POINT =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_LAT + " REAL," +
                        COLUMN_NAME_LNG + " REAL," +
                        COLUMN_NAME_ELEV + " INTEGER," +
                        COLUMN_NAME_TIME + " INTEGER," +
                        COLUMN_NAME_DURATION + " INTEGER," +
                        COLUMN_NAME_TRACK_ID + " INTEGER," +
                        "FOREIGN KEY(" + COLUMN_NAME_TRACK_ID + ") REFERENCES " + TrackEntry.TABLE_NAME + "(" + TrackEntry.COLUMN_NAME_ID + ")" +
                        ");";

        public static final String SQL_DROP_TABLE_POINT =
                "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    }
}
