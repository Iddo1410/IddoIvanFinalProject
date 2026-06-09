package com.example.iddoivanfinalproject; // הגדרת המיקום של הקובץ בתוך תיקיות הפרויקט

// ייבוא כל המחלקות והספריות הנדרשות מתוך אנדרואיד ומתוך הפרויקט שלך
import android.content.Intent; // מחלקה המשמשת למעבר בין מסכים
import android.os.Bundle; // מחלקה לשמירת הנתונים של מצב המסך בזמן פתיחה
import android.util.Log; // מחלקה להדפסת הודעות דיבאג (לוגים) למפתח למטה במסוף
import android.view.View; // מחלקת הבסיס לכל הרכיבים שמוצגים על המסך
import android.widget.AdapterView; // מחלקה שעוזרת לזהות בחירה מתוך רשימות
import android.widget.ArrayAdapter; // מתווך שלוקח מערך (כמו מערך טקסטים) ומכין אותו לתצוגה ברשימה
import android.widget.Button; // רכיב של כפתור
import android.widget.Spinner; // רכיב של תפריט בחירה נפתח (כמו סינון קטגוריות)

import androidx.annotation.Nullable; // תגית שמציינת שמשתנה יכול להיות שווה ל-null (ריק) בלי לקרוס
import androidx.appcompat.app.AppCompatActivity; // מחלקת האם שממנה יורש המסך
import androidx.recyclerview.widget.LinearLayoutManager; // מסדר את הפריטים ברשימה אחד מתחת לשני
import androidx.recyclerview.widget.RecyclerView; // רכיב מתקדם להצגת רשימות ארוכות נגללות ביעילות

import com.example.iddoivanfinalproject.adapter.ItemAdapter; // המתווך שיצרת כדי להציג את המוצרים ברשימה
import com.example.iddoivanfinalproject.model.Item; // מודל הנתונים המייצג מוצר בודד בחנות
import com.example.iddoivanfinalproject.model.User; // מודל הנתונים המייצג משתמש במערכת
import com.example.iddoivanfinalproject.services.DataBaseService; // שירות הגישה למסד הנתונים בענן (Firebase)
import com.google.firebase.auth.FirebaseAuth; // מערכת אימות המשתמשים של פיירבייס
import com.google.firebase.auth.FirebaseUser; // אובייקט המייצג את המשתמש שמחובר כעת

import java.util.ArrayList; // מחלקה של מערך דינמי (רשימה שיכולה לגדול או לקטון)
import java.util.List; // ממשק לעבודה עם רשימות

public class Items extends AppCompatActivity { // הגדרת המחלקה של מסך החנות (רשימת המוצרים)
    // משתני רכיבי התצוגה במסך
    private RecyclerView recyclerView; // משתנה לרכיב הרשימה הנגללת
    private ItemAdapter adapter; // משתנה למתווך שיצייר את המוצרים בתוך הרשימה
    private DataBaseService.DatabaseService databaseService; // משתנה שיאפשר לדבר עם מסד הנתונים
    private Button btnGoToCart, btnBack; // כפתור למעבר לעגלה וכפתור חזרה אחורה
    private Spinner spTypeFilter; // התפריט הנפתח לסינון מוצרים לפי קטגוריה

    // משתני רשימות הנתונים
    private ArrayList<Item> allItemsList = new ArrayList<>(); // רשימה שתשמור את *כל* המוצרים בחנות
    private ArrayList<Item> filteredList = new ArrayList<>(); // רשימה שתשמור *רק* את המוצרים שמתאימים לסינון הנוכחי (היא זו שתוצג במסך)

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) { // הפונקציה המרכזית שמופעלת ברגע שהמסך נפתח
        super.onCreate(savedInstanceState); // קריאה להכנה הבסיסית של אנדרואיד
        setContentView(R.layout.activity_items); // חיבור קובץ העיצוב (XML) של מסך החנות לקוד הזה

        // 1. אתחול וחיפוש הרכיבים מתוך קובץ העיצוב לפי ה-ID שלהם
        recyclerView = findViewById(R.id.rvItems); // מציאת הרשימה
        btnGoToCart = findViewById(R.id.btnGoToCart); // מציאת כפתור המעבר לעגלה
        spTypeFilter = findViewById(R.id.spTypeFilter); // מציאת הספינר
        btnBack = findViewById(R.id.btnUniversalBack); // מציאת כפתור החזרה

        if (btnBack != null) { // מוודא שכפתור החזרה קיים כדי למנוע קריסה
            btnBack.setOnClickListener(v -> finish()); // קובע שלחיצה עליו פשוט תסגור את מסך החנות
        }

        // הגדרת צורת התצוגה של הרשימה (מלמעלה למטה ברצף)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseService = DataBaseService.DatabaseService.getInstance(); // קבלת חיבור למסד הנתונים

        // 2. יצירת מתווך הרשימה
        adapter = new ItemAdapter(filteredList); // יוצר מתווך חדש ושולח לו את הרשימה המסוננת (שתהיה ריקה בהתחלה)
        recyclerView.setAdapter(adapter); // מחבר את המתווך לרכיב הרשימה במסך כדי שיציג את הנתונים

        setupSpinner(); // הפעלת פונקציה שמגדירה את הנתונים בתוך תפריט הסינון (ספינר)

        // 3. הגדרות כפתור המעבר לעגלת הקניות
        if (btnGoToCart != null) {
            btnGoToCart.setVisibility(View.GONE); // כברירת מחדל, מעלים (מסתיר) את הכפתור לגמרי מהמסך!
            btnGoToCart.setOnClickListener(v -> { // מגדיר את פעולת הלחיצה על הכפתור
                Intent intent = new Intent(Items.this, CartActivity.class); // יוצר כוונה (Intent) לעבור למסך העגלה
                startActivity(intent); // משגר את המעבר ומציג את העגלה
            });
        }

        // 4. בודק איזה סוג משתמש מחובר (מנהל או לקוח) כדי לדעת אם להציג לו את כפתור העגלה
        checkUserStatus();
    }

    @Override
    protected void onResume() { // פונקציית מערכת שמופעלת בכל פעם שחוזרים למסך הזה (למשל אחרי שיצאנו מהעגלה)
        super.onResume();
        loadItemsFromDatabase(); // מושך מחדש את המוצרים מהענן כדי שאם מנהל מחק משהו, זה יתעדכן מיד
    }

    private void checkUserStatus() { // פונקציה שבודקת האם המשתמש הוא מנהל (Admin)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // בודק מי מחובר כרגע לאפליקציה

        if (currentUser != null) { // אם אכן יש משתמש מחובר
            String uid = currentUser.getUid(); // שולף את המזהה (תעודת הזהות) הייחודי שלו

            databaseService.getUser(uid, new DataBaseService.DatabaseCallback<User>() { // שולף את פרטי המשתמש המלאים ממסד הנתונים
                @Override
                public void onCompleted(User user) { // כשהפרטים הגיעו מהאינטרנט
                    if (user != null) { // אם המשתמש נמצא
                        if (user.isAdmin()) { // בודק האם השדה IsAdmin שלו מוגדר כ'אמת'
                            if (btnGoToCart != null) btnGoToCart.setVisibility(View.GONE); // אם זה מנהל, משאיר את כפתור העגלה מוסתר
                        } else {
                            if (btnGoToCart != null) btnGoToCart.setVisibility(View.VISIBLE); // אם זה לקוח רגיל, חושף (מציג) את כפתור העגלה
                        }
                    }
                }

                @Override
                public void onFailed(Exception e) { // אם התקשורת נכשלה
                    if (btnGoToCart != null) btnGoToCart.setVisibility(View.GONE); // מסתיר את הכפתור ליתר ביטחון
                }
            });
        } else { // אם אין משתמש מחובר (אורח)
            if (btnGoToCart != null) btnGoToCart.setVisibility(View.GONE); // מסתיר את הכפתור
        }
    }

    private void setupSpinner() { // פונקציה שמגדירה את תפריט הסינון
        // יוצר מתווך (Adapter) מיוחד שמושך את רשימת הקטגוריות מתוך קובץ המשאבים arrs.xml
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.typeArr, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // מגדיר את העיצוב בזמן שהתפריט פתוח
        spTypeFilter.setAdapter(spinnerAdapter); // מחבר את המתווך לספינר במסך

        spTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // מאזין לשינויים בתפריט בחירה
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // כשנבחרת קטגוריה חדשה
                String selectedType = parent.getItemAtPosition(position).toString(); // קורא את השם של הקטגוריה שנבחרה
                filterItems(selectedType); // שולח אותה לפונקציה שמסננת את רשימת המוצרים
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { } // לא עושה כלום אם לא נבחר כלום
        });
    }

    private void loadItemsFromDatabase() { // פונקציה למשיכת כל המוצרים מהאינטרנט (Firebase)
        databaseService.getAllItems(new DataBaseService.DatabaseCallback<List<Item>>() { // מבקש מהמסד את כל החנות
            @Override
            public void onCompleted(List<Item> items) { // כשהחנות יורדת מהאינטרנט בהצלחה
                if (items != null) { // אם יש מוצרים
                    allItemsList.clear(); // מרוקן את רשימת המוצרים הכללית במכשיר (כדי למנוע כפילויות של רענונים קודמים)
                    allItemsList.addAll(items); // מכניס אליה את כל המוצרים החדשים שירדו מהרשת

                    if (spTypeFilter.getSelectedItem() != null) { // אם המשתמש כבר בחר קטגוריה מסוימת קודם לכן
                        filterItems(spTypeFilter.getSelectedItem().toString()); // מפעיל סינון לפי הקטגוריה הזו על הנתונים החדשים
                    } else { // אם זו הפעלה ראשונה ועדיין אין סינון
                        filteredList.clear(); // מרוקן את הרשימה שמוצגת במסך
                        filteredList.addAll(allItemsList); // דוחף אליה פשוט את כל המוצרים בחנות
                        adapter.notifyDataSetChanged(); // אומר למתווך (לרשימה הוויזואלית) לרענן את עצמו ולהציג את הנתונים החדשים
                    }
                }
            }

            @Override
            public void onFailed(Exception e) { // אם הייתה שגיאת תקשורת
                Log.e("ItemsPage", "Failed to load items", e); // מדפיס הודעה אדומה למפתח למטה ביומן השגיאות
            }
        });
    }

    private void filterItems(String type) { // פונקציה שעושה את עבודת הסינון בפועל (מקבלת את הקטגוריה המבוקשת)
        filteredList.clear(); // מנקה לחלוטין את הרשימה המוצגת עכשיו במסך כדי להכין מקום למוצרים החדשים שיסוננו

        // בודק אם המשתמש ביקש לראות את כל המוצרים (או שבחר "הכל" או שהקוד העביר בטעות מילה ריקה)
        if (type.equals("הכל") || type.equals("All Categories") || type.isEmpty()) {
            filteredList.addAll(allItemsList); // פשוט מכניס פנימה את כל הרשימה הכללית ללא סינון
        } else { // אם המשתמש בחר קטגוריה ספציפית (כמו "עכברים")
            for (Item item : allItemsList) { // עובר בלולאה על כל המוצרים בחנות אחד-אחד
                if (item.getType() != null && item.getType().equals(type)) { // בודק האם הסוג של המוצר זהה לקטגוריה שבחרנו
                    filteredList.add(item); // אם כן, הוא עבר את ה"סלקציה" ונוסיף אותו לרשימה שתוצג למשתמש
                }
            }
        }

        // אחרי שסיימנו לסנן ולהכניס לרשימה רק את מה שצריך - מודיעים למסך לרענן את התצוגה המעודכנת
        adapter.notifyDataSetChanged();
    }
}