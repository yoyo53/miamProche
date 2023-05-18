package com.example.miamproche;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterPage extends AppCompatActivity {

    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        myRef = FirebaseDatabase.getInstance("https://miam-proche-9fb82-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference();

        TextView fname = findViewById(R.id.fname);
        TextView lname = findViewById(R.id.lname);
        TextView email = findViewById(R.id.email);
        TextView password = findViewById(R.id.password);

        MaterialButton registerBtn = findViewById(R.id.register_btn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userFname = fname.getText().toString();
                String userLname = lname.getText().toString();
                String userEmail = email.getText().toString();
                String userPassword = password.getText().toString();

                try {
                    // Calculate the MD5 hash of the password
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] hash = md.digest(userPassword.getBytes(StandardCharsets.UTF_8));

                    // Convert the hash to a hexadecimal string
                    StringBuilder hexString = new StringBuilder();
                    for (byte b : hash) {
                        String hex = Integer.toHexString(0xff & b);
                        if (hex.length() == 1) hexString.append('0');
                        hexString.append(hex);
                    }

                    // Store the user details in the database
                    DatabaseReference userRef = myRef.child("Utilisateur").push(); // Generate a unique key
                    userRef.child("prenom").setValue(userFname);
                    userRef.child("nom").setValue(userLname);
                    userRef.child("email").setValue(userEmail);
                    userRef.child("mdp").setValue(hexString.toString());

                    Toast.makeText(RegisterPage.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterPage.this, LoginPage.class));
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("MD5 is not available", e);
                }
            }
        });
    }
}

