package com.example.iddoivanfinalproject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.iddoivanfinalproject.model.Cart;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.services.DataBaseService;
import com.example.iddoivanfinalproject.utils.ImageUtil;

public class Itemdetails extends AppCompatActivity {

    private TextView tvName, tvDescription, tvPrice, tvBrand, tvType, tvYear;
    private ImageView ivPic;
    private DataBaseService.DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemdetails);

        // חיבור רכיבי ה-UI
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrice = findViewById(R.id.tvPrice);
        tvBrand=findViewById(R.id.tvBrand);
        tvType=findViewById(R.id.tvType);
        tvYear=findViewById(R.id.tvYear);
        ivPic = findViewById(R.id.ivPic);

        databaseService = DataBaseService.DatabaseService.getInstance();

        // קבלת ה-ID מה-Intent (חשוב להגדיר כ-final לשימוש בתוך ה-Listener)
        final String itemId = getIntent().getStringExtra("ITEM_ID");

        if (itemId != null) {
            databaseService.getItemById(itemId, new DataBaseService.DatabaseCallback<Item>() {
                @Override
                public void onCompleted(Item item) {
                    if (item == null) {
                        Toast.makeText(Itemdetails.this, "הפריט לא נמצא", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // עדכון התצוגה בנתוני הפריט
                    tvName.setText(item.getName());
                    tvDescription.setText(item.getDetails());
                    tvPrice.setText(String.valueOf(item.getPrice()));
                    tvBrand.setText("Brand: " + item.getBrand());
                    tvType.setText("Type: "+item.getType());
                    tvYear.setText("Year: " +String.valueOf((item.getYear())));
                    if (item.getPic() != null && !item.getPic().isEmpty()) {
                        ivPic.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
                    }
                }



                    @Override
                    public void onFailed(Exception e) {
                        tvName.setText("Error loading item");
                    }
                });
            }
        }
    }