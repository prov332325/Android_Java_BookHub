package com.example.myapplication.data.retrofit.responsemodel;

import com.google.gson.annotations.SerializedName;

public class KakaoJoinResponse {
    //
    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;


    @SerializedName("kakao_id")
    private String kakao_id;

    @SerializedName("kakao_email")
    private String kakao_email;


    @SerializedName("kakao_nickname")
    private String kakao_nickname;

    @SerializedName("kakao_imgUrl")
    private String kakao_imgUrl;



    public int getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }

    public String getKakao_id() { return kakao_id; }

    public String getKakao_email() { return kakao_email; }

    public String getKakao_nickname() { return kakao_nickname; }

    public String getKakao_imgUrl() { return kakao_imgUrl; }
}
