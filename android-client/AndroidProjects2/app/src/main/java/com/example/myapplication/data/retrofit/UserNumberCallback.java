package com.example.myapplication.data.retrofit;

public interface UserNumberCallback {
    void onUserNumberReceived(String userNumber);
    void onError(String errorMessage);
}
