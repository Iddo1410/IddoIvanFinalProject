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

import com.example.iddoivanfinalproject.R;
import com.example.iddoivanfinalproject.model.User;
import com.example.iddoivanfinalproject.services.DataBaseService;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



/// Activity for logging in the user
/// This activity is used to log in the user
/// It contains fields for the user to enter their email and password
/// It also contains a button to log in the user
/// When the user is logged in, they are redirected to the main activity
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    String email2,pass2;
    private FirebaseAuth mAuth;
    private DataBaseService.DatabaseService dataBaseService;
    public static final String MyPREFERENCES="MyPrefs";
    SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        /// set the layout for the activity
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /// get the views
        sharedPreferences=getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        dataBaseService=DataBaseService.DatabaseService.getInstance();
        mAuth=FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvSignup);
        email2=sharedPreferences.getString("email","");
        pass2=sharedPreferences.getString("password","");
        etEmail.setText(email2);
        etPassword.setText(pass2);
        /// set the click listener
        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnLogin.getId()) {
            Log.d(TAG, "onClick: Login button clicked");

            /// get the email and password entered by the user
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("email", email);
            editor.putString("password", password);

            editor.commit();

            /// log the email and password
            Log.d(TAG, "onClick: Email: " + email);
            Log.d(TAG, "onClick: Password: " + password);

            Log.d(TAG, "onClick: Validating input...");
            /// Validate input
            if (!checkInput(email, password)) {
                /// stop if input is invalid
                return;
            }

            Log.d(TAG, "onClick: Logging in user...");

            /// Login user
            loginUser(email, password);
        } else if (v.getId() == tvRegister.getId()) {
            /// Navigate to Register Activity
            Intent registerIntent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(registerIntent);
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
                            etPassword.setError("שגיאה בכניסה, נסה שוב");
                            etPassword.requestFocus();
                            return;
                        }
                        String uid = firebaseUser.getUid();

                        dataBaseService.getUser(uid, new DataBaseService.DatabaseCallback<User>() {
                            @Override
                            public void onCompleted(User user) {
                                Log.d(TAG, "Login success, user: " + user.getId());

                                Intent homepageIntent = new Intent(LoginActivity.this, AdminPage.class);
                                homepageIntent.putExtra("USER_ID", user.getId());
                                homepageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(homepageIntent);
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Log.e(TAG, "Failed to get user data", e);
                                etPassword.setError("שגיאה בטעינת נתוני המשתמש");
                                etPassword.requestFocus();
                            }
                        });

                    } else {
                        etPassword.setError("אימייל או סיסמה שגויים");
                        etPassword.requestFocus();
                        Log.e(TAG, "Login failed", task.getException());
                    }
                });



    }
}