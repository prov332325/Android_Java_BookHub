package com.example.myapplication.socket;

import static android.content.Context.MODE_PRIVATE;
import static com.example.myapplication.layout.ChattingViewActivity.IS_CHAT_ACTIVITY_RUNNING;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.os.Handler; // 이걸로 import 해야함.
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.layout.ChattingViewActivity;
import com.example.myapplication.layout.MainActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SocketManager {

    // import
    private Context context;  // Context를 멤버 변수로 선언

    // 기존 회원 정보 가져오는 쉐어드
    SharedPreferences sharedPreferences; // 쉐어드 초기화


    // 채팅방 정보 저장하는 쉐어드
//     SharedPreferences chatPreferences;

    private static final String CHAT_PREFS_NAME = "ChatPrefs";
    private static final String CURRENT_ROOM_ID = "currentRoomId";




    private static SocketManager instance = null;
    private Socket socket;


    // broad cast receiver
    LocalBroadcastManager localBroadcastManager;



    // notification 그룹 키

    private BufferedWriter out;
    private BufferedReader in;


    private String serverIp;
    private int port;
    private Handler handler;


    // string
    String user_number;
    String nickname;
    String emailId;

    String roomId;


    // string
    String key = "signin_email_id"; // 쉐어드 키
    String signin_email_id_value; // 쉐어드 값 (이메일아이디, 닉네임 json string 값 - 파싱 전)




    // socket 서버로부터 받은 string 값 -> intent 로 노티피케이션, 브로드 캐스트 보낼거임.
       String sent_message;
       String sent_time;
       String sender_number;
       String sender_emailId;
       String sender_nickname;
       String receiver_number;
       String receiver_emailId;
       String receiver_nickname;


    public SocketManager(Context context, String serverIp, int port) {
        Log.d("소켓매니저", "SocketManager 생성자 호출됨");
        this.context = context;
        this.serverIp = serverIp;
        this.port = port;
        this.handler = new Handler(Looper.getMainLooper());
        createNotificationChannel(); // notification 채널 post 하기


//        // check notification permission
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            Log.e("소켓매니저", "POST_NOTIFICATIONS 권한이 없습니다.");
//            return;
//        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e("소켓매니저", "POST_NOTIFICATIONS 권한이 없습니다.");
                // 권한 요청 추가
                ActivityCompat.requestPermissions(
                        (Activity) context,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
                return;
            }
        }



        // 위 노티 퍼미션이 없어서 리턴 당한거임... !!!

        Log.d("sharedPreferences 가져오기 전", "" );
        sharedPreferences = context.getSharedPreferences("session_contain", MODE_PRIVATE);
        signin_email_id_value = sharedPreferences.getString(key, "");
        Log.d("소켓매니저 쉐어드 if 진입전", "signin_email_id_value:   " + signin_email_id_value);
        if (!signin_email_id_value.equals("") || signin_email_id_value !=null) {
            Log.d("소켓매니저 쉐어드 try진입전", "signin_email_id_value:   " + signin_email_id_value);
            try {
                JSONObject jsonObject = new JSONObject(signin_email_id_value);
                emailId = jsonObject.optString("emailid", ""); // default 값 설정 가능
                nickname = jsonObject.optString("nickname", ""); // nullpoint exception 발생할 수 있으므로, 빈문자열로 두기
                //get_userNumber(emailId);
                Log.d("소켓매니저 쉐어드 emailid", "emailid:   " + emailId);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("소켓매니저 쉐어드 비었음", "쉐어드왜비었죠? : 진짜 비었나? >>>  " + signin_email_id_value);
//            Toast.makeText(getApplicationContext(), " 저장 액티비티 진입 실패. 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
//            finish();
        }
//          브로드 캐스트 리시버
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }


    // get instance
    public static synchronized SocketManager getInstance(Context context, String serverIp, int port) {
        if (instance == null) {
            instance = new SocketManager(context, serverIp, port);
        }
        return instance;
    }


    // 서버와 연결 한다. 서버와 연결 한다는 것은 메시지를 받을 준비를 한다는 것임.
    public void connect(String user_number) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("소켓매니저 connect", "connect 들어옴 ");

                try {
                    socket = new Socket(serverIp, port);

                    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                    // 소켓에 연결되자마자 현재 유저의 번호를 서버로 보냄.
//                    if(user_number != null) {
                    Log.d("소켓매니저 connect 1 emailId", "emailId :" + emailId);
                    out.write(emailId);
                    Log.d("소켓매니저 connect 2 emailId", "emailId :" + emailId);
                    out.newLine();
                    out.flush();
//                    } else {
//                        Log.d("소켓매니저 유저번호없음", "쉐어드로부터 유저 넘버 못가져옴 " );
//
//                    }

                    // receive - while 문으로 돈다 계속.
                    receiveMessage();

//                    try {
//                        // 메시지를 수신한다.
//                        while (true && in.readLine() !=null ) {
//                            String message = in.readLine(); // 서버로부터 메시지를 읽어옴.
//                            Log.d("소켓매니저 메시지받음", "받은메시지: " + message);
//                            // broadcast
//                            // 서버에서 받아온 메시지를 상대에게 보내기 위해.
//                            // notification
//
//                            // 핸들러가 뭐길래.
//                            // 현재 위치한 액티비티가 어딘가?
//
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    // ui 업데이트 코드 !!
//                                    // 여기서 알람을 보내거나 !!! 아니면 리사이클러뷰에 뿌려주는 역할을 하는건가 ?
//                                }
//                            });
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                // 연결 오류 처리
//                                Log.d("소켓 매니저 메시지 받기 실패 catch 문", "서버로부터 메시지 받기 실패");
//                            }
//                        });
//                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // 연결 오류 알려준다.
                            Log.d("소켓 매니저 메시지 받기 실패 catch 문", "서버로부터 메시지 받기 실패");
                        }
                    });
                }
            }
        }).start();

    } // connect 메소드 끝


    // 메시지 수신 하는 메소드
    public void receiveMessage() {
        Log.d("메시지 받는 receive 로 들어옴!! ", "받은메시지는? receive ");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 메시지를 수신한다.
                    String message;
                    while (true && (message = in.readLine()) != null) {
                        Log.d("소켓매니저 메시지받음", "받은메시지: " + message);

                        // 받은 메시지는 json 임.
                        // json 여부 확인. 하고 파싱하기.
                        // 또한 MESSAGE 인지 확인하기.
                        if (isVaildJson(message)) {
                            // 브로드 캐스트 인텐트를 생성한다.
                            try {
                                JsonObject object = JsonParser.parseString(message).getAsJsonObject();

                                String type = object.get("type").getAsString();

                                if (type.equals("MESSAGE")) {
                                    roomId = object.get("roomId").getAsString();
                                    sent_message = object.get("sent_message").getAsString();
                                    sent_time = object.get("sent_time").getAsString();
                                    sender_number = object.get("sender_number").getAsString();
                                    sender_emailId = object.get("sender_emailId").getAsString();
                                    sender_nickname = object.get("sender_nickname").getAsString();
                                    receiver_number = object.get("receiver_number").getAsString();
                                    receiver_emailId = object.get("receiver_emailId").getAsString();
                                    receiver_nickname = object.get("receiver_nickname").getAsString();

                                    // sent time -> 로컬 시간으로 형변환 해주기
                                    // utc time -> local time 으로 변경 해주기.
                                    String message_localTime = convertUTCtoLocaltime(sent_time);

                                    Intent intent = new Intent("NEW_MESSAGE");
                                    intent.putExtra("type", "MESSAGE");
                                    intent.putExtra("message_content", sent_message);
                                    intent.putExtra("roomId", roomId);
                                    intent.putExtra("message_time", message_localTime); // local time
                                    intent.putExtra("sender_number", sender_number); // 상대
                                    intent.putExtra("sender_emailId", sender_emailId);
                                    intent.putExtra("sender_nickname", sender_nickname);
                                    intent.putExtra("receiver_number", receiver_number); // 나
                                    intent.putExtra("receiver_emailId", receiver_emailId);
                                    intent.putExtra("receiver_nickname", receiver_nickname);

                                    localBroadcastManager.sendBroadcast(intent);



///public void showNotification(Context context, String roomId, String message, String senderNickname, String senderNumber, String senderEmailId, String receiverNumber, String receiverEmailId) {

                                    // 채팅방 액티비티가 실행 중이 아닐 때 알림 처리 !!!!
                                    if (!isChatActivityVisible()) {
                                        Log.d("받는 클라- 채팅 acti 실행x", "");
                                        Log.d("받는 클라- 채팅 acti 실행x", "받은 메시지: " + sent_message + ", 보낸사람 닉네임: " + sender_nickname);
                                        showNotification(context, roomId, sent_message, sender_nickname, sender_number, sender_emailId, receiver_number, receiver_emailId);
                                    }
                                    else {
                                        // 실행 중이지만!! 현재 보내는 사람이랑, 내가 방에 있는 상대랑 다를때에도,, 알림이 와야 한닫.
                                        // 이걸 어케 가져 오지?
                                        String currentRoomId = getCurrentRoomId();
                                        if (currentRoomId == null || !currentRoomId.equals(roomId)) {
                                            // 현재 방에 없는 다른 유저로부터 온 메시지라면 알림을 띄운다
                                            Log.d("채팅실행중이나 다른방!!", "현재 룸: " + currentRoomId + ", 메시지온 룸: " + roomId);
                                            showNotification(context, roomId, sent_message, sender_nickname, sender_number, sender_emailId, receiver_number, receiver_emailId);
                                        } else {
                                            Log.d("채팅실행중이고 같은방임!! else ", "현재 룸: " + currentRoomId + ", 메시지온 룸: " + roomId);
                                        }


                                    }


                                } else if (type.equals("IS_READ")) {

                                    String sender_emailId = object.get("sender_emailId").getAsString();
                                    String receiver_emailId = object.get("receiver_emailId").getAsString();
                                    String read_time = object.get("read_time").getAsString();
                                    Intent intent = new Intent("NEW_MESSAGE");
                                    intent.putExtra("type", "IS_READ");
                                    intent.putExtra("sender_emailId", sender_emailId);
                                    intent.putExtra("receiver_emailId", receiver_emailId);
                                    intent.putExtra("read_time", read_time);

                                    localBroadcastManager.sendBroadcast(intent);


                                } else if (type.equals("NOT_AVAILABLE")) {
                                    // 둘중 한명 이라도 팔로우 상태가 아닐때 ~!, 혹은 그냥 아무도 팔로 안 하거나, 오류 났을 때 NOT_FOLLOW_OR_ERROR
                                    String alert_message = object.get("alert_message").getAsString();
                                    Intent intent = new Intent("NEW_MESSAGE");
                                    intent.putExtra("type", "NOT_AVAILABLE");
                                    intent.putExtra("alert_number", "2");

                                    if(alert_message.equals("맞팔아님")) {
                                        intent.putExtra("alert_message", "맞팔아님");
                                    } else if (alert_message.equals("NOT_FOLLOW_OR_ERROR")) {
                                        intent.putExtra("alert_message", "NOT_FOLLOW_OR_ERROR");
                                        intent.putExtra("alert_number", "3");
                                    }
                                    localBroadcastManager.sendBroadcast(intent);

                                }


                            } catch (JsonSyntaxException je) {
                                je.printStackTrace();
                            }

                        } else { // 여기로 빠질 일 없음.
                            Log.d("서버로 받은 메시지가 json이 아님", "받은메시지: " + message);
                        }


                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // ui 업데이트 코드 !!
                                // 여기서 알람을 보내거나 !!! 아니면 리사이클러뷰에 뿌려주는 역할을 하는건가 ?
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // 연결 오류 처리
                            Log.d("소켓 매니저 메시지 받기 실패 catch 문", "서버로부터 메시지 받기 실패");
                        }
                    });
                }
            }
        }).start();
    }


    // 현재 유저 방번호 가져오기
    private String getCurrentRoomId() {
        SharedPreferences chatPreferences = context.getSharedPreferences(CHAT_PREFS_NAME, Context.MODE_PRIVATE);
        return chatPreferences.getString(CURRENT_ROOM_ID, null);
    }

    // 메시지 보내기
    public void sendMessage(String message) {
        //while 이 아니니까 메시지를 전송할때마다 실행되는 스레드. 흐름이 끝난다.
        //
        new Thread(new Runnable() {
            @Override
            public void run() {
                try { // 작성한 메시지를 서버로 보냄.
                    Log.d("소켓매니저 send message 진입", "메시지 가져옴! : " + message);

                    out.write(message + "\n");
                    out.flush();
                    Log.d("소켓매니저 send message write", "메시지 보냄ㅎㅎ! : " + message);

                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            }
        }).start();
    }


    // 서버로부터 받은 메시지 json 인지 확인하는 메소드
    private boolean isVaildJson(String jsonString) {
        try {
            JsonParser.parseString(jsonString).getAsJsonObject();
            return true;
        } catch (JsonSyntaxException je) {
            je.printStackTrace();
            return false;
        }
    }


    // UTC -> Local 형변환
    String convertUTCtoLocaltime(String utcTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Z는 UTC 시간을 나타냄. ISO 8601 표준에 의거.
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 현지 시간을 위한 포맷

        try {
            Date date = sdf.parse(utcTime); // 가지고 온 utc time string값을 date 로 형변환
            localFormat.setTimeZone(TimeZone.getDefault()); // 현지 시간을 반환하도록 기존 UTC 데이트 폼을 변경 해줌.
            return localFormat.format(date); // 포맷팅 다시 시킴.
        } catch (ParseException e) {
            e.printStackTrace();
            return utcTime;
        }

    }


    // 소켓 닫기
    public void close() {
        try {
            if (socket != null) {
                socket.close();
                Log.d("소켓 매니저 socket close", "소켓 닫음 socket close");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 현재 채팅 액티비티가 활성화 되어 있는지 확인하기
    private boolean isChatActivityVisible() {
        if (IS_CHAT_ACTIVITY_RUNNING) {
            Log.d("현재 채팅 액티비티 실행 중 ... ", "Chatting View Activity 실행 중");
            return true;
        } else {
            Log.d("현재 채팅 액티비티 실행아님 ... ", "Chatting View Activity 실행 중 아님 !!!! ");
            return false;
        }
    }


    // 현재 유저가 채팅 액티비티가 아닐때 !! Notification 을 보내는 로직.
    public void showNotification(Context context, String roomId, String message, String senderNickname, String senderNumber, String senderEmailId, String receiverNumber, String receiverEmailId) {
        // 방번호, 메시지
        // 보내는 사람 닉네임, 번호, 이메일
        // 받는 사람 번호, 이메일

        // 방번호 가져와서 그룹 id 로 만들어주기
        Log.d("showNotification 진입 ", "받은 메시지: " + message + ", 보낸사람 닉네임: " + senderNickname + ", 인텐트에 있는 보낸사람 닉네임: " + sender_nickname);
        // sender_nickname == senderNickname

        String GROUP_KEY_CHAT = "com.example.CHAT_GROUP";
        String groupKey = GROUP_KEY_CHAT + roomId;

        Intent intent = new Intent(context, ChattingViewActivity.class); // 채팅 상세보기로 감
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 채팅방이 없을때에는 채팅방을 만들지만, 채팅방 액티비티가 이미
        // 최상위에 있는 경우라면, on create 가 아닌 on new intent 임.

        //        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        // FLAG_ACTIVITY_NEW_TASK :
        // FLAG_ACTIVITY_CLEAR_TASK :

        intent.putExtra("roomId", roomId);
        intent.putExtra("FROM_WHERE", "FROM_NOTIFICATION");
        intent.putExtra("this_user_nickname", senderNickname);
        intent.putExtra("this_user_number", senderNumber);
        intent.putExtra("this_user_emailID", senderEmailId);
        intent.putExtra("login_user_number", receiverNumber);
        intent.putExtra("login_user_emailId", receiverEmailId);
        intent.putExtra("fragment", "chatting");
        intent.putExtra("groupKey", groupKey);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // 새로운 작업 스택을 생성하고, 그 스택에다가 인텐트를 추가한다는 뜻이다. 주로 알림을 통해 어플 시작할때 사용된다.
        // 사용자가 알림을 클릭했을때 특정 액티비티로 이동하도록 하고, 뒤로 갈때에도 예상대로 앱이 이전 화면으로 돌아가게 할때 사용한다.



        stackBuilder.addNextIntentWithParentStack(intent);
        // Q. 그럼 이거 없으면 매니페스트에 설정한 parent 도 소용없나??
        // a. 소용이 없는지 까지는 모르겠지만
        // 위 메소드로 인해 매니페스트에 설정한 ChattingViewActivity 의 부모 액티비티 main 까지 스택에 포함된다.
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(Integer.valueOf(roomId), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // 그래서 최종적으로 intent 를 래핑한다.
        // 다른 애플리케이션이나 시스템 컴포넌트가 intent 를 실행할 수 있게한다.
        // stackBuilder 를 가지고 pending 인텐트 생성하는 코드임.

//        // 알림 클릭 시, 그룹 내에 모든 알림을 취소 시키는 인텐트를 생성한다.
//        Intent cancelIntent = new Intent(context, NotificationReceiver.class);
//       // cancelIntent.putExtra("groupKey", GROUP_KEY_CHAT + roomId);
//        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context,Integer.valueOf(roomId), cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        // request code 가 같다는 것은??
        // pending intent 를 고유하게 식별하기 때문에 !!!
        // 같은 방 아이디를 가진 노티피케이션을 클릭하면 기존 팬딩인텐트가 불러와진다. 즉 같은 곳으로 가게 된다 ~~

        // flag: FLAG_UPDATE_CURRENT => 동일한 요청 코드를 가진 기존의 팬딩 인텐트가 존재할 경우, 새로운 intent 정보로 업뎃한다.
        // flad: FLAG_IMMUTABLE => 팬딩인텐트 변경 불가능하도록 만든다.
        // 이를 통해 인텐트 내용을 바꾸지 못하게 한다. 안드로이드 12, API 레벨 31 에서 권장된다.


        /// pending intent 요약 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        /// 그러니까 기본적으로 알림 클릭했을때 이동하기 위해서 사용하는 팬딩인텐트인데,
        // 스택 관리를 위해서 stack builder 를 사용해서 뒤로 가기 했을때 원하는 내용이 나오게 한다.
        // 그리고 필요한 intent 내용을 정의하고 pending intent 객체 생성할때 intent 넣으면 알림 클릭할때마다 가지고 간다.
        //리퀘스트 코드는 팬딩 인텐트에게 고유성을 부여한다. 같은 코드는 같은 곳으로 이동 시킴.





        // 1) 개별 알림 생성 설정
        // notification compat . builder 는
        // 알림을 생성하고 설정하는데 사용된다.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "my_notification_channel")
                .setSmallIcon(R.drawable.mainimg)
                .setContentTitle(senderNickname)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)// 여기에도 중요도를 high 로 설정해줘야지 그룹핑 했을때 메시지 받으면 그룹에 알림 추가될때마다 헤드업 뜸.
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}) // 알림 진동 패턴 설정한다.
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true) // 알람 클릭시 자동으로 삭제 되도록 설정함. 삭제 되긴하는데 그룹이 안 됨.
//                .setDeleteIntent(cancelPendingIntent) // 알림 삭제 시 그룹 내 모든 알림 취소
                .setContentIntent(pendingIntent) // 이 메서드는 set content intent 사용자가 알림을 "클릭" 했을 때 실행될 pending intent 를 설정 한다.
                .setGroup(groupKey); // 알림 그룹을 설정한다. 그룹키는!!  com.example.CHAT_GROUP20 이런식임.



        // 1) 개별 알림 표시
        // 개별알림을 표시하는 부분이다.
        // 알림의 고유 id 를 설정하고 알림을 표시한다. 나는 시간으로 줘서 모든 알림이 개별적으로 나뉘게 했고, 그룹 설정을 따로 해줬을뿐이다.
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        int current_time = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        Log.d("노티id, current time!!  ", "current_time : " + current_time);
        notificationManagerCompat.notify(current_time, builder.build()); // current time 은 노티피케이션 id 로 request code 와는 다른 개념이다.





        // 3) 그룹 요약 알림 생성
        // 그룹 요약 알림 생성
        // 여기에도 중요도 설정을 해줘야 한다.
        Notification summaryNotification = new NotificationCompat.Builder(context, "my_notification_channel") // 새로운 NotificationCompat.Builder 의 객체를 생성해 알림을 구성한다.
                // 알림 채널 id 로 위 문자열을 사용한다.
                .setContentTitle("새 메시지가 있습니다") // 알림 그룹에 대한 title
                .setSmallIcon(R.drawable.mainimg) // 알림 이미지
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // 중요도를 HIGH로 설정
                .setStyle(new NotificationCompat.InboxStyle() // 알림의 스타일을 설정한다. 여러줄의 텍스트를 표시할 수 있는 스타일이다. 이거 꼭 필요해 ?
//                        .addLine(message) // 요약 알림에 추가 표시될때 내용을 한줄추가된다. 최근 내용 볼 수 있음. 이거 없어도 되지 않나? 없으면 최신으로 업뎃되는건지 보기~~~
                        .setBigContentTitle(senderNickname) // 알림이 확장되었을 때의 큰 제목을 설정한다.
                        .setSummaryText("새 메시지가 있습니다")) // 알람 그룹화 했을때에 요약 알림이다.
                .setGroup(groupKey) // 여러개의 개별 알림을 위 그룹키를 통해 감.
                .setGroupSummary(true) // 알림을 요약하는데, 그룹 요약 알림이기 때문에 같은 그룹 키를 가진 사람끼리 개별 알림을 하나로 묶어서 보여준다.
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
//                .setDeleteIntent(cancelPendingIntent) // 알림 삭제 시 그룹 내 모든 알림 취소
                .build(); // 알림 빌드해서 Notification 객체 생성한다.

        notificationManagerCompat.notify(roomId.hashCode(), summaryNotification);
    }




    // ===================================== Notification 에 대하여 =====================================

    // Notification

    // NotificationCompat, NotificationCompat.Build : 알림을 생성하고 구성하는데에 사용된다. 이 빌더를 통해 알림의 아이콘, 제목, 내용, pending intent 등등 설정한다.

    // NotificationManagerCompat : 구성된 알림을 시스템에 표시하거나 삭제하는데에 사용된다. 이를 통해 알림을 실제로 표시하거나, 특정 id 또는 그룹 키를 기반으로 알림을 취소할 수 있다. !!







    // 채널 설정하기
    public void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "채널이름";
            String description = "채널설명";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel("my_notification_channel", name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000, 1000});
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



//    public class NotificationReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String groupKey = intent.getStringExtra("groupKey");
//            if (groupKey != null) {
//                cancelGroupNotifications(context, groupKey);
//            }
//        }
//
//        // 그룹 내의 모든 알림을 취소하는 메서드
//        private void cancelGroupNotifications(Context context, String groupKey) {
//            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
//                for (StatusBarNotification notification : notifications) {
//                    if (groupKey.equals(notification.getNotification().getGroup())) {
//                        notificationManager.cancel(notification.getId());
//                    }
//                }
//            }
//        }
//    }


} // socket manager









