package com.example.miamproche;

import android.net.Uri;

public class Produit {
    private int id_produit;
    private int id_producteur;
    private String nom_produit;
    private String quantite;
    private String prix;
    private String description;
    private int ProductRef;

    private Uri image;

    private String imageUrl;



    public Produit() {
        // Constructeur vide requis pour Firebase
    }

    public Produit(String description, int newProductRef, int newProductId, int id_producteur, String nom_produit, String quantite, String prix, Uri image) {
        this.id_produit = newProductId;
        this.id_producteur = id_producteur;
        this.nom_produit = nom_produit;
        this.quantite = quantite;
        this.prix = prix;
        this.ProductRef = newProductRef;
        this.description = description;
        this.image = image;

    }


    public int getId_produit() {
        return id_produit;
    }

    public int getId_producteur() {
        return id_producteur;
    }

    public String getNom_produit() {
        return nom_produit;
    }

    public String getQuantite() {
        return quantite;
    }

    public String getPrix() {
        return prix;
    }

    public String getDescription() { return description; }

    public String getImageUrl() { return imageUrl; }



    @Override
    public String toString() {
        return "Produit{" +
                "description='" + description + '\'' +
                ", newProductRef=" + ProductRef +
                ", newProductId=" + id_produit +
                ", id_producteur=" + id_producteur +
                ", nom_produit='" + nom_produit + '\'' +
                ", quantite='" + quantite + '\'' +
                ", prix='" + prix + '\'' +
                '}';
    }


    public void setImageUri(Uri image) {
        this.image = image;
    }
}



