package com.droid.mooresoft.materiallibrary;

/**
 * Created by Ed on 7/11/15.
 */
public class Utils {

    public static float getEuclideanDistance(float x1, float y1, float x2, float y2) {
        double dx2 = Math.pow(x1 - x2, 2),
                dy2 = Math.pow(y1 - y2, 2);
        return (float) Math.pow(dx2 + dy2, 0.5f);
    }
}
