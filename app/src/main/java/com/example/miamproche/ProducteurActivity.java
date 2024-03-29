package com.example.miamproche;

import static android.content.ContentValues.TAG;


import androidx.activity.result.ActivityResultLauncher;
import android.content.SharedPreferences;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.UploadTask;

import android.widget.Toast;

public class ProducteurActivity extends AppCompatActivity {
    private TextView mGreetingTextView;
    private TextView item;
    private TextView itemdes;
    private EditText mNomEditText;
    private EditText mQuantiteEditText;
    private EditText mPrixEditText;
    private EditText mDescriptionEditText;
    private DatabaseReference myRef;
    private Integer cpt;
    private Uri image;
    private Uri imageUri;
    private ImageView userphoto;
    private ImageView addimage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producteur);

        mNomEditText = findViewById(R.id.editText_nom_produit);
        mQuantiteEditText = findViewById(R.id.editText_quantite);
        mPrixEditText = findViewById(R.id.editText_prix);
        mDescriptionEditText = findViewById(R.id.editText_description);
        userphoto = findViewById(R.id.userImage);
        mGreetingTextView = findViewById(R.id.main_textview_greeting);
        myRef = FirebaseDatabase.getInstance(" https://miam-proche-9fb82-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        findViewById(R.id.map_button).setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
        findViewById(R.id.search_button).setOnClickListener(v -> startActivity(new Intent(this, SearchableActivity.class)));
        findViewById(R.id.logout_button).setOnClickListener(v -> startActivity(new Intent(this, LoginPage.class)));

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String idProducteur = prefs.getString("id_producteur", "1");
        String idUtilisateur = prefs.getString("id_utilisateur", "0");
        //String idProducteur = "112";
        getDescriptionById(idProducteur);
        getProductNameByProductId(idProducteur);
        getPhotoForUser(idUtilisateur);
        getProductNameByProductId(idProducteur);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                if (result.getData().getData() != null) {
                    // Utiliser l'URI pour la galerie
                    Uri uri = result.getData().getData();
                    ImageView imageView = findViewById(R.id.newimage);
                    imageView.setImageURI(uri);
                    imageUri = uri;
                    Log.d("Image URI", uri.toString());
                } else {
                    // Utiliser le bitmap pour l'image de la caméra
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");

                    // Enregistrer le bitmap dans une URI
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    OutputStream outputStream;
                    try {
                        outputStream = getContentResolver().openOutputStream(imageUri);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Afficher l'image à partir de l'URI
                    ImageView imageView = findViewById(R.id.newimage);
                    imageView.setImageBitmap(bitmap);
                    Log.d("Image URI", imageUri.toString());
                }
            }
        });


        addimage = findViewById(R.id.newimage);

        addimage.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProducteurActivity.this);
            builder.setTitle("Options :");
            String[] options = {"Prendre une Photo", "Choisir depuis la Galerie"};
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        imagePickerLauncher.launch(intent);
                    }
                } else if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        imagePickerLauncher.launch(Intent.createChooser(intent, "Choisir l'image"));
                    }
                }
            });
            builder.show();
        });


        Button add = findViewById(R.id.addbutton);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nomProduit = mNomEditText.getText().toString().trim();
                String quantiteProduit = mQuantiteEditText.getText().toString().trim();
                String prixProduit = mPrixEditText.getText().toString().trim();
                String descriptionProduit = mDescriptionEditText.getText().toString().trim();

                if (TextUtils.isEmpty(nomProduit) || TextUtils.isEmpty(quantiteProduit) || TextUtils.isEmpty(prixProduit) || TextUtils.isEmpty(descriptionProduit)) {
                    Toast.makeText(getApplicationContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (imageUri == null) {
                    Toast.makeText(getApplicationContext(), "Veuillez sélectionner une photo", Toast.LENGTH_SHORT).show();
                    return;
                }

                addProduct(descriptionProduit, idProducteur, nomProduit, quantiteProduit, prixProduit, imageUri);
            }
        });
    }


    private void getDescriptionById(String idProducteur) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        Query producteurQuery = databaseRef.child("Producteur").orderByChild("id_producteur").equalTo(Integer.parseInt(idProducteur));
        ((Query) producteurQuery).addListenerForSingleValueEvent(new ValueEventListener() {
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


    private void getPhotoForUser(String userId) {
        int userIdInt = Integer.parseInt(userId);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://miam-proche-9fb82.appspot.com");
        StorageReference photoRef = storageRef.child("Utilisateurs/" + userIdInt);
        ImageView imageView = findViewById(R.id.userImage);
        photoRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Une erreur s'est produite lors du chargement de l'image
                Log.e(TAG, "Erreur lors du chargement de l'image : " + e.getMessage());
            }
        });
    }

    private void getPhotoForproduct(int productId, int nb) {
        int productIdInt = productId;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://miam-proche-9fb82.appspot.com");
        StorageReference photoRef = storageRef.child("Produits/" + productIdInt);
        System.out.println("nb = " + (nb - 1));
        int imageViewId = getResources().getIdentifier("item" + nb, "id", getPackageName());
        ImageView imageViewproduct = findViewById(imageViewId);
        photoRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageViewproduct.setImageBitmap(bitmap);

                imageViewproduct.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ProducteurActivity.this, ProductPage.class);
                        intent.putExtra("productID", String.valueOf(productIdInt)); // Utilisez la variable productIdInt ici
                        startActivity(intent);
                    }
                });
            }
        });
    }



    private void getProductNameByProductId(String idProducteur) {
        myRef.child("Produit").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                cpt = 0;
                List<TextView> itemNames = new ArrayList<>();
                List<TextView> itemDescriptions = new ArrayList<>();
                for (DataSnapshot child : task.getResult().getChildren()) {
                    if (cpt < 14) {
                        int idprod = child.child("id_producteur").getValue(Integer.class);
                        if (idprod == Integer.parseInt(idProducteur)) {
                            int idproduit = child.child("id_produit").getValue(Integer.class);
                            System.out.println(cpt);
                            getPhotoForproduct(idproduit, cpt);
                            item = findViewById(getResources().getIdentifier("item_name" + cpt, "id", getPackageName()));
                            itemdes = findViewById(getResources().getIdentifier("item_des" + cpt, "id", getPackageName()));
                            item.setText(child.child("nom_produit").getValue(String.class));
                            itemdes.setText("Prix : " + child.child("prix").getValue(String.class) + "\n" + "Quantité disponible : " + child.child("quantite").getValue(String.class));
                            System.out.println(child.child("nom_produit").getValue(String.class));
                            itemNames.add(item);
                            itemDescriptions.add(itemdes);
                            cpt += 1;
                        }
                    }
                }
                for (int i = cpt; i < 14; i++) {
                    item = findViewById(getResources().getIdentifier("item_des" + i, "id", getPackageName()));
                    item.setText("Aucun produit");
                    itemDescriptions.add(item);
                }
            }
        });
    }


    private void uploadImageToFirebaseStorage(int productIdInt, Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://miam-proche-9fb82.appspot.com");
        StorageReference imageRef = storageRef.child("Produits/" + productIdInt);
        UploadTask uploadTask = imageRef.putFile(imageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
        }).addOnFailureListener(e -> {
            Toast.makeText(ProducteurActivity.this, "Erreur lors du téléchargement de l'image", Toast.LENGTH_SHORT).show();
        });
    }



    public void addProduct(String description, String id_producteur, String nom_produit, String quantite, String prix, Uri imageUri) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("Produit");
        databaseRef.orderByChild("id_produit").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int lastProductId;
                int lastProductRef;
                if (dataSnapshot.exists()) {
                    DataSnapshot lastProductSnapshot = dataSnapshot.getChildren().iterator().next();
                    lastProductRef = Integer.parseInt(lastProductSnapshot.getKey());
                    lastProductId = lastProductSnapshot.child("id_produit").getValue(Integer.class);
                } else {
                    lastProductId = 0;
                    lastProductRef = 0;
                }

                int newProductRef = lastProductRef + 1;
                int newProductId = lastProductId + 1;
                int id_Producteur = Integer.parseInt(id_producteur);


                uploadImageToFirebaseStorage(newProductId, imageUri);
                Produit produit = new Produit(description, newProductRef, newProductId, id_Producteur, nom_produit, quantite, prix, image);
                // Ajouter le produit à la base de données
                databaseRef.child(String.valueOf(newProductRef)).setValue(produit)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Le produit a été ajouté avec succès
                                Toast.makeText(ProducteurActivity.this, "Produit ajouté avec succès", Toast.LENGTH_SHORT).show();
                                finish(); // Fermer l'activité d'ajout de produit
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProducteurActivity.this, "Erreur lors de l'ajout du produit", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }



    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private void requestStoragePermission() {
        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

    }

    private boolean checkStoragePermission() {
        boolean res2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
        return res2;
    }

    private boolean checkCameraPermission() {
        boolean res1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED;
        boolean res2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
        return res1 && res2;
    }


}
