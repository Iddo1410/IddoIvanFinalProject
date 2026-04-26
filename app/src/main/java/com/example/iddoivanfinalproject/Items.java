package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iddoivanfinalproject.adapter.ItemAdapter;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.model.User;
import com.example.iddoivanfinalproject.services.DataBaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class Items extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private DataBaseService.DatabaseService databaseService;
    private Button btnGoToCart;
    private Spinner spTypeFilter;

    private ArrayList<Item> allItemsList = new ArrayList<>();
    private ArrayList<Item> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        // 1. אתחול רכיבים
        recyclerView = findViewById(R.id.rvItems);
        btnGoToCart = findViewById(R.id.btnGoToCart);
        spTypeFilter = findViewById(R.id.spTypeFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseService = DataBaseService.DatabaseService.getInstance();

        // 2. הגדרת האדפטר (RecyclerView)
        // תיקון: העברנו רק את הרשימה. הלחיצה כבר מטופלת בתוך ה-ItemAdapter שתיקנו קודם!
        adapter = new ItemAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        setupSpinner();

        // 3. הגדרת כפתור עגלה - מוסתר כברירת מחדל (GONE)
        if (btnGoToCart != null) {
            btnGoToCart.setVisibility(View.GONE);
            btnGoToCart.setOnClickListener(v -> {
                Intent intent = new Intent(Items.this, CartActivity.class);
                startActivity(intent);
            });
        }

        // 4. בדיקת סטטוס אדמין/משתמש והצגת הכפתור בהתאם
        checkUserStatus();
    }

    // פונקציה שקוראת לרשימה בכל פעם שחוזרים למסך (למשל אחרי מחיקה)
    @Override
    protected void onResume() {
        super.onResume();
        loadItemsFromDatabase();
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();

            // משיכת פרטי המשתמש מה-Database כדי לבדוק את השדה isAdmin
            databaseService.getUser(uid, new DataBaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    if (user != null) {
                        if (user.isAdmin()) {
                            // זה אדמין - הכפתור נשאר חבוי
                            if (btnGoToCart != null) btnGoToCart.setVisibility(View.GONE);
                        } else {
                            // זה משתמש רגיל - מציגים את העגלה
                            if (btnGoToCart != null) btnGoToCart.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    // במקרה של תקלה בטעינה, נשאיר מוסתר ליתר ביטחון
                    if (btnGoToCart != null) btnGoToCart.setVisibility(View.GONE);
                }
            });
        } else {
            if (btnGoToCart != null) btnGoToCart.setVisibility(View.GONE);
        }
    }

    private void setupSpinner() {
        // וודא שבקובץ arrs.xml המערך שלך נקרא typeArr
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.typeArr, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTypeFilter.setAdapter(spinnerAdapter);

        spTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                filterItems(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void loadItemsFromDatabase() {
        databaseService.getAllItems(new DataBaseService.DatabaseCallback<List<Item>>() {
            @Override
            public void onCompleted(List<Item> items) {
                if (items != null) {
                    allItemsList.clear();
                    allItemsList.addAll(items);

                    // רענון הסינון הנוכחי
                    if (spTypeFilter.getSelectedItem() != null) {
                        filterItems(spTypeFilter.getSelectedItem().toString());
                    } else {
                        filteredList.clear();
                        filteredList.addAll(allItemsList);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("ItemsPage", "Failed to load items", e);
            }
        });
    }

    private void filterItems(String type) {
        filteredList.clear();

        // תיקון: הגנה במקרה שהמשתמש בוחר להציג "הכל"
        // שים לב: אם במערך שלך (arrs.xml) קוראים לזה אחרת למשל "All", שנה את "הכל" למילה המתאימה
        if (type.equals("הכל") || type.equals("All Categories") || type.isEmpty()) {
            filteredList.addAll(allItemsList);
        } else {
            for (Item item : allItemsList) {
                if (item.getType() != null && item.getType().equals(type)) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}