package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Userdetails extends AppCompatActivity {

    TextView tvDetails;
    Button btnUpdate;

    SharedPreferences sp;

    String fname, lname, email, phoneNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdetails);

        tvDetails = findViewById(R.id.tvUserDetails);
        btnUpdate = findViewById(R.id.btnUpdate);

        // SharedPreferences
        sp = getSharedPreferences("user_data", MODE_PRIVATE);

        // קבלת נתונים – קודם מ־SharedPreferences ואם אין אז מה־Intent
        Intent intent = getIntent();

        fname = sp.getString("fname", intent.getStringExtra("fname"));
        lname = sp.getString("lname", intent.getStringExtra("lname"));
        email = sp.getString("email", intent.getStringExtra("email"));
        phoneNumber = sp.getString("phoneNumber", intent.getStringExtra("phoneNumber"));

        updateText();

        btnUpdate.setOnClickListener(v -> {
            Intent i = new Intent(Userdetails.this, UpdateUserDetails.class);
            i.putExtra("fname", fname);
            i.putExtra("lname", lname);
            i.putExtra("email", email);
            i.putExtra("phoneNumber", phoneNumber);

            startActivityForResult(i, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            fname = data.getStringExtra("fname");
            lname = data.getStringExtra("lname");
            email = data.getStringExtra("email");
            phoneNumber = data.getStringExtra("phnumber");

            // שמירה קבועה
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("fname", fname);
            editor.putString("lname", lname);
            editor.putString("email", email);
            editor.putString("phnumber", phoneNumber);
            editor.apply();

            updateText();
        }
    }

    private void updateText() {
        tvDetails.setText(
                "שם פרטי: " + fname + "\n" +
                        "שם משפחה: " + lname + "\n" +
                        "אימייל: " + email + "\n" +
                        "מספר טלפון: " + phoneNumber
        );
    }
}



