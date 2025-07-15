package com.example.myapplication.data.retrofit.responsemodel;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChattingRoomResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("follow_each_other")
    private int follow_each_other;

    @SerializedName("my_user_number") // 내 번호
    private String my_user_number;

    @SerializedName("chat_room_items")
    private List<ChattingRoomResponse> chat_room_items; // 채팅방 목록

    // 채팅방 목록용 추가
    @SerializedName("chat_room_number")
    private String chat_room_number; // 채팅방번호

    @SerializedName("last_sent_message") // 마지막 보낸 메시지
    private String last_sent_message;

    @SerializedName("last_sent_time") // 마지막 보낸 메시지 시간
    private String last_sent_time;

    @SerializedName("user_number") // 상대 번호
    private String user_number;

    @SerializedName("user_emailId") // 상대 이메일 아이디
    private String user_emailId;


    @SerializedName("user_nickname") // 상대 닉네임
    private String user_nickname;

    @SerializedName("profile_img") // 상대 프로필 이미지
    private String profile_img;

    @SerializedName("unread_cnt") // 안 읽은 메시지 개수
    private String unread_cnt;



    // Getters and Setters for all fields...

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getFollow_each_other() {
        return follow_each_other;
    }

    public void setFollow_each_other(int follow_each_other) {
        this.follow_each_other = follow_each_other;
    }

    public String getMy_user_number() {
        return my_user_number;
    }

    public void setMy_user_number(String my_user_number) {
        this.my_user_number = my_user_number;
    }

    public String getUser_emailId() {
        return user_emailId;
    }

    public void setUser_emailId(String user_emailId) {
        this.user_emailId = user_emailId;
    }

    public List<ChattingRoomResponse> getChat_room_items() {
        return chat_room_items;
    }

    public void setChat_room_items(List<ChattingRoomResponse> chat_room_items) {
        this.chat_room_items = chat_room_items;
    }

    public String getChat_room_number() {
        return chat_room_number;
    }

    public void setChat_room_number(String chat_room_number) {
        this.chat_room_number = chat_room_number;
    }

    public String getLast_sent_message() {
        return last_sent_message;
    }

    public void setLast_sent_message(String last_sent_message) {
        this.last_sent_message = last_sent_message;
    }

    public String getLast_sent_time() {
        return last_sent_time;
    }

    public void setLast_sent_time(String last_sent_time) {
        this.last_sent_time = last_sent_time;
    }

    public String getUser_number() {
        return user_number;
    }

    public void setUser_number(String user_number) {
        this.user_number = user_number;
    }

    public String getUser_nickname() {
        return user_nickname;
    }

    public void setUser_nickname(String user_nickname) {
        this.user_nickname = user_nickname;
    }

    public String getProfile_img() {
        return profile_img;
    }

    public void setProfile_img(String profile_img) {
        this.profile_img = profile_img;
    }

    public String getUnread_cnt() {
        return unread_cnt;
    }

    public void setUnread_cnt(String unread_cnt) {
        this.unread_cnt = unread_cnt;
    }
}
