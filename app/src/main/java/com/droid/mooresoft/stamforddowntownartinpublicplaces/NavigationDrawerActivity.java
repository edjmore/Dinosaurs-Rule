package com.droid.mooresoft.stamforddowntownartinpublicplaces;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import com.droid.mooresoft.materiallibrary.widgets.AvatarView;
import com.droid.mooresoft.materiallibrary.widgets.FloatingActionButton;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;

/**
 * Created by Ed on 7/20/15.
 */
public class NavigationDrawerActivity extends Activity {

    private class DrawerItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.START);
        // TODO: disable or enable items based on drawer and content state
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load all dinosaur data
        mData = getData();

        setContentView(R.layout.navigation_drawer_activity);
        // by default, use padding to keep the action bar from overlaying the drawer
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Space padding = (Space) findViewById(R.id.action_bar_padding);
            padding.getLayoutParams().height = 0; // remove padding
        }
        setStatusBarColor(findViewById(R.id.status_bar), getResources().getColor(R.color.status_bar));

        mTitle = mDrawerTitle = getTitle();
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawer) {
                setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawer) {
                setTitle(mTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        String[] drawerTitles = getResources().getStringArray(R.array.drawer_options);
        mDrawerList.setAdapter(new ArrayAdapter(this, R.layout.drawer_list_item, drawerTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        if (getIntent() != null && getIntent().hasExtra(KEY_SHOW_SPONSORS)) {
            if (getIntent().getBooleanExtra(KEY_SHOW_SPONSORS, false)) {
                selectItem(SPONSORS);
                getIntent().removeExtra(KEY_SHOW_SPONSORS);
            }
        } else {
            // by default show the map
            selectItem(EXHIBIT_MAP);
        }
    }

    @Override
    protected void onDestroy() {
        if (mDatabase != null) {
            mDatabase.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentDrawer == DINOSAURS) {
            if (mClickedView != null && mClickedDinosaur != null) {
                ImageView checkView = (ImageView) mClickedView.findViewById(R.id.check_view);
                checkView.setVisibility(
                        mClickedDinosaur.wasVisited(NavigationDrawerActivity.this) ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    private void selectItem(int position) {
        mDrawerLayout.closeDrawer(GravityCompat.START);

        // don't reload the fragment if already active
        if (mCurrentDrawer == position && mCurrentDrawer != DINOSAURS) {
            return;
        }

        // open the correct fragment
        switch (mCurrentDrawer = position) {
            case EXHIBIT_MAP:
                // was a target location passed by intent
                Intent intent = getIntent();
                LatLng target = null;
                if (intent.hasExtra(KEY_MAP_TARGET_LATITUDE) && intent.hasExtra(KEY_MAP_TARGET_LONGITUDE)) {
                    double lat = intent.getDoubleExtra(KEY_MAP_TARGET_LATITUDE, 0),
                            lng = intent.getDoubleExtra(KEY_MAP_TARGET_LONGITUDE, 0);
                    target = new LatLng(lat, lng);
                }
                showExhibitMap(target);
                break;
            case DINOSAURS:
                showDinosaurs();
                break;
            case ARTISTS:
                showArtists();
                break;
            case SPONSORS:
                showSponsors();
                break;
            case STAMFORD_DOWNTOWN:
                showStamfordDowntown();
                break;
        }

        mDrawerList.setItemChecked(position, true);
        mTitle = mDrawerList.getItemAtPosition(position).toString();
        setTitle(mTitle);
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

    private Cursor getData() {
        DatabaseHelper helper = new DatabaseHelper(this);
        mDatabase = helper.getReadableDatabase();
        return mDatabase.query(DatabaseContract.Entries.TABLE_NAME, null, null, null, null, null,
                DatabaseContract.Entries.COLUMN_NAME_NAME); // order by dinosaur name
    }

    private void showExhibitMap(final LatLng target) {
        CameraPosition camera = new CameraPosition.Builder()
                .target(target != null ? target : DEFAULT_LOCATION)
                .zoom(target != null ? TARGET_ZOOM : DEFAULT_ZOOM)
                .build();
        GoogleMapOptions options = new GoogleMapOptions()
                .camera(camera);
        final MapFragment mapFragment = MapFragment.newInstance(options);
        getFragmentManager().beginTransaction().replace(R.id.content, mapFragment).commit();
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMyLocationEnabled(true);

                // add a marker for every dinosaur in the cursor
                mData.moveToFirst();
                do {
                    Dinosaur dino = new Dinosaur(mData);
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(dino.getLocation())
                            .title(dino.getName())
                            .snippet("by " + dino.getArtist())
                            .icon(BitmapDescriptorFactory.defaultMarker( // visited dinosaurs will be magenta and unvisited will be orange
                                    dino.wasVisited(NavigationDrawerActivity.this) ? BitmapDescriptorFactory.HUE_MAGENTA : BitmapDescriptorFactory.HUE_ORANGE));
                    googleMap.addMarker(markerOptions);
                } while (mData.moveToNext());
            }
        });
    }

    private void showDinosaurs() {
        mDinosaurFragment = new ListFragment() {
            @Override
            public void onListItemClick(ListView lv, View v, int position, long id) {
                super.onListItemClick(lv, v, position, id);
                mClickedView = v;

                // start the dinosaur detail activity using the dinosaur represented by this list item
                Cursor cursor = (Cursor) lv.getItemAtPosition(position);
                Dinosaur dino = new Dinosaur(cursor);
                mClickedDinosaur = dino;
                Intent intent = new Intent(NavigationDrawerActivity.this, DinosaurDetailActivity.class);
                intent.putExtra(Dinosaur.KEY, dino.toBundle());
                startActivityForResult(intent, 0);
            }
        };
        mDinosaurFragment.setListAdapter(new CursorAdapter(this, mData, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return View.inflate(context, R.layout.dinosaur_list_item, null);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                Dinosaur dino = new Dinosaur(cursor);

                AvatarView avatarView = (AvatarView) view.findViewById(R.id.avatar);
                int avatarId = dino.getAvatarImageId(NavigationDrawerActivity.this);
                if (avatarId > 0) {
                    Bitmap img = mBitmapManager.fetchBitmap(avatarId);
                    avatarView.setImage(img);
                } else {
                    avatarView.setImage(mBitmapManager.fetchBitmap(R.mipmap.dino_poster));
                }

                TextView nameView = (TextView) view.findViewById(R.id.dinosaur_name),
                        artistView = (TextView) view.findViewById(R.id.artist);
                nameView.setText(dino.getName());
                artistView.setText(dino.getArtist());

                ImageView checkView = (ImageView) view.findViewById(R.id.check_view);
                if (!dino.wasVisited(NavigationDrawerActivity.this)) {
                    checkView.setVisibility(View.INVISIBLE);
                } else {
                    checkView.setVisibility(View.VISIBLE);
                }
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.content, mDinosaurFragment).commit();
    }

    private void showArtists() {
        final ListFragment artistList = new ListFragment() {
            @Override
            public void onListItemClick(ListView lv, View v, int pos, long id) {
                super.onListItemClick(lv, v, pos, id);
                // TODO: start artist detail activity
                Cursor c = (Cursor) lv.getItemAtPosition(pos);
                String artistName = c.getString(1);
                Intent viewArtist = new Intent(NavigationDrawerActivity.this, ArtistDetailActivity.class)
                        .putExtra(ArtistDetailActivity.KEY_NAME, artistName); // pass artist name via intent
                startActivity(viewArtist);
            }
        };
        // get the artist data
        Cursor artistData = mDatabase.query(DatabaseContract.Entries.TABLE_NAME,
                new String[]{DatabaseContract.Entries.COLUMN_NAME_ID, DatabaseContract.Entries.COLUMN_NAME_ARTIST},
                null, null, DatabaseContract.Entries.COLUMN_NAME_ARTIST, null, null);
        artistList.setListAdapter(new CursorAdapter(this, artistData, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return View.inflate(NavigationDrawerActivity.this, R.layout.artist_list_item, null);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView artistView = (TextView) view.findViewById(R.id.name);
                String artistName = cursor.getString(1);
                artistView.setText(artistName);

                String[] dinoNames = getDinosaursByArtist(artistName);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < dinoNames.length; ) {
                    sb.append(dinoNames[i]);
                    if (++i < dinoNames.length) {
                        sb.append(", ");
                    }
                }
                TextView dinoText = (TextView) view.findViewById(R.id.dinosaurs_by_artist);
                dinoText.setText(sb.toString());
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.content, artistList).commit();
    }

    private String[] getDinosaursByArtist(String artist) {
        String[] columns = new String[]{DatabaseContract.Entries.COLUMN_NAME_NAME};
        String selectClause = DatabaseContract.Entries.COLUMN_NAME_ARTIST + " = ?";
        String[] selectionArgs = new String[]{artist};
        String orderBy = DatabaseContract.Entries.COLUMN_NAME_NAME;
        Cursor c = mDatabase.query(DatabaseContract.Entries.TABLE_NAME, columns, selectClause, selectionArgs,
                null, null, orderBy);
        String[] dinos = new String[c.getCount()];
        int i = 0;
        while (c.moveToNext()) {
            dinos[i++] = c.getString(0);
        }
        return dinos;
    }

    private void showSponsors() {
        // just show an image of all the sponsors
        // TODO: this is pretty boring
        Fragment fragment = new Fragment() {
            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                return inflater.inflate(R.layout.sponsor_fragment, null);
            }
        };
        getFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }

    private void showStamfordDowntown() {
        Fragment fragment = new Fragment() {
            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.downtown_stamford_fragment, null);
                mWebView = (WebView) relativeLayout.findViewById(R.id.webview);
                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.loadUrl("http://stamford-downtown.com/");

                // allow use to open the site in their default browser
                final FloatingActionButton fab = (FloatingActionButton) relativeLayout.findViewById(R.id.browser_button);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // spin the icon icon before proceeding
                        ValueAnimator animator = ValueAnimator.ofFloat(0f, 360f);
                        animator.setDuration(500)
                                .setInterpolator(new AccelerateDecelerateInterpolator());
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float degrees = (float) animation.getAnimatedValue();
                                fab.setIconRotation(degrees);
                            }
                        });
                        animator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                // do nothing
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                Uri uri = Uri.parse(mWebView.getUrl());
                                Intent openBrowser = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(openBrowser);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                // do nothing
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                                // do nothing
                            }
                        });
                        animator.start();
                    }
                });
                return relativeLayout;
            }

            private WebView mWebView;
        };
        getFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }

    public static final LatLng DEFAULT_LOCATION = new LatLng(41.05343, -73.538734); // downtown Stamford
    public static final int DEFAULT_ZOOM = 15,
            TARGET_ZOOM = 17;

    // indices of each drawer title
    public static final int EXHIBIT_MAP = 0, DINOSAURS = 1, ARTISTS = 2, SPONSORS = 3, STAMFORD_DOWNTOWN = 4;

    public static final String KEY_MAP_TARGET_LATITUDE = "key_map_target_latitude",
            KEY_MAP_TARGET_LONGITUDE = "key_map_target_longitude";
    public static final String KEY_SHOW_SPONSORS = "key_show_sponsors";

    private int mCurrentDrawer = -1;
    private ListFragment mDinosaurFragment;
    private View mClickedView;
    private Dinosaur mClickedDinosaur;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private Cursor mData;
    private BitmapManager mBitmapManager = new BitmapManager(NavigationDrawerActivity.this);
    private SQLiteDatabase mDatabase;
}
