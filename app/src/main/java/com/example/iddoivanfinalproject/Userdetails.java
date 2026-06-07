package com.example.iddoivanfinalproject; // הגדרת החבילה ומיקום הקובץ בפרויקט

// ייבוא המחלקות הדרושות מהספריות של אנדרואיד
import android.content.Intent; // מחלקה למעבר בין מסכים והעברת נתונים
import android.content.SharedPreferences; // מחלקה לשמירת נתונים בזיכרון המקומי של המכשיר
import android.os.Bundle; // מחלקה לשמירת מצב המסך בזמן יצירתו
import android.view.View;
import android.widget.Button; // רכיב כפתור
import android.widget.TextView; // רכיב תצוגת טקסט

import androidx.annotation.Nullable; // מחלקה המציינת שערך יכול להיות ריק (null) ללא שגיאה
import androidx.appcompat.app.AppCompatActivity; // מחלקת האם הבסיסית למסכי אנדרואיד

import com.google.firebase.auth.FirebaseAuth; // ייבוא שירות האימות של Firebase

    public class Userdetails extends BaseActivity {
    // הגדרת משתנים פרטיים לרכיבי התצוגה במסך
    TextView tvDetails; // שדה טקסט גדול שיציג את כל פרטי המשתמש
    Button btnUpdate; // כפתור "עדכן פרטים"
        public void onMenuClick(View v) {
            openDrawer(); // קורא לפונקציה שכתבנו ב-BaseActivity
        }

    SharedPreferences sp; // משתנה לגישה לזיכרון המקומי (כדי לשמור נתונים זמניים)

    // הוספנו את userId! (הערה מהקוד שלך)
    // משתנים לשמירת נתוני המשתמש שמוצג כרגע
    String userId, fname, lname, email, phoneNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) { // הפונקציה המרכזית שמופעלת כשהמסך נוצר
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdetails); // חיבור קוד ה-Java לעיצוב שבקובץ ה-XML

        // קישור המשתנים אל הרכיבים בקובץ ה-XML באמצעות ה-ID שלהם
        tvDetails = findViewById(R.id.tvUserDetails);
        btnUpdate = findViewById(R.id.btnUpdate);

        // יצירה או פתיחה של קובץ זיכרון מקומי בשם "user_data" (במצב פרטי לאפליקציה בלבד)
        sp = getSharedPreferences("user_data", MODE_PRIVATE);

        // קבלת האובייקט (Intent) שהפעיל את המסך הזה (למשל, כשעברנו ממסך רשימת המשתמשים)
        Intent intent = getIntent();

        // בדיקה אם קיבלנו נתונים ממסך קודם (מוודא שה-Intent לא ריק ושיש בתוכו מידע על השם הפרטי)
        if (intent != null && intent.hasExtra("fname")) {
            // שולפים את כל הנתונים שהועברו אלינו ושומרים במשתנים של המחלקה
            userId = intent.getStringExtra("userId"); // <--- תופסים את ה-ID
            fname = intent.getStringExtra("fname");
            lname = intent.getStringExtra("lname");
            email = intent.getStringExtra("email");
            phoneNumber = intent.getStringExtra("phoneNumber");

            // פותחים עורך לזיכרון המקומי כדי לשמור את הנתונים החדשים שקיבלנו (גיבוי)
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("userId", userId); // <--- שומרים אותו בזיכרון המקומי
            editor.putString("fname", fname);
            editor.putString("lname", lname);
            editor.putString("email", email);
            editor.putString("phoneNumber", phoneNumber);
            editor.apply(); // שומר את השינויים ברקע
        } else {
            // תרחיש חלופי: אם לא קיבלנו נתונים מבחוץ (למשל סובבנו את המסך והוא נטען מחדש)
            // שולפים את הנתונים מהזיכרון המקומי.
            // אם ה-ID לא קיים בזיכרון, לוקחים כברירת מחדל את ה-ID של המשתמש המחובר כרגע מ-Firebase.
            userId = sp.getString("userId", FirebaseAuth.getInstance().getUid());

            // שולפים את שאר הפרטים. אם הם לא קיימים, נציג ערך ברירת מחדל כמו "לא ידוע".
            fname = sp.getString("fname", "לא ידוע");
            lname = sp.getString("lname", "לא ידוע");
            email = sp.getString("email", "לא ידוע");
            phoneNumber = sp.getString("phoneNumber", "לא הוזן מספר");
        }

        updateText(); // קריאה לפונקציית עזר שלוקחת את כל המשתנים ומדפיסה אותם למסך

        // הגדרת פעולה ללחיצה על כפתור "עדכון"
        btnUpdate.setOnClickListener(v -> {
            // יצירת מעבר ממסך פרטי משתמש למסך עריכת הפרטים (UpdateUserDetails)
            Intent i = new Intent(Userdetails.this, UpdateUserDetails.class);

            // "אורזים" את הנתונים הנוכחיים ומעבירים אותם למסך העריכה כדי שהמשתמש לא יצטרך להקליד הכל מאפס
            i.putExtra("userId", userId); // <--- השורה הכי חשובה! מעבירים לעריכה את המזהה כדי שנדע את מי לעדכן
            i.putExtra("fname", fname);
            i.putExtra("lname", lname);
            i.putExtra("email", email);
            i.putExtra("phoneNumber", phoneNumber);

            // מפעילים את מסך העריכה בצורה מיוחדת: "לך למסך הבא, אבל אני מחכה שתחזור עם תוצאה".
            // המספר '1' הוא פשוט קוד זיהוי פנימי (Request Code) כדי שנדע מאיזו בקשה חזרנו.
            startActivityForResult(i, 1);
        });
    }

    // פונקציה מיוחדת המופעלת אוטומטית כשאנחנו *חוזרים* ממסך שהופעל בעזרת startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // קריאה לפונקציית האם

        // בודקים 3 דברים: חזרנו מהבקשה שלנו (1)? הפעולה הצליחה (RESULT_OK)? ויש נתונים שחזרו (data)?
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            // שולפים את הנתונים המעודכנים שהמסך הקודם (העריכה) החזיר לנו ושומרים במשתנים
            fname = data.getStringExtra("fname");
            lname = data.getStringExtra("lname");
            email = data.getStringExtra("email");
            phoneNumber = data.getStringExtra("phoneNumber");

            // שומרים גם את הנתונים המעודכנים בזיכרון המקומי (כדי שלא יאבדו)
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("fname", fname);
            editor.putString("lname", lname);
            editor.putString("email", email);
            editor.putString("phoneNumber", phoneNumber);
            editor.apply();

            updateText(); // מעדכן את הטקסט במסך מיד עם הנתונים החדשים שחזרו!
        }
    }

    // פונקציית עזר שמסדרת את כל הנתונים כמחרוזת אחת ארוכה ומציגה אותה במסך
    private void updateText() {
        // מציב את הטקסט בתוך הרכיב tvDetails.
        // הסימן "\n" מסמל "ירידת שורה" (Enter), כך שכל פריט יופיע בשורה נפרדת.
        tvDetails.setText(
                "שם פרטי: " + fname + "\n" +
                        "שם משפחה: " + lname + "\n" +
                        "אימייל: " + email + "\n" +
                        "מספר טלפון: " + phoneNumber
        );
    }
}