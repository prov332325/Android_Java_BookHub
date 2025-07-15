package com.example.myapplication.data.retrofit.datamodel;

import com.google.gson.annotations.SerializedName;

public class ToMeChatData {

    @SerializedName("message_id")
    private int message_id;

    @SerializedName("rooms_id")
    private int rooms_id;

    @SerializedName("sender_id")
    private int sender_id;

    @SerializedName("receiver_id")
    private int receiver_id;

    @SerializedName("message_content")
    private String message_content;

    @SerializedName("message_sent_time")
    private String message_sent_time;

    @SerializedName("is_read")
    private int is_read;


    // getter & setter


    public int getMessage_id() {
        return message_id;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public int getRooms_id() {
        return rooms_id;
    }

    public void setRooms_id(int rooms_id) {
        this.rooms_id = rooms_id;
    }

    public int getSender_id() {
        return sender_id;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public int getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(int receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getMessage_content() {
        return message_content;
    }

    public void setMessage_content(String message_content) {
        this.message_content = message_content;
    }

    public String getMessage_sent_time() {
        return message_sent_time;
    }

    public void setMessage_sent_time(String message_sent_time) {
        this.message_sent_time = message_sent_time;
    }

    public int getIs_read() {
        return is_read;
    }

    public void setIs_read(int is_read) {
        this.is_read = is_read;
    }
}
