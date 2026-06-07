package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // אנו דורסים את setContentView כך שיטען קודם כל את ה-Drawer ורק אז את התוכן של הדף הספציפי
    @Override
    public void setContentView(int layoutResID) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout contentFrame = drawerLayout.findViewById(R.id.content_frame);

        getLayoutInflater().inflate(layoutResID, contentFrame, true);
        super.setContentView(drawerLayout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // הטיפול בלחיצות על התפריט
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, AdminPage.class)); // או MainActivity, תלוי לאן תרצה להפנות
        } else if (id == R.id.nav_shop) {
            startActivity(new Intent(this, Items.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, Userdetails.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, About.class));
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // פונקציה לפתיחת התפריט דרך כפתור במסך
    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }
}
