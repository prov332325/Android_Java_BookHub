package com.example.myapplication.data.recyclerview;

public class FollowingListData {

    private int user_number;
    private String user_emailId;
    private String user_nickname;
    private String user_profileImg;
    private String following_status;


    // 생성자
    public FollowingListData(int user_number,String user_emailId, String user_nickname, String user_profileImg, String following_status) {
        this.user_number = user_number;
        this.user_emailId = user_emailId;
        this.user_nickname = user_nickname;
        this.user_profileImg = user_profileImg;
        this.following_status = following_status;
    }


    // getter setter

    public int getUser_number() {
        return user_number;
    }

    public void setUser_number(int user_number) {
        this.user_number = user_number;
    }

    public String getUser_emailId() {
        return user_emailId;
    }

    public void setUser_emailId(String user_emailId) {
        this.user_emailId = user_emailId;
    }

    public String getUser_nickname() {
        return user_nickname;
    }

    public void setUser_nickname(String user_nickname) {
        this.user_nickname = user_nickname;
    }

    public String getUser_profileImg() {
        return user_profileImg;
    }

    public void setUser_profileImg(String user_profileImg) {
        this.user_profileImg = user_profileImg;
    }

    public String getFollowing_status() {
        return following_status;
    }

    public void setFollowing_status(String following_status) {
        this.following_status = following_status;
    }
}


