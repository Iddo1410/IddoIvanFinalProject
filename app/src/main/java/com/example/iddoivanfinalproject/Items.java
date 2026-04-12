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

    private ArrayList<Item> allItemsList = new ArrayList<>(); // כל המוצרים מהמסד
    private ArrayList<Item> filteredList = new ArrayList<>(); // המוצרים להצגה לאחר סינון

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        // אתחול רכיבי UI
        recyclerView = findViewById(R.id.rvItems);
        btnGoToCart = findViewById(R.id.btnGoToCart);
        spTypeFilter = findViewById(R.id.spTypeFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseService = DataBaseService.DatabaseService.getInstance();

        // הגדרת האדפטר עם הרשימה המסוננת
// הגדרת האדפטר עם הרשימה ועם המאזין ללחיצות (Listener)
        adapter = new ItemAdapter(filteredList, new ItemAdapter.OnItemClickListener() {
            @Override
            public void onClick(Item item) {
                // מה קורה כשלוחצים על מוצר (מעבר לדף פרטים)
                Intent go = new Intent(Items.this, Itemdetails.class);
                go.putExtra("ITEM_ID", item.getId());
                startActivity(go);
            }

            @Override
            public void onLongClick(Item item) {
                // מה קורה בלחיצה ארוכה (אפשר להשאיר ריק)
            }
        });        recyclerView.setAdapter(adapter);

        // הגדרת הספינר עם הנתונים מ-arrs.xml
        setupSpinner();

        // מעבר לעגלה
        if (btnGoToCart != null) {
            btnGoToCart.setOnClickListener(v -> {
                Intent intent = new Intent(Items.this, Userdetails.class);
                startActivity(intent);
            });
        }

        // טעינת המוצרים מהמסד
        loadItemsFromDatabase();
    }

    private void setupSpinner() {
        // שימוש במערך typeArr הקיים ב-res/values/arrs.xml
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.typeArr, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTypeFilter.setAdapter(spinnerAdapter);

        // האזנה לשינויים בספינר
        spTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                filterItems(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // ללא שינוי
            }
        });
    }

    private void loadItemsFromDatabase() {
        databaseService.getAllItems(new DataBaseService.DatabaseCallback<List<Item>>() {
            @Override
            public void onCompleted(List<Item> items) {
                if (items != null) {
                    allItemsList.clear();
                    allItemsList.addAll(items);
                    // הצגה ראשונית לפי הבחירה הנוכחית בספינר
                    filterItems(spTypeFilter.getSelectedItem().toString());
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
            // אם סוג המוצר מתאים לבחירה בספינר
            if (item.getType() != null && item.getType().equals(type)) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }
}