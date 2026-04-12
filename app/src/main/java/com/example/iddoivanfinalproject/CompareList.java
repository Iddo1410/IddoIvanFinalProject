package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

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
import java.util.List;

public class CompareList extends AppCompatActivity {


    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private DataBaseService.DatabaseService databaseService;
    private Button btnGoToCart; // משתנה חדש לכפתור העגלה
    ArrayList<Item> itemArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compare_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // אתחול רכיבי ה-UI
        recyclerView = findViewById(R.id.rvCompare);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseService = DataBaseService.DatabaseService.getInstance();

        // הגדרת לחיצה על כפתור העגלה



        adapter = new ItemAdapter(itemArrayList, new ItemAdapter.OnItemClickListener() {
            @Override
            public void onClick(Item item) {
                Intent go = new Intent(CompareList.this, Itemdetails.class);
                go.putExtra("ITEM_ID", item.getId());
                startActivity(go);
            }

            @Override
            public void onLongClick(Item item) {
                // טיפול בלחיצה ארוכה אם צריך
            }
        });

        recyclerView.setAdapter(adapter);

        // טעינת המוצרים מהמסד
        databaseService.getCompareByType("טלפון", new DataBaseService.DatabaseCallback<Compareitem>() {
            @Override
            public void onCompleted(Compareitem compareitem) {
                if (compareitem.getItemArrayList() != null) {

                    itemArrayList.addAll(compareitem.getItemArrayList());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailed(Exception e) {
                //Log.e("ItemsPage", "Failed to load items", e);
            }
        });



    }
}