package com.example.miamproche;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ListAdapter extends BaseAdapter {
    DataSnapshot mData;
    List<DataSnapshot> mFilteredData;
    Activity mActivity;
    LatLng mLocation = new LatLng(48.7887217, 2.361176);
    private LayoutInflater inflater;
    private final String bucket = FirebaseStorage.getInstance().getReference().getBucket();
    private final HashMap<Long, DataSnapshot> producteurs = new HashMap<>();

    public ListAdapter(DataSnapshot data, Activity activity) {
        mData = data;
        mFilteredData = new ArrayList<>();
        mActivity = activity;
        for (DataSnapshot producteur : mData.child("Producteur").getChildren()) {
            producteurs.put(producteur.child("id_producteur").getValue(Long.class), producteur);
        }
    }

    @Override
    public int getCount() {
        return mFilteredData.size();
    }

    @Override
    public DataSnapshot getItem(int position) {
        return mFilteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public double getDistance(DataSnapshot product) {
        DataSnapshot producteur = producteurs.get(product.child("id_producteur").getValue(Long.class));
        if (producteur != null) {
            Double latitude = producteur.child("latitude").getValue(Double.class), longitude = producteur.child("longitude").getValue(Double.class);
            if (latitude != null && longitude != null) {
                double a = (1 - Math.cos(Math.toRadians(latitude - mLocation.latitude))
                        + Math.cos(Math.toRadians(mLocation.latitude)) * Math.cos(Math.toRadians(latitude))
                        * (1 - Math.cos(Math.toRadians(longitude - mLocation.longitude)))) / 2;
                return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            }
        }
        return 9999;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_item, parent, false);
        }

        DataSnapshot product = mFilteredData.get(position);
        ((TextView) convertView.findViewById(R.id.product_name)).setText(String.format(Locale.getDefault(), "%s\n%.1f km", product.child("nom_produit").getValue(String.class), getDistance(product)));
        Glide.with(convertView)
                .load("https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/Produits%2F" + product.child("id_produit").getValue(Long.class) + "?alt=media")
                .into((ImageView) convertView.findViewById(R.id.product_image));
        convertView.findViewById(R.id.product_button).setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, ProductPage.class);
            intent.putExtra("productID", String.valueOf(product.child("id_produit").getValue(Long.class)));
            mActivity.startActivity(intent);
        });
        return convertView;
    }

    public void filter(String constraint) {
        mFilteredData = new ArrayList<>();
        if (constraint != null && constraint.length() > 0) {
            for (DataSnapshot product : mData.child("Produit").getChildren()) {
                String nom = product.child("nom_produit").getValue(String.class);
                if (nom != null && nom.toUpperCase().contains(constraint.toUpperCase())) {
                    mFilteredData.add(product);
                }
            }
        }
        mFilteredData.sort((product1, product2) -> Double.compare(getDistance(product1), getDistance(product2)));
        notifyDataSetChanged();
    }
}
