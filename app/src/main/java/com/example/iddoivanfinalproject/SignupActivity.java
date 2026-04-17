package com.example.iddoivanfinalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iddoivanfinalproject.R;
import com.example.iddoivanfinalproject.model.User;
import com.example.iddoivanfinalproject.services.DataBaseService;


/// Activity for registering the user
/// This activity is used to register the user
/// It contains fields for the user to enter their information
/// It also contains a button to register the user
/// When the user is registered, they are redirected to the main activity
public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private EditText etEmail, etPassword, etFName, etLName, etPhone;
    String email,password;
    private Button btnRegister, btnBacktoMain;
    private TextView tvLogin;

    private DataBaseService.DatabaseService dataBaseService;
    public static final String MyPREFERENCES="MyPrefs";
    SharedPreferences sharedPreferences;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        /// set the layout for the activity
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dataBaseService= DataBaseService.DatabaseService.getInstance();

        /// get the views
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etFName = findViewById(R.id.fname);
        etLName = findViewById(R.id.lname);
        etPhone = findViewById(R.id.phNumber);
        btnRegister = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);
        btnBacktoMain=findViewById(R.id.btnBackToMain);

        /// set the click listener
        btnRegister.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
        sharedPreferences=getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnRegister.getId()) {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String fName = etFName.getText().toString().trim();
            String lName = etLName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            // --- תוספת הבדיקות לפני שנרשמים ל-Firebase ---

            // 1. בדיקה ששום שדה לא ריק
            if (email.isEmpty() || password.isEmpty() || fName.isEmpty() || lName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "נא למלא את כל הפרטים", Toast.LENGTH_SHORT).show();
                return; // עוצר את הפונקציה כאן ולא ממשיך להרשמה
            }

            // 2. פיירבייס דורש סיסמה של 6 תווים לפחות, נבדוק את זה כדי שלא יקרוס
            if (password.length() < 6) {
                etPassword.setError("סיסמה חייבת להכיל לפחות 6 תווים");
                etPassword.requestFocus();
                return;
            }

            // 3. (אופציונלי אבל מומלץ) בדיקה שהאימייל בפורמט תקין
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("אימייל לא תקין");
                etEmail.requestFocus();
                return;
            }

            // אם הכל תקין - ממשיכים להרשמה
            registerUser(fName, lName, phone, email, password);

        } else if (v.getId() == tvLogin.getId()) {
            Intent registerIntent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(registerIntent);
        } else if (v.getId() == btnBacktoMain.getId()) {
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }






    /// Register the user
    private void registerUser(String fname, String lname, String phone, String email, String password) {
        Log.d(TAG, "registerUser: Registering user...");

        String uid;


        /// create a new user object
        User user = new User("jkjk", fname, lname, email,phone, password, false);



        /// proceed to create the user
        createUserInDatabase(user);

    }



    private void createUserInDatabase(User user) {

        dataBaseService.createNewUser(user, new DataBaseService.DatabaseCallback<String>() {

            @Override
            public void onCompleted(String uid) {
                Log.d(TAG, "createUserInDatabase: User created successfully");
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("email", email);
                editor.putString("password", password);

                editor.commit();

                Intent mainIntent = new Intent(SignupActivity.this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "createUserInDatabase: Failed to create user", e);
                Toast.makeText(SignupActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
