package com.example.iddoivanfinalproject; // מיקום הקובץ בתוך תיקיות הפרויקט

// ייבוא מחלקות וספריות שדרושות להפעלת הקוד
import android.os.Bundle; // מחלקה לשמירת מצב המסך בזמן פתיחתו
import android.view.View; // מחלקת הבסיס לכל הרכיבים הוויזואליים במסך
import android.widget.Button; // רכיב כפתור
import android.widget.ProgressBar; // רכיב מד טעינה (עיגול מסתובב)
import android.widget.Toast; // מחלקה להצגת הודעות קופצות קצרות בתחתית המסך

import androidx.appcompat.app.AppCompatActivity; // מחלקת האם הבסיסית למסכי אנדרואיד
import androidx.recyclerview.widget.LinearLayoutManager; // מסדר את הרשימה בצורה אנכית (אחד מתחת לשני)
import androidx.recyclerview.widget.RecyclerView; // רכיב תצוגה מתקדם להצגת רשימות ארוכות ביעילות

import com.example.iddoivanfinalproject.adapter.OrderAdapter; // המתווך שיודע להפוך אובייקט "הזמנה" לשורה ויזואלית במסך
import com.example.iddoivanfinalproject.model.Order; // מודל הנתונים המייצג הזמנה
import com.example.iddoivanfinalproject.services.DataBaseService; // שירות התקשורת מול מסד הנתונים
import com.google.firebase.auth.FirebaseAuth; // שירות האימות של Firebase
import com.google.firebase.auth.FirebaseUser; // מחלקה המייצגת את המשתמש שמחובר כרגע לאפליקציה

import java.util.ArrayList; // מחלקה של מערך דינמי שניתן להוסיף אליו איברים
import java.util.List; // ממשק לרשימות כלליות ב-Java

public class UserOrderHistory extends AppCompatActivity { // הגדרת המחלקה של היסטוריית הרכישות למשתמש הרגיל

    // הגדרת משתנים פרטיים עבור הרכיבים במסך
    private RecyclerView rvOrders; // הרכיב שיציג את רשימת ההזמנות
    private OrderAdapter adapter; // המתווך שיחבר את הנתונים לרשימה
    Button btnBack; // כפתור לחזרה אחורה
    private DataBaseService.DatabaseService databaseService; // משתנה להתקשרות מול מסד הנתונים
    private ProgressBar progressBar; // משתנה למד הטעינה (כדי להראות שהאפליקציה חושבת)

    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה המרכזית שמופעלת ברגע שהמסך נטען
        super.onCreate(savedInstanceState);
        // שימי לב: הוא משתמש באותו קובץ עיצוב (XML) שבו השתמש מסך המנהל, כי העיצוב זהה
        setContentView(R.layout.activity_order_history);

        initViews(); // קריאה לפונקציה שמקשרת את הרכיבים לקובץ ה-XML
        loadOrders(); // קריאה לפונקציה ששולפת את ההזמנות ממסד הנתונים ומסננת אותן

        btnBack = findViewById(R.id.btnUniversalBack); // קשירת כפתור החזרה ל-ID שלו ב-XML
        if (btnBack != null) { // וידוא שהכפתור קיים
            btnBack.setOnClickListener(v -> finish()); // בלחיצה על "אחורה", סגור את המסך וחזור למסך הקודם
        }
    }

    private void initViews() { // פונקציה לאתחול רכיבי המסך
        rvOrders = findViewById(R.id.rvOrders); // קשירת רכיב הרשימה
        rvOrders.setLayoutManager(new LinearLayoutManager(this)); // הגדרת הפריסה של הרשימה כרשימה ליניארית (מלמעלה למטה)

        // הגדרת פס טעינה (אופציונלי - אם הוספת ל-XML)
        progressBar = findViewById(R.id.progressBar); // קשירת מד הטעינה

        databaseService = DataBaseService.DatabaseService.getInstance(); // קבלת מופע (Instance) של שירות מסד הנתונים
    }

    private void loadOrders() { // הפונקציה שטוענת ומסננת את ההזמנות
        // שלב 1: קבלת המשתמש המחובר מ-Firebase כדי לדעת של מי ההזמנות שאנחנו מחפשים
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) { // הגנה: אם מאיזושהי סיבה אין משתמש מחובר
            Toast.makeText(this, "שגיאה: משתמש לא מחובר", Toast.LENGTH_SHORT).show(); // הודעת שגיאה
            return; // עצירת הפונקציה (לא נמשוך נתונים אם אין למי)
        }

        String currentUserId = currentUser.getUid(); // שליפת המזהה הייחודי (ID) של המשתמש שלנו

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE); // הדלקת מד הטעינה בזמן שהנתונים יורדים מהרשת

        // שלב 2: קריאה לשירות מסד הנתונים למשיכת *כל* ההזמנות הקיימות במערכת (של כולם)
        databaseService.getAllOrders(new DataBaseService.DatabaseCallback<List<Order>>() {
            @Override
            public void onCompleted(List<Order> orders) { // בלוק הקוד שירוץ כשההזמנות יגיעו
                if (progressBar != null) progressBar.setVisibility(View.GONE); // סיום הטעינה - כיבוי מד הטעינה

                if (orders != null && !orders.isEmpty()) { // אם יש הזמנות במערכת
                    List<Order> userOrders = new ArrayList<>(); // יצירת רשימה חדשה וריקה שתכיל רק את ההזמנות של המשתמש הזה

                    // שלב 3: סינון הרשימה כך שיוצגו רק הזמנות של המשתמש הנוכחי
                    for (Order order : orders) { // לולאה שעוברת על כל ההזמנות שהגיעו מהשרת
                        // שים לב: ודא שבמחלקה Order יש לך מתודה getUserId() שמחזירה את ה-uid של המזמין (הערה שלך מהקוד)

                        // בודק האם המזהה של מי שביצע את ההזמנה, שווה למזהה של המשתמש המחובר כרגע
                        if (order.getUserId() != null && order.getUserId().equals(currentUserId)) {
                            userOrders.add(order); // אם יש התאמה, נוסיף את ההזמנה הזו לרשימה המסוננת שלנו!
                        }
                    }

                    // שלב 4: הצגת התוצאות למשתמש
                    if (!userOrders.isEmpty()) { // אם אחרי הסינון נמצאו הזמנות של המשתמש
                        // חיבור האדפטר (המתווך) לרשימה המסוננת (userOrders)
                        adapter = new OrderAdapter(userOrders);
                        rvOrders.setAdapter(adapter); // מציג את ההזמנות בפועל במסך
                    } else { // אם אחרי הסינון לא נמצאו הזמנות בכלל עבורו
                        // הודעה שמסבירה שהוא עוד לא קנה כלום
                        Toast.makeText(UserOrderHistory.this, "אין היסטוריית רכישות להצגה עבורך", Toast.LENGTH_SHORT).show();
                    }
                } else { // אם רשימת ההזמנות הכללית ב-Firebase ריקה לחלוטין (אף אחד לא קנה שום דבר מעולם)
                    Toast.makeText(UserOrderHistory.this, "אין היסטוריית רכישות כלל", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed(Exception e) { // אם הייתה שגיאת רשת / שגיאת מסד נתונים במשיכת המידע
                if (progressBar != null) progressBar.setVisibility(View.GONE); // כיבוי מד הטעינה גם במקרה של שגיאה
                Toast.makeText(UserOrderHistory.this, "שגיאה בטעינת הנתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // הצגת סיבת השגיאה
            }
        });
    }
}