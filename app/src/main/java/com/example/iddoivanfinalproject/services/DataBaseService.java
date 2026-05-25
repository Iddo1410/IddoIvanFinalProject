// הגדרת החבילה (Package) שבה נמצא הקובץ, תחת תיקיית השירותים (services)
package com.example.iddoivanfinalproject.services;

// ייבוא כל הספריות הנדרשות, כולל מודלים של האפליקציה ורכיבי Firebase
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.iddoivanfinalproject.model.Cart;
import com.example.iddoivanfinalproject.model.Compareitem;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.model.Order;
import com.example.iddoivanfinalproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

// מחלקה העוטפת את שירותי מסד הנתונים
public class DataBaseService {
    // תגית המשמשת להדפסות ללוג (לצורכי מעקב ודיבאגינג)
    private static final String TAG = "DatabaseService";

    // הגדרת שמות הנתיבים (תיקיות/טבלאות) המרכזיים בתוך מסד הנתונים של Firebase
    private static final String USERS_PATH = "users",
            ITEM_PATH = "items",
            COMPARE_PATH = "compare",
            CARTS_PATH = "carts";

    // ממשק (Interface) המשמש כ-Callback.
    // מכיוון שפניות ל-Firebase מתבצעות ברקע, אנחנו צריכים דרך "להודיע" לאפליקציה כשהפעולה הסתיימה.
    public interface DatabaseCallback<T> {
        void onCompleted(T object);     // מופעל כשהפעולה מסתיימת בהצלחה (מחזיר את המידע שביקשנו)
        void onFailed(Exception e);     // מופעל במקרה של שגיאה
    }

    // מחלקה פנימית סטטית המנהלת את הקשר מול Firebase בפועל
    public static class DatabaseService {

        // שימוש בתבנית Singleton - ייווצר רק מופע אחד של מחלקה זו בכל זמן ריצת האפליקציה
        private static DatabaseService instance;
        // אובייקט מסוג DatabaseReference המצביע על השורש של מסד הנתונים
        private final DatabaseReference databaseReference;

        // בנאי פרטי (Private) - מופעל פעם אחת. מאתחל את החיבור למסד הנתונים
        private DatabaseService() {
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference();
        }

        // מתודה שמחזירה את המופע היחיד של המחלקה. אם הוא עדיין לא נוצר - היא יוצרת אותו.
        public static DatabaseService getInstance() {
            if (instance == null) {
                instance = new DatabaseService();
            }
            return instance;
        }

        // ==========================================
        // region Generic Private Methods (פעולות גנריות פנימיות למסד הנתונים)
        // ==========================================

        // פונקציה פנימית לכתיבת נתונים לתוך נתיב (path) ספציפי
        private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
            readData(path).setValue(data, (error, ref) -> {
                if (error != null) {
                    // במקרה של שגיאה, קוראים לפונקציית onFailed של ה-callback
                    if (callback != null) callback.onFailed(error.toException());
                } else {
                    // במקרה של הצלחה, קוראים ל-onCompleted
                    if (callback != null) callback.onCompleted(null);
                }
            });
        }

        // פונקציה פנימית למחיקת נתונים מנתיב מסוים
        private void deleteData(@NotNull final String path, @Nullable final DatabaseCallback<Void> callback) {
            readData(path).removeValue((error, ref) -> {
                if (error != null) {
                    if (callback != null) callback.onFailed(error.toException());
                } else {
                    if (callback != null) callback.onCompleted(null);
                }
            });
        }

        // פונקציה פנימית המחזירה הפנייה (Reference) לנתיב ספציפי במסד הנתונים
        private DatabaseReference readData(@NotNull final String path) {
            return databaseReference.child(path);
        }

        // פונקציה גנרית לשליפת פריט בודד מהמסד והמרתו לאובייקט מהסוג המבוקש (clazz)
        private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
            readData(path).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    callback.onFailed(task.getException());
                    return;
                }
                // שאיבת המידע מה-snapshot והמרתו למחלקה שביקשנו (למשל User.class)
                T data = task.getResult().getValue(clazz);
                callback.onCompleted(data);
            });
        }

        // פונקציה גנרית לשליפת רשימה (List) של פריטים מתוך נתיב
        private <T> void getDataList(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<List<T>> callback) {
            readData(path).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    callback.onFailed(task.getException());
                    return;
                }
                List<T> tList = new ArrayList<>();
                // מעבר על כל הילדים (children) בתוך הנתיב והוספתם לרשימה
                for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                    T t = dataSnapshot.getValue(clazz);
                    if (t != null) tList.add(t);
                }
                callback.onCompleted(tList);
            });
        }

        // פונקציה המייצרת מזהה (ID) חדש וייחודי בתוך נתיב מסוים (משתמשת בפונקציה push של פיירבייס)
        private String generateNewId(@NotNull final String path) {
            return databaseReference.child(path).push().getKey();
        }

        // פונקציה להפעלת טרנזקציה (פעולה בטוחה בסביבה מרובת משתמשים כדי למנוע דריסת נתונים בו זמנית)
        private <T> void runTransaction(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull UnaryOperator<T> function, @NotNull final DatabaseCallback<T> callback) {
            readData(path).runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    T currentValue = currentData.getValue(clazz);
                    currentValue = function.apply(currentValue);
                    currentData.setValue(currentValue);
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (error != null) {
                        callback.onFailed(error.toException());
                        return;
                    }
                    T result = currentData != null ? currentData.getValue(clazz) : null;
                    callback.onCompleted(result);
                }
            });
        }
        // endregion

        // ==========================================
        // region User Section (פעולות הקשורות למשתמשים)
        // ==========================================

        // יצירת משתמש חדש - קודם כל במערכת ההזדהות (Auth) ולאחר מכן שמירת פרטיו במסד הנתונים
        public void createNewUser(@NotNull final User user, @Nullable final DatabaseCallback<String> callback) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            // יצירת יוזר עם אימייל וסיסמה ב-Firebase Authentication
            mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // אם ההרשמה הצליחה, שולפים את ה-ID שנוצר
                            String uid = mAuth.getCurrentUser().getUid();
                            user.setId(uid);
                            // כותבים את אובייקט המשתמש לטבלת "users" תחת ה-ID שלו
                            writeData(USERS_PATH + "/" + uid, user, new DatabaseCallback<Void>() {
                                @Override
                                public void onCompleted(Void v) { if (callback != null) callback.onCompleted(uid); }
                                @Override
                                public void onFailed(Exception e) { if (callback != null) callback.onFailed(e); }
                            });
                        } else {
                            if (callback != null) callback.onFailed(task.getException());
                        }
                    });
        }

        // שליפת משתמש ספציפי לפי מזהה (uid)
        public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
            getData(USERS_PATH + "/" + uid, User.class, callback);
        }

        // שליפת רשימת כל המשתמשים (למנהל לדוגמה)
        public void getUserList(@NotNull final DatabaseCallback<List<User>> callback) {
            getDataList(USERS_PATH, User.class, callback);
        }

        // מחיקת משתמש ממסד הנתונים לפי uid
        public void deleteUser(@NotNull final String uid, @Nullable final DatabaseCallback<Void> callback) {
            deleteData(USERS_PATH + "/" + uid, callback);
        }
        // endregion

        // ==========================================
        // region Item Section (פעולות הקשורות למוצרים בחנות)
        // ==========================================

        // יצירת מוצר חדש ושמירתו בנתיב "items"
        public void createNewItem(@NotNull final Item item, @Nullable final DatabaseCallback<Void> callback) {
            writeData(ITEM_PATH + "/" + item.getId(), item, callback);
        }

        // שליפת כל המוצרים מהחנות
        public void getAllItems(@NotNull final DatabaseCallback<List<Item>> callback) {
            getDataList(ITEM_PATH, Item.class, callback);
        }

        // יצירת מזהה (ID) חדש למוצר בעת הוספת מוצר חדש
        public String generateItemId() {
            return generateNewId(ITEM_PATH);
        }
        // endregion

        // ==========================================
        // region Cart Section (פעולות הקשורות לעגלת הקניות)
        // ==========================================

        // הוספת פריט לעגלת הקניות של משתמש ספציפי
        public void createNewCart(@NotNull final Cart cart, @Nullable final DatabaseCallback<Void> callback) {
            // שמירה בנתיב: carts / userId / cartId
            writeData(CARTS_PATH + "/" + cart.getUserId() + "/" + cart.getId(), cart, callback);
        }

        // שליפת רשימת הפריטים שבעגלה עבור משתמש מסוים
        public void getCartList(String userId, @NotNull final DatabaseCallback<List<Cart>> callback) {
            // משיכת רשימה רק עבור המשתמש המחובר
            getDataList(CARTS_PATH + "/" + userId, Cart.class, callback);
        }

        // מחיקת פריט ספציפי מתוך העגלה של משתמש
        public void deleteCartItem(@NotNull final String userId, @NotNull final String cartId, @Nullable final DatabaseCallback<Void> callback) {
            // מחיקה מהנתיב הספציפי של המשתמש
            deleteData(CARTS_PATH + "/" + userId + "/" + cartId, callback);
        }

        // פונקציה לשליפת פריט בודד מהחנות (ולא מהעגלה) לפי ה-ID שלו
        public void getItemById(@NotNull final String itemId, @NotNull final DatabaseCallback<Item> callback) {
            // אנחנו משתמשים בנתיב items/itemId ומחזירים אובייקט מסוג Item
            getData(ITEM_PATH + "/" + itemId, Item.class, callback);
        }

        // יצירת מזהה חדש עבור פריט בעגלת הקניות
        public String generateCartId() {
            return generateNewId(CARTS_PATH);
        }
        // endregion

        // ==========================================
        // region Compare Section (פעולות הקשורות להשוואת מוצרים)
        // ==========================================

        // יצירה או שמירה של רשימת השוואה של משתמש
        public void createNewCompareList(@NotNull final Compareitem compareitem, @Nullable final DatabaseCallback<Void> callback) {
            // קבלת מזהה המשתמש המחובר כעת
            String userid = FirebaseAuth.getInstance().getUid();
            if(userid != null) {
                // שמירת ההשוואה תחת compare / userid / type
                writeData(COMPARE_PATH + "/" + userid + "/" + compareitem.getType(), compareitem, callback);
            }
        }

        // ייצור מזהה חדש לרשימת השוואה
        public String generateCompareId() {
            return generateNewId(COMPARE_PATH);
        }

        // פונקציה חדשה לעדכון שדות ספציפיים של משתמש מבלי לדרוס את כל האובייקט (למשל, עדכון פרטים מבלי לשנות סיסמה)
        public void updateUserFields(@NotNull String userId, String fname, String lname, String email, String phone, @Nullable final DatabaseCallback<Void> callback) {

            // יצירת מפה (Map) שמכילה רק את השדות שאנחנו רוצים לעדכן (מילון של מפתח וערך)
            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("fname", fname);
            updates.put("lname", lname);
            updates.put("email", email);
            updates.put("phoneNumber", phone);

            // הפעלת פונקציית updateChildren המעדכנת רק את השדות שהעברנו אליה (לא מוחקת שדות אחרים)
            readData(USERS_PATH + "/" + userId).updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (callback != null) callback.onCompleted(null);
                } else {
                    if (callback != null) callback.onFailed(task.getException());
                }
            });
        }

        // שליפת רשימת ההשוואה של משתמש לפי סוג מוצר (type)
        public void getCompareByType(@NotNull final String type, @NotNull final DatabaseCallback<Compareitem> callback) {
            String userid = FirebaseAuth.getInstance().getUid();
            if(userid != null) {
                getData(COMPARE_PATH + "/" + userid + "/" + type, Compareitem.class, callback);
            }
        }

        // עדכון קיימת של רשימת השוואה (פועלת בדומה ליצירה כי היא דורסת את הקיים באותו נתיב)
        public void updateCompareList(@NotNull final Compareitem compareitem, @Nullable final DatabaseCallback<Void> callback) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if(mAuth.getCurrentUser() != null) {
                String userid = mAuth.getCurrentUser().getUid();
                writeData(COMPARE_PATH + "/" + userid + "/" + compareitem.getType(), compareitem, callback);
            }
        }

        // פונקציה למחיקת כל העגלה של משתמש ספציפי בבת אחת (מופעל בדרך כלל לאחר השלמת קנייה)
        public void clearUserCart(@NotNull final String userId, @Nullable final DatabaseCallback<Void> callback) {
            // מוחק את כל הנתיב: carts / userId - מה שמנקה את כל פריטי העגלה שמתחתיו
            deleteData(CARTS_PATH + "/" + userId, callback);
        }

        // ==========================================
        // region Orders Section (פעולות של הזמנות/רכישות)
        // ==========================================

        // שמירת הזמנה חדשה (רכישה) במסד הנתונים
        public void saveOrder(Order order, DatabaseCallback<Void> callback) {
            // יצירת מזהה ייחודי להזמנה בתוך טבלת "Purchases"
            String key = databaseReference.child("Purchases").push().getKey();
            order.setId(key); // עדכון ה-ID בתוך האובייקט

            // שמירת ההזמנה במסד הנתונים
            databaseReference.child("Purchases").child(key).setValue(order)
                    .addOnSuccessListener(unused -> callback.onCompleted(null)) // הצלחה
                    .addOnFailureListener(callback::onFailed); // כישלון
        }

        // פונקציה עבור המנהל (Admin) לצפייה בכל ההיסטוריה של כל הרכישות באפליקציה
        public void getAllOrders(DatabaseCallback<List<Order>> callback) {
            // האזנה חד-פעמית למשיכת הנתונים מטבלת הרכישות
            databaseReference.child("Purchases").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Order> orders = new ArrayList<>();
                    // מעבר על כל הרכישות במסד והוספתן לרשימה
                    for (DataSnapshot child : snapshot.getChildren()) {
                        orders.add(child.getValue(Order.class));
                    }
                    callback.onCompleted(orders); // שליחת הרשימה המלאה חזרה למסך שביקש
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // טיפול בשגיאה במידה והקריאה בוטלה או נכשלה
                    callback.onFailed(error.toException());
                }
            });
        }

        // פונקציה למחיקת מוצר מתוך טבלת המוצרים (למנהל שמסיר מוצר מהחנות)
        public void deleteItem(@NotNull final String itemId, @Nullable final DatabaseCallback<Void> callback) {
            deleteData("items/" + itemId, callback);
        }
        // endregion
    }
}