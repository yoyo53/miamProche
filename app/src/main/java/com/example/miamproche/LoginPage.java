package com.example.miamproche;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class LoginPage extends AppCompatActivity {

    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        myRef = FirebaseDatabase.getInstance(" https://miam-proche-9fb82-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        TextView email = (TextView) findViewById(R.id.email);
        TextView password = (TextView) findViewById(R.id.password);
        TextView register = (TextView) findViewById(R.id.register2);

        MaterialButton loginbtn = (MaterialButton) findViewById(R.id.loginbtn);

        register.setOnClickListener(view -> startActivity(new Intent(LoginPage.this, RegisterPage.class)));

        loginbtn.setOnClickListener(view -> myRef.child("Utilisateur").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for(DataSnapshot child: task.getResult().getChildren()){
                    String semail = child.child("email").getValue(String.class);
                    if(email.getText().toString().equals(semail)){
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
                            if (mdp.equals(hexString.toString())){
                                Toast.makeText(LoginPage.this, "LOGIN SUCESSFULL", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginPage.this, MapActivity.class));
                            }
                            else{
                                Toast.makeText(LoginPage.this, "LOGIN FAILED", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NoSuchAlgorithmException e) {
                            // Cette exception ne devrait jamais être lancée car MD5 est toujours disponible
                            throw new RuntimeException("MD5 n'est pas disponible", e);
                        }
                    }
                }
            }
        }));
    }
}