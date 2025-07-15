package com.example.myapplication.layout;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.recyclerview.ChattingContentAdapter;
import com.example.myapplication.data.recyclerview.ChattingContentData;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.datamodel.ToMeChatData;
import com.example.myapplication.data.retrofit.responsemodel.ChattingResponse;
import com.example.myapplication.socket.SocketService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChattingViewActivity extends AppCompatActivity {


    // activity running check
    public static boolean IS_CHAT_ACTIVITY_RUNNING;


    // 쉐어드에 방번호 저장하기
    private static final String PREFS_NAME = "ChatPrefs";
    private static final String CURRENT_ROOM_ID = "currentRoomId";


    // import
    RetrofitService service; // 레트로핏 서비스
    Intent getIntent;

    // 쉐어드 프리퍼런스 - 꼭 안해도 됨 intent 로 넘어옴.
    SharedPreferences sharedPreferences;
    String key = "signin_email_id";
    String signin_email_id_value; // 쉐어드에 저장된 이메일/아이디, 닉네임 키값쌍.
    String emailId; // 이메일/아이디만 파싱한 값.
    String signin_user_nickname; // 로그인한 유저 닉네임


    // view 초기화
    Toolbar toolbar; // 툴바
    TextView chatting_partner; // 현재 채팅중인 유저 닉네임.
    EditText chat_input; // 현재 유저가 작성한 메시지
    Button chatting_send_btn; // 채팅 입력후 전송 버튼 from me


    // 리사이클러뷰
    RecyclerView chatting_content_recyclerview; // 채팅 내용 리사이클러뷰.


    // 리사이클러뷰 어댑터
    ChattingContentAdapter chatAdapter; // 메시지 어댑터


    // 현재 로그인 유저
    int user_num_int;
    //String login_user_nickname;
    String login_user_emailId;


    // 상대 유저
    int this_usernum_int;
    String this_user_nickname;
    String this_user_emailID;


    // 프래그먼트로부터 온 유저들의 넘버 string 값
    String this_user_number;
    String login_user_number;

    // 상대유저 - 레트로핏
    String this_user_profileImg = null;


    // get intent - 노티피케이션을 타고 넘어온 넘버 string 값
    String roomId = null;


    // notification 그룹 키 만드는 틀
    private static final String GROUP_KEY_CHAT = "com.example.CHAT_GROUP";

    // boolean
    private boolean isLoading = false; // 페이징 로딩 상태를 추적


    // 채팅 내역 페이징을 위한 시간 변수
    // 날짜 구분선을 위한... before date


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatting_view);

        Log.d("생명주기확인onCreate !! ", "onStart 들어옴 ");


//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    try {
//                        Thread.sleep(1000);
//                        Log.d("현재시간", "" + getCurrentUTCTime());
//                    } catch (Exception e) {
//
//                    }
//                }
//
//            }
//        }).start();


        // ======================= 초기화 zone 시작 =================================

        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의


        // 툴바
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }


        // 뷰 연결
        chatting_partner = findViewById(R.id.chatting_partner); // 현재 대화 중인 유저 닉네임.
        chatting_send_btn = findViewById(R.id.chatting_send);

        chat_input = findViewById(R.id.chat_input); // 유저가 작성한 채팅 내용

        // 리사이클러뷰 연결
        chatting_content_recyclerview = findViewById(R.id.chatting_content_list_recyclerview); // 채팅 내용 리사이클러뷰

        // 리사이클러뷰 어댑터
        chatAdapter = new ChattingContentAdapter(ChattingViewActivity.this);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);

        // 레이아웃 매니저
        chatting_content_recyclerview.setLayoutManager(layoutManager);
        chatting_content_recyclerview.setAdapter(chatAdapter);


        // 쉐어드에서 현재 로그인한 유저 닉네임 가져오기
        // 여기서 쉐어드, 레트로핏
        sharedPreferences = this.getSharedPreferences("session_contain", Context.MODE_PRIVATE);
        signin_email_id_value = sharedPreferences.getString(key, "");
        // 로그인 한 사용자 닉네임
        if (!signin_email_id_value.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(signin_email_id_value);
                emailId = jsonObject.optString("emailid", ""); // default 값 설정 가능
                signin_user_nickname = jsonObject.optString("nickname", ""); // nullpoint exception 발생할 수 있으므로, 빈문자열로 두기

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("소켓매니저 쉐어드 비었음", "쉐어드왜비었죠? : 진짜 비었나? >>>  " + signin_email_id_value);
//            Toast.makeText(getApplicationContext(), " 저장 액티비티 진입 실패. 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
//            finish();
        }


        // ======================= 초기화 zone 끝 =================================

        // on create 1. 툴바 뒤로가기 클릭 시, 이전 화면으로 (프래그먼트로 나가게 됨.)
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChattingViewActivity.this, MainActivity.class);
                intent.putExtra("fragment", "chatting");
                startActivity(intent);
                finish();
            }
        });


        // on create 2. intent 로 닉네임, 현재 대화하는 양 유저들의 number, 대화상대 nickname 넘어옴
        // get intent 할 때

        // 기존 인텐트를 전역 변수로 만들어서
        // on new intent 에서 재정의 할 수 있게 만든다.

        getIntent = getIntent();
        if (getIntent.getStringExtra("FROM_WHERE").equals("FROM_NOTIFICATION")) {
            String groupKey = getIntent.getStringExtra("groupKey");
            if (groupKey != null) {
                // 그룹 내 모든 알림을 취소하는 메서드 호출
                cancelGroupNotifications(this, groupKey);
            }
        }


        // 상대유저 닉네임, 번호, 이메일아이티, 로그인 유저 이메일아이디, 로그인 유저 넘버
        roomId = getIntent.getStringExtra("roomId");
        this_user_nickname = getIntent.getStringExtra("this_user_nickname");
        this_user_number = getIntent.getStringExtra("this_user_number");
        this_user_emailID = getIntent.getStringExtra("this_user_emailID");
        login_user_number = getIntent.getStringExtra("login_user_number");
        //login_user_nickname = getIntent.getStringExtra("login_user_nickname");
        login_user_emailId = getIntent.getStringExtra("login_user_emailId");

        user_num_int = Integer.parseInt(login_user_number);
        this_usernum_int = Integer.parseInt(this_user_number);
        Log.d("1:1채팅하기 넘어옴!", "(다 string임 !!) 로그인한 유저 number: " + user_num_int + ", 로그인유저닉네임: " + signin_user_nickname + ", 대화상대유저number: " + this_user_number + ", 대화상대유저nickname: " + this_user_nickname);


        // 위 값을 가지고 상대 유저 정보 가져오기.
        chatting_room_record(user_num_int, this_usernum_int);

        // 채팅 하고 있는 유저 닉네임 적어줌.
        if (this_user_nickname != null) {
            chatting_partner.setText(this_user_nickname);
        }


        // on create 3. 채팅 전송 버튼
        chatting_send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 맞팔 확인 하기 !
                String message_text = null;
                message_text = chat_input.getText().toString();
                String time_now = getLocalTime();

                if (!message_text.isEmpty()) {
                    chatAdapter.addItem_NEW(new ChattingContentData(user_num_int, 0, signin_user_nickname, null, message_text, time_now, 0, 1, null));
                    chatAdapter.notifyDataSetChanged(); // 어댑터에 데이터 변경 알림
                    chat_input.setText(""); // 입력 필드 초기화
//                socketManager.sendMessage(message_text);
                    // 스크롤 맨 밑으로 내리기
                    scollToPosition(chatAdapter.getItemCount() - 1);

                    // service에 구현한 메시지 전송 메소드 가져오기
                    // json 메시지로 대화 참여하는 유저들 정보 + 메시지 만들기
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("login_user_number", login_user_number);
                    jsonObject.addProperty("login_user_emailId", login_user_emailId);
                    jsonObject.addProperty("login_user_nickname", signin_user_nickname);
                    jsonObject.addProperty("this_user_number", this_user_number);
                    jsonObject.addProperty("this_user_emailID", this_user_emailID);
                    jsonObject.addProperty("this_user_nickname", this_user_nickname);
                    jsonObject.addProperty("message", message_text);
                    jsonObject.addProperty("message_time", getCurrentUTCTime());
                    jsonObject.addProperty("type", "MESSAGE");

                    Gson gson = new Gson();
                    String jsonString = gson.toJson(jsonObject);
                    Log.d("메시지 전송이벤트!", "gson으로 만든 json string값: " + jsonString);

                    sendMessage(jsonString); // service 로 연결 됨.
                    // Toast.makeText(ChattingViewActivity.this, "채팅 작성 메소드 호출 !", Toast.LENGTH_SHORT).show();
//
                    chat_input.setText(""); // 클릭이벤트 끝내고 초기화 시키기.

                    // 채팅을 보내는 순간 기존 방 존재를 확인하고 나서 채팅 내용을 저장하도록 한다.
                    // 서버로 보내야 하는 정보는?
                    // 채팅을 보내는 유저번호, 채팅 받는 유저 번호, 채팅 내용.
                } else {
                    // 채팅 미입력 상태로 전송 버튼 누름

                    Log.d("빈 메시지 전송 이벤트", " 채팅 입력값 없음 ");
                    Toast.makeText(ChattingViewActivity.this, "메시지를 입력해 주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Log.d("생명주기확인 on create ", "현재방: " + roomId);

        // on create 4. notification 있으면 삭제하기
        cancelNotification(roomId);

        // on create 5. 상단 스크롤 감지 및 이전 메시지 로드하기
        chatting_content_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLoading) {// 로딩 중이 아닐 때만 다음 페이지 요청
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    Log.i("스크롤리스너!!로딩중아닐때", "가장 상단아이템: " + firstVisibleItemPosition);
                    int totalItemCnt = layoutManager.getItemCount();
                    Log.i("스크롤리스너!!로딩중아닐때", "현재 전체아이템개수: " + totalItemCnt);


                    if (layoutManager != null && roomId !=null) {
                        // 상단에 도달했는지 확인하기
                        if (firstVisibleItemPosition == 0 && !isLoading) {
                            isLoading = true;
                            // 데이터 20개 더 로딩 하기
                            int first_message_number = chatAdapter.getMessageNumber(0);
                            int roomId_int = Integer.parseInt(roomId);
                            Log.i("0번아이템 메시지넘버", "first_message_number: " + first_message_number);
                            loadMoreMessage(roomId_int, first_message_number);
                        }
                    }
                }
            }
        });
    } // on create 끝

    // 이전 메시지 로드 - 채팅 내용 페이징
    public void loadMoreMessage(int room_id, int first_message_number) {
        Log.i("loadMoreMessage 채팅 페이징 진입!!", "현재 방번호: " + room_id + ", first_message_number: " + first_message_number);
        service.loadMoreMessage(room_id, first_message_number).enqueue(new Callback<ChattingResponse>() {
            @Override
            public void onResponse(Call<ChattingResponse> call, Response<ChattingResponse> response) {
                //내용없음 인 경우에는? 마지막 메시지 입니다.
                ChattingResponse result = response.body();
                Log.i("채팅방에서 채팅기록 loadmore 메소드", " reponse 진입함");
                Log.i("채팅방에서 loadmore http 성공!!", " php 응답코드: " + result.getCode() + ", 메시지: " + result.getMessage());

                // 채팅 내용 목록 가져오기 - 서버에서
                List<ToMeChatData> chat_items = result.getChat_items();
                // 리사이클러뷰에 넣을 데이터를 위한 리스트 !!
                List<ChattingContentData> tempChatItems = new ArrayList<>();

                if (chat_items != null) {
                    // for문 1. 시간선 추가 여부를 포함해 tempChatItems 넣기
                    for (int i = 0; i < chat_items.size(); i++) {
                        ToMeChatData items = chat_items.get(i);
                        String local_time = convertUTCtoLocaltime(items.getMessage_sent_time());

                        // 다음메시지와 시간 비교
                        String showtimeLine = "2"; // 기본값 안추가
                        if (i + 1 < chat_items.size()) { // 아직 마지막 메시지가 아닌 경우 인덱스값+1 한 값이 사이즈 이상이 되면, 마지막인거임. 무조건 1개 작아야 다음 것이 있음.
                            String next_local_time = convertUTCtoLocaltime(chat_items.get(i+1).getMessage_sent_time()); // 다음 아이템의 시간
                            // 연도 월, 날짜만 비교 하기.
                            showtimeLine = year_date_compare(local_time, next_local_time);
                            Log.i("연도 월, 날짜만 비교!!", " showtimeLine: " + showtimeLine );
                        } else {
                            showtimeLine= "2";
                        }

                        if (items.getSender_id() == user_num_int) {
                            // 내가 보낸 메시지일 경우
                            tempChatItems.add(new ChattingContentData(
                                    items.getSender_id(),
                                    items.getMessage_id(),
                                    signin_user_nickname,
                                    null,
                                    items.getMessage_content(),
                                    local_time,
                                    items.getIs_read(),
                                    1,
                                    showtimeLine
                            ));
                            Log.i("채팅내용목록 리사이클러뷰", "items.getSender_id(): " + items.getSender_id() + " user_num_int: " + user_num_int + " 메시지 고유번호: " + items.getMessage_id() + ", 메시지내용: " + items.getMessage_content() + ", 메시지보낸시간: " + local_time + ", showTimeLine: " + showtimeLine);
                        } else if (this_usernum_int == items.getSender_id()) {
                            // 현재 채팅방의 상대가 보낸 메시지일 경우
                            tempChatItems.add(new ChattingContentData(
                                    items.getSender_id(),
                                    items.getMessage_id(),
                                    this_user_nickname,
                                    this_user_profileImg,
                                    items.getMessage_content(),
                                    local_time,
                                    items.getIs_read(),
                                    2,
                                    showtimeLine
                            ));
                            Log.i("채팅내용목록 리사이클러뷰", "items.getSender_id(): " + items.getSender_id() + " user_num_int: " + user_num_int + " 메시지 고유번호: " + items.getMessage_id() + ", 메시지내용: " + items.getMessage_content() + ", 메시지보낸시간: " + local_time + ", showTimeLine: " + showtimeLine);
                        } else {
                            Log.i("현재 채팅방에없는 사람이 보낸 메시지??", "가져온 데이터가 이상함");
                        }
                        Log.i("채팅내용목록 리사이클러뷰", "user_num_int: " + user_num_int + " 메시지 고유번호: " + items.getMessage_id() + ", 메시지내용: " + items.getMessage_content() + ", 메시지보낸시간: " + local_time);

                    } // for문 1. 끝

                    // for문 2. 시간선 여부 포함한 tempChatItems 을 리사이클러뷰에 넣기
                    for (ChattingContentData chatItem : tempChatItems) {
                        chatAdapter.addItem(chatItem);

                    }
                    isLoading = false;
                    scollToPosition(20);

                } else {
                    Log.i("채팅내용목록 chat_items null 임", " null");
                }


            } // onResponse 끝

            @Override
            public void onFailure(Call<ChattingResponse> call, Throwable throwable) {
                throwable.getMessage();
                Log.e("채팅방 페이징 http  통신 실패", "onFailure:실패한이유: " + throwable.getMessage());

            }
        });


    }



    // 스크롤 맨 밑으로 내리기
    private void scollToPosition(int positoin) {
        Log.d("scollToPosition 이벤트!", "포지션 이동하기 포지션값: " + positoin);
        if (chatting_content_recyclerview != null) {
            chatting_content_recyclerview.post(new Runnable() {
                @Override
                public void run() {
                    chatting_content_recyclerview.smoothScrollToPosition(positoin);
                }
            });
        }
    }


    // 메시지 전송 메소드 !! service 연결됨. ㄱㄱㄷ
    private void sendMessage(String message) {
        Intent intent = new Intent(ChattingViewActivity.this, SocketService.class);
        intent.putExtra("message", message);
        startService(intent);
        // 이미 서비스가 시작된 상태에서 send message 를 하게 되어 있다.
        // 이때 서비스를 다시 시작 하는 것이 아니라 on start command 가 호출 되면서 intent 처리를 하게 된다.
    }


    // 현재 현지 시간 포맷
    String getLocalTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }


    // UTC 시간 포맷
    // UTC란?  협정 세계시 (Coordinated Universal Time) 국제 표준 시간 기준. 1972년 1월 1일부터 사용 / 런던 기준 / 우리나라는 +9 (일본, 러시아, 인도네시아 공유)
    // GMT 기반이며 초의 소숫점 단위에서만 차이 나기 때문에 일상적으로는 혼용 되나, 기술적인 표기에서는 UTC 씀
    // GMT란? 기존에 사용하던 평균 태양시 기준의 그리니치 표준시 / 그리니치 천문대 기준임.
    // UTC랑 GMT 차이? 초의 소숫점 단위에서 차이남. 원자 시계 도입으로 시간 측정의 정확성이 향상돼서. 지구 자전의 불규칙성을 보완하고자 UTC로 변경.

    String getCurrentUTCTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date date = new Date();
        try {
            date = sdf.parse(sdf.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return outputFormat.format(date);
    }


    // UTC -> local time 현지 시간으로 변환
    String convertUTCtoLocaltime(String utcTime) {
        // Z는 UTC 시간을 나타냄. ISO 8601 표준에 의거.
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date date = inputFormat.parse(utcTime); // 가지고 온 utc time string값을 date 로 형변환

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            outputFormat.setTimeZone(TimeZone.getDefault()); // 현지 시간을 반환하도록 기존 UTC 데이트 폼을 변경 해줌.


            return outputFormat.format(date); // 포맷팅 다시 시킴.
        } catch (ParseException e) {
            e.printStackTrace();
            return utcTime;
        }

    }


    // 상대 유저 정보 및 채팅 목록 !!!  가지고 오기
    public void chatting_room_record(int login_user_number, int this_user_number) {
        Log.e("채팅방에서 상대유저, 채팅기록 가져오는 메소드", " 상대유저번호: " + this_user_number);
        service.chatting_room_record(login_user_number, this_user_number).enqueue(new Callback<ChattingResponse>() {
            @Override
            public void onResponse(Call<ChattingResponse> call, Response<ChattingResponse> response) {

                ChattingResponse result = response.body();
                Log.i("채팅방에서 상대유저, 채팅기록 가져오는 메소드", " reponse 진입함");
                Log.i("채팅방에서 상대 유저 정보 http 성공!!", " php 응답코드: " + result.getCode() + ", 메시지: " + result.getMessage());
                String room_number = result.getChat_room_number();
                roomId = room_number;


                // 현재 방번호 저장하기
                if (roomId != null) {
                    saveCurrentRoomId(roomId);
                } else {
                    Log.d("ChattingViewActivity", "roomId is null in onStart");
                }


                Log.i("채팅방에서 상대 유저 정보 http 성공!!", " 유저번호: " + result.getUser_number() + ", 유저 닉네임: " + result.getUser_nickname() + ", 유저 프사: " + result.getProfile_img() + ", 맞팔여부: " + result.getFollow_each_other() + ", 방번호: " + result.getChat_room_number());
                this_user_profileImg = result.getProfile_img();
                this_user_nickname = result.getUser_nickname();

                // 맞팔 여부
                int follow_each_other = result.getFollow_each_other();


//                EditText chat_input; // 현재 유저가 작성한 메시지
//                Button chatting_send_btn; // 채팅 입력후 전송 버튼 from me
                // 맞팔이 아닌 경우, edit text에 텍스트 띄우고, 전송버튼 비활성화. 버튼 글씨체 바꿔버령
                if (follow_each_other == 2) {
                    chat_input.setText("채팅이 불가능합니다. 맞팔로우 후 가능합니다.");
                    chat_input.setTextColor(R.color.search_text);
                    chat_input.setEnabled(false);
                    chatting_send_btn.setVisibility(View.GONE);
                }

                // 채팅 내용 목록 가져오기
                List<ToMeChatData> chat_items = result.getChat_items();

                String before_date = null;
                before_date = null;
                if (chat_items != null) {
                    for (ToMeChatData items : chat_items) {
                        Log.i("채팅내용목록", "메시지 고유번호: " + items.getMessage_id() + ", 방번호: " + items.getRooms_id() + ", 보낸사람번호: " + items.getSender_id() + ", 받은사람번호 : " + items.getReceiver_id() + ", 메시지내용: " + items.getMessage_content() + ", 메시지보낸시간: " + items.getMessage_sent_time() + ", 읽음여부: " + items.getIs_read());
                        String local_time = convertUTCtoLocaltime(items.getMessage_sent_time());
                        if (items.getSender_id() == user_num_int) {
                            // 내가 보낸 메시지일 경우 - 내 정보 로그인 유저 닉네임 없어도 됨. 안보낸다.
                            chatAdapter.addItem(new ChattingContentData(items.getSender_id(), items.getMessage_id(), signin_user_nickname, null, items.getMessage_content(), local_time, items.getIs_read(), 1, null));
                        } else if (items.getSender_id() == this_user_number) {
                            // 현재 채팅방의 상대가 보낸 메시지 일 경우
                            chatAdapter.addItem(new ChattingContentData(items.getSender_id(), items.getMessage_id(), result.getUser_nickname(), result.getProfile_img(), items.getMessage_content(), local_time, items.getIs_read(), 2, null));
                        } else {
                            Log.i("현재 채팅방에없는 사람이 보낸 메시지??", " 가져온 데이터가 이상함");
                        }
                        before_date = local_time;
                    }
                } else {
                    Log.i("채팅내용목록 chat_items null 임", " null");

                }


            }

            @Override
            public void onFailure(Call<ChattingResponse> call, Throwable throwable) {
                throwable.getMessage();
                Log.e("채팅방에서 상대 유저 정보 http  통신 실패", "onFailure:실패한이유: " + throwable.getMessage());
            }
        });

    }


    // 그룹 내의 모든 알림을 취소하는 메서드
    private void cancelGroupNotifications(Context context, String groupKey) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                if (groupKey.equals(notification.getNotification().getGroup())) {
                    notificationManager.cancel(notification.getId());
                }
            }
        }
    } // intent 로 넘어온 group key 가 있을 경우에만 (알림을 눌러서 채팅방에 들어온 경우 중... ) 알림 취소하는 메소드임 !!!
    // 아래의 경우 어디에서 오든 채팅방에 일단 진입하면 현재 방번호에 해당하는 노티피케이션 확인 후 삭제 .


    // 채팅방 관련 알림 전부 삭제

    private void cancelNotification(String charoomId) {

        String groupKey = GROUP_KEY_CHAT + charoomId;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 가져오기
        // NotificationManager 는 시스템 서비스를 통해서 알림을 관리하는 클래스이다.
        // 알림 생성, 업뎃, 취소 작업할때 노티 매니저를 사용해야한다. notificationManager 의 객체를 가져옴.

        // 안드로이드 system service
        // 운영체제가 동작하는 동안 수행할 수 있도록 구성된 어플리케이션 구성요소, 메시지 표시와 같이 시스템의 기본적인 기능들을 제공한다.
        // 안드로이드 그 자체임. 시스템 서비스는 시스템 권한이 있어야함. 프로그램 만드는 일반 안드 개발자들의 영역이 아님.


        // 그룹 키에 해당하는 모든 알림 취소
        for (StatusBarNotification notification : notificationManager.getActiveNotifications()) {
            if (groupKey.equals(notification.getNotification().getGroup())) {
                notificationManager.cancel(notification.getId()); // 1) 개별 알림 취소한다.
            }
        }

        // 그룹 요약 알림도 취소(일반적으로 그룹 키를 기반으로 ID를 사용합니다) !!
        notificationManager.cancel(groupKey.hashCode()); // 2) 그룹 알림 취소한다.

    } // cancelNotification


    // ==================================================== Activity Life Cycle ====================================================


    // 액티비티 생명주기 초기화
    // Acticity 생명주기 순서

    // On create -> On start -> On resume -> On Pause -> On Stop -> On Destroy

    // 브로드캐스트 리시버 수신
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // 결국 여기서도 받을때 type 확인 후, MESSAGE 일때, IS_READ 일때 ?
            String type = intent.getStringExtra("type");

            switch (type) {
                case "MESSAGE":
                    String message = intent.getStringExtra("message_content");
                    String message_time = intent.getStringExtra("message_time"); // utc time 임.
                    String sender_number = intent.getStringExtra("sender_number");
                    String sender_emailId = intent.getStringExtra("sender_emailId");
                    String sender_nickname = intent.getStringExtra("sender_nickname");
                    String receiver_number = intent.getStringExtra("receiver_number");
                    String receiver_emailId = intent.getStringExtra("receiver_emailId");
                    String receiver_nickname = intent.getStringExtra("receiver_nickname");

                    Log.d("broad cast 로 메시지 받아옴.", " 메시지 string값: " + message + ", 시간: " + message_time + ", 보낸사람번호: " + sender_number + ", 받은 사람번호(현재 로그인 번호랑 같아야함): " + receiver_number);

                    if (this_user_number.equals(sender_number)) {
                        String local_time = convertUTCtoLocaltime(message_time);
                        Log.d("전송 받은 메시지 시간 변경 ", " DB 시간 : " + message_time + ", 변경된 로컬시간 : " + local_time);

                        // ui 작업 해주기
                        chatAdapter.addItem_NEW(new ChattingContentData(this_usernum_int, 0, this_user_nickname, this_user_profileImg, message, local_time, 1, 2, null));

                        chatAdapter.notifyDataSetChanged(); // 어댑터에 데이터 변경 알림
                        scollToPosition(chatAdapter.getItemCount() - 1);

                        // 여기서 새로 추가한 아이템의 날짜와, 이전 마지막 아이템의 날짜와 비교해서 날짜 구분선추가해주는 로직 짜기 !!


                        // 여기서 읽음 처리해주기 위한 서버 전송 json 만들면 됨.
                        // 현재 시간, 즉 읽은 시간이랑 sender, receiver, type IS_READ 보내기.
                        // sender 는 상대, receiver 는 현재 로그인 유저임.
                        // 왜냐면 브로드캐스트로 왔다는 것은 서버로부터 "받은것" 상대로부터 온것이기 때문에
                        // sender 가 상대이고 리시버가 내 번호인 방의 메시지중에서
                        // 내가 읽은 시간 이전에 있는 is_read 칼럼을 읽음으로 바꾸는 로직을 수행하면 됨.

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("sender_number", sender_number);
                        jsonObject.addProperty("sender_emailId", sender_emailId);
                        jsonObject.addProperty("receiver_number", receiver_number); // 나
                        jsonObject.addProperty("receiver_emailId", receiver_emailId); // 내 이메일 아이디
                        jsonObject.addProperty("read_time", getCurrentUTCTime());
                        jsonObject.addProperty("type", "IS_READ");

                        Gson gson = new Gson();
                        String jsonString = gson.toJson(jsonObject);
                        Log.d("메시지 전송 이벤트! (읽음 처리를 위한 )", "gson으로 만든 json string값: " + jsonString);

                        sendMessage(jsonString); // service 로 연결 됨.
                    } else {
                        Log.d(" 내가 받는 메시지 아님 ", "gson으로 만든 json string값: ");
                    }

//                    chat_input.setText(""); // 이거 왜 하는 거임?
                    chat_input.setEnabled(true);
                    chatting_send_btn.setVisibility(View.VISIBLE);

                    // notification 있으면 삭제하기
                    cancelNotification(roomId);

                    break;

                case "IS_READ":
                    // 리사이클러뷰 다시 세팅해주기 ?
                    // 서버에서 데이터 베이스 고쳤기 때문에 !!
                    String read_time = intent.getStringExtra("read_time"); // utc time 임??


                    // sender 가 나임.
                    // 리사이클러뷰 세팅만 다시 해주면 됨.
                    // 서버에서 데이터 베이스 업데이트 후에 알려주려고 보낸거라서... ㄱㅊ

                    Log.d("메시지 읽음 처리!!", "is read의 real time : " + read_time);
                    // 리사이클러뷰에는 local 시간이잖아
                    String realToLocal = convertUTCtoLocaltime(read_time);
                    chatAdapter.read_update(realToLocal);
                    break;

                case "NOT_AVAILABLE":
                    // 메시지를 보낼 수 없음에 대한 toast message 띄우고
                    // edittext, button 비활성화 시켜주기
                    // 보낸 사람 스스로에게 돌아온 것임.
                    String alert_number = intent.getStringExtra("alert_number");
                    Log.d("맞팔아님", "alert_number : " + alert_number);

                    // 마지막에 보내진 메시지 삭제 ㅋㅋ 하기 리사이클러뷰에서..
                    Toast.makeText(ChattingViewActivity.this, "채팅이 불가능 합니다. 맞팔로우 후 가능합니다", Toast.LENGTH_LONG).show();
                    chat_input.setText("채팅이 불가능합니다. 맞팔로우 후 가능합니다.");
                    chat_input.setTextColor(R.color.search_text);
                    chat_input.setEnabled(false);
                    chatting_send_btn.setVisibility(View.GONE);

                    // 마지막으로 보낸 메시지 삭제
                    chatAdapter.removeLastItem();

                    break;
            }
        }
    };


    // =========================================== 생명 주기 재정의 시작 ==========================================

    @Override
    protected void onStart() {
        Log.d("onStart !! ", "onStart 들어옴 ");
        Log.d("생명주기확인 on start ", "현재방: " + roomId);
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver), new IntentFilter("NEW_MESSAGE"));
        IS_CHAT_ACTIVITY_RUNNING = true;
//        String is_chat_running;
//        if(IS_CHAT_ACTIVITY_RUNNING) {
//            is_chat_running = "true";
//        } else {
//            is_chat_running = "false";
//        }
//        Log.d("onStart !! ", " IS_CHAT_ACTIVITY_RUNNING 값: " + is_chat_running);


    }

    @Override
    protected void onResume() {
        Log.d("생명주기확인 onResume ", "현재방: " + roomId + ", sender number: " + this_user_number + ", sender email: " + this_user_emailID + ", receiver num: " + login_user_number + ", receiv ema: " + login_user_emailId);
        super.onResume();
        IS_CHAT_ACTIVITY_RUNNING = true;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("sender_number", this_user_number);
        jsonObject.addProperty("sender_emailId", this_user_emailID);
        jsonObject.addProperty("receiver_number", login_user_number); // 나
        jsonObject.addProperty("receiver_emailId", login_user_emailId); // 내 이메일 아이디
        jsonObject.addProperty("read_time", getCurrentUTCTime());
        jsonObject.addProperty("type", "IS_READ");


        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonObject);


        sendMessage(jsonString); // service 로 연결 됨.
        Log.d("(on resume )메시지 전송 이벤트! (읽음 처리를 위한 )", "gson으로 만든 json string값: " + jsonString);

        // 채팅방
        int offset = 0;

    }

    @Override
    protected void onPause() {
        Log.d("생명주기확인 onPause ", "현재방: " + roomId);
        super.onPause();
        IS_CHAT_ACTIVITY_RUNNING = false;
        // 방 정보 삭제
        clearCurrentRoomId(); // 채팅방에서 나갈 때 정보 삭제
    }

    // pause 와 stop 의 차이

    @Override
    protected void onStop() {
        Log.d(" onStop ", "현재방: " + roomId);
        super.onStop();
        // 브로드캐스트 리시버 해지
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

    }

    @Override
    protected void onDestroy() {
        Log.d("생명주기확인 onDestroy ", "현재방: " + roomId);
        super.onDestroy();
    }

    // 채팅방 -> Notification -> 채팅방으로 이동 하는 경우
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getIntent = intent;
    }


    // =========================================== 생명 주기 재정의 끝 ==========================================


    // 방 들어올때, 나갈때 방 아이디 저장 삭제하기

    private void saveCurrentRoomId(String roomId) {

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CURRENT_ROOM_ID, roomId);
        editor.apply(); // 변경 사항을 적용합니다.

        // 적용 후에 SharedPreferences에서 값을 조회합니다.
        String current_roomId = prefs.getString(CURRENT_ROOM_ID, "");
        Log.d("saveCurrentRoomId방저장 !! ", " 현재방: " + current_roomId);

    }

    private void clearCurrentRoomId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(CURRENT_ROOM_ID);
        editor.apply();
        String current_roomId = prefs.getString(CURRENT_ROOM_ID, "");
        Log.d("saveCurrentRoomId방저장 -삭제!!", " 삭제된 현재방: " + current_roomId);
    }


    // 로컬 시간 중에서 연도, 년, 월만 비교 하기.
    private String year_date_compare(String local, String next_local) {
        Log.d("syear_date_compare", " local: " + local + ", next: " + next_local);
        String local_yeardate = local.substring(0, 10);
        String next_yeardate = next_local.substring(0, 10);
        Log.d("syear_date_compare", " local_yeardate: " + local_yeardate + ", next_yeardate: " + next_yeardate);
        return local_yeardate.equals(next_yeardate) ? "2" : "1";
    }


} // chatting view activity 끝

