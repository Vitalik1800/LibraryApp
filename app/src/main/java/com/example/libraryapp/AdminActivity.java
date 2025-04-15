package com.example.libraryapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.libraryapp.databinding.ActivityAdminBinding;

public class AdminActivity extends AppCompatActivity {

    ActivityAdminBinding binding;
    int userId;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getUserId();
        Log.d("AdminActivity", "UserId: " + userId);

        binding.scheduleHeaderTextView.setText("Адміністратор з №: " + userId);

        binding.btnToBook.setOnClickListener(v -> startActivity(new Intent(this, BooksActivity.class)));
        binding.btnToTransactions.setOnClickListener(v -> startActivity(new Intent(this, TransactionsActivity.class)));
        binding.btnToUsers.setOnClickListener(v -> startActivity(new Intent(this, UsersActivity.class)));
        binding.btnToLogs.setOnClickListener(v -> startActivity(new Intent(this, UserLogsActivity.class)));
        binding.logout.setOnClickListener(v -> logout());
    }

    private int getUserId() {
        SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        return preferences.getInt("userId", -1);
    }

    private void logout() {
        SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(AdminActivity.this, "Ви вийшли з облікового запису", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}