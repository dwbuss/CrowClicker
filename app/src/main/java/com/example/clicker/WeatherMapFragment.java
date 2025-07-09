package com.example.clicker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String RAINVIEWER_URL = "https://api.rainviewer.com/public/weather-maps.json";

    private List<Long> radarTimestamps = new ArrayList<>();
    private GoogleMap map;
    private Button playButton;
    private final int FRAME_COUNT = 10;
    private final int FRAME_INTERVAL_MS = 500;
    private final List<TileOverlay> overlays = new ArrayList<>();
    private int currentFrame = FRAME_COUNT - 1;
    private Handler handler = new Handler();
    private boolean isPlaying = false;

    private final Runnable playLoop = new Runnable() {
        @Override
        public void run() {
            if (!isPlaying || overlays.isEmpty()) return;

            showFrame(currentFrame);

            if (currentFrame >= FRAME_COUNT - 1) {
                // Stop after last frame
                isPlaying = false;
                playButton.setText("Play");
            } else {
                currentFrame++;
                handler.postDelayed(this, FRAME_INTERVAL_MS);
            }
        }
    };

    private void fetchRadarFramesAndShow() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(RAINVIEWER_URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to load radar data", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray frames = json.getJSONObject("radar").getJSONArray("past");
                    radarTimestamps.clear();

                    for (int i = 0; i < frames.length(); i++) {
                        radarTimestamps.add(frames.getJSONObject(i).getLong("time"));
                    }

                    requireActivity().runOnUiThread(() -> {
                        if (map != null) {
                            loadRadarOverlays();
                            showFrame(radarTimestamps.size() - 1); // latest
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void loadRadarOverlays() {
        overlays.clear();

        for (long timestamp : radarTimestamps) {
            final long frameTime = timestamp;
            TileProvider tileProvider = new UrlTileProvider(256, 256) {
                @Override
                public URL getTileUrl(int x, int y, int zoom) {
                    try {
                        return new URL(String.format(Locale.US,
                                "https://tilecache.rainviewer.com/v2/radar/%d/256/%d/%d/%d/2/1_1.png",
                                frameTime, zoom, x, y));
                    } catch (MalformedURLException e) {
                        return null;
                    }
                }
            };

            TileOverlay overlay = map.addTileOverlay(new TileOverlayOptions()
                    .tileProvider(tileProvider)
                    .visible(false));
            overlays.add(overlay);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather_map, container, false);
        playButton = view.findViewById(R.id.btn_play);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


        playButton.setOnClickListener(v -> {
            if (overlays.isEmpty()) {
                Toast.makeText(requireContext(), "No radar data to play.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isPlaying) {
                // Start from beginning
                currentFrame = 0;
                isPlaying = true;
                playButton.setText("Pause");
                handler.post(playLoop);
            } else {
                // Stop early
                isPlaying = false;
                playButton.setText("Play");
                handler.removeCallbacks(playLoop);
                showFrame(FRAME_COUNT - 1); // reset to current
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        map.setMyLocationEnabled(true);
        Location location = LocationHelper.CURRENT_LOCATION(getContext());
        if (location != null) {
            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 8f)); // ~100mi
            fetchRadarFramesAndShow();
        }
    }

    private void showFrame(int frameIndex) {
        for (int i = 0; i < overlays.size(); i++) {
            overlays.get(i).setVisible(i == frameIndex);
        }
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacks(playLoop);
        super.onDestroyView();
    }
}
