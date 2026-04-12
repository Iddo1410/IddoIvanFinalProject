package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iddoivanfinalproject.model.User;
import com.example.iddoivanfinalproject.services.DataBaseService;
import com.google.firebase.auth.FirebaseAuth;

public class UsersPage extends AppCompatActivity {

    private TextView tvHi;
    private DataBaseService.DatabaseService dataBaseService;
    private Button btnShop, btnCompare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_users_page);

        // קישור רכיבים
        tvHi = findViewById(R.id.tvHiUser);
        btnShop = findViewById(R.id.btnShop);
        btnCompare = findViewById(R.id.btnCompare);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dataBaseService = DataBaseService.DatabaseService.getInstance();
        String userId = getIntent().getStringExtra("USER_ID");

        // טעינת שם המשתמש
        if (userId != null) {
            dataBaseService.getUser(userId, new DataBaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    if (user != null) tvHi.setText("היי " + user.getFname());
                }
                @Override
                public void onFailed(Exception e) {
                    tvHi.setText("היי");
                }
            });
        }

        // לחיצה על כפתור החנות
        if (btnShop != null) {
            btnShop.setOnClickListener(v -> {
                Intent intent = new Intent(UsersPage.this, Items.class);
                startActivity(intent);
            });
        }

        // לחיצה על כפתור השוואה - התיקון כאן
        if (btnCompare != null) {
            btnCompare.setOnClickListener(v -> {
                // וודא שיצרת Activity כזה! אם לא, שנה את השם ל-Userdetails או Activity קיים אחר
                try {
                    Intent intent = new Intent(UsersPage.this, CompareList.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "הדף לא נמצא", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // התנתקות
        Button btnLogout = findViewById(R.id.btnLogoutUser);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(UsersPage.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}