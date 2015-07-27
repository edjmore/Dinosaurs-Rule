package com.droid.mooresoft.stamforddowntownartinpublicplaces;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by Ed on 7/22/15.
 */
public class LaunchHelperActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // has the dinosaur database been created yet?
        boolean exists = checkForDatabase();
        if (!exists) {
            // TODO: display loading screen?

            // need to create the database
            initDatabase();
        }

        startActivity(new Intent(this, NavigationDrawerActivity.class));
        finish();
    }

    private boolean checkForDatabase() {
        File dbFile = getDatabasePath(DatabaseHelper.DATABASE_NAME);
        return dbFile.exists();
    }

    private void initDatabase() {
        DatabaseHelper helper = new DatabaseHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        // get the raw text file with dinosaur data
        InputStream in = getResources().openRawResource(R.raw.dinosaur_data);
        Scanner s = new Scanner(in).useDelimiter("\t");

        int i = 0;
        while (s.hasNextLine()) {
            /* int id = s.nextInt();
            String name = s.next(),
                    artist = s.next(),
                    sponsor = s.next();
            double latitude = s.nextDouble(),
                    longitude = s.nextDouble();
            Log.d(getClass().toString(), "id = " + id + " name = " + name + " artist = " + artist + " sponsor = "
                    + sponsor + " latitude = " + latitude + " longitude = " + longitude); */

            // see 'dinosaur_data.txt' for data format
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.Entries.COLUMN_NAME_ID, s.nextInt());
            values.put(DatabaseContract.Entries.COLUMN_NAME_NAME, s.next());
            values.put(DatabaseContract.Entries.COLUMN_NAME_ARTIST, s.next());
            values.put(DatabaseContract.Entries.COLUMN_NAME_SPONSOR, s.next());
            values.put(DatabaseContract.Entries.COLUMN_NAME_LATITUDE, s.nextDouble());
            values.put(DatabaseContract.Entries.COLUMN_NAME_LONGITUDE, s.nextDouble());

            // insert values for this dinosaur
            db.insert(DatabaseContract.Entries.TABLE_NAME, null, values);

            // skip to next line
            s.nextLine();
        }
        // avoid database leak
        db.close();
    }
}
