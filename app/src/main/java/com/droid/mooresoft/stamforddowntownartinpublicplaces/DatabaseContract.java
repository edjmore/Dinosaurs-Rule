package com.droid.mooresoft.stamforddowntownartinpublicplaces;

import android.provider.BaseColumns;

/**
 * Created by Ed on 7/22/15.
 */
public final class DatabaseContract {

    public static abstract class Entries implements BaseColumns {
        // a single table
        public static final String TABLE_NAME = "dinosaurs";

        // columns
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_ARTIST = "artist";
        public static final String COLUMN_NAME_SPONSOR = "sponsor";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
    }

    private DatabaseContract() {
        // this class should never be instantiated
    }
}
