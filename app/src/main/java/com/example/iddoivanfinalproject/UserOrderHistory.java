package com.example.iddoivanfinalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iddoivanfinalproject.adapter.OrderAdapter;
import com.example.iddoivanfinalproject.model.Order;
import com.example.iddoivanfinalproject.services.DataBaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class UserOrderHistory extends AppCompatActivity {

    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    Button btnBack;
    private DataBaseService.DatabaseService databaseService;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initViews();
        loadOrders();
        btnBack = findViewById(R.id.btnUniversalBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        // הגדרת פס טעינה (אופציונלי - אם הוספת ל-XML)
        progressBar = findViewById(R.id.progressBar);

        databaseService = DataBaseService.DatabaseService.getInstance();
    }

    private void loadOrders() {
        // קבלת המשתמש המחובר מ-Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "שגיאה: משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getUid();

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // קריאה לשירות מסד הנתונים למשיכת כל ההזמנות
        databaseService.getAllOrders(new DataBaseService.DatabaseCallback<List<Order>>() {
            @Override
            public void onCompleted(List<Order> orders) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (orders != null && !orders.isEmpty()) {
                    List<Order> userOrders = new ArrayList<>();

                    // סינון הרשימה כך שיוצגו רק הזמנות של המשתמש הנוכחי
                    for (Order order : orders) {
                        // שים לב: ודא שבמחלקה Order יש לך מתודה getUserId() שמחזירה את ה-uid של המזמין
                        if (order.getUserId() != null && order.getUserId().equals(currentUserId)) {
                            userOrders.add(order);
                        }
                    }

                    if (!userOrders.isEmpty()) {
                        // חיבור האדפטר לרשימה המסוננת
                        adapter = new OrderAdapter(userOrders);
                        rvOrders.setAdapter(adapter);
                    } else {
                            Toast.makeText(UserOrderHistory.this, "אין היסטוריית רכישות להצגה עבורך", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserOrderHistory.this, "אין היסטוריית רכישות כלל", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(UserOrderHistory.this, "שגיאה בטעינת הנתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}