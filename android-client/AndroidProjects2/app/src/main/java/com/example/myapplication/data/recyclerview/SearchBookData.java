package com.example.myapplication.data.recyclerview;

public class SearchBookData { // 책 검색 결과 리사이클러뷰에 들어갈 데이터

    // 책 제목, 지은이, 책 설명, 출판사, 출판날짜, 표지 사진
    private String title; // 책 제목
    private String author; // 책 지은이
    private String description; // 책 설명
    private String publisher; // 출판사
    private String pubDate; // 출판 날짜
    private String cover; // 표지 사진
    private String isbn; // isbn


    // 생성자
    public SearchBookData(String title, String author, String description, String publisher, String pubDate, String cover, String isbn) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.publisher = publisher;
        this.pubDate = pubDate;
        this.cover = cover;
        this.isbn = isbn;
    }

    // getter setter


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

    public String getIsbn() { return isbn; }

    public void setIsbn(String isbn) { this.isbn = isbn; }
}
