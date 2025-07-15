package com.example.myapplication.data.retrofit.datamodel;

import com.google.gson.annotations.SerializedName;

public class MyLibraryWantData {

    // 책정보 + 읽고 싶은 책 정보

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


    // 읽고 싶은 책
    @SerializedName("want_rating") // 읽고 싶은책 기대지수
    private String want_rating;
    @SerializedName("want_preview") // 읽고 싶은책 기대평
    private String want_preview;



    // getter , setter
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

    public String getWant_rating() {
        return want_rating;
    }

    public void setWant_rating(String want_rating) {
        this.want_rating = want_rating;
    }

    public String getWant_preview() {
        return want_preview;
    }

    public void setWant_preview(String want_preview) {
        this.want_preview = want_preview;
    }
}
