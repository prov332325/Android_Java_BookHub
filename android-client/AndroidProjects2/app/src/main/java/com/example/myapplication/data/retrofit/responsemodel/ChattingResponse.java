package com.example.myapplication.data.retrofit.responsemodel;

import com.example.myapplication.data.retrofit.datamodel.ToMeChatData;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChattingResponse {

    // code, message, 상대 유저 닉네임, 상대 프로필,  TomeChatData (채팅 내용, 보내는 사람, 받는 사람, 텍스트, 텍스트 시간, 읽음 여부? )
    // int 는 string 에 담긴다. 자동 형변환 됨.


    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("follow_each_other")
    private int follow_each_other;

    @SerializedName("user_number") // 상대번호
    private String user_number;

    @SerializedName("user_nickname") // 상대 닉네임
    private String user_nickname;

    @SerializedName("profile_img") // 상대 프사
    private String profile_img;


    // 채팅 내용 목록
    @SerializedName("chat_items")
    private List<ToMeChatData> chat_items; // 채팅 목록

    // 채팅방 목록용 추가


    @SerializedName("last_sent_message") // 마지막 보낸메시지
    private String last_sent_message;

    @SerializedName("last_sent_time") // 마지막 보낸 메시지 시간
    private String last_sent_time;

    @SerializedName("my_user_number") // 내 번호
    private String my_user_number;


    // 채팅 내용 목록
    @SerializedName("chat_room_number")
    private String chat_room_number; // 채팅방번호

    // 채팅방 목록 list
    @SerializedName("chat_room_items")
    private List<ChattingResponse> chat_room_items; // 채팅방 목록


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

    public String getChat_room_number() {
        return chat_room_number;
    }

    public int getFollow_each_other() {
        return follow_each_other;
    }

    public void setFollow_each_other(int follow_each_other) {
        this.follow_each_other = follow_each_other;
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


    // 마지막 보낸 메시지, 시간, 현재 로그인한 유저 번호 getter, setter


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

    public String getMy_user_number() {
        return my_user_number;
    }

    public void setMy_user_number(String my_user_number) {
        this.my_user_number = my_user_number;
    }



    public List<ToMeChatData> getChat_items() {
        return chat_items;
    }

    public void setChat_items(List<ToMeChatData> chat_items) {
        this.chat_items = chat_items;
    }





    public List<ChattingResponse> getChat_room_items() {
        return chat_room_items;
    }

    public void setChat_room_items(List<ChattingResponse> chat_room_items) {
        this.chat_room_items = chat_room_items;
    }


}


