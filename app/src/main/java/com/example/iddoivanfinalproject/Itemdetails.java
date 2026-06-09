package com.example.iddoivanfinalproject; // הגדרת מיקום הקובץ בתוך תיקיות הפרויקט

// ייבוא מחלקות וספריות שדרושות להפעלת הקוד (אנדרואיד ופיירבייס)
import android.content.Intent; // מחלקה המשמשת למעבר בין מסכים באפליקציה
import android.os.Bundle; // מחלקה לשמירת מצב המסך בזמן הפתיחה
import android.view.View; // מחלקת הבסיס לכל רכיבי התצוגה באנדרואיד
import android.widget.Button; // רכיב של כפתור
import android.widget.CheckBox; // רכיב של תיבת סימון (V) - משמש אותנו להוספה להשוואה
import android.widget.ImageView; // רכיב המציג תמונה במסך
import android.widget.TextView; // רכיב המציג טקסט במסך
import android.widget.Toast; // מחלקה להצגת הודעות קופצות קצרות (פופ-אפ) בתחתית המסך

import androidx.appcompat.app.AppCompatActivity; // מחלקת האם הבסיסית שממנה יורש מסך מודרני

// ייבוא המודלים והשירותים שיצרת בפרויקט
import com.example.iddoivanfinalproject.model.Cart; // מודל המייצג פריט בעגלה
import com.example.iddoivanfinalproject.model.Compareitem; // מודל המייצג רשימת השוואה
import com.example.iddoivanfinalproject.model.Item; // מודל המייצג את המוצר הספציפי שאנחנו צופים בו
import com.example.iddoivanfinalproject.model.User; // מודל המייצג את המשתמש המחובר
import com.example.iddoivanfinalproject.services.DataBaseService; // שירות התקשורת עם מסד הנתונים (Firebase)
import com.example.iddoivanfinalproject.utils.ImageUtil; // מחלקת עזר שיצרת לטיפול בהמרת תמונות
import com.google.firebase.auth.FirebaseAuth; // מערכת ההזדהות של פיירבייס (לדעת מי מחובר)
import com.google.firebase.auth.FirebaseUser; // אובייקט של המשתמש המחובר כרגע

import java.time.LocalDate; // מחלקה לעבודה עם תאריכים
import java.time.format.DateTimeFormatter; // מחלקה לעיצוב פורמט התאריך
import java.util.ArrayList; // מחלקה של מערך דינמי (רשימה)
import java.util.List; // ממשק עבודה עם רשימות

public class Itemdetails extends AppCompatActivity { // הגדרת מחלקת המסך "פרטי מוצר"
    // הגדרת המשתנים שייצגו את הרכיבים במסך
    private TextView tvName, tvDescription, tvPrice, tvBrand, tvType, tvYear; // טקסטים לפרטי המוצר
    private ImageView ivPic; // תמונת המוצר
    private Button btnBack, btnAddToCart, btnGoToCompare, btnDeleteItem; // הכפתורים במסך
    private CheckBox cbCompare; // תיבת הסימון להשוואה
    private DataBaseService.DatabaseService databaseService; // החיבור למסד הנתונים בענן

    Compareitem compareitem = new Compareitem(); // אובייקט שישמור את רשימת ההשוואה של המשתמש
    Item currentItem; // אובייקט שישמור את פרטי המוצר שאנחנו צופים בו ממש עכשיו
    String formattedDate; // משתנה שישמור את התאריך של היום
    private String itemId = null; // משתנה שישמור את ה-ID (המזהה) של המוצר, בהתחלה ריק

    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה הראשונה שרצה כשנכנסים למסך
        super.onCreate(savedInstanceState); // הכנה בסיסית של אנדרואיד
        setContentView(R.layout.activity_itemdetails); // חיבור קובץ העיצוב XML לקוד

        formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); // שולף את התאריך של היום ומעצב אותו (יום/חודש/שנה)
        initViews(); // מפעיל פונקציה שמקשרת את המשתנים לרכיבים במסך

        itemId = getIntent().getStringExtra("ITEM_ID"); // שולף את ה-ID של המוצר שהועבר מהמסך הקודם (למשל מהחנות או מההשוואה)
        if (itemId != null) { // בודק שבאמת הועבר ID חוקי
            loadItemData(); // אם כן, מפעיל פונקציה שתמשוך את פרטי המוצר מפיירבייס
        }

        checkUserStatus(); // בודק מי מחובר עכשיו (מנהל או לקוח) כדי לדעת אילו כפתורים להציג לו
    }

    private void initViews() { // פונקציה שעושה סדר ומקשרת כל משתנה ל-ID של הרכיב ב-XML
        tvName = findViewById(R.id.tvName); // קישור שם
        tvDescription = findViewById(R.id.tvDescription); // קישור תיאור
        tvPrice = findViewById(R.id.tvPrice); // קישור מחיר
        tvBrand = findViewById(R.id.tvBrand); // קישור מותג
        tvType = findViewById(R.id.tvType); // קישור סוג/קטגוריה
        tvYear = findViewById(R.id.tvYear); // קישור שנה
        ivPic = findViewById(R.id.ivPic); // קישור תמונה

        btnAddToCart = findViewById(R.id.btnAddToCart); // קישור כפתור עגלה
        btnGoToCompare = findViewById(R.id.btnGoToCompare); // קישור כפתור מעבר להשוואה
        btnDeleteItem = findViewById(R.id.btnDeleteItem); // קישור כפתור מחיקה (למנהל)
        btnBack = findViewById(R.id.btnBack); // קישור כפתור חזרה
        cbCompare = findViewById(R.id.cbCompare); // קישור תיבת הסימון

        databaseService = DataBaseService.DatabaseService.getInstance(); // קבלת המופע לעבודה מול פיירבייס

        if (btnDeleteItem != null) { // מוודא שכפתור המחיקה קיים במסך
            btnDeleteItem.setOnClickListener(v -> deleteCurrentItem()); // קובע שבלחיצה עליו תופעל פונקציית המחיקה
        }

        if (btnGoToCompare != null) { // מוודא שכפתור מעבר להשוואה קיים
            btnGoToCompare.setOnClickListener(v -> { // קובע מה יקרה בלחיצה עליו
                Intent intent = new Intent(Itemdetails.this, CompareList.class); // מכין מעבר למסך ההשוואה
                if (currentItem != null) intent.putExtra("COMPARE_TYPE", currentItem.getType()); // שולח יחד איתו את הקטגוריה של המוצר הנוכחי
                startActivity(intent); // משגר את המעבר
            });
        }

        if (btnAddToCart != null) { // מוודא שכפתור "הוסף לעגלה" קיים
            btnAddToCart.setOnClickListener(v -> addToCart()); // קובע שלחיצה תפעיל פונקציה להוספה לעגלה
        }
    }

    private void checkUserStatus() { // פונקציה שמסתירה/מציגה כפתורים לפי סוג המשתמש (מנהל או לקוח)
        // בהתחלה מסתירים את כל הכפתורים מכולם עד שנבדוק מי מחובר
        if (btnAddToCart != null) btnAddToCart.setVisibility(View.GONE); // מעלים כפתור עגלה
        if (btnGoToCompare != null) btnGoToCompare.setVisibility(View.GONE); // מעלים כפתור השוואה
        if (cbCompare != null) cbCompare.setVisibility(View.GONE); // מעלים תיבת סימון
        if (btnDeleteItem != null) btnDeleteItem.setVisibility(View.GONE); // מעלים כפתור מחיקה

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // שולף את פרטי המשתמש המחובר מפיירבייס

        if (currentUser != null) { // אם אכן יש מישהו שמחובר לאפליקציה
            String uid = currentUser.getUid(); // לוקח את תעודת הזהות (ID) שלו
            databaseService.getUser(uid, new DataBaseService.DatabaseCallback<User>() { // מבקש מהמסד את כל הפרטים עליו (כולל האם הוא מנהל)
                @Override
                public void onCompleted(User user) { // כשהפרטים הגיעו מהמסד
                    if (user != null) { // אם מצאנו את המשתמש
                        if (user.isAdmin()) { // בודק: האם הוא מוגדר כמנהל?
                            if (btnDeleteItem != null) btnDeleteItem.setVisibility(View.VISIBLE); // אם כן, מציג לו רק את כפתור ה"מחק מוצר"
                        } else { // אם הוא לא מנהל (הוא לקוח רגיל)
                            showCustomerButtons(); // מפעיל פונקציה שמציגה לו את כפתורי העגלה וההשוואה
                        }
                    } else { // אם משום מה לא מצאנו נתונים על המשתמש
                        showCustomerButtons(); // נציג לו כפתורי לקוח ליתר ביטחון
                    }
                }
                @Override
                public void onFailed(Exception e) { // אם התקשורת נכשלה
                    showCustomerButtons(); // נציג כפתורי לקוח
                }
            });
        } else { // אם אין משתמש מחובר בכלל (אורח)
            showCustomerButtons(); // גם אורח רואה את הכפתורים (למרות שאם ילחץ 'עגלה' יקפוץ לו שהוא חייב להתחבר)
        }
    }

    private void showCustomerButtons() { // פונקציית עזר שפשוט חושפת את הכפתורים שרלוונטיים ללקוחות
        if (btnAddToCart != null) btnAddToCart.setVisibility(View.VISIBLE); // מציג "הוסף לעגלה"
        if (btnGoToCompare != null) btnGoToCompare.setVisibility(View.VISIBLE); // מציג "מעבר להשוואה"

        if (cbCompare != null) { // מוודא שתיבת ההשוואה קיימת
            cbCompare.setVisibility(View.VISIBLE); // מציג אותה
            cbCompare.setEnabled(false); // נועל אותה זמנית ללחיצות (כדי שלא ילחץ לפני שפיירבייס מסיים לחשוב וה-V יעלם)
        }

        if (btnDeleteItem != null) btnDeleteItem.setVisibility(View.GONE); // מוודא שכפתור מחיקת המוצר (של מנהלים) נשאר מוסתר
    }

    private void deleteCurrentItem() { // פונקציה למחיקת המוצר מהחנות (מיועדת למנהלים בלבד)
        if (itemId == null || itemId.isEmpty()) { // בודק שיש לנו מזהה מוצר חוקי למחוק
            Toast.makeText(this, "שגיאה: לא נמצא ID של מוצר למחיקה!", Toast.LENGTH_LONG).show(); // מקפיץ שגיאה
            return; // עוצר הכל
        }

        databaseService.deleteItem(itemId, new DataBaseService.DatabaseCallback<Void>() { // שלב 1: פקודה למחיקת המוצר ממאגר המוצרים של החנות
            @Override
            public void onCompleted(Void object) { // כשהמוצר נמחק מהחנות בהצלחה
                if (currentItem != null && currentItem.getType() != null) { // בודק שאנחנו יודעים מי המוצר שכרגע נמחק
                    databaseService.getCompareByType(currentItem.getType(), new DataBaseService.DatabaseCallback<Compareitem>() { // שלב 2: מחפש אם המוצר הזה היה שמור ברשימת השוואה של מישהו
                        @Override
                        public void onCompleted(Compareitem dbCompare) { // כשרשימת ההשוואה מגיעה מהרשת
                            if (dbCompare != null && dbCompare.getItemArrayList() != null) { // אם קיימת רשימת השוואה
                                List<Item> newList = new ArrayList<>(); // יוצר רשימה חדשה חלופית
                                boolean itemFoundInCompare = false; // מסמן אם המוצר שמחקנו כרגע בכלל נמצא בתוך רשימת ההשוואה הזו

                                for (Item i : dbCompare.getItemArrayList()) { // עובר על המוצרים בתוך רשימת ההשוואה
                                    boolean isSameId = i.getId() != null && i.getId().equals(itemId); // בודק התאמה לפי ID
                                    boolean isSameName = i.getName() != null && currentItem.getName() != null && i.getName().equals(currentItem.getName()); // בודק התאמה לפי שם

                                    if (isSameId || isSameName) { // אם זה המוצר שלנו
                                        itemFoundInCompare = true; // סימן שמצאנו אותו
                                    } else {
                                        newList.add(i); // כל שאר המוצרים שלא נמחקו עוברים לרשימה החדשה
                                    }
                                }

                                if (itemFoundInCompare) { // אם המוצר באמת היה שם וצריך למחוק אותו גם משם
                                    dbCompare.getItemArrayList().clear(); // מרוקן את הישנה
                                    dbCompare.getItemArrayList().addAll(newList); // דוחף את הרשימה החדשה (הנקייה מהמוצר שנמחק)

                                    databaseService.updateCompareList(dbCompare, new DataBaseService.DatabaseCallback<Void>() { // שלב 3: מעדכן את פיירבייס ברשימת ההשוואה הנקייה
                                        @Override
                                        public void onCompleted(Void o) { // כשהעדכון הסתיים
                                            Toast.makeText(Itemdetails.this, "המוצר נמחק מהחנות וגם מההשוואה!", Toast.LENGTH_LONG).show(); // הודעת הצלחה כפולה
                                            finish(); // סוגר את עמוד המוצר וחוזר לחנות
                                        }
                                        @Override
                                        public void onFailed(Exception e) { // אם נכשל
                                            Toast.makeText(Itemdetails.this, "המוצר נמחק, אך שגיאה בעדכון ההשוואה", Toast.LENGTH_LONG).show(); // מודיע על חצי הצלחה
                                            finish(); // סוגר מסך
                                        }
                                    });
                                } else { // אם המוצר בכלל לא היה בהשוואה מלכתחילה
                                    Toast.makeText(Itemdetails.this, "המוצר נמחק בהצלחה!", Toast.LENGTH_SHORT).show(); // מודיע על הצלחה
                                    finish(); // סוגר מסך
                                }
                            } else { // אם אין רשימת השוואה לקטגוריה הזו בכלל ב-DB
                                Toast.makeText(Itemdetails.this, "נמחק בהצלחה מ-Firebase!", Toast.LENGTH_SHORT).show(); // מודיע על הצלחה
                                finish(); // סוגר מסך
                            }
                        }
                        @Override
                        public void onFailed(Exception e) { // תקלה בגישה ל-Compare
                            Toast.makeText(Itemdetails.this, "נמחק מהחנות. שגיאה בגישה להשוואה.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } else { // אם חסרים נתונים על הקטגוריה
                    Toast.makeText(Itemdetails.this, "נמחק בהצלחה מ-Firebase!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailed(Exception e) { // אם מחיקת המוצר מהחנות נכשלה
                Toast.makeText(Itemdetails.this, "שגיאה במחיקת המוצר: " + e.getMessage(), Toast.LENGTH_LONG).show(); // מקפיץ שגיאה
            }
        });
    }

    private void loadItemData() { // פונקציה שמושכת את נתוני המוצר כדי להציג אותם במסך
        databaseService.getItemById(itemId, new DataBaseService.DatabaseCallback<Item>() { // פונה למסד ומבקש מוצר לפי ה-ID שלו
            @Override
            public void onCompleted(Item item) { // כשהמוצר מתקבל
                if (item != null) { // אם המוצר נמצא במערכת
                    currentItem = item; // שומר אותו במשתנה כדי שנוכל לעבוד איתו בשאר המסך
                    tvName.setText(item.getName()); // מציב את השם בטקסט
                    tvDescription.setText(item.getDetails()); // מציב את התיאור
                    tvPrice.setText(String.valueOf(item.getPrice())); // מציב את המחיר
                    tvBrand.setText("Brand: " + item.getBrand()); // מציב מותג
                    tvType.setText("Type: " + item.getType()); // מציב סוג
                    tvYear.setText("Year: " + item.getYear()); // מציב שנה
                    if (item.getPic() != null) ivPic.setImageBitmap(ImageUtil.convertFrom64base(item.getPic())); // ממיר את התמונה ומציג אותה

                    setupCompareLogic(); // מפעיל פונקציה שבודקת האם המוצר הזה נמצא כרגע ברשימת ההשוואה של המשתמש (כדי לסמן V)
                }
            }
            @Override
            public void onFailed(Exception e) {} // אם הייתה תקלה מתעלם
        });
    }

    private void setupCompareLogic() { // פונקציה שמטפלת במנגנון הסימון של "הוסף להשוואה"
        databaseService.getCompareByType(currentItem.getType(), new DataBaseService.DatabaseCallback<Compareitem>() { // מבקש מהמסד את רשימת ההשוואה של המשתמש
            @Override
            public void onCompleted(Compareitem dbCompare) { // כשהרשימה מגיעה
                if (dbCompare != null) { // אם למשתמש יש רשימת השוואה בענן
                    compareitem = dbCompare; // שומר אותה במשתנה שלנו

                    databaseService.getAllItems(new DataBaseService.DatabaseCallback<List<Item>>() { // מבקש את כל מוצרי החנות (בשביל מנגנון הריפוי שדיברנו עליו)
                        @Override
                        public void onCompleted(List<Item> storeItems) { // כשהחנות מגיעה
                            if (storeItems != null && compareitem.getItemArrayList() != null) { // אם הכל תקין
                                List<Item> validItems = new ArrayList<>(); // רשימה זמנית למוצרים תקינים שעדיין קיימים

                                for (Item cItem : compareitem.getItemArrayList()) { // עובר על המוצרים שברשימת ההשוואה
                                    boolean exists = false;
                                    for (Item sItem : storeItems) { // עובר על המוצרים שבחנות
                                        if (cItem.getId() != null && sItem.getId() != null && cItem.getId().equals(sItem.getId())) { // בודק התאמה
                                            exists = true; // סימן שהמוצר קיים בחנות ולא נמחק
                                            break;
                                        }
                                    }
                                    if (exists) validItems.add(cItem); // שומר רק את מה שקיים וזורק את "רוחות הרפאים"
                                }

                                if (validItems.size() != compareitem.getItemArrayList().size()) { // אם זרקנו משהו, מעדכנים את הרשימה בענן
                                    compareitem.getItemArrayList().clear();
                                    compareitem.getItemArrayList().addAll(validItems);

                                    databaseService.updateCompareList(compareitem, new DataBaseService.DatabaseCallback<Void>() {
                                        @Override public void onCompleted(Void o) {}
                                        @Override public void onFailed(Exception e) {}
                                    });
                                }
                            }

                            checkIfItemInCompare(); // בודק האם המוצר הספציפי שלנו ברשימה ומסמן V אם כן
                            setCheckboxListener(); // מפעיל את מאזין הלחיצות של תיבת הסימון
                            if (cbCompare != null) cbCompare.setEnabled(true); // פיירבייס סיים לעבוד! פותח את נעילת תיבת הסימון (עכשיו אפשר ללחוץ)
                        }

                        @Override
                        public void onFailed(Exception e) { // במקרה של שגיאה עם החנות מפעילים בלי ריפוי
                            checkIfItemInCompare();
                            setCheckboxListener();
                            if (cbCompare != null) cbCompare.setEnabled(true);
                        }
                    });

                } else { // אם למשתמש עדיין אין רשימת השוואה בכלל ב-DB
                    compareitem = new Compareitem(); // יוצר רשימה חדשה לגמרי
                    compareitem.setId(databaseService.generateCompareId()); // מגריל לה מזהה ID
                    setCheckboxListener(); // מפעיל את מאזין הלחיצות
                    if (cbCompare != null) cbCompare.setEnabled(true); // פותח את הנעילה של תיבת הסימון למשתמש
                }
            }
            @Override
            public void onFailed(Exception e) { // במקרה של תקלה כוללת פותח את הנעילה
                if (cbCompare != null) cbCompare.setEnabled(true);
            }
        });
    }

    private void checkIfItemInCompare() { // פונקציה שעוברת על הרשימה ובודקת אם המוצר שלנו כבר שם
        if (compareitem.getItemArrayList() != null) { // אם הרשימה לא ריקה
            for (Item i : compareitem.getItemArrayList()) { // עובר פריט-פריט
                if (i.getId() != null && currentItem.getId() != null && i.getId().equals(currentItem.getId())) { // אם מצאנו התאמה ב-ID
                    cbCompare.setOnCheckedChangeListener(null); // מנתק את המאזין לשנייה (כדי שלא ישמור בטעות בענן סתם סימון)
                    cbCompare.setChecked(true); // שם 'V' ויזואלית על המסך
                    break; // עוצר את הלולאה כי כבר מצאנו
                }
            }
        }
    }

    private void setCheckboxListener() { // פונקציה שמגדירה מה יקרה כשמשתמש לוחץ על תיבת הסימון (מוסיף/מסיר השוואה)
        cbCompare.setOnCheckedChangeListener((buttonView, isChecked) -> { // מתחיל להאזין ללחיצות על התיבה
            if (currentItem == null) return; // הגנה: אם אין מוצר אי אפשר לעשות כלום
            if (compareitem.getItemArrayList() == null) compareitem.setItemArrayList(new ArrayList<>()); // אם הרשימה ריקה, יוצר מערך חדש

            if (isChecked) { // אם המשתמש לחץ וסימן V (רוצה להוסיף)
                if (compareitem.getItemArrayList().size() >= 3) { // בודק האם כבר יש 3 מוצרים בהשוואה
                    Toast.makeText(Itemdetails.this, "ניתן להוסיף עד 3 פריטים להשוואה", Toast.LENGTH_SHORT).show(); // אם כן, מודיע לו שאסור
                    cbCompare.setOnCheckedChangeListener(null); // מנתק מאזין
                    cbCompare.setChecked(false); // מוריד את ה-V חזרה כי ההוספה נכשלה
                    setCheckboxListener(); // מחזיר מאזין
                    return; // עוצר את ההוספה
                }

                compareitem.getItemArrayList().add(currentItem); // אם מותר להוסיף: מוסיף את המוצר הנוכחי לאובייקט ההשוואה
                compareitem.setType(currentItem.getType()); // שומר את הקטגוריה
                compareitem.setDate(formattedDate); // שומר תאריך עדכון
            } else { // אם המשתמש לחץ כדי להוריד את ה-V (רוצה לבטל)
                compareitem.getItemArrayList().removeIf(i -> i.getId().equals(currentItem.getId())); // מסיר מהרשימה באובייקט את הפריט עם ה-ID של המוצר הנוכחי
            }

            databaseService.updateCompareList(compareitem, new DataBaseService.DatabaseCallback<Void>() { // מעדכן את האובייקט החדש במסד הנתונים בענן
                @Override public void onCompleted(Void o) {} // בסיום אין צורך לעשות משהו מיוחד
                @Override public void onFailed(Exception e) {} // במקרה תקלה מתעלמים
            });
        });
    }

    private void addToCart() { // פונקציה להוספת המוצר לעגלת הקניות של המשתמש
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // שולף את המשתמש
        if (user == null) { // בודק אם הוא מחובר
            Toast.makeText(this, "עליך להתחבר קודם", Toast.LENGTH_SHORT).show(); // דורש התחברות מאורחים
            return; // עוצר
        }

        if (currentItem != null) { // מוודא שהמוצר נטען כראוי
            databaseService.getCartList(user.getUid(), new DataBaseService.DatabaseCallback<List<Cart>>() { // מושך את העגלה של המשתמש
                @Override
                public void onCompleted(List<Cart> carts) { // כשהעגלה מגיעה
                    Cart existingCartItem = null; // משתנה שישמור אם מצאנו את המוצר כבר בעגלה

                    if (carts != null) { // אם יש דברים בעגלה
                        for (Cart cart : carts) { // עובר על המוצרים בעגלה
                            if (cart.getName() != null && cart.getName().equals(currentItem.getName())) { // אם יש מוצר בעגלה עם אותו שם
                                existingCartItem = cart; // מסמן שמצאנו
                                break;
                            }
                        }
                    }

                    if (existingCartItem != null) { // תרחיש 1: המוצר כבר קיים בעגלה!
                        existingCartItem.setQuantity(existingCartItem.getQuantity() + 1); // במקום להוסיף פעמיים, אנחנו פשוט מוסיפים עוד 1 לכמות

                        databaseService.createNewCart(existingCartItem, new DataBaseService.DatabaseCallback<Void>() { // מעדכן את הרשומה במסד
                            @Override public void onCompleted(Void object) {
                                Toast.makeText(Itemdetails.this, "הכמות עודכנה בעגלה!", Toast.LENGTH_SHORT).show(); // הודעת הצלחה
                            }
                            @Override public void onFailed(Exception e) {}
                        });
                    } else { // תרחיש 2: המוצר עדיין לא קיים בעגלה
                        String cartId = databaseService.generateCartId(); // יצירת ID חדש לפריט בעגלה
                        Cart cartItem = new Cart(currentItem.getName(), currentItem.getPrice(), 1, cartId, user.getUid(), currentItem.getPic()); // יוצר אובייקט עם כמות התחלתית 1

                        databaseService.createNewCart(cartItem, new DataBaseService.DatabaseCallback<Void>() { // שולח למסד
                            @Override public void onCompleted(Void object) {
                                Toast.makeText(Itemdetails.this, "נוסף לעגלה שלך!", Toast.LENGTH_SHORT).show(); // הודעת הצלחה
                            }
                            @Override public void onFailed(Exception e) {}
                        });
                    }
                }
                @Override public void onFailed(Exception e) {}
            });
        }
    }

    // פונקציה שכפתור ה"חזור לחנות" מפעיל בממשק
    public void onBack(View view) {
        // יצירת כוונה (Intent) לעבור במפורש לעמוד החנות (Items)
        Intent intent = new Intent(Itemdetails.this, Items.class);

        // הגדרה חשובה: אם עמוד החנות כבר פתוח אי שם ברקע במכשיר, במקום לפתוח מעליו עוד עמוד חנות חדש -
        // אנדרואיד ינקה את כל המסכים שמעליו ויחזיר אותנו לחנות המקורית, כך שהאפליקציה לא "תתנפח".
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent); // הפעלת המעבר לחנות
        finish(); // סגירת עמוד פרטי המוצר הנוכחי ומחיקתו מהזיכרון
    }
}