package com.example.quizfragments.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizfragments.R;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DISPLAY_LENGTH = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find views to animate
        ImageView logoImage = findViewById(R.id.splash_logo);
        TextView appNameText = findViewById(R.id.app_name_text);
        TextView taglineText = findViewById(R.id.app_tagline);

        // Create fade-in animation
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);

        // Apply animations
        logoImage.startAnimation(fadeIn);
        appNameText.startAnimation(fadeIn);
        taglineText.startAnimation(fadeIn);

        // Perform any initialization tasks here
        // For example, preload data, check login status, etc.
        preloadAppData();

        // Navigate to MainActivity after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void preloadAppData() {
        // Here you can:
        // 1. Initialize database if needed
        // 2. Check login status
        // 3. Load user preferences
        // 4. Preload initial data

        // This runs on the main thread, so avoid heavy operations
        // For heavy tasks, use a background thread or coroutines
    }
}
