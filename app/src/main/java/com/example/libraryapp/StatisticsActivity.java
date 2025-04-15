package com.example.libraryapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.libraryapp.databinding.ActivityStatisticsBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class StatisticsActivity extends AppCompatActivity {

    ActivityStatisticsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnToday.setOnClickListener(v -> fetchStatistics("today"));

        binding.btnWeek.setOnClickListener(v -> fetchStatistics("week"));

        binding.btnMonth.setOnClickListener(v -> fetchStatistics("month"));
    }

    private void fetchStatistics(String period) {
        String url = "http://10.0.2.2:3000/statistics/" + period; // Заміни на IP адрес вашого сервера

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // Парсинг JSON відповіді та відображення статистики
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        int booksIssued = jsonResponse.getInt("books_issued"); // Припустимо, що відповідає поле "books_issued"

                        String report = "Статистика за період (" + period + "): " + booksIssued + " книг видано";
                        binding.tvReport.setText(report);

                        // Створення PDF після отримання статистики
                        createPdf(report);
                    } catch (JSONException e) {
                        binding.tvReport.setText("Помилка отримання даних.");
                    }
                },
                error -> binding.tvReport.setText("Помилка підключення до сервера."));

        // Додавання запиту до черги Volley
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void createPdf(String report) {
        // Створення нового PDF-документа
        PdfDocument pdfDocument = new PdfDocument();

        // Створення сторінки
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Створення об'єкта Canvas для малювання на сторінці
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);

        // Додаємо заголовок
        canvas.drawText("Звіт по бібліотеці", 100, 100, paint);

        // Додаємо текст звіту
        canvas.drawText(report, 100, 150, paint);

        // Завершуємо сторінку
        pdfDocument.finishPage(page);

        // Збереження документа на диск
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/LibraryReport.pdf"; // Шлях до файлу PDF
        File file = new File(filePath);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            pdfDocument.writeTo(fileOutputStream);
            pdfDocument.close();
            fileOutputStream.close();
            Toast.makeText(this, "PDF збережено: " + filePath, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Помилка при створенні PDF", Toast.LENGTH_SHORT).show();
        }
    }

}
