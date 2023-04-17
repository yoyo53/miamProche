package com.example.miamproche;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class SearchableActivity extends Activity {
    ListAdapter adapter;

    List<String> arrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        arrayList.add("January");
        arrayList.add("February");
        arrayList.add("March");
        arrayList.add("April");
        arrayList.add("May");
        arrayList.add("June");
        arrayList.add("July");
        arrayList.add("August");
        arrayList.add("September");
        arrayList.add("October");
        arrayList.add("November");
        arrayList.add("December");

        adapter = new ListAdapter(arrayList);
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
    }
}
