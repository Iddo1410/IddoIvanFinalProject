package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Userdetails extends AppCompatActivity {

    TextView tvDetails;
    Button btnUpdate;

    SharedPreferences sp;

    // הוספנו את userId!
    String userId, fname, lname, email, phoneNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdetails);

        tvDetails = findViewById(R.id.tvUserDetails);
        btnUpdate = findViewById(R.id.btnUpdate);

        sp = getSharedPreferences("user_data", MODE_PRIVATE);

        Intent intent = getIntent();

        // בדיקה אם קיבלנו נתונים ממסך קודם
        if (intent != null && intent.hasExtra("fname")) {
            userId = intent.getStringExtra("userId"); // <--- תופסים את ה-ID
            fname = intent.getStringExtra("fname");
            lname = intent.getStringExtra("lname");
            email = intent.getStringExtra("email");
            phoneNumber = intent.getStringExtra("phoneNumber");

            SharedPreferences.Editor editor = sp.edit();
            editor.putString("userId", userId); // <--- שומרים אותו
            editor.putString("fname", fname);
            editor.putString("lname", lname);
            editor.putString("email", email);
            editor.putString("phoneNumber", phoneNumber);
            editor.apply();
        } else {
            // שולפים מהזיכרון או לוקחים את ה-UID של המחובר כברירת מחדל
            userId = sp.getString("userId", FirebaseAuth.getInstance().getUid());
            fname = sp.getString("fname", "לא ידוע");
            lname = sp.getString("lname", "לא ידוע");
            email = sp.getString("email", "לא ידוע");
            phoneNumber = sp.getString("phoneNumber", "לא הוזן מספר");
        }

        updateText();

        btnUpdate.setOnClickListener(v -> {
            Intent i = new Intent(Userdetails.this, UpdateUserDetails.class);
            i.putExtra("userId", userId); // <--- השורה הכי חשובה! מעבירים לעריכה
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
            phoneNumber = data.getStringExtra("phoneNumber");

            SharedPreferences.Editor editor = sp.edit();
            editor.putString("fname", fname);
            editor.putString("lname", lname);
            editor.putString("email", email);
            editor.putString("phoneNumber", phoneNumber);
            editor.apply();

            updateText(); // מעדכן את המסך מיד
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


