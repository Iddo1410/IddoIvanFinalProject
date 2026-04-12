package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iddoivanfinalproject.adapter.ItemAdapter;
import com.example.iddoivanfinalproject.model.Compareitem;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.services.DataBaseService;

import java.util.ArrayList;

public class CompareList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private DataBaseService.DatabaseService databaseService;
    private Spinner spinnerCategory;

    ArrayList<Item> itemArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compare_list);

        try {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } catch (Exception e) {}

        // אתחול רכיבים
        recyclerView = findViewById(R.id.rvCompare);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        databaseService = DataBaseService.DatabaseService.getInstance();

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ItemAdapter(itemArrayList, new ItemAdapter.OnItemClickListener() {
                @Override
                public void onClick(Item item) {
                    Intent go = new Intent(CompareList.this, Itemdetails.class);
                    go.putExtra("ITEM_ID", item.getId());
                    startActivity(go);
                }

                @Override
                public void onLongClick(Item item) { }
            });
            recyclerView.setAdapter(adapter);
        }

        // --- הגדרת התפריט הנפתח (Spinner) כמו ב-Items ---
        setupSpinner();
    }

    private void setupSpinner() {
        // שימוש במערך typeArr הקיים ב-res/values/arrs.xml
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.typeArr, android.R.layout.simple_spinner_item);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        // מאזין לשינוי בחירה
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // המרת הבחירה למחרוזת וטעינת הנתונים
                String selectedType = parent.getItemAtPosition(position).toString();
                loadCompareDataByType(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void loadCompareDataByType(String type) {
        databaseService.getCompareByType(type, new DataBaseService.DatabaseCallback<Compareitem>() {
            @Override
            public void onCompleted(Compareitem compareitem) {
                itemArrayList.clear();

                if (compareitem != null && compareitem.getItemArrayList() != null && !compareitem.getItemArrayList().isEmpty()) {
                    itemArrayList.addAll(compareitem.getItemArrayList());
                } else {
                    Toast.makeText(CompareList.this, "אין מוצרים להשוואה בקטגוריית " + type, Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("CompareList", "Failed to load items", e);
                itemArrayList.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }
}