package com.example.iddoivanfinalproject;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iddoivanfinalproject.adapter.CartAdapter;
import com.example.iddoivanfinalproject.model.Cart;
import com.example.iddoivanfinalproject.services.DataBaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private CartAdapter adapter;
    private DataBaseService.DatabaseService databaseService;

    // הוספת משתנים עבור כפתור הרכישה ורשימת המוצרים
    private Button btnPurchase;
    private List<Cart> currentCartList;

    // ממשק המטפל בפעולות השונות על פריט בעגלה
    public interface CartActionListener {
        void onDelete(Cart cart);
        void onQuantityChanged(Cart cart, int newQuantity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        loadCartItems();
    }

    private void initViews() {
        rvCart = findViewById(R.id.rvCart);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        databaseService = DataBaseService.DatabaseService.getInstance();

        // אתחול כפתור הרכישה והוספת מאזין לחיצה שיפתח את חלונית האישור
        btnPurchase = findViewById(R.id.btnPurchase);
        btnPurchase.setOnClickListener(v -> showPurchaseConfirmationDialog());
    }

    // פונקציה חדשה: הצגת חלונית אישור לפני רכישה
    private void showPurchaseConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("אישור רכישה")
                .setMessage("האם אתה בטוח שברצונך לבצע את הרכישה ולקנות את המוצרים בעגלה?")
                .setPositiveButton("כן, קנה עכשיו", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // אם המשתמש אישר - נבצע את הרכישה
                        processPurchase();
                    }
                })
                .setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // אם המשתמש ביטל - פשוט נסגור את החלונית
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void loadCartItems() {
        String currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId != null) {
            databaseService.getCartList(currentUserId, new DataBaseService.DatabaseCallback<List<Cart>>() {
                @Override
                public void onCompleted(List<Cart> carts) {
                    currentCartList = carts; // שמירת הרשימה הנוכחית

                    if (carts != null && !carts.isEmpty()) {
                        btnPurchase.setEnabled(true); // אפשור כפתור הרכישה אם העגלה לא ריקה

                        adapter = new CartAdapter(carts, new CartActionListener() {
                            @Override
                            public void onDelete(Cart cart) {
                                deleteItem(cart);
                            }

                            @Override
                            public void onQuantityChanged(Cart cart, int newQuantity) {
                                if (newQuantity > 0) {
                                    updateItemQuantity(cart, newQuantity);
                                } else {
                                    // אם הכמות ירדה ל-0, נמחק את הפריט
                                    deleteItem(cart);
                                }
                            }
                        });
                        rvCart.setAdapter(adapter);
                    } else {
                        rvCart.setAdapter(null); // ניקוי הרשימה אם היא ריקה
                        btnPurchase.setEnabled(false); // כיבוי כפתור הרכישה כשהעגלה ריקה
                        Toast.makeText(CartActivity.this, "העגלה שלך ריקה", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(CartActivity.this, "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // פונקציית הרכישה וניקוי כל העגלה בבת אחת (תופעל רק לאחר אישור)
    private void processPurchase() {
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null || currentCartList == null || currentCartList.isEmpty()) {
            return; // אין מה לרכוש
        }

        // קריאה לפונקציה שמוחקת את כל העגלה של המשתמש במסד הנתונים
        databaseService.clearUserCart(uid, new DataBaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void unused) {
                // הרכישה והמחיקה הצליחו
                Toast.makeText(CartActivity.this, "הרכישה בוצעה בהצלחה! העגלה רוקנה.", Toast.LENGTH_LONG).show();

                // קריאה לטעינת העגלה - תמשוך נתונים ריקים, תנקה את המסך ותכבה את הכפתור
                loadCartItems();
            }

            @Override
            public void onFailed(Exception e) {
                // טיפול בשגיאה
                Toast.makeText(CartActivity.this, "שגיאה בביצוע הרכישה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה לעדכון הכמות בדאטה-בייס
    private void updateItemQuantity(Cart cart, int newQuantity) {
        cart.setQuantity(newQuantity);

        databaseService.createNewCart(cart, new DataBaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void unused) {
                // מרענן את הרשימה כדי להציג את המחיר והכמות המעודכנים
                loadCartItems();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(CartActivity.this, "שגיאה בעדכון הכמות: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה למחיקת פריט בודד מהעגלה
    private void deleteItem(Cart cart) {
        databaseService.deleteCartItem(cart.getUserId(), cart.getId(), new DataBaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void unused) {
                Toast.makeText(CartActivity.this, "הפריט הוסר מהעגלה", Toast.LENGTH_SHORT).show();
                loadCartItems(); // טעינה מחדש של הרשימה לאחר המחיקה
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(CartActivity.this, "המחיקה נכשלה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}