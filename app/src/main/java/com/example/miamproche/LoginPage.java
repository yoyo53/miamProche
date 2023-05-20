package com.example.miamproche;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.security.*;

public class LoginPage extends AppCompatActivity {

    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        myRef = FirebaseDatabase.getInstance(" https://miam-proche-9fb82-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        TextView email = findViewById(R.id.email);
        TextView password = findViewById(R.id.password);
        TextView register = findViewById(R.id.register2);

        MaterialButton loginbtn = findViewById(R.id.loginbtn);

        register.setOnClickListener(view -> startActivity(new Intent(LoginPage.this, RegisterPage.class)));

        loginbtn.setOnClickListener(view -> myRef.child("Utilisateur").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean isLoginSuccessful = false;
                for(DataSnapshot child: task.getResult().getChildren()){
                    String semail = child.child("email").getValue(String.class);
                    if(email.getText().toString().equals(semail)){
                        isLoginSuccessful = true;
                        String pwd = password.getText().toString();
                        String mdp = child.child("mdp").getValue(String.class);
                        try {
                            // Obtenir une instance de MessageDigest pour l'algorithme MD5
                            MessageDigest md = MessageDigest.getInstance("MD5");

                            // Convertir la chaîne en tableau de bytes et calculer le hachage
                            byte[] hash = md.digest(pwd.getBytes(StandardCharsets.UTF_8));

                            // Convertir le hachage en chaîne hexadécimale
                            StringBuilder hexString = new StringBuilder();
                            for (byte b : hash) {
                                String hex = Integer.toHexString(0xff & b);
                                if (hex.length() == 1) hexString.append('0');
                                hexString.append(hex);
                            }
                            if (hexString.toString().equals(mdp)){
                                Integer idUtilisateur = child.child("id_utilisateur").getValue(Integer.class);
                                if (idUtilisateur != null) {
                                    Toast.makeText(LoginPage.this, "LOGIN SUCESSFULL", Toast.LENGTH_SHORT).show();
                                    // Query the "Producteur" table based on the email
                                    DatabaseReference producteurRef = myRef.child("Producteur");
                                    Query producteurQuery = producteurRef.orderByChild("id_utilisateur").equalTo(idUtilisateur);

                                    producteurQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot producteurSnapshot : dataSnapshot.getChildren()) {
                                                    Integer idProducteur = producteurSnapshot.child("id_producteur").getValue(Integer.class);
                                                    SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                                                    editor.putString("id", String.valueOf(idProducteur));
                                                    editor.apply();
                                                    if (idProducteur != null) {
                                                        startActivity(new Intent(LoginPage.this, MapActivity.class));
                                                    } else {
                                                        System.out.println("ID Producteur not found");
                                                    }
                                                }
                                            } else {
                                                Integer idProducteur = -1;
                                                SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                                                editor.putString("id", String.valueOf(idProducteur));
                                                editor.apply();
                                                startActivity(new Intent(LoginPage.this, MapActivity.class));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Handle any potential errors
                                        }
                                    });
                                }
                                else {
                                    isLoginSuccessful = false;
                                }
                            }
                            else{
                                isLoginSuccessful = false;
                            }
                        } catch (NoSuchAlgorithmException e) {
                            // Cette exception ne devrait jamais être lancée car MD5 est toujours disponible
                            throw new RuntimeException("MD5 n'est pas disponible", e);
                        }
                    }
                }
                if (!isLoginSuccessful){
                    Toast.makeText(LoginPage.this, "LOGIN FAILED", Toast.LENGTH_SHORT).show();
                }
            }
        }));
    }
}