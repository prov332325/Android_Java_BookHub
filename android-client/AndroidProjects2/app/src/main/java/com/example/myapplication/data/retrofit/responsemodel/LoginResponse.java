package com.example.myapplication.data.retrofit.responsemodel;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    // 모든 요소를 다 응답해야하는건 아니다. 기본적으로 값이 안들어가면 null 값이 됨. null 일때 gson은 이를 무시하기 때문에 괜찮음.
    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("user_number")
    private String user_number;

    @SerializedName("user_email")
    private String user_email;


    @SerializedName("user_nickname")
    private String user_nickname;



    public int getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }

    public String getUser_number() { return user_number; }

    public String getUser_email() { return user_email; }

    public String getUser_nickname() { return user_nickname; }
}
