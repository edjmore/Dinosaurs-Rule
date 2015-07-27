package com.droid.mooresoft.stamforddowntownartinpublicplaces;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.droid.mooresoft.materiallibrary.widgets.FloatingActionButton;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by Ed on 7/22/15.
 */
public class DinosaurDetailActivity extends Activity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dinosaur_detail_activity);

        // by default, use padding to keep the action bar from overlaying the drawer
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Space padding = (Space) findViewById(R.id.action_bar_padding);
            padding.getLayoutParams().height = 0; // remove padding
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setUpIndicator(R.mipmap.ic_action_back);
        setStatusBarColor(findViewById(R.id.status_bar), getResources().getColor(R.color.status_bar));

        // load dinosaur data from intent
        Intent intent = getIntent();
        mDino = new Dinosaur(intent.getBundleExtra(Dinosaur.KEY));

        // populate views with data
        setTitle(mDino.getName());
        TextView artistView = (TextView) findViewById(R.id.artist),
                sponsorView = (TextView) findViewById(R.id.sponsor);
        artistView.setText(mDino.getArtist());
        artistView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewArtist = new Intent(DinosaurDetailActivity.this, ArtistDetailActivity.class);
                viewArtist.putExtra(ArtistDetailActivity.KEY_NAME, mDino.getArtist());
                startActivity(viewArtist);
            }
        });
        sponsorView.setText(mDino.getSponsor());
        sponsorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewSponsors = new Intent(DinosaurDetailActivity.this, NavigationDrawerActivity.class);
                viewSponsors.putExtra(NavigationDrawerActivity.KEY_SHOW_SPONSORS, true);
                startActivity(viewSponsors);
            }
        });
        // large image
        int heroImageId = mDino.getImageId(this);
        if (heroImageId > 0) {
            ImageView heroImageView = (ImageView) findViewById(R.id.hero_image);
            heroImageView.setImageResource(heroImageId);
        }

        showMapFragment();

        CheckBox checkBox = (CheckBox) findViewById(R.id.visited_checkbox);
        // refresh check box state
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean checked = sharedPrefs.getBoolean(mDino.getQualifiedName(), false);
        checkBox.setChecked(checked);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update if this dinosaur has been visited
                updatePreferences(mDino, isChecked);
            }
        });

        final FloatingActionButton cameraButton = (FloatingActionButton) findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // spin the camera icon before proceeding
                ValueAnimator animator = ValueAnimator.ofFloat(0f, 360f);
                animator.setDuration(500)
                        .setInterpolator(new AccelerateDecelerateInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float degrees = (float) animation.getAnimatedValue();
                        cameraButton.setIconRotation(degrees);
                    }
                });
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        // do nothing
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // launch the camera so user can take a picture
                        Intent launchCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File outFile = null;
                        try {
                            outFile = createImageFile();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                        if (outFile != null) {
                            launchCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile));
                            startActivityForResult(launchCamera, REQUEST_IMAGE_CAPTURE);
                        }
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // prompt the gallery to scan for new images
                addImageToGallery();
                // bring the user to the gallery to see the image
                Intent view = new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.fromFile(mImageFile), "image/*");
                startActivity(view);
            }
        }
    }

    private void updatePreferences(Dinosaur dinosaur, boolean visited) {
        // update if the person has visited this dinosaur or not
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(DinosaurDetailActivity.this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(dinosaur.getQualifiedName(), visited);
        // udpate the total number of dinosaurs visited
        int numVisited = sharedPrefs.getInt(getResources().getString(R.string.key_dinos_visited), 0);
        numVisited = visited ? numVisited + 1 : numVisited - 1;
        editor.putInt(getResources().getString(R.string.key_dinos_visited), numVisited);
        editor.commit();
        // show the user how many dinosaurs they have left to visit
        if (visited) {
            Toast.makeText(DinosaurDetailActivity.this, 40 - numVisited + " dinosaurs to go!", Toast.LENGTH_SHORT).show();
        }
        updateMapMarker(dinosaur, visited);
    }

    private void updateMapMarker(Dinosaur dinosaur, boolean visited) {
        mGoogleMap.clear();
        MarkerOptions options = new MarkerOptions()
                .position(dinosaur.getLocation())
                .icon(BitmapDescriptorFactory.defaultMarker(visited ? BitmapDescriptorFactory.HUE_MAGENTA : BitmapDescriptorFactory.HUE_ORANGE));
        mGoogleMap.addMarker(options);
    }

    private void addImageToGallery() {
        Intent refreshGallery = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        refreshGallery.setData(Uri.fromFile(mImageFile));
        sendBroadcast(refreshGallery);
    }

    private File createImageFile() throws IOException {
        String fileName = mDino.getQualifiedName() + "_" +
                new SimpleDateFormat("hhmmssMMddyy").format(System.currentTimeMillis()) +
                ".png";
        mImageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                fileName);
        return mImageFile;
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

    private void showMapFragment() {
        GoogleMapOptions options = new GoogleMapOptions()
                .liteMode(true) // this map will just serve as a button
                .mapToolbarEnabled(false)
                .camera(new CameraPosition.Builder()
                        .target(mDino.getLocation())
                        .zoom(DEFAULT_ZOOM)
                        .build());
        final MapFragment mapFragment = MapFragment.newInstance(options);
        getFragmentManager().beginTransaction().add(R.id.map_container, mapFragment).commit();
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                // mark this dinosaur's location on the map
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(mDino.getLocation())
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                mDino.wasVisited(DinosaurDetailActivity.this) ? BitmapDescriptorFactory.HUE_MAGENTA : BitmapDescriptorFactory.HUE_ORANGE));
                googleMap.addMarker(markerOptions);

                // when the map is clicked, open the larger map fragment
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        mapFragment.getView().playSoundEffect(SoundEffectConstants.CLICK);
                        viewLargeMap();
                    }
                });
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        mapFragment.getView().playSoundEffect(SoundEffectConstants.CLICK);
                        viewLargeMap();
                        return true;
                    }
                });
                mGoogleMap = googleMap;
            }
        });
    }

    private void viewLargeMap() {
        Intent viewMap = new Intent(DinosaurDetailActivity.this, NavigationDrawerActivity.class);
        LatLng target = mDino.getLocation();
        viewMap.putExtra(NavigationDrawerActivity.KEY_MAP_TARGET_LATITUDE, target.latitude)
                .putExtra(NavigationDrawerActivity.KEY_MAP_TARGET_LONGITUDE, target.longitude);
        startActivity(viewMap);
    }

    public static final int REQUEST_IMAGE_CAPTURE = 1;

    public static final int DEFAULT_ZOOM = 18;

    private File mImageFile;
    private GoogleMap mGoogleMap;
    private Dinosaur mDino;
}
