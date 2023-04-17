package com.example.miamproche;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProducteurActivity extends AppCompatActivity {

    private TextView mGreetingTextView;
    private EditText mNameEditText;
    private Button mPlayButton;

    private FirebaseDatabase database;
    private DatabaseReference myRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producteur);

        // écrire dans la base de donnnée
        // Récupérer l'instance de la base de données
        database = FirebaseDatabase.getInstance(" https://miamproche-default-rtdb.europe-west1.firebasedatabase.app");

        // Récupérer une référence vers un nœud dans la base de données
        myRef = database.getReference("utilisateurs/lalala/test2");

        // Écrire des données dans la base de données
        myRef.setValue("c'est un test");

        mGreetingTextView = findViewById(R.id.main_textview_greeting);
        mNameEditText = findViewById(R.id.main_edittext_name);
        mPlayButton = findViewById(R.id.main_button);

        mPlayButton.setEnabled(false);


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



    }

}