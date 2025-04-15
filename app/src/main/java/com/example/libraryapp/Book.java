package com.example.libraryapp;

public class Book{

    int bookId;
    String title, author;
    int publication_year;
    String genre, isbn;

    public Book(int bookId, String title, String author, int publication_year, String genre, String isbn) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.publication_year = publication_year;
        this.genre = genre;
        this.isbn = isbn;
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPublication_year() {
        return publication_year;
    }

    public String getGenre() {
        return genre;
    }

    public String getIsbn() {
        return isbn;
    }
}
