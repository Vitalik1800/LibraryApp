package com.example.libraryapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.libraryapp.databinding.ActivityEditTransactionBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditTransactionActivity extends AppCompatActivity {

    ActivityEditTransactionBinding binding;
    RequestQueue queue;
    String TRANSACTION_URL = "http://10.0.2.2:3000/transaction";
    String USER_URL = "http://10.0.2.2:3000/users_no_admin";
    String BOOK_URL = "http://10.0.2.2:3000/book";
    List<String> userList;
    List<Integer> userIds;
    ArrayAdapter<String> userAdapter;
    List<String> bookList;
    List<Integer> bookIds;
    ArrayAdapter<String> bookAdapter;
    int transactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        queue = Volley.newRequestQueue(this);
        userList = new ArrayList<>();
        userIds = new ArrayList<>();
        bookList = new ArrayList<>();
        bookIds = new ArrayList<>();

        userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, userList);
        binding.spinnerUser.setAdapter(userAdapter);

        fetchUserItems();

        bookAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bookList);
        binding.spinnerBook.setAdapter(bookAdapter);

        fetchBookItems();

        transactionId = getIntent().getIntExtra("transaction_id", -1);
        if(transactionId != -1){
            loadTransactionData();
        }

        binding.btnUpdateTransaction.setOnClickListener(v -> updateTransaction());
    }

    private void fetchUserItems(){
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, USER_URL, null,
                response -> {
                    userList.clear();
                    userIds.clear();
                    for(int i = 0; i < response.length(); i++){
                        try{
                            JSONObject userItem = response.getJSONObject(i);
                            int id = userItem.getInt("id");
                            String email = userItem.getString("email");
                            userList.add(email);
                            userIds.add(id);
                        }catch (JSONException e){
                            Log.e("EditTransactionsActivity", "JSON Error: " + e.getMessage());
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                },
                error -> Log.e("EditTransactionActivity", "Volley Error: " + error.getMessage())
        );

        queue.add(request);
    }

    private void fetchBookItems(){
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, BOOK_URL, null,
                response -> {
                    bookList.clear();
                    bookIds.clear();
                    for(int i = 0; i < response.length(); i++){
                        try{
                            JSONObject bookItem = response.getJSONObject(i);
                            int id = bookItem.getInt("id");
                            String title = bookItem.getString("title");
                            bookList.add(title);
                            bookIds.add(id);
                        }catch (JSONException e){
                            Log.e("EditTransactionsActivity", "JSON Error: " + e.getMessage());
                        }
                    }
                    bookAdapter.notifyDataSetChanged();
                },
                error -> Log.e("EditTransactionActivity", "Volley Error: " + error.getMessage())
        );

        queue.add(request);
    }

    private void updateTransaction(){
        JSONObject transactionObject = getTransactionDataFromFields();
        if(transactionObject == null) return;

        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token == null) {
            Log.e("AddTransactionsActivity", "Токен не знайдено!");
            return;
        }

        String updateUrl = TRANSACTION_URL + "/" + transactionId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, updateUrl, transactionObject,
                response -> {
                    Toast.makeText(this, "Транзакція оновлена!", Toast.LENGTH_LONG).show();
                    finish();
                },
                error -> {
                    Log.e("EditTransactionActivity", "Volley Error: " + error.getMessage());
                    Toast.makeText(this, "Помилка оновлення!", Toast.LENGTH_LONG).show();
                })
        {
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

    private void loadTransactionData(){
        String issueDate = getIntent().getStringExtra("issue_date");

        if (issueDate != null) {
            try {
                // Assuming the incoming date format is "yyyy-MM-dd" (change this if it's different)
                @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = inputFormat.parse(issueDate);

                // Set the date in the format "yyyy-MM-dd"
                @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = outputFormat.format(date);

                // Set the formatted date in the EditText
                binding.etIssueDate.setText(formattedDate);
            } catch (ParseException e) {
                Log.e("EditTransactionActivity", "Date Parse Error: " + e.getMessage());
            }
        }

        String returnDate = getIntent().getStringExtra("return_date");

        if (returnDate != null) {
            try {
                // Assuming the incoming date format is "yyyy-MM-dd" (change this if it's different)
                @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = inputFormat.parse(returnDate);

                // Set the date in the format "yyyy-MM-dd"
                @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = outputFormat.format(date);

                // Set the formatted date in the EditText
                binding.etReturnDate.setText(formattedDate);
            } catch (ParseException e) {
                Log.e("EditTransactionActivity", "Date Parse Error: " + e.getMessage());
            }
        }
        binding.etStatus.setText(getIntent().getStringExtra("status"));

        int userId = getIntent().getIntExtra("user_id", -1);
        int userIndex = userIds.indexOf(userId);
        if(userIndex != 1){
            binding.spinnerUser.setSelection(userIndex);
        }

        int bookId = getIntent().getIntExtra("book_id", -1);
        int bookIndex = bookIds.indexOf(bookId);
        if(bookIndex != 1){
            binding.spinnerBook.setSelection(bookIndex);
        }
    }

    private JSONObject getTransactionDataFromFields(){
        String issue_date = binding.etIssueDate.getText().toString();
        String return_date = binding.etReturnDate.getText().toString();
        String status = binding.etStatus.getText().toString();

        if(issue_date.isEmpty() || return_date.isEmpty() || status.isEmpty()){
            Toast.makeText(this, "Заповніть всі поля!", Toast.LENGTH_LONG).show();
            return null;
        }

        int userId = userIds.get(binding.spinnerUser.getSelectedItemPosition());
        int bookId = bookIds.get(binding.spinnerBook.getSelectedItemPosition());

        JSONObject transactionObject = new JSONObject();

        try{
            transactionObject.put("user_id", userId);
            transactionObject.put("book_id", bookId);
            transactionObject.put("issue_date", issue_date);
            transactionObject.put("return_date", return_date);
            transactionObject.put("status", status);
        }catch (JSONException e){
            Log.e("EditTransactionActivity", "JSON Error: " + e.getMessage());
            return null;
        }

        return transactionObject;
    }

}