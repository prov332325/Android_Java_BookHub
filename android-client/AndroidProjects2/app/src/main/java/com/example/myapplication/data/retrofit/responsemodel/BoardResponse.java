package com.example.myapplication.data.retrofit.responsemodel;

import com.google.gson.annotations.SerializedName;

public class BoardResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("board_number")
    private int board_number;



    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getBoard_number() {
        return board_number;
    }
}
