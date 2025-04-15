package com.example.libraryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.libraryapp.databinding.ActivityRegisterBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    String URL = "http://10.0.2.2:3000/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser(){
        String userName = binding.username.getText().toString();
        String userEmail = binding.email.getText().toString();
        String userPassword = binding.password.getText().toString();

        if(userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()){
            Toast.makeText(this, "Будь ласка заповніть всі поля!", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject postData = new JSONObject();
        try{
            postData.put("username", userName);
            postData.put("email", userEmail);
            postData.put("password", userPassword);
        }catch (JSONException e){
            Log.e("RegisterActivity", "Error: " + e.getMessage());
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, postData,
                response -> {
                    Log.d("RegisterActivity", "Відповідь сервера: " + response.toString());
                    Toast.makeText(this, "Реєстрація успішна!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, LoginActivity.class));
                },
                error -> {
                    Log.e("RegisterError", "Помилка: " + error.getMessage());
                    Toast.makeText(this, "Реєстрація неуспішна!", Toast.LENGTH_LONG).show();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}