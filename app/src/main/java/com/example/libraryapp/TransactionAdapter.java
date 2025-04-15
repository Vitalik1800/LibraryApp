package com.example.libraryapp;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>{

    List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList){
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.textTransactionId.setText("№" + transaction.getTransactionId());

        StringBuilder booksText = new StringBuilder();
        for(Book book : transaction.getBooks()){
            booksText.append(book.getTitle()).append("\n").append(book.getAuthor()).append("\n").append(book.getPublication_year()).append("\n").append(book.getGenre()).append("\n").append(book.getIsbn());
        }
        holder.textBooks.setText(booksText.toString());

        String formattedIssueDate = formatDate(transaction.getIssue_date());
        holder.textIssueDate.setText("Дата видачі: " + formattedIssueDate);

        String formattedReturnDate = formatDate(transaction.getReturned_date());
        holder.textIssueDate.setText("Дата повернення: " + formattedReturnDate);

        holder.textStatus.setText("Статус: " + transaction.getStatus());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder{
        TextView textTransactionId, textBooks, textIssueDate, textReturnedDate, textStatus;

        public TransactionViewHolder(View view){
            super(view);
            textTransactionId = view.findViewById(R.id.textTransactionId);
            textBooks = view.findViewById(R.id.textBooks);
            textIssueDate = view.findViewById(R.id.textIssueDate);
            textReturnedDate = view.findViewById(R.id.textReturnedDate);
            textStatus = view.findViewById(R.id.textStatus);
        }
    }

    private String formatDate(String rawDate){
        try{
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(rawDate);
            return outputFormat.format(date);
        }catch (ParseException e){
            e.printStackTrace();
            return rawDate;
        }
    }
}
