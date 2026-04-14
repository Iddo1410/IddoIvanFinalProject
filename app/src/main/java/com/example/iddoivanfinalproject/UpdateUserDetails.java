package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.iddoivanfinalproject.services.DataBaseService;
import com.google.firebase.auth.FirebaseAuth;

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

        // קבלת נתונים ראשונית לתצוגה
        Intent intent = getIntent();
        etFname.setText(intent.getStringExtra("fname"));
        etLname.setText(intent.getStringExtra("lname"));
        etEmail.setText(intent.getStringExtra("email"));
        etPhone.setText(intent.getStringExtra("phoneNumber"));

        btnSave.setOnClickListener(v -> {
            String newFname = etFname.getText().toString();
            String newLname = etLname.getText().toString();
            String newEmail = etEmail.getText().toString();
            String newPhone = etPhone.getText().toString();

            // מציאת ה-ID של המשתמש (אם העברנו אותו מהמסך הקודם, ואם לא - מהמשתמש המחובר)
            String userId = getIntent().getStringExtra("userId");
            if (userId == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }

            if (userId == null) {
                Toast.makeText(UpdateUserDetails.this, "שגיאה: לא נמצא מזהה משתמש", Toast.LENGTH_SHORT).show();
                return;
            }

            // קריאה לפונקציה החדשה שלנו לעדכון השדות בפיירבייס!
            DataBaseService.DatabaseService.getInstance().updateUserFields(
                    userId, newFname, newLname, newEmail, newPhone, new DataBaseService.DatabaseCallback<Void>() {

                        @Override
                        public void onCompleted(Void object) {
                            Toast.makeText(UpdateUserDetails.this, "הפרטים עודכנו בהצלחה!", Toast.LENGTH_SHORT).show();

                            // מחזירים את הנתונים החדשים למסך הקודם כדי שיתרענן מיד
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("fname", newFname);
                            resultIntent.putExtra("lname", newLname);
                            resultIntent.putExtra("email", newEmail);
                            resultIntent.putExtra("phoneNumber", newPhone);

                            setResult(RESULT_OK, resultIntent);
                            finish(); // סוגר את המסך וחוזר אחורה
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(UpdateUserDetails.this, "העדכון נכשל: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
