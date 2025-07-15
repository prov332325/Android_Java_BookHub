package com.example.myapplication.data.retrofit.responsemodel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BoardListResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("user_number")
    private int user_number;


    @SerializedName("user_nickname")
    private String user_nickname;


    @SerializedName("user_profile_img")
    private String user_profile_img;

    @SerializedName("user_emailID")
    private String user_emailID;


    // 게시글 제목, 내용, 카테고리, 날짜
    @SerializedName("board_number")
    private int board_number;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("category")
    private String category;

    @SerializedName("createdTime")
    private String createdTime;

    // board 댓글 개수
    @SerializedName("comment_cnt")
    private int comment_cnt;


    // 좋아요 개수
    @SerializedName("like_cnt")
    private int like_cnt;


    // 내가 좋아요 눌렀는지 안눌렀는지 여부
    @SerializedName("like_or_not")
    private int like_or_not; // boolean 대신 int 사용


    // board 에 추가된 책 정보 가져오기. list 로 가져와야 하나 ??
    @SerializedName("item")
    private List<AladinResponse> books; // 책 목록



    // board에 존재하는 댓글 가져오기
    @SerializedName("comment_item")
    private List<BoardCommentResponse> comments; // 댓글 목록




    // 생성자
    public BoardListResponse(int code, String message, int user_number, int board_number, String title, String content, String category, String createdTime) {
        this.code = code;
        this.message = message;
        this.user_number = user_number;
        this.board_number = board_number;
        this.title = title;
        this.content = content;
        this.category = category;
        this.createdTime = createdTime;
    }


    // getter, setter

    public String getUser_emailID() {
        return user_emailID;
    }

    public void setUser_emailID(String user_emailID) {
        this.user_emailID = user_emailID;
    }

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

    public String getUser_nickname() {return user_nickname; }

    public String getUser_profile_img() {
        return user_profile_img;
    }

    public void setUser_profile_img(String user_profile_img) {
        this.user_profile_img = user_profile_img;
    }

    public int getBoard_number() {
        return board_number;
    }

    public void setBoard_number(int board_number) {
        this.board_number = board_number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public List<AladinResponse> getBooks() {
        return books;
    }

    public List<BoardCommentResponse> getComments() { return comments; }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public int getComment_cnt() {
        return comment_cnt;
    }

    // 좋아요 수
    public int getLike_cnt() {
        return like_cnt;
    }


    // 좋아요 여부
    public int isLike_or_not() {
        return like_or_not; // 1이면 true, 0이면 false
    }

    // 게시판을 목록으로 가져오기 위한 내부 클래스
    public class BoardResponse2 {

        @SerializedName("code")
        private int code;

        @SerializedName("message")
        private String message;

        @SerializedName("total_cnt")
        private int total_cnt;

        @SerializedName("item")
        private List<BoardListResponse> board_items; // 게시글 목록




        // getter
        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public List<BoardListResponse> getBoard_items() {
            return board_items;
        }

        public int getTotal_cnt() { return total_cnt;
        }
    }
}
