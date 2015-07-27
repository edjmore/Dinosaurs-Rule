package com.droid.mooresoft.materiallibrary;

/**
 * Created by Ed on 7/20/15.
 */
public abstract class ShadowProvider {

    public abstract float getDimensionScale();

    public abstract int getShadowColor(float z, float maxZ);

    public abstract float getShadowDx(float z, float maxZ, int viewWidth, int viewHeight);

    public abstract float getShadowDy(float z, float maxZ, int viewWidth, int viewHeight);

    public abstract int getShadowResourceId();
}
