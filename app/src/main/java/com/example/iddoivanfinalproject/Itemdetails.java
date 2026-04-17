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
import com.example.iddoivanfinalproject.model.User;
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
    private Button btnBack, btnAddToCart, btnGoToCompare, btnDeleteItem;
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

        // בדיקת משתמש והצגת כפתורים מתאימים
        checkUserStatus();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrice = findViewById(R.id.tvPrice);
        tvBrand = findViewById(R.id.tvBrand);
        tvType = findViewById(R.id.tvType);
        tvYear = findViewById(R.id.tvYear);
        ivPic = findViewById(R.id.ivPic);

        // כפתורים
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnGoToCompare = findViewById(R.id.btnGoToCompare);
        btnDeleteItem = findViewById(R.id.btnDeleteItem);
        btnBack = findViewById(R.id.btnBack);
        cbCompare = findViewById(R.id.cbCompare);

        databaseService = DataBaseService.DatabaseService.getInstance();

        // הגדרת מחיקה כאן מראש (אבל הכפתור מוסתר עד שנבדוק אם זה אדמין)
        if (btnDeleteItem != null) {
            btnDeleteItem.setOnClickListener(v -> deleteCurrentItem());
        }

        if (btnGoToCompare != null) {
            btnGoToCompare.setOnClickListener(v -> {
                Intent intent = new Intent(Itemdetails.this, CompareList.class);
                if (currentItem != null) intent.putExtra("COMPARE_TYPE", currentItem.getType());
                startActivity(intent);
            });
        }

        if (btnAddToCart != null) {
            btnAddToCart.setOnClickListener(v -> addToCart());
        }
    }

    // הפונקציה שמחליטה מה להציג למי
    private void checkUserStatus() {
        // קודם כל נעלים הכל כדי שלא יהיו "הבהובים" במסך
        if (btnAddToCart != null) btnAddToCart.setVisibility(View.GONE);
        if (btnGoToCompare != null) btnGoToCompare.setVisibility(View.GONE);
        if (cbCompare != null) cbCompare.setVisibility(View.GONE);
        if (btnDeleteItem != null) btnDeleteItem.setVisibility(View.GONE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();

            // פונים למסד הנתונים כדי להביא את כל פרטי המשתמש (כולל האם הוא אדמין)
            databaseService.getUser(uid, new DataBaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    if (user != null) {
                        // --- כאן הבדיקה המקצועית: האם הוא מנהל? ---
                        if (user.isAdmin()) {
                            // זה אדמין -> מדליקים לו אך ורק את כפתור המחיקה
                            if (btnDeleteItem != null) btnDeleteItem.setVisibility(View.VISIBLE);
                        } else {
                            // זה לקוח רגיל -> מדליקים לו קנייה והשוואה
                            showCustomerButtons();
                        }
                    } else {
                        // למקרה שאין נתונים על המשתמש
                        showCustomerButtons();
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    // למקרה של שגיאה בטעינה ממסד הנתונים
                    showCustomerButtons();
                }
            });
        } else {
            // אם המשתמש הוא אורח (לא מחובר כלל)
            showCustomerButtons();
        }
    }

    // פונקציית עזר קטנה כדי לא לכתוב את אותו קוד 3 פעמים
    private void showCustomerButtons() {
        if (btnAddToCart != null) btnAddToCart.setVisibility(View.VISIBLE);
        if (btnGoToCompare != null) btnGoToCompare.setVisibility(View.VISIBLE);
        if (cbCompare != null) cbCompare.setVisibility(View.VISIBLE);
        if (btnDeleteItem != null) btnDeleteItem.setVisibility(View.GONE);
    }

    // פונקציית המחיקה מ-Firebase (מופעלת רק ע"י האדמין)
    // פונקציית המחיקה המשודרגת (למציאת הבעיה)
    // פונקציית המחיקה מ-Firebase (מופעלת רק ע"י האדמין)
    // פונקציית המחיקה מ-Firebase (מופעלת רק ע"י האדמין)
    private void deleteCurrentItem() {
        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "שגיאה: לא נמצא ID של מוצר למחיקה!", Toast.LENGTH_LONG).show();
            return;
        }

        // 1. קודם נמחוק את המוצר הראשי מהחנות
        databaseService.deleteItem(itemId, new DataBaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {

                // 2. עכשיו ניגש לרשימת ההשוואה וננקה גם אותה
                if (currentItem != null && currentItem.getType() != null) {
                    databaseService.getCompareByType(currentItem.getType(), new DataBaseService.DatabaseCallback<Compareitem>() {
                        @Override
                        public void onCompleted(Compareitem dbCompare) {
                            if (dbCompare != null && dbCompare.getItemArrayList() != null) {

                                List<Item> newList = new ArrayList<>();
                                boolean itemFoundInCompare = false;

                                // מעבר בטוח על המוצרים בהשוואה - מונע קריסות אם נתונים חסרים
                                for (Item i : dbCompare.getItemArrayList()) {
                                    // נבדוק גם לפי ID וגם לפי שם כדי להיות בטוחים ב-100%
                                    boolean isSameId = i.getId() != null && i.getId().equals(itemId);
                                    boolean isSameName = i.getName() != null && currentItem.getName() != null && i.getName().equals(currentItem.getName());

                                    if (isSameId || isSameName) {
                                        itemFoundInCompare = true; // מצאנו את המוצר! לא נוסיף אותו לרשימה החדשה
                                    } else {
                                        newList.add(i); // מוצרים אחרים נשמור ברשימה
                                    }
                                }

                                // אם המוצר אכן היה בהשוואה ונמחק, נעדכן את פיירבייס
                                if (itemFoundInCompare) {
                                    dbCompare.getItemArrayList().clear();
                                    dbCompare.getItemArrayList().addAll(newList);

                                    databaseService.updateCompareList(dbCompare, new DataBaseService.DatabaseCallback<Void>() {
                                        @Override
                                        public void onCompleted(Void o) {
                                            Toast.makeText(Itemdetails.this, "המוצר נמחק מהחנות וגם מההשוואה!", Toast.LENGTH_LONG).show();
                                            finish();
                                        }

                                        @Override
                                        public void onFailed(Exception e) {
                                            Toast.makeText(Itemdetails.this, "המוצר נמחק, אך שגיאה בעדכון ההשוואה", Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    });
                                } else {
                                    // המוצר נמחק מהחנות, אבל הוא בכלל לא היה בהשוואה
                                    Toast.makeText(Itemdetails.this, "המוצר נמחק בהצלחה!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                            } else {
                                // אין בכלל מוצרים בהשוואה מהסוג הזה
                                Toast.makeText(Itemdetails.this, "נמחק בהצלחה מ-Firebase!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(Itemdetails.this, "נמחק מהחנות. שגיאה בגישה להשוואה.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } else {
                    Toast.makeText(Itemdetails.this, "נמחק בהצלחה מ-Firebase!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(Itemdetails.this, "שגיאה במחיקת המוצר: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // פונקציית עזר קטנה כדי לחסוך קוד כפול - מציגה הודעה וסוגרת את המסך
    private void finishWithSuccess() {
        Toast.makeText(Itemdetails.this, "המוצר נמחק בהצלחה מכל המקומות!", Toast.LENGTH_SHORT).show();
        finish();
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
                if (compareitem.getItemArrayList().size() >= 2) {
                    Toast.makeText(Itemdetails.this, "ניתן להוסיף עד 2 פריטים להשוואה", Toast.LENGTH_SHORT).show();
                    cbCompare.setOnCheckedChangeListener(null);
                    cbCompare.setChecked(false);
                    setCheckboxListener();
                    return;
                }

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

    private void addToCart() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "עליך להתחבר קודם", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentItem != null) {
            databaseService.getCartList(user.getUid(), new DataBaseService.DatabaseCallback<List<Cart>>() {
                @Override
                public void onCompleted(List<Cart> carts) {
                    Cart existingCartItem = null;

                    if (carts != null) {
                        for (Cart cart : carts) {
                            if (cart.getName() != null && cart.getName().equals(currentItem.getName())) {
                                existingCartItem = cart;
                                break;
                            }
                        }
                    }

                    if (existingCartItem != null) {
                        existingCartItem.setQuantity(existingCartItem.getQuantity() + 1);
                        databaseService.createNewCart(existingCartItem, new DataBaseService.DatabaseCallback<Void>() {
                            @Override public void onCompleted(Void object) {
                                Toast.makeText(Itemdetails.this, "הכמות עודכנה בעגלה!", Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onFailed(Exception e) {}
                        });
                    } else {
                        String cartId = databaseService.generateCartId();
                        Cart cartItem = new Cart(currentItem.getName(), currentItem.getPrice(), 1, cartId, user.getUid(), currentItem.getPic());
                        databaseService.createNewCart(cartItem, new DataBaseService.DatabaseCallback<Void>() {
                            @Override public void onCompleted(Void object) {
                                Toast.makeText(Itemdetails.this, "נוסף לעגלה שלך!", Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onFailed(Exception e) {}
                        });
                    }
                }
                @Override public void onFailed(Exception e) {}
            });
        }
    }


    public void onBack(View view) { finish(); }
}