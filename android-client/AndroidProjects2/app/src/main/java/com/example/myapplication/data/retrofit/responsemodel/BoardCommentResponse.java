package com.example.myapplication.data.retrofit.responsemodel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BoardCommentResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("user_number")
    private int user_number;

    @SerializedName("board_number")
    private int board_number;

    @SerializedName("comment_number")
    private int comment_number;

    @SerializedName("comment_content")
    private String comment_content;

    @SerializedName("user_nickname")
    private String user_nickname;

    @SerializedName("user_profileImg")
    private String user_profileImg;

    @SerializedName("createdTime")
    private String createdTime;

    @SerializedName("updateTime")
    private String updateTime;


    // 댓글 1개에 대한 생성자
    public BoardCommentResponse(int code, String message, int user_number, int board_number, int comment_number, String comment_content, String user_nickname, String user_profileImg, String createdTime, String updateTime) {
        this.code = code;
        this.message = message;
        this.user_number = user_number;
        this.board_number = board_number;
        this.comment_number = comment_number;
        this.comment_content = comment_content;
        this.user_nickname = user_nickname;
        this.user_profileImg = user_profileImg;
        this.createdTime = createdTime;
        this.updateTime = updateTime;
    }


    // getter

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getUser_number() {
        return user_number;
    }

    public int getBoard_number() {
        return board_number;
    }

    public int getComment_number() {
        return comment_number;
    }

    public String getComment_content() {
        return comment_content;
    }

    public String getUser_nickname() {
        return user_nickname;
    }

    public String getUser_profileImg() {
        return user_profileImg;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }
    // 댓글 목록을 위한 list 내부 클래스

    public class BoardCommentResponse2 {

        @SerializedName("code")
        private int code;

        @SerializedName("message")
        private String message;

        @SerializedName("item")
        private List<BoardCommentResponse> comments; // 댓글 목록


        // getter

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public List<BoardCommentResponse> getComments() {
            return comments;
        }
    }
}
