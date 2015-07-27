package com.droid.mooresoft.stamforddowntownartinpublicplaces;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ed on 7/22/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dinosaurs.db";

    public static final String SQL_CREATE = "create table " + DatabaseContract.Entries.TABLE_NAME +
            " (" + DatabaseContract.Entries.COLUMN_NAME_ID + " integer primary key autoincrement, " + // 0
            DatabaseContract.Entries.COLUMN_NAME_NAME + " text, " + // 1
            DatabaseContract.Entries.COLUMN_NAME_ARTIST + " text, " + // 2
            DatabaseContract.Entries.COLUMN_NAME_SPONSOR + " text, " + // 3
            DatabaseContract.Entries.COLUMN_NAME_LATITUDE + " real, " + // 4
            DatabaseContract.Entries.COLUMN_NAME_LONGITUDE + " real);"; // 5
    public static final String SQL_DELETE = "drop table if exists " + DatabaseContract.Entries.TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE);
        onCreate(db);
    }
}
