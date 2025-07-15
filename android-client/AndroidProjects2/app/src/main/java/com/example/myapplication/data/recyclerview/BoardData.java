package com.example.myapplication.data.recyclerview;

public class BoardData {

    private int board_number;
    private int user_number;
    private String title;
    private String content;
    private String category;
    private String date;
    private int like_cnt;
    private int comment_cnt;
    private int like_or_not;



    public BoardData(int board_numbber, int user_number, String title, String content, String category, String date, int like_cnt, int comment_cnt, int like_or_not) {
        this.board_number = board_numbber;
        this.user_number = user_number;
        this.title = title;
        this.content = content;
        this.category = category;
        this.date = date;
        this.like_cnt = like_cnt;
        this.comment_cnt = comment_cnt;
        this.like_or_not = like_or_not;
    }

    public int getBoard_number() {
        return board_number;
    }

    public void setBoard_number(int board_number) {
        this.board_number = board_number;
    }

    public int getUser_number() {
        return user_number;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setUser_number(int user_number) {
        this.user_number = user_number;
    }

    public int getLike_cnt() {
        return like_cnt;
    }

    public void setLike_cnt(int like_cnt) {
        this.like_cnt = like_cnt;
    }

    public int getComment_cnt() {
        return comment_cnt;
    }

    public void setComment_cnt(int comment_cnt) {
        this.comment_cnt = comment_cnt;
    }

    public int getLike_or_not() {
        return like_or_not;
    }

    public void setLike_or_not(int like_or_not) {
        this.like_or_not = like_or_not;
    }
}



