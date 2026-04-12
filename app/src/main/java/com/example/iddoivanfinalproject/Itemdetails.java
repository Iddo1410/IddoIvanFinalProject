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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Itemdetails extends AppCompatActivity {

    private TextView tvName, tvDescription, tvPrice, tvBrand, tvType, tvYear;
    private ImageView ivPic;
    private Button btnBack, btnAddToCart, btnGoToCompare;
    private CheckBox cbCompare;
    private DataBaseService.DatabaseService databaseService;

    Compareitem compareitem = new Compareitem();
    Item currentItem;
    DateTimeFormatter formatter;
    LocalDate date;
    String formattedDate;
    private String itemId = null;

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

                    // משיכת רשימת ההשוואה
                    databaseService.getCompareByType(currentItem.getType(), new DataBaseService.DatabaseCallback<Compareitem>() {
                        @Override
                        public void onCompleted(Compareitem compareitemDB) {
                            if (compareitemDB != null) {
                                compareitem = compareitemDB;
                                if (compareitem.getItemArrayList() != null) {
                                    for (Item existingItem : compareitem.getItemArrayList()) {
                                        if (existingItem.getId().equals(currentItem.getId())) {
                                            cbCompare.setOnCheckedChangeListener(null);
                                            cbCompare.setChecked(true);
                                            setCheckboxListener();
                                            break;
                                        }
                                    }
                                }
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

        // אתחול כפתור המעבר להשוואה
        btnGoToCompare = findViewById(R.id.btnGoToCompare);

        databaseService = DataBaseService.DatabaseService.getInstance();

        // לחיצה על כפתור מעבר לדף ההשוואות
        if (btnGoToCompare != null) {
            btnGoToCompare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Itemdetails.this, CompareList.class);
                    startActivity(intent);
                }
            });
        }

        // הוספה לעגלה
        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentItem != null) {
                    String cartId = databaseService.generateCartId();
                    Cart cartItem = new Cart(currentItem.getName(), currentItem.getPrice(), 1, cartId);
                    databaseService.createNewCart(cartItem, new DataBaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            Toast.makeText(Itemdetails.this, "המוצר נוסף לעגלה!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(Itemdetails.this, "שגיאה בהוספה", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        setCheckboxListener();
    }

    private void setCheckboxListener() {
        cbCompare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean checked) {
                if (currentItem == null) return;

                if (compareitem.getItemArrayList() == null) {
                    compareitem.setItemArrayList(new ArrayList<>());
                }

                if (checked) {
                    boolean isAlreadyInList = false;
                    for (Item item : compareitem.getItemArrayList()) {
                        if (item.getId().equals(currentItem.getId())) {
                            isAlreadyInList = true;
                            break;
                        }
                    }

                    if (!isAlreadyInList) {
                        compareitem.setType(currentItem.getType());
                        compareitem.setDate(formattedDate);
                        compareitem.getItemArrayList().add(currentItem);

                        databaseService.updateCompareList(compareitem, new DataBaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void object) {
                                Toast.makeText(Itemdetails.this, "המוצר נוסף להשוואה", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onFailed(Exception e) { }
                        });
                    }
                } else {
                    Item itemToRemove = null;
                    for (Item item : compareitem.getItemArrayList()) {
                        if (item.getId().equals(currentItem.getId())) {
                            itemToRemove = item;
                            break;
                        }
                    }

                    if (itemToRemove != null) {
                        compareitem.getItemArrayList().remove(itemToRemove);
                        databaseService.updateCompareList(compareitem, new DataBaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void object) {
                                Toast.makeText(Itemdetails.this, "המוצר הוסר מההשוואה", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onFailed(Exception e) { }
                        });
                    }
                }
            }
        });
    }

    public void onBack(View view) {
        finish(); // סגירת הדף הנוכחי וחזרה אחורה
    }
}