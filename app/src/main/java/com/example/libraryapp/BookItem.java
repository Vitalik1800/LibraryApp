package com.example.libraryapp;

import java.io.Serializable;

public class BookItem implements Serializable {

    int bookId;
    String title;

    public BookItem(int bookId, String title) {
        this.bookId = bookId;
        this.title = title;
    }
}
