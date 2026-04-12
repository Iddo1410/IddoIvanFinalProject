package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iddoivanfinalproject.adapter.ItemAdapter;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.services.DataBaseService;

import java.util.ArrayList;
import java.util.List;

public class Items extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private DataBaseService.DatabaseService databaseService;
    private Button btnGoToCart;
    private Spinner spTypeFilter;

    private ArrayList<Item> allItemsList = new ArrayList<>();
    private ArrayList<Item> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        // אתחול רכיבים
        recyclerView = findViewById(R.id.rvItems);
        btnGoToCart = findViewById(R.id.btnGoToCart);
        spTypeFilter = findViewById(R.id.spTypeFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseService = DataBaseService.DatabaseService.getInstance();

        // הגדרת האדפטר
        adapter = new ItemAdapter(filteredList, new ItemAdapter.OnItemClickListener() {
            @Override
            public void onClick(Item item) {
                Intent go = new Intent(Items.this, Itemdetails.class);
                go.putExtra("ITEM_ID", item.getId());
                startActivity(go);
            }

            @Override
            public void onLongClick(Item item) { }
        });
        recyclerView.setAdapter(adapter);

        setupSpinner();

        // --- התיקון למעבר לעגלה ---
        if (btnGoToCart != null) {
            btnGoToCart.setOnClickListener(v -> {
                // מעבר למחלקה CartActivity (צריך ליצור אותה למטה)
                Intent intent = new Intent(Items.this, CartActivity.class);
                startActivity(intent);
            });
        }

        loadItemsFromDatabase();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.typeArr, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTypeFilter.setAdapter(spinnerAdapter);

        spTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                filterItems(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void loadItemsFromDatabase() {
        databaseService.getAllItems(new DataBaseService.DatabaseCallback<List<Item>>() {
            @Override
            public void onCompleted(List<Item> items) {
                if (items != null) {
                    allItemsList.clear();
                    allItemsList.addAll(items);
                    if (spTypeFilter.getSelectedItem() != null) {
                        filterItems(spTypeFilter.getSelectedItem().toString());
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("ItemsPage", "Failed to load items", e);
            }
        });
    }

    private void filterItems(String type) {
        filteredList.clear();
        for (Item item : allItemsList) {
            if (item.getType() != null && item.getType().equals(type)) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }
}