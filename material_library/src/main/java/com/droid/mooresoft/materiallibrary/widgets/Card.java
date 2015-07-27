package com.droid.mooresoft.materiallibrary.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;

import com.droid.mooresoft.materiallibrary.MaterialLayout;
import com.droid.mooresoft.materiallibrary.R;
import com.droid.mooresoft.materiallibrary.ShadowProvider;

/**
 * Created by Ed on 7/15/15.
 */
public class Card extends MaterialLayout {

    public Card(Context context) {
        super(context);
        init();
    }

    public Card(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Card(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Material, defStyle, 0);
        try {
            mColor = a.getColor(R.styleable.Material_m_color, getResources().getColor(android.R.color.holo_blue_light));
        } finally {
            a.recycle();
        }
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        if (color != mColor) {
            mColor = color;
            // redraw
            invalidate();
        }
    }

    @Override
    protected void doDraw(Canvas canvas) {
        canvas.drawColor(mColor);
    }

    @Override
    protected void doDrawShadow(Canvas canvas) {
        ShadowProvider provider = getShadowProvider();
        if (provider == null) return;

        if (mShadow == null) {
            mShadow = BitmapFactory.decodeResource(getResources(), provider.getShadowResourceId());
        }

        float edgeRadius = Math.min(getWidth(), getHeight()) * (provider.getDimensionScale() - 1f);
        int width = (int) (getWidth() + edgeRadius),
                height = (int) (getHeight() + edgeRadius);
        Bitmap bitmap = Bitmap.createScaledBitmap(mShadow, width, height, false);
        Canvas temp = new Canvas(bitmap);

        mPaint.setColor(provider.getShadowColor(getZ(), getMaxZ()));
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        temp.drawPaint(mPaint);

        float offsetX = -(width - getWidth()) / 2, offsetY = -(height - getHeight()) / 2;
        offsetX += provider.getShadowDx(getZ(), getMaxZ(), getWidth(), getHeight());
        offsetY += provider.getShadowDy(getZ(), getMaxZ(), getWidth(), getHeight());
        mPaint.reset();
        canvas.drawBitmap(bitmap, offsetX, offsetY, mPaint);
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT); // clear background
        setShadowProvider(new ShadowProvider() {
            @Override
            public float getDimensionScale() {
                return 1.075f;
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
                float maxScale = 0.05f;
                // higher views should have "longer" shadows
                float scale = (z / maxZ) * maxScale;
                return viewHeight * scale;
            }

            @Override
            public int getShadowResourceId() {
                return R.mipmap.rectangle_shadow;
            }
        });
    }

    private int mColor;
    private final Paint mPaint = new Paint();
    private static Bitmap mShadow;
}
