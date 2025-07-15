package com.example.myapplication.data.retrofit.datamodel;

import com.google.gson.annotations.SerializedName;

public class MyLibraryReadingData {


    // 책정보 + 읽고 있는 책 정보

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

    // 읽고 있는 책
    @SerializedName("read_page") // 읽은 페이지
    private String read_page;
    @SerializedName("started_date") // 시작한 날짜 2024.01.03 형태
    private String started_date;
    @SerializedName("been_days") // 읽은 지 며칠 됐는지.
    private String been_days;



    // getter, setter


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

    public String getRead_page() {
        return read_page;
    }

    public void setRead_page(String read_page) {
        this.read_page = read_page;
    }

    public String getStarted_date() {
        return started_date;
    }

    public void setStarted_date(String started_date) {
        this.started_date = started_date;
    }

    public String getBeen_days() {
        return been_days;
    }

    public void setBeen_days(String been_days) {
        this.been_days = been_days;
    }
}
