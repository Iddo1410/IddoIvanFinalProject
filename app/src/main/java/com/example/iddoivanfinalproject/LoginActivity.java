package com.example.iddoivanfinalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iddoivanfinalproject.model.User;
import com.example.iddoivanfinalproject.services.DataBaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private FirebaseAuth mAuth;
    private DataBaseService.DatabaseService dataBaseService;

    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        dataBaseService = DataBaseService.DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvSignup);

        // טעינת פרטים שמורים
        etEmail.setText(sharedPreferences.getString("email", ""));
        etPassword.setText(sharedPreferences.getString("password", ""));

        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnLogin.getId()) {

            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            // שמירה ב-SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", email);
            editor.putString("password", password);
            editor.apply();

            if (!checkInput(email, password)) {
                return;
            }

            loginUser(email, password);

        } else if (v.getId() == tvRegister.getId()) {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        }
    }

    private boolean checkInput(String email, String password) {

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email address");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters long");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser == null) {
                            etPassword.setError("שגיאה בכניסה");
                            return;
                        }

                        String uid = firebaseUser.getUid();

                        dataBaseService.getUser(uid, new DataBaseService.DatabaseCallback<User>() {
                            @Override
                            public void onCompleted(User user) {

                                if (user == null) {
                                    etPassword.setError("משתמש לא נמצא");
                                    return;
                                }

                                if (user.isAdmin()) {

                                    Intent adminIntent = new Intent(LoginActivity.this, AdminPage.class);
                                    adminIntent.putExtra("USER_ID", user.getId());
                                    adminIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(adminIntent);

                                } else {

                                    Intent userIntent = new Intent(LoginActivity.this, UsersPage.class);
                                    userIntent.putExtra("USER_ID", user.getId());
                                    userIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(userIntent);
                                }
                            }

                            @Override
                            public void onFailed(Exception e) {
                                etPassword.setError("שגיאה בטעינת המשתמש");
                            }
                        });

                    } else {
                        etPassword.setError("אימייל או סיסמה שגויים");
                    }
                });
    }
}