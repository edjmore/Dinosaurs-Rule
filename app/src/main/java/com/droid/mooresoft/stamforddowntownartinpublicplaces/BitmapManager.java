package com.droid.mooresoft.stamforddowntownartinpublicplaces;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ed on 7/23/15.
 */
public class BitmapManager {

    public BitmapManager(Context context) {
        mContext = context;
        mMap = new HashMap<>();
    }

    public Bitmap fetchBitmap(int resId) {
        if (mMap.containsKey(resId)) {
            return mMap.get(resId);
        }

        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), resId);
        mMap.put(resId, bmp);

        return bmp;
    }

    private Map<Integer, Bitmap> mMap;
    private Context mContext;
}
