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

    private TextView tvName, tvDescription, tvPrice;
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
                    if (item.getPic() != null && !item.getPic().isEmpty()) {
                        ImageUtil.convertFrom64base(item.getPic(), ivPic);
                    }

                    // טיפול בכפתור הוספה לעגלה
                    Button btnAddToCart = findViewById(R.id.btnAddToCart);
                    if (btnAddToCart != null) {
                        btnAddToCart.setOnClickListener(v -> {
                            // יצירת אובייקט עגלה
                            Cart cartItem = new Cart(item.getName(), item.getPrice(), 1, itemId);

                            // שמירה ב-Database
                            databaseService.addToCart(cartItem, new DataBaseService.DatabaseCallback<Void>() {
                                @Override
                                public void onCompleted(Void unused) {
                                    Toast.makeText(Itemdetails.this, "נוסף לעגלה!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailed(Exception e) {
                                    Toast.makeText(Itemdetails.this, "שגיאה בהוספה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    // חובה לממש כדי למנוע שגיאת קומפילציה
                    Toast.makeText(Itemdetails.this, "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
                }
            }); // סגירה נכונה של ה-getItemById
        }
    }
}