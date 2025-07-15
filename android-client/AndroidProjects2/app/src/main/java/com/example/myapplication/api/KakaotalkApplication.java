package com.example.myapplication.api;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class KakaotalkApplication extends Application {
    private static KakaotalkApplication instance;


    @Override
    public void onCreate() {
        super.onCreate();
        // Kakao SDK 초기화
        KakaoSdk.init(this, "46596e04da4d38f06e2a7e1748245c4e");

    }
}

//