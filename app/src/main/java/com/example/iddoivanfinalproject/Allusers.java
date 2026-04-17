package com.example.iddoivanfinalproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iddoivanfinalproject.model.User;
import com.example.iddoivanfinalproject.services.DataBaseService;

import java.util.ArrayList;
import java.util.List;

public class Allusers extends AppCompatActivity {

    ListView lvUsers;
    ArrayList<String> userDisplayList;
    ArrayAdapter<String> adapter;
    Button btnBack;
    ArrayList<User> usersList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allusers);
        usersList = new ArrayList<>();

        lvUsers = findViewById(R.id.lvUsers);
        userDisplayList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userDisplayList);
        lvUsers.setAdapter(adapter);

        // --- לחיצה רגילה: מעבר לעמוד פרטי משתמש ---
        lvUsers.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = usersList.get(position);

            Intent intent = new Intent(Allusers.this, Userdetails.class);
            intent.putExtra("userId", selectedUser.getId());
            intent.putExtra("fname", selectedUser.getFname());
            intent.putExtra("lname", selectedUser.getLname());
            intent.putExtra("email", selectedUser.getEmail());
            intent.putExtra("phoneNumber", selectedUser.getPhoneNumber());

            startActivity(intent);
        });

        // --- לחיצה ארוכה: מחיקת משתמש (התוספת החדשה) ---
        lvUsers.setOnItemLongClickListener((parent, view, position, id) -> {
            User selectedUser = usersList.get(position);

            // יצירת חלונית קופצת לאישור המחיקה
            new AlertDialog.Builder(Allusers.this)
                    .setTitle("מחיקת משתמש")
                    .setMessage("האם אתה בטוח שברצונך למחוק את המשתמש " + selectedUser.getFname() + " " + selectedUser.getLname() + "?")
                    .setPositiveButton("כן, מחק", (dialog, which) -> {
                        // קריאה לפונקציית המחיקה ב-Firebase
                        DataBaseService.DatabaseService.getInstance().deleteUser(selectedUser.getId(), new DataBaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void object) {
                                Toast.makeText(Allusers.this, "המשתמש נמחק בהצלחה!", Toast.LENGTH_SHORT).show();
                                loadUsersFromDatabase(); // רענון הרשימה מיד אחרי המחיקה
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Toast.makeText(Allusers.this, "שגיאה במחיקה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setNegativeButton("ביטול", null)
                    .show();

            return true; // מחזיר true כדי שהלחיצה הרגילה לא תופעל במקביל
        });
        // הוסף את זה בתוך onCreate
        btnBack = findViewById(R.id.btnUniversalBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    // --- הפונקציה הזו מבטיחה שהרשימה תתרענן כל פעם שתחזור למסך הזה ---
    @Override
    protected void onResume() {
        super.onResume();
        loadUsersFromDatabase();
    }

    private void loadUsersFromDatabase() {
        DataBaseService.DatabaseService.getInstance().getUserList(new DataBaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                userDisplayList.clear();
                usersList.clear();

                for (User user : users) {
                    // --- כאן הסינון: מוסיפים רק אם המשתמש הוא לא אדמין ---
                    if (!user.isAdmin()) {
                        usersList.add(user);

                        String display = user.getFname() + " " + user.getLname() + "\n" +
                                "Email: " + user.getEmail() + "\n" +
                                "Phone: " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "אין מספר");
                        userDisplayList.add(display);
                    }
                }

                adapter.notifyDataSetChanged(); // מעדכן את המסך
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(Allusers.this, "שגיאה בטעינת המשתמשים", Toast.LENGTH_SHORT).show();
            }
        });
    }
}