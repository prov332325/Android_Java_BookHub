package com.example.myapplication.data.recyclerview;

public class MylibraryAlreadyData {

    private int mylibrary_number;
    private int lib_user_number;
    private int bookNumber;
    private String bookCover;
    private String bookTitle;
    private String bookAuthor;
    private String rating;
    private String started;
    private String finished;

    private String createdTime;

    private int type;
    private String save_type;


    // 생성자

    public MylibraryAlreadyData(int mylibrary_number, int lib_user_number,  int bookNumber, String bookCover, String bookTitle, String bookAuthor, String rating, String started, String finished, String createdTime, int type,String save_type) {
        this.mylibrary_number = mylibrary_number;
        this.lib_user_number = lib_user_number;
        this.bookNumber = bookNumber;
        this.bookCover = bookCover;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.rating = rating;
        this.started = started;
        this.finished = finished;
        this.createdTime = createdTime;
        this.type = type;
        this.save_type = save_type;
    }


    // getter , setter
    public int getMylibrary_number() {
        return mylibrary_number;
    }

    public void setMylibrary_number(int mylibrary_number) {
        this.mylibrary_number = mylibrary_number;
    }

    public int getLib_user_number() {
        return lib_user_number;
    }

    public void setLib_user_number(int lib_user_number) {
        this.lib_user_number = lib_user_number;
    }

    public int getBookNumber() {
        return bookNumber;
    }

    public void setBookNumber(int bookNumber) {
        this.bookNumber = bookNumber;
    }

    public String getBookCover() {
        return bookCover;
    }

    public void setBookCover(String bookCover) {
        this.bookCover = bookCover;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getStarted() {
        return started;
    }

    public void setStarted(String started) {
        this.started = started;
    }

    public String getFinished() {
        return finished;
    }

    public void setFinished(String finished) {
        this.finished = finished;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSave_type() {
        return save_type;
    }

    public void setSave_type(String save_type) {
        this.save_type = save_type;
    }
}