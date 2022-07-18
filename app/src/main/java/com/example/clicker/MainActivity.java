package com.example.clicker;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointsHelper;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import io.objectbox.Box;
import io.objectbox.BoxStore;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
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
    private final float zoomLevel = 10;
    SupportMapFragment mapFragment;
    private PointsHelper pointsHelper;
    private Map<String, Float> colors;
    private boolean follow = false;
    private boolean northUp = false;
    private GoogleMap mMap;
    private boolean visible = false;
    private LocationManager locationManager;
    private MyReceiver solunarReciever;
    private List<Marker> markers;
    private SheetAccess sheets;
    private Point gotoPoint = null;

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
            pointsHelper.addOrUpdatePoint(point);
        }
    });

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

    private final GoogleMap.OnCameraMoveListener onCameraMoverListener = new GoogleMap.OnCameraMoveListener() {
        @Override
        public void onCameraMove() {
            var zoom = mMap.getCameraPosition().zoom;
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

    private final GoogleMap.OnInfoWindowLongClickListener onInfoWindowLongClickListener = new GoogleMap.OnInfoWindowLongClickListener() {
        @Override
        public void onInfoWindowLongClick(final Marker marker) {
            Point point = (Point) marker.getTag();
            Intent editPoint = new Intent(MainActivity.this, PointActivity.class);
            editPoint.putExtra("point", point);
            editPoint.putExtra("shouldNotify", true);
            startActivity(editPoint);
            refreshCounts();
        }
    };

    private final GoogleMap.OnMapLongClickListener onMyMapLongClickListener = latLng -> {
        String[] contactType = {"CATCH", "CONTACT", "FOLLOW"};
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Choose an Action")
                .setItems(contactType, (dialog, which) -> {
                    Location loc = new Location(LocationManager.GPS_PROVIDER);
                    loc.setLongitude(latLng.longitude);
                    loc.setLatitude(latLng.latitude);
                    if (which == 0)
                        addPoint("CATCH", loc);
                    if (which == 1)
                        addPoint("CONTACT", loc);
                    if (which == 2)
                        addPoint("FOLLOW", loc);
                }).show();
    };

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
        if (getIntent().hasExtra("gotoPoint")) {
            gotoPoint = getIntent().getParcelableExtra("gotoPoint");
        }
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
        permissions.add(Manifest.permission.READ_CONTACTS);

        if (!checkPermission()) {
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

        registerClickerButtons();
        initView();
    }

    private void registerClickerButtons() {
        List<Flic2Button> buttons = Collections.EMPTY_LIST;
        ClickerListener clucker = new ClickerListener(this);

        buttons = Flic2Manager.getInstance().getButtons();
        Log.d(TAG, String.format("Found %d Clickers!", buttons.size()));
        for (Flic2Button button : buttons) {
            button.connect();
            button.addListener(clucker);
        }
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
        pointsHelper = new PointsHelper(getApplicationContext());
        refreshCounts();

        if (mMap != null) {
            mMap.clear();
            markers.clear();
            List<Point> points = filterPoints();
            for (Point p : points) {
                addPointMarker(p);
            }
            addCrowLayer();
        }
    }

    private List<Point> filterPoints() {
        String label = "label";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int tripLength = Integer.parseInt(prefs.getString("TripLength", "0"));
        Calendar today = GregorianCalendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.add(Calendar.DATE, 0 - tripLength);
        BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
        Box<Point> pointBox = boxStore.boxFor(Point.class);
        return pointBox.query()
                .greater(Point_.timeStamp, today.getTime())
                .or()
                .equal(Point_.name, label)
                .build().find();
    }

    private void flash(TextView textObj) {
        ObjectAnimator animator = ObjectAnimator.ofInt(textObj, "backgroundColor", Color.TRANSPARENT, Color.BLUE);
        animator.setDuration(500);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(Animation.INFINITE);
        animator.start();
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
        int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int result3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int result4 = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int result5 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int result6 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

        return (result1 == PackageManager.PERMISSION_GRANTED &&
                result2 == PackageManager.PERMISSION_GRANTED &&
                result3 == PackageManager.PERMISSION_GRANTED &&
                result4 == PackageManager.PERMISSION_GRANTED &&
                result5 == PackageManager.PERMISSION_GRANTED &&
                result6 == PackageManager.PERMISSION_GRANTED);
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

    private void addFromClick(ContactType type) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean friendly = prefs.getBoolean("Friendly", true);
        int soundBite = type.lookupSoundBite(friendly);
        MediaPlayer song = MediaPlayer.create(getApplicationContext(), soundBite);
        song.start();
        addPoint(type.toString());
    }

    public void addFromButton(ContactType contactType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean friendly = prefs.getBoolean("Friendly", true);

        int soundBite = contactType.lookupSoundBite(friendly);
        MediaPlayer song = MediaPlayer.create(getApplicationContext(), soundBite);
        song.start();

        String username = prefs.getString("Username", null);
        String defaultBait = prefs.getString("CurrentBait", "");

        Location loc = getLocation();
        if (loc == null) return;

        final Point point = new Point(0, username, contactType.toString(), loc.getLongitude(), loc.getLatitude());
        point.setBait(defaultBait);
        point.setName(username);

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
                pointsHelper.addOrUpdatePoint(point);
            }

            @Override
            public void onFailure() {
                pointsHelper.addOrUpdatePoint(point);
            }
        });
        addPointMarker(point);
        refreshCounts();
    }

    public void addContact(View view) {
        addFromClick(ContactType.CONTACT);
    }

    public void addFollow(View view) {
        addFromClick(ContactType.FOLLOW);
    }

    public void addCatch(View view) {
        addFromClick(ContactType.CATCH);
    }

    public void addPoint(String contactType) {
        final Location location = getLocation();
        if (location != null) {
            addPoint(contactType, location);
        }
    }

    public void addPoint(String contactType, Location loc) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("Username", null);
        String defaultBait = prefs.getString("CurrentBait", "");
        final Point point = new Point(0, username, contactType, loc.getLongitude(), loc.getLatitude());
        point.setBait(defaultBait);
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
                Log.d(TAG, point.toString());
                Intent addPoint = new Intent(MainActivity.this, PointActivity.class);
                addPoint.putExtra("point", point);
                addPoint.putExtra("shouldNotify", true);
                startActivity(addPoint);
            }

            @Override
            public void onFailure() {
                Toast.makeText(getApplicationContext(), "Unable to retrieve weather data.", Toast.LENGTH_LONG).show();
                Intent addPoint = new Intent(MainActivity.this, PointActivity.class);
                addPoint.putExtra("point", point);
                addPoint.putExtra("shouldNotify", true);
                startActivity(addPoint);
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
            if (point.getName().equalsIgnoreCase("Adam")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_adam);
            } else if (point.getName().equalsIgnoreCase("Amy")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_amy);
            } else if (point.getName().equalsIgnoreCase("Blair")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_blair);
            } else if (point.getName().equalsIgnoreCase("Calvin")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_calvin);
            } else if (point.getName().equalsIgnoreCase("Carey")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_carey);
            } else if (point.getName().equalsIgnoreCase("Chris")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_chris);
            } else if (point.getName().equalsIgnoreCase("Chuck")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_chuck);
            } else if (point.getName().equalsIgnoreCase("Cullen")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_cullen);
            } else if (point.getName().equalsIgnoreCase("Dan")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_dan);
            } else if (point.getName().equalsIgnoreCase("Deb")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_deb);
            } else if (point.getName().equalsIgnoreCase("Eric")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_eric);
            } else if (point.getName().equalsIgnoreCase("Jeff")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_jeff);
            } else if (point.getName().equalsIgnoreCase("Kendra")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_kendra);
            } else if (point.getName().equalsIgnoreCase("Mark")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_mark);
            } else if (point.getName().equalsIgnoreCase("Nicole")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_nicole);
            } else if (point.getName().equalsIgnoreCase("Tony")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_tony);
            }
            if (!point.getName().equalsIgnoreCase("Label")) {
                return BitmapDescriptorFactory.fromResource(R.drawable.gm_dave);
            } else {
                String text = point.getNotes();
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
            }
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
        mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        mMap.setOnMapLongClickListener(onMyMapLongClickListener);
        mMap.setOnCameraMoveStartedListener(onCameraMoveStartedListener);
        mMap.setOnMarkerDragListener(onMarkerDragListener);
        mMap.setOnInfoWindowLongClickListener(onInfoWindowLongClickListener);
        mMap.setOnCameraMoveListener(onCameraMoverListener);

        addCrowLayer();
        List<Point> points = filterPoints();
        for (Point p : points) {
            addPointMarker(p);
        }

        LatLng crow = new LatLng(49.217314, -93.863248);
        if (gotoPoint != null) {
            Log.d(TAG, "found gotoPoint");
            crow = new LatLng(gotoPoint.getLat(), gotoPoint.getLon());
            gotoPoint = null;
        } else {
            Location location = getLocation();
            if (location != null) {
                crow = new LatLng(location.getLatitude(), location.getLongitude());
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(crow, 16.0f));
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
        dialog.setItems(items, (dialogInterface, mapType) -> mMap.setMapType(mapType));
        dialog.show();
    }

    public void refreshCounts() {
        ((Button) findViewById(R.id.catchBtn)).setText(pointsHelper.getDailyCatch());
        ((Button) findViewById(R.id.contactBtn)).setText(pointsHelper.getDailyContact());
        ((Button) findViewById(R.id.followBtn)).setText(pointsHelper.getDailyFollow());
    }
}
