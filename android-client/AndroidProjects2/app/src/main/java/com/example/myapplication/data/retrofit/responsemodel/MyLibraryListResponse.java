package com.example.myapplication.data.retrofit.responsemodel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MyLibraryListResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("user_number")
    private int user_number;

    // 내 서재 고유 번호 - 각 테이블에 있는 인덱스 번호
    @SerializedName("mylibrary_number")
    private int mylibrary_number;


    // 서재 작성자.
    @SerializedName("mylibrary_user_number")
    private int mylibrary_user_number;

    // 책 정보
    @SerializedName("book_number")
    private int book_number;

    @SerializedName("cover")
    private String cover;

    @SerializedName("title")
    private String title;

    @SerializedName("author")
    private String author;

    // 책소개, 출판사, isbn
    @SerializedName("description")
    private String description;

    @SerializedName("publisher")
    private String publisher;

    @SerializedName("isbn")
    private String isbn;

    // 시작 종료
    @SerializedName("started")
    private String started;

    @SerializedName("finished")
    private String finished;

    // 읽은 페이지
    @SerializedName("readPage")
    private String readPage;

    // rating 평점, 기대지수
    @SerializedName("rating")
    private String rating;

    // 기대평
    @SerializedName("preview")
    private String preview;

    // 생성 시간 , type
    @SerializedName("createdTime")
    private String createdTime;

    @SerializedName("type")
    private String type;

    @SerializedName("save_type")
    private String save_type;


    //생성자
    public MyLibraryListResponse(int code, String message, int mylibrary_number, int user_number, int book_number, String cover, String title, String author, String description, String publisher, String isbn, String started, String finished, String readPage, String rating, String preview, String createdTime, String type, String save_type) {
        this.code = code;
        this.message = message;
        this.user_number = user_number;
        this.mylibrary_number = mylibrary_number;
        this.book_number = book_number;
        this.cover = cover;
        this.description = description;
        this.publisher = publisher;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.started = started;
        this.finished = finished;
        this.readPage = readPage;
        this.rating = rating;
        this.preview = preview;
        this.createdTime = createdTime;
        this.type = type;
        this.save_type = save_type;
    }



    // getter, setter

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getUser_number() {
        return user_number;
    }

    public void setUser_number(int user_number) {
        this.user_number = user_number;
    }

    public int getMylibrary_number() {
        return mylibrary_number;
    }

    public void setMylibrary_number(int mylibrary_number) {
        this.mylibrary_number = mylibrary_number;
    }

    public int getMylibrary_user_number() {
        return mylibrary_user_number;
    }

    public void setMylibrary_user_number(int mylibrary_user_number) {
        this.mylibrary_user_number = mylibrary_user_number;
    }

    public int getBook_number() {
        return book_number;
    }

    public void setBook_number(int book_number) {
        this.book_number = book_number;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

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

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
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

    public String getReadPage() {
        return readPage;
    }

    public void setReadPage(String readPage) {
        this.readPage = readPage;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSave_type() {
        return save_type;
    }

    public void setSave_type(String save_type) {
        this.save_type = save_type;
    }

    // 내 서재 전체 목록을 포함하는 내부 클래스

    public class MyLibraryResponse2 {

        @SerializedName("code")
        private int code;

        @SerializedName("message")
        private String message;

        @SerializedName("item")
        private List<MyLibraryListResponse> library_items; // 책 목록


        // getter
        public List<MyLibraryListResponse> getLibrary_items() {
            return library_items;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    } // response 2 클래스


}
