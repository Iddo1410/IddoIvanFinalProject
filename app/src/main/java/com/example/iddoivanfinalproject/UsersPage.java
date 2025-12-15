package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_users_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvHi = findViewById(R.id.tvHi);
        dataBaseService = DataBaseService.DatabaseService.getInstance();

        String userId = getIntent().getStringExtra("USER_ID");

        if (userId == null) {
            tvHi.setText("היי");
            return;
        }

        dataBaseService.getUser(userId, new DataBaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                tvHi.setText("היי " + user.getFname());
            }

            @Override
            public void onFailed(Exception e) {
                tvHi.setText("היי");
            }
        });
        Button btnLogout = findViewById(R.id.btnLogoutUser);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(UsersPage.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

}