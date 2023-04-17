package com.example.miamproche;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ExecutorService mExecutor;
    private Handler mHandler;
    private FusedLocationProviderClient mLocationProvider;
    private LocationRequest mRequest;
    private LocationCallback mLocationCallback;
    private Bitmap mMarkerBmp;
    private final int MY_REQUEST_CODE = 1;
    private final int REQUEST_CHECK_SETTINGS = 2;
    private final int IMAGE_SIZE = 128;
    private final int BORDER = 3;
    private DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        myRef = FirebaseDatabase.getInstance(" https://miamproche-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        findViewById(R.id.search_button).setOnClickListener(v -> startActivity(new Intent(this, SearchableActivity.class)));
        findViewById(R.id.settings_button).setOnClickListener(v -> startActivity(new Intent(this, ProducteurActivity.class)));

        mExecutor = Executors.newSingleThreadExecutor();
        mHandler = new Handler(Looper.getMainLooper());
        mLocationProvider = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {};

        int square_size = IMAGE_SIZE + 4 * BORDER, arrow_height = IMAGE_SIZE / 4;
        mMarkerBmp = Bitmap.createBitmap(square_size, square_size + arrow_height, Bitmap.Config.ARGB_8888);
        Canvas cvs = new Canvas(mMarkerBmp);
        Paint paint = new Paint();

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * BORDER);
        cvs.drawRect(BORDER, BORDER, square_size - BORDER, square_size - BORDER, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        Path path = new Path();
        path.lineTo((float) square_size / 2 - 10, square_size);
        path.lineTo((float) square_size / 2 + 10, square_size);
        path.lineTo((float) square_size / 2, square_size + arrow_height);
        path.close();
        cvs.drawPath(path, paint);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        runOnUiThread(() -> {
            if (mapFragment != null)
                mapFragment.getMapAsync(this);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        mMap.setOnMarkerClickListener(marker -> {
            startActivity(new Intent(this, ProductPage.class));
            return false;
        });
        myRef.child("Produit").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    MarkerOptions options = new MarkerOptions().title(child.child("nom_produit").getValue(String.class));
                    myRef.child("Producteur").child(String.valueOf(child.child("id_producteur").getValue(long.class))).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Double latitude = task.getResult().child("latitude").getValue(Double.class);
                            Double longitude = task.getResult().child("longitude").getValue(Double.class);
                            if (latitude != null && longitude != null) {
                                options.position(new LatLng(latitude, longitude));
                                addMarker(child.child("image_produit").getValue(String.class), options);
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        LatLng paris = new LatLng(48.86, 2.33);
        MarkerOptions options = new MarkerOptions().position(paris).title("yoyo53");
        addMarker("https://cdn.discordapp.com/icons/640609774823538688/794e2c583d99b516a9cb51a5a95a49a6.webp?size=256", options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(paris, 10));

        mRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();

        if(isLocationEnabled(this)) {
            checkLocationPermissions();
        }
        else {
            SwitchGPS_ON();
        }
    }

    public void SwitchGPS_ON(){
        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();
        locationSettingsRequestBuilder.addLocationRequest(mRequest);
        locationSettingsRequestBuilder.setAlwaysShow(true);

        LocationServices.getSettingsClient(this)
                .checkLocationSettings(locationSettingsRequestBuilder.build())
                .addOnSuccessListener(this, response -> {})
                .addOnFailureListener(this, exception -> {
            if (exception instanceof ResolvableApiException){
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) exception;
                    resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                }
                catch (IntentSender.SendIntentException ignored) {}
            }
        });
    }

    public static Boolean isLocationEnabled(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            // new method of API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        }
        else {
            // deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    private void addMarker(final String url, final MarkerOptions options) {
        mExecutor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setDoInput(true);
                conn.connect();
                Bitmap image = BitmapFactory.decodeStream(conn.getInputStream());

                Bitmap marker_bmp = mMarkerBmp.copy(mMarkerBmp.getConfig(), true);
                image = Bitmap.createScaledBitmap(image, IMAGE_SIZE, IMAGE_SIZE, false);
                new Canvas(marker_bmp).drawBitmap(image, 2 * BORDER, 2 * BORDER, null);

                MarkerOptions result = options.icon(BitmapDescriptorFactory.fromBitmap(marker_bmp));
                mHandler.post(() -> mMap.addMarker(result));
            }
            catch (Exception ignored) {}
            finally {
                if (conn != null)
                    conn.disconnect();
            }
        });
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_REQUEST_CODE);
        }
        else {
            activateLocation();
        }
    }

    private void activateLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationProvider.requestLocationUpdates(mRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);

            mLocationProvider.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(loc -> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 20)));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CHECK_SETTINGS == requestCode) {
            checkLocationPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activateLocation();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocationProvider != null) {
            mLocationProvider.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocationProvider != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mLocationProvider.requestLocationUpdates(mRequest, mLocationCallback, Looper.myLooper());
        }
    }
}