package com.example.iddoivanfinalproject;

import android.os.Bundle;
import android.widget.Toast;
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
    }

    private void loadCartItems() {
        String currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId != null) {
            databaseService.getCartList(currentUserId, new DataBaseService.DatabaseCallback<List<Cart>>() {
                @Override
                public void onCompleted(List<Cart> carts) {
                    if (carts != null && !carts.isEmpty()) {
                        // יצירת האדפטר עם פונקציית המחיקה
                        adapter = new CartAdapter(carts, cart -> {
                            deleteItem(cart);
                        });
                        rvCart.setAdapter(adapter);
                    } else {
                        rvCart.setAdapter(null); // ניקוי הרשימה אם היא ריקה
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

    private void deleteItem(Cart cart) {
        // קריאה לפונקציית המחיקה ב-DatabaseService
        databaseService.deleteCartItem(cart.getUserId(),cart.getId(), new DataBaseService.DatabaseCallback<Void>() {
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