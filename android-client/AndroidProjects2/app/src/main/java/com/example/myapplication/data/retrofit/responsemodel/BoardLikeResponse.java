package com.example.myapplication.data.retrofit.responsemodel;

import com.google.gson.annotations.SerializedName;

public class BoardLikeResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("user_number")
    private int user_number;

    @SerializedName("board_number")
    private int board_number;

    @SerializedName("status_now")
    private String status_now;

    @SerializedName("like_cnt")
    private int like_cnt;


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

    public String getStatus_now() {
        return status_now;
    }

    public int getLike_cnt() { return like_cnt; }
}
