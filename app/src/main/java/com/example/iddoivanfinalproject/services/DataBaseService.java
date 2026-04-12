package com.example.iddoivanfinalproject.services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.iddoivanfinalproject.model.Cart;
import com.example.iddoivanfinalproject.model.Compareitem;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

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
        public void onCompleted(T object);
        public void onFailed(Exception e);
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

        // region private generic methods

        private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
            readData(path).setValue(data, (error, ref) -> {
                if (error != null) {
                    if (callback == null) return;
                    callback.onFailed(error.toException());
                } else {
                    if (callback == null) return;
                    callback.onCompleted(null);
                }
            });
        }

        private void deleteData(@NotNull final String path, @Nullable final DatabaseCallback<Void> callback) {
            readData(path).removeValue((error, ref) -> {
                if (error != null) {
                    if (callback == null) return;
                    callback.onFailed(error.toException());
                } else {
                    if (callback == null) return;
                    callback.onCompleted(null);
                }
            });
        }

        private DatabaseReference readData(@NotNull final String path) {
            return databaseReference.child(path);
        }

        private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
            readData(path).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Error getting data", task.getException());
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
                    Log.e(TAG, "Error getting data", task.getException());
                    callback.onFailed(task.getException());
                    return;
                }
                List<T> tList = new ArrayList<>();
                task.getResult().getChildren().forEach(dataSnapshot -> {
                    T t = dataSnapshot.getValue(clazz);
                    tList.add(t);
                });

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
                    if (currentValue == null) {
                        currentValue = function.apply(null);
                    } else {
                        currentValue = function.apply(currentValue);
                    }
                    currentData.setValue(currentValue);
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (error != null) {
                        Log.e(TAG, "Transaction failed", error.toException());
                        callback.onFailed(error.toException());
                        return;
                    }
                    T result = currentData != null ? currentData.getValue(clazz) : null;
                    callback.onCompleted(result);
                }
            });
        }

        // endregion of private methods

        // region User Section
        public void createNewUser(@NotNull final User user,
                                  @Nullable final DatabaseCallback<String> callback) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "createUserWithEmail:success");
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            user.setId(uid);
                            writeData(USERS_PATH + "/" + uid, user, new DatabaseCallback<Void>() {
                                @Override
                                public void onCompleted(Void v) {
                                    if (callback != null) callback.onCompleted(uid);
                                }

                                @Override
                                public void onFailed(Exception e) {
                                    if (callback != null) callback.onFailed(e);
                                }
                            });
                        } else {
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            if (callback != null)
                                callback.onFailed(task.getException());
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

        public void getUserByEmailAndPassword(@NotNull final String email, @NotNull final String password, @NotNull final DatabaseCallback<User> callback) {
            readData(USERS_PATH).orderByChild("email").equalTo(email).get()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Error getting data", task.getException());
                            callback.onFailed(task.getException());
                            return;
                        }
                        if (task.getResult().getChildrenCount() == 0) {
                            callback.onFailed(new Exception("User not found"));
                            return;
                        }
                        for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user == null || !Objects.equals(user.getPassword(), password)) {
                                callback.onFailed(new Exception("Invalid email or password"));
                                return;
                            }

                            callback.onCompleted(user);
                            return;
                        }
                    });
        }

        public void checkIfEmailExists(@NotNull final String email, @NotNull final DatabaseCallback<Boolean> callback) {
            readData(USERS_PATH).orderByChild("email").equalTo(email).get()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Error getting data", task.getException());
                            callback.onFailed(task.getException());
                            return;
                        }
                        boolean exists = task.getResult().getChildrenCount() > 0;
                        callback.onCompleted(exists);
                    });
        }

        public void updateUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
            runTransaction(USERS_PATH + "/" + user.getId(), User.class, currentUser -> user, new DatabaseCallback<User>() {
                @Override
                public void onCompleted(User object) {
                    if (callback != null) {
                        callback.onCompleted(null);
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    if (callback != null) {
                        callback.onFailed(e);
                    }
                }
            });
        }

        public void createNewItem(@NotNull final Item item, @Nullable final DatabaseCallback<Void> callback) {
            writeData("items/" + item.getId(), item, callback);
        }

        public String generateItemId() {
            return generateNewId(ITEM_PATH);
        }
        // endregion User Section

        // region cart section

        // --- התיקון: הפונקציה שומרת את העגלה תחת מזהה המשתמש ---
        public void createNewCart(@NotNull final Cart cart, @Nullable final DatabaseCallback<Void> callback) {
            if (cart.getUserId() == null || cart.getUserId().isEmpty()) {
                if(callback != null) callback.onFailed(new Exception("User ID is missing in Cart"));
                return;
            }
            // הנתיב עכשיו הוא: carts / מזהה_המשתמש / מזהה_המוצר_בעגלה
            writeData(CARTS_PATH + "/" + cart.getUserId() + "/" + cart.getId(), cart, callback);
        }

        // --- התיקון: הפונקציה מושכת רק את העגלה של המשתמש הספציפי ---
        public void getCartList(String userId, @NotNull final DatabaseCallback<List<Cart>> callback) {
            getDataList(CARTS_PATH + "/" + userId, Cart.class, callback);
        }

        public void getAllItems(@NotNull final DatabaseCallback<List<Item>> callback) {
            readData("items").get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    callback.onFailed(task.getException());
                    return;
                }
                List<Item> itemList = new ArrayList<>();
                task.getResult().getChildren().forEach(dataSnapshot -> {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null) {
                        item.setId(dataSnapshot.getKey());
                        itemList.add(item);
                    }
                });
                callback.onCompleted(itemList);
            });
        }

        public void getItemById(@NotNull final String itemId, @NotNull final DatabaseCallback<Item> callback) {
            getData("items/" + itemId, Item.class, callback);
        }

        public void getItem(@NotNull final String itemId, @NotNull final DatabaseCallback<Item> callback) {
            getData(ITEM_PATH + "/" + itemId, Item.class, callback);
        }

        public String generateCartId() {
            return generateNewId(CARTS_PATH);
        }

        // --- התיקון: מחיקת פריט מהעגלה לפי משתמש ---
        public void deleteCartItem(@NotNull final String userId, @NotNull final String cartId, @Nullable final DatabaseCallback<Void> callback) {
            deleteData(CARTS_PATH + "/" + userId + "/" + cartId, callback);
        }
        // endregion cart section

        // region Compare section
        public void createNewCompareList(@NotNull final Compareitem compareitem, @Nullable final DatabaseCallback<Void> callback) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if(mAuth.getCurrentUser() != null) {
                String userid = mAuth.getCurrentUser().getUid();
                writeData(COMPARE_PATH + "/" + userid + "/" + compareitem.getType(), compareitem, callback);
            }
        }

        public void updateCompareList(@NotNull final Compareitem compareitem, @Nullable final DatabaseCallback<Void> callback) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if(mAuth.getCurrentUser() != null) {
                String userid = mAuth.getCurrentUser().getUid();
                writeData(COMPARE_PATH + "/" + userid + "/" + compareitem.getType(), compareitem, callback);
            }
        }

        public String generateCompareId() {
            return generateNewId(COMPARE_PATH);
        }

        public void getCompareByType(@NotNull final String type, @NotNull final DatabaseCallback<Compareitem> callback) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if(mAuth.getCurrentUser() != null) {
                String userid = mAuth.getCurrentUser().getUid();
                getData(COMPARE_PATH + "/" + userid + "/" + type, Compareitem.class, callback);
            }
        }
        // endregion Compare section
    }
}