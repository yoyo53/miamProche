package com.example.miamproche;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;


public class SearchableActivity extends Activity {
    ListAdapter adapter;
    FusedLocationProviderClient mLocationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        mLocationProvider = LocationServices.getFusedLocationProviderClient(this);

        SearchView search = findViewById(R.id.searchView);
        search.setQueryHint("Chercher un produit...");
        search.setIconifiedByDefault(false);
        FirebaseDatabase.getInstance().getReference().get().addOnSuccessListener(result -> {
            adapter = new ListAdapter(result, this);
            ((ListView) findViewById(R.id.list_view)).setAdapter(adapter);
            search.setIconified(false);
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (ActivityCompat.checkSelfPermission(SearchableActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(SearchableActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && MapActivity.isLocationEnabled(SearchableActivity.this)) {
                        mLocationProvider.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        adapter.mLocation = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                    }
                                    adapter.filter(newText);
                                });
                    }
                    else {
                        adapter.filter(newText);
                    }
                    return false;
                }
            });
        });
    }
}
