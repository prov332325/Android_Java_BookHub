package com.example.myapplication.data.retrofit.responsemodel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProfileViewResponse {

    // 있어야 하는 것.
    // 사용자 번호, 사용자 프사, 사용자 닉네임, 사용자 읽은책수, 읽고 있는책 수, 읽고 싶은 책 수, 팔로워수, 팔로잉수,
    // 사용자가 작성한 게시글 전부.

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("follow_each_other")
    private int follow_each_other;

    @SerializedName("user_number")
    private int user_number;

    @SerializedName("user_emailId")
    private String user_emailId;

    @SerializedName("user_nickname")
    private String user_nickname;

    @SerializedName("profile_img")
    private String profile_img;

    @SerializedName("profile_bio")
    private String profile_bio;



    @SerializedName("user_readBook_cnt")
    private int user_readBook_cnt;

    @SerializedName("user_readingBook_cnt")
    private int user_readingBook_cnt;

    @SerializedName("user_wantBook_cnt")
    private int user_wantBook_cnt;

    @SerializedName("follow_status_now")
    private String follow_status_now;

    @SerializedName("following_cnt")
    private int following_cnt;

    @SerializedName("follower_cnt")
    private int follower_cnt;



    // getter
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getFollow_each_other() { return follow_each_other; }


    public int getUser_number() {
        return user_number;
    }

    public String getProfile_img() {
        return profile_img;
    }

    public String getProfile_bio() { return profile_bio;
    }

    public String getUser_emailId() { return user_emailId; }

    public String getUser_nickname() {
        return user_nickname;
    }

    public int getUser_readBook_cnt() {
        return user_readBook_cnt;
    }

    public int getUser_readingBook_cnt() {
        return user_readingBook_cnt;
    }

    public int getUser_wantBook_cnt() {
        return user_wantBook_cnt;
    }

    public String getFollow_status_now() {
        return follow_status_now;
    }

    public int getFollowing_cnt() {
        return following_cnt;
    }

    public int getFollower_cnt() {
        return follower_cnt;
    }


    // 팔로워 목록을 가져오기 위해서 내부 클래스 생성

    public class ProfileViewResponse2 {

        @SerializedName("code")
        private int code;

        @SerializedName("message")
        private String message;

        @SerializedName("item")
        private List<ProfileViewResponse> user_list; // 팔로잉, 혹은 팔로워 유저 리스트


        // getter

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public List<ProfileViewResponse> getUser_list() {
            return user_list;
        }
    }


}
