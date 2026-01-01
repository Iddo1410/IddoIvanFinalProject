package com.example.iddoivanfinalproject;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Userdetails extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdetails);

        TextView tvDetails = findViewById(R.id.tvUserDetails);

        String fname = getIntent().getStringExtra("fname");
        String lname = getIntent().getStringExtra("lname");
        String email = getIntent().getStringExtra("email");
        String password=getIntent().getStringExtra("password");
        String phnumber=getIntent().getStringExtra("phnumber");

        tvDetails.setText(
                "שם פרטי: " + fname + "\n" +
                        "שם משפחה: " + lname + "\n" +
                        "אימייל: " + email + "\n" +
                        "סיסמה: " + password+ "\n" +
                        "מספר טלפון: " + phnumber
        );
    }
}


