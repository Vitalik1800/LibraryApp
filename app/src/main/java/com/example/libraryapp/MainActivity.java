package com.example.libraryapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.libraryapp.databinding.ActivityMainBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    BookAdapter adapter;
    List<Book> bookList;
    RequestQueue queue;
    private static final String BOOK_URL = "http://10.0.2.2:3000/available_book";
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getUserId();
        Log.d("MainActivity", "UserId: " + userId);

        binding.recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this));

        bookList = new ArrayList<>();
        adapter = new BookAdapter(this, bookList);
        binding.recyclerViewBooks.setAdapter(adapter);

        queue = Volley.newRequestQueue(this);
        fetchBooks();

        binding.btnMyBooks.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyTransactionsActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
        binding.logout.setOnClickListener(v -> logout());
    }

    private void logout() {
        SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(MainActivity.this, "Ви вийшли з облікового запису", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private int getUserId() {
        SharedPreferences preferences = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        return preferences.getInt("userId", -1);
    }

    public void fetchBooks(){
        @SuppressLint("NotifyDataSetChanged") JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                BOOK_URL,
                null,
                response -> {
                    try{
                        bookList.clear();
                        for(int i = 0; i < response.length(); i++){
                            JSONObject bookObject = response.getJSONObject(i);
                            int id = bookObject.getInt("id");
                            String title = bookObject.getString("title");
                            String author = bookObject.getString("author");
                            int publication_year = bookObject.getInt("publication_year");
                            String genre = bookObject.getString("genre");
                            String isbn = bookObject.getString("isbn");
                            bookList.add(new Book(id, title, author, publication_year, genre, isbn));
                        }
                        adapter.notifyDataSetChanged();
                    }catch (JSONException e){
                        Log.e("MainActivity", "JSON Error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("MainActivity", "Volley Error: " + error.getMessage());
                    Toast.makeText(this, "Помилка отримання даних!", Toast.LENGTH_LONG).show();
                }
        );

        queue.add(jsonArrayRequest);
    }

}