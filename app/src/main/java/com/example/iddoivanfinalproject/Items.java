package com.example.iddoivanfinalproject;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iddoivanfinalproject.adapter.ItemAdapter;
import com.example.iddoivanfinalproject.model.Item;

import java.util.ArrayList;

public class Items extends AppCompatActivity {

    RecyclerView rvItems;
    ArrayList<Item> itemList;
    ItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items); // ה-XML של הטבלה

        rvItems = findViewById(R.id.rvItems);

        itemList = new ArrayList<>();
        adapter = new ItemAdapter(itemList);

        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }
}
