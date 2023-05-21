package com.example.miamproche;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class RegisterPage extends AppCompatActivity {
    private DatabaseReference myRef;
    private int currentid = 0; // Variable to store the number of people in the database
    private int currentid_producteur = 0; // Variable to store the number of people in the database

    private Uri image;


    private static final int SELECT_PICTURE = 1;

    private String selectedImagePath;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            image = selectedImageUri;
            //uploadImageToFirebaseStorage(selectedImageUri);
        }
    }


    private void uploadImageToFirebaseStorage(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Utilisateurs");
        String fileName =  currentid+"";
        StorageReference imageRef = storageRef.child(fileName);
        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }



    private void requestStoragePermission() {
        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

    }

    private boolean checkStoragePermission() {
        boolean res2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
        return res2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        myRef = FirebaseDatabase.getInstance("https://miam-proche-9fb82-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference();



        findViewById(R.id.btn).setOnClickListener(arg0 -> {
            if (!checkStoragePermission()) {
                requestStoragePermission();
            } else {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Choisir une image"), SELECT_PICTURE);
            }
        });

        myRef.child("Utilisateur").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentid = (int) snapshot.getChildrenCount();
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle error
            }
        });

        myRef.child("Producteur").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentid_producteur = (int) snapshot.getChildrenCount();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle error
            }
        });




        TextView fname = findViewById(R.id.fname);
        TextView lname = findViewById(R.id.lname);
        TextView email = findViewById(R.id.email);
        TextView password = findViewById(R.id.password);


        MaterialButton registerBtn = findViewById(R.id.register_btn);


        //@+id/checkbox
        CheckBox checkBox = findViewById(R.id.checkbox);
        final EditText editText_description = findViewById(R.id.description);
        final EditText editText_ville = findViewById(R.id.ville);
        final EditText editText_pays = findViewById(R.id.pays);
        final EditText editText_telephone = findViewById(R.id.telephone);
        final EditText editText_adresse = findViewById(R.id.adresse);
        final EditText editText_code_postal = findViewById(R.id.code_postal);



        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editText_description.setVisibility(View.VISIBLE);
                editText_adresse.setVisibility(View.VISIBLE);
                editText_ville.setVisibility(View.VISIBLE);
                editText_pays.setVisibility(View.VISIBLE);
                editText_telephone.setVisibility(View.VISIBLE);
                editText_code_postal.setVisibility(View.VISIBLE);
            } else {
                editText_description.setVisibility(View.GONE);
                editText_adresse.setVisibility(View.GONE);
                editText_ville.setVisibility(View.GONE);
                editText_pays.setVisibility(View.GONE);
                editText_telephone.setVisibility(View.GONE);
                editText_code_postal.setVisibility(View.GONE);
            }
        });





        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userFname = fname.getText().toString();
                String userLname = lname.getText().toString();
                String userEmail = email.getText().toString();
                String userPassword = password.getText().toString();



                myRef.child("Utilisateur").orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(RegisterPage.this, "Email déjà existant", Toast.LENGTH_SHORT).show();
                        }
                        else{
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

                                myRef.child("Utilisateur").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        currentid = (int) snapshot.getChildrenCount();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                // Store the user details in the database
                                DatabaseReference userRef = myRef.child("Utilisateur").child(String.valueOf(currentid));
                                userRef.child("id_utilisateur").setValue(currentid);
                                userRef.child("prenom").setValue(userFname);
                                userRef.child("nom").setValue(userLname);
                                userRef.child("email").setValue(userEmail);
                                userRef.child("mdp").setValue(hexString.toString());

                                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Utilisateurs");
                                String fileName =  currentid+"";
                                StorageReference imageRef = storageRef.child(fileName);
                                imageRef.putFile(image)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                            }
                                        });

                                if (checkBox.isChecked()) {

                                    myRef.child("Producteur").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            currentid_producteur = (int) snapshot.getChildrenCount();
                                        }


                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // handle error
                                        }
                                    });

                                    DatabaseReference producteurRef = myRef.child("Producteur").child(String.valueOf(currentid_producteur));

                                    String description = editText_description.getText().toString();
                                    String telephone = editText_telephone.getText().toString();
                                    String adresse = editText_adresse.getText().toString();
                                    String ville = editText_ville.getText().toString();
                                    String pays = editText_pays.getText().toString();
                                    String code_postal = editText_code_postal.getText().toString();

                                    System.out.println("yooooooooooooo");
                                    Geocoder geocoder = new Geocoder(RegisterPage.this);
                                    String address = adresse+" "+code_postal+" "+ ville + " "+ pays;
                                    List<Address> addressList;

                                    try {
                                        addressList =geocoder.getFromLocationName(address, 1);
                                        System.out.println(addressList);

                                        if (addressList != null && addressList.size() > 0) {
                                            double latitude = addressList.get(0).getLatitude();
                                            double longitude = addressList.get(0).getLongitude();
                                            System.out.println("test "+latitude);
                                            System.out.println(longitude);

                                            producteurRef.child("description").setValue(description);
                                            producteurRef.child("telephone").setValue(telephone);
                                            producteurRef.child("longitude").setValue(longitude);
                                            producteurRef.child("latitude").setValue(latitude);
                                            producteurRef.child("ville").setValue(ville);
                                            producteurRef.child("pays").setValue(pays);
                                            producteurRef.child("id_producteur").setValue(currentid_producteur);
                                            producteurRef.child("id_utilisateur").setValue(currentid);
                                        }

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }

                                Toast.makeText(RegisterPage.this, "Inscription Réussie", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterPage.this, LoginPage.class));
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException("MD5 n'est pas disponible", e);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // handle error
                    }
                });
            }
        });
    }
}

