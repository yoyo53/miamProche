package com.example.miamproche;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.widget.SearchView;

import com.google.firebase.database.FirebaseDatabase;


public class SearchableActivity extends Activity {
    ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        FirebaseDatabase.getInstance().getReference().get().addOnSuccessListener(result -> {
            adapter = new ListAdapter(result.child("Produit"), this);
            ListView listView = findViewById(R.id.list_view);
            listView.setAdapter(adapter);

            SearchView search = findViewById(R.id.searchView);
            search.setQueryHint("Type your keyword here");
            search.setIconifiedByDefault(false);
            search.clearFocus();
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {

                    adapter.filter(newText);
                    System.out.println(newText);
                    return false;
                }
            });
        });
    }
}
