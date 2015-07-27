package com.droid.mooresoft.stamforddowntownartinpublicplaces;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ed on 7/22/15.
 */
public class Dinosaur {

    public Dinosaur(Cursor cursor) {
        mNumber = cursor.getInt(0);
        mName = cursor.getString(1);
        mArtist = cursor.getString(2);
        mSponsor = cursor.getString(3);
        mLocation = new LatLng(cursor.getDouble(4), cursor.getDouble(5));
    }

    public Dinosaur(Bundle bundle) {
        mNumber = bundle.getInt(DatabaseContract.Entries.COLUMN_NAME_ID);
        mName = bundle.getString(DatabaseContract.Entries.COLUMN_NAME_NAME);
        mArtist = bundle.getString(DatabaseContract.Entries.COLUMN_NAME_ARTIST);
        mSponsor = bundle.getString(DatabaseContract.Entries.COLUMN_NAME_SPONSOR);
        mLocation = new LatLng(bundle.getDouble(DatabaseContract.Entries.COLUMN_NAME_LATITUDE),
                bundle.getDouble(DatabaseContract.Entries.COLUMN_NAME_LONGITUDE));
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt(DatabaseContract.Entries.COLUMN_NAME_ID, getNumber());
        bundle.putString(DatabaseContract.Entries.COLUMN_NAME_NAME, getName());
        bundle.putString(DatabaseContract.Entries.COLUMN_NAME_ARTIST, getArtist());
        bundle.putString(DatabaseContract.Entries.COLUMN_NAME_SPONSOR, getSponsor());
        bundle.putDouble(DatabaseContract.Entries.COLUMN_NAME_LATITUDE, getLocation().latitude);
        bundle.putDouble(DatabaseContract.Entries.COLUMN_NAME_LONGITUDE, getLocation().longitude);
        return bundle;
    }

    public LatLng getLocation() {
        return mLocation;
    }

    public String getName() {
        return mName;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getSponsor() {
        return mSponsor;
    }

    public int getNumber() {
        return mNumber;
    }

    public int getAvatarImageId(Context context) {
        String qualifiedName = getQualifiedName(getName());
        // avatar image is just a smaller version of the regular image
        qualifiedName += "_avatar";
        int resId = context.getResources().getIdentifier(qualifiedName, "mipmap", context.getPackageName());
        if (resId > 0) return resId;
        qualifiedName = qualifiedName.replace("_avatar", "");
        return context.getResources().getIdentifier(qualifiedName, "mipmap", context.getPackageName());
    }

    public int getImageId(Context context) {
        String qualifiedName = getQualifiedName(getName());
        // find the ID using the resource's name
        return context.getResources().getIdentifier(qualifiedName, "mipmap", context.getPackageName());
    }

    public String getQualifiedName() {
        return getQualifiedName(getName());
    }

    public boolean wasVisited(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(getQualifiedName(), false);
    }

    private String getQualifiedName(String name) {
        return name.toLowerCase()
                .replace(" ", "_")
                .replace("-", "_")
                .replace(".", "")
                .replace("'", "")
                .replace("!", "");
    }

    public static final String KEY = "dinosaur";

    private LatLng mLocation;
    private String mName, mArtist, mSponsor;

    // number is the designated marker and the database ID of this dinosaur
    private int mNumber;
}
