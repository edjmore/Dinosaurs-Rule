package com.droid.mooresoft.materiallibrary;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * Created by Ed on 7/15/15.
 */
public abstract class MaterialLayout extends FrameLayout {

    public MaterialLayout(Context context) {
        super(context);
        init();
    }

    public MaterialLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Material, defStyle, 0);
        try {
            // 'm_elevation' will override 'elevation' for devices running Lollipop or higher
            setElevation(a.getDimension(R.styleable.Material_m_elevation, 0));
            mForceZReorder = a.getBoolean(R.styleable.Material_m_force_z_reorder, true);
        } finally {
            a.recycle();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public float getElevation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return super.getElevation();
        }
        return mElevation;
    }

    public float getMaxZ() {
        return mMaxZ;
    }

    public float getMinZ() {
        return mMinZ;
    }

    public ShadowProvider getShadowProvider() {
        return provider;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public float getTranslationZ() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return super.getTranslationZ();
        }
        return mTranslationZ;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public float getZ() {
        return getElevation() + getTranslationZ();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setElevation(float elevation) {
        if (getMinZ() <= getElevation() + getTranslationZ() && getElevation() + getTranslationZ() <= getMaxZ()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                super.setElevation(elevation);
            } else {
                mElevation = elevation;
            }
        }
        // reposition this view in the layout hierarchy
        if (mForceZReorder) updateParentViewHierarchy();
        else invalidateInParent(); // still might need to redraw shadow
    }

    /**
     * Reordering by z value is enabled by default, but you may want to disable it for layouts such
     * as linear layout because it could change the ordering of views on the screen. This property can also
     * be set directly from an XML layout file.
     *
     * @param enabled should the view be reordered in its parent according to its z value
     */
    public void setForceZReorder(boolean enabled) {
        if (enabled != mForceZReorder) {
            mForceZReorder = enabled;
            // reposition in parent if necessary
            if (mForceZReorder) updateParentViewHierarchy();
        }
    }

    public void setShadowProvider(ShadowProvider provider) {
        if (provider != this.provider) {
            this.provider = provider;
            invalidateInParent();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setTranslationZ(float translationZ) {
        if (getMinZ() <= getElevation() + getTranslationZ() && getElevation() + getTranslationZ() <= getMaxZ()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                super.setTranslationZ(translationZ);
            } else {
                mTranslationZ = translationZ;
            }
        }
        // reposition this view in the layout heirarchy
        if (mForceZReorder) updateParentViewHierarchy();
        else invalidateInParent(); // still might need to redraw shadow
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setZ(float z) {
        setTranslationZ(z - getElevation());
    }

    /**
     * Implement this method to do all normal view drawing operations. It will be called after 'doDrawShadow(Canvas).'
     *
     * @param canvas a canvas clipped to this view's bounds and translated to its origin
     */
    protected void doDraw(Canvas canvas) {

    }

    /**
     * Implement this method to draw a shadow under the view. It will be called before 'doDraw(Canvas).'
     * Take care to do all draw operations in the correct locations as the canvas passed into this method
     * covers the entire parent view but is translated to the view's clipped origin.
     *
     * @param canvas a canvas covering this view's parent
     */
    protected void doDrawShadow(Canvas canvas) {

    }

    /**
     * In most cases this method should not be overriden to do drawing operations. Subclasses should
     * implement the 'doDraw(Canvas)' and 'doDrawShadow(Canvas)' callbacks instead.
     *
     * @param canvas a canvas provided by the superclass
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // draw the shadow before the view
        doDrawShadow(canvas);

        // clip the canvas to view bounds
        int restoreTo = canvas.save();
        canvas.clipRect(0, 0, getWidth(), getHeight());
        doDraw(canvas);
        canvas.restoreToCount(restoreTo);

        // draw children and foreground
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // use this callback to ensure that this view has the correct z index before the first draw
        if (mForceZReorder) updateParentViewHierarchy();
        // layout children
        super.onLayout(changed, left, top, right, bottom);
    }

    protected void setMaxZ(float maxZ) {
        if (maxZ > getMinZ()) {
            mMaxZ = maxZ;
        }
    }

    protected void setMinZ(float minZ) {
        if (minZ >= 0 && minZ < getMaxZ()) {
            mMinZ = minZ;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private float getZ(View view) {
        float z = 0;
        if (view instanceof MaterialView) {
            z = ((MaterialView) view).getZ();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            z = view.getZ();
        }
        return z;
    }

    private void init() {
        mMinZ = 0;
        mMaxZ = getResources().getDisplayMetrics().density * 24; // dp to px
    }

    private void insertInParent(ViewGroup parent, float z, int i) {
        // base case 1: parent is empty or this is the highest child view
        if (parent.getChildCount() == i) {
            parent.addView(this);
            return;
        }
        // base case 2: the child view has a higher z value than this one
        View v = parent.getChildAt(i);
        if (getZ(v) >= z) {
            parent.addView(this, i);
            return;
        }
        // recursive case
        insertInParent(parent, z, i + 1);
    }

    private void invalidateInParent() {
        ViewParent vp = getParent();
        if (vp != null && vp instanceof View) {
            View parent = (View) vp;
            parent.invalidate();
            invalidate();
        }
    }

    private void updateParentViewHierarchy() {
        ViewParent viewParent = getParent();
        if (viewParent == null || viewParent == getRootView()) {
            // can't reorder
            return;
        }
        if (viewParent instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) viewParent;
            // remove this view from its parent and then add it back at correct index
            parent.removeView(this);
            insertInParent(parent, getZ(), 0);
        }
    }

    private float mElevation, mMaxZ, mMinZ; // px
    private ShadowProvider provider;
    private float mTranslationZ; // px
    private boolean mForceZReorder;
}
