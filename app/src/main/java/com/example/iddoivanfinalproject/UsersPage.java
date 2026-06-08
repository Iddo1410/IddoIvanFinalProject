package com.example.iddoivanfinalproject; // הגדרת מיקום הקובץ בתוך תיקיות הפרויקט

// ייבוא מחלקות וספריות הדרושות להפעלת הקוד
import android.content.Intent; // מחלקה לביצוע מעברים בין מסכים
import android.os.Bundle; // מחלקה לשמירת מצב המסך בזמן יצירתו
import android.view.View;
import android.widget.Button; // רכיב כפתור
import android.widget.TextView; // רכיב להצגת טקסט
import android.widget.Toast; // מחלקה להצגת הודעות קופצות קצרות (פופ-אפ) בתחתית המסך

import androidx.activity.EdgeToEdge; // מחלקה לתצוגת מסך מלאה "מקצה לקצה"
import androidx.appcompat.app.AppCompatActivity; // מחלקת האם הבסיסית של מסכי אנדרואיד
import androidx.core.graphics.Insets; // טיפול בשוליים הפנימיים של המערכת (כמו שורת הסטטוס)
import androidx.core.view.ViewCompat; // מחלקה לתאימות תצוגה בין גרסאות אנדרואיד
import androidx.core.view.WindowInsetsCompat; // מחלקה לטיפול באזורי המערכת של המסך

import com.example.iddoivanfinalproject.model.User; // מודל הנתונים של המשתמש שיצרת
import com.example.iddoivanfinalproject.services.DataBaseService; // שירות הגישה למסד הנתונים
import com.google.firebase.auth.FirebaseAuth; // מערכת האימות וההתחברות של Firebase

public class UsersPage extends AppCompatActivity {
    // הגדרת משתנים פרטיים עבור רכיבי התצוגה במסך
    private TextView tvHi; // שדה הטקסט שיציג את הברכה ("היי [שם]")
    private DataBaseService.DatabaseService dataBaseService; // משתנה לתקשורת מול מסד הנתונים בענן
    private Button btnShop, btnCompare, btnCart, btnUserOrderHistory; // כפתורי הניווט השונים של הלקוח

    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה המרכזית שמופעלת ברגע שהמסך נטען
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // הגדרת התצוגה כך שתתפרס על כל המסך (כולל מאחורי סרגלי המערכת)
        setContentView(R.layout.activity_users_page); // חיבור קוד ה-Java לקובץ העיצוב הויזואלי (XML)

        // קישור רכיבים: חיבור המשתנים ב-Java לרכיבים בקובץ ה-XML באמצעות ה-ID שלהם
        tvHi = findViewById(R.id.tvHiUser); // קישור טקסט הברכה
        btnShop = findViewById(R.id.btnShop); // קישור כפתור "חנות"
        btnCompare = findViewById(R.id.btnCompare); // קישור כפתור "השוואה"
        btnCart = findViewById(R.id.btnCart); // קישור כפתור "עגלת קניות"
        btnUserOrderHistory = findViewById(R.id.btnUserOrderHistory); // קישור כפתור "היסטוריית רכישות"

        // הוספת מרווחים (Padding) פנימיים כדי שהכפתורים והטקסט לא יוסתרו מתחת לשורת הסטטוס או הניווט של הטלפון
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dataBaseService = DataBaseService.DatabaseService.getInstance(); // קבלת מופע (חיבור פעיל) למסד הנתונים

        // קבלת מזהה המשתמש (USER_ID) שהועבר ממסך ההתחברות (LoginActivity)
        String userId = getIntent().getStringExtra("USER_ID");

        // טעינת שם המשתמש ממסד הנתונים
        if (userId != null) { // מוודא שבאמת קיבלנו מזהה תקין
            // מבקש משירות הנתונים את פרטי המשתמש לפי ה-ID שלו
            dataBaseService.getUser(userId, new DataBaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) { // כשהנתונים חזרו מהשרת בהצלחה
                    if (user != null)
                        tvHi.setText("היי " + user.getFname()); // מעדכן את הטקסט לברכה אישית עם השם הפרטי
                }
                @Override
                public void onFailed(Exception e) { // במקרה של שגיאה בשליפת הנתונים מהשרת
                    tvHi.setText("היי"); // מציג ברכה כללית
                }
            });
        }

        // --- הגדרת פעולות (מאזינים) ללחיצות על הכפתורים השונים ---

        // לחיצה על כפתור החנות
        if (btnShop != null) { // מוודא שהכפתור קיים
            btnShop.setOnClickListener(v -> {
                Intent intent = new Intent(UsersPage.this, Items.class); // יצירת מעבר למסך רשימת המוצרים (Items)
                startActivity(intent); // הפעלת המעבר
            });
        }

        // לחיצה על כפתור עגלת הקניות
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                Intent intent = new Intent(UsersPage.this, CartActivity.class); // יצירת מעבר למסך העגלה
                startActivity(intent);
            });
        }

        // לחיצה על כפתור היסטוריית רכישות של המשתמש
        if (btnUserOrderHistory != null) {
            btnUserOrderHistory.setOnClickListener(v -> {
                Intent intent = new Intent(UsersPage.this, UserOrderHistory.class); // יצירת מעבר להיסטוריית הרכישות האישית
                startActivity(intent);
            });
        }

        // לחיצה על כפתור השוואה - התיקון כאן
        if (btnCompare != null) {
            btnCompare.setOnClickListener(v -> {
                try { // שימוש בבלוק Try-Catch (נסה-ותפוס) כדי למנוע קריסה במקרה שיש שגיאה בפתיחת המסך
                    Intent intent = new Intent(UsersPage.this, CompareList.class); // יצירת מעבר למסך ההשוואה
                    startActivity(intent); // נסיון לפתוח את המסך
                } catch (Exception e) { // אם קרתה שגיאה כלשהי
                    Toast.makeText(this, "הדף לא נמצא", Toast.LENGTH_SHORT).show(); // הצגת הודעה קופצת ידידותית במקום קריסת האפליקציה
                }
            });
        }

        // --- כפתור התנתקות ---
        Button btnLogout = findViewById(R.id.btnLogoutUser); // קישור כפתור ההתנתקות ב-XML
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> { // בעת לחיצה על "התנתק"
                FirebaseAuth.getInstance().signOut(); // פקודה למערכת Firebase לנתק את המשתמש הנוכחי

                Intent intent = new Intent(UsersPage.this, LoginActivity.class); // יצירת מעבר למסך ההתחברות (לוגין)

                // דגלים אלו מאוד חשובים: הם מוחקים את כל היסטוריית המסכים.
                // כך הלקוח לא יוכל ללחוץ על "אחורה" בטלפון ולחזור בטעות לחשבון שלו לאחר שהתנתק.
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent); // הפעלת המעבר למסך ההתחברות
                finish(); // סגירת מסך הבית הנוכחי לחלוטין
            });
        }
    }
}