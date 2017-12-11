package ch.mse.biketracks.database;

import android.provider.BaseColumns;

/**
 * Created by antoi on 25.10.2017.
 */

public final class TrackContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private TrackContract() {}

    /* Inner class that defines the table contents */
    public static class TrackEntry implements BaseColumns {
        public static final String TABLE_NAME = "contact";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DATE = "date";
    }
}
