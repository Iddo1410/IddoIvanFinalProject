package com.example.iddoivanfinalproject; // מגדיר את מיקום הקובץ בחבילת הפרויקט

// ייבוא כל המחלקות הנדרשות מתוך ספריות אנדרואיד ומהפרויקט
import android.content.DialogInterface; // מחלקה לטיפול בלחיצות בתוך חלונות קופצים (דיאלוגים)
import android.content.Intent; // הוספנו: מחלקה למעבר בין מסכים
import android.os.Bundle; // מחלקה לשמירת מצב המסך בזמן פתיחה/סגירה
import android.view.View; // מחלקת הבסיס לכל הרכיבים שרואים במסך
import android.widget.Button; // מחלקה המייצגת כפתור לחיץ
import android.widget.TextView; // מחלקה המייצגת טקסט שמוצג על המסך
import android.widget.Toast; // מחלקה המציגה הודעות קופצות קצרות בתחתית המסך

import androidx.appcompat.app.AlertDialog; // מחלקה ליצירת חלונות קופצים למשתמש
import androidx.appcompat.app.AppCompatActivity; // מחלקת האם של מסכי אנדרואיד המודרניים
import androidx.recyclerview.widget.LinearLayoutManager; // מחלקה שמסדרת רשימות בצורה אנכית מלמעלה למטה
import androidx.recyclerview.widget.RecyclerView; // רכיב מתקדם ויעיל מאוד להצגת רשימות ארוכות נגללות

import com.example.iddoivanfinalproject.adapter.CartAdapter; // ייבוא המתווך שמחבר בין הנתונים לשורות העגלה
import com.example.iddoivanfinalproject.model.Cart; // מודל הנתונים של פריט בעגלת הקניות
import com.example.iddoivanfinalproject.model.Item; // הוספנו: מודל הנתונים של מוצר (כדי שנוכל למצוא אותו במעבר למסך)
import com.example.iddoivanfinalproject.model.Order; // מודל הנתונים המייצג הזמנה שבוצעה
import com.example.iddoivanfinalproject.services.DataBaseService; // שירות הגישה למסד הנתונים בענן (Firebase)
import com.google.firebase.auth.FirebaseAuth; // מערכת ההזדהות של פיירבייס (כדי לדעת מי מחובר)

import java.util.List; // ממשק לעבודה עם רשימות דינמיות ב-Java

public class CartActivity extends AppCompatActivity { // הגדרת המחלקה של מסך העגלה, שיורשת ממסך אנדרואיד רגיל
    private RecyclerView rvCart; // משתנה שישמור את רכיב הרשימה (RecyclerView) מהמסך
    private CartAdapter adapter; // משתנה שישמור את המתווך (Adapter) שאחראי על ציור השורות
    private DataBaseService.DatabaseService databaseService; // משתנה לעבודה מול פונקציות מסד הנתונים

    private Button btnPurchase, btnBack; // משתנים לכפתורי הרכישה והחזרה אחורה
    private TextView tvTotalPrice; // משתנה לשדה הטקסט שמציג את המחיר הכולל של העגלה
    private List<Cart> currentCartList; // משתנה מסוג רשימה שישמור את כל המוצרים שנמצאים כרגע בעגלה

    // יצירת ממשק (Interface) פנימי שמגדיר איזה פעולות אפשר לעשות על שורה בעגלה
    public interface CartActionListener {
        void onDelete(Cart cart); // פעולה ראשונה: מחיקת מוצר מהעגלה (בלחיצה על הפח)
        void onQuantityChanged(Cart cart, int newQuantity); // פעולה שנייה: שינוי כמות של מוצר בעגלה (פלוס ומינוס)
        void onItemClicked(Cart cart); // תוספת חדשה: פעולה שתופעל כשהמשתמש לוחץ על כל שורת המוצר (כדי לעבור לפרטים שלו)
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) { // פונקציית המערכת הראשונה שרצה כשפותחים את המסך הזה
        super.onCreate(savedInstanceState); // קריאה לפונקציית הבסיס של אנדרואיד כדי לאתחל את המסך
        setContentView(R.layout.activity_cart); // מחבר את המחלקה הנוכחית לקובץ העיצוב XML שנקרא activity_cart

        initViews(); // מפעיל את הפונקציה שלנו שמקשרת את המשתנים לרכיבים במסך
        loadCartItems(); // מפעיל את הפונקציה ששואבת את המוצרים של המשתמש ממסד הנתונים
    }

    private void initViews() { // פונקציה שעושה סדר ומקשרת כל משתנה לרכיב שלו בקובץ ה-XML
        rvCart = findViewById(R.id.rvCart); // מחפש ב-XML את הרשימה לפי ה-ID שלה ושומר במשתנה
        rvCart.setLayoutManager(new LinearLayoutManager(this)); // מגדיר שהרשימה תהיה מסודרת מלמעלה למטה
        databaseService = DataBaseService.DatabaseService.getInstance(); // מקבל את מופע ההתחברות למסד הנתונים

        btnBack = findViewById(R.id.btnUniversalBack); // מחפש ב-XML את כפתור החזרה ושומר במשתנה
        tvTotalPrice = findViewById(R.id.tvSummary); // מחפש את טקסט סך הכל מחיר ושומר במשתנה

        if (btnBack != null) { // מוודא שכפתור החזרה אכן קיים במסך כדי לא לקרוס
            btnBack.setOnClickListener(v -> finish()); // מגדיר שבלחיצה עליו, המסך פשוט ייסגר ויחזור אחורה
        }

        btnPurchase = findViewById(R.id.btnPurchase); // מחפש ב-XML את כפתור הרכישה ושומר במשתנה
        btnPurchase.setOnClickListener(v -> showPurchaseConfirmationDialog()); // מגדיר שבלחיצה עליו תיפתח חלונית אישור
    }

    private void showPurchaseConfirmationDialog() { // פונקציה שמציגה חלונית קופצת לאישור קנייה
        new AlertDialog.Builder(this) // מתחיל לבנות חלונית דיאלוג למסך הנוכחי
                .setTitle("אישור רכישה") // מגדיר את כותרת החלונית
                .setMessage("האם אתה בטוח שברצונך לבצע את הרכישה ולקנות את המוצרים בעגלה?") // מגדיר את הטקסט שבתוך החלונית
                .setPositiveButton("כן, קנה עכשיו", new DialogInterface.OnClickListener() { // מגדיר כפתור חיובי (אישור)
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // כשלוחצים על "כן"
                        processPurchase(); // מפעיל את פונקציית ביצוע הקנייה עצמה
                    }
                })
                .setNegativeButton("ביטול", new DialogInterface.OnClickListener() { // מגדיר כפתור שלילי (ביטול)
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // כשלוחצים על "ביטול"
                        dialog.dismiss(); // סוגר ומעלים את החלונית בלי לעשות כלום
                    }
                })
                .show(); // נותן פקודה להציג בפועל את החלונית על המסך
    }

    private void showDeleteConfirmationDialog(Cart cart) { // פונקציה שמציגה חלונית קופצת לווידוא מחיקת פריט
        new AlertDialog.Builder(this) // מתחיל לבנות את חלונית הדיאלוג
                .setTitle("מחיקת פריט") // קובע את הכותרת
                .setMessage("האם אתה בטוח שברצונך להסיר את '" + cart.getName() + "' מהעגלה?") // מכניס לטקסט את שם המוצר
                .setPositiveButton("כן, הסר", new DialogInterface.OnClickListener() { // כפתור מחיקה
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // בעת לחיצה
                        deleteItem(cart); // מפעיל את פונקציית המחיקה של הפריט ממסד הנתונים
                    }
                })
                .setNegativeButton("ביטול", new DialogInterface.OnClickListener() { // כפתור התחרטות
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // בעת לחיצה
                        dialog.dismiss(); // מעלים את החלון
                        loadCartItems(); // טוען מחדש את העגלה (חשוב אם המשתמש הוריד כמות ל-0 ואז התחרט, שהכמות תחזור ל-1)
                    }
                })
                .show(); // מציג את הדיאלוג
    }

    private void loadCartItems() { // פונקציה ששואבת את המוצרים של המשתמש מהענן
        String currentUserId = FirebaseAuth.getInstance().getUid(); // שולף את תעודת הזהות (ID) של המשתמש שמחובר לאפליקציה כעת

        if (currentUserId != null) { // בודק שבאמת יש מישהו מחובר (שלא נקרוס אם הוא התנתק)
            databaseService.getCartList(currentUserId, new DataBaseService.DatabaseCallback<List<Cart>>() { // מבקש מהמסד את העגלה של המשתמש
                @Override
                public void onCompleted(List<Cart> carts) { // ברגע שהמידע מגיע בהצלחה מהאינטרנט
                    currentCartList = carts; // שומר את הרשימה שהגיעה במשתנה המחלקה שלנו
                    updateTotalPrice(carts); // שולח את הרשימה לפונקציית חישוב המחיר הכולל

                    if (carts != null && !carts.isEmpty()) { // אם קיבלנו רשימה והיא לא ריקה (כלומר יש מוצרים)
                        btnPurchase.setEnabled(true); // מדליק את כפתור הקנייה שיהיה אפשר ללחוץ עליו

                        adapter = new CartAdapter(carts, new CartActionListener() { // יוצר מתווך חדש לעגלה ומעביר לו את הרשימה ואת רשימת הפעולות
                            @Override
                            public void onDelete(Cart cart) { // מה קורה כשהמתווך מדווח שלחצו על מחיקה
                                showDeleteConfirmationDialog(cart); // פותח את חלונית אישור המחיקה
                            }

                            @Override
                            public void onQuantityChanged(Cart cart, int newQuantity) { // מה קורה כשהמתווך מדווח על שינוי כמות (פלוס/מינוס)
                                if (newQuantity > 0) { // אם הכמות החדשה גדולה מאפס
                                    updateItemQuantity(cart, newQuantity); // מפעיל פונקציה שמעדכנת את הכמות במסד הנתונים
                                } else { // אם הגיעו לאפס
                                    showDeleteConfirmationDialog(cart); // פותח חלונית אישור מחיקה כי אי אפשר לקנות 0
                                }
                            }

                            @Override
                            public void onItemClicked(Cart cart) { // הפונקציה החדשה: מה קורה שלוחצים על שורת המוצר עצמה
                                // מכיוון שבעגלה אנחנו שומרים רק את שם המוצר, אנחנו צריכים למצוא מה ה-ID המקורי שלו בחנות
                                databaseService.getAllItems(new DataBaseService.DatabaseCallback<List<Item>>() { // מושכים את כל חנות המוצרים
                                    @Override
                                    public void onCompleted(List<Item> items) { // כשהחנות מגיעה מהאינטרנט
                                        if (items != null) { // אם החנות לא ריקה
                                            for (Item item : items) { // עוברים על כל מוצר ומוצר בחנות
                                                if (item.getName() != null && item.getName().equals(cart.getName())) { // אם מצאנו מוצר בחנות עם שם זהה לזה שבעגלה
                                                    Intent intent = new Intent(CartActivity.this, Itemdetails.class); // מכינים מעבר למסך פרטי המוצר
                                                    intent.putExtra("ITEM_ID", item.getId()); // "אורזים" בתוך המעבר את ה-ID המקורי של המוצר שמצאנו
                                                    startActivity(intent); // משגרים את המשתמש למסך פרטי המוצר
                                                    return; // עוצרים כאן כדי לא להמשיך לבדוק שאר המוצרים
                                                }
                                            }
                                        }
                                        // אם הלולאה סיימה ולא מצאה את המוצר (אולי המנהל מחק אותו מהחנות בינתיים)
                                        Toast.makeText(CartActivity.this, "המוצר הזה כבר לא זמין בחנות", Toast.LENGTH_SHORT).show(); // מודיעים למשתמש
                                    }
                                    @Override
                                    public void onFailed(Exception e) {} // אם כשלנו בהבאת החנות מתעלמים
                                });
                            }
                        });
                        rvCart.setAdapter(adapter); // מחבר את המתווך לרכיב התצוגה של הרשימה, כך שיופיעו השורות במסך
                    } else { // אם הרשימה שקיבלנו מהמסד ריקה לחלוטין
                        rvCart.setAdapter(null); // מרוקן את התצוגה מהמסך
                        btnPurchase.setEnabled(false); // מכבה את כפתור הקנייה (אי אפשר לקנות אוויר)
                        Toast.makeText(CartActivity.this, "העגלה שלך ריקה", Toast.LENGTH_SHORT).show(); // מציג הודעה שהעגלה ריקה
                    }
                }

                private void updateTotalPrice(List<Cart> carts) { // פונקציה פנימית לחישוב הסכום הכולל בעגלה
                    double total = 0; // מאתחל את הסכום ל-0
                    if (carts != null) { // מוודא שהרשימה לא ריקה כדי לא לקרוס
                        for (Cart item : carts) { // עובר על כל פריט בתוך העגלה
                            total += item.getPrice() * item.getQuantity(); // מוסיף לסכום הכולל את מחיר המוצר כפול הכמות שהוזמנה ממנו
                        }
                    }
                    tvTotalPrice.setText("סה״כ לתשלום: ₪" + String.format("%.2f", total)); // מעדכן את הטקסט במסך עם הסכום שחושב, ושומר על 2 ספרות עשרוניות בלבד
                }

                @Override
                public void onFailed(Exception e) { // אם התקשורת עם מסד הנתונים נכשלה מסיבה כלשהי
                    Toast.makeText(CartActivity.this, "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show(); // מציג הודעת שגיאה למשתמש
                }
            });
        }
    }

    private void processPurchase() { // פונקציה שמבצעת את תהליך הקנייה מול המסד
        String uid = FirebaseAuth.getInstance().getUid(); // שולף שוב את מזהה המשתמש
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // שולף את כתובת האימייל של המשתמש המחובר

        if (uid == null || currentCartList == null || currentCartList.isEmpty()) { // בדיקת בטיחות: אם משהו חסר, אל תעשה כלום
            return; // עוצר את הפעולה
        }

        double total = 0; // מחשבים שוב את הסכום (כדי להיות בטוחים)
        for (Cart item : currentCartList) { // עוברים על המוצרים
            total += item.getPrice() * item.getQuantity(); // סוכמים מחיר כפול כמות
        }

        Order newOrder = new Order(uid, email, currentCartList, total, System.currentTimeMillis()); // יוצרים אובייקט "הזמנה" חדש שכולל מי הזמין, מה הזמין, כמה שילם ומתי

        databaseService.saveOrder(newOrder, new DataBaseService.DatabaseCallback<Void>() { // שולחים למסד הנתונים את ההזמנה לשמירה
            @Override
            public void onCompleted(Void unused) { // אם ההזמנה נשמרה בהצלחה אצל המנהל
                databaseService.clearUserCart(uid, new DataBaseService.DatabaseCallback<Void>() { // מבקשים מהמסד לרוקן (למחוק) את כל העגלה של המשתמש כדי שהוא יתחיל מחדש
                    @Override
                    public void onCompleted(Void unused) { // אם העגלה רוקנה בהצלחה
                        Toast.makeText(CartActivity.this, "הרכישה הושלמה ונשמרה במערכת!", Toast.LENGTH_LONG).show(); // מודיעים למשתמש שהקנייה הצליחה
                        loadCartItems(); // טוענים מחדש את העגלה (שתהיה ריקה עכשיו, מה שיעדכן את המסך)
                    }

                    @Override
                    public void onFailed(Exception e) { // אם ההזמנה הצליחה אבל משום מה העגלה לא התרוקנה
                        Toast.makeText(CartActivity.this, "ההזמנה נשמרה אך ארעה שגיאה בפינוי העגלה", Toast.LENGTH_SHORT).show(); // מודיעים למשתמש
                    }
                });
            }

            @Override
            public void onFailed(Exception e) { // אם ההזמנה נכשלה (למשל אין אינטרנט)
                Toast.makeText(CartActivity.this, "שגיאה בביצוע הרכישה: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // מודיעים למשתמש
            }
        });
    }

    private void updateItemQuantity(Cart cart, int newQuantity) { // פונקציה לעדכון כמות של מוצר ספציפי
        cart.setQuantity(newQuantity); // משנה באובייקט שבזיכרון את הכמות למה שנבחר

        databaseService.createNewCart(cart, new DataBaseService.DatabaseCallback<Void>() { // שומר את האובייקט המעודכן חזרה במסד הנתונים (הוא דורס את עצמו)
            @Override
            public void onCompleted(Void unused) { // אם העדכון בענן הצליח
                loadCartItems(); // שואב מחדש את הרשימה כדי שהמסך והמחירים יתעדכנו
            }

            @Override
            public void onFailed(Exception e) { // אם העדכון נכשל
                Toast.makeText(CartActivity.this, "שגיאה בעדכון הכמות: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // הודעת שגיאה
            }
        });
    }

    private void deleteItem(Cart cart) { // פונקציה למחיקת פריט ספציפי מהעגלה
        databaseService.deleteCartItem(cart.getUserId(), cart.getId(), new DataBaseService.DatabaseCallback<Void>() { // פונה למסד בבקשה למחוק פריט לפי ה-ID שלו
            @Override
            public void onCompleted(Void unused) { // כשהמחיקה מצליחה
                Toast.makeText(CartActivity.this, "הפריט הוסר מהעגלה", Toast.LENGTH_SHORT).show(); // הודעת אישור
                loadCartItems(); // רענון רשימת העגלה (שתעלים את הפריט מהמסך)
            }

            @Override
            public void onFailed(Exception e) { // כשהמחיקה נכשלת
                Toast.makeText(CartActivity.this, "המחיקה נכשלה: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // הודעת שגיאה
            }
        });
    }
}