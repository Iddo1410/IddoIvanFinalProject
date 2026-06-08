package com.example.iddoivanfinalproject; // הגדרת החבילה ומיקום הקובץ בפרויקט

// ייבוא מחלקות וספריות שדרושות להפעלת הקוד
import android.content.Intent; // מחלקה למעבר בין מסכים
import android.os.Bundle; // מחלקה לשמירת נתונים ומצב המסך
import android.view.View;
import android.widget.Button; // רכיב כפתור

import androidx.appcompat.app.AppCompatActivity; // מחלקת האם הבסיסית למסכים באנדרואיד

import com.google.firebase.auth.FirebaseAuth; // ייבוא שירות האימות של Firebase (כדי לבצע את הניתוק)

public class SignoutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה המרכזית שמופעלת ברגע שהמסך נוצר
        super.onCreate(savedInstanceState); // קריאה לפעולת ההקמה של מחלקת האם

        // חיבור קוד ה-Java לקובץ העיצוב (XML).
        // ⚠️ שימי לב: מוגדר כאן activity_main, כדאי לתקן ל-activity_signout אם קיים קובץ כזה!
        setContentView(R.layout.activity_main);

        // מציאת כפתור ההתנתקות מתוך קובץ ה-XML בעזרת ה-ID שלו
        Button btnLogout = findViewById(R.id.btnLogout);

        // הגדרת פעולה שתקרה ברגע שהמשתמש ילחץ על כפתור ההתנתקות
        btnLogout.setOnClickListener(v -> {

            // פקודה ל-Firebase לנתק את המשתמש הנוכחי מהמערכת
            FirebaseAuth.getInstance().signOut();

            // יצירת פעולת מעבר (Intent) ממסך ההתנתקות אל מסך ההתחברות (LoginActivity)
            Intent intent = new Intent(SignoutActivity.this, LoginActivity.class);

            // הגדרת דגלים (Flags) שמנקים לחלוטין את היסטוריית המסכים הפתוחים ברקע.
            // פעולה זו סופר-חשובה באבטחה: היא מבטיחה שאחרי שהמשתמש חזר למסך ההתחברות,
            // הוא לא יוכל ללחוץ על כפתור "אחורה" בטלפון שלו ולחזור בטעות למסכים הפנימיים של האפליקציה.
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // הפעלת המעבר בפועל למסך ההתחברות
            startActivity(intent);

            // סגירה סופית של מסך ההתנתקות הנוכחי (כדי לשחרר זיכרון ולסיים את התהליך)
            finish();
        });
    }
}