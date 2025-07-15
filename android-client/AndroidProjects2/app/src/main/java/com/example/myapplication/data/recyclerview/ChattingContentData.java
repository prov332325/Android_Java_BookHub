package com.example.myapplication.data.recyclerview;

public class ChattingContentData {

    // 유저 번호, 닉네임, 유저 프로필 이미지, 메시지내용, 메시지 시간, 타입 type

    private int user_number;
    private int message_number;
    private String user_nickname;
    private String user_profile_img;
    private String message;
    private String message_time;
    private int is_read;
    private int type; // 나일 경우 1, 상대일 경우 2.
    private String showTimeLine; // 이전 아이템의 시간.


    // 생성자 - 리사이클러뷰에서는 필요함 !!
    public ChattingContentData(int user_number, int message_number ,String user_nickname, String user_profile_img, String message, String message_time, int is_read, int type, String showTimeLine) {
        this.user_number = user_number;
        this.message_number = message_number;
        this.user_nickname = user_nickname;
        this.user_profile_img = user_profile_img;
        this.message = message;
        this.message_time = message_time;
        this.is_read = is_read;
        this.type = type;
        this.showTimeLine = showTimeLine;
    }


    public int getUser_number() {
        return user_number;
    }

    public void setUser_number(int user_number) {
        this.user_number = user_number;
    }

    public int getMessage_number() {
        return message_number;
    }

    public void setMessage_number(int message_number) {
        this.message_number = message_number;
    }

    public String getUser_nickname() {
        return user_nickname;
    }

    public void setUser_nickname(String user_nickname) {
        this.user_nickname = user_nickname;
    }

    public String getUser_profile_img() {
        return user_profile_img;
    }

    public void setUser_profile_img(String user_profile_img) {
        this.user_profile_img = user_profile_img;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_time() {
        return message_time;
    }

    public void setMessage_time(String message_time) {
        this.message_time = message_time;
    }

    public int getIs_read() {
        return is_read;
    }

    public void setIs_read(int is_read) {
        this.is_read = is_read;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getShowTimeLine() {
        return showTimeLine;
    }

    public void setShowTimeLine(String showTimeLine) {
        this.showTimeLine = showTimeLine;
    }
}
