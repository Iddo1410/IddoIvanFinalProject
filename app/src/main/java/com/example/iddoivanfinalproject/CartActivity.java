package com.example.iddoivanfinalproject;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iddoivanfinalproject.adapter.CartAdapter;
import com.example.iddoivanfinalproject.model.Cart;
import com.example.iddoivanfinalproject.services.DataBaseService;

import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private CartAdapter adapter;
    private DataBaseService.DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCart = findViewById(R.id.rvCart);
        rvCart.setLayoutManager(new LinearLayoutManager(this));

        databaseService = DataBaseService.DatabaseService.getInstance();

        // משיכת הפריטים שנמצאים בעגלה מהפיירבייס
        databaseService.getCartList(new DataBaseService.DatabaseCallback<List<Cart>>() {
            @Override
            public void onCompleted(List<Cart> carts) {
                if (carts != null && !carts.isEmpty()) {
                    adapter = new CartAdapter(carts);
                    rvCart.setAdapter(adapter);
                } else {
                    Toast.makeText(CartActivity.this, "העגלה ריקה", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(CartActivity.this, "שגיאה בטעינת העגלה", Toast.LENGTH_SHORT).show();
            }
        });
    }
}