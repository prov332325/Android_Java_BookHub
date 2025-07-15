package com.example.myapplication.data.recyclerview;

public class BoardCommentData {

    private int comment_number;
    private String commnet_user_img;
    private int comment_user_number;
    private String comment_user_nickname;
    private String comment_content;
    private String comment_date;
    private String comment_update_date;


    public BoardCommentData(int comment_number, String commnet_user_img, int comment_user_number, String comment_user_nickname, String comment_content, String comment_date, String comment_update_date) {
        this.comment_number = comment_number;
        this.commnet_user_img = commnet_user_img;
        this.comment_user_number = comment_user_number;
        this.comment_user_nickname = comment_user_nickname;
        this.comment_content = comment_content;
        this.comment_date = comment_date;
        this.comment_update_date = comment_update_date;
    }


    public int getComment_number() {
        return comment_number;
    }

    public void setComment_number(int comment_number) {
        this.comment_number = comment_number;
    }

    public String getCommnet_user_img() {
        return commnet_user_img;
    }

    public void setCommnet_user_img(String commnet_user_img) {
        this.commnet_user_img = commnet_user_img;
    }

    public int getComment_user_number() {
        return comment_user_number;
    }

    public void setComment_user_number(int comment_user_number) {
        this.comment_user_number = comment_user_number;
    }

    public String getComment_user_nickname() {
        return comment_user_nickname;
    }

    public void setComment_user_nickname(String comment_user_nickname) {
        this.comment_user_nickname = comment_user_nickname;
    }

    public String getComment_content() {
        return comment_content;
    }

    public void setComment_content(String comment_content) {
        this.comment_content = comment_content;
    }

    public String getComment_date() {
        return comment_date;
    }

    public void setComment_date(String comment_date) {
        this.comment_date = comment_date;
    }

    public String getComment_update_date() {
        return comment_update_date;
    }

    public void setComment_update_date(String comment_update_date) {
        this.comment_update_date = comment_update_date;
    }
}
