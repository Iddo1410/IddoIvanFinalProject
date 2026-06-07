package com.example.iddoivanfinalproject; // הגדרת החבילה (המיקום של הקובץ בפרויקט)

// ייבוא המחלקות הדרושות מהספריות של אנדרואיד ושל הפרויקט
import android.os.Bundle; // מחלקה לשמירת מצב המסך בזמן פתיחתו
import android.view.View; // מחלקת הבסיס לכל הרכיבים הוויזואליים במסך
import android.widget.Button; // רכיב כפתור
import android.widget.ProgressBar; // רכיב אנימציית טעינה (עיגול מסתובב בדרך כלל)
import android.widget.Toast; // מחלקה להצגת הודעות קופצות קצרות בתחתית המסך

import androidx.appcompat.app.AppCompatActivity; // מחלקת האם למסכים באנדרואיד
import androidx.recyclerview.widget.LinearLayoutManager; // מסדר את הפריטים ברשימה אחד מתחת לשני
import androidx.recyclerview.widget.RecyclerView; // רכיב רשימה מתקדם ויעיל (מתאים לרשימות ארוכות כמו הזמנות)

import com.example.iddoivanfinalproject.adapter.OrderAdapter; // המתווך שיצרת כדי לצייר כל הזמנה בתוך הרשימה
import com.example.iddoivanfinalproject.model.Order; // מודל הנתונים המייצג הזמנה בודדת
import com.example.iddoivanfinalproject.services.DataBaseService; // שירות הגישה למסד הנתונים (Firebase)

import java.util.List; // ממשק לרשימות דינמיות ב-Java

public class OrderHistory extends BaseActivity {
    // הגדרת משתנים פרטיים עבור רכיבי המסך
    private RecyclerView rvOrders; // הרכיב שיציג את רשימת ההזמנות במסך
    private OrderAdapter adapter; // המתווך (Adapter) שיחבר בין נתוני ההזמנות לתצוגה ברשימה
    Button btnBack; // כפתור חזרה אחורה
    private DataBaseService.DatabaseService databaseService; // משתנה לתקשורת מול מסד הנתונים
    private ProgressBar progressBar; // משתנה למד הטעינה (כדי להראות למשתמש שהנתונים נטענים)

    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה שמופעלת ברגע שהמסך נטען
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history); // חיבור קובץ העיצוב (XML) למסך

        initViews(); // קריאה לפונקציה שמקשרת את הרכיבים ל-XML
        loadOrders(); // קריאה לפונקציה שמושכת את כל ההזמנות ממסד הנתונים

        btnBack = findViewById(R.id.btnUniversalBack); // קשירת כפתור החזרה אחורה
        if (btnBack != null) { // מוודא שהכפתור אכן נמצא ב-XML
            btnBack.setOnClickListener(v -> finish()); // בלחיצה על הכפתור, המסך ייסגר (finish) ונחזור אחורה
        }
    }
    public void onMenuClick(View v) {
        openDrawer(); // קורא לפונקציה שכתבנו ב-BaseActivity
    }

    private void initViews() { // פונקציה לאתחול רכיבי התצוגה
        rvOrders = findViewById(R.id.rvOrders); // קישור משתנה הרשימה לרכיב ב-XML

        // הגדרת פריסת הרשימה: פריסה ליניארית (LinearLayoutManager) אומרת שההזמנות יוצגו זו תחת זו (כמו רשימה רגילה)
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        // הגדרת פס טעינה (אופציונלי - אם הוספת ל-XML)
        progressBar = findViewById(R.id.progressBar); // קישור מעגל הטעינה ל-XML

        databaseService = DataBaseService.DatabaseService.getInstance(); // קבלת המופע הפעיל של שירות מסד הנתונים
    }

    private void loadOrders() { // פונקציה לטעינת נתוני ההזמנות מהשרת
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE); // אם יש פס טעינה, מציגים אותו (כי התחלנו למשוך נתונים)

        // קריאה לשירות מסד הנתונים למשיכת *כל* ההזמנות שיש במערכת
        databaseService.getAllOrders(new DataBaseService.DatabaseCallback<List<Order>>() {
            @Override
            public void onCompleted(List<Order> orders) { // בלוק הקוד שירוץ ברגע שהנתונים הגיעו בהצלחה

                if (progressBar != null) progressBar.setVisibility(View.GONE); // מסתירים את פס הטעינה (כי סיימנו לטעון)

                if (orders != null && !orders.isEmpty()) { // בדיקה האם הרשימה שחזרה תקינה ולא ריקה
                    // חיבור האדפטר לרשימה שחזרה מהענן:
                    // מעבירים את רשימת ההזמנות למתווך (OrderAdapter) כדי שהוא יבנה מהם שורות ויזואליות
                    adapter = new OrderAdapter(orders);
                    rvOrders.setAdapter(adapter); // מחברים את המתווך המלא בנתונים לתוך הרשימה במסך
                } else {
                    // אם הרשימה ריקה, מציגים הודעה למנהל שאין עדיין הזמנות
                    Toast.makeText(OrderHistory.this, "אין היסטוריית רכישות להצגה", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed(Exception e) { // בלוק הקוד שירוץ אם הייתה שגיאה במשיכת הנתונים (למשל אין אינטרנט)
                if (progressBar != null) progressBar.setVisibility(View.GONE); // גם במקרה של שגיאה, חובה להסתיר את פס הטעינה
                // מציגים הודעת שגיאה המכילה את סיבת הכישלון
                Toast.makeText(OrderHistory.this, "שגיאה בטעינת הנתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}