package com.example.iddoivanfinalproject; // הגדרת חבילת הפרויקט

// ייבוא מחלקות הדרושות לעבודת הקוד
import android.app.AlertDialog; // מחלקה ליצירת חלונות קופצים (דיאלוגים) לאישור פעולות
import android.content.Intent; // מחלקה לביצוע מעברים בין מסכים
import android.os.Bundle; // מחלקה לשמירת מצב המסך
import android.view.View;
import android.widget.ArrayAdapter; // מתווך (Adapter) בסיסי לחיבור רשימת טקסטים לתצוגה
import android.widget.Button; // מחלקת כפתור
import android.widget.ListView; // רכיב תצוגה שמציג רשימה נגללת של פריטים
import android.widget.Toast; // מחלקה להצגת הודעות קופצות קצרות בתחתית המסך

import androidx.annotation.Nullable; // מחלקה שמציינת שמשתנה יכול להיות ריק (null) ללא קריסת המערכת
import androidx.appcompat.app.AppCompatActivity; // מחלקת האם הבסיסית של מסכי אנדרואיד

import com.example.iddoivanfinalproject.model.User; // ייבוא מחלקת המודל שמייצגת משתמש
import com.example.iddoivanfinalproject.services.DataBaseService; // ייבוא שירות מסד הנתונים של האפליקציה

import java.util.ArrayList; // מחלקה ליצירת רשימות גמישות (מערכים דינמיים)
import java.util.List; // ממשק (Interface) של רשימות כלליות ב-Java

public class Allusers extends BaseActivity {
    ListView lvUsers; // משתנה לרכיב התצוגה של הרשימה (כפי שהוא מופיע ב-XML)
    ArrayList<String> userDisplayList; // רשימה שתשמור את הטקסטים שיוצגו בפועל במסך (שם, אימייל וכו')
    ArrayAdapter<String> adapter; // המתווך שייקח את הטקסטים מ-userDisplayList וישים אותם בתוך ה-ListView
    Button btnBack; // משתנה לייצוג כפתור החזרה במסך
    ArrayList<User> usersList; // רשימה שתשמור את אובייקטי המשתמשים האמיתיים (לא רק טקסט, אלא את כל המידע עליהם)\
    public void onMenuClick(View v) {
        openDrawer(); // קורא לפונקציה שכתבנו ב-BaseActivity
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) { // הפונקציה המרכזית שמופעלת ברגע שהמסך נוצר
        super.onCreate(savedInstanceState); // קריאה לפונקציית האם
        setContentView(R.layout.activity_allusers); // חיבור המסך לקובץ העיצוב שלו (XML)

        usersList = new ArrayList<>(); // אתחול הרשימה שתשמור את אובייקטי המשתמשים

        lvUsers = findViewById(R.id.lvUsers); // קישור המשתנה אל ה-ID של רכיב הרשימה ב-XML
        userDisplayList = new ArrayList<>(); // אתחול הרשימה שתשמור את המחרוזות (הטקסטים שיוצגו)

        // יצירת המתווך: הוא לוקח את הרקע והעיצוב הפשוט של אנדרואיד (simple_list_item_1) ומחבר אליו את רשימת הטקסטים
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userDisplayList);
        lvUsers.setAdapter(adapter); // חיבור המתווך בפועל לרכיב הרשימה במסך

        // --- לחיצה רגילה (קצרה): מעבר לעמוד פרטי משתמש ---
        lvUsers.setOnItemClickListener((parent, view, position, id) -> { // הגדרת מאזין ללחיצה על אחת מהשורות ברשימה
            User selectedUser = usersList.get(position); // שליפת המשתמש שעליו לחצנו בעזרת המיקום שלו (position) ברשימה

            Intent intent = new Intent(Allusers.this, Userdetails.class); // יצירת כוונה (Intent) לעבור למסך פרטי המשתמש

            // "אריזת" הנתונים של המשתמש שנבחר לתוך המעבר כדי שהמסך הבא יוכל לקרוא אותם
            intent.putExtra("userId", selectedUser.getId());
            intent.putExtra("fname", selectedUser.getFname());
            intent.putExtra("lname", selectedUser.getLname());
            intent.putExtra("email", selectedUser.getEmail());
            intent.putExtra("phoneNumber", selectedUser.getPhoneNumber());

            startActivity(intent); // הפעלת המעבר למסך פרטי המשתמש
        });

        // --- לחיצה ארוכה: מחיקת משתמש (התוספת החדשה) ---
        lvUsers.setOnItemLongClickListener((parent, view, position, id) -> { // מאזין מיוחד ללחיצה ארוכה על שורה ברשימה
            User selectedUser = usersList.get(position); // שליפת המשתמש שעליו ביצעו לחיצה ארוכה

            // יצירת חלונית קופצת (Dialog) לאישור המחיקה, כדי למנוע מחיקה בטעות
            new AlertDialog.Builder(Allusers.this)
                    .setTitle("מחיקת משתמש") // כותרת החלונית
                    .setMessage("האם אתה בטוח שברצונך למחוק את המשתמש " + selectedUser.getFname() + " " + selectedUser.getLname() + "?") // הודעת החלונית שמציגה את שם המשתמש
                    .setPositiveButton("כן, מחק", (dialog, which) -> { // הגדרת כפתור "אישור" והפעולה שתתבצע אם ילחצו עליו

                        // קריאה לפונקציית המחיקה בשירות מסד הנתונים (Firebase) תוך העברת ה-ID של המשתמש
                        DataBaseService.DatabaseService.getInstance().deleteUser(selectedUser.getId(), new DataBaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void object) { // אם המחיקה במסד הנתונים הצליחה
                                Toast.makeText(Allusers.this, "המשתמש נמחק בהצלחה!", Toast.LENGTH_SHORT).show(); // הודעת הצלחה למשתמש
                                loadUsersFromDatabase(); // קריאה לפונקציה שמרעננת את הרשימה במסך (כדי שהמשתמש ייעלם מיד)
                            }

                            @Override
                            public void onFailed(Exception e) { // אם המחיקה נכשלה
                                Toast.makeText(Allusers.this, "שגיאה במחיקה: " + e.getMessage(), Toast.LENGTH_LONG).show(); // הצגת סיבת השגיאה
                            }
                        });
                    })
                    .setNegativeButton("ביטול", null) // כפתור "ביטול" שפשוט סוגר את החלונית בלי לעשות כלום (null)
                    .show(); // הפקודה שמציגה את החלונית על המסך בפועל

            return true; // מחזיר true כדי לסמן למערכת ש"טיפלנו" בלחיצה הארוכה, כך שהיא לא תפעיל גם את הלחיצה הרגילה (קצרה) בטעות
        });

        // הוסף את זה בתוך onCreate (הערה מהקוד המקורי)
        btnBack = findViewById(R.id.btnUniversalBack); // קשירת כפתור החזרה אחורה ל-XML
        if (btnBack != null) { // בדיקה שהכפתור אכן קיים
            btnBack.setOnClickListener(v -> finish()); // הגדרת לחיצה: סוגרת את המסך וחוזרת למסך הקודם
        }
    }

    // --- הפונקציה הזו מבטיחה שהרשימה תתרענן כל פעם שתחזור למסך הזה ---
    @Override
    protected void onResume() { // פונקציית מערכת שמופעלת בכל פעם שהמסך חוזר להיות פעיל (למשל אם חזרת אליו ממסך אחר)
        super.onResume();
        loadUsersFromDatabase(); // קריאה מחדש לטעינת משתמשים כדי להציג את המידע הכי עדכני
    }

    private void loadUsersFromDatabase() { // פונקציית עזר המושכת את רשימת המשתמשים מהמסד
        // פניה לשירות מסד הנתונים כדי לקבל את כל רשימת המשתמשים
        DataBaseService.DatabaseService.getInstance().getUserList(new DataBaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) { // אם הקריאה למסד הנתונים הצליחה
                userDisplayList.clear(); // מחיקת רשימת הטקסטים הישנה (כדי למנוע כפילויות ברענון)
                usersList.clear(); // מחיקת רשימת האובייקטים הישנה

                for (User user : users) { // לולאה שעוברת על כל המשתמשים שהתקבלו ממסד הנתונים
                    // --- כאן הסינון: מוסיפים רק אם המשתמש הוא לא אדמין (כדי שהמנהל לא יראה את עצמו או מנהלים אחרים ברשימה) ---
                    if (!user.isAdmin()) { // בדיקה האם המשתמש *אינו* מנהל
                        usersList.add(user); // הוספת המשתמש לרשימת האובייקטים

                        // בניית מחרוזת הטקסט שתייצג את המשתמש במסך (שם, אימייל וטלפון - עם ירידת שורה \n)
                        String display = user.getFname() + " " + user.getLname() + "\n" +
                                "Email: " + user.getEmail() + "\n" +
                                // בודק אם יש מספר טלפון, אם לא מציג "אין מספר"
                                "Phone: " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "אין מספר");

                        userDisplayList.add(display); // הוספת המחרוזת המוכנה לרשימת התצוגה
                    }
                }

                adapter.notifyDataSetChanged(); // פקודה למתווך: "הנתונים השתנו, רענן את התצוגה של הרשימה במסך!"
            }

            @Override
            public void onFailed(Exception e) { // אם הייתה שגיאה במשיכת הנתונים ממסד הנתונים
                Toast.makeText(Allusers.this, "שגיאה בטעינת המשתמשים", Toast.LENGTH_SHORT).show(); // הודעת שגיאה למשתמש
            }
        });
    }
}