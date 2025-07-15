package com.example.myapplication.data.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient_aladin {

    // API 호출시 사용하게 되는 첫번째 클래스이다.

    private static final String BASE_URL = "http://www.aladin.co.kr/ttb/api/"; //URL은 주소를 포함한 완전한 리소스의 경로를 의미하므로 http 명시 해줘야함.
    private static Retrofit retrofit = null;

    static Gson gson = new GsonBuilder().setLenient().create(); //json 직렬/역직렬화.

    private RetrofitClient_aladin() {}
    public static Retrofit getClient(){
        if(retrofit==null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // 로그 수준 설정

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging); // 로깅 인터셉터 추가


            retrofit = new Retrofit.Builder() // 레트로핏 빌더
                    .baseUrl(BASE_URL) //
                    .addConverterFactory(GsonConverterFactory.create(gson)) // JSON을 분석할 수 있는 객체를 추가하는 메서드.
                    .build(); // 최종적으로 레트로핏 객체를 생성함.
        }
        return retrofit;
    }
}
