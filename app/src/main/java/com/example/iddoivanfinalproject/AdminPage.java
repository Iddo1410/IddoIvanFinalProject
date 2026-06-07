package com.example.iddoivanfinalproject; // הגדרת החבילה (המיקום של הקובץ בתוך תיקיות הפרויקט)

// ייבוא מחלקות וספריות שדרושות להפעלת הקוד
import android.content.Intent; // ייבוא מחלקה למעבר בין מסכים
import android.os.Bundle; // ייבוא מחלקה לשמירה והעברה של מצב המסך
import android.view.View; // ייבוא מחלקה המייצגת רכיבי תצוגה במסך
import android.widget.Button; // ייבוא מחלקת רכיב הכפתור
import android.widget.TextView; // ייבוא מחלקת רכיב להצגת טקסט

import androidx.activity.EdgeToEdge; // ייבוא מחלקה לאפשור תצוגה על כל שטח המסך (מקצה לקצה)
import androidx.appcompat.app.AppCompatActivity; // מחלקת הבסיס למסכים באנדרואיד
import androidx.core.graphics.Insets; // מחלקה לטיפול בשוליים הפנימיים של המסך (כמו אזור שורת הסטטוס)
import androidx.core.view.ViewCompat; // מחלקת תאימות לטיפול בתצוגות בגרסאות אנדרואיד שונות
import androidx.core.view.WindowInsetsCompat; // מחלקה לטיפול בשוליים של חלונות מערכת ההפעלה

import com.example.iddoivanfinalproject.model.User; // ייבוא מודל המשתמש שיצרת
import com.example.iddoivanfinalproject.services.DataBaseService; // ייבוא שירות מסד הנתונים
import com.google.firebase.auth.FirebaseAuth; // ייבוא מערכת אימות המשתמשים של Firebase (להתנתקות)

public class AdminPage extends BaseActivity {
    private TextView textView5; // משתנה שייצג את שדה הטקסט של הברכה ("היי...") במסך
    private DataBaseService.DatabaseService dataBaseService; // משתנה לגישה לפעולות מול מסד הנתונים
    public void onMenuClick(View v) {
        openDrawer(); // קורא לפונקציה שכתבנו ב-BaseActivity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה המרכזית שמופעלת ברגע שהמסך נטען
        super.onCreate(savedInstanceState); // קריאה לפעולת ההקמה של מחלקת האם
        EdgeToEdge.enable(this); // מאפשר למסך להתפרס על כל גודל התצוגה של המכשיר (כולל אזור שורת הסטטוס העליונה)
        setContentView(R.layout.activity_admin_page); // קביעת קובץ העיצוב (XML) שיוצג למשתמש במסך זה

        // הוספת מרווחים (Padding) למסך כדי שהתוכן לא יוסתר מתחת לשורת הסטטוס (למעלה) או כפתורי הניווט (למטה)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()); // משיכת מידות שולי המערכת
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // החלת המרווחים על המסך המרכזי
            return insets;
        });

        textView5 = findViewById(R.id.tvHi); // חיבור משתנה הטקסט ל-ID שלו בקובץ ה-XML
        dataBaseService = DataBaseService.DatabaseService.getInstance(); // קבלת מופע של מסד הנתונים (מחלקה מסוג סינגלטון)

        // קבלת מזהה המשתמש (ID) שהועבר ממסך ההתחברות (LoginActivity) אל המסך הזה
        String userId = getIntent().getStringExtra("USER_ID");

        if (userId == null) { // בדיקה האם מזהה המשתמש ריק (למשל, אם לא עברנו דרך מסך התחברות תקין)
            textView5.setText("היי"); // אם אין מזהה, נציג ברכה כללית
        } else {
            // אם קיים מזהה, נבקש ממסד הנתונים להביא לנו את פרטי המשתמש הספציפי הזה
            dataBaseService.getUser(userId, new DataBaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) { // בלוק הקוד שרץ אם הצלחנו למצוא את המשתמש במסד הנתונים
                    textView5.setText("היי " + user.getFname()); // מעדכן את הטקסט לברכה אישית עם השם הפרטי של המנהל
                }

                @Override
                public void onFailed(Exception e) { // בלוק הקוד שרץ אם הייתה שגיאה במשיכת הנתונים
                    textView5.setText("היי"); // במקרה של שגיאה, מציג ברכה כללית
                }
            });
        }

        // --- כפתור התנתקות ---
        Button btnLogout = findViewById(R.id.btnLogout); // קישור כפתור ההתנתקות ל-XML
        if (btnLogout != null) { // מוודא שהכפתור אכן קיים במסך כדי למנוע קריסה
            btnLogout.setOnClickListener(v -> { // הגדרת פעולה שתקרה כשהמשתמש ילחץ על כפתור התנתקות
                FirebaseAuth.getInstance().signOut(); // מנתק את המשתמש ממערכת האימות של Firebase

                Intent intent = new Intent(AdminPage.this, LoginActivity.class); // יצירת כוונה לחזור למסך ההתחברות
                // דגלים אלו מנקים את היסטוריית המסכים, כך שאם המשתמש ילחץ "אחורה" הוא ייצא מהאפליקציה ולא יחזור לעמוד המנהל
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent); // הפעלת מסך ההתחברות מחדש
                finish(); // סגירת המסך הנוכחי לחלוטין
            });
        }

        // --- התוספת החדשה: כפתור היסטוריית רכישות ---
        // ודא שה-ID של הכפתור בקובץ activity_admin_page.xml הוא אכן btnHistory
        Button btnHistory = findViewById(R.id.btnHistory); // קישור כפתור היסטוריית רכישות ל-XML
        if (btnHistory != null) { // מוודא שהכפתור אכן קיים
            btnHistory.setOnClickListener(v -> { // הגדרת מאזין לחיצה
                Intent intent = new Intent(AdminPage.this, OrderHistory.class); // יוצר פעולת מעבר למסך היסטוריית הרכישות
                startActivity(intent); // עובר למסך
            });
        }
    }

    // פונקציה למעבר לחנות (מופעלת דרך תכונת android:onClick בקובץ ה-XML של העיצוב)
    public void onShop(View v) {
        if (v.getId() == R.id.btnShopAdmin) { // מוודא שהכפתור שנלחץ הוא כפתור "חנות מנהל"
            Intent intent = new Intent(AdminPage.this, Items.class); // יצירת מעבר לעמוד הפריטים/מוצרים
            startActivity(intent); // הפעלת המעבר
        }
    }

    // מעבר לעמוד המשתמשים (מופעל מה-XML על ידי android:onClick="onClick")
    public void onClick(View v) {
        if (v.getId() == R.id.btnUsers) { // מוודא שהכפתור שנלחץ הוא כפתור "משתמשים"
            Intent intent = new Intent(AdminPage.this, Allusers.class); // יצירת מעבר לעמוד ניהול כל המשתמשים
            startActivity(intent); // הפעלת המעבר
        }
    }

    // מעבר להוספת פריט (מופעל מה-XML על ידי android:onClick="onAdd")
    public void onAdd(View v) {
        if (v.getId() == R.id.btnAddItem) { // מוודא שהכפתור שנלחץ הוא כפתור "הוסף פריט"
            Intent intent = new Intent(AdminPage.this, Additemtostore.class); // יצירת מעבר לעמוד שבו מוסיפים פריטים (העמוד מהשאלה הקודמת שלך)
            startActivity(intent); // הפעלת המעבר
        }
    }

    // פונקציית גיבוי למעבר להיסטוריית רכישות
    // (למקרה שתגדיר בכפתור ב-XML את התכונה android:onClick="onHistory")
    public void onHistory(View v) {
        if (v.getId() == R.id.btnHistory) { // בודק אם ה-ID של הרכיב שנלחץ הוא כפתור ההיסטוריה
            Intent intent = new Intent(AdminPage.this, OrderHistory.class); // יצירת מעבר למסך היסטוריית הרכישות
            startActivity(intent); // הפעלת המעבר
        }
    }
}