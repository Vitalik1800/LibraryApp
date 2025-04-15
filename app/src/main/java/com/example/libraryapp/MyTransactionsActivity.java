package com.example.libraryapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.libraryapp.databinding.ActivityMyTransactionsBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MyTransactionsActivity extends AppCompatActivity {

    TransactionAdapter transactionAdapter;
    ActivityMyTransactionsBinding binding;
    List<Transaction> transactionList = new ArrayList<>();
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyTransactionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getIntent().getIntExtra("userId", -1);

        Log.d("MyTransactionsActivity", "User ID: " + userId);

        if(userId != -1){
            loadTransactions(userId);
        } else{
            Toast.makeText(this, "Помилка: ID користувача відсутній", Toast.LENGTH_LONG).show();
        }
    }

    public void loadTransactions(int userId){
        String url = "http://10.0.2.2:3000/transactions/" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        @SuppressLint("NotifyDataSetChanged") JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try{
                        transactionList.clear();

                        if(response.length() == 0){
                            Toast.makeText(MyTransactionsActivity.this, "Немає історії взяття книг", Toast.LENGTH_SHORT).show();
                        }

                        for(int i = 0; i < response.length(); i++){
                            JSONObject transactionJson = response.getJSONObject(i);

                            int transactionId = transactionJson.getInt("transaction_id");
                            String issueDate = transactionJson.getString("issue_date");
                            String returnDate = transactionJson.getString("return_date");
                            String status = transactionJson.getString("status");

                            JSONArray booksJson = transactionJson.getJSONArray("books");
                            List<Book> books = new ArrayList<>();
                            for(int j = 0; j < booksJson.length(); j++){
                                JSONObject bookJson = booksJson.getJSONObject(j);
                                int bookId = bookJson.getInt("id");
                                String bookTitle = bookJson.getString("title");
                                String bookAuthor = bookJson.getString("author");
                                int book_publication_year = bookJson.getInt("publication_year");
                                String bookGenre = bookJson.getString("genre");
                                String bookIsbn = bookJson.getString("isbn");

                                books.add(new Book(bookId, bookTitle, bookAuthor, book_publication_year, bookGenre, bookIsbn));
                            }

                            transactionList.add(new Transaction(transactionId, books, issueDate, returnDate, status));
                        }

                        Log.d("MyTransactionsActivity", "Transaction List Size: " + transactionList.size());

                        if(transactionList.isEmpty()){
                            Toast.makeText(this, "Немає даних для відображення", Toast.LENGTH_SHORT).show();
                        } else{
                            transactionAdapter = new TransactionAdapter(transactionList);
                            binding.recyclerViewTransactions.setAdapter(transactionAdapter);
                            binding.recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));

                            transactionAdapter.notifyDataSetChanged();
                        }

                    }catch (JSONException e){
                        Log.e("MyTransactionsActivity", "Помилка: " + e.getMessage());
                    }
                },
                error -> Log.e("MyTransactionsActivity", "Помилка при завантаженні історії взяття книг", error)
        );

        queue.add(request);
    }
}