package com.example.iddoivanfinalproject; // הגדרת מיקום הקובץ בתוך תיקיות הפרויקט

// ייבוא המחלקות הדרושות מהספריות של אנדרואיד
import android.content.Intent; // מחלקה לטיפול במעבר בין מסכים והעברת נתונים ביניהם
import android.os.Bundle; // מחלקה לשמירת מצב המסך
import android.widget.Button; // רכיב כפתור
import android.widget.EditText; // רכיב שדה טקסט שניתן לעריכה (להקשת קלט)
import android.widget.Toast; // מחלקה להצגת הודעות קופצות קצרות בתחתית המסך

import androidx.appcompat.app.AppCompatActivity; // מחלקת האם הבסיסית למסכים באנדרואיד

import com.example.iddoivanfinalproject.services.DataBaseService; // ייבוא שירות הגישה למסד הנתונים שיצרת
import com.google.firebase.auth.FirebaseAuth; // ייבוא מערכת האימות של Firebase

public class UpdateUserDetails extends AppCompatActivity { // מחלקת מסך עדכון פרטי משתמש

    // הגדרת משתנים פרטיים עבור רכיבי התצוגה שבמסך
    EditText etFname, etLname, etEmail, etPhone; // שדות לעריכת שם פרטי, משפחה, אימייל וטלפון
    Button btnSave; // כפתור לשמירת השינויים

    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה המרכזית שמופעלת ברגע שהמסך נוצר
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_details); // חיבור קוד ה-Java לקובץ העיצוב הויזואלי (XML)

        // קישור המשתנים אל הרכיבים בקובץ ה-XML באמצעות ה-ID שלהם
        etFname = findViewById(R.id.etFname);
        etLname = findViewById(R.id.etLname);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);

        // קבלת נתונים ראשונית לתצוגה ממסך קודם
        Intent intent = getIntent(); // משיכת האובייקט (Intent) שהפעיל את המסך הזה

        // מציב בתוך שדות העריכה את הנתונים הנוכחיים של המשתמש (כדי שלא יצטרך להקליד הכל מחדש)
        etFname.setText(intent.getStringExtra("fname")); // מציב את השם הפרטי
        etLname.setText(intent.getStringExtra("lname")); // מציב את שם המשפחה
        etEmail.setText(intent.getStringExtra("email")); // מציב את האימייל
        etPhone.setText(intent.getStringExtra("phoneNumber")); // מציב את מספר הטלפון

        // הגדרת מאזין לחיצה על כפתור ה"שמירה"
        btnSave.setOnClickListener(v -> {

            // שליפת הטקסטים (המעודכנים) מתוך שדות העריכה והמרתם למחרוזות (String)
            String newFname = etFname.getText().toString();
            String newLname = etLname.getText().toString();
            String newEmail = etEmail.getText().toString();
            String newPhone = etPhone.getText().toString();

            // מציאת ה-ID של המשתמש (אם העברנו אותו מהמסך הקודם, ואם לא - מהמשתמש המחובר)
            String userId = getIntent().getStringExtra("userId"); // קודם מנסים לקבל את ה-ID מהמסך הקודם (שימושי למשל כשאדמין עורך מישהו אחר)

            // אם לא הועבר ID מהמסך הקודם ויש משתמש מחובר באפליקציה כרגע
            if (userId == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
                // לוקחים את ה-ID של המשתמש שכרגע מחובר למערכת
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }

            // הגנת ביטחון: אם עדיין לא מצאנו שום מזהה משתמש חוקי
            if (userId == null) {
                Toast.makeText(UpdateUserDetails.this, "שגיאה: לא נמצא מזהה משתמש", Toast.LENGTH_SHORT).show(); // הודעת שגיאה
                return; // עוצרים את הפעולה ולא ממשיכים לעדכון
            }

            // קריאה לפונקציה החדשה שלנו לעדכון השדות בפיירבייס!
            // מעבירים את ה-ID ואת כל השדות המעודכנים לשירות מסד הנתונים
            DataBaseService.DatabaseService.getInstance().updateUserFields(
                    userId, newFname, newLname, newEmail, newPhone, new DataBaseService.DatabaseCallback<Void>() {

                        @Override
                        public void onCompleted(Void object) { // אם העדכון ב-Firebase עבר בהצלחה
                            Toast.makeText(UpdateUserDetails.this, "הפרטים עודכנו בהצלחה!", Toast.LENGTH_SHORT).show(); // הודעת הצלחה למשתמש

                            // מחזירים את הנתונים החדשים למסך הקודם כדי שיתרענן מיד (בלי צורך לשלוף שוב מהרשת)
                            Intent resultIntent = new Intent(); // יצירת כוונה ריקה רק לצורך החזרת נתונים
                            // "אורזים" את הנתונים החדשים לתוך המעבר
                            resultIntent.putExtra("fname", newFname);
                            resultIntent.putExtra("lname", newLname);
                            resultIntent.putExtra("email", newEmail);
                            resultIntent.putExtra("phoneNumber", newPhone);

                            // מגדירים שהפעולה הסתיימה בהצלחה (RESULT_OK) ומצרפים את הנתונים (resultIntent)
                            setResult(RESULT_OK, resultIntent);
                            finish(); // סוגר את המסך הנוכחי וחוזר אחורה למסך הקודם שהפעיל אותו
                        }

                        @Override
                        public void onFailed(Exception e) { // אם העדכון נכשל (למשל בעיית רשת)
                            Toast.makeText(UpdateUserDetails.this, "העדכון נכשל: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // הצגת שגיאה למשתמש
                        }
                    });
        });
    }
}