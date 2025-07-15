package com.example.myapplication.data.retrofit.responsemodel;

import com.google.gson.annotations.SerializedName;

public class ProfileFollowResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("follow_each_other")
    private int follow_each_other;


    @SerializedName("user_number")
    private int user_number;

    @SerializedName("this_user_number")
    private int this_user_number;

    @SerializedName("status_now")
    private String status_now;


    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getFollow_each_other() {
        return follow_each_other;
    }

    public int getUser_number() {
        return user_number;
    }

    public int getThis_user_number() {
        return this_user_number;
    }

    public String getStatus_now() {
        return status_now;
    }
}
