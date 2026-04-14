package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.iddoivanfinalproject.model.Cart;
import com.example.iddoivanfinalproject.model.Compareitem;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.services.DataBaseService;
import com.example.iddoivanfinalproject.utils.ImageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Itemdetails extends AppCompatActivity {

    private TextView tvName, tvDescription, tvPrice, tvBrand, tvType, tvYear;
    private ImageView ivPic;
    private Button btnBack, btnAddToCart, btnGoToCompare;
    private CheckBox cbCompare;
    private DataBaseService.DatabaseService databaseService;

    Compareitem compareitem = new Compareitem();
    Item currentItem;
    String formattedDate;
    private String itemId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemdetails);

        formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        initViews();

        itemId = getIntent().getStringExtra("ITEM_ID");
        if (itemId != null) {
            loadItemData();
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
        btnGoToCompare = findViewById(R.id.btnGoToCompare);

        databaseService = DataBaseService.DatabaseService.getInstance();

        if (btnGoToCompare != null) {
            btnGoToCompare.setOnClickListener(v -> startActivity(new Intent(this, CompareList.class)));
        }

        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void addToCart() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "עליך להתחבר קודם", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentItem != null) {
            // קריאה לרשימת העגלות של המשתמש
            databaseService.getCartList(user.getUid(), new DataBaseService.DatabaseCallback<List<Cart>>() {
                @Override
                public void onCompleted(List<Cart> carts) {
                    Cart existingCartItem = null;

                    // חיפוש אם המוצר כבר קיים בעגלה
                    if (carts != null) {
                        for (Cart cart : carts) {
                            if (cart.getName() != null && cart.getName().equals(currentItem.getName())) {
                                existingCartItem = cart;
                                break;
                            }
                        }
                    }

                    if (existingCartItem != null) {
                        // אם המוצר קיים, נגדיל את הכמות שלו
                        existingCartItem.setQuantity(existingCartItem.getQuantity() + 1);

                        databaseService.createNewCart(existingCartItem, new DataBaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void object) {
                                Toast.makeText(Itemdetails.this, "הכמות עודכנה בעגלה!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Toast.makeText(Itemdetails.this, "שגיאה בעדכון הכמות", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // אם המוצר לא קיים, ניצור אותו כחדש
                        String cartId = databaseService.generateCartId();
                        Cart cartItem = new Cart(currentItem.getName(), currentItem.getPrice(), 1, cartId, user.getUid(), currentItem.getPic());

                        databaseService.createNewCart(cartItem, new DataBaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void object) {
                                Toast.makeText(Itemdetails.this, "נוסף לעגלה שלך!", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onFailed(Exception e) {
                                Toast.makeText(Itemdetails.this, "שגיאה בהוספה", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(Itemdetails.this, "שגיאה בטעינת העגלה", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadItemData() {
        databaseService.getItemById(itemId, new DataBaseService.DatabaseCallback<Item>() {
            @Override
            public void onCompleted(Item item) {
                if (item != null) {
                    currentItem = item;
                    tvName.setText(item.getName());
                    tvDescription.setText(item.getDetails());
                    tvPrice.setText(String.valueOf(item.getPrice()));
                    tvBrand.setText("Brand: " + item.getBrand());
                    tvType.setText("Type: " + item.getType());
                    tvYear.setText("Year: " + item.getYear());
                    if (item.getPic() != null) ivPic.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));

                    setupCompareLogic();
                }
            }
            @Override
            public void onFailed(Exception e) {}
        });
    }

    private void setupCompareLogic() {
        databaseService.getCompareByType(currentItem.getType(), new DataBaseService.DatabaseCallback<Compareitem>() {
            @Override
            public void onCompleted(Compareitem dbCompare) {
                if (dbCompare != null) {
                    compareitem = dbCompare;
                    checkIfItemInCompare();
                } else {
                    compareitem = new Compareitem();
                    compareitem.setId(databaseService.generateCompareId());
                }
                setCheckboxListener();
            }
            @Override
            public void onFailed(Exception e) {}
        });
    }

    private void checkIfItemInCompare() {
        if (compareitem.getItemArrayList() != null) {
            for (Item i : compareitem.getItemArrayList()) {
                if (i.getId().equals(currentItem.getId())) {
                    cbCompare.setOnCheckedChangeListener(null);
                    cbCompare.setChecked(true);
                    break;
                }
            }
        }
    }

    private void setCheckboxListener() {
        cbCompare.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentItem == null) return;
            if (compareitem.getItemArrayList() == null) compareitem.setItemArrayList(new ArrayList<>());

            if (isChecked) {
                compareitem.getItemArrayList().add(currentItem);
                compareitem.setType(currentItem.getType());
                compareitem.setDate(formattedDate);
            } else {
                compareitem.getItemArrayList().removeIf(i -> i.getId().equals(currentItem.getId()));
            }
            databaseService.updateCompareList(compareitem, new DataBaseService.DatabaseCallback<Void>() {
                @Override public void onCompleted(Void o) {}
                @Override public void onFailed(Exception e) {}
            });
        });
    }

    public void onBack(View view) { finish(); }
}