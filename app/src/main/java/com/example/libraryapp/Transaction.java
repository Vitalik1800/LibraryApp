package com.example.libraryapp;

import java.util.List;

public class Transaction {

    int transactionId;
    List<Book> books;
    String issue_date, returned_date, status;

    public Transaction(int transactionId, List<Book> books, String issue_date, String returned_date, String status) {
        this.transactionId = transactionId;
        this.books = books;
        this.issue_date = issue_date;
        this.returned_date = returned_date;
        this.status = status;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public List<Book> getBooks() {
        return books;
    }

    public String getIssue_date() {
        return issue_date;
    }

    public String getReturned_date() {
        return returned_date;
    }

    public String getStatus() {
        return status;
    }
}
