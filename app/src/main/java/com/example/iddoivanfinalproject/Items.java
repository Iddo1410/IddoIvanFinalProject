package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iddoivanfinalproject.R;


import com.example.iddoivanfinalproject.adapter.ItemAdapter;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.services.DataBaseService;

import java.util.ArrayList;
import java.util.List;


public class Items extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private DataBaseService.DatabaseService databaseService;
    ArrayList<Item> itemArrayList=new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        recyclerView = findViewById(R.id.rvItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseService = DataBaseService.DatabaseService.getInstance();
        adapter = new ItemAdapter(itemArrayList, new ItemAdapter.OnItemClickListener() {
            @Override
            public void onClick(Item item) {

                Intent go=new Intent(Items.this, Itemdetails.class);
                go.putExtra("ITEM_ID",item.getId());
                startActivity(go);




            }

            @Override
            public void onLongClick(Item item) {

            }
        });

        recyclerView.setAdapter(adapter);

        // קרא את כל הפריטים מהמסד
        databaseService.getAllItems(new DataBaseService.DatabaseCallback<List<Item>>() {
            @Override
            public void onCompleted(List<Item> items) {

                itemArrayList.addAll(items);

                adapter.notifyDataSetChanged();
            }


            @Override
            public void onFailed(Exception e) {
                Log.e("ItemsPage", "Failed to load items", e);
            }
        });

    }
}

