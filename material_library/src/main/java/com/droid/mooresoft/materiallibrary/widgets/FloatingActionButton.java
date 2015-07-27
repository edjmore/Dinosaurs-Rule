package com.droid.mooresoft.materiallibrary.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.droid.mooresoft.materiallibrary.MaterialView;
import com.droid.mooresoft.materiallibrary.R;
import com.droid.mooresoft.materiallibrary.ShadowProvider;
import com.droid.mooresoft.materiallibrary.Utils;

/**
 * Created by Ed on 7/6/15.
 */
public class FloatingActionButton extends MaterialView {

    public FloatingActionButton(Context context) {
        super(context);
        init();
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Material, defStyle, 0);
        try {
            mColor = a.getColor(R.styleable.Material_m_color, getResources().getColor(android.R.color.holo_blue_light));
            // try to load an icon from XML reference
            int iconId = a.getResourceId(R.styleable.Material_m_icon, -1);
            if (iconId != -1) mIcon = getResources().getDrawable(iconId);
        } finally {
            a.recycle();
        }
    }

    public int getColor() {
        return mColor;
    }

    public float getIconRotation() {
        return mIconRotation;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // check if the event was actually within the button circle
        float dist = Utils.getEuclideanDistance(0, 0, event.getX() - mCenter.x, event.getY() - mCenter.y);
        if (dist <= mRadius) {
            // the user did tap on the button
            return super.onTouchEvent(event);
        }
        // don't handle the touch event
        return false;
    }

    public void setColor(int color) {
        if (color != mColor) {
            mColor = color;
            // redraw
            invalidate();
        }
    }

    public void setIconRotation(float degrees) {
        if (degrees != mIconRotation) {
            mIconRotation = degrees;
            // redraw
            invalidate();
        }
    }

    @Override
    protected void doDraw(Canvas canvas) {
        // prepare the paint
        mPaint.setColor(getColor());
        // use dimensions and position from the shadow
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius, mPaint);

        if (mIcon != null) {
            // icon must fit entirely in the button circle
            float iconRadius = (int) (Math.cos(45) * mRadius);
            // desired bounds for the icon
            float left = mCenter.x - iconRadius, top = mCenter.y - iconRadius,
                    right = mCenter.x + iconRadius, bottom = mCenter.y + iconRadius;
            // add in padding
            left += getPaddingLeft();
            top += getPaddingTop();
            right -= getPaddingRight();
            bottom -= getPaddingBottom();
            // apply bounds to icon drawable
            mIcon.setBounds((int) left, (int) top, (int) right, (int) bottom);
            // rotate the icon if necessary
            canvas.rotate(mIconRotation, mCenter.x, mCenter.y); // pivot around button center
            // the icon will only be drawn if the bounds are possible
            mIcon.draw(canvas);
            canvas.rotate(-mIconRotation, mCenter.x, mCenter.y); // restore canvas rotation
        }

        // TODO: provide the option to add an overlay
    }

    @Override
    protected void doDrawShadow(Canvas canvas) {
        ShadowProvider provider = getShadowProvider();
        if (provider == null) return;

        // setup the paint
        mPaint.setAntiAlias(true); // sharper diagonals
        mPaint.setDither(true); // better colors
        // radial gradient creates the shadow effect
        int[] colors = {provider.getShadowColor(getZ(), getMaxZ()), Color.TRANSPARENT};
        float[] stops = {0.7f, 1f};
        // get shadow center
        float dx = provider.getShadowDx(getZ(), getMaxZ(), (int) mRadius, (int) mRadius),
                dy = provider.getShadowDy(getZ(), getMaxZ(), (int) mRadius, (int) mRadius);
        PointF shadowCenter = new PointF(mCenter.x + dx, mCenter.y + dy);
        RadialGradient shader = new RadialGradient(shadowCenter.x, shadowCenter.y, mRadius * provider.getDimensionScale(),
                colors, stops, Shader.TileMode.CLAMP);
        mPaint.setShader(shader);

        canvas.drawCircle(shadowCenter.x, shadowCenter.y, mRadius * provider.getDimensionScale(), mPaint);
        mPaint.setShader(null); // remove shader
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        // calculate dimensions and position
        mRadius = Math.min(getWidth(), getHeight()) / 2;
        mCenter = new PointF((getRight() - getLeft()) / 2, (getBottom() - getTop()) / 2);
    }

    private void init() {
        setShadowProvider(new ShadowProvider() {
            @Override
            public float getDimensionScale() {
                return 1.125f;
            }

            @Override
            public int getShadowColor(float z, float maxZ) {
                float minAlpha = 51, maxAlpha = 153;
                // lower views should have the darkest shadows
                float alpha = maxAlpha - ((z / maxZ) * (maxAlpha - minAlpha));
                return Color.argb((int) alpha, 0, 0, 0);
            }

            @Override
            public float getShadowDx(float z, float maxZ, int viewWidth, int viewHeight) {
                return 0;
            }

            @Override
            public float getShadowDy(float z, float maxZ, int viewWidth, int viewHeight) {
                float maxScale = 0.125f;
                // higher views should have "longer" shadows
                float scale = (z / maxZ) * maxScale;
                return viewHeight * scale;
            }

            @Override
            public int getShadowResourceId() {
                return -1; // unused in this implementation
            }
        });
    }

    private PointF mCenter;
    private int mColor;
    private Drawable mIcon;
    private float mIconRotation; // degrees
    private final Paint mPaint = new Paint();
    private float mRadius;
}
