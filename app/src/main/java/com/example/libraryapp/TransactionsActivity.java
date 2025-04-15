package com.example.libraryapp;

import android.annotation.SuppressLint;
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
import com.android.volley.toolbox.Volley;
import com.example.libraryapp.databinding.ActivityTransactionsBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionsActivity extends AppCompatActivity {

    ActivityTransactionsBinding binding;
    RequestQueue queue;
    private static final String BASE_URL = "http://10.0.2.2:3000/transaction";
    List<String> transactionList;
    ArrayAdapter<String> adapter;
    JSONArray transactionJsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        queue = Volley.newRequestQueue(this);
        transactionList = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, transactionList);
        binding.lvTransaction.setAdapter(adapter);

        fetchTransaction();

        binding.lvTransaction.setOnItemClickListener((parent, view, position, id) -> showOptionsDialog(position));
        binding.btnGenerateReport.setOnClickListener(v -> generateReport());
        binding.btnStatistics.setOnClickListener(v -> startActivity(new Intent(this, StatisticsActivity.class)));
    }

    private void fetchTransaction() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                response -> {
                    transactionList.clear();
                    transactionJsonArray = response;
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject transactionItem = response.getJSONObject(i);
                            String user_id = transactionItem.getString("email");
                            String book_id = transactionItem.getString("title");
                            String issueDate = transactionItem.getString("issue_date");
                            String returnDate = transactionItem.getString("return_date");
                            String status = transactionItem.getString("status");

                            String formattedIssueDate = formatDate(issueDate);
                            String formattedReturnDate = formatDate(returnDate);

                            transactionList.add(user_id + "\n" + book_id + "\n" + formattedIssueDate + "\n" + formattedReturnDate + "\n" + status);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e("TransactionsActivity", "JSON Помилка: " + e.getMessage());
                        Toast.makeText(this, "Помилка обробки даних", Toast.LENGTH_LONG).show();
                    }
                },
                error -> Log.e("TransactionsActivity", "Помилка мережі: " + error.getMessage())
        );

        queue.add(jsonArrayRequest);
    }

    private void showOptionsDialog(int position){
        String selectedItem = transactionList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Опції")
                .setMessage("Оберіть дію для: " + selectedItem)
                .setPositiveButton("Редагувати", (dialog, which) -> editTransaction(position))
                .setNegativeButton("Скасувати", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void editTransaction(int position){
        try{
            JSONObject transactionItem = transactionJsonArray.getJSONObject(position);

            Intent intent = new Intent(this, EditTransactionActivity.class);
            intent.putExtra("transaction_id", transactionItem.getInt("id"));
            intent.putExtra("user_id", transactionItem.getInt("user_id"));
            intent.putExtra("book_id", transactionItem.getInt("book_id"));
            intent.putExtra("issue_date", transactionItem.getString("issue_date"));
            intent.putExtra("return_date", transactionItem.getString("return_date"));
            intent.putExtra("status", transactionItem.getString("status"));
            startActivity(intent);
        }catch (JSONException e){
            Log.e("TransactionsActivity", "Помилка отримання даних для редагування: " + e.getMessage());
        }
    }


    // Метод для форматування дат
    private String formatDate(String dateStr) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");  // Формат, в якому дата отримується з сервера
        @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm");  // Формат для відображення в UI
        String formattedDate = "";
        try {
            Date date = inputFormat.parse(dateStr);
            formattedDate = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
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
        canvas.drawText("Звіт про транзакції", 200, y, titlePaint);

        paint.setTextSize(14);
        y += 40; // Відступ після заголовку

        for (String book : transactionList) {
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

        File file = new File(getExternalFilesDir(null), "TransactionsReport.pdf");

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
        fetchTransaction();
    }
}
