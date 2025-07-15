package com.example.myapplication.data.retrofit.datamodel;

import com.google.gson.annotations.SerializedName;

public class MyLibraryAlreadyData {

    // 책정보 + 읽은 책 정보

    @SerializedName("title") // 책 제목
    private String title;
    @SerializedName("author") // 작가
    private String author;
    @SerializedName("description") // 책소개
    private String description;
    @SerializedName("publisher") // 출판사
    private String publisher;
    @SerializedName("pubDate") // 출판 날짜
    private String pubDate;
    @SerializedName("cover") // 책 커버 - 알라딘 서버에 저장된 url 형식
    private String cover;
    @SerializedName("isbn") // isbn
    private String isbn;


    // 읽은 책 정보
    @SerializedName("started_date") // 시작한 날짜 2024.01.03 형태
    private String started_date;
    @SerializedName("finished_date") // 종료한 날짜 2024.01.03 형태
    private String finished_date;
    @SerializedName("already_rating") // 읽은 책 평점.
    private String already_rating;



    // getter and setter

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getStarted_date() {
        return started_date;
    }

    public void setStarted_date(String started_date) {
        this.started_date = started_date;
    }

    public String getFinished_date() {
        return finished_date;
    }

    public void setFinished_date(String finished_date) {
        this.finished_date = finished_date;
    }

    public String getAlready_rating() {
        return already_rating;
    }

    public void setAlready_rating(String already_rating) {
        this.already_rating = already_rating;
    }
}
