package com.example.iddoivanfinalproject; // הגדרת מיקום הקובץ בחבילת הפרויקט

// ייבוא כל המחלקות הנדרשות לפעולת המסך
import android.content.Intent; // מחלקה למעבר בין מסכים
import android.os.Bundle; // מחלקה לשמירת מצב המסך
import android.util.Log; // מחלקה להדפסת הודעות דיבאג (לוגים) למפתח
import android.view.View; // מחלקת הבסיס לכל אלמנט עיצובי במסך
import android.widget.AdapterView; // מחלקה לטיפול בבחירה מתוך רשימות (כמו ספינר)
import android.widget.ArrayAdapter; // מתווך פשוט להצגת טקסטים בתוך רשימה
import android.widget.Button; // רכיב כפתור
import android.widget.Spinner; // רכיב רשימה נפתחת (ספינר) לסינון קטגוריות

import androidx.annotation.Nullable; // מחלקה המציינת שערך יכול להיות ריק (null) ללא שגיאה
import androidx.appcompat.app.AppCompatActivity; // מחלקת האם למסכים באנדרואיד
import androidx.recyclerview.widget.LinearLayoutManager; // מסדר את רכיבי הרשימה בצורה אנכית
import androidx.recyclerview.widget.RecyclerView; // רכיב מתקדם ויעיל להצגת רשימה נגללת של פריטים

import com.example.iddoivanfinalproject.adapter.ItemAdapter; // מתווך מותאם אישית שיצרת עבור הצגת שורות של מוצרים
import com.example.iddoivanfinalproject.model.Item; // מודל הנתונים של מוצר
import com.example.iddoivanfinalproject.model.User; // מודל הנתונים של משתמש
import com.example.iddoivanfinalproject.services.DataBaseService; // שירות הגישה למסד הנתונים
import com.google.firebase.auth.FirebaseAuth; // מערכת ההתחברות של Firebase
import com.google.firebase.auth.FirebaseUser; // מייצג את המשתמש שמחובר כרגע למערכת

import java.util.ArrayList; // מחלקת מערך דינמי (רשימה שגודלה יכול להשתנות)
import java.util.List; // ממשק רשימה ב-Java

public class Items extends AppCompatActivity { // הגדרת המחלקה של מסך "מוצרים"

    // משתני המסך
    private RecyclerView recyclerView; // רכיב התצוגה של רשימת המוצרים
    private ItemAdapter adapter; // המתווך שיחבר בין הנתונים למסך
    private DataBaseService.DatabaseService databaseService; // משתנה להתקשרות מול מסד הנתונים
    private Button btnGoToCart, btnBack; // כפתור מעבר לעגלה וכפתור חזרה
    private Spinner spTypeFilter; // תפריט נפתח לבחירת קטגוריית סינון

    // רשימות נתונים
    private ArrayList<Item> allItemsList = new ArrayList<>(); // רשימה שתשמור את *כל* המוצרים שקיימים בחנות
    private ArrayList<Item> filteredList = new ArrayList<>(); // רשימה שתשמור רק את המוצרים שמתאימים לסינון הנוכחי (והיא זו שמוצגת בפועל)

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) { // הפונקציה המרכזית שמופעלת עם יצירת המסך
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items); // חיבור לעיצוב של המסך (XML)

        // 1. אתחול רכיבים (קישור בין המשתנים ב-Java לרכיבים ב-XML בעזרת ה-ID שלהם)
        recyclerView = findViewById(R.id.rvItems);
        btnGoToCart = findViewById(R.id.btnGoToCart);
        spTypeFilter = findViewById(R.id.spTypeFilter);
        btnBack = findViewById(R.id.btnUniversalBack); // קשירת כפתור חזרה כללי

        if (btnBack != null) { // מוודא שכפתור החזרה קיים
            btnBack.setOnClickListener(v -> finish()); // הגדרת לחיצה: תסגור את המסך (חזרה אחורה)
        }

        // הגדרת סידור הרשימה - מציג את המוצרים אחד מתחת לשני (אנכי)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseService = DataBaseService.DatabaseService.getInstance(); // קבלת החיבור למסד הנתונים

        // 2. הגדרת האדפטר (RecyclerView)
        // תיקון (הערה מהקוד שלך): העברנו רק את הרשימה (filteredList). הלחיצה כבר מטופלת בתוך ה-ItemAdapter!
        adapter = new ItemAdapter(filteredList); // יצירת מתווך שמאזין לרשימה המסוננת
        recyclerView.setAdapter(adapter); // חיבור המתווך אל רכיב התצוגה במסך

        setupSpinner(); // הפעלת הפונקציה שמגדירה את תפריט הסינון

        // 3. הגדרת כפתור עגלה - מוסתר כברירת מחדל (GONE) כדי שמנהל לא יראה אותו בטעות לפני שבדקנו מי הוא
        if (btnGoToCart != null) {
            btnGoToCart.setVisibility(View.GONE); // הסתרה מלאה מהמסך
            btnGoToCart.setOnClickListener(v -> { // הגדרת מאזין ללחיצה על הכפתור
                Intent intent = new Intent(Items.this, CartActivity.class); // יצירת כוונה למעבר למסך העגלה
                startActivity(intent); // הפעלת המעבר למסך העגלה
            });
        }

        // 4. בדיקת סטטוס אדמין/משתמש והצגת הכפתור בהתאם
        checkUserStatus(); // קריאה לפונקציה שבודקת האם זה לקוח (ואז היא תציג את הכפתור)
    }

    // פונקציה שקוראת לרשימה בכל פעם שחוזרים למסך (למשל אחרי מחיקה בעמוד אחר)
    @Override
    protected void onResume() { // פונקציית מערכת שרצה כשהמסך חוזר להיות בקדמת התצוגה
        super.onResume();
        loadItemsFromDatabase(); // טוען מחדש את רשימת המוצרים כדי להציג את המידע הכי עדכני
    }

    private void checkUserStatus() { // פונקציה שבודקת הרשאות ומתאימה את כפתור העגלה
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // משיכת המשתמש המחובר כרגע

        if (currentUser != null) { // אם אכן יש משתמש מחובר
            String uid = currentUser.getUid(); // קבלת המזהה הייחודי שלו

            // משיכת פרטי המשתמש מה-Database כדי לבדוק את השדה isAdmin
            databaseService.getUser(uid, new DataBaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    if (user != null) { // אם המשתמש נמצא
                        if (user.isAdmin()) { // בדיקה האם הוא מוגדר כמנהל
                            // זה אדמין - הכפתור נשאר חבוי
                            if (btnGoToCart != null) btnGoToCart.setVisibility(View.GONE);
                        } else {
                            // זה משתמש רגיל (לקוח) - מציגים לו את כפתור המעבר לעגלה
                            if (btnGoToCart != null) btnGoToCart.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    // במקרה של תקלה בטעינת פרטי המשתמש, נשאיר את הכפתור מוסתר ליתר ביטחון
                    if (btnGoToCart != null) btnGoToCart.setVisibility(View.GONE);
                }
            });
        } else { // אם משום מה אין משתמש מחובר, נסתיר את הכפתור
            if (btnGoToCart != null) btnGoToCart.setVisibility(View.GONE);
        }
    }

    private void setupSpinner() { // פונקציה להגדרת תפריט הסינון לפי קטגוריות
        // וודא שבקובץ arrs.xml המערך שלך נקרא typeArr (הערה שלך בקוד)
        // יצירת מתווך פשוט (Adapter) שלוקח את מערך הקטגוריות שיצרת בקובץ המשאבים (arrs.xml)
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.typeArr, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // הגדרת העיצוב כשהתפריט נפתח
        spTypeFilter.setAdapter(spinnerAdapter); // חיבור המתווך אל הספינר שבמסך

        // הגדרת פעולה שתתרחש כל פעם שהמשתמש בוחר פריט אחר בספינר
        spTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // קבלת שם הקטגוריה שנבחרה כטקסט
                String selectedType = parent.getItemAtPosition(position).toString();
                filterItems(selectedType); // קריאה לפונקציית העזר שתסנן את המוצרים לפי הקטגוריה הזו
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { } // לא צריך לעשות כלום אם לא נבחר כלום
        });
    }

    private void loadItemsFromDatabase() { // פונקציה ששולפת את כל המוצרים ממסד הנתונים
        // פניה לשירות המסד בבקשה למשוך את כל המוצרים השמורים בחנות
        databaseService.getAllItems(new DataBaseService.DatabaseCallback<List<Item>>() {
            @Override
            public void onCompleted(List<Item> items) { // כשמקבלים את הרשימה מהענן
                if (items != null) {
                    allItemsList.clear(); // מרוקנים את הרשימה הכללית הישנה (כדי שלא יהיו כפילויות)
                    allItemsList.addAll(items); // מכניסים לרשימה הכללית את כל המוצרים שהגיעו כרגע מהשרת

                    // רענון הסינון הנוכחי (אם המשתמש כבר בחר קטגוריה, נרצה לשמור עליה)
                    if (spTypeFilter.getSelectedItem() != null) { // אם נבחרה קטגוריה
                        filterItems(spTypeFilter.getSelectedItem().toString()); // הפעלת הסינון על הרשימה החדשה
                    } else { // אם טרם נבחר כלום (מצב התחלתי)
                        filteredList.clear(); // מרוקנים את הרשימה המסוננת
                        filteredList.addAll(allItemsList); // שמים בה פשוט את כל המוצרים
                        adapter.notifyDataSetChanged(); // מודיעים למתווך של המסך: "הנתונים התעדכנו, נא לרענן תצוגה!"
                    }
                }
            }

            @Override
            public void onFailed(Exception e) { // אם הייתה שגיאה בתקשורת מול המסד
                Log.e("ItemsPage", "Failed to load items", e); // מדפיס הודעת שגיאה באדום ביומן הלוגים של המפתח
            }
        });
    }

    private void filterItems(String type) { // פונקציה האחראית על סינון הרשימה המוצגת לפי סוג מוצר
        filteredList.clear(); // מרוקנת את הרשימה שמוצגת כעת למשתמש כדי לבנות אותה מחדש

        // תיקון: הגנה במקרה שהמשתמש בוחר להציג "הכל" (הערה מהקוד שלך)
        // בודק אם הקטגוריה שנבחרה היא קטגוריה כללית (לפי המילים שהוגדרו או אם היא ריקה)
        if (type.equals("הכל") || type.equals("All Categories") || type.isEmpty()) {
            filteredList.addAll(allItemsList); // אם כן, פשוט מציגים את כל רשימת המוצרים במלואה
        } else { // אם נבחרה קטגוריה ספציפית (כמו למשל "עכברים", "מקלדות" וכו')
            for (Item item : allItemsList) { // לולאה שעוברת על כל מוצר מתוך הרשימה המלאה
                // בודק אם הסוג של המוצר הנוכחי בלולאה שווה לסוג שהמשתמש בחר בספינר
                if (item.getType() != null && item.getType().equals(type)) {
                    filteredList.add(item); // אם יש התאמה, נוסיף את המוצר הזה לרשימה המסוננת שתוצג במסך
                }
            }
        }

        // לאחר שסיימנו למיין ולבנות את הרשימה החדשה, נודיע למתווך לרענן את הרשימה הוויזואלית במסך
        adapter.notifyDataSetChanged();
    }
}