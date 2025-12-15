package com.example.iddoivanfinalproject;

import android.os.Bundle;
import android.widget.ArrayAdapter;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allusers);

        lvUsers = findViewById(R.id.lvUsers);
        userDisplayList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userDisplayList);
        lvUsers.setAdapter(adapter);

        // קבלת כל המשתמשים
        DataBaseService.DatabaseService.getInstance().getUserList(new DataBaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                userDisplayList.clear();
                for (User user : users) {
                    String display = user.getFname() + " " + user.getLname() + "\n" +
                            "Email: " + user.getEmail() + "\n" ;
                    userDisplayList.add(display);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(Allusers.this, "שגיאה בטעינת המשתמשים", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


