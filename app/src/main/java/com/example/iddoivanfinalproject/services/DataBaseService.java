package com.example.iddoivanfinalproject.services;

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

public class DataBaseService {
    private static final String TAG = "DatabaseService";

    private static final String USERS_PATH = "users",
            ITEM_PATH = "items",
            COMPARE_PATH = "compare",
            CARTS_PATH = "carts";

    public interface DatabaseCallback<T> {
        void onCompleted(T object);
        void onFailed(Exception e);
    }

    public static class DatabaseService {
        private static DatabaseService instance;
        private final DatabaseReference databaseReference;

        private DatabaseService() {
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference();
        }

        public static DatabaseService getInstance() {
            if (instance == null) {
                instance = new DatabaseService();
            }
            return instance;
        }

        // region Generic Private Methods
        private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
            readData(path).setValue(data, (error, ref) -> {
                if (error != null) {
                    if (callback != null) callback.onFailed(error.toException());
                } else {
                    if (callback != null) callback.onCompleted(null);
                }
            });
        }

        private void deleteData(@NotNull final String path, @Nullable final DatabaseCallback<Void> callback) {
            readData(path).removeValue((error, ref) -> {
                if (error != null) {
                    if (callback != null) callback.onFailed(error.toException());
                } else {
                    if (callback != null) callback.onCompleted(null);
                }
            });
        }

        private DatabaseReference readData(@NotNull final String path) {
            return databaseReference.child(path);
        }

        private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
            readData(path).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    callback.onFailed(task.getException());
                    return;
                }
                T data = task.getResult().getValue(clazz);
                callback.onCompleted(data);
            });
        }

        private <T> void getDataList(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<List<T>> callback) {
            readData(path).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    callback.onFailed(task.getException());
                    return;
                }
                List<T> tList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                    T t = dataSnapshot.getValue(clazz);
                    if (t != null) tList.add(t);
                }
                callback.onCompleted(tList);
            });
        }

        private String generateNewId(@NotNull final String path) {
            return databaseReference.child(path).push().getKey();
        }

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

        // region User Section
        public void createNewUser(@NotNull final User user, @Nullable final DatabaseCallback<String> callback) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();
                            user.setId(uid);
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

        public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
            getData(USERS_PATH + "/" + uid, User.class, callback);
        }

        public void getUserList(@NotNull final DatabaseCallback<List<User>> callback) {
            getDataList(USERS_PATH, User.class, callback);
        }

        public void deleteUser(@NotNull final String uid, @Nullable final DatabaseCallback<Void> callback) {
            deleteData(USERS_PATH + "/" + uid, callback);
        }
        // endregion

        // region Item Section
        public void createNewItem(@NotNull final Item item, @Nullable final DatabaseCallback<Void> callback) {
            writeData(ITEM_PATH + "/" + item.getId(), item, callback);
        }

        public void getAllItems(@NotNull final DatabaseCallback<List<Item>> callback) {
            getDataList(ITEM_PATH, Item.class, callback);
        }

        public String generateItemId() {
            return generateNewId(ITEM_PATH);
        }
        // endregion

        // region Cart Section
        public void createNewCart(@NotNull final Cart cart, @Nullable final DatabaseCallback<Void> callback) {
            // שמירה בנתיב: carts / userId / cartId
            writeData(CARTS_PATH + "/" + cart.getUserId() + "/" + cart.getId(), cart, callback);
        }

        public void getCartList(String userId, @NotNull final DatabaseCallback<List<Cart>> callback) {
            // משיכת רשימה רק עבור המשתמש המחובר
            getDataList(CARTS_PATH + "/" + userId, Cart.class, callback);
        }

        public void deleteCartItem(@NotNull final String userId, @NotNull final String cartId, @Nullable final DatabaseCallback<Void> callback) {
            // מחיקה מהנתיב הספציפי של המשתמש
            deleteData(CARTS_PATH + "/" + userId + "/" + cartId, callback);
        }
        // פונקציה לשליפת פריט בודד מהחנות לפי ה-ID שלו
        public void getItemById(@NotNull final String itemId, @NotNull final DatabaseCallback<Item> callback) {
            // אנחנו משתמשים בנתיב items/itemId ומחזירים אובייקט מסוג Item
            getData(ITEM_PATH + "/" + itemId, Item.class, callback);
        }

        public String generateCartId() {
            return generateNewId(CARTS_PATH);
        }
        // endregion

        // region Compare Section
        public void createNewCompareList(@NotNull final Compareitem compareitem, @Nullable final DatabaseCallback<Void> callback) {
            String userid = FirebaseAuth.getInstance().getUid();
            if(userid != null) {
                writeData(COMPARE_PATH + "/" + userid + "/" + compareitem.getType(), compareitem, callback);
            }
        }
        public String generateCompareId() {

            return generateNewId(COMPARE_PATH);

        }
        /// פונקציה חדשה לעדכון שדות ספציפיים של משתמש מבלי לדרוס את כל האובייקט (שומר על הסיסמה)
        public void updateUserFields(@NotNull String userId, String fname, String lname, String email, String phone, @Nullable final DatabaseCallback<Void> callback) {

            // יצירת מפה (Map) שמכילה רק את השדות שאנחנו רוצים לעדכן
            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("fname", fname);
            updates.put("lname", lname);
            updates.put("email", email);
            updates.put("phoneNumber", phone);

            // עדכון השדות ב-Firebase
            readData(USERS_PATH + "/" + userId).updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (callback != null) callback.onCompleted(null);
                } else {
                    if (callback != null) callback.onFailed(task.getException());
                }
            });
        }

        public void getCompareByType(@NotNull final String type, @NotNull final DatabaseCallback<Compareitem> callback) {
            String userid = FirebaseAuth.getInstance().getUid();
            if(userid != null) {
                getData(COMPARE_PATH + "/" + userid + "/" + type, Compareitem.class, callback);
            }
        }
        public void updateCompareList(@NotNull final Compareitem compareitem, @Nullable final DatabaseCallback<Void> callback) {

            FirebaseAuth mAuth = FirebaseAuth.getInstance();

            if(mAuth.getCurrentUser() != null) {

                String userid = mAuth.getCurrentUser().getUid();

                writeData(COMPARE_PATH + "/" + userid + "/" + compareitem.getType(), compareitem, callback);

            }

        }
        // פונקציה למחיקת כל העגלה של משתמש ספציפי בבת אחת
        public void clearUserCart(@NotNull final String userId, @Nullable final DatabaseCallback<Void> callback) {
            // מוחק את כל הנתיב: carts / userId
            deleteData(CARTS_PATH + "/" + userId, callback);
        }
        // בתוך מחלקת DatabaseService
        public void saveOrder(Order order, DatabaseCallback<Void> callback) {
            // יצירת מזהה ייחודי להזמנה
            String key = databaseReference.child("Purchases").push().getKey();
            order.setId(key);

            databaseReference.child("Purchases").child(key).setValue(order)
                    .addOnSuccessListener(unused -> callback.onCompleted(null))
                    .addOnFailureListener(callback::onFailed);
        }

        // פונקציה עבור המנהל לצפייה בכל ההיסטוריה
        public void getAllOrders(DatabaseCallback<List<Order>> callback) {
            databaseReference.child("Purchases").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Order> orders = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        orders.add(child.getValue(Order.class));
                    }
                    callback.onCompleted(orders);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onFailed(error.toException());
                }
            });
        }
        // הוסף את הפונקציה הזו ל-DataBaseService
        public void deleteItem(String itemId, DatabaseCallback<Void> callback) {
            // מוחק את הפריט מצומת ה-Items לפי ה-ID שלו
            databaseReference.child("Items").child(itemId).removeValue()
                    .addOnSuccessListener(aVoid -> callback.onCompleted(null))
                    .addOnFailureListener(e -> callback.onFailed(e));
        }




        // endregion
    }
}