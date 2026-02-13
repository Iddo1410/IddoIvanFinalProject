package com.example.iddoivanfinalproject;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.services.DataBaseService;

public class Itemdetails extends AppCompatActivity {

    private TextView tvName, tvDescription, tvPrice;
    private DataBaseService.DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemdetails);

        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrice = findViewById(R.id.tvPrice);

        databaseService = DataBaseService.DatabaseService.getInstance();

        String itemId = getIntent().getStringExtra("ITEM_ID");
        if (itemId != null) {
            databaseService.getItemById(itemId, new DataBaseService.DatabaseCallback<Item>() {
                @Override
                public void onCompleted(Item item) {
                    tvName.setText(item.getName());
                    tvDescription.setText(item.getDetails());
                    tvPrice.setText(String.valueOf(item.getPrice()));
                }

                @Override
                public void onFailed(Exception e) {
                    tvName.setText("Error loading item");
                }
            });

        }
    }
}
