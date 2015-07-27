package com.droid.mooresoft.materiallibrary.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;

import com.droid.mooresoft.materiallibrary.MaterialView;
import com.droid.mooresoft.materiallibrary.R;

/**
 * Created by Ed on 7/12/15.
 */
public class AvatarView extends MaterialView {

    public AvatarView(Context context) {
        super(context);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Material, defStyle, 0);
        try {
            int imgId = a.getResourceId(R.styleable.Material_m_image, -1);
            // try to decode an image referenced in XML
            if (imgId != -1) {
                mImage = BitmapFactory.decodeResource(getResources(), imgId);
            }
        } finally {
            a.recycle();
        }
    }

    public Bitmap getImage() {
        return mImage;
    }

    /**
     * Set a new image to be shown in the view. The image may not be preserved. Passing a null
     * image to this method will simply remove any image from the view.
     *
     * @param image may not be preserved, can be null
     */
    public void setImage(Bitmap image) {
        if (image != mImage) {
            mImage = image;
            mBitmapIsDirty = true;
            invalidate(); // redraw
        }
    }

    @Override
    protected void doDraw(Canvas canvas) {
        if (mBitmapIsDirty) prepareBitmap();
        if (mImage /* mResizedRoundedBitmap */ != null) {
            mPaint.setAntiAlias(true);
            mPaint.setFilterBitmap(true);
            mPaint.setDither(true);
            canvas.drawBitmap(mImage /* mResizedRoundedBitmap */, mCenter.x - mRadius, mCenter.y - mRadius, mPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        // radius and center of avatar view circle
        mRadius = Math.min(w, h) / 2;
        mCenter = new PointF((getRight() - getLeft()) / 2, (getBottom() - getTop()) / 2);
        // bitmap will need to be resized
        mBitmapIsDirty = true;
    }

    private void prepareBitmap() {
        // mResizedRoundedBitmap = null; // will initialize this bitmap
        if (mImage == null) return; // cancel

        // calculate desired image dimensions
        float bmpW = mImage.getWidth(), bmpH = mImage.getHeight();
        // quick sanity check to avoid runtime errors
        if (mRadius <= 0 || bmpW <= 0 || bmpH <= 0) return;
        // taking the maximum scale ensures that the bitmap will at least large enough to fill both dimensions
        float scale = Math.max(mRadius * 2 / bmpW, mRadius * 2 / bmpH);
        int dstWidth = (int) (bmpW * scale), dstHeight = (int) (bmpH * scale);
        // resize the image
        Bitmap resizedImage = Bitmap.createScaledBitmap(mImage, dstWidth, dstHeight, false);

        // need to do all drawing on a new, blank canvas
        int diameter = (int) (mRadius * 2);
        mImage /* mResizedRoundedBitmap */ = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mImage /* mResizedRoundedBitmap */);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);
        // draw the circle the image will be overlayed onto
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawCircle(mRadius, mRadius, mRadius, mPaint);
        // draw the image centered
        float imgLeft = mRadius - resizedImage.getWidth() / 2, imgTop = mRadius - resizedImage.getHeight() / 2;
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(resizedImage, imgLeft, imgTop, mPaint);

        mBitmapIsDirty = false;
        // cleanup
        resizedImage.recycle();
        mPaint.reset();
    }

    private boolean mBitmapIsDirty = true;
    private PointF mCenter;
    private Bitmap mImage;
    private final Paint mPaint = new Paint();
    private float mRadius;
    // private Bitmap mResizedRoundedBitmap;
}
