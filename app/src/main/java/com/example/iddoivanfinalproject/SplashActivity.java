package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {
    private ImageView myImageView;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_splash);
            myImageView=(ImageView)findViewById(R.id.imageView);

            Thread mSplashThread = new Thread() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            Animation myFadeInAnimation= AnimationUtils.loadAnimation(SplashActivity.this, R.anim.anim);
                            wait(3000);
                        }
                    } catch (InterruptedException ex) {
                        finish();
                    }

                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            };

            mSplashThread.start();
        }
    }
