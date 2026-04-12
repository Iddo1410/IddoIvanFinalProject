package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iddoivanfinalproject.model.Cart;
import com.example.iddoivanfinalproject.model.Compareitem;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.services.DataBaseService;
import com.example.iddoivanfinalproject.utils.ImageUtil;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Itemdetails extends AppCompatActivity {

    private TextView tvName, tvDescription, tvPrice, tvBrand, tvType, tvYear;
    private ImageView ivPic;
    private Button btnBack, btnAddToCart;
    private CheckBox cbCompare;
    private DataBaseService.DatabaseService databaseService;

    Compareitem compareitem = new Compareitem();
    Item currentItem;
    DateTimeFormatter formatter;
    LocalDate date;
    String formattedDate;
    private String itemId=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemdetails);

        // אתחול תאריך
        date = LocalDate.now();
        formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        formattedDate = date.format(formatter);

        // אתחול רכיבי UI
        initViews();

        // קבלת ה-ID מה-Intent
       itemId = getIntent().getStringExtra("ITEM_ID");

        if (itemId != null) {
            databaseService.getItemById(itemId, new DataBaseService.DatabaseCallback<Item>() {
                @Override
                public void onCompleted(Item item) {
                    if (item == null) {
                        Toast.makeText(Itemdetails.this, "הפריט לא נמצא", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentItem = item;
                    // עדכון התצוגה
                    tvName.setText(item.getName());
                    tvDescription.setText(item.getDetails());
                    tvPrice.setText(String.valueOf(item.getPrice()));
                    tvBrand.setText("Brand: " + item.getBrand());
                    tvType.setText("Type: " + item.getType());
                    tvYear.setText("Year: " + String.valueOf((item.getYear())));
                    if (item.getPic() != null && !item.getPic().isEmpty()) {
                        ivPic.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
                    }



                    databaseService.getCompareByType(currentItem.getType(), new DataBaseService.DatabaseCallback<Compareitem>() {
                        @Override
                        public void onCompleted(Compareitem compareitemDB) {
                            if (compareitemDB != null) {
                                compareitem = compareitemDB;


                            } else {
                                compareitem = new Compareitem();
                                compareitem.setId(databaseService.generateCompareId());


                            }

                        }
                        @Override
                        public void onFailed(Exception e) {

                            compareitem = new Compareitem();
                            compareitem.setId(databaseService.generateCompareId());
                        }
                        });
                    }

                @Override
                public void onFailed(Exception e) {
                    tvName.setText("Error loading item");
                }
            });
        }
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrice = findViewById(R.id.tvPrice);
        tvBrand = findViewById(R.id.tvBrand);
        tvType = findViewById(R.id.tvType);
        tvYear = findViewById(R.id.tvYear);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBack = findViewById(R.id.btnBack);
        cbCompare = findViewById(R.id.cbCompare);
        ivPic = findViewById(R.id.ivPic);

        databaseService = DataBaseService.DatabaseService.getInstance();

        // --- הוספת מוצר לעגלה ---
        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentItem != null) {
                    // יצירת מזהה ייחודי לשורת העגלה
                    String cartId = databaseService.generateCartId();

                    // יצירת אובייקט Cart לפי הבנאי שלך: שם, מחיר, כמות, ID
                    Cart cartItem = new Cart(
                            currentItem.getName(),
                            currentItem.getPrice(),
                            1, // כמות ברירת מחדל
                            cartId
                    );

                    // שמירה במסד הנתונים
                    databaseService.createNewCart(cartItem, new DataBaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            Toast.makeText(Itemdetails.this, "המוצר נוסף לעגלה!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(Itemdetails.this, "שגיאה בהוספה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(Itemdetails.this, "טוען נתונים, נא להמתין...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // לוגיקת צ'קבוקס השוואה
        cbCompare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean checked) {
                if (checked && currentItem != null) {
                    // הקוד המקורי שלך להשוואה

                   compareitem.setType(currentItem.getType());

                    compareitem.setDate(formattedDate);

                    if (compareitem.getItemArrayList() == null) {
                        compareitem.setItemArrayList(new ArrayList<>());
                    }
                    compareitem.getItemArrayList().add(currentItem);
                    // כאן חסרה מתודה createNewCompareList ב-Service שלך, וודא שהיא קיימת



                         databaseService.updateCompareList(compareitem, new DataBaseService.DatabaseCallback<Void>() {
                             @Override
                             public void onCompleted(Void object) {

                             }

                             @Override
                             public void onFailed(Exception e) {

                             }
                         });
                     }




        }
        });
    }

    public void onBack(View view) {
        Intent intent = new Intent(Itemdetails.this, Items.class);
        startActivity(intent);
    }
}
