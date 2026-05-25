package com.example.iddoivanfinalproject; // הגדרת מיקום הקובץ בתוך חבילות הפרויקט

// ייבוא מחלקות וספריות הנדרשות לפעולת המסך
import android.content.Context; // מחלקה המספקת גישה למידע על סביבת האפליקציה (כמו זיכרון מקומי)
import android.content.Intent; // מחלקה למעבר בין מסכים
import android.content.SharedPreferences; // מחלקה לשמירת נתונים קטנים בזיכרון המכשיר (כמו אימייל וסיסמה אחרונים)
import android.os.Bundle; // מחלקה לשמירת נתונים בעת יצירת המסך
import android.util.Log; // מחלקה להדפסת הודעות למתכנת (לוג)
import android.view.View; // מחלקת הבסיס לכל הרכיבים הוויזואליים במסך
import android.widget.Button; // רכיב כפתור
import android.widget.EditText; // רכיב שדה טקסט לעריכה (להזנת קלט)
import android.widget.TextView; // רכיב תצוגת טקסט רגיל

import androidx.activity.EdgeToEdge; // מחלקה לתצוגת מסך מלאה (מקצה לקצה)
import androidx.appcompat.app.AppCompatActivity; // מחלקת האם למסכים באנדרואיד
import androidx.core.graphics.Insets; // טיפול בשוליים הפנימיים של המערכת
import androidx.core.view.ViewCompat; // טיפול בתאימות תצוגה
import androidx.core.view.WindowInsetsCompat; // טיפול בשוליים (כמו אזור סוללה/שעון)

import com.example.iddoivanfinalproject.model.User; // מודל המשתמש שיצרת
import com.example.iddoivanfinalproject.services.DataBaseService; // שירות הגישה למסד הנתונים
import com.google.firebase.auth.FirebaseAuth; // מערכת ההתחברות והאימות של Firebase
import com.google.firebase.auth.FirebaseUser; // מחלקה המייצגת משתמש מחובר ב-Firebase

// המחלקה של מסך ההתחברות. יורשת ממסך בסיסי ומיישמת מאזין ללחיצות (OnClickListener)
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity"; // תגית לשימוש בהדפסות לוג (לצרכי דיבאג)

    // הגדרת משתנים פרטיים עבור רכיבי המסך
    private EditText etEmail, etPassword; // שדות להזנת אימייל וסיסמה
    private Button btnLogin; // כפתור "התחבר"
    private Button btnBackToMain; // <-- 1. משתנה לכפתור החזרה למסך הראשי (כהערתך)
    private TextView tvRegister; // טקסט לחיץ למעבר להרשמה

    private FirebaseAuth mAuth; // משתנה לביצוע פעולות התחברות מול Firebase
    private DataBaseService.DatabaseService dataBaseService; // משתנה למשיכת נתוני המשתמש ממסד הנתונים

    public static final String MyPREFERENCES = "MyPrefs"; // שם הקובץ שבו יישמרו הנתונים המקומיים (SharedPreferences)
    SharedPreferences sharedPreferences; // משתנה לגישה לזיכרון המקומי

    @Override
    protected void onCreate(Bundle savedInstanceState) { // פונקציה המופעלת עם יצירת המסך
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // הגדרת תצוגה על כל המסך (כולל מאחורי שורת הסטטוס)

        setContentView(R.layout.activity_login); // חיבור קובץ העיצוב (XML) למסך הנוכחי

        // הוספת מרווחים (Padding) כדי שהתוכן לא יוסתר על ידי שורת הסטטוס או כפתורי הניווט של המכשיר
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // אתחול האובייקט שקורא וכותב לזיכרון המקומי במצב פרטי (רק האפליקציה הזו יכולה לגשת)
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        dataBaseService = DataBaseService.DatabaseService.getInstance(); // קבלת מופע לשירות מסד הנתונים
        mAuth = FirebaseAuth.getInstance(); // קבלת מופע לשירות האימות של Firebase

        // קישור משתני התצוגה ב-Java ל-ID של הרכיבים בקובץ ה-XML
        etEmail = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvSignup);

        // <-- 2. קישור הכפתור מקובץ ה-XML (כהערתך)
        btnBackToMain = findViewById(R.id.btnBackToMain);

        // טעינת פרטים שמורים: מחפש בזיכרון המקומי אימייל וסיסמה, אם אין - מחזיר מחרוזת ריקה ("")
        etEmail.setText(sharedPreferences.getString("email", "")); // מציב את האימייל השמור בשדה הטקסט
        etPassword.setText(sharedPreferences.getString("password", "")); // מציב את הסיסמה השמורה

        // הגדרת מאזין ללחיצות (this - הכוונה למחלקה הנוכחית שמיישמת את הפונקציה onClick למטה)
        btnLogin.setOnClickListener(this); // מאזין ללחיצה על "התחבר"
        tvRegister.setOnClickListener(this); // מאזין ללחיצה על "הרשמה"

        // <-- 3. הוספת מאזין לכפתור החזרה (כהערתך)
        btnBackToMain.setOnClickListener(this);
    }

    // פונקציה שמופעלת אוטומטית ברגע שמעבירים לחיצה על אחד הרכיבים שהגדרנו למעלה
    @Override
    public void onClick(View v) {
        if (v.getId() == btnLogin.getId()) { // אם לחצו על כפתור "התחבר"

            String email = etEmail.getText().toString(); // משיכת האימייל שהוקלד
            String password = etPassword.getText().toString(); // משיכת הסיסמה שהוקלדה

            // שמירה ב-SharedPreferences (זיכרון מקומי) כדי שבפעם הבאה לא נצטרך להקליד שוב
            SharedPreferences.Editor editor = sharedPreferences.edit(); // פתיחת עורך לזיכרון
            editor.putString("email", email); // שמירת האימייל
            editor.putString("password", password); // שמירת הסיסמה
            editor.apply(); // החלת (שמירת) השינויים מידית ברקע

            // בדיקת תקינות קלט (האם הזינו ערכים תקינים לפני שפונים לשרת)
            if (!checkInput(email, password)) {
                return; // אם הקלט לא תקין, הפעולה עוצרת פה (הפונקציה מסתיימת)
            }

            loginUser(email, password); // אם הקלט תקין, קריאה לפונקציית ההתחברות

        } else if (v.getId() == tvRegister.getId()) { // אם לחצו על "הרשמה"
            startActivity(new Intent(LoginActivity.this, SignupActivity.class)); // מעבר למסך ההרשמה

        } else if (v.getId() == btnBackToMain.getId()) { // אם לחצו על כפתור "חזרה"
            // <-- 4. הפעולה שתקרה בלחיצה: חזרה לדף הראשי וסגירת חלון ההתחברות (כהערתך)
            startActivity(new Intent(LoginActivity.this, MainActivity.class)); // מעבר למסך הפתיחה
            finish(); // סגירת מסך ההתחברות לחלוטין
        }
    }

    // פונקציה שבודקת האם מה שהמשתמש הקליד הוא חוקי
    private boolean checkInput(String email, String password) {

        if (email.isEmpty()) { // אם שדה האימייל ריק
            etEmail.setError("Email is required"); // הצגת הודעת שגיאה אדומה ליד השדה
            etEmail.requestFocus(); // מיקוד (העברת הסמן) לשדה הבעייתי
            return false; // מחזיר "שקר" (קלט לא תקין)
        }

        // בדיקה שהאימייל תקין מבחינת פורמט (מכיל @ ונקודה) בעזרת תבניות מובנות של אנדרואיד
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email address"); // הודעת שגיאה על אימייל לא חוקי
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) { // אם שדה הסיסמה ריק
            etPassword.setError("Password is required"); // הודעת שגיאה
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) { // בדיקה שהסיסמה מכילה לפחות 6 תווים (דרישה של Firebase)
            etPassword.setError("Password must be at least 6 characters long"); // הודעת שגיאה
            etPassword.requestFocus();
            return false;
        }

        return true; // אם כל הבדיקות עברו בהצלחה, מחזיר "אמת" (קלט תקין)
    }

    // פונקציה המבצעת את תהליך ההתחברות מול השרת
    private void loginUser(String email, String password) {
        // בקשה ל-Firebase לבצע התחברות בעזרת האימייל והסיסמה
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> { // מאזין לסיום הפעולה (הצלחה או כישלון)
                    if (task.isSuccessful()) { // אם ההתחברות הצליחה מול Firebase

                        FirebaseUser firebaseUser = mAuth.getCurrentUser(); // משיכת האובייקט של המשתמש המחובר
                        if (firebaseUser == null) { // בדיקת ביטחון - אם משום מה האובייקט ריק
                            etPassword.setError("שגיאה בכניסה");
                            return;
                        }

                        String uid = firebaseUser.getUid(); // שליפת המזהה הייחודי (UID) של המשתמש שכרגע התחבר

                        // פנייה למסד הנתונים כדי למשוך את שאר הפרטים על המשתמש הזה (כמו למשל האם הוא מנהל)
                        dataBaseService.getUser(uid, new DataBaseService.DatabaseCallback<User>() {
                            @Override
                            public void onCompleted(User user) { // כשמסד הנתונים מחזיר את פרטי המשתמש

                                if (user == null) { // אם לא נמצא רישום של המשתמש במסד הנתונים שלך
                                    etPassword.setError("משתמש לא נמצא");
                                    return;
                                }

                                if (user.isAdmin()) { // בדיקה: האם המשתמש מוגדר כמנהל (admin)

                                    // יצירת כוונה למעבר למסך מנהל
                                    Intent adminIntent = new Intent(LoginActivity.this, AdminPage.class);
                                    adminIntent.putExtra("USER_ID", user.getId()); // העברת מזהה המשתמש למסך הבא
                                    // הוספת דגלים (Flags) המנקים את היסטוריית המסכים.
                                    // זה מבטיח שאם המנהל ילחץ "אחורה", הוא לא יחזור בטעות למסך ההתחברות, אלא יצא מהאפליקציה.
                                    adminIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(adminIntent); // ביצוע המעבר בפועל

                                } else { // אם המשתמש הוא לקוח רגיל (לא מנהל)

                                    // יצירת כוונה למעבר למסך לקוחות
                                    Intent userIntent = new Intent(LoginActivity.this, UsersPage.class);
                                    userIntent.putExtra("USER_ID", user.getId()); // העברת המזהה
                                    // מחיקת היסטוריית המסכים גם פה, מאותה סיבה
                                    userIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(userIntent); // מעבר למסך הלקוח
                                }
                            }

                            @Override
                            public void onFailed(Exception e) { // אם קריאת הנתונים מהמסד נכשלה עקב שגיאת רשת וכו'
                                etPassword.setError("שגיאה בטעינת המשתמש"); // הצגת הודעת שגיאה
                            }
                        });

                    } else { // אם ההתחברות מול Firebase נכשלה (למשל סיסמה שגויה או משתמש לא קיים)
                        etPassword.setError("אימייל או סיסמה שגויים"); // הצגת שגיאה למשתמש
                    }
                });
    }
}