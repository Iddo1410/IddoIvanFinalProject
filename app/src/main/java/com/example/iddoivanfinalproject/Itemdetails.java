package com.example.iddoivanfinalproject; // הגדרת מיקום הקובץ בחבילת הפרויקט

// ייבוא מחלקות וספריות שדרושות להפעלת הקוד
import android.content.Intent; // מחלקה למעבר בין מסכים
import android.os.Bundle; // מחלקה לשמירת מצב המסך
import android.view.View; // מחלקת הבסיס לכל רכיבי התצוגה באנדרואיד
import android.widget.Button; // רכיב כפתור
import android.widget.CheckBox; // רכיב תיבת סימון (וי)
import android.widget.ImageView; // רכיב להצגת תמונה
import android.widget.TextView; // רכיב להצגת טקסט
import android.widget.Toast; // רכיב להצגת הודעות קופצות קצרות (פופ-אפ)

import androidx.appcompat.app.AppCompatActivity; // מחלקת האם למסכי אנדרואיד

// ייבוא מודלים ושירותים מתוך הפרויקט שלך
import com.example.iddoivanfinalproject.model.Cart; // מודל עגלת קניות
import com.example.iddoivanfinalproject.model.Compareitem; // מודל רשימת השוואה
import com.example.iddoivanfinalproject.model.Item; // מודל פריט/מוצר
import com.example.iddoivanfinalproject.model.User; // מודל משתמש
import com.example.iddoivanfinalproject.services.DataBaseService; // שירות התקשורת עם מסד הנתונים
import com.example.iddoivanfinalproject.utils.ImageUtil; // מחלקת עזר לטיפול בתמונות
import com.google.firebase.auth.FirebaseAuth; // מערכת אימות המשתמשים של Firebase
import com.google.firebase.auth.FirebaseUser; // מודל המייצג משתמש מחובר ב-Firebase

// ספריות עזר לתאריכים ורשימות
import java.time.LocalDate; // מחלקה שמייצגת תאריך (ללא שעה)
import java.time.format.DateTimeFormatter; // מחלקה לעיצוב פורמט התאריך
import java.util.ArrayList; // מחלקה למערך דינמי (רשימה)
import java.util.List; // ממשק רשימה ב-Java

public class Itemdetails extends AppCompatActivity {
    // הגדרת משתנים פרטיים לרכיבי התצוגה במסך
    private TextView tvName, tvDescription, tvPrice, tvBrand, tvType, tvYear; // שדות טקסט
    private ImageView ivPic; // תמונת המוצר
    private Button btnBack, btnAddToCart, btnGoToCompare, btnDeleteItem; // כפתורים שונים
    private CheckBox cbCompare; // תיבת סימון להוספה/הסרה מהשוואה
    private DataBaseService.DatabaseService databaseService; // משתנה להתקשרות מול מסד הנתונים

    Compareitem compareitem = new Compareitem(); // אובייקט שישמור את רשימת ההשוואה הנוכחית של הקטגוריה
    Item currentItem; // אובייקט שישמור את המוצר הנוכחי שאנחנו צופים בו כעת
    String formattedDate; // מחרוזת שתשמור את התאריך של היום
    private String itemId = null; // משתנה שישמור את המזהה (ID) של המוצר, בהתחלה מוגדר כריק (null)

    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה המרכזית שמופעלת כשנכנסים למסך
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemdetails); // חיבור קובץ העיצוב (XML) של המסך

        // שמירת התאריך הנוכחי בפורמט של יום/חודש/שנה
        formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        initViews(); // קריאה לפונקציה שמקשרת את המשתנים לרכיבים במסך

        // משיכת המזהה של המוצר (ITEM_ID) מהמסך הקודם שממנו עברנו לכאן
        itemId = getIntent().getStringExtra("ITEM_ID");
        if (itemId != null) { // אם אכן התקבל מזהה תקין
            loadItemData(); // מפעיל את הפונקציה שטוענת את פרטי המוצר מהמסד
        }

        checkUserStatus(); // קריאה לפונקציה שבודקת מי המשתמש (מנהל/לקוח) ומעדכנת את הכפתורים בהתאם
    }

    private void initViews() { // פונקציה לקישור משתני התצוגה לקובץ ה-XML באמצעות ה-ID שלהם
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrice = findViewById(R.id.tvPrice);
        tvBrand = findViewById(R.id.tvBrand);
        tvType = findViewById(R.id.tvType);
        tvYear = findViewById(R.id.tvYear);
        ivPic = findViewById(R.id.ivPic);

        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnGoToCompare = findViewById(R.id.btnGoToCompare);
        btnDeleteItem = findViewById(R.id.btnDeleteItem);
        btnBack = findViewById(R.id.btnBack);
        cbCompare = findViewById(R.id.cbCompare);

        databaseService = DataBaseService.DatabaseService.getInstance(); // קבלת מופע (חיבור) למסד הנתונים

        // הגדרת פעולות בעת לחיצה על הכפתורים
        if (btnDeleteItem != null) { // מוודא שהכפתור קיים
            btnDeleteItem.setOnClickListener(v -> deleteCurrentItem()); // בלחיצה, קורא לפונקציית המחיקה
        }

        if (btnGoToCompare != null) {
            btnGoToCompare.setOnClickListener(v -> { // בלחיצה על "מעבר להשוואה"
                Intent intent = new Intent(Itemdetails.this, CompareList.class); // יוצר מעבר למסך ההשוואה
                // מעביר למסך ההשוואה את סוג המוצר הנוכחי (כדי שיפתח ישר על הקטגוריה המתאימה)
                if (currentItem != null) intent.putExtra("COMPARE_TYPE", currentItem.getType());
                startActivity(intent); // מתחיל את המעבר
            });
        }

        if (btnAddToCart != null) {
            btnAddToCart.setOnClickListener(v -> addToCart()); // בלחיצה על "הוסף לעגלה" יפעיל את הפונקציה המתאימה
        }
    }

    private void checkUserStatus() { // פונקציה שבודקת הרשאות ומתאימה את המסך
        // כברירת מחדל, מסתירים את כל הכפתורים עד שנדע מי המשתמש
        if (btnAddToCart != null) btnAddToCart.setVisibility(View.GONE);
        if (btnGoToCompare != null) btnGoToCompare.setVisibility(View.GONE);
        if (cbCompare != null) cbCompare.setVisibility(View.GONE);
        if (btnDeleteItem != null) btnDeleteItem.setVisibility(View.GONE);

        // שולף את המשתמש הנוכחי שמחובר למערכת
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) { // אם המשתמש מחובר
            String uid = currentUser.getUid(); // מקבל את המזהה הייחודי שלו
            // מבקש את פרטי המשתמש ממסד הנתונים
            databaseService.getUser(uid, new DataBaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    if (user != null) { // אם נמצאו פרטים
                        if (user.isAdmin()) { // בודק האם המשתמש מוגדר כמנהל
                            // אם כן - חושף רק את כפתור המחיקה
                            if (btnDeleteItem != null) btnDeleteItem.setVisibility(View.VISIBLE);
                        } else { // אם זה משתמש רגיל (לא מנהל)
                            showCustomerButtons(); // מפעיל פונקציה שחושפת כפתורי לקוח
                        }
                    } else { // אם לא נמצאו פרטי משתמש (למקרה של תקלה), נציג כפתורי לקוח ליתר ביטחון
                        showCustomerButtons();
                    }
                }
                @Override
                public void onFailed(Exception e) { // אם הייתה שגיאה בתקשורת נציג כפתורי לקוח
                    showCustomerButtons();
                }
            });
        } else { // אם אף אחד לא מחובר בכלל
            showCustomerButtons(); // נציג כפתורי לקוח
        }
    }

    private void showCustomerButtons() { // פונקציית עזר שחושפת את כל הכפתורים שרלוונטיים ללקוחות
        if (btnAddToCart != null) btnAddToCart.setVisibility(View.VISIBLE); // הצגת "הוסף לעגלה"
        if (btnGoToCompare != null) btnGoToCompare.setVisibility(View.VISIBLE); // הצגת "מעבר להשוואה"
        if (cbCompare != null) cbCompare.setVisibility(View.VISIBLE); // הצגת תיבת הסימון של השוואה
        if (btnDeleteItem != null) btnDeleteItem.setVisibility(View.GONE); // וידוא שכפתור המחיקה (של המנהל) מוסתר
    }

    private void deleteCurrentItem() { // פונקציה למחיקת המוצר (רלוונטי רק למנהלים)
        if (itemId == null || itemId.isEmpty()) { // בדיקה שיש לנו מזהה מוצר חוקי למחוק
            Toast.makeText(this, "שגיאה: לא נמצא ID של מוצר למחיקה!", Toast.LENGTH_LONG).show();
            return; // עצירת הפעולה אם אין מזהה
        }

        // שלב 1: מחיקת המוצר המרכזי מתוך מאגר המוצרים בחנות
        databaseService.deleteItem(itemId, new DataBaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) { // אם המוצר נמחק מהחנות בהצלחה

                // שלב 2: ניקוי המוצר מתוך רשימת ההשוואה (אם מישהו הוסיף אותו לשם)
                if (currentItem != null && currentItem.getType() != null) {
                    databaseService.getCompareByType(currentItem.getType(), new DataBaseService.DatabaseCallback<Compareitem>() {
                        @Override
                        public void onCompleted(Compareitem dbCompare) { // מקבל את רשימת ההשוואה של הקטגוריה שלו
                            if (dbCompare != null && dbCompare.getItemArrayList() != null) {
                                List<Item> newList = new ArrayList<>(); // רשימה חדשה ונקייה
                                boolean itemFoundInCompare = false; // דגל שיסמן אם מצאנו את המוצר שלנו בהשוואה

                                // לולאה שעוברת על המוצרים שברשימת ההשוואה
                                for (Item i : dbCompare.getItemArrayList()) {
                                    // בודק התאמה לפי מזהה (ID) או לפי שם, למקרה שה-ID השתנה
                                    boolean isSameId = i.getId() != null && i.getId().equals(itemId);
                                    boolean isSameName = i.getName() != null && currentItem.getName() != null && i.getName().equals(currentItem.getName());

                                    if (isSameId || isSameName) { // אם מצאנו את המוצר שנמחק
                                        itemFoundInCompare = true; // מסמנים שמצאנו
                                    } else {
                                        newList.add(i); // שומרים את שאר המוצרים שלא נמחקו לתוך הרשימה החדשה
                                    }
                                }

                                if (itemFoundInCompare) { // אם אכן נדרש ניקוי
                                    dbCompare.getItemArrayList().clear(); // מרוקנים את הרשימה הישנה
                                    dbCompare.getItemArrayList().addAll(newList); // מכניסים את הרשימה המעודכנת (בלי המוצר שנמחק)

                                    // שומרים את רשימת ההשוואה המעודכנת חזרה ב-Firebase
                                    databaseService.updateCompareList(dbCompare, new DataBaseService.DatabaseCallback<Void>() {
                                        @Override
                                        public void onCompleted(Void o) {
                                            Toast.makeText(Itemdetails.this, "המוצר נמחק מהחנות וגם מההשוואה!", Toast.LENGTH_LONG).show();
                                            finish(); // סוגרים את המסך וחוזרים אחורה
                                        }
                                        @Override
                                        public void onFailed(Exception e) {
                                            Toast.makeText(Itemdetails.this, "המוצר נמחק, אך שגיאה בעדכון ההשוואה", Toast.LENGTH_LONG).show();
                                            finish(); // סוגרים את המסך גם במקרה של שגיאה חלקית
                                        }
                                    });
                                } else { // אם המוצר ממילא לא היה ברשימת ההשוואה
                                    Toast.makeText(Itemdetails.this, "המוצר נמחק בהצלחה!", Toast.LENGTH_SHORT).show();
                                    finish(); // סוגר מסך
                                }
                            } else { // אם אין רשימת השוואה לקטגוריה הזו בכלל
                                Toast.makeText(Itemdetails.this, "נמחק בהצלחה מ-Firebase!", Toast.LENGTH_SHORT).show();
                                finish(); // סוגר מסך
                            }
                        }
                        @Override
                        public void onFailed(Exception e) { // אם שגיאה בקריאת ההשוואה, עדיין המוצר נמחק מהחנות
                            Toast.makeText(Itemdetails.this, "נמחק מהחנות. שגיאה בגישה להשוואה.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } else { // אם חסר סוג המוצר ואי אפשר למצוא את ההשוואה
                    Toast.makeText(Itemdetails.this, "נמחק בהצלחה מ-Firebase!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailed(Exception e) { // אם המחיקה מהחנות עצמה נכשלה
                Toast.makeText(Itemdetails.this, "שגיאה במחיקת המוצר: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadItemData() { // פונקציה ששולפת את נתוני המוצר כדי להציג אותם במסך
        databaseService.getItemById(itemId, new DataBaseService.DatabaseCallback<Item>() { // פנייה למסד לפי ה-ID
            @Override
            public void onCompleted(Item item) { // אם נמצא המוצר
                if (item != null) {
                    currentItem = item; // שומרים את האובייקט המלא במשתנה המחלקה
                    // מאכלסים את כל רכיבי הטקסט עם המידע שהגיע מהשרת
                    tvName.setText(item.getName());
                    tvDescription.setText(item.getDetails());
                    tvPrice.setText(String.valueOf(item.getPrice()));
                    tvBrand.setText("Brand: " + item.getBrand());
                    tvType.setText("Type: " + item.getType());
                    tvYear.setText("Year: " + item.getYear());
                    // ממירים את התמונה מפורמט טקסטואלי (Base64) חזרה לתמונה ומציגים אותה
                    if (item.getPic() != null) ivPic.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));

                    setupCompareLogic(); // קריאה לפונקציה שמגדירה את מצב תיבת הסימון (אם המוצר כבר בהשוואה או לא)
                }
            }
            @Override
            public void onFailed(Exception e) {} // התעלמות אם נכשל
        });
    }

    private void setupCompareLogic() { // פונקציה שמטפלת במנגנון ההשוואה (מופעלת לאחר טעינת המוצר)
        // מבקשים מהמסד את רשימת ההשוואה של הקטגוריה הרלוונטית
        databaseService.getCompareByType(currentItem.getType(), new DataBaseService.DatabaseCallback<Compareitem>() {
            @Override
            public void onCompleted(Compareitem dbCompare) { // כשהרשימה חוזרת
                if (dbCompare != null) { // אם כבר קיימת רשימת השוואה לקטגוריה הזו
                    compareitem = dbCompare; // נשמור אותה במשתנה שלנו

                    // --- מנגנון הריפוי: מנקה פריטי "רפאים" מ-Firebase לפני שהוא מאפשר למשתמש ללחוץ על הצ'קבוקס ---
                    databaseService.getAllItems(new DataBaseService.DatabaseCallback<List<Item>>() { // מושכים את כל פריטי החנות
                        @Override
                        public void onCompleted(List<Item> storeItems) {
                            if (storeItems != null && compareitem.getItemArrayList() != null) {
                                List<Item> validItems = new ArrayList<>(); // רשימת פריטים שעדיין חוקיים (קיימים בחנות)

                                // עוברים על פריטי ההשוואה ובודקים אם הם עדיין קיימים במאגר הכללי של החנות
                                for (Item cItem : compareitem.getItemArrayList()) {
                                    boolean exists = false;
                                    for (Item sItem : storeItems) {
                                        if (cItem.getId() != null && sItem.getId() != null && cItem.getId().equals(sItem.getId())) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (exists) validItems.add(cItem); // רק מה שקיים נשמר
                                }

                                // אם מצאנו שיש חוסר התאמה (יש מוצרים בהשוואה שכבר נמחקו מהחנות)
                                if (validItems.size() != compareitem.getItemArrayList().size()) {
                                    compareitem.getItemArrayList().clear();
                                    compareitem.getItemArrayList().addAll(validItems); // מעדכנים לרשימה הנקייה

                                    // שומרים את הניקוי ב-Firebase
                                    databaseService.updateCompareList(compareitem, new DataBaseService.DatabaseCallback<Void>() {
                                        @Override public void onCompleted(Void o) {}
                                        @Override public void onFailed(Exception e) {}
                                    });
                                }
                            }

                            checkIfItemInCompare(); // בודק האם לסמן וי בתיבת ההשוואה
                            setCheckboxListener(); // מגדיר את מאזין הלחיצות לתיבה
                        }

                        @Override
                        public void onFailed(Exception e) { // אם נכשל המשיכה מהחנות, ממשיכים כרגיל עם מה שיש
                            checkIfItemInCompare();
                            setCheckboxListener();
                        }
                    });

                } else { // אם לא קיימת בכלל רשימת השוואה לסוג הזה
                    compareitem = new Compareitem(); // יוצרים אובייקט חדש
                    compareitem.setId(databaseService.generateCompareId()); // מגרילים לו מזהה חדש למסד
                    setCheckboxListener(); // מגדירים את התיבה
                }
            }
            @Override
            public void onFailed(Exception e) {}
        });
    }

    private void checkIfItemInCompare() { // פונקציה שבודקת האם המוצר הספציפי הזה כבר נמצא ברשימת ההשוואה
        if (compareitem.getItemArrayList() != null) {
            for (Item i : compareitem.getItemArrayList()) { // לולאה שעוברת על הרשימה
                if (i.getId().equals(currentItem.getId())) { // אם יש התאמה ב-ID
                    cbCompare.setOnCheckedChangeListener(null); // מכבה רגע את המאזין כדי שלא יופעל בטעות
                    cbCompare.setChecked(true); // מסמן אוטומטית וי (V) בתיבה במסך
                    break; // עוצר את הלולאה
                }
            }
        }
    }

    private void setCheckboxListener() { // פונקציה שמגדירה מה יקרה כאשר המשתמש לוחץ על תיבת הסימון של ההשוואה
        cbCompare.setOnCheckedChangeListener((buttonView, isChecked) -> { // מאזין לשינוי מצב התיבה
            if (currentItem == null) return; // הגנה: אם טרם נטען המוצר, אי אפשר להוסיף אותו
            if (compareitem.getItemArrayList() == null) compareitem.setItemArrayList(new ArrayList<>()); // יוצר רשימה ריקה אם היא לא קיימת עדיין

            if (isChecked) { // אם המשתמש סימן V (מעוניין להוסיף להשוואה)
                if (compareitem.getItemArrayList().size() >= 3) { // בדיקה: יש הגבלה לעד 3 מוצרים בהשוואה
                    Toast.makeText(Itemdetails.this, "ניתן להוסיף עד 3 פריטים להשוואה", Toast.LENGTH_SHORT).show(); // הודעת שגיאה
                    cbCompare.setOnCheckedChangeListener(null); // ניתוק זמני של המאזין
                    cbCompare.setChecked(false); // ביטול הסימון במסך בחזרה
                    setCheckboxListener(); // חיבור המאזין מחדש
                    return; // עצירת הפונקציה (לא נוסיף למסד)
                }

                compareitem.getItemArrayList().add(currentItem); // מוסיפים את המוצר לרשימת ההשוואה באובייקט שלנו
                compareitem.setType(currentItem.getType()); // מוודאים שהסוג מעודכן
                compareitem.setDate(formattedDate); // מעדכנים את תאריך ההשוואה
            } else { // אם המשתמש הוריד את ה-V (ביטל השוואה)
                // עובר על הרשימה ומסיר את המוצר שה-ID שלו שווה למוצר שאנחנו צופים בו כרגע
                compareitem.getItemArrayList().removeIf(i -> i.getId().equals(currentItem.getId()));
            }
            // סיום השינוי: שומרים את האובייקט המעודכן בתוך מסד הנתונים בענן (Firebase)
            databaseService.updateCompareList(compareitem, new DataBaseService.DatabaseCallback<Void>() {
                @Override public void onCompleted(Void o) {}
                @Override public void onFailed(Exception e) {}
            });
        });
    }

    private void addToCart() { // פונקציה המוסיפה את המוצר לעגלת הקניות של המשתמש המחובר
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // משיכת פרטי המשתמש מהאימות
        if (user == null) { // בדיקה האם יש בכלל משתמש שמחובר לאפליקציה
            Toast.makeText(this, "עליך להתחבר קודם", Toast.LENGTH_SHORT).show(); // דרישת התחברות
            return; // עצירת הפונקציה
        }

        if (currentItem != null) { // מוודא שפרטי המוצר נטענו בהצלחה למסך
            // מושך את רשימת עגלת הקניות של המשתמש הנוכחי מתוך ה-DB
            databaseService.getCartList(user.getUid(), new DataBaseService.DatabaseCallback<List<Cart>>() {
                @Override
                public void onCompleted(List<Cart> carts) { // כאשר רשימת העגלה התקבלה
                    Cart existingCartItem = null; // משתנה שיבדוק האם המוצר הזה *כבר* קיים בעגלה

                    if (carts != null) { // אם העגלה לא ריקה
                        for (Cart cart : carts) { // לולאה העוברת על כל מוצרי העגלה
                            if (cart.getName() != null && cart.getName().equals(currentItem.getName())) { // בדיקת התאמה לפי שם המוצר
                                existingCartItem = cart; // אם נמצא, נשמור את האובייקט שלו
                                break; // עוצרים את הלולאה
                            }
                        }
                    }

                    if (existingCartItem != null) { // תרחיש 1: המוצר *כבר נמצא* בעגלה
                        // פשוט מגדילים את כמות הפריט ב-1 (במקום ליצור פריט כפול)
                        existingCartItem.setQuantity(existingCartItem.getQuantity() + 1);

                        // מעדכנים את השינוי ב-Firebase
                        databaseService.createNewCart(existingCartItem, new DataBaseService.DatabaseCallback<Void>() {
                            @Override public void onCompleted(Void object) {
                                Toast.makeText(Itemdetails.this, "הכמות עודכנה בעגלה!", Toast.LENGTH_SHORT).show(); // הודעה למשתמש
                            }
                            @Override public void onFailed(Exception e) {}
                        });
                    } else { // תרחיש 2: המוצר *לא* נמצא כרגע בעגלה של המשתמש
                        String cartId = databaseService.generateCartId(); // יצירת מזהה ID חדש וייחודי לעגלה
                        // יצירת אובייקט "פריט בעגלה" חדש, עם כמות התחלתית של 1
                        Cart cartItem = new Cart(currentItem.getName(), currentItem.getPrice(), 1, cartId, user.getUid(), currentItem.getPic());

                        // שמירת האובייקט החדש בתוך מסד הנתונים
                        databaseService.createNewCart(cartItem, new DataBaseService.DatabaseCallback<Void>() {
                            @Override public void onCompleted(Void object) {
                                Toast.makeText(Itemdetails.this, "נוסף לעגלה שלך!", Toast.LENGTH_SHORT).show(); // הודעה למשתמש
                            }
                            @Override public void onFailed(Exception e) {}
                        });
                    }
                }
                @Override public void onFailed(Exception e) {}
            });
        }
    }

    public void onBack(View view) { finish(); } // פונקציה פשוטה הסוגרת את המסך (finish) שמקושרת מה-XML
}