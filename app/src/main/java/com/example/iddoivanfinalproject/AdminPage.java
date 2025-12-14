package com.example.iddoivanfinalproject;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iddoivanfinalproject.model.User;
import com.example.iddoivanfinalproject.services.DataBaseService;

public class AdminPage extends AppCompatActivity {

    private TextView textView5;
    private DataBaseService.DatabaseService dataBaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textView5 = findViewById(R.id.tvHi);
        dataBaseService = DataBaseService.DatabaseService.getInstance();

        String userId = getIntent().getStringExtra("USER_ID");

        if (userId == null) {
            textView5.setText("היי");
            return;
        }

        dataBaseService.getUser(userId, new DataBaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                textView5.setText("היי " + user.getFname());
            }

            @Override
            public void onFailed(Exception e) {
                textView5.setText("היי");
            }
        });
    }
}

