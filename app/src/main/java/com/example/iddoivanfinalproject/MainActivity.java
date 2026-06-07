package com.example.iddoivanfinalproject; // הגדרת חבילת הפרויקט (מיקום הקובץ במערכת התיקיות)

import android.content.Intent; // ייבוא מחלקה האחראית על מעבר בין מסכים
import android.os.Bundle; // ייבוא מחלקה לשמירת מצב המסך בזמן פתיחתו
import android.view.View; // ייבוא מחלקת הבסיס לכל אלמנט ויזואלי במסך (כמו כפתורים וטקסטים)

import androidx.activity.EdgeToEdge; // מחלקה לאפשור תצוגת מסך "מקצה לקצה" (ללא פסים שחורים למעלה ולמטה)
import androidx.appcompat.app.AppCompatActivity; // מחלקת האם הבסיסית למסכים באנדרואיד
import androidx.core.graphics.Insets; // מחלקה לטיפול בשוליים הפנימיים של המערכת
import androidx.core.view.ViewCompat; // מחלקה לטיפול בתאימות תצוגה בין גרסאות אנדרואיד שונות
import androidx.core.view.WindowInsetsCompat; // מחלקה לטיפול באזורי המערכת (כמו אזור שורת הסטטוס והסוללה)

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה המרכזית שמופעלת אוטומטית ברגע שהמסך נוצר ונטען
        super.onCreate(savedInstanceState); // קריאה לפעולת ההקמה של מחלקת האם (חובה בכל מסך אנדרואיד)

        EdgeToEdge.enable(this); // מאפשר למסך להתפרס על כל שטח התצוגה של המכשיר (כולל מאחורי שורת הסטטוס למעלה)

        setContentView(R.layout.activity_main); // מחבר את קוד ה-Java הנוכחי לקובץ העיצוב (XML) של המסך הראשי

        // קטע הקוד הבא מונע מהתוכן של המסך (כפתורים וכו') להיות מוסתר מתחת לשורת הסטטוס (למעלה) או כפתורי הניווט (למטה)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // שולף את הגדלים (השוליים) של סרגלי המערכת של הטלפון
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // מגדיר ריווח (Padding) פנימי למסך הראשי לפי הגדלים שנשלפו, כדי לדחוף את התוכן פנימה למקום בטוח
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets; // מחזיר את הנתונים המעודכנים למערכת ההפעלה
        });


    }
    public void onMenuClick(View v) {
        openDrawer(); // קורא לפונקציה שכתבנו ב-BaseActivity
    }

    // פונקציה המופעלת בלחיצה על כפתור ההרשמה (מקושרת ישירות מקובץ ה-XML דרך תכונת android:onClick="onClick")
    public void onClick (View v){
        if(v.getId()==R.id.btnSignup){ // בודק האם ה-ID של הרכיב שנלחץ הוא אכן כפתור "הרשמה"
            // יצירת "כוונה" (Intent) למעבר מהמסך הנוכחי (MainActivity) למסך ההרשמה (SignupActivity)
            Intent intent= new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent); // ביצוע המעבר בפועל למסך ההרשמה
        }
    }

    // פונקציה המופעלת בלחיצה על כפתור ההתחברות (מקושרת מה-XML דרך android:onClick="onLogin")
    public void onLogin (View v){
        if(v.getId()==R.id.btnLogin){ // בודק האם ה-ID של הכפתור שנלחץ הוא כפתור "התחברות"
            // יצירת כוונה למעבר ממסך ראשי למסך התחברות
            Intent intent= new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent); // ביצוע המעבר בפועל למסך ההתחברות
        }
    }

    // פונקציה המופעלת בלחיצה על כפתור הפרטים/אודות (מקושרת מה-XML דרך android:onClick="onDetails")
    public void onDetails (View v){
        // בודק האם הכפתור שנלחץ הוא כפתור "פרטים" (הערה: שים לב לשגיאת הכתיב btnDeatils בקוד, אבל כל עוד זה מוגדר ככה גם ב-XML זה יעבוד מצוין!)
        if (v.getId()==R.id.btnDeatils){
            // יצירת כוונה למעבר מהמסך הראשי למסך ה"אודות" (About)
            Intent intent= new Intent(MainActivity.this, About.class);
            startActivity(intent); // ביצוע המעבר בפועל
        }
    }
}