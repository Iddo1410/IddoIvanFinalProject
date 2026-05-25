package com.example.iddoivanfinalproject; // מיקום הקובץ בתוך תיקיות הפרויקט

// ייבוא מחלקות וספריות של אנדרואיד הדרושות להפעלת המסך
import android.annotation.SuppressLint; // מחלקה למניעת אזהרות של המערכת (Lint) במקרים ספציפיים
import android.content.Context; // מחלקה לגישה לסביבת האפליקציה (כמו הזיכרון המקומי)
import android.content.Intent; // מחלקה לביצוע מעברים בין מסכים
import android.content.SharedPreferences; // מחלקה לשמירת נתונים קטנים בזיכרון המכשיר (כמו שמירת אימייל מראש)
import android.os.Bundle; // מחלקה לשמירת נתונים על מצב המסך בזמן יצירתו
import android.util.Log; // מחלקה להדפסת הודעות למתכנת (לוגים) לבקרת שגיאות
import android.view.View; // מחלקת הבסיס לכל הרכיבים הוויזואליים (כפתורים, טקסטים וכו')
import android.widget.EditText; // רכיב של שדה טקסט הניתן לעריכה על ידי המשתמש (קלט)
import android.widget.Button; // רכיב כפתור
import android.widget.TextView; // רכיב להצגת טקסט רגיל במסך
import android.widget.Toast; // מחלקה להצגת הודעות קופצות קצרות בתחתית המסך

import androidx.activity.EdgeToEdge; // מחלקה לתצוגת מסך "מקצה לקצה"
import androidx.appcompat.app.AppCompatActivity; // מחלקת האם הבסיסית לכל מסכי אנדרואיד
import androidx.core.graphics.Insets; // מחלקה לטיפול בשוליים הפנימיים של המערכת
import androidx.core.view.ViewCompat; // מחלקה לטיפול בתאימות תצוגה
import androidx.core.view.WindowInsetsCompat; // טיפול באזורים מיוחדים של המסך (שורת סטטוס וכדומה)

import com.example.iddoivanfinalproject.R; // גישה לקובץ המשאבים הראשי של הפרויקט
import com.example.iddoivanfinalproject.model.User; // ייבוא מודל הנתונים של המשתמש שיצרת
import com.example.iddoivanfinalproject.services.DataBaseService; // שירות התקשורת מול מסד הנתונים

/// Activity for registering the user
/// This activity is used to register the user
/// It contains fields for the user to enter their information
/// It also contains a button to register the user
/// When the user is registered, they are redirected to the main activity
// המחלקה של מסך ההרשמה. היא יורשת ממסך אנדרואיד רגיל ומיישמת מאזין ללחיצות (OnClickListener)
public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity"; // תגית שתשמש אותנו להדפסת הודעות (Logs) למסוף

    // הגדרת משתנים לרכיבי המסך
    private EditText etEmail, etPassword, etFName, etLName, etPhone; // שדות הקלט (אימייל, סיסמה, שם פרטי, משפחה, טלפון)
    String email, password; // משתנים ברמת המחלקה לשמירת האימייל והסיסמה
    private Button btnRegister, btnBacktoMain; // כפתורי "הרשם" ו"חזור לראשי"
    private TextView tvLogin; // כפתור טקסט המעביר למסך ההתחברות (למי שכבר יש חשבון)

    private DataBaseService.DatabaseService dataBaseService; // משתנה להתקשרות מול מסד הנתונים ב-Firebase
    public static final String MyPREFERENCES="MyPrefs"; // שם הקובץ שבו יישמרו נתונים מקומיים בטלפון
    SharedPreferences sharedPreferences; // משתנה שיאפשר קריאה וכתיבה לזיכרון המקומי

    @SuppressLint("MissingInflatedId") // מתעלם מאזהרה במקרה ששם של ID קצת שונה ממה שהמערכת ציפתה
    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה המרכזית שרצה עם פתיחת המסך
        super.onCreate(savedInstanceState); // קריאה לפעולת ההקמה של מחלקת האם
        EdgeToEdge.enable(this); // הגדרת תצוגת מסך מלאה

        /// set the layout for the activity
        setContentView(R.layout.activity_signup); // חיבור קוד ה-Java הנוכחי אל קובץ ה-XML של העיצוב

        // הגדרת מרווחים פנימיים למסך כדי שהתוכן לא יוסתר מאחורי שורת הסוללה/סטטוס העליונה
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dataBaseService = DataBaseService.DatabaseService.getInstance(); // קבלת מופע (חיבור) של שירות מסד הנתונים

        /// get the views
        // קישור המשתנים ב-Java אל הרכיבים בקובץ ה-XML באמצעות ה-ID שלהם
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etFName = findViewById(R.id.fname);
        etLName = findViewById(R.id.lname);
        etPhone = findViewById(R.id.phNumber);
        btnRegister = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);
        btnBacktoMain = findViewById(R.id.btnBackToMain); // שימי לב: בקובץ XML ייתכן שאין כפתור כזה, כדאי לוודא שהוא אכן שם!

        /// set the click listener
        // הגדרת מאזין ללחיצות. ה-this אומר שהמחלקה הנוכחית היא זו שתטפל בלחיצות (בפונקציה onClick למטה)
        btnRegister.setOnClickListener(this);
        tvLogin.setOnClickListener(this);

        // אתחול האובייקט שמאפשר לשמור נתונים בזיכרון המקומי של המכשיר
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    // הפונקציה שמתופעלת אוטומטית ברגע שמתבצעת לחיצה על רכיב כלשהו במסך
    @Override
    public void onClick(View v) {
        if (v.getId() == btnRegister.getId()) { // אם לחצו על כפתור "הרשם"

            // שליפת כל הטקסטים שהמשתמש הקליד, והורדת רווחים מיותרים בהתחלה ובסוף בעזרת ()trim.
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String fName = etFName.getText().toString().trim();
            String lName = etLName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            // --- תוספת הבדיקות לפני שנרשמים ל-Firebase ---

            // 1. בדיקה ששום שדה לא ריק (בדיקת חובה לכל השדות)
            if (email.isEmpty() || password.isEmpty() || fName.isEmpty() || lName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "נא למלא את כל הפרטים", Toast.LENGTH_SHORT).show(); // הצגת שגיאה מוקפצת
                return; // עוצר את הפונקציה כאן ולא ממשיך להרשמה, עד שהמשתמש יתקן
            }

            // 2. פיירבייס דורש סיסמה של 6 תווים לפחות, נבדוק את זה כדי שלא יקרוס בעת הפנייה לשרת
            if (password.length() < 6) {
                etPassword.setError("סיסמה חייבת להכיל לפחות 6 תווים"); // הודעת שגיאה אדומה ליד השדה
                etPassword.requestFocus(); // מיקוד (הקפצת סמן המקלדת) לשדה הבעייתי
                return; // עצירת הפעולה
            }

            // 3. (אופציונלי אבל מומלץ) בדיקה שהאימייל בפורמט תקין (עם @ ונקודה)
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("אימייל לא תקין");
                etEmail.requestFocus();
                return; // עצירת הפעולה
            }

            // שמירת האימייל והסיסמה במשתנים הכלליים של המחלקה (כדי שנוכל לשמור אותם בזיכרון המקומי אחר כך)
            this.email = email;
            this.password = password;

            // אם הכל תקין ועבר את הבדיקות - מעבירים את הנתונים לפונקציה שתבצע את ההרשמה מול Firebase
            registerUser(fName, lName, phone, email, password);

        } else if (v.getId() == tvLogin.getId()) { // אם המשתמש לחץ על "יש לך חשבון? התחבר"
            Intent registerIntent = new Intent(SignupActivity.this, LoginActivity.class); // יצירת כוונה למעבר
            startActivity(registerIntent); // מעבר למסך ההתחברות

        } else if (v.getId() == btnBacktoMain.getId()) { // אם המשתמש לחץ על "חזור למסך הראשי"
            Intent intent = new Intent(SignupActivity.this, MainActivity.class); // יצירת מעבר חזרה למסך הראשון
            startActivity(intent); // מעבר למסך
            finish(); // סגירת המסך הנוכחי לחלוטין
        }
    }

    /// Register the user
    // פונקציית ביניים שמכינה את הנתונים להרשמה במסד הנתונים
    private void registerUser(String fname, String lname, String phone, String email, String password) {
        Log.d(TAG, "registerUser: Registering user..."); // הדפסת הודעת בקרה ללוגים שההרשמה החלה

        String uid; // משתנה לשמירת ה-ID העתידי של המשתמש

        /// create a new user object
        // יצירת אובייקט מסוג 'משתמש' עם הנתונים שהתקבלו.
        // הערה: כרגע המזהה קבוע ל-"jkjk", השירות של פיירבייס יחליף אותו במזהה אמיתי (UID) בזמן ההרשמה.
        // הפרמטר האחרון `false` מציין שזה לקוח רגיל ולא מנהל (Admin).
        User user = new User("jkjk", fname, lname, email, phone, password, false);

        /// proceed to create the user
        createUserInDatabase(user); // קריאה לפונקציה ששולחת את האובייקט למסד הנתונים
    }

    // הפונקציה שפונה בפועל לשירות מסד הנתונים ומבצעת את שמירת המשתמש בענן
    private void createUserInDatabase(User user) {

        // קריאה לפונקציית ההרשמה ב-DatabaseService, שיוצרת יוזר חדש במערכת האימות (Authentication) ואז שומרת ב-Database
        dataBaseService.createNewUser(user, new DataBaseService.DatabaseCallback<String>() {

            @Override
            public void onCompleted(String uid) { // בלוק הקוד שירוץ אם המשתמש נוצר בהצלחה
                Log.d(TAG, "createUserInDatabase: User created successfully"); // הדפסה למפתח על הצלחה

                // פתיחת עורך לזיכרון המקומי כדי לשמור את פרטי המשתמש לפעם הבאה
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // שמירת האימייל והסיסמה (כדי שיהיו כבר כתובים כשיגיע למסך התחברות או כשיחזור לאפליקציה)
                editor.putString("email", email);
                editor.putString("password", password);

                editor.commit(); // שמירה סופית של הנתונים בטלפון

                // אחרי שהרשמנו את המשתמש בהצלחה, מעבירים אותו למסך הראשי
                Intent mainIntent = new Intent(SignupActivity.this, MainActivity.class);

                // מנקים את היסטוריית המסכים (כדי שאם ילחץ אחורה לא יחזור למסך ההרשמה)
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent); // מעבר למסך הראשי
            }

            @Override
            public void onFailed(Exception e) { // בלוק שירוץ במקרה של שגיאה בהרשמה מול הענן
                Log.e(TAG, "createUserInDatabase: Failed to create user", e); // הדפסת השגיאה המלאה ללוג
                // הקפצת הודעה למשתמש שההרשמה נכשלה
                Toast.makeText(SignupActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
            }
        });
    }
}