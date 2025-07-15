package com.example.myapplication.data.recyclerview;

public class ChattingListData {

    //  방 번호, 마지막 메시지, 마지막 메시지 시간, 상대 번호, 프사, 닉네임,

    private int room_number;
    private int my_user_number;
    private String my_user_emailId;
    private int other_user_number;
    private String other_user_emailId;
    private String other_user_profile_img;
    private String other_user_nickname;
    private String last_message_txt;
    private String last_message_time;
    private int unread_msg_cnt;
    private int follow_each_other;


    // 생성자
    public ChattingListData(int room_number, int my_user_number, String my_user_emailId, int other_user_number,  String other_user_emailId, String other_user_profile_img, String other_user_nickname, String last_message_txt, String last_message_time, int unread_msg_cnt, int follow_each_other) {
        this.room_number = room_number;
        this.my_user_number = my_user_number;
        this.my_user_emailId = my_user_emailId;
        this.other_user_number = other_user_number;
        this.other_user_emailId = other_user_emailId;
        this.other_user_profile_img = other_user_profile_img;
        this.other_user_nickname = other_user_nickname;
        this.last_message_txt = last_message_txt;
        this.last_message_time = last_message_time;
        this.unread_msg_cnt = unread_msg_cnt;
        this.follow_each_other = follow_each_other;
    }

    // getter, setter


    public int getRoom_number() {
        return room_number;
    }

    public void setRoom_number(int room_number) {
        this.room_number = room_number;
    }

    public int getMy_user_number() {
        return my_user_number;
    }

    public void setMy_user_number(int my_user_number) {
        this.my_user_number = my_user_number;
    }

    public String getMy_user_emailId() {
        return my_user_emailId;
    }

    public void setMy_user_emailId(String my_user_emailId) {
        this.my_user_emailId = my_user_emailId;
    }

    public int getOther_user_number() {
        return other_user_number;
    }

    public void setOther_user_number(int other_user_number) {
        this.other_user_number = other_user_number;
    }

    public String getOther_user_emailId() {
        return other_user_emailId;
    }

    public void setOther_user_emailId(String other_user_emailId) {
        this.other_user_emailId = other_user_emailId;
    }

    public String getOther_user_profile_img() {
        return other_user_profile_img;
    }

    public void setOther_user_profile_img(String other_user_profile_img) {
        this.other_user_profile_img = other_user_profile_img;
    }

    public String getOther_user_nickname() {
        return other_user_nickname;
    }

    public void setOther_user_nickname(String other_user_nickname) {
        this.other_user_nickname = other_user_nickname;
    }

    public String getLast_message_txt() {
        return last_message_txt;
    }

    public void setLast_message_txt(String last_message_txt) {
        this.last_message_txt = last_message_txt;
    }

    public String getLast_message_time() {
        return last_message_time;
    }

    public void setLast_message_time(String last_message_time) {
        this.last_message_time = last_message_time;
    }

    public int getUnread_msg_cnt() {
        return unread_msg_cnt;
    }

    public void setUnread_msg_cnt(int unread_msg_cnt) {
        this.unread_msg_cnt = unread_msg_cnt;
    }

    public int getFollow_each_other() {
        return follow_each_other;
    }

    public void setFollow_each_other(int follow_each_other) {
        this.follow_each_other = follow_each_other;
    }
}
