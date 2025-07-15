package com.example.myapplication.data.retrofit.datamodel;

import com.google.gson.annotations.SerializedName;

public class KakaoJoinData {


    @SerializedName("kakao_id")
    private String kakao_id;

    @SerializedName("kakao_email")
    private String kakao_email;


    @SerializedName("kakao_nickname")
    private String kakao_nickname;


    @SerializedName("kakao_imgUrl")
    private String kakao_imgUrl;


    public String getKakao_id() {
        return kakao_id;
    }

    public void setKakao_id(String kakao_id) {
        this.kakao_id = kakao_id;
    }

    public String getKakao_email() {
        return kakao_email;
    }

    public void setKakao_email(String kakao_email) {
        this.kakao_email = kakao_email;
    }

    public String getKakao_nickname() {
        return kakao_nickname;
    }

    public void setKakao_nickname(String kakao_nickname) {
        this.kakao_nickname = kakao_nickname;
    }

    public String getKakao_imgUrl() {
        return kakao_imgUrl;
    }

    public void setKakao_imgUrl(String kakao_imgUrl) {
        this.kakao_imgUrl = kakao_imgUrl;
    }


    public KakaoJoinData(String kakao_id, String kakao_email, String kakao_nickname, String kakao_imgUrl) {
        this.kakao_id = kakao_id;
        this.kakao_email = kakao_email;
        this.kakao_nickname = kakao_nickname;
        this.kakao_imgUrl = kakao_imgUrl;
    }
}
