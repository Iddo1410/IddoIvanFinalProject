package com.example.iddoivanfinalproject; // הגדרת מיקום הקובץ בתוך תיקיות (חבילות) הפרויקט

// ייבוא כל המחלקות הנדרשות מתוך ספריות האנדרואיד והפרויקט
import android.content.Intent; // מחלקה המאפשרת מעבר בין מסכים שונים באפליקציה
import android.graphics.Color; // מחלקה לטיפול ושימוש בצבעים (למשל לצביעת טקסט לירוק/שחור)
import android.os.Bundle; // מחלקה ששומרת את המידע על מצב המסך בזמן פתיחה או שחזור
import android.view.View; // מחלקת הבסיס שמייצגת כל אלמנט ויזואלי על המסך (כפתור, טקסט וכו')
import android.widget.AdapterView; // מחלקה שעוזרת לזהות כשמשתמש בוחר פריט מתוך רשימה נפתחת
import android.widget.ArrayAdapter; // מתווך שלוקח מערך פשוט של נתונים ומציג אותו ברשימה (בספינר)
import android.widget.Button; // מחלקת רכיב של כפתור לחיץ
import android.widget.ImageView; // מחלקת רכיב המציג תמונה
import android.widget.Spinner; // מחלקת רכיב של תפריט נפתח (רשימת בחירה)
import android.widget.TextView; // מחלקת רכיב המציג טקסט
import android.widget.Toast; // מחלקה המציגה הודעות קופצות קצרות בתחתית המסך

import androidx.appcompat.app.AppCompatActivity; // מחלקת האם שממנה יורש כל מסך מודרני באנדרואיד
import androidx.cardview.widget.CardView; // רכיב עיצובי מתקדם של "כרטיסייה" עם צל ושוליים מעוגלים

import com.example.iddoivanfinalproject.model.Compareitem; // המודל שמייצג את מבנה הנתונים של רשימת ההשוואה
import com.example.iddoivanfinalproject.model.Item; // המודל שמייצג מוצר בודד בחנות
import com.example.iddoivanfinalproject.services.DataBaseService; // השירות שבנית כדי לדבר עם מסד הנתונים (Firebase)
import com.example.iddoivanfinalproject.utils.ImageUtil; // מחלקת עזר שיצרת לטיפול והמרת תמונות

import java.util.ArrayList; // מחלקת מערך דינמי (רשימה שיכולה לגדול ולקטון)
import java.util.List; // ממשק בסיסי לעבודה עם רשימות ב-Java

public class CompareList extends AppCompatActivity { // הגדרת המחלקה של מסך ההשוואה, שיורשת ממסך אנדרואיד רגיל

    // הגדרת משתנים כלליים של המסך
    private TextView tvEmptyMessage, tvTitle; // משתנים לשדה ההודעה כשההשוואה ריקה, ולכותרת המסך
    private CardView cardTable; // משתנה למסגרת הכרטיסייה שעוטפת את כל טבלת ההשוואה
    private Spinner spCompareCategory; // משתנה לתפריט הנפתח (ספינר) לבחירת קטגוריה
    private Button btnBack; // משתנה לכפתור שחוזר אחורה למסך הקודם

    // רכיבי התצוגה בעמודה של פריט מספר 1 בטבלה
    private TextView tvName1, tvBrand1, tvYear1, tvPrice1, tvDetails1; // שדות טקסט לפרטי המוצר הראשון
    private ImageView imgItem1; // שדה תמונה למוצר הראשון

    // רכיבי התצוגה בעמודה של פריט מספר 2 בטבלה
    private TextView tvName2, tvBrand2, tvYear2, tvPrice2, tvDetails2; // שדות טקסט לפרטי המוצר השני
    private ImageView imgItem2; // שדה תמונה למוצר השני

    // רכיבי התצוגה בעמודה של פריט מספר 3 בטבלה
    private TextView tvName3, tvBrand3, tvYear3, tvPrice3, tvDetails3; // שדות טקסט לפרטי המוצר השלישי
    private ImageView imgItem3; // שדה תמונה למוצר השלישי

    private DataBaseService.DatabaseService databaseService; // משתנה שישמור את החיבור למסד הנתונים שלנו

    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה הראשונה שמופעלת ברגע שהמסך נוצר
        super.onCreate(savedInstanceState); // קריאה לפעולת ההכנה הבסיסית של אנדרואיד למסך
        setContentView(R.layout.activity_compare_list); // חיבור קובץ העיצוב (XML) אל המחלקה הזו ב-Java

        initViews(); // קריאה לפונקציה שאחראית לאתר את כל הרכיבים ב-XML ולחבר אותם למשתנים
        setupSpinner(); // קריאה לפונקציה שאחראית לאתחל את רשימת הקטגוריות בספינר
    }

    @Override
    protected void onResume() { // פונקציית מערכת שמופעלת בכל פעם שהמסך הזה חוזר להיות גלוי ופעיל
        super.onResume(); // קריאה לפעולת ה-Resume הבסיסית של אנדרואיד
        if (spCompareCategory != null && spCompareCategory.getSelectedItem() != null) { // מוודא שהספינר קיים ושנבחרה בו קטגוריה
            loadComparisonData(spCompareCategory.getSelectedItem().toString()); // טוען מחדש את הנתונים לפי הקטגוריה שנבחרה כרגע
        }
    }

    private void initViews() { // פונקציה שעושה סדר ומקשרת כל משתנה ב-Java ל-ID של הרכיב ב-XML
        tvTitle = findViewById(R.id.tvTitle); // חיפוש וקישור כותרת
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage); // חיפוש וקישור הודעת "אין מוצרים"
        cardTable = findViewById(R.id.cardTable); // חיפוש וקישור מסגרת הטבלה
        spCompareCategory = findViewById(R.id.spCompareCategory); // חיפוש וקישור הספינר

        btnBack = findViewById(R.id.btnUniversalBack); // חיפוש וקישור כפתור החזרה
        if (btnBack != null) { // מוודא שהכפתור אכן מצא חיבור ל-XML כדי לא לקרוס
            btnBack.setOnClickListener(v -> finish()); // מגדיר שלחיצה על הכפתור פשוט תסגור את המסך (finish)
        }

        // חיבור הרכיבים של עמודת המוצר הראשון מתוך ה-XML
        tvName1 = findViewById(R.id.tvName1);
        tvBrand1 = findViewById(R.id.tvBrand1);
        tvYear1 = findViewById(R.id.tvYear1);
        tvPrice1 = findViewById(R.id.tvPrice1);
        tvDetails1 = findViewById(R.id.tvDetails1);
        imgItem1 = findViewById(R.id.imgItem1);

        // חיבור הרכיבים של עמודת המוצר השני מתוך ה-XML
        tvName2 = findViewById(R.id.tvName2);
        tvBrand2 = findViewById(R.id.tvBrand2);
        tvYear2 = findViewById(R.id.tvYear2);
        tvPrice2 = findViewById(R.id.tvPrice2);
        tvDetails2 = findViewById(R.id.tvDetails2);
        imgItem2 = findViewById(R.id.imgItem2);

        // חיבור הרכיבים של עמודת המוצר השלישי מתוך ה-XML
        tvName3 = findViewById(R.id.tvName3);
        tvBrand3 = findViewById(R.id.tvBrand3);
        tvYear3 = findViewById(R.id.tvYear3);
        tvPrice3 = findViewById(R.id.tvPrice3);
        tvDetails3 = findViewById(R.id.tvDetails3);
        imgItem3 = findViewById(R.id.imgItem3);

        databaseService = DataBaseService.DatabaseService.getInstance(); // קבלת המופע (Instance) היחיד של שירות מסד הנתונים
    }

    private void setupSpinner() { // פונקציה שמגדירה את הנתונים וההתנהגות של תפריט בחירת הקטגוריות (ספינר)
        String[] categoriesArray = getResources().getStringArray(R.array.typeArr); // שולף את רשימת הקטגוריות מתוך קובץ string/arrs שבמשאבי האפליקציה
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesArray); // יוצר מתווך שממיר את מערך הטקסטים לשורות ברשימה
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // מגדיר איך תיראה הרשימה כשהיא נפתחת (עיצוב מובנה של אנדרואיד)
        spCompareCategory.setAdapter(adapter); // מחבר את המתווך (עם הנתונים) אל רכיב הספינר במסך

        String initialType = getIntent().getStringExtra("COMPARE_TYPE"); // בודק האם מסך אחר שלח לנו כוונה (Intent) לפתוח קטגוריה ספציפית
        if (initialType != null) { // אם אכן נשלחה קטגוריה
            int pos = adapter.getPosition(initialType); // מחפש באיזה מיקום (אינדקס) ברשימה נמצאת הקטגוריה הזו
            if (pos >= 0) spCompareCategory.setSelection(pos); // אם היא נמצאה, משנה את בחירת הספינר לאותה קטגוריה אוטומטית
        }

        spCompareCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // מגדיר מאזין שקופץ בכל פעם שבוחרים פריט בספינר
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // כשנבחר פריט מסוים
                loadComparisonData(parent.getItemAtPosition(position).toString()); // מפעיל את טעינת הנתונים עם שם הקטגוריה שנבחרה
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {} // לא עושה כלום אם לא נבחר שום דבר
        });
    }

    private void loadComparisonData(String type) { // פונקציה לטעינת נתוני ההשוואה ממסד הנתונים, בהתאם לקטגוריה המבוקשת
        databaseService.getAllItems(new DataBaseService.DatabaseCallback<List<Item>>() { // שלב 1: פונה למסד הנתונים ומבקש את כל המוצרים שיש בחנות כרגע
            @Override
            public void onCompleted(List<Item> allStoreItems) { // ברגע שרשימת כל המוצרים מתקבלת
                if (allStoreItems == null) return; // הגנה: אם אין נתונים בכלל, עוצר את הפונקציה

                databaseService.getCompareByType(type, new DataBaseService.DatabaseCallback<Compareitem>() { // שלב 2: מבקש את רשימת ההשוואה השמורה של המשתמש
                    @Override
                    public void onCompleted(Compareitem dbCompare) { // ברגע שרשימת ההשוואה מתקבלת
                        if (dbCompare != null && dbCompare.getItemArrayList() != null && !dbCompare.getItemArrayList().isEmpty()) { // בודק שהרשימה קיימת ולא ריקה

                            List<Item> allValidItems = new ArrayList<>(); // רשימה זמנית שתשמור את כל המוצרים שעדיין קיימים בחנות
                            List<Item> filteredItemsToShow = new ArrayList<>(); // רשימה זמנית רק למוצרים שמתאימים לקטגוריה שבחרנו עכשיו

                            for (Item compareItem : dbCompare.getItemArrayList()) { // עובר בלולאה על כל מוצר שנמצא ברשימת ההשוואה השמורה
                                boolean existsInStore = false; // דגל (משתנה בוליאני) שיסמן לנו אם מצאנו את המוצר בחנות

                                for (Item storeItem : allStoreItems) { // עובר על כל המוצרים בחנות
                                    if (compareItem.getId() != null && storeItem.getId() != null && compareItem.getId().equals(storeItem.getId())) { // בודק האם ה-ID תואם
                                        existsInStore = true; // אם יש התאמה, מסמן שהמוצר קיים בחנות
                                        break; // עוצר את הלולאה הפנימית (אין צורך להמשיך לחפש)
                                    }
                                }

                                if (existsInStore) { // אם המוצר באמת עדיין קיים בחנות (לא נמחק ע"י מנהל)
                                    allValidItems.add(compareItem); // מוסיף אותו לרשימת "המוצרים התקינים" הכללית

                                    if (compareItem.getType() != null && compareItem.getType().equals(type)) { // בודק האם הסוג שלו תואם לסוג שבחרנו בספינר
                                        filteredItemsToShow.add(compareItem); // אם כן, מוסיף אותו לרשימה שתוצג בפועל על המסך
                                    }
                                }
                            }

                            if (allValidItems.size() != dbCompare.getItemArrayList().size()) { // בודק האם נמחקו פריטים (האם רשימת התקינים קטנה מהמקורית)
                                dbCompare.getItemArrayList().clear(); // מרוקן את הרשימה המקורית באובייקט
                                dbCompare.getItemArrayList().addAll(allValidItems); // מכניס רק את הפריטים התקינים

                                databaseService.updateCompareList(dbCompare, new DataBaseService.DatabaseCallback<Void>() { // שומר את העדכון הזה ב-Firebase כדי לנקות שאריות לתמיד
                                    @Override public void onCompleted(Void o) {} // אין צורך לעשות כלום בסיום
                                    @Override public void onFailed(Exception e) {} // מתעלם במקרה של שגיאה
                                });
                            }

                            if (!filteredItemsToShow.isEmpty()) { // אם יש לנו פריטים חוקיים ששייכים לקטגוריה הנוכחית להציג
                                cardTable.setVisibility(View.VISIBLE); // מציג את מסגרת הטבלה
                                tvEmptyMessage.setVisibility(View.GONE); // מעלים את ההודעה שאומרת שאין מוצרים

                                populateItem1(filteredItemsToShow.get(0)); // שולח את הפריט הראשון ברשימה לאכלס את העמודה הראשונה

                                if (filteredItemsToShow.size() > 1) { // אם יש ברשימה יותר ממוצר אחד
                                    populateItem2(filteredItemsToShow.get(1)); // מאכלס את העמודה השנייה עם הפריט השני
                                } else { // אם אין פריט שני
                                    clearItem2(); // מפעיל פונקציה שמנקה את העמודה השנייה
                                }

                                if (filteredItemsToShow.size() > 2) { // אם יש ברשימה יותר משני מוצרים
                                    populateItem3(filteredItemsToShow.get(2)); // מאכלס את העמודה השלישית
                                } else { // אם אין פריט שלישי
                                    clearItem3(); // מנקה את העמודה השלישית
                                }

                                applyHighlighting(filteredItemsToShow); // מפעיל את הפונקציה שצובעת בירוק את הנתונים הטובים ביותר

                            } else { // אם יש מוצרים בהשוואה, אבל אף אחד מהם לא תואם לקטגוריה שבחרנו עכשיו
                                cardTable.setVisibility(View.GONE); // מסתיר את הטבלה
                                tvEmptyMessage.setVisibility(View.VISIBLE); // מציג הודעה
                                tvEmptyMessage.setText("אין מוצרים זמינים להשוואה בקטגוריית " + type); // מעדכן את תוכן ההודעה
                            }

                        } else { // אם רשימת ההשוואה המקורית ריקה לחלוטין מכל סוג של מוצר
                            cardTable.setVisibility(View.GONE); // מסתיר את הטבלה
                            tvEmptyMessage.setVisibility(View.VISIBLE); // מציג הודעה
                            tvEmptyMessage.setText("אין מוצרים להשוואה בקטגוריית " + type); // מעדכן את תוכן ההודעה
                        }
                    }

                    @Override
                    public void onFailed(Exception e) { // אם הבאת ההשוואה מהמסד נכשלה
                        Toast.makeText(CompareList.this, "שגיאה בטעינת ההשוואה", Toast.LENGTH_SHORT).show(); // מקפיץ שגיאה קצרה
                    }
                });
            }

            @Override
            public void onFailed(Exception e) { // אם הבאת מוצרי החנות נכשלה
                Toast.makeText(CompareList.this, "שגיאה בחיבור לנתוני החנות", Toast.LENGTH_SHORT).show(); // מקפיץ שגיאה קצרה
            }
        });
    }

    private void applyHighlighting(List<Item> items) { // פונקציה שעוברת על הנתונים ומדגישה בירוק את הזול והחדש ביותר
        resetColors(); // קודם כל מאפסת את כל הצבעים לשחור כדי לנקות הדגשות קודמות

        if (items.size() < 2) return; // אם יש רק מוצר אחד להשוות, יוצא מהפונקציה כי אין טעם להדגיש מול כלום

        double minPrice = items.get(0).getPrice(); // קובע שהמחיר המינימלי ההתחלתי הוא המחיר של המוצר הראשון
        for (Item item : items) { // עובר על כל המוצרים ברשימה
            if (item.getPrice() < minPrice) minPrice = item.getPrice(); // אם מוצר מסוים זול יותר, הוא הופך למינימלי החדש
        }

        int maxYear = 0; // מגדיר משתנה שייצג את השנה הכי גדולה (חדשה), מתחיל מאפס
        for (Item item : items) { // עובר על המוצרים
            try { // מנסה להפעיל המרה ללא קריסה
                int year = Integer.parseInt(item.getYear()); // ממיר את טקסט השנה למספר שלם
                if (year > maxYear) maxYear = year; // אם השנה יותר גדולה, היא הופכת למקסימום החדש
            } catch (Exception ignored) {} // אם ההמרה נכשלה (למשל נכתבו מילים במקום שנה), מתעלם
        }

        if (items.size() >= 1) { // בדיקה עבור המוצר הראשון
            if (items.get(0).getPrice() == minPrice) tvPrice1.setTextColor(Color.parseColor("#27AE60")); // אם מחירו שווה למינימלי, צובע לירוק
            try { if (Integer.parseInt(items.get(0).getYear()) == maxYear) tvYear1.setTextColor(Color.parseColor("#27AE60")); } catch(Exception ignored){} // אותו דבר לגבי שנת הייצור
        }

        if (items.size() >= 2) { // בדיקה עבור המוצר השני
            if (items.get(1).getPrice() == minPrice) tvPrice2.setTextColor(Color.parseColor("#27AE60")); // צביעת מחיר לירוק אם הוא הכי זול
            try { if (Integer.parseInt(items.get(1).getYear()) == maxYear) tvYear2.setTextColor(Color.parseColor("#27AE60")); } catch(Exception ignored){} // צביעת שנה
        }

        if (items.size() >= 3 && tvPrice3 != null && tvYear3 != null) { // בדיקה עבור המוצר השלישי (אם קיים)
            if (items.get(2).getPrice() == minPrice) tvPrice3.setTextColor(Color.parseColor("#27AE60")); // צביעת מחיר
            try { if (Integer.parseInt(items.get(2).getYear()) == maxYear) tvYear3.setTextColor(Color.parseColor("#27AE60")); } catch(Exception ignored){} // צביעת שנה
        }
    }

    private void resetColors() { // פונקציית איפוס הצבעים חזרה לשחור
        tvPrice1.setTextColor(Color.BLACK); // מחזיר מחיר 1 לשחור
        tvYear1.setTextColor(Color.BLACK); // מחזיר שנה 1 לשחור
        tvPrice2.setTextColor(Color.BLACK); // מחזיר מחיר 2 לשחור
        tvYear2.setTextColor(Color.BLACK); // מחזיר שנה 2 לשחור

        if (tvPrice3 != null) tvPrice3.setTextColor(Color.BLACK); // מוודא קיום ומחזיר מחיר 3 לשחור
        if (tvYear3 != null) tvYear3.setTextColor(Color.BLACK); // מוודא קיום ומחזיר שנה 3 לשחור
    }

    // הפונקציה למעבר לעמוד המוצר (כשלוחצים על תמונה או שם בהשוואה)
    private void openItemDetails(Item item) {
        if (item != null && item.getId() != null) { // מוודא שהמוצר וה-ID שלו תקינים
            Intent intent = new Intent(CompareList.this, Itemdetails.class); // מכין מעבר מסך מפה למסך פרטי המוצר
            intent.putExtra("ITEM_ID", item.getId()); // "מעמיס" על המעבר את המזהה של המוצר כדי שהמסך הבא ידע מה להציג
            startActivity(intent); // משגר את המעבר ומציג את המסך החדש
        }
    }

    private void populateItem1(Item item) { // פונקציה שממלאת את השדות של עמודה 1 לפי הנתונים שקיבלה
        tvName1.setText(item.getName()); // מציבה את שם המוצר
        tvBrand1.setText(item.getBrand()); // מציבה את שם המותג
        tvYear1.setText(item.getYear()); // מציבה את שנת הייצור
        tvPrice1.setText("₪" + item.getPrice()); // מציבה את המחיר פלוס סימן מטבע
        tvDetails1.setText(item.getDetails()); // מציבה את הטקסט של פרטי המוצר
        if (item.getPic() != null) imgItem1.setImageBitmap(ImageUtil.convertFrom64base(item.getPic())); // הופכת את טקסט התמונה לביטמאפ ושמה ברכיב התמונה

        imgItem1.setOnClickListener(v -> openItemDetails(item)); // מגדירה שלחיצה על התמונה תפתח את פרטי המוצר
        tvName1.setOnClickListener(v -> openItemDetails(item)); // מגדירה שלחיצה על השם תפתח את פרטי המוצר
    }

    private void populateItem2(Item item) { // פונקציה שממלאת את השדות של עמודה 2
        tvName2.setText(item.getName()); // מציבה שם
        tvBrand2.setText(item.getBrand()); // מציבה מותג
        tvYear2.setText(item.getYear()); // מציבה שנה
        tvPrice2.setText("₪" + item.getPrice()); // מציבה מחיר
        tvDetails2.setText(item.getDetails()); // מציבה פרטים
        if (item.getPic() != null) imgItem2.setImageBitmap(ImageUtil.convertFrom64base(item.getPic())); // ממירה ושמה תמונה

        imgItem2.setOnClickListener(v -> openItemDetails(item)); // מאזין לחיצה לתמונה
        tvName2.setOnClickListener(v -> openItemDetails(item)); // מאזין לחיצה לשם
    }

    private void populateItem3(Item item) { // פונקציה שממלאת את השדות של עמודה 3 (עם הגנות למקרה שהמסך קטן והעמודה הוסתרה)
        if (tvName3 != null) tvName3.setText(item.getName()); // אם הרכיב קיים מציב שם
        if (tvBrand3 != null) tvBrand3.setText(item.getBrand()); // מציב מותג
        if (tvYear3 != null) tvYear3.setText(item.getYear()); // מציב שנה
        if (tvPrice3 != null) tvPrice3.setText("₪" + item.getPrice()); // מציב מחיר
        if (tvDetails3 != null) tvDetails3.setText(item.getDetails()); // מציב פרטים
        if (imgItem3 != null && item.getPic() != null) imgItem3.setImageBitmap(ImageUtil.convertFrom64base(item.getPic())); // ממיר ומציב תמונה

        if (imgItem3 != null) imgItem3.setOnClickListener(v -> openItemDetails(item)); // מאזין לחיצה לתמונה
        if (tvName3 != null) tvName3.setOnClickListener(v -> openItemDetails(item)); // מאזין לחיצה לשם
    }

    private void clearItem2() { // פונקציה לאיפוס התצוגה של עמודה 2 במקרה שהיא ריקה
        tvName2.setText("טרם נבחר"); // משנה לטקסט ריק
        tvBrand2.setText("-"); // שם מקף
        tvYear2.setText("-"); // שם מקף
        tvPrice2.setText("-"); // שם מקף
        tvDetails2.setText("-"); // שם מקף
        imgItem2.setImageDrawable(null); // מסיר את התמונה אם הייתה קודם

        imgItem2.setOnClickListener(null); // מנתק את מאזין הלחיצה כדי שלא יהיה אפשר ללחוץ על הריק
        tvName2.setOnClickListener(null); // מנתק מאזין לחיצה מהשם
    }

    private void clearItem3() { // פונקציה לאיפוס התצוגה של עמודה 3 במקרה שהיא ריקה
        if (tvName3 != null) tvName3.setText("טרם נבחר"); // משנה שם
        if (tvBrand3 != null) tvBrand3.setText("-"); // שם מקף למותג
        if (tvYear3 != null) tvYear3.setText("-"); // שם מקף לשנה
        if (tvPrice3 != null) tvPrice3.setText("-"); // שם מקף למחיר
        if (tvDetails3 != null) tvDetails3.setText("-"); // שם מקף לפרטים
        if (imgItem3 != null) imgItem3.setImageDrawable(null); // מנקה את התמונה

        if (imgItem3 != null) imgItem3.setOnClickListener(null); // מנתק מאזין
        if (tvName3 != null) tvName3.setOnClickListener(null); // מנתק מאזין
    }
}