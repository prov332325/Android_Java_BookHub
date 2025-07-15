package com.example.myapplication.data.retrofit.responsemodel;

import com.google.gson.annotations.SerializedName;

public class MylibraryResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;


    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
