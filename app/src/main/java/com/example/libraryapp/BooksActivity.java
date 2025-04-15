package com.example.libraryapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.libraryapp.databinding.ActivityBooksBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BooksActivity extends AppCompatActivity {

    ActivityBooksBinding binding;
    RequestQueue queue;
    private static final String BASE_URL = "http://10.0.2.2:3000/book";
    List<String> bookList;
    ArrayAdapter<String> adapter;
    JSONArray bookJsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBooksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        queue = Volley.newRequestQueue(this);
        bookList = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bookList);
        binding.lvBook.setAdapter(adapter);

        fetchBook();

        binding.btnAddBook.setOnClickListener(v -> startActivity(new Intent(this, AddBookActivity.class)));
        binding.lvBook.setOnItemClickListener((parent, view, position, id) -> showOptionsDialog(position));
        binding.btnGenerateReport.setOnClickListener(v -> generateReport());
    }

    private void fetchBook(){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                response -> {
                    bookList.clear();
                    bookJsonArray = response;
                    try{
                        for(int i = 0; i < response.length(); i++){
                            JSONObject bookItem = response.getJSONObject(i);
                            String title = bookItem.getString("title");
                            String author = bookItem.getString("author");
                            int publication_year = bookItem.getInt("publication_year");
                            String genre = bookItem.getString("genre");
                            String isbn = bookItem.getString("isbn");
                            boolean isAvailable = bookItem.getInt("available") == 1;
                            String availableText = isAvailable ? "Доступна" : "Недоступна";
                            bookList.add(title + "\n" + author + "\n" + publication_year + "\n" + genre + "\n" + isbn + "\n" + availableText);
                        }
                        adapter.notifyDataSetChanged();
                    }catch (JSONException e){
                        Log.e("BooksActivity", "JSON Помилка: " + e.getMessage());
                        Toast.makeText(this, "Помилка обробки даних", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("BooksActivity", "Volley Помилка: " + error.getMessage());
                    Toast.makeText(this, "Помилка отримання даних", Toast.LENGTH_LONG).show();
                }
        );

        queue.add(jsonArrayRequest);
    }

    private void showOptionsDialog(int position){
        String selectedItem = bookList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Опції")
                .setMessage("Оберіть дію для: " + selectedItem)
                .setPositiveButton("Редагувати", (dialog, which) -> editBook(position))
                .setNegativeButton("Видалити", (dialog, which) -> deleteBook(position))
                .setNeutralButton("Скасувати", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void editBook(int position) {
        try {
            JSONObject bookItem = bookJsonArray.getJSONObject(position);

            Intent intent = new Intent(this, AddBookActivity.class);
            intent.putExtra("id", bookItem.getInt("id"));
            intent.putExtra("title", bookItem.getString("title"));
            intent.putExtra("author", bookItem.getString("author"));
            intent.putExtra("publication_year", bookItem.getInt("publication_year"));
            intent.putExtra("genre", bookItem.getString("genre"));
            intent.putExtra("isbn", bookItem.getString("isbn"));
            intent.putExtra("available", bookItem.getInt("available"));
            startActivity(intent);
        } catch (JSONException e) {
            Log.e("StockActivity", "Помилка отримання елементу: " + e.getMessage());
        }
    }


    private void deleteBook(int position) {
        try {
            JSONObject bookItem = bookJsonArray.getJSONObject(position);
            int bookId = bookItem.getInt("id");

            String deleteUrl = BASE_URL + "/" + bookId;

            String token = getSharedPreferences("user_data", MODE_PRIVATE).getString("token", null);

            if (token == null) {
                Toast.makeText(this, "Не вдалося отримати токен", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, deleteUrl, null,
                    response -> {
                        Toast.makeText(this, "Книгу видалено!", Toast.LENGTH_LONG).show();
                        fetchBook(); // Оновлюємо список після видалення
                    },
                    error -> {
                        Log.e("BooksActivity", "Помилка видалення: " + error.toString());
                        Toast.makeText(this, "Помилка видалення!", Toast.LENGTH_LONG).show();
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token); // Додаємо токен в заголовки
                    return headers;
                }
            };

            queue.add(request);
        } catch (JSONException e) {
            Log.e("BooksActivity", "Помилка JSON: " + e.getMessage());
        }
    }

    private void generateReport() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 800, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int y = 50; // Початкова координата Y

        titlePaint.setTextSize(20);
        titlePaint.setFakeBoldText(true);
        canvas.drawText("Звіт про книги", 200, y, titlePaint);

        paint.setTextSize(14);
        y += 40; // Відступ після заголовку

        for (String book : bookList) {
            if (y > 750) { // Нова сторінка, якщо немає місця
                pdfDocument.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(600, 800, pdfDocument.getPages().size() + 1).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }

            // Розбиваємо кожен запис по рядках для кращої читабельності
            String[] lines = book.split("\n");
            for (String line : lines) {
                canvas.drawText(line, 50, y, paint);
                y += 25; // Відступ між рядками
            }

            y += 15; // Додатковий відступ між записами
            canvas.drawLine(40, y, 560, y, paint); // Горизонтальна лінія-роздільник
            y += 15;
        }

        pdfDocument.finishPage(page);

        File file = new File(getExternalFilesDir(null), "BooksReport.pdf");

        try {
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();
            Toast.makeText(this, "PDF збережено: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            openPdf(file);
        } catch (IOException e) {
            Log.e("BooksActivity", "Помилка створення PDF: " + e.getMessage());
        }
    }


    private void openPdf(File file) {
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Відкрити PDF"));
    }


    @Override
    protected void onResume() {
        super.onResume();
        fetchBook();
    }
}