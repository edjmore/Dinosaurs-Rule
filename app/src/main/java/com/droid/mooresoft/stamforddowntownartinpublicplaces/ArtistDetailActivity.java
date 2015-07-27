package com.droid.mooresoft.stamforddowntownartinpublicplaces;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import com.droid.mooresoft.materiallibrary.widgets.AvatarView;

/**
 * Created by Ed on 7/24/15.
 */
public class ArtistDetailActivity extends Activity {

    private class OnAvatarClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.dino_avatar_0:
                    if (mDinos[0] != null) {
                        launchDinosaurDetailActivity(mDinos[0]);
                    }
                    break;
                case R.id.dino_avatar_1:
                    if (mDinos[0] != null) {
                        launchDinosaurDetailActivity(mDinos[1]);
                    }
                    break;
                case R.id.dino_avatar_2:
                    if (mDinos[0] != null) {
                        launchDinosaurDetailActivity(mDinos[2]);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_detail_activity);
        // by default, use padding to keep the action bar from overlaying the content
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Space padding = (Space) findViewById(R.id.action_bar_padding);
            padding.getLayoutParams().height = 0; // remove padding
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setUpIndicator(R.mipmap.ic_action_back);
        setStatusBarColor(findViewById(R.id.status_bar), getResources().getColor(R.color.status_bar));

        String artistName = getIntent().getStringExtra(KEY_NAME);
        setTitle(artistName);
        displayArtistBio(artistName, (TextView) findViewById(R.id.bio));

        mDinos = getDinosaurs(artistName);
        AvatarView av0 = (AvatarView) findViewById(R.id.dino_avatar_0),
                av1 = (AvatarView) findViewById(R.id.dino_avatar_1),
                av2 = (AvatarView) findViewById(R.id.dino_avatar_2);
        av0.setOnClickListener(mClickListener);
        av1.setOnClickListener(mClickListener);
        av2.setOnClickListener(mClickListener);
        Bitmap b0 = BitmapFactory.decodeResource(getResources(), mDinos[0].getAvatarImageId(this));
        Bitmap b1 = mDinos[1] != null ? BitmapFactory.decodeResource(getResources(), mDinos[1].getAvatarImageId(this)) : null;
        Bitmap b2 = mDinos[2] != null ? BitmapFactory.decodeResource(getResources(), mDinos[2].getAvatarImageId(this)) : null;
        av0.setImage(b0);
        av1.setImage(b1);
        av2.setImage(b2);
        TextView tv0 = (TextView) findViewById(R.id.dino_name_0),
                tv1 = (TextView) findViewById(R.id.dino_name_1),
                tv2 = (TextView) findViewById(R.id.dino_name_2);
        tv0.setText(mDinos[0].getName());
        if (b1 != null) tv1.setText(mDinos[1].getName());
        if (b2 != null) tv2.setText(mDinos[2].getName());
    }

    private void launchDinosaurDetailActivity(Dinosaur dino) {
        Intent intent = new Intent(ArtistDetailActivity.this, DinosaurDetailActivity.class);
        intent.putExtra(Dinosaur.KEY, dino.toBundle());
        startActivity(intent);
    }

    private Dinosaur[] getDinosaurs(String artist) {
        DatabaseHelper helper = new DatabaseHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        // want all rows associated with this artist
        String select = DatabaseContract.Entries.COLUMN_NAME_ARTIST + " = ?";
        String[] args = new String[]{artist};
        Cursor c = db.query(DatabaseContract.Entries.TABLE_NAME, null, select, args, null, null, null);
        Dinosaur[] dinos = new Dinosaur[3];
        int i = 0;
        while (c.moveToNext()) {
            dinos[i++] = new Dinosaur(c);
        }
        return dinos;
    }

    private boolean displayArtistBio(String artistName, TextView dstView) {
        int resId = getResources().getIdentifier(getArtistResourceName(artistName), "string", getPackageName());
        if (resId > 0) {
            dstView.setText(resId);
            return true;
        }
        return false;
    }

    private String getArtistResourceName(String artistName) {
        return artistName.toLowerCase()
                .replace(" ", "_")
                .replace("-", "_")
                .replace("é", "e")
                .replace(".", "")
                .replace("&", "and");
    }

    private void setUpIndicator(int resId) {
        int viewId = getResources().getIdentifier("up", "id", "android");
        if (viewId > 0) {
            ImageView iv = (ImageView) findViewById(viewId);
            iv.setImageResource(resId);
        }
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

    public static final String KEY_NAME = "key_name";

    private final OnAvatarClickListener mClickListener = new OnAvatarClickListener();
    private Dinosaur[] mDinos;
}
