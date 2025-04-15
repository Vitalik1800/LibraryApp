package com.example.libraryapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.libraryapp.databinding.ActivityAddTransactionsBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class AddTransactionsActivity extends AppCompatActivity {

    ActivityAddTransactionsBinding binding;
    RequestQueue queue;
    String TRANSACTION_URL = "http://10.0.2.2:3000/transaction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTransactionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String title = getIntent().getStringExtra("title");
        binding.bookTitleTextView.setText(title);
        queue = Volley.newRequestQueue(this);

        int userId = getIntent().getIntExtra("userId", -1);
        Log.d("AddTransactionsActivity", "User ID: " + userId);

        binding.btnSaveOrder.setOnClickListener(v -> saveOrder());
    }

    private void saveOrder() {
        JSONObject orderData = getOrderDataFromFields();
        if (orderData == null) {
            return;
        }

        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Log.e("AddTransactionsActivity", "Токен не знайдено!");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, TRANSACTION_URL, orderData,
                response -> {
                    Log.d("AddTransactionsActivity", "Замовлення успішно додано: " + response.toString());
                    finish();
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("AddTransactionsActivity", "Помилка: " + new String(error.networkResponse.data));
                    } else {
                        Log.e("AddTransactionsActivity", "Помилка при додаванні замовлення: " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

    private JSONObject getOrderDataFromFields() {
        int userId = getIntent().getIntExtra("userId", -1);
        int bookId = getIntent().getIntExtra("bookId", -1);

        if (userId == -1 || bookId == -1) {
            Log.e("AddTransactionsActivity", "Некоректні дані для замовлення!");
            return null;
        }

        try {
            JSONObject orderObject = new JSONObject();
            orderObject.put("user_id", userId);
            orderObject.put("book_id", bookId);
            orderObject.put("return_date", "");  // Приклад дати

            return orderObject;
        } catch (JSONException e) {
            Log.e("AddTransactionsActivity", "Помилка формування JSON: " + e.getMessage());
            return null;
        }
    }
}