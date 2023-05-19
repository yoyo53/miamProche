package com.example.miamproche;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ObjectInputStream;


public class ProducteurActivity extends AppCompatActivity {

    private ImageView userphoto;
    private TextView mGreetingTextView;

    private TextView item;
    private EditText mNameEditText;
    private Button mPlayButton;

    private FirebaseDatabase database;
    private DatabaseReference myRef;




    private void getDescriptionById(String idProducteur) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        Query producteurQuery = databaseRef.child("Producteur").orderByChild("id_producteur").equalTo(Integer.parseInt(idProducteur));


        ((Query)producteurQuery).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String description = dataSnapshot.child(idProducteur).child("description").getValue(String.class);
                    String image = dataSnapshot.child(idProducteur).child("image").getValue(String.class);
                    if (description != null) {
                        mGreetingTextView.setText(description);

                    } else {
                        mGreetingTextView.setText("Aucune description disponible.");
                    }
                } else {
                    mGreetingTextView.setText("Producteur introuvable.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Erreur lors de la récupération de la description du producteur : " + databaseError.getMessage());
            }
        });
    }



    private void getProductNameByProductId(String idProducteur) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        Query produitsQuery = databaseRef.child("Produits").orderByChild("id_producteur").equalTo(Integer.parseInt(idProducteur));

        ((Query)produitsQuery).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //DataSnapshot premierProduit = dataSnapshot.getChildren().iterator().next();
                    //String nomProduit = premierProduit.child("nom_produit").getValue(String.class);
                    String nomProduit = dataSnapshot.child(idProducteur).child("nom_produit").getValue(String.class);
                    if (nomProduit != null) {
                        item.setText(nomProduit);
                    } else {
                        item.setText("Nom de produit non trouvé.");
                    }
                } else {
                    item.setText("Aucun produit trouvé pour ce producteur.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Erreur lors de la récupération du nom de produit : " + databaseError.getMessage());
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producteur);


        item = findViewById(R.id.item_des);
        userphoto = findViewById(R.id.userImage);
        mGreetingTextView = findViewById(R.id.main_textview_greeting);
        myRef = FirebaseDatabase.getInstance(" https://miam-proche-9fb82-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String idProducteur = prefs.getString("id", "1");
        getDescriptionById(idProducteur);
        getProductNameByProductId(idProducteur);


/*


        // Récupérer une référence vers un nœud dans la base de données
        //myRef = database.getReference("producteurs");

        // Écrire des données dans la base de données
        // myRef.setValue("c'est un test");

       // mGreetingTextView = findViewById(R.id.main_textview_greeting);
        mNameEditText = findViewById(R.id.main_edittext_name);
        mPlayButton = findViewById(R.id.main_button);

        mPlayButton.setEnabled(false);
*/

       /* ValueEventListener greetListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                description description = dataSnapshot.getValue(description.class);
                // ..
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mPostReference.addValueEventListener(postListener);
    }
*/
        /*
        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // This is where we'll check the user input
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // The user just clicked
            }
        });

*/

    }

}