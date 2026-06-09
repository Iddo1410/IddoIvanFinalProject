// הגדרת החבילה (Package) שבה נמצא הקובץ - עוזר לארגן את הקוד בתיקיות
package com.example.iddoivanfinalproject.services;

// --- אזור ייבוא הספריות (Imports) ---
import android.util.Log; // ספרייה להדפסת הודעות למסך הלוג (עוזר מאוד בדיבאגינג)
import androidx.annotation.NonNull; // תגית המציינת שפרמטר מסוים אסור שיהיה ריק (null)
import androidx.annotation.Nullable; // תגית המציינת שפרמטר מסוים יכול להיות ריק (null)

// ייבוא המודלים (המבנים) של הנתונים באפליקציה: עגלה, השוואה, פריט, הזמנה, משתמש
import com.example.iddoivanfinalproject.model.Cart;
import com.example.iddoivanfinalproject.model.Compareitem;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.model.Order;
import com.example.iddoivanfinalproject.model.User;

// ייבוא ספריות של Firebase - לניהול אימות משתמשים ומסד הנתונים
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull; // תגית המקבילה ל-NonNull, מגיעה מקוטלין

// ייבוא מבני נתונים של Java (רשימות ופונקציות עזר)
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

// מחלקה העוטפת את כל פעולות התקשורת מול מסד הנתונים (Firebase)
public class DataBaseService {

    // תגית קבועה המשמשת ככותרת בהדפסות ללוג (כדי שנדע מי הדפיס את ההודעה)
    private static final String TAG = "DatabaseService";

    // הגדרת מחרוזות קבועות לשמות ה"טבלאות" (צמתים) ב-Firebase
    private static final String USERS_PATH = "users",      // נתיב נתוני המשתמשים
            ITEM_PATH = "items",                           // נתיב רשימת המוצרים
            COMPARE_PATH = "compare",                      // נתיב רשימות ההשוואה
            CARTS_PATH = "carts";                          // נתיב עגלות הקניות

    // ממשק (Interface) המשמש כ-Callback. מאחר שהפניות ל-Firebase מתבצעות ברקע (אסינכרוניות),
    // הממשק הזה מאפשר לנו "להמתין" ולקבל הודעה כשהפעולה מסתיימת.
    public interface DatabaseCallback<T> {
        void onCompleted(T object);     // פונקציה שתופעל כשהפעולה הסתיימה בהצלחה (ומחזירה את המידע שביקשנו)
        void onFailed(Exception e);     // פונקציה שתופעל במקרה של שגיאה (למשל בעיית רשת)
    }

    // מחלקה פנימית המיישמת תבנית עיצוב מסוג Singleton (מבטיח שיהיה רק מופע אחד כזה בכל האפליקציה)
    public static class DatabaseService {

        private static DatabaseService instance; // משתנה סטטי שישמור את המופע היחיד של המחלקה
        private final DatabaseReference databaseReference; // אובייקט "מצביע" למיקום בתוך מסד הנתונים של פיירבייס

        // בנאי פרטי (private) - חוסם את האפשרות ליצור מופע חדש מבחוץ עם 'new'
        private DatabaseService() {
            // מקבל את מופע מסד הנתונים מפיירבייס
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            // מקבל נקודת הצבעה לשורש (התקייה הראשית) של מסד הנתונים
            databaseReference = firebaseDatabase.getReference();
        }

        // הפונקציה שדרכה משיגים את המחלקה מכל מקום באפליקציה
        public static DatabaseService getInstance() {
            if (instance == null) { // אם המופע עדיין לא נוצר מעולם
                instance = new DatabaseService(); // ניצור אותו בפעם הראשונה
            }
            return instance; // נחזיר את המופע
        }

        // ==========================================
        // region Generic Private Methods (פונקציות עזר פנימיות לביצוע הפעולות הבסיסיות)
        // ==========================================

        // פונקציה גנרית לכתיבת נתונים ל-Firebase אל נתיב (path) מסוים
        private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
            // ניגש לנתיב המבוקש ומגדיר לו את הערך החדש (data)
            readData(path).setValue(data, (error, ref) -> {
                if (error != null) { // אם התקבלה שגיאה מהשרת
                    if (callback != null) callback.onFailed(error.toException()); // מעביר את השגיאה החוצה דרך ה-callback
                } else { // אם הפעולה הצליחה
                    if (callback != null) callback.onCompleted(null); // מודיע על הצלחה
                }
            });
        }

        // פונקציה גנרית למחיקת נתונים מנתיב מסוים
        private void deleteData(@NotNull final String path, @Nullable final DatabaseCallback<Void> callback) {
            // ניגש לנתיב ומוחק את הערך שבו (removeValue)
            readData(path).removeValue((error, ref) -> {
                if (error != null) {
                    if (callback != null) callback.onFailed(error.toException()); // דיווח שגיאה
                } else {
                    if (callback != null) callback.onCompleted(null); // דיווח הצלחה
                }
            });
        }

        // פונקציית עזר להחזרת ה"מצביע" (Reference) לתיקייה/נתיב ספציפי
        private DatabaseReference readData(@NotNull final String path) {
            return databaseReference.child(path); // מוסיף את השם המבוקש לשורש המסד
        }

        // פונקציה לקריאת אובייקט בודד מהמסד והמרתו למחלקה הרצויה (clazz)
        private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
            readData(path).get().addOnCompleteListener(task -> { // מבקש מהשרת להביא את הנתונים (get)
                if (!task.isSuccessful()) { // אם הבקשה נכשלה
                    callback.onFailed(task.getException());
                    return;
                }
                // אם הצליח: מחלץ את המידע וממיר אותו לאובייקט מהסוג שהתבקש (למשל User.class)
                T data = task.getResult().getValue(clazz);
                callback.onCompleted(data); // מחזיר את האובייקט המוכן
            });
        }

        // פונקציה לקריאת רשימה של אובייקטים (למשל כל המשתמשים או כל המוצרים)
        private <T> void getDataList(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<List<T>> callback) {
            readData(path).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    callback.onFailed(task.getException());
                    return;
                }
                List<T> tList = new ArrayList<>(); // יוצר רשימה ריקה לאחסון התוצאות
                // עובר בלולאה על כל "ילד" (רשומה) שתחת הנתיב שקראנו
                for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                    T t = dataSnapshot.getValue(clazz); // ממיר את הילד לאובייקט
                    if (t != null) tList.add(t); // אם ההמרה תקינה, מוסיף אותו לרשימה
                }
                callback.onCompleted(tList); // מחזיר את כל הרשימה המוכנה
            });
        }

        // פונקציה המייצרת מזהה (ID) חדש, ייחודי ואקראי עבור רשומה חדשה
        private String generateNewId(@NotNull final String path) {
            // הפעולה push() מייצרת ענף חדש וריק, ו-getKey() שולף את השם האקראי שלו
            return databaseReference.child(path).push().getKey();
        }

        // פונקציה להרצת טרנזקציה. נועדה למנוע התנגשויות כשמספר משתמשים כותבים לאותו מקום באותו זמן.
        private <T> void runTransaction(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull UnaryOperator<T> function, @NotNull final DatabaseCallback<T> callback) {
            readData(path).runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) { // בלוק הפעולה
                    T currentValue = currentData.getValue(clazz); // קורא את המצב הנוכחי במסד
                    currentValue = function.apply(currentValue); // מפעיל את פונקציית העדכון על הערך הנוכחי
                    currentData.setValue(currentValue); // מגדיר את הערך המעודכן כהכנה לשמירה
                    return Transaction.success(currentData); // מאשר שהתהליך המקומי תקין
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) { // בסיום הניסיון מול השרת
                    if (error != null) {
                        callback.onFailed(error.toException()); // במקרה של שגיאת התנגשות או רשת
                        return;
                    }
                    // מחלץ ומחזיר את הערך הסופי שבאמת נשמר בשרת
                    T result = currentData != null ? currentData.getValue(clazz) : null;
                    callback.onCompleted(result);
                }
            });
        }
        // endregion

        // ==========================================
        // region User Section (אזור פעולות משתמשים)
        // ==========================================

        // פונקציה ליצירת משתמש חדש
        public void createNewUser(@NotNull final User user, @Nullable final DatabaseCallback<String> callback) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance(); // מפעיל את שירות האימות של Firebase
            // יוצר משתמש במערכת האימות עם אימייל וסיסמה מתוך אובייקט ה-user
            mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) { // אם היצירה במערכת האימות הצליחה
                            String uid = mAuth.getCurrentUser().getUid(); // שולף את המזהה (UID) שקיבל מהשרת
                            user.setId(uid); // מצמיד את המזהה לאובייקט ה-user המקומי
                            // שומר את שאר פרטי המשתמש במסד הנתונים תחת הנתיב: users/UID
                            writeData(USERS_PATH + "/" + uid, user, new DatabaseCallback<Void>() {
                                @Override
                                public void onCompleted(Void v) { if (callback != null) callback.onCompleted(uid); } // מחזיר את ה-UID בסיום מוצלח
                                @Override
                                public void onFailed(Exception e) { if (callback != null) callback.onFailed(e); }
                            });
                        } else {
                            if (callback != null) callback.onFailed(task.getException()); // אם נכשל בשלב האימות
                        }
                    });
        }

        // קבלת פרטי משתמש ספציפי לפי ה-ID שלו
        public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
            getData(USERS_PATH + "/" + uid, User.class, callback); // קורא מנתיב users/UID
        }

        // קבלת רשימה של כל המשתמשים באפליקציה
        public void getUserList(@NotNull final DatabaseCallback<List<User>> callback) {
            getDataList(USERS_PATH, User.class, callback);
        }

        // מחיקת משתמש ממסד הנתונים
        public void deleteUser(@NotNull final String uid, @Nullable final DatabaseCallback<Void> callback) {
            deleteData(USERS_PATH + "/" + uid, callback);
        }
        // endregion

        // ==========================================
        // region Item Section (אזור פעולות מוצרים/פריטים)
        // ==========================================

        // הוספת פריט חדש למאגר המוצרים בחנות
        public void createNewItem(@NotNull final Item item, @Nullable final DatabaseCallback<Void> callback) {
            writeData(ITEM_PATH + "/" + item.getId(), item, callback); // שומר תחת נתיב items/itemID
        }

        // שליפת רשימת כל המוצרים בחנות
        public void getAllItems(@NotNull final DatabaseCallback<List<Item>> callback) {
            getDataList(ITEM_PATH, Item.class, callback);
        }

        // יצירת מזהה אקראי לפריט חדש (כדי שנוכל לתת לו ID לפני השמירה)
        public String generateItemId() {
            return generateNewId(ITEM_PATH);
        }
        // endregion

        // ==========================================
        // region Cart Section (אזור פעולות עגלת קניות)
        // ==========================================

        // הוספת פריט לעגלה / יצירת עגלה למשתמש
        public void createNewCart(@NotNull final Cart cart, @Nullable final DatabaseCallback<Void> callback) {
            // שומר בנתיב מיוחד: carts -> מזהה המשתמש -> מזהה הפריט בעגלה. מפריד כך בין משתמשים שונים.
            writeData(CARTS_PATH + "/" + cart.getUserId() + "/" + cart.getId(), cart, callback);
        }

        // קבלת כל הפריטים בעגלה של משתמש ספציפי
        public void getCartList(String userId, @NotNull final DatabaseCallback<List<Cart>> callback) {
            getDataList(CARTS_PATH + "/" + userId, Cart.class, callback);
        }

        // מחיקת פריט ספציפי מתוך העגלה של משתמש ספציפי
        public void deleteCartItem(@NotNull final String userId, @NotNull final String cartId, @Nullable final DatabaseCallback<Void> callback) {
            deleteData(CARTS_PATH + "/" + userId + "/" + cartId, callback);
        }

        // קבלת פרטים על מוצר בודד לפי מזהה (למשל כשלוחצים עליו בעגלה)
        public void getItemById(@NotNull final String itemId, @NotNull final DatabaseCallback<Item> callback) {
            getData(ITEM_PATH + "/" + itemId, Item.class, callback);
        }

        // יצירת מזהה אקראי לפריט בתוך העגלה
        public String generateCartId() {
            return generateNewId(CARTS_PATH);
        }
        // endregion

        // ==========================================
        // region Compare Section (אזור פעולות השוואת מוצרים)
        // ==========================================

        // יצירת רשימת השוואה חדשה
        public void createNewCompareList(@NotNull final Compareitem compareitem, @Nullable final DatabaseCallback<Void> callback) {
            String userid = FirebaseAuth.getInstance().getUid(); // בודק מי המשתמש שמחובר כרגע
            if(userid != null) {
                // שומר רשימה אחת כללית למשתמש תחת הנתיב compare/userID
                writeData(COMPARE_PATH + "/" + userid, compareitem, callback);
            }
        }

        // יצירת מזהה לפריט השוואה
        public String generateCompareId() {
            return generateNewId(COMPARE_PATH);
        }

        // פונקציה לעדכון שדות בודדים בפרופיל המשתמש (במקום לדרוס את כל האובייקט)
        public void updateUserFields(@NotNull String userId, String fname, String lname, String email, String phone, @Nullable final DatabaseCallback<Void> callback) {
            java.util.Map<String, Object> updates = new java.util.HashMap<>(); // יצירת מילון/מפה להגדרת השינויים
            updates.put("fname", fname); // עדכון שם פרטי
            updates.put("lname", lname); // עדכון שם משפחה
            updates.put("email", email); // עדכון אימייל
            updates.put("phoneNumber", phone); // עדכון טלפון

            // updateChildren מאפשרת לשנות רק את השדות ששלחנו במפה מבלי למחוק שדות אחרים ב-DB
            readData(USERS_PATH + "/" + userId).updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (callback != null) callback.onCompleted(null);
                } else {
                    if (callback != null) callback.onFailed(task.getException());
                }
            });
        }

        // קבלת רשימת ההשוואה של המשתמש הנוכחי
        public void getCompareByType(@NotNull final String type, @NotNull final DatabaseCallback<Compareitem> callback) {
            String userid = FirebaseAuth.getInstance().getUid(); // זיהוי המשתמש המחובר
            if(userid != null) {
                // קורא את הרשימה מהנתיב שלו
                getData(COMPARE_PATH + "/" + userid, Compareitem.class, callback);
            }
        }

        // עדכון רשימת ההשוואה של המשתמש (בפועל, כותב/דורס את הרשימה הקיימת באותו נתיב)
        public void updateCompareList(@NotNull final Compareitem compareitem, @Nullable final DatabaseCallback<Void> callback) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if(mAuth.getCurrentUser() != null) {
                String userid = mAuth.getCurrentUser().getUid();
                writeData(COMPARE_PATH + "/" + userid, compareitem, callback);
            }
        }

        // מחיקת כל התוכן בעגלת הקניות של המשתמש (מוחקת את התיקייה כולה)
        public void clearUserCart(@NotNull final String userId, @Nullable final DatabaseCallback<Void> callback) {
            deleteData(CARTS_PATH + "/" + userId, callback);
        }

        // ==========================================
        // region Orders Section (אזור פעולות הזמנות / רכישות)
        // ==========================================

        // שמירת הזמנה חדשה במערכת
        public void saveOrder(Order order, DatabaseCallback<Void> callback) {
            String key = databaseReference.child("Purchases").push().getKey(); // יצירת מזהה ייחודי להזמנה
            order.setId(key); // הכנסת המזהה לאובייקט ההזמנה
            // שמירת אובייקט ההזמנה המלא בנתיב Purchases/orderID
            databaseReference.child("Purchases").child(key).setValue(order)
                    .addOnSuccessListener(unused -> callback.onCompleted(null)) // שימוש ב-Listeners חלופיים במקום Callback רגיל
                    .addOnFailureListener(callback::onFailed);
        }

        // קבלת היסטוריית כל ההזמנות שבוצעו באפליקציה (שנמצאות תחת Purchases)
        public void getAllOrders(DatabaseCallback<List<Order>> callback) {
            // משתמש ב-Listener לפעם אחת בלבד לקריאת הנתונים מהשרת
            databaseReference.child("Purchases").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) { // ברגע שכל המידע ירד
                    List<Order> orders = new ArrayList<>(); // בניית רשימה ריקה
                    for (DataSnapshot child : snapshot.getChildren()) { // מעבר על כל הזמנה בשרת
                        orders.add(child.getValue(Order.class)); // המרת הנתונים למחלקת Order והוספה לרשימה
                    }
                    callback.onCompleted(orders); // החזרת הרשימה המלאה
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { // אם הקריאה נכשלה או בוטלה
                    callback.onFailed(error.toException());
                }
            });
        }

        // מחיקת פריט ממערכת (בפועל, מוחק מוצר מהחנות הכללית, הנתיב הראשי של items)
        public void deleteItem(@NotNull final String itemId, @Nullable final DatabaseCallback<Void> callback) {
            deleteData("items/" + itemId, callback);
        }
        // endregion
    }
}