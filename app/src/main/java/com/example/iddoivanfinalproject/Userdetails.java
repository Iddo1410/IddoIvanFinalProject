package com.example.iddoivanfinalproject;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iddoivanfinalproject.model.User;
import com.example.iddoivanfinalproject.services.DataBaseService;

public class Userdetails extends AppCompatActivity {

    TextView tvUserDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdetails);

        tvUserDetails = findViewById(R.id.tvUserDetails);

        String email = getIntent().getStringExtra("email");
        String password=getIntent().getStringExtra(("password"));

        DataBaseService.DatabaseService.getInstance()
                .getUserByEmailAndPassword(email, password, new DataBaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User user) {
                        tvUserDetails.setText(
                                "שם פרטי: " + user.getFname() + "\n" +
                                        "שם משפחה: " + user.getLname() + "\n" +
                                        "אימייל: " + user.getEmail() + "\n" +
                                        "טלפון: " + user.getPhoneNumber()+ "\n" +
                                        "שם משפחה: " + user.getPassword()

                        );
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(Userdetails.this,
                                "שגיאה בטעינת פרטים",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
