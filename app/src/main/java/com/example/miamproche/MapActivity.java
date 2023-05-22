package com.example.miamproche;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private final ArrayList<LatLng> mLocations = new ArrayList<>();
    private FusedLocationProviderClient mLocationProvider;
    private LocationRequest mRequest;
    private LocationCallback mLocationCallback;
    private Bitmap mMarkerBmp;
    private final int MY_REQUEST_CODE = 1;
    private final int REQUEST_CHECK_SETTINGS = 2;
    private final int IMAGE_SIZE = 128;
    private final int BORDER = 3;
    private DatabaseReference dbRef;
    private final String bucket = FirebaseStorage.getInstance().getReference().getBucket();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbRef = FirebaseDatabase.getInstance().getReference();

        findViewById(R.id.search_button).setOnClickListener(v -> startActivity(new Intent(this, SearchableActivity.class)));
        findViewById(R.id.logout_button).setOnClickListener(v -> startActivity(new Intent(this, LoginPage.class)));
        if (getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("id_producteur", "-1").equals("-1")){
            findViewById(R.id.settings_button).setVisibility(View.GONE);
        }
        else{
            findViewById(R.id.settings_button).setOnClickListener(v -> startActivity(new Intent(this, ProducteurActivity.class)));
        }

        mLocationProvider = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {};

        int square_size = IMAGE_SIZE + 4 * BORDER, arrow_height = IMAGE_SIZE / 4;
        mMarkerBmp = Bitmap.createBitmap(square_size, square_size + arrow_height, Bitmap.Config.ARGB_8888);
        Canvas cvs = new Canvas(mMarkerBmp);
        Paint paint = new Paint();

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * BORDER);
        cvs.drawCircle(square_size / 2f, square_size / 2f, square_size / 2f - BORDER, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        Path path = new Path();
        path.lineTo((float) square_size / 2 - 15, square_size - 10);
        path.lineTo((float) square_size / 2 + 15, square_size - 10);
        path.lineTo((float) square_size / 2, square_size + arrow_height);
        path.close();
        cvs.drawPath(path, paint);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        mMap.setOnMarkerClickListener(marker -> {
            Intent intent = new Intent(this, ProductPage.class);
            intent.putExtra("productID", (String) marker.getTag());
            startActivity(intent);
            return false;
        });

        dbRef.get().addOnSuccessListener(result -> {
            HashMap<Long, DataSnapshot> producteurs = new HashMap<>();
            for (DataSnapshot producteur : result.child("Producteur").getChildren()) {
                producteurs.put(producteur.child("id_producteur").getValue(Long.class), producteur);
            }
            for(DataSnapshot produit : result.child("Produit").getChildren()){
                DataSnapshot producteur = producteurs.get(produit.child("id_producteur").getValue(Long.class));
                if (producteur != null) {
                    Double latitude = producteur.child("latitude").getValue(Double.class);
                    Double longitude = producteur.child("longitude").getValue(Double.class);
                    String productID = String.valueOf(produit.child("id_produit").getValue(Long.class));
                    if (latitude != null && longitude != null) {
                        MarkerOptions options = new MarkerOptions().position(getFreeLocation(latitude, longitude));
                        addMarker(options, productID);
                    }
                }
            }
        });

        mRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();

        if(isLocationEnabled(this)) {
            checkLocationPermissions();
        }
        else {
            SwitchGPS_ON();
        }
    }

    public LatLng getFreeLocation(Double latitude, Double longitude) {
        LatLng location = new LatLng(latitude, longitude);
        int i = 1;
        while (mLocations.contains(location)) {
            location = new LatLng(latitude + i * 0.0002, longitude + i * 0.0002);
            if (!mLocations.contains(location)) {break;}
            location = new LatLng(latitude + i * 0.0002, longitude);
            if (!mLocations.contains(location)) {break;}
            location = new LatLng(latitude + i * 0.0002, longitude - i * 0.0002);
            if (!mLocations.contains(location)) {break;}
            location = new LatLng(latitude, longitude - i * 0.0002);
            if (!mLocations.contains(location)) {break;}
            location = new LatLng(latitude, longitude + i * 0.0002);
            if (!mLocations.contains(location)) {break;}
            location = new LatLng(latitude - i * 0.0002, longitude - i * 0.0002);
            if (!mLocations.contains(location)) {break;}
            location = new LatLng(latitude - i * 0.0002, longitude);
            if (!mLocations.contains(location)) {break;}
            location = new LatLng(latitude - i * 0.0002, longitude + i * 0.0002);
            i++;
        }
        mLocations.add(location);
        return location;
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

    private void addMarker(final MarkerOptions options, final String productID) {
        Glide.with(this).asBitmap()
                .load("https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/Produits%2F" + productID + "?alt=media")
                .into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Bitmap marker_bmp = mMarkerBmp.copy(mMarkerBmp.getConfig(), true);
                Bitmap image = resource;
                int size = Math.min(image.getWidth(), image.getHeight());
                int x_offset = (image.getWidth() - size) / 2, y_offset = (image.getHeight() - size) / 2;
                image = Bitmap.createBitmap(image, x_offset, y_offset, size + x_offset, size + y_offset);
                image = Bitmap.createScaledBitmap(image, IMAGE_SIZE, IMAGE_SIZE, false);

                Bitmap circle_bmp = Bitmap.createBitmap(IMAGE_SIZE, IMAGE_SIZE, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(circle_bmp);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                canvas.drawARGB(0, 0, 0, 0);
                canvas.drawCircle(IMAGE_SIZE / 2f, IMAGE_SIZE / 2f, IMAGE_SIZE / 2f, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(image, 0, 0, paint);
                new Canvas(marker_bmp).drawBitmap(circle_bmp, 2 * BORDER, 2 * BORDER, null);
                MarkerOptions result = options.icon(BitmapDescriptorFactory.fromBitmap(marker_bmp));
                Marker marker = mMap.addMarker(result);
                if (marker != null) {
                    marker.setTag(productID);
                }
            }
            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {}
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
                    .addOnSuccessListener(loc -> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 12)));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CHECK_SETTINGS == requestCode) {
            if (resultCode == -1) {
                checkLocationPermissions();
            }
            else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.7887217, 2.3611764), 12));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activateLocation();
            }
            else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.7887217, 2.3611764), 12));
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