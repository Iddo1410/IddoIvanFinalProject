package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
        } else {
            // ה-return שהיה כאן בעבר הוסר כדי שהקוד למטה יוכל להמשיך לרוץ
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

        // --- כפתור התנתקות ---
        Button btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(AdminPage.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // --- התוספת החדשה: כפתור היסטוריית רכישות ---
        // ודא שה-ID של הכפתור בקובץ activity_admin_page.xml הוא אכן btnHistory
        Button btnHistory = findViewById(R.id.btnHistory);
        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> {
                Intent intent = new Intent(AdminPage.this, OrderHistory.class);
                startActivity(intent);
            });
        }
    }
    public void onShop(View v) {
        if (v.getId() == R.id.btnShopAdmin) {
            Intent intent = new Intent(AdminPage.this, Items.class);
            startActivity(intent);
        }
    }

    // מעבר לעמוד המשתמשים (מופעל מה-XML על ידי android:onClick="onClick")
    public void onClick(View v) {
        if (v.getId() == R.id.btnUsers) {
            Intent intent = new Intent(AdminPage.this, Allusers.class);
            startActivity(intent);
        }
    }

    // מעבר להוספת פריט (מופעל מה-XML על ידי android:onClick="onAdd")
    public void onAdd(View v) {
        if (v.getId() == R.id.btnAddItem) {
            Intent intent = new Intent(AdminPage.this, Additemtostore.class);
            startActivity(intent);
        }
    }

    // פונקציית גיבוי למעבר להיסטוריית רכישות
    // (למקרה שתגדיר בכפתור ב-XML את התכונה android:onClick="onHistory")
    public void onHistory(View v) {
        if (v.getId() == R.id.btnHistory) {
            Intent intent = new Intent(AdminPage.this, OrderHistory.class);
            startActivity(intent);
        }
    }
}
