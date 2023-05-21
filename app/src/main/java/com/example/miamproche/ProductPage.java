package com.example.miamproche;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductPage extends AppCompatActivity {
    String id_product;
    private final String bucket = FirebaseStorage.getInstance().getReference().getBucket();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id_product = getIntent().getStringExtra("productID");
        if(id_product == null)
            id_product = "1";
        fetch_data();

        setContentView(R.layout.activity_product_page);
    }

    void fetch_data(){
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        Query produitsQuery = databaseRef.child("Produit").orderByChild("id_produit").equalTo(Integer.parseInt(id_product));

        ((Query)produitsQuery).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //DataSnapshot premierProduit = dataSnapshot.getChildren().iterator().next();
                    //String nomProduit = premierProduit.child("nom_produit").getValue(String.class);
                    String nomProduit = dataSnapshot.getChildren().iterator().next().child("nom_produit").getValue(String.class);
                    String description = dataSnapshot.getChildren().iterator().next().child("description").getValue(String.class);
                    String price = dataSnapshot.getChildren().iterator().next().child("prix").getValue(String.class);
                    Integer id_prod = dataSnapshot.getChildren().iterator().next().child("id_producteur").getValue(Integer.class);

                    Glide.with(ProductPage.this)
                            .load("https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/Produits%2F" + id_product + "?alt=media")
                            .into((ImageView) findViewById(R.id.product_img));

                    ((TextView) findViewById(R.id.product_name)).setText(nomProduit);
                    ((TextView) findViewById(R.id.textView_description)).setText(description);
                    ((TextView) findViewById(R.id.textView_price)).setText(price);

                    Query producteurQuery = databaseRef.child("Producteur").orderByChild("id_producteur").equalTo(id_prod);
                    ((Query)producteurQuery).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {

                                String prod_descr = snapshot.getChildren().iterator().next().child("description").getValue(String.class);
                                Long id_utilisateur = snapshot.getChildren().iterator().next().child("id_utilisateur").getValue(Long.class);
                                ((TextView) findViewById(R.id.textView_producer_description)).setText(prod_descr);

                                Glide.with(ProductPage.this)
                                        .load("https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/Utilisateurs%2F" + id_utilisateur + "?alt=media")
                                        .into((ImageView) findViewById(R.id.imageView_pp));

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Erreur producteur : " + error.getMessage());
                        }
                    });

                    Query getOtherProduct = databaseRef.child("Produit").orderByChild("id_producteur").equalTo(id_prod);
                    ((Query)getOtherProduct).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                if(snapshot.getChildrenCount() == 1){
                                    ((RelativeLayout) findViewById(R.id.view_suggestion1)).setVisibility(View.GONE);
                                    ((RelativeLayout) findViewById(R.id.view_suggestion2)).setVisibility(View.GONE);
                                    ((RelativeLayout) findViewById(R.id.view_suggestion3)).setVisibility(View.GONE);
                                }
                                if(snapshot.getChildrenCount() == 2){

                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        if(ds.child("id_produit").getValue(Long.class) != Long.valueOf(id_product)){
                                            final Long id1 = ds.child("id_produit").getValue(Long.class);
                                            ((TextView) findViewById(R.id.view_suggestion1_TV)).setText(ds.child("nom_produit").getValue(String.class));
                                            Glide.with(ProductPage.this)
                                                    .load("https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/Produits%2F" + id1 + "?alt=media")
                                                    .into((ImageView) findViewById(R.id.view_suggestion1_IV));

                                            findViewById(R.id.view_suggestion1_B).setOnClickListener(v -> {
                                                Intent intent = new Intent(ProductPage.this, ProductPage.class);
                                                intent.putExtra("productID", String.valueOf(id1));
                                                startActivity(intent);
                                            });

                                            break;
                                        }
                                    }

                                    ((RelativeLayout) findViewById(R.id.view_suggestion2)).setVisibility(View.GONE);
                                    ((RelativeLayout) findViewById(R.id.view_suggestion3)).setVisibility(View.GONE);
                                }
                                if(snapshot.getChildrenCount() == 3){
                                    Long id1 = null;
                                    Long id2 = null;
                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        if(ds.child("id_produit").getValue(Long.class) != Long.valueOf(id_product)){
                                            if(id1 == null){
                                                id1 = ds.child("id_produit").getValue(Long.class);
                                                ((TextView) findViewById(R.id.view_suggestion1_TV)).setText(ds.child("nom_produit").getValue(String.class));
                                                Glide.with(ProductPage.this)
                                                        .load("https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/Produits%2F" + id1 + "?alt=media")
                                                        .into((ImageView) findViewById(R.id.view_suggestion1_IV));
                                                Long finalId1 = id1; //must be final or semi final for the lambda expression bellow
                                                findViewById(R.id.view_suggestion1_B).setOnClickListener(v -> {
                                                    Intent intent = new Intent(ProductPage.this, ProductPage.class);
                                                    intent.putExtra("productID", String.valueOf(finalId1));
                                                    startActivity(intent);
                                                });
                                            }
                                            else{
                                                id2 = ds.child("id_produit").getValue(Long.class);
                                                ((TextView) findViewById(R.id.view_suggestion2_TV)).setText(ds.child("nom_produit").getValue(String.class));
                                                Glide.with(ProductPage.this)
                                                        .load("https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/Produits%2F" + id2 + "?alt=media")
                                                        .into((ImageView) findViewById(R.id.view_suggestion2_IV));
                                                Long finalId2 = id2;
                                                findViewById(R.id.view_suggestion2_B).setOnClickListener(v -> {
                                                    Intent intent = new Intent(ProductPage.this, ProductPage.class);
                                                    intent.putExtra("productID", String.valueOf(finalId2));
                                                    startActivity(intent);
                                                });
                                                break;
                                            }
                                        }
                                    }
                                    ((RelativeLayout) findViewById(R.id.view_suggestion3)).setVisibility(View.GONE);
                                }
                                if(snapshot.getChildrenCount() > 3){
                                    Long id1 = null;
                                    Long id2 = null;
                                    Long id3 = null;
                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        if(ds.child("id_produit").getValue(Long.class) != Long.valueOf(id_product)){
                                            if(id1 == null){
                                                id1 = ds.child("id_produit").getValue(Long.class);
                                                ((TextView) findViewById(R.id.view_suggestion1_TV)).setText(ds.child("nom_produit").getValue(String.class));
                                                Glide.with(ProductPage.this)
                                                        .load("https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/Produits%2F" + id1 + "?alt=media")
                                                        .into((ImageView) findViewById(R.id.view_suggestion1_IV));
                                                Long finalId1 = id1;
                                                findViewById(R.id.view_suggestion1_B).setOnClickListener(v -> {
                                                    Intent intent = new Intent(ProductPage.this, ProductPage.class);
                                                    intent.putExtra("productID", String.valueOf(finalId1));
                                                    startActivity(intent);
                                                });

                                            }
                                            else if(id2 == null){
                                                id2 = ds.child("id_produit").getValue(Long.class);
                                                ((TextView) findViewById(R.id.view_suggestion2_TV)).setText(ds.child("nom_produit").getValue(String.class));
                                                Glide.with(ProductPage.this)
                                                        .load("https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/Produits%2F" + id2 + "?alt=media")
                                                        .into((ImageView) findViewById(R.id.view_suggestion2_IV));
                                                Long finalId2 = id2;
                                                findViewById(R.id.view_suggestion2_B).setOnClickListener(v -> {
                                                    Intent intent = new Intent(ProductPage.this, ProductPage.class);
                                                    intent.putExtra("productID", String.valueOf(finalId2));
                                                    startActivity(intent);
                                                });
                                            }
                                            else{
                                                id3 = ds.child("id_produit").getValue(Long.class);
                                                ((TextView) findViewById(R.id.view_suggestion3_TV)).setText(ds.child("nom_produit").getValue(String.class));
                                                Glide.with(ProductPage.this)
                                                        .load("https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/Produits%2F" + id3 + "?alt=media")
                                                        .into((ImageView) findViewById(R.id.view_suggestion3_IV));
                                                Long finalId3 = id3;
                                                findViewById(R.id.view_suggestion3_B).setOnClickListener(v -> {
                                                    Intent intent = new Intent(ProductPage.this, ProductPage.class);
                                                    intent.putExtra("productID", String.valueOf(finalId3));
                                                    startActivity(intent);
                                                });
                                                break;
                                            }
                                        }
                                    }
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Erreur load suggestion : " + error.getMessage());
                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Erreur Produit: " + databaseError.getMessage());
            }
        });


    }

    void beReponsive(){
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();

        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        ImageView product_image = findViewById(R.id.product_img);
        product_image.requestLayout();
        product_image.getLayoutParams().height = (int) (0.6*dpHeight);
        product_image.getLayoutParams().width = (int) (0.5*dpWidth);

    }

}