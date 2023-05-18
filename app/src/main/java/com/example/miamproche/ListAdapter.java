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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends BaseAdapter {
    DataSnapshot mData;
    List<DataSnapshot> mFilteredData;
    Activity mActivity;
    private LayoutInflater inflater;
    private final String bucket = FirebaseStorage.getInstance().getReference().getBucket();

    public ListAdapter(DataSnapshot data, Activity activity) {
        mData = data;
        mFilteredData = new ArrayList<>();
        mActivity = activity;
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

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_item, parent, false);
        }

        DataSnapshot product = mFilteredData.get(position);
        ((TextView) convertView.findViewById(R.id.product_name)).setText(product.child("nom_produit").getValue(String.class));
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
            for (DataSnapshot product : mData.getChildren()) {
                String nom = product.child("nom_produit").getValue(String.class);
                if (nom != null && nom.toUpperCase().contains(constraint.toUpperCase())) {
                    mFilteredData.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }
}
