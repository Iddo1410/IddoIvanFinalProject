package com.example.iddoivanfinalproject; // הגדרת מיקום הקובץ בחבילות של הפרויקט

// ייבוא כל המחלקות הנדרשות מתוך ספריות האנדרואיד והפרויקט
import android.graphics.Color; // מחלקה לשימוש בצבעים (למשל לצביעת טקסט)
import android.os.Bundle; // מחלקה לשמירת מצב המסך
import android.view.View; // מחלקת הבסיס לכל רכיבי התצוגה במסך
import android.widget.AdapterView; // מחלקה לזיהוי בחירה מתוך רשימות (כמו ספינר)
import android.widget.ArrayAdapter; // מתווך להצגת נתונים פשוטים ברשימה
import android.widget.Button; // רכיב כפתור
import android.widget.ImageView; // רכיב תמונה
import android.widget.Spinner; // רכיב תפריט נפתח (רשימת בחירה)
import android.widget.TextView; // רכיב טקסט
import android.widget.Toast; // רכיב להודעות קופצות קצרות

import androidx.appcompat.app.AppCompatActivity; // מחלקת האם למסכי אנדרואיד
import androidx.cardview.widget.CardView; // רכיב עיצובי של "כרטיסייה" עם צל ושוליים מעוגלים

import com.example.iddoivanfinalproject.model.Compareitem; // מודל המייצג רשימת השוואה של קטגוריה מסוימת
import com.example.iddoivanfinalproject.model.Item; // מודל המייצג פריט/מוצר בודד
import com.example.iddoivanfinalproject.services.DataBaseService; // שירות הגישה ל-Firebase שיצרת
import com.example.iddoivanfinalproject.utils.ImageUtil; // מחלקת עזר להמרת תמונות

import java.util.ArrayList; // מחלקת מערך דינמי
import java.util.List; // ממשק לרשימות ב-Java

public class CompareList extends BaseActivity {
    // משתנים כלליים של המסך
    private TextView tvEmptyMessage, tvTitle; // טקסט להודעה כשאין מוצרים, וטקסט כותרת
    private CardView cardTable; // הכרטיסייה שמכילה את טבלת ההשוואה עצמה
    private Spinner spCompareCategory; // התפריט הנפתח לבחירת איזה סוג מוצרים להשוות
    private Button btnBack; // כפתור חזרה
    public void onMenuClick(View v) {
        openDrawer(); // קורא לפונקציה שכתבנו ב-BaseActivity
    }

    // רכיבי התצוגה בעמודה של פריט מספר 1 בטבלה
    private TextView tvName1, tvBrand1, tvYear1, tvPrice1, tvDetails1;
    private ImageView imgItem1;

    // רכיבי התצוגה בעמודה של פריט מספר 2 בטבלה
    private TextView tvName2, tvBrand2, tvYear2, tvPrice2, tvDetails2;
    private ImageView imgItem2;

    // רכיבי התצוגה בעמודה של פריט מספר 3 בטבלה
    private TextView tvName3, tvBrand3, tvYear3, tvPrice3, tvDetails3;
    private ImageView imgItem3;

    private DataBaseService.DatabaseService databaseService; // משתנה להתחברות למסד הנתונים

    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה המופעלת עם פתיחת המסך
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_list); // חיבור קובץ העיצוב (XML) של מסך ההשוואה

        initViews(); // קריאה לפונקציה שמקשרת את המשתנים ל-XML
        setupSpinner(); // קריאה לפונקציה שמגדירה את הרשימה הנפתחת של הקטגוריות
    }

    @Override
    protected void onResume() { // מופעל בכל פעם שהמסך חוזר להיות הפעיל ביותר (למשל אחרי שחזרנו ממסך אחר)
        super.onResume();
        // מוודא שהספינר קיים ושנבחרה בו קטגוריה, ואז טוען את הנתונים שלה
        if (spCompareCategory != null && spCompareCategory.getSelectedItem() != null) {
            loadComparisonData(spCompareCategory.getSelectedItem().toString());
        }
    }

    private void initViews() { // פונקציה שמקשרת בין משתני ה-Java ל-ID של הרכיבים ב-XML
        tvTitle = findViewById(R.id.tvTitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        cardTable = findViewById(R.id.cardTable);
        spCompareCategory = findViewById(R.id.spCompareCategory);

        btnBack = findViewById(R.id.btnUniversalBack); // חיבור כפתור החזרה
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish()); // לחיצה עליו סוגרת את המסך הנוכחי
        }

        // חיבור הרכיבים של עמודת המוצר הראשון
        tvName1 = findViewById(R.id.tvName1);
        tvBrand1 = findViewById(R.id.tvBrand1);
        tvYear1 = findViewById(R.id.tvYear1);
        tvPrice1 = findViewById(R.id.tvPrice1);
        tvDetails1 = findViewById(R.id.tvDetails1);
        imgItem1 = findViewById(R.id.imgItem1);

        // חיבור הרכיבים של עמודת המוצר השני
        tvName2 = findViewById(R.id.tvName2);
        tvBrand2 = findViewById(R.id.tvBrand2);
        tvYear2 = findViewById(R.id.tvYear2);
        tvPrice2 = findViewById(R.id.tvPrice2);
        tvDetails2 = findViewById(R.id.tvDetails2);
        imgItem2 = findViewById(R.id.imgItem2);

        // חיבור הרכיבים של עמודת המוצר השלישי
        tvName3 = findViewById(R.id.tvName3);
        tvBrand3 = findViewById(R.id.tvBrand3);
        tvYear3 = findViewById(R.id.tvYear3);
        tvPrice3 = findViewById(R.id.tvPrice3);
        tvDetails3 = findViewById(R.id.tvDetails3);
        imgItem3 = findViewById(R.id.imgItem3);

        databaseService = DataBaseService.DatabaseService.getInstance(); // קבלת הגישה למסד הנתונים
    }

    private void setupSpinner() { // הגדרת תפריט בחירת הקטגוריות
        // שולף את רשימת הקטגוריות מתוך קובץ ה-XML של משאבי הפרויקט (arrs.xml)
        String[] categoriesArray = getResources().getStringArray(R.array.typeArr);
        // יצירת מתווך בסיסי שמחבר את המערך לרכיב התצוגה של אנדרואיד
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // הגדרת עיצוב פתיחת התפריט
        spCompareCategory.setAdapter(adapter); // חיבור המתווך לספינר

        // בדיקה אם הועברה קטגוריה התחלתית ממסך אחר (COMPARE_TYPE)
        String initialType = getIntent().getStringExtra("COMPARE_TYPE");
        if (initialType != null) { // אם כן
            int pos = adapter.getPosition(initialType); // מוצא את המיקום של הקטגוריה ברשימה
            if (pos >= 0) spCompareCategory.setSelection(pos); // בוחר אוטומטית בקטגוריה הזו בספינר
        }

        // הגדרת מאזין המופעל בכל פעם שהמשתמש בוחר פריט אחר בספינר
        spCompareCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // מפעיל את פונקציית טעינת הנתונים עם שם הקטגוריה שנבחרה
                loadComparisonData(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {} // לא נדרשת פעולה אם לא נבחר כלום
        });
    }

    private void loadComparisonData(String type) { // פונקציה לטעינת נתוני ההשוואה לפי קטגוריה
        // שלב 1: משיכת *כל* המוצרים שיש בחנות ממסד הנתונים
        databaseService.getAllItems(new DataBaseService.DatabaseCallback<List<Item>>() {
            @Override
            public void onCompleted(List<Item> allStoreItems) {
                if (allStoreItems == null) return; // אם אין מוצרים בכלל, יוצאים

                // שלב 2: משיכת רשימת ההשוואה הספציפית לקטגוריה הזו (type) ממסד הנתונים
                databaseService.getCompareByType(type, new DataBaseService.DatabaseCallback<Compareitem>() {
                    @Override
                    public void onCompleted(Compareitem dbCompare) { // כשהנתונים חזרו מהמסד
                        // בודקים אם יש רשימת השוואה קיימת ושהיא לא ריקה
                        if (dbCompare != null && dbCompare.getItemArrayList() != null && !dbCompare.getItemArrayList().isEmpty()) {

                            List<Item> validItems = new ArrayList<>(); // יצירת רשימה זמנית למוצרים "חוקיים" (כאלה שעדיין קיימים בחנות)

                            // מעבר על כל פריט שנמצא ברשימת ההשוואה
                            for (Item compareItem : dbCompare.getItemArrayList()) {
                                boolean existsInStore = false; // משתנה מסמן האם הפריט קיים בחנות

                                // לולאה פנימית שבודקת מול כל מוצרי החנות
                                for (Item storeItem : allStoreItems) {
                                    // אם ה-ID של פריט ההשוואה שווה ל-ID של מוצר קיים בחנות
                                    if (compareItem.getId() != null && storeItem.getId() != null && compareItem.getId().equals(storeItem.getId())) {
                                        existsInStore = true; // סימון שהוא קיים!
                                        break; // עצירת הלולאה הפנימית כי מצאנו אותו
                                    }
                                }
                                if (existsInStore) { // אם הוא אכן עדיין בחנות
                                    validItems.add(compareItem); // מוסיפים אותו לרשימה התקינה
                                }
                            }

                            // --- מנגנון הריפוי האוטומטי (Auto-Heal) ---
                            // אם מצאנו שיש ב-Firebase פריטים שכבר לא קיימים בחנות ("רוחות רפאים" - למשל כי מנהל מחק אותם), אנחנו מנקים אותם לתמיד!
                            // אם כמות הפריטים התקינים קטנה מהכמות שהייתה ברשימת ההשוואה המקורית:
                            if (validItems.size() != dbCompare.getItemArrayList().size()) {
                                dbCompare.getItemArrayList().clear(); // מרוקנים את רשימת ההשוואה מהמסד
                                dbCompare.getItemArrayList().addAll(validItems); // מוסיפים לה רק את הפריטים התקינים

                                // שומרים את הרשימה המעודכנת חזרה ב-Firebase כדי שלא נצטרך לעשות את הבדיקה שוב בפעם הבאה
                                databaseService.updateCompareList(dbCompare, new DataBaseService.DatabaseCallback<Void>() {
                                    @Override public void onCompleted(Void o) {}
                                    @Override public void onFailed(Exception e) {}
                                });
                            }
                            // ------------------------------------------------

                            if (!validItems.isEmpty()) { // אם נותרו פריטים תקינים להשוואה
                                cardTable.setVisibility(View.VISIBLE); // מציגים את טבלת ההשוואה
                                tvEmptyMessage.setVisibility(View.GONE); // מעלימים את הודעת "אין מוצרים"

                                // אכלוס (מילוי) עמודת המוצר הראשון עם הפריט הראשון ברשימה
                                populateItem1(validItems.get(0));

                                // אם יש יותר ממוצר אחד, מאכלסים את העמודה השנייה, אחרת מנקים אותה
                                if (validItems.size() > 1) {
                                    populateItem2(validItems.get(1));
                                } else {
                                    clearItem2();
                                }

                                // אם יש יותר משני מוצרים, מאכלסים את העמודה השלישית, אחרת מנקים אותה
                                if (validItems.size() > 2) {
                                    populateItem3(validItems.get(2));
                                } else {
                                    clearItem3();
                                }

                                applyHighlighting(validItems); // קריאה לפונקציה שמדגישה בצבע את המחיר הכי זול והשנה הכי חדשה

                            } else { // אם אחרי הסינון לא נשארו פריטים חוקיים
                                cardTable.setVisibility(View.GONE); // מסתירים את הטבלה
                                tvEmptyMessage.setVisibility(View.VISIBLE); // מראים הודעה
                                tvEmptyMessage.setText("אין מוצרים זמינים להשוואה בקטגוריית " + type);
                            }

                        } else { // אם מראש הרשימה ב-Firebase הייתה ריקה
                            cardTable.setVisibility(View.GONE); // מסתירים טבלה
                            tvEmptyMessage.setVisibility(View.VISIBLE); // מראים הודעה
                            tvEmptyMessage.setText("אין מוצרים להשוואה בקטגוריית " + type);
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(CompareList.this, "שגיאה בטעינת ההשוואה", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(CompareList.this, "שגיאה בחיבור לנתוני החנות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyHighlighting(List<Item> items) { // פונקציה להדגשת נתונים בולטים (הכי זול, הכי חדש)
        resetColors(); // קודם כל מאפסים את הצבעים חזרה לשחור למקרה שהם היו ירוקים מחיפוש קודם

        if (items.size() < 2) return; // אם יש רק מוצר אחד להשוות, אין טעם להדגיש כלום (כי הוא ממילא מנצח בעצמו)

        // --- חיפוש המחיר הנמוך ביותר ---
        double minPrice = items.get(0).getPrice(); // מניחים שהמחיר של הראשון הוא הכי נמוך בהתחלה
        for (Item item : items) { // עוברים על כולם
            if (item.getPrice() < minPrice) minPrice = item.getPrice(); // אם מצאנו מחיר יותר נמוך, הוא הופך להיות המינימלי
        }

        // --- חיפוש שנת הייצור המאוחרת (החדשה) ביותר ---
        int maxYear = 0;
        for (Item item : items) {
            try { // מנסים להמיר את שנת הייצור ממחרוזת למספר שלם
                int year = Integer.parseInt(item.getYear());
                if (year > maxYear) maxYear = year; // אם מצאנו שנה גבוהה יותר, היא המקסימום
            } catch (Exception ignored) {} // מתעלמים משגיאות המרה (למשל אם השנה כתובה בטקסט לא חוקי)
        }

        // --- צביעת הנתונים במסך (בירוק) ---
        // עבור המוצר הראשון: אם המחיר שלו שווה למינימום - צבע לירוק, אם השנה שווה למקסימום - צבע לירוק
        if (items.size() >= 1) {
            if (items.get(0).getPrice() == minPrice) tvPrice1.setTextColor(Color.parseColor("#27AE60")); // #27AE60 זה קוד לצבע ירוק
            try { if (Integer.parseInt(items.get(0).getYear()) == maxYear) tvYear1.setTextColor(Color.parseColor("#27AE60")); } catch(Exception ignored){}
        }

        // אותו דבר עבור המוצר השני
        if (items.size() >= 2) {
            if (items.get(1).getPrice() == minPrice) tvPrice2.setTextColor(Color.parseColor("#27AE60"));
            try { if (Integer.parseInt(items.get(1).getYear()) == maxYear) tvYear2.setTextColor(Color.parseColor("#27AE60")); } catch(Exception ignored){}
        }

        // אותו דבר עבור המוצר השלישי (אם קיים)
        if (items.size() >= 3 && tvPrice3 != null && tvYear3 != null) {
            if (items.get(2).getPrice() == minPrice) tvPrice3.setTextColor(Color.parseColor("#27AE60"));
            try { if (Integer.parseInt(items.get(2).getYear()) == maxYear) tvYear3.setTextColor(Color.parseColor("#27AE60")); } catch(Exception ignored){}
        }
    }

    private void resetColors() { // פונקציה פשוטה שצובעת את כל הטקסטים של מחיר ושנה חזרה לשחור (איפוס)
        tvPrice1.setTextColor(Color.BLACK);
        tvYear1.setTextColor(Color.BLACK);
        tvPrice2.setTextColor(Color.BLACK);
        tvYear2.setTextColor(Color.BLACK);

        if (tvPrice3 != null) tvPrice3.setTextColor(Color.BLACK);
        if (tvYear3 != null) tvYear3.setTextColor(Color.BLACK);
    }

    // פונקציה שמכניסה את נתוני מוצר 1 לתוך שדות התצוגה בעמודה הראשונה
    private void populateItem1(Item item) {
        tvName1.setText(item.getName()); // קביעת שם
        tvBrand1.setText(item.getBrand()); // קביעת מותג
        tvYear1.setText(item.getYear()); // קביעת שנה
        tvPrice1.setText("₪" + item.getPrice()); // קביעת מחיר כולל סמל שקל
        tvDetails1.setText(item.getDetails()); // קביעת פרטים נוספים
        // המרת התמונה מ-Base64 (הפורמט שנשמר במסד) לתמונה אמיתית (Bitmap) והצגתה
        if (item.getPic() != null) imgItem1.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
    }

    // פונקציה שמכניסה את נתוני מוצר 2 לתוך שדות התצוגה בעמודה השנייה
    private void populateItem2(Item item) {
        tvName2.setText(item.getName());
        tvBrand2.setText(item.getBrand());
        tvYear2.setText(item.getYear());
        tvPrice2.setText("₪" + item.getPrice());
        tvDetails2.setText(item.getDetails());
        if (item.getPic() != null) imgItem2.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
    }

    // פונקציה שמכניסה את נתוני מוצר 3 לתוך שדות התצוגה בעמודה השלישית
    private void populateItem3(Item item) {
        if (tvName3 != null) tvName3.setText(item.getName()); // הוספנו בדיקת null למקרה שהרכיב לא קיים במסכים קטנים
        if (tvBrand3 != null) tvBrand3.setText(item.getBrand());
        if (tvYear3 != null) tvYear3.setText(item.getYear());
        if (tvPrice3 != null) tvPrice3.setText("₪" + item.getPrice());
        if (tvDetails3 != null) tvDetails3.setText(item.getDetails());
        if (imgItem3 != null && item.getPic() != null) imgItem3.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
    }

    // פונקציה שמנקה את העמודה השנייה אם חסר בה מוצר (מציגה מקפים)
    private void clearItem2() {
        tvName2.setText("טרם נבחר");
        tvBrand2.setText("-");
        tvYear2.setText("-");
        tvPrice2.setText("-");
        tvDetails2.setText("-");
        imgItem2.setImageDrawable(null); // מסיר תמונה קודמת
    }

    // פונקציה שמנקה את העמודה השלישית אם חסר בה מוצר
    private void clearItem3() {
        if (tvName3 != null) tvName3.setText("טרם נבחר");
        if (tvBrand3 != null) tvBrand3.setText("-");
        if (tvYear3 != null) tvYear3.setText("-");
        if (tvPrice3 != null) tvPrice3.setText("-");
        if (tvDetails3 != null) tvDetails3.setText("-");
        if (imgItem3 != null) imgItem3.setImageDrawable(null); // מסיר תמונה קודמת
    }
}