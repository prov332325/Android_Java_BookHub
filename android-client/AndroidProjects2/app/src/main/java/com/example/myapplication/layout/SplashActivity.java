package com.example.myapplication.layout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.myapplication.R;
import com.example.myapplication.api.KakaotalkApplication;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.socket.SocketManager;
import com.example.myapplication.socket.SocketService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {


    // socket
//    private SocketManager socketManager;  // socket manager
//    private String serverIp = "3.35.169.251";
//    private int port = 9999;



    SharedPreferences sharedPreferences;

    // 쉐어드 id, email 각각에 대한 키값
    String key = "signin_email_id";
    String signin_email_id_value ;


    // 레트로핏 객체 생성
     RetrofitService service;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);



        // fcm 토큰 확인 및 서버 전송



        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의
        sharedPreferences = getSharedPreferences("session_contain", Context.MODE_PRIVATE);



    } // on create 끝



    // 자동 로그인 혹은, 로그인 할 때 해당 유저에 대한 fcm 토큰을 db에 저장 한다.
    //



    // on start 에서 자동 로그인 처리하고 !! 자동 로그인 레트로핏 호출 response 에다가 소켓 연결시킨당.
    @Override
    protected void onStart() {
        super.onStart();
        signin_email_id_value = sharedPreferences.getString(key, null);
        // email or id 있는지 !
        if (signin_email_id_value==null) {  // 쉐어드가 아예 비었을때 !! 로그인 하러 가기
            Log.d("스플래시쉐어드비었음", "splash임. key:signin_email_id  value: "+signin_email_id_value);
            // 로그인화면으로 이동
            navigateToLogin();
        } else { // 쉐어드에 값이 있을때 !!
            Log.d("자동로그인값있음", "splash임. key:signin_email_id  value: "+signin_email_id_value);
            // JSON 파싱하여 특정 키 값 추출
            try {
                JSONObject jsonObject = new JSONObject(signin_email_id_value);
                String emailId = jsonObject.optString("emailid", ""); // default 값 설정 가능
                String nickname = jsonObject.optString("nickname", "");

                if (emailId.isEmpty()) {
                    Log.d("json아이디없음", "splash임. key:signin_email_id  value: "+signin_email_id_value);
                    navigateToLogin();
                } else {
                    Log.d("json아이디있음.", "splash임. key:signin_email_id  value: "+signin_email_id_value);
                    // fcm 토큰 확인 및, 자동 로그인 처리 로직
                    checkAndSendToken(emailId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                navigateToLogin(); // 파싱 오류 시 로그인 화면으로 이동
            }
        }
    }


    // 토큰 확인 하기 + 자동 로그인 하기 + 토큰 update
    private void checkAndSendToken(String emailId) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            navigateToLogin(); // 만에 하나 토큰을 가져오지 못한 경우에는 사용자를 로그인 화면으로 보내기 !!
                            Toast.makeText(getApplicationContext(), " 토큰 오류 ! 로그인 화면으로 이동합니다. 관리자에게 문의하세요.", Toast.LENGTH_SHORT).show();
                            return;
//                            Log.w("SplashActivity", "Fetching FCM registration token failed", task.getException());
//                            autoSigninCheckDB(emailId, null);
//                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        autoSigninCheckDB(emailId, token); // 토큰 가져와서 db 로 보내기. token 에 대해서는 update 할 예정
                    }
                });
    }



    // 쉐어드에 있는 email 혹은 id 값이 DB에 존재하는지
    private void autoSigninCheckDB (String emailId, String token ) {

        Log.d("자동로그인 및토큰저장하러!", "token: " + token + ", emailId: " + emailId );
        service.autoSignin(emailId, token).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                Log.d("자동로그인 DB 존재 여부", "서버자체에서 보낸code: " + response.code());
                LoginResponse result = response.body(); // 카카오 DB 저장하고 넘어온 값.
                Log.d("통신성공!!! ", "내가 보낸 code : " + result.getCode() );

                if (result.getCode() == 200) {
                    if(result.getMessage().contains("일치")) { // 메인 화면으로 이동
                        Log.d("자동로그인 성공!", "회원아이디혹은 이메일가져오기: " + result.getUser_email());
                        Log.d("자동로그인 성공!", "회원닉네임가져오기:  " + result.getUser_nickname());
                        // 일치할 경우에는 아이디도 가져오까 ?? 메인 화면에 띄워주게...

                        // 여기서 소켓 연결?? - 서비스 시작으로 변경.
//                        socketManager = new SocketManager(serverIp, port);
//                        socketManager.connect();
                        Intent serviceIntent = new Intent(SplashActivity.this, SocketService.class);
                        startService(serviceIntent);

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("user_emailid",result.getUser_email());
                        intent.putExtra("user_nickname",result.getUser_nickname());
                        startActivity(intent);
                        finish();

                    } else  { // 로그인 화면으로 이동
                        navigateToLogin();
                    }
                } else {
                    navigateToLogin();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Log.d("자동로그인확인 통신실패 fail진입", "onFailure: " + throwable.getMessage(), throwable);
            }
        });
    } // 쉐어드 내용을 디비에 확인하는 메소드

    private void navigateToLogin() {
        // 로그인 화면으로 이동
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }


}
