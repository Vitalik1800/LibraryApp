package com.example.libraryapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.libraryapp.databinding.ActivityAddBookBinding;
import org.json.JSONException;
import org.json.JSONObject;

public class AddBookActivity extends AppCompatActivity {

    ActivityAddBookBinding binding;
    RequestQueue queue;
    String BOOK_URL = "http://10.0.2.2:3000/book";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        queue = Volley.newRequestQueue(this);

        int bookId = getIntent().getIntExtra("id", -1);
        Log.d("AddBookActivity", "Отриманий bookId: " + bookId);
        if(bookId != -1){
            binding.btnAddBook.setText("Оновити");
            String title = getIntent().getStringExtra("title");
            if(title != null) binding.etTitle.setText(title);
            String author = getIntent().getStringExtra("author");
            if(author != null) binding.etAuthor.setText(author);
            int publication_year = getIntent().getIntExtra("publication_year", 0);
            binding.etYear.setText(String.valueOf(publication_year));
            String genre = getIntent().getStringExtra("genre");
            if(genre != null) binding.etGenre.setText(genre);
            String isbn = getIntent().getStringExtra("isbn");
            if(isbn != null) binding.etIsbn.setText(isbn);
            boolean available = getIntent().getIntExtra("available", 0) == 1;
            binding.switchAvailable.setChecked(available);
        } else{
            binding.btnAddBook.setText("Додати книгу");
        }

        binding.btnAddBook.setOnClickListener(v -> {
            if(bookId == -1){
                addBook();
            } else{
                updateBook(bookId);
            }
        });
    }

    private void addBook(){
        JSONObject bookData = getBookData();
        if(bookData == null) return;

        String token = getSharedPreferences("user_data", MODE_PRIVATE).getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Не вдалося отримати токен", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BOOK_URL, bookData,
                response -> {
                    Toast.makeText(this, "Книгу додано!", Toast.LENGTH_LONG).show();
                    finish();
                }, error -> {
            Log.e("AddBookActivity", "Помилка додавання: " + error.getMessage());
            Toast.makeText(this, "Помилка під час додавання!", Toast.LENGTH_LONG).show();
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token); // Додаємо токен до заголовків
                return headers;
            }
        };

        queue.add(request);
    }


    private void updateBook(int id){
        JSONObject bookData = getBookData();
        if(bookData == null) return;

        String token = getSharedPreferences("user_data", MODE_PRIVATE).getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Не вдалося отримати токен", Toast.LENGTH_SHORT).show();
            return;
        }

        String updateURL = BOOK_URL + "/" + id;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, updateURL, bookData,
                response -> {
                    Toast.makeText(this, "Книгу оновлено!", Toast.LENGTH_LONG).show();
                    finish();
                }, error -> {
            if (error.networkResponse != null) {
                int statusCode = error.networkResponse.statusCode;
                Log.e("AddBookActivity", "Помилка оновлення: Код " + statusCode);
                String responseBody = new String(error.networkResponse.data);
                Log.e("AddBookActivity", "Відповідь сервера: " + responseBody);
            } else {
                Log.e("AddBookActivity", "Помилка оновлення: невідома");
            }
            Toast.makeText(this, "Помилка під час оновлення!", Toast.LENGTH_LONG).show();
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

    private JSONObject getBookData(){
        String title = binding.etTitle.getText().toString();
        String author = binding.etAuthor.getText().toString();
        String year = binding.etYear.getText().toString().trim();
        String genre = binding.etGenre.getText().toString();
        String isbn = binding.etIsbn.getText().toString();
        boolean available = binding.switchAvailable.isChecked();

        if (title.isEmpty() || author.isEmpty() || year.isEmpty() || genre.isEmpty() || isbn.isEmpty()) {
            Toast.makeText(this, "Будь ласка, заповніть всі поля!", Toast.LENGTH_SHORT).show();
            return null;
        }

        JSONObject bookData = new JSONObject();
        try{
            bookData.put("title", title);
            bookData.put("author", author);
            bookData.put("publication_year", Integer.parseInt(year));
            bookData.put("genre", genre);
            bookData.put("isbn", isbn);
            bookData.put("available", available ? 1 : 0);
        } catch (JSONException e){
            Log.e("AddBookActivity", "JSON Помилка: " + e.getMessage());
            return null;
        }

        return bookData;
    }
}