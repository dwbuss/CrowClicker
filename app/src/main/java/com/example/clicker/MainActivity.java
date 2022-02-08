package com.example.clicker;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointListAdapter;
import com.example.clicker.objectbo.Point_;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private static final String TAG = "MainActivity";
    private static final String KWS_SEARCH = "wakeup";
    private static final String LOST = "lost";
    private static final String FOLLOW = "follow";
    private static final String CATCH = "catch";
    private static final String MENU_SEARCH = "menu";
    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "crow clicker";
    private final static String default_notification_channel_id = "default";
    private static final int pic_id = 123;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private final List<Point> pointList = new ArrayList<>();
    SupportMapFragment mapFragment;
    private PointListAdapter pointListAdapter;
    private final GoogleMap.OnInfoWindowLongClickListener onInfoWindowLongClickListener = new GoogleMap.OnInfoWindowLongClickListener() {
        @Override
        public void onInfoWindowLongClick(final Marker marker) {
            Point point = (Point) marker.getTag();
            showDialogUpdate(point, marker);
        }
    };
    private final GoogleMap.OnMarkerDragListener onMarkerDragListener = (new GoogleMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {
        }

        @Override
        public void onMarkerDrag(Marker marker) {
        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            Point point = (Point) marker.getTag();
            point.setLat(marker.getPosition().latitude);
            point.setLon(marker.getPosition().longitude);
            pointListAdapter.addOrUpdatePoint(point);
        }
    });
    private Map<String, Float> colors;
    private boolean follow = false;
    private boolean northUp = false;
    private GoogleMap mMap;
    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            if (follow) {
                LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(coordinate)
                        .bearing(location.getBearing())
                        .zoom(mMap.getCameraPosition().zoom)
                        .build();
                if (northUp)
                    cameraPosition = new CameraPosition.Builder()
                            .target(coordinate)
                            .zoom(mMap.getCameraPosition().zoom)
                            .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
    private final GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
            if (!follow || northUp) {
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                View mMyLocationButtonView = mapFragment.getView().findViewWithTag("GoogleMapMyLocationButton");
                mMyLocationButtonView.setBackgroundColor(Color.RED);
                northUp = false;
                follow = true;
            } else if (follow) {
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                View mMyLocationButtonView = mapFragment.getView().findViewWithTag("GoogleMapMyLocationButton");
                mMyLocationButtonView.setBackgroundColor(Color.GREEN);
                northUp = true;
            }
            return false;
        }
    };
    private final GoogleMap.OnCameraMoveStartedListener onCameraMoveStartedListener = (new GoogleMap.OnCameraMoveStartedListener() {
        @Override
        public void onCameraMoveStarted(int reason) {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                follow = false;
                northUp = true;
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                View mMyLocationButtonView = mapFragment.getView().findViewWithTag("GoogleMapMyLocationButton");
                mMyLocationButtonView.setBackgroundColor(Color.GRAY);
            }
        }
    });
    private boolean visible = false;
    private float zoomLevel = 10;
    private final GoogleMap.OnCameraMoveListener onCameraMoverListener = new GoogleMap.OnCameraMoveListener() {
        @Override
        public void onCameraMove() {
            var zoom = mMap.getCameraPosition().zoom;
            System.err.println("ZOOM " + zoom);
            if (zoom > zoomLevel && !visible) {
                markers.forEach(m -> m.setVisible(true));
                visible = true;
            }
            if (zoom < zoomLevel && visible) {
                markers.forEach(m -> m.setVisible(false));
                visible = false;
            }
        }
    };

    private final GoogleMap.OnMapLongClickListener onMyMapLongClickListener = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(final LatLng latLng) {
            String[] contactType = {"CATCH", "CONTACT", "FOLLOW"};
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Choose an Action")
                    .setItems(contactType, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Location loc = new Location(LocationManager.GPS_PROVIDER);
                            loc.setLongitude(latLng.longitude);
                            loc.setLatitude(latLng.latitude);
                            if (which == 0)
                                addPoint("CATCH", loc);
                            if (which == 1)
                                addPoint("CONTACT", loc);
                            if (which == 2)
                                addPoint("FOLLOW", loc);
                        }
                    }).show();
        }
    };
    private LocationManager locationManager;
    private MyReceiver solunarReciever;
    private List<Marker> markers;
    private SheetAccess sheets;

    private static String getExternalStoragePath(Context mContext, boolean is_removable) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removable == removable) {
                    return path;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "failure", e);
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        colors = new HashMap<>();
        colors.put("CATCH", BitmapDescriptorFactory.HUE_RED);
        colors.put("FOLLOW", BitmapDescriptorFactory.HUE_BLUE);
        colors.put("CONTACT", BitmapDescriptorFactory.HUE_YELLOW);
        setContentView(R.layout.activity_main);
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.INTERNET);
        permissions.add(Manifest.permission.SEND_SMS);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermission()) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), ALL_PERMISSIONS_RESULT);
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 3, locationListenerGPS);
        } catch (Exception e) {
            Log.e(TAG, "Something happened with LocationManager?", e);
        }

        solunarReciever = new MyReceiver(getLocation());

        sheets = new SheetAccess(getApplicationContext());
        registerReceiver(solunarReciever, new IntentFilter(Intent.ACTION_TIME_TICK));
        getLocation();
        initView();
    }

    public void openCamera(View view) {
        PackageManager packman = getPackageManager();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String pack = intent.resolveActivity(packman).getPackageName();
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pack);
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Toast.makeText(MainActivity.this, "There is no package available in android", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(solunarReciever);
    }

    public void initView() {
        Solunar solunar = new Solunar();
        solunar.populate(getLocation(), GregorianCalendar.getInstance());
        TextView majorText = findViewById(R.id.majorLbl);
        TextView minorText = findViewById(R.id.minorLbl);
        majorText.setText(solunar.major);
        minorText.setText(solunar.minor);
        if (solunar.isMajor)
            flash(majorText);

        if (solunar.isMinor)
            flash(minorText);

        ((ImageButton) findViewById(R.id.forecastButton)).setImageResource(solunar.moonPhaseIcon);
        pointListAdapter = new PointListAdapter(getApplicationContext(), pointList);
        pointListAdapter.updatePoints();
        refreshCounts();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int tripLength = Integer.parseInt(prefs.getString("TripLength", "0"));
        Calendar today = GregorianCalendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.add(Calendar.DATE, 0 - tripLength);
        BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
        Box<Point> pointBox = boxStore.boxFor(Point.class);
        String label = "label";
        if (mMap != null) {
            mMap.clear();
            markers.clear();
            List<Point> points = pointBox.query()
                    .greater(Point_.timeStamp, today.getTime())
                    .or()
                    .equal(Point_.name, label)
                    .build().find();
            for (Point p : points) {
                addPointMarker(p);
            }
            addCrowLayer();
        }
    }

    private void flash(TextView textObj) {
        ObjectAnimator animator = ObjectAnimator.ofInt(textObj, "backgroundColor", Color.TRANSPARENT, Color.BLUE);
        animator.setDuration(500);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(Animation.INFINITE);
        animator.start();
    }

    public void refreshCounts() {
        BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
        Box<Point> pointBox = boxStore.boxFor(Point.class);
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        ((Button) findViewById(R.id.catchBtn)).setText(Long.toString(pointBox.query().equal(Point_.contactType, "CATCH").greater(Point_.timeStamp, today.getTime()).build().count()));
        ((Button) findViewById(R.id.contactBtn)).setText(Long.toString(pointBox.query().equal(Point_.contactType, "CONTACT").greater(Point_.timeStamp, today.getTime()).build().count()));
        ((Button) findViewById(R.id.followBtn)).setText(Long.toString(pointBox.query().equal(Point_.contactType, "FOLLOW").greater(Point_.timeStamp, today.getTime()).build().count()));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.about:
                Intent forecast = new Intent(this, ForecastActivity.class);
                forecast.putExtra("LOCATION", getLocation());
                startActivity(forecast);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.clicker_menu, menu);
        return true;
    }

    private boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int result3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int result4 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET);
        int result5 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return (result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED && result4 == PackageManager.PERMISSION_GRANTED && result5 == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted .");
                } else {
                    Log.e("value", "Permission Denied.");
                }
                break;
        }
    }

    public void addContact(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int soundBite = R.raw.cc_yousuck2;
        if (!prefs.getBoolean("Friendly", true)) {
            Random ran = new Random();
            List<Integer> tones = new LinkedList<>();
            tones.add(R.raw.f_upped);
            tones.add(R.raw.cc_yousuck3);
            soundBite = tones.get(ran.nextInt(pointList.size()));
        }
        MediaPlayer song = MediaPlayer.create(getApplicationContext(), soundBite);
        song.start();
        addPoint("CONTACT");
    }

    public void addFollow(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int soundBite = R.raw.cc_follow2;
        if (!prefs.getBoolean("Friendly", true)) {
            Random ran = new Random();
            List<Integer> tones = new LinkedList<>();
            tones.add(R.raw.wtf_lookin);
            tones.add(R.raw.cc_follow3);
            soundBite = tones.get(ran.nextInt(pointList.size()));
        }
        MediaPlayer song = MediaPlayer.create(getApplicationContext(), soundBite);
        song.start();
        addPoint("FOLLOW");
    }

    public void addCatch(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int soundBite = R.raw.cc_nice2;
        if (!prefs.getBoolean("Friendly", true)) {
            Random ran = new Random();
            List<Integer> tones = new LinkedList<>();
            tones.add(R.raw.cc_nice3);
            tones.add(R.raw.g_bitch);
            soundBite = tones.get(ran.nextInt(pointList.size()));
        }
        MediaPlayer song = MediaPlayer.create(getApplicationContext(), soundBite);
        song.start();
        addPoint("CATCH");
    }

    public void addPoint(String contactType) {
        if (getLocation() != null) {
            addPoint(contactType, getLocation());
        }
    }

    public void addPoint(String contactType, Location loc) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("Username", null);
        final Point point = new Point(0, username, contactType, loc.getLongitude(), loc.getLatitude());
        final Weather weather = new Weather();
        weather.populate(loc.getLatitude(), loc.getLongitude(), getApplicationContext(), new VolleyCallBack() {
            @Override
            public void onSuccess() {
                point.setAirTemp(weather.temperature);
                point.setDewPoint(weather.dewPoint);
                point.setWindSpeed(weather.windSpeed);
                point.setHumidity(weather.humidity);
                point.setPressure(weather.pressure);
                point.setCloudCover(weather.cloudCover);
                point.setWindDir(weather.windDir);
                pointListAdapter.addOrUpdatePoint(point);
                pointListAdapter.updatePoints();

                showDialogUpdate(point, addPointMarker(point));
                refreshCounts();
            }

            @Override
            public void onFailure() {
                showDialogUpdate(point, addPointMarker(point));
                refreshCounts();
            }
        });
    }

    private Marker addPointMarker(Point point) {
        if (mMap != null) {
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(point.getLat(), point.getLon()))
                    .title("Hold to Edit")
                    .draggable(true)
                    .anchor(0.5f, 0.5f)
                    .visible(false)
                    .flat(true)
                    .zIndex(0)
                    .icon(getMarker(point)));
            m.setTag(point);
            if (mMap.getCameraPosition().zoom > zoomLevel)
                m.setVisible(true);
            markers.add(m);
            return m;
        }
        return null;
    }

    private BitmapDescriptor getMarker(Point point) {

        if (point.getContactType().equals("CATCH") || point.getName().equalsIgnoreCase("label")) {
            String text = point.getName();
            if (point.getName() == null) {
                point.setName("No Name");
            }
            if (point.getName().equalsIgnoreCase("label"))
                text = point.getNotes();
            if (text == null)
                text = " ";
            Paint strokePaint = new Paint();
            strokePaint.setTextAlign(Paint.Align.LEFT);
            strokePaint.setARGB(255, 255, 255, 255);
            strokePaint.setTextSize(23.0f);
            strokePaint.setTypeface(Typeface.DEFAULT_BOLD);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(4);

            Paint textPaint = new Paint();
            textPaint.setARGB(255, 0, 0, 0);
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(23.0f);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float height2 = fm.bottom - fm.top + fm.leading;
            float baseline = -textPaint.ascent(); // ascent() is negative
            int width = (int) (textPaint.measureText(text) + 5.0f); // round
            int height = (int) (baseline + textPaint.descent() + 0.0f);

            int trueWidth = width;
            if (width > height) height = width;
            else width = height;
            Bitmap image = Bitmap.createBitmap(width, ((int) height2), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);
            canvas.drawText(text, width / 2 - trueWidth / 2, baseline, strokePaint);
            canvas.drawText(text, width / 2 - trueWidth / 2, baseline, textPaint);
            return BitmapDescriptorFactory.fromBitmap(image);
        } else if (point.getContactType().equals("CONTACT"))
            return BitmapDescriptorFactory.fromResource(R.drawable.gm_contact);
        return BitmapDescriptorFactory.fromResource(R.drawable.gm_follow);
    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private Location getLocation() {
        try {
            return getLastKnownLocation();//locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Log.e(TAG, "This is bad.", e);
            return null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        markers = new LinkedList();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        Location location = getLocation();
        LatLng crow = new LatLng(location.getLatitude(), location.getLongitude());// new LatLng(49.217314, -93.863248);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(crow, (float) 16.0));
        mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        mMap.setOnMapLongClickListener(onMyMapLongClickListener);
        mMap.setOnCameraMoveStartedListener(onCameraMoveStartedListener);
        mMap.setOnMarkerDragListener(onMarkerDragListener);
        mMap.setOnInfoWindowLongClickListener(onInfoWindowLongClickListener);
        mMap.setOnCameraMoveListener(onCameraMoverListener);

        addCrowLayer();
        BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
        Box<Point> pointBox = boxStore.boxFor(Point.class);
        List<Point> points = pointBox.getAll();
        for (Point p : points) {
            addPointMarker(p);
        }
    }

    private void addCrowLayer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean locationLocal = prefs.getBoolean("MapLocation", true);
        try {
            File file;
            if (locationLocal) {
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Crow.mbtiles");
                // File sdcard = new File("/mnt/sdcard/");
                //  file = new File(sdcard, "Crow.mbtiles");
            } else {
                File sdcard = new File(getExternalStoragePath(getApplicationContext(), true));
                file = new File(sdcard, "Crow.mbtiles");
            }
            if (!file.exists())
                Toast.makeText(this, "File not Found" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            TileProvider tileProvider = new ExpandedMBTilesTileProvider(file, 256, 256);
            mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
        } catch (Exception e) {
            Log.e(TAG, "Failed to load Crow.mbtiles", e);
            Toast.makeText(this, "Failed to load mbtiles " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDialogUpdate(final Point point, final Marker marker) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.update_dialog);
        ((EditText) dialog.findViewById(R.id.name)).setText(point.getName());
        ((EditText) dialog.findViewById(R.id.contactType)).setText(point.getContactType());
        String timeStamp = new SimpleDateFormat("MM-dd-yyyy h:mm a").format(point.getTimeStamp());
        ((TextView) dialog.findViewById(R.id.timeStamp)).setText(timeStamp);
        ((TextView) dialog.findViewById(R.id.lat)).setText(Double.toString(point.getLat()));
        ((TextView) dialog.findViewById(R.id.lon)).setText(Double.toString(point.getLon()));
        ((EditText) dialog.findViewById(R.id.bait)).setText(point.getBait());
        ((EditText) dialog.findViewById(R.id.fishSize)).setText(point.getFishSize());
        ((EditText) dialog.findViewById(R.id.airtemp)).setText(point.getAirTemp());
        ((EditText) dialog.findViewById(R.id.watertemp)).setText(point.getWaterTemp());
        ((EditText) dialog.findViewById(R.id.windSpeed)).setText(point.getWindSpeed());
        ((EditText) dialog.findViewById(R.id.windDir)).setText(point.getWindDir());
        ((EditText) dialog.findViewById(R.id.cloudCover)).setText(point.getCloudCover());
        ((EditText) dialog.findViewById(R.id.dewPoint)).setText(point.getDewPoint());
        ((EditText) dialog.findViewById(R.id.pressure)).setText(point.getPressure());
        ((EditText) dialog.findViewById(R.id.humidity)).setText(point.getHumidity());
        ((EditText) dialog.findViewById(R.id.notes)).setText(point.getNotes());

        int width = (int) (this.getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (this.getResources().getDisplayMetrics().heightPixels * 0.95);
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button btnPush = dialog.findViewById(R.id.btnPush);
        btnPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (point.getSheetId() <= 0) {
                    point.setSheetId(point.getId());
                }
                savePoint(view, point, dialog);
                sheets.storePoint(point);
            }
        });
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogDelete = new AlertDialog.Builder(MainActivity.this);
                dialogDelete.setTitle("Warning!!");
                dialogDelete.setMessage("Are you sure to delete this point?");
                dialogDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
                            Box<Point> pointBox = boxStore.boxFor(Point.class);
                            pointBox.remove(point);
                            marker.remove();

                            sheets.deletePoint(point);
                            refreshCounts();
                            Toast.makeText(MainActivity.this, "Delete successfully", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e("error", e.getMessage());
                        }
                        dialogInterface.dismiss();
                        dialog.dismiss();
                    }
                });
                dialogDelete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        dialog.dismiss();
                    }
                });
                dialogDelete.show();
            }
        });

        Button btnSave = dialog.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePoint(view, point, dialog);
            }
        });
        Button weatherUpdate = dialog.findViewById(R.id.btnWeather);
        weatherUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    final Weather weather = new Weather();
                    weather.populate(point.getLat(), point.getLon(), point.getTimeStamp(), getApplicationContext(), new VolleyCallBack() {
                        @Override
                        public void onSuccess() {
                            point.setAirTemp(weather.temperature);
                            point.setDewPoint(weather.dewPoint);
                            point.setWindSpeed(weather.windSpeed);
                            point.setHumidity(weather.humidity);
                            point.setPressure(weather.pressure);
                            point.setCloudCover(weather.cloudCover);
                            point.setWindDir(weather.windDir);
                            pointListAdapter.addOrUpdatePoint(point);
                            pointListAdapter.updatePoints();
                        }

                        @Override
                        public void onFailure() {
                        }
                    });

                    ((EditText) dialog.findViewById(R.id.airtemp)).setText(point.getAirTemp());
                    ((EditText) dialog.findViewById(R.id.watertemp)).setText(point.getWaterTemp());
                    ((EditText) dialog.findViewById(R.id.windSpeed)).setText(point.getWindSpeed());
                    ((EditText) dialog.findViewById(R.id.windDir)).setText(point.getWindDir());
                    ((EditText) dialog.findViewById(R.id.cloudCover)).setText(point.getCloudCover());
                    ((EditText) dialog.findViewById(R.id.dewPoint)).setText(point.getDewPoint());
                    ((EditText) dialog.findViewById(R.id.pressure)).setText(point.getPressure());
                    ((EditText) dialog.findViewById(R.id.humidity)).setText(point.getHumidity());
                } catch (Exception error) {
                    Log.e("Update Weather error", error.getMessage());
                }
            }
        });
    }

    private void savePoint(View view, Point point, Dialog dialog) {
        try {
            point.setName(((EditText) dialog.findViewById(R.id.name)).getText().toString().trim());
            point.setContactType(((EditText) dialog.findViewById(R.id.contactType)).getText().toString().trim());
            String timeStampStr = ((EditText) dialog.findViewById(R.id.timeStamp)).getText().toString().trim();

            Date timeStamp = new SimpleDateFormat("MM-dd-yyyy h:mm a").parse(timeStampStr);
            point.setTimeStamp(new Timestamp(timeStamp.getTime()));
            point.setBait(((EditText) dialog.findViewById(R.id.bait)).getText().toString().trim());
            point.setFishSize(((EditText) dialog.findViewById(R.id.fishSize)).getText().toString().trim());
            point.setAirTemp(((EditText) dialog.findViewById(R.id.airtemp)).getText().toString().trim());
            point.setWaterTemp(((EditText) dialog.findViewById(R.id.watertemp)).getText().toString().trim());
            point.setWindSpeed(((EditText) dialog.findViewById(R.id.windSpeed)).getText().toString().trim());
            point.setWindDir(((EditText) dialog.findViewById(R.id.windDir)).getText().toString().trim());
            point.setCloudCover(((EditText) dialog.findViewById(R.id.cloudCover)).getText().toString().trim());
            point.setDewPoint(((EditText) dialog.findViewById(R.id.dewPoint)).getText().toString().trim());
            point.setPressure(((EditText) dialog.findViewById(R.id.pressure)).getText().toString().trim());
            point.setHumidity(((EditText) dialog.findViewById(R.id.humidity)).getText().toString().trim());
            point.setNotes(((EditText) dialog.findViewById(R.id.notes)).getText().toString().trim());
            boolean notify = ((CheckBox) dialog.findViewById(R.id.notify)).isChecked();
            BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
            Box<Point> pointBox = boxStore.boxFor(Point.class);
            pointBox.put(point);
            dialog.dismiss();
            if (notify) {
                sendMessage(point.getMessage(), point.getContactType());
            }
            Toast.makeText(getApplicationContext(), "Save Successful", Toast.LENGTH_SHORT).show();
        } catch (Exception error) {
            Log.e("Update error", error.getMessage());
        }
    }

    public void openSettings(View view) {
        Intent settings = new Intent(this, SettingsActivity.class);
        startActivity(settings);
    }

    public void forcast(View view) {
        Intent forecast = new Intent(this, ForecastActivity.class);
        forecast.putExtra("LOCATION", getLocation());
        startActivity(forecast);
    }

    public void switchLayer(View view) {
        final CharSequence[] items = {"None", "Normal", "Satellite", "TERRAIN", "Hybrid",};
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

        dialog.setTitle("Select Layer");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int mapType) {
                mMap.setMapType(mapType);
            }
        });
        dialog.show();
    }

    private void sendMessage(String message, String action) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String notification = "";
        if (action.equalsIgnoreCase("CATCH"))
            notification = prefs.getString("Catch Notification", "");
        if (action.equalsIgnoreCase("FOLLOW"))
            notification = prefs.getString("Follow Notification", "");
        if (action.equalsIgnoreCase("CONTACT"))
            notification = prefs.getString("Lost Notification", "");
        if (!notification.isEmpty()) {
            String[] list = notification.split(",");
            SmsManager smgr = SmsManager.getDefault();
            int length = Array.getLength(list);
            for (int i = 0; i < length; i++) {
                smgr.sendTextMessage(list[i], null, message, null, null);
            }
        }
    }
}
