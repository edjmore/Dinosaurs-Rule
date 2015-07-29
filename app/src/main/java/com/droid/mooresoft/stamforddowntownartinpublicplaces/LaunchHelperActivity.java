package com.droid.mooresoft.stamforddowntownartinpublicplaces;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

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
        setContentView(R.layout.launch_helper_activity);
        getActionBar().hide();
        setStatusBarColor(findViewById(R.id.status_bar), getResources().getColor(R.color.status_bar));

        mStartTime = System.currentTimeMillis();

        // has the dinosaur database been created yet?
        boolean exists = checkForDatabase();
        if (!exists) {
            // need to create the database
            initDatabase();
        }

        long elapsed = System.currentTimeMillis() - mStartTime;
        long delay = mActivityDuration - elapsed;
        delay = delay > 0 ? delay : 0;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LaunchHelperActivity.this, NavigationDrawerActivity.class));
                finish();
            }
        }, delay);
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

    private int getStatusBarHeight() {
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : 0;
    }

    private void setStatusBarColor(View statusBar, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // this will only work on devices running KitKat or higher
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // resize and color the background view to make the transluscent status bar
            // appear colored
            statusBar.getLayoutParams().height = getStatusBarHeight();
            statusBar.setBackgroundColor(color);
        }
    }

    private final Handler mHandler = new Handler();
    private long mStartTime;
    private final int mActivityDuration = 1000; // milliseconds
}
