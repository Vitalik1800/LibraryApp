package com.example.libraryapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.libraryapp.databinding.ActivitySplashBinding;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;
    SharedPreferences preferences;
    String token, role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        token = preferences.getString("token", null);
        role = preferences.getString("role", "user");

        new Handler().postDelayed(() -> {
            if(token != null){
                if("admin".equalsIgnoreCase(role)){
                    startActivity(new Intent(SplashActivity.this, AdminActivity.class));
                } else{
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
            } else{
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 2000);
    }
}