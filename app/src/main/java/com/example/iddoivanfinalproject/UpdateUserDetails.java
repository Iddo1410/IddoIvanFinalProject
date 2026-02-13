package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.iddoivanfinalproject.R;

public class UpdateUserDetails extends AppCompatActivity {

    EditText etFname, etLname, etEmail, etPhone;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_details);

        etFname = findViewById(R.id.etFname);
        etLname = findViewById(R.id.etLname);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);

        // קבלת נתונים
        Intent intent = getIntent();
        etFname.setText(intent.getStringExtra("fname"));
        etLname.setText(intent.getStringExtra("lname"));
        etEmail.setText(intent.getStringExtra("email"));
        etPhone.setText(intent.getStringExtra("phoneNumber"));

        btnSave.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("fname", etFname.getText().toString());
            resultIntent.putExtra("lname", etLname.getText().toString());
            resultIntent.putExtra("email", etEmail.getText().toString());
            resultIntent.putExtra("phoneNumber", etPhone.getText().toString());

            setResult(RESULT_OK, resultIntent);
            finish(); // חזרה למסך הקודם
        });
    }
}
