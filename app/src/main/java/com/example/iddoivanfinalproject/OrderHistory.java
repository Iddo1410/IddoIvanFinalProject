package com.example.iddoivanfinalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iddoivanfinalproject.adapter.OrderAdapter;
import com.example.iddoivanfinalproject.model.Order;
import com.example.iddoivanfinalproject.services.DataBaseService;

import java.util.List;

public class OrderHistory extends AppCompatActivity {

    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private DataBaseService.DatabaseService databaseService;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initViews();
        loadOrders();
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        // הגדרת פס טעינה (אופציונלי - אם הוספת ל-XML)
        progressBar = findViewById(R.id.progressBar);

        databaseService = DataBaseService.DatabaseService.getInstance();
    }

    private void loadOrders() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // קריאה לשירות מסד הנתונים למשיכת כל ההזמנות
        databaseService.getAllOrders(new DataBaseService.DatabaseCallback<List<Order>>() {
            @Override
            public void onCompleted(List<Order> orders) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (orders != null && !orders.isEmpty()) {
                    // חיבור האדפטר לרשימה שחזרה מהענן
                    adapter = new OrderAdapter(orders);
                    rvOrders.setAdapter(adapter);
                } else {
                    Toast.makeText(OrderHistory.this, "אין היסטוריית רכישות להצגה", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderHistory.this, "שגיאה בטעינת הנתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}