package com.example.myapplication.layout;

import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {

    private String userNumber;

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public String getUserNumber() {
        return userNumber;
    }


}
