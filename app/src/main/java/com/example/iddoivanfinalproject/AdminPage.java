package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ViewUtils;
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
        Button btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(AdminPage.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });


    }
    public void onClick (View v){
        if(v.getId()==R.id.btnUsers){
            Intent intent= new Intent(AdminPage.this, Allusers.class);
            startActivity(intent);
        }
    }
    public void onAdd (View v){
        if(v.getId()==R.id.btnAddItem){
            Intent intent= new Intent(AdminPage.this, Additemtostore.class);
            startActivity(intent);
        }
    }
    public void onShop (View v){
        if (v.getId()==R.id.button)
        {
            Intent intent=new Intent(AdminPage.this, Items.class);
            startActivity(intent);
        }
    }
}

