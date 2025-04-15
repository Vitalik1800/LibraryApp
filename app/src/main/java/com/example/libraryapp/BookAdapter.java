package com.example.libraryapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.libraryapp.databinding.BookCardBinding;
import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final List<Book> bookList;
    private final Context context;

    public BookAdapter(Context context, List<Book> bookList){
        this.context = context;
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BookCardBinding binding = BookCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.binding.tvTitle.setText(book.getTitle());
        holder.binding.tvAuthor.setText(book.getAuthor());
        holder.binding.tvPublicationYear.setText(String.valueOf(book.getPublication_year()));
        holder.binding.tvGenre.setText(book.getGenre());
        holder.binding.tvIsbn.setText(book.getIsbn());

        int userId = getUserId();
        Log.d("BookAdapter", "User ID: " + userId);

        holder.binding.btnOrder.setOnClickListener(v -> {
            if(userId != -1 && book.getBookId() > 0){
                Intent intent = new Intent(context, AddTransactionsActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("bookId", book.getBookId());
                intent.putExtra("title", book.getTitle());

                ArrayList<BookItem> bookItemList = new ArrayList<>();
                BookItem bookItem = new BookItem(book.getBookId(), book.getTitle());
                bookItemList.add(bookItem);

                intent.putExtra("bookList", bookItemList);

                context.startActivity(intent);
            } else{
                Log.e("BookAdapter", "Invalid userId or bookId!");
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    private int getUserId() {
        SharedPreferences preferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        return preferences.getInt("userId", -1);
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder{
        final BookCardBinding binding;

        public BookViewHolder(BookCardBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
