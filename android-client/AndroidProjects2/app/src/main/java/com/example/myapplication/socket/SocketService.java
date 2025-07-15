package com.example.myapplication.socket;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.layout.ChattingViewActivity;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/*
* 서비스는 액티비티와 독립적으로 실행된다.
* 사용자가 앱 내의 여러 액티비티를 이동하더라도 connect 를 실행했기 때문에 소켓 연결이 유지될 수 있다.
* */

public class SocketService extends Service {



    // import

    SharedPreferences sharedPreferences; // 쉐어드 초기화
    RetrofitService service; // 레트로핏 서비스

    private SocketManager socketManager; //
    private final String serverIp = "3.39.255.234";
    private final int port = 9999;


    // string
    String user_number;


    // string
    String key = "signin_email_id"; // 쉐어드 키
    String signin_email_id_value ; // 쉐어드 값 (이메일아이디, 닉네임 json string 값 - 파싱 전)


        /*
    서비스가 처음 시작될 때:
사용자가 로그인하면 SocketService가 처음 시작됩니다.
이 시점에서 onCreate와 onStartCommand가 순차적으로 호출됩니다.
onCreate에서 소켓 연결을 설정하고, onStartCommand는 인텐트를 처리합니다.
서비스가 이미 실행 중일 때:

사용자가 채팅 전송 버튼을 누를 때마다 startService를 호출하여 새로운 인텐트를 전달합니다.
이 경우 서비스가 이미 실행 중이므로 onCreate는 호출되지 않고, onStartCommand만 호출됩니다.
onStartCommand는 전달된 인텐트를 처리하여 메시지를 서버로 전송합니다.
    * */





    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("Socket Service 소켓 서비스", "온 크리에이트 - onCreate");
        socketManager = SocketManager.getInstance(this, serverIp, port); //

        // 여기서 쉐어드, 레트로핏
        sharedPreferences = this.getSharedPreferences("session_contain", Context.MODE_PRIVATE);
        signin_email_id_value = sharedPreferences.getString(key, "");
        socketManager.connect("뿌엥"); // 스레드임
        // 로그인 한 사용자 닉네임
//        if(!signin_email_id_value.equals("")) {
//            try {
//                JSONObject jsonObject = new JSONObject(signin_email_id_value);
//                String emailId = jsonObject.optString("emailid", ""); // default 값 설정 가능
//                // String nickname = jsonObject.optString("nickname", ""); // nullpoint exception 발생할 수 있으므로, 빈문자열로 두기
//                //get_userNumber(emailId);
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        } else {
//            Log.d("소켓매니저 쉐어드 비었음", "쉐어드왜비었죠? : 진짜 비었나? >>>  " + signin_email_id_value);
////            Toast.makeText(getApplicationContext(), " 저장 액티비티 진입 실패. 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
////            finish();
//        }

    } // on create 끝


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스 처음 시작시,
        // 1. on create -> on start command 호출 됨.
        // 서비스가 이미 실행 중이고, start service 가 다시 호출되면, on start command 만 호출 된당.
        // 서비스가 강제 종료된 후에 시스템에 의해 다시 서비스 재시작되면 START STICKY를 return 하는 경우에 한해 on start command 호출됨.

        // 서비스가 중단되었다가 다시 시작되는 경우에 이전 작업을 이어서 처리하기 위함.
        if (intent !=null && intent.hasExtra("message")) {
            Log.d("소켓 서비스 onStartCommand send !!", "onStartCommand 메시지 있음 " + intent.getStringExtra("message").toString());
            String message = intent.getStringExtra("message");
            sendMessageToServer(message);
        }

        return START_NOT_STICKY;
        // START_STICKY : 시스템에 의해 종료된 서비스가 자동으로 재시작 된다.
        // 네트워크 연결과 같은 지속적인 작업을 유지하는데 유용하다.
    } // onStartCommand 끝


    // 메시지 서버에 send 하는 메소드 !!
    private void sendMessageToServer(String message) {
        if (socketManager !=null ) {
            socketManager.sendMessage(message);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        super.onDestroy();
        if (socketManager != null) {
            socketManager.close();
        } else {
            Log.e("SocketService", "SocketManager is null in onDestroy");
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    // 유저 넘버 가져오는 메소드.
    public void get_userNumber (String user_emailid) {
        Log.i("소켓매니저에서 유저넘버가져오기", "유저의 emailId: " + user_emailid);
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의
        service.user_emailId(user_emailid).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("소켓매니저에서 number 체크 response 진입", "onResponse: 응답메시지: " + result.getMessage()); // echo로 보내주는 메시지.
                Log.e("소켓매니저에서 number 체크 response 진입", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.

                if(result.getCode() == 200 ) {
                    Log.e("소켓매니저 code==200!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                    user_number = result.getUser_number();
                   // 서비스가 실행될때 소켓 연결. 유저 넘버를 받은 후에 소켓 연결
                } else {
                    Log.e("php에서 뭔가 잘못됨. / code==400!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Log.e("소켓매니저에서 통신 failed", "실패원인: " +throwable.getMessage());
            }
        });
    }



}
