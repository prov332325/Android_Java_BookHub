package com.example.myapplication.socket;



import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.google.firebase.messaging.RemoteMessage;

//네, Firebase 관련 라이브러리(firebase-messaging)를 프로젝트에 추가하고 Gradle을 동기화하면 필요한 종속성들이 다운로드됩니다.
// 그래서 Firebase 메시징 서비스 클래스를 올바르게 확장하고 사용할 수 있게 됩니다.

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {


    private static final String TAG = "MyFirebaseMsgService";
    private RetrofitService service;


    private final String serverIp = "3.39.255.234";
    private final int port = 9999;



    @Override
    public void onCreate() {
        super.onCreate();
        service = RetrofitClient.getClient().create(RetrofitService.class);
    }




    // 앱이 포그라운드에 있을 때에만 on message received 가 실행됨.
    // 근데 그거에 대한 로직은 socket service 에 있잖아 ?
    // 그러니, 앱이 종료 되었을 때에 대한 로직만 짜주면 된다.




    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Log.d(TAG, "ㅇㄴㅁㄴㅁㅇㄴㅁㅇFrom: " + message.getFrom()); // 이게 뭔데요 ?

        // 메시지가 수신 되었을 때 처리 하는 코드. 어디로부터 수신돼?
        // A. 자바 소켓 서버로부터 받는다 !! 상대 유저가 소켓에 연결 되었는지 확인한 후에 연결안되었을때 db에 저장하고 fcm 서버로도 보내면 여기로 옴 !

        if(message.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + message.getData());
            // 여기에 데이터 메시지를 처리하는 로직을 추가합니다. 받은 메시지를 어떻게 하면 좋지 ??
            // 데이터 페이로드가 있음.
            // 데이터 페이로드가 뭐임?

            //메시지 받아서 !! 제이슨 파싱해서 noti만들어주깅깅깅


            // 데이터 페이로드에서 제목과 본문을 추출합니다. !! 근데 이내용 저내용 다 가져와야 할듯 ㅎㅎ

            String roomId = message.getData().get("roomId");
            String messageText = message.getData().get("message_text");
            String sender_nickname = message.getData().get("sender_nickname");

            String sender_number = message.getData().get("sender_number");
            String sender_emailId = message.getData().get("sender_emailId");
            String receiver_number = message.getData().get("receiver_number");
            String receiver_emailId = message.getData().get("receiver_emailId");



            // 알림을 생성하고 표시한다 !! 오마갓
          //  SocketManager.showNotification(getApplicationContext(),roomId, messageText, sender_nickname);

            // SocketManager 인스턴스를 사용하여 showNotification 호출
            SocketManager socketManager = SocketManager.getInstance(getApplicationContext(), serverIp,port);
            socketManager.showNotification(getApplicationContext(), roomId, messageText, sender_nickname, sender_number, sender_emailId, receiver_number, receiver_emailId);



            }

        // 데이터 메시지랑 알림 메시지는 뭐가 다르지 ?
        // 데: 사용자가 정의한 커스텀 키-값 쌍
        // 알: 사용자에게 표시되는 키 모음이 사전에 정의되어 있다. / 클라이언트 앱이 사용자를 대신해서 최종적


        if (message.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + message.getNotification().getBody());
            // 여기에 알림 메시지를 처리하는 로직을 추가합니다.

        }


    }
    // 위 on message received 는 서버로부터 메시지를 받는 부분인건지 ?
    // A. 네 자바 소켓 서버에서, fcm 으로 메시지를 보내는 경우임. (메시지를 수신하는 상대유저의 소켓이 연결되어있지 않을때. 즉 앱을 사용중이지 않음. 백그라운드에도 없음. )
    // FirebaseMessagingService 에서 receive 할 수 있게 됨.


    // on new token
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "새로 갱신된 (refreshed) token: " + token);
        // 새 토큰을 서버로 전송. 이 부분을 로그인 완성되면 하도록,,
        sendRegistrationToServer(token);
    }


    //
    private void sendRegistrationToServer(String token) {
        // 서버에 토큰을 전송하여 저장하는 코드
        // retrofit 으로 보낸다.

        // 먼저 쉐어드로부터 현재 로그인한 유저의 이메일 id 를 가져온다.
        SharedPreferences sharedPreferences = getSharedPreferences("session_contain", Context.MODE_PRIVATE);
        String emailId = sharedPreferences.getString("signin_email_id", null);


        if (emailId != null) {
            service.updateToken(emailId, token).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                    LoginResponse result = response.body();
                    Log.i("Fcm클래스 new토큰갱신성공", " reponse 진입함" );
                    Log.i("Fcm클래스 new토큰갱신성공", " php 응답코드: " + result.getCode() + ", 메시지: " + result.getMessage());
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                    throwable.getMessage();
                    Log.e("Fcm클래스 new토큰갱신성공", "onFailure:실패한이유: " + throwable.getMessage());

                }
            });


        }

    }



    // on delete

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
