package com.example.myapplication.data.retrofit.responsemodel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AladinResponse {
    @SerializedName("title")
    private String title; // 책 제목

    @SerializedName("author")
    private String author; // 책 지은이

    @SerializedName("description")
    private String description; // 책 설명

    @SerializedName("publisher")
    private String publisher; // 출판사

    @SerializedName("pubDate")
    private String pubDate; // 출판 날짜

    @SerializedName("cover")
    private String cover; // 표지 사진

    @SerializedName("isbn")
    private String isbn; // ISBN 국제 표준 도서 고유값

    // Getters
    public String toString() {
        return "Title: " + title + ", Author: " + author + ", description: " + description +", Publisher: " + publisher + ", PubDate: " + pubDate + ", Cover: " + cover + ", ISBN: " + isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getCover() {
        return cover;
    }

    public String getIsbn() { return isbn;
    }

    // 응답에서 책 목록을 포함하는 클래스
    public class AladinResponse2 {
        @SerializedName("totalResults")
        private int totalResults;

        @SerializedName("itemsPerPage")
        private int itemsPerPage;

        @SerializedName("item")
        private List<AladinResponse> books; // 책 목록

        // Getters...

        public int getTotalResults() {
            return totalResults;
        }

        public int getItemsPerPage() {
            return itemsPerPage;
        }

        public List<AladinResponse> getBooks() {
            return books;
        }
    }


}


