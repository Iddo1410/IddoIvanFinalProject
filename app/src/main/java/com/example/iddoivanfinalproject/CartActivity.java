package com.example.iddoivanfinalproject; // הגדרת מיקום הקובץ בחבילת הפרויקט

// ייבוא מחלקות וספריות שדרושות להפעלת הקוד
import android.content.DialogInterface; // מחלקה לטיפול בלחיצות בתוך חלונות קופצים (דיאלוגים)
import android.os.Bundle; // מחלקה לשמירת מצב המסך בזמן פתיחה/סגירה
import android.view.View;
import android.widget.Button; // מחלקת רכיב כפתור
import android.widget.TextView; // מחלקת רכיב טקסט לתצוגה
import android.widget.Toast; // מחלקה להצגת הודעות קופצות קצרות בתחתית המסך

import androidx.appcompat.app.AlertDialog; // מחלקה ליצירת חלונות קופצים (כמו חלון אישור רכישה)
import androidx.appcompat.app.AppCompatActivity; // מחלקת הבסיס למסכי אנדרואיד
import androidx.recyclerview.widget.LinearLayoutManager; // מחלקה שמסדרת רשימות בצורה אנכית/אופקית
import androidx.recyclerview.widget.RecyclerView; // רכיב מתקדם להצגת רשימות ארוכות בצורה יעילה (כמו רשימת עגלת הקניות)

import com.example.iddoivanfinalproject.adapter.CartAdapter; // ייבוא המתווך (Adapter) שמחבר בין הנתונים לתצוגה ברשימה
import com.example.iddoivanfinalproject.model.Cart; // מודל הנתונים המייצג פריט בתוך עגלת הקניות
import com.example.iddoivanfinalproject.model.Order; // מודל הנתונים המייצג הזמנה שבוצעה
import com.example.iddoivanfinalproject.services.DataBaseService; // שירות הגישה למסד הנתונים
import com.google.firebase.auth.FirebaseAuth; // מערכת אימות המשתמשים של Firebase (לזיהוי מי המשתמש שמחובר)

import java.util.List; // ממשק לרשימות דינמיות ב-Java

public class CartActivity extends BaseActivity {
    private RecyclerView rvCart; // משתנה לרכיב הרשימה במסך
    private CartAdapter adapter; // משתנה למתווך שמצייר את השורות ברשימה
    private DataBaseService.DatabaseService databaseService; // משתנה לפעולות מול מסד הנתונים

    // הוספת משתנים עבור כפתור הרכישה, כפתור חזרה אחורה ושדה הטקסט של המחיר הכולל
    private Button btnPurchase, btnBack;
    public void onMenuClick(View v) {
        openDrawer(); // קורא לפונקציה שכתבנו ב-BaseActivity
    }
    private TextView tvTotalPrice;
    private List<Cart> currentCartList; // משתנה שישמור את רשימת המוצרים הנוכחית שבעגלה

    // ממשק (Interface) המגדיר אילו פעולות ניתן לבצע על פריט בתוך העגלה
    // המתווך (CartAdapter) ישתמש בממשק הזה כדי "לדווח" למסך מתי לחצו על מחיקה או שינו כמות
    public interface CartActionListener {
        void onDelete(Cart cart); // פונקציה שתופעל כאשר נדרש למחוק פריט מהעגלה
        void onQuantityChanged(Cart cart, int newQuantity); // פונקציה שתופעל כשהכמות של פריט שונתה
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) { // הפונקציה הראשונה שרצה כשפותחים את המסך
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart); // קביעת עיצוב המסך מתוך קובץ ה-XML

        initViews(); // קריאה לפונקציית אתחול הרכיבים הוויזואליים
        loadCartItems(); // קריאה לפונקציה שטוענת את רשימת הפריטים מהענן
    }

    private void initViews() { // פונקציה שמקשרת בין המשתנים שלנו לרכיבים ב-XML
        rvCart = findViewById(R.id.rvCart); // קישור רשימת העגלה
        rvCart.setLayoutManager(new LinearLayoutManager(this)); // קביעה שהרשימה תוצג מלמעלה למטה בצורה אנכית
        databaseService = DataBaseService.DatabaseService.getInstance(); // קבלת מופע (Instance) של שירות מסד הנתונים

        btnBack = findViewById(R.id.btnUniversalBack); // קישור כפתור חזרה אחורה
        tvTotalPrice = findViewById(R.id.tvSummary); // קישור טקסט סיכום המחיר

        if (btnBack != null) { // מוודא שכפתור החזרה קיים
            btnBack.setOnClickListener(v -> finish()); // לחיצה על חזרה תסגור את המסך (finish)
        }

        // אתחול כפתור הרכישה והוספת מאזין לחיצה שיפתח את חלונית האישור
        btnPurchase = findViewById(R.id.btnPurchase);
        btnPurchase.setOnClickListener(v -> showPurchaseConfirmationDialog()); // לחיצה תפתח דיאלוג לאישור הקנייה
    }

    // פונקציה חדשה: הצגת חלונית אישור לפני רכישה
    private void showPurchaseConfirmationDialog() {
        new AlertDialog.Builder(this) // יצירת בונה חלון קופץ עבור המסך הנוכחי
                .setTitle("אישור רכישה") // הגדרת כותרת לחלונית
                .setMessage("האם אתה בטוח שברצונך לבצע את הרכישה ולקנות את המוצרים בעגלה?") // הגדרת תוכן השאלה
                .setPositiveButton("כן, קנה עכשיו", new DialogInterface.OnClickListener() { // הגדרת כפתור "אישור"
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // אם המשתמש אישר - נבצע את הרכישה
                        processPurchase(); // קריאה לפונקציה שמבצעת את תהליך הקנייה מול המסד
                    }
                })
                .setNegativeButton("ביטול", new DialogInterface.OnClickListener() { // הגדרת כפתור "ביטול"
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // אם המשתמש ביטל - פשוט נסגור את החלונית ללא ביצוע שום פעולה
                        dialog.dismiss();
                    }
                })
                .show(); // הצגת החלונית שיצרנו למשתמש
    }

    private void loadCartItems() { // פונקציה למשיכת נתוני העגלה של המשתמש ממסד הנתונים
        String currentUserId = FirebaseAuth.getInstance().getUid(); // שליפת ה-ID של המשתמש שמחובר כרגע לאפליקציה

        if (currentUserId != null) { // מוודא שיש משתמש מחובר
            // פנייה למסד הנתונים בבקשה לקבל את רשימת פריטי העגלה של המשתמש
            databaseService.getCartList(currentUserId, new DataBaseService.DatabaseCallback<List<Cart>>() {
                @Override
                public void onCompleted(List<Cart> carts) { // אם הנתונים התקבלו בהצלחה
                    currentCartList = carts; // שמירת הרשימה שהתקבלה בתוך המשתנה שלנו במסך
                    updateTotalPrice(carts); // קריאה לפונקציית עזר שתעדכן את סך המחיר לתשלום

                    if (carts != null && !carts.isEmpty()) { // אם הרשימה אינה ריקה (יש מוצרים בעגלה)
                        btnPurchase.setEnabled(true); // הדלקת/אפשור כפתור הרכישה

                        // יצירת מתווך חדש והעברת רשימת המוצרים וגם מאזין לפעולות (מחיקה/שינוי כמות) אליו
                        adapter = new CartAdapter(carts, new CartActionListener() {
                            @Override
                            public void onDelete(Cart cart) { // קוד שיופעל כשהמתווך יזהה לחיצה על 'מחק'
                                deleteItem(cart); // קריאה לפונקציית מחיקה מהמסד
                            }

                            @Override
                            public void onQuantityChanged(Cart cart, int newQuantity) { // קוד שיופעל כשמשנים כמות (פלוס/מינוס)
                                if (newQuantity > 0) { // אם הכמות חיובית
                                    updateItemQuantity(cart, newQuantity); // נעדכן את הכמות במסד הנתונים
                                } else {
                                    // אם הכמות ירדה ל-0, במקום להשאיר 0 פשוט נמחק את הפריט לגמרי
                                    deleteItem(cart);
                                }
                            }
                        });
                        rvCart.setAdapter(adapter); // חיבור המתווך (עם הנתונים והמאזינים) לרכיב הרשימה במסך
                    } else { // אם הרשימה ריקה
                        rvCart.setAdapter(null); // ניקוי הרשימה מהמסך
                        btnPurchase.setEnabled(false); // כיבוי כפתור הרכישה כי אין מה לקנות
                        Toast.makeText(CartActivity.this, "העגלה שלך ריקה", Toast.LENGTH_SHORT).show(); // הודעה למשתמש
                    }
                }

                // פונקציית עזר פנימית לחישוב ועדכון המחיר הכולל של כל העגלה
                private void updateTotalPrice(List<Cart> carts) {
                    double total = 0; // אתחול הסכום לאפס

                    for (Cart item : carts) { // לולאה שעוברת על כל מוצר ומוצר בעגלה
                        total += item.getPrice() * item.getQuantity(); // מוסיפה לסכום הכולל את (מחיר המוצר * הכמות שהוזמנה ממנו)
                    }

                    // עדכון שדה הטקסט במסך עם הסכום שחושב, מעוגל ל-2 ספרות אחרי הנקודה العشרונית
                    tvTotalPrice.setText("סה״כ לתשלום: ₪" + String.format("%.2f", total));
                }

                @Override
                public void onFailed(Exception e) { // אם הייתה שגיאה בקריאת הנתונים מהמסד
                    Toast.makeText(CartActivity.this, "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // פונקציית הרכישה וניקוי כל העגלה בבת אחת (תופעל רק לאחר אישור בחלונית)
    private void processPurchase() {
        String uid = FirebaseAuth.getInstance().getUid(); // שליפת ה-ID של המשתמש
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // שליפת האימייל של המשתמש

        // בדיקה שאכן יש משתמש, יש עגלה והיא איננה ריקה. אם אחד מהתנאים מתקיים, יוצאים מהפונקציה
        if (uid == null || currentCartList == null || currentCartList.isEmpty()) {
            return;
        }

        // חישוב המחיר הכולל של העגלה בשנית כדי לוודא נכונות לפני יצירת ההזמנה
        double total = 0;
        for (Cart item : currentCartList) {
            total += item.getPrice() * item.getQuantity();
        }

        // יצירת אובייקט הזמנה חדש שמכיל את: מי הזמין (ID ומייל), מה הוזמן (רשימת הפריטים), הסכום לתשלום, והזמן הנוכחי במערכת
        Order newOrder = new Order(uid, email, currentCartList, total, System.currentTimeMillis());

        // 1. שמירת ההזמנה בהיסטוריית ההזמנות במסד הנתונים
        databaseService.saveOrder(newOrder, new DataBaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void unused) { // אם שמירת ההזמנה הצליחה
                // 2. רק אם השמירה הצליחה - ננקה (נמחוק) את כל פריטי העגלה של המשתמש כדי שהוא יוכל להתחיל מחדש
                databaseService.clearUserCart(uid, new DataBaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void unused) { // אם העגלה רוקנה בהצלחה
                        Toast.makeText(CartActivity.this, "הרכישה הושלמה ונשמרה במערכת!", Toast.LENGTH_LONG).show(); // הודעת הצלחה
                        loadCartItems(); // טעינה מחדש של העגלה (תציג עכשיו עגלה ריקה כי מחקנו הכל)
                    }

                    @Override
                    public void onFailed(Exception e) { // אם הייתה שגיאה בריקון העגלה
                        Toast.makeText(CartActivity.this, "ההזמנה נשמרה אך ארעה שגיאה בפינוי העגלה", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(Exception e) { // אם שמירת ההזמנה עצמה נכשלה
                Toast.makeText(CartActivity.this, "שגיאה בביצוע הרכישה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה לעדכון הכמות של מוצר ספציפי במסד הנתונים (מופעלת מלחיצה על פלוס/מינוס)
    private void updateItemQuantity(Cart cart, int newQuantity) {
        cart.setQuantity(newQuantity); // מעדכנים את אובייקט ה-Cart הקיים עם הכמות החדשה

        // שומרים את האובייקט המעודכן חזרה במסד הנתונים (בדורס את הישן)
        databaseService.createNewCart(cart, new DataBaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void unused) { // אם העדכון בענן הצליח
                // מרענן את כל הרשימה מהמסד כדי להציג את המחיר והכמות המעודכנים באופן תקין במסך
                loadCartItems();
            }

            @Override
            public void onFailed(Exception e) { // אם העדכון נכשל
                Toast.makeText(CartActivity.this, "שגיאה בעדכון הכמות: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // פונקציה למחיקת פריט בודד מהעגלה (מופעלת בלחיצה על כפתור מחק או כשהכמות יורדת ל-0)
    private void deleteItem(Cart cart) {
        // קריאה לפעולת מחיקה משירות המסד, מועבר ה-ID של המשתמש וה-ID של הפריט שרוצים למחוק
        databaseService.deleteCartItem(cart.getUserId(), cart.getId(), new DataBaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void unused) { // אם הפריט נמחק בהצלחה
                Toast.makeText(CartActivity.this, "הפריט הוסר מהעגלה", Toast.LENGTH_SHORT).show(); // הודעה למשתמש
                loadCartItems(); // טעינה מחדש של הרשימה לאחר המחיקה כדי שהפריט ייעלם מהמסך
            }

            @Override
            public void onFailed(Exception e) { // אם הייתה שגיאה במחיקה
                Toast.makeText(CartActivity.this, "המחיקה נכשלה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}