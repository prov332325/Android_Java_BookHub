package com.example.myapplication.layout.bottomnavi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.recyclerview.ChattingListAdapter;
import com.example.myapplication.data.recyclerview.ChattingListData;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.ChattingResponse;
import com.example.myapplication.data.retrofit.responsemodel.ChattingRoomResponse;
import com.example.myapplication.layout.MainActivity;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentChatting extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    // 안전하게 context 사용하기
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }



    // import
    private NavigationBarView bottomNavigationView; // 바텀 네비게이션 인스턴스 변수
    RetrofitService service; // 레트로핏 서비스

    RecyclerView chatting_list_recyclerview;  // 리사이클러뷰
    ChattingListAdapter chattingListAdapter;  // 리사이클러뷰 어댑터

    SharedPreferences sharedPreferences; // 쉐어드 초기화
    PreferenceManager pref;  // 쉐어드 매니저 초기화


    // 채팅방 목록
    List<ChattingRoomResponse> chatRoomItems = null;


    // view
    Toolbar toolbar;


    // 쉐어드 string
    String user_number; // 쉐어드에서 가져온 이메일 아이디로, 서버에서 가져온 user number !!
    String key = "signin_email_id"; // 쉐어드 키
    String signin_email_id_value ; // 쉐어드 값 (이메일아이디, 닉네임 json string 값 - 파싱 전)




    // string
    String other_user_profile_img;
    String other_user_nickname;

    String img;

    public FragmentChatting(){};

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordFragment.
     */
    // TODO: Rename and change types and number of parameters


    public static FragmentChatting newInstance(String param1, String param2) {
        FragmentChatting fragmentChatting = new FragmentChatting();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragmentChatting.setArguments(args);
        return fragmentChatting;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saveInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.bmnavi_chatting, container, false);

        // ======================================== 초기화 시작 ===========================================
        // 상단 툴바
        toolbar = rootView.findViewById(R.id.toolbar);

        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의


        //  액티비티에서 바텀 네비게이션 가져오기
        if (getActivity() instanceof MainActivity) {  // MainActivity로부터 가져오기 // 이 과정이 꼭 필요함 !!!
            // 내가 지금 활동하는 액티비티가 올바른 액티비티인지 꼭 확인 !!
            MainActivity mainActivity = (MainActivity) getActivity();
            bottomNavigationView = mainActivity.findViewById(R.id.bottomNavigationView);
        }


        // 쉐어드 관련 클래스 및 값 초기화
        pref = new PreferenceManager();
        sharedPreferences = getActivity().getSharedPreferences("session_contain", Context.MODE_PRIVATE);
        signin_email_id_value = sharedPreferences.getString(key, "");


        // 리사이클러뷰 세팅 준비 !! view 연결 시키기
        chatting_list_recyclerview = rootView.findViewById(R.id.chatting_list);
        chattingListAdapter = new ChattingListAdapter(context); // 리사이클러뷰 어댑터
        chatting_list_recyclerview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)); // 리니어 레이아웃 매니저
        chatting_list_recyclerview.setAdapter(chattingListAdapter); // 어댑터 세팅하기

        // ======================================== 초기화 끝 ===========================================


        // on create 1. 툴바 홈 이모티콘 클릭 시, 홈으로 이동하기.
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentHome fragmentHome = FragmentHome.newInstance("param1", "param2");
                ((MainActivity) getActivity()).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_layout, fragmentHome)
                        .addToBackStack(null)
                        .commit();
                // 홈버튼 클릭시 홈 아이콘으로 setting
                bottomNavigationView.setSelectedItemId(R.id.home);
            }
        });

        return rootView;
    } // on create view 끝


    // method1. 유저 번호와, 현재 유저가 속한 채팅방 가져오기 가져오기
    public void get_chatting_list (String user_emailid) {
        Log.d("채팅방 목록가져오는메소드 진입", "get_chatting_list : 진입!! 현재 접속 유저 이메일아이디: " + user_emailid);

        service.get_chatting_list(user_emailid).enqueue(new Callback<ChattingRoomResponse>() {
            @Override
            public void onResponse(Call<ChattingRoomResponse> call, Response<ChattingRoomResponse> response) {
                Log.d("채팅방 목록 가져오기 성공 !! ", "onResponse : 진입!!");
                ChattingRoomResponse result = response.body();
                Log.e("채팅방 목록 가져오기 response 진입", "onResponse: 응답메시지: " + result.getMessage()); // echo로 보내주는 메시지.
                Log.e("채팅방 목록 가져오기 response 진입", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.


                // 채팅방 아이템 가져오기
                chatRoomItems = result.getChat_room_items();
                // 리사이클러뷰 한번 초기화 해주기
                chattingListAdapter.clearItem();

                if(chatRoomItems !=null) {
                    for(ChattingRoomResponse item : chatRoomItems) {
                        Log.e("채팅방 목록!! 가져오는 for문", "방번호: " + item.getChat_room_number() + "안읽은 메시지 개수: " + item.getUnread_cnt()  + ", 상대번호(string): " + item.getUser_number()+ ", 상대닉넴: " + item.getUser_nickname() + ", 마지막메시지: " + item.getLast_sent_message() + ", 마지막메시지 시간: " + item.getLast_sent_time() + ", 현재유저=나의번호: " + result.getMy_user_number() + ", 맞팔여부: " + item.getFollow_each_other());

                        String local_time= null;
                        int roomNumber = Integer.parseInt(item.getChat_room_number());
                        int my_user_number = Integer.parseInt(result.getMy_user_number());
                        int other_user_number = Integer.parseInt(item.getUser_number());
                        String other_user_emailId = item.getUser_emailId();
                        String other_user_profile_img = item.getProfile_img();
                        String other_user_nickname = item.getUser_nickname();
                        String last_message = item.getLast_sent_message();
                        String last_sent_time = item.getLast_sent_time();
                        int unread_msg_cnt = Integer.parseInt(item.getUnread_cnt());
                        int follow_eachOther = item.getFollow_each_other();
                        Log.e("채팅방 목록!! 가져오는 for문", "follow_eachOther: " +  item.getFollow_each_other());

                        local_time = convertUTCtoLocaltime(last_sent_time);
                        chattingListAdapter.addItem(new ChattingListData(roomNumber, my_user_number,user_emailid,other_user_number, other_user_emailId, other_user_profile_img, other_user_nickname, last_message, local_time, unread_msg_cnt, follow_eachOther));

                    } // for 문 끝
                    chattingListAdapter.notifyDataSetChanged(); // 어댑터에 데이터 변경 알림
                } else {

                }


            }

            @Override
            public void onFailure(Call<ChattingRoomResponse> call, Throwable throwable) {
                Log.e("채팅 목록 가져오기 통신 failed", "실패원인: " +throwable.getMessage());
            }
        });


    }

    public void chatting_list_update(){
        if(!signin_email_id_value.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(signin_email_id_value);
                String emailId = jsonObject.optString("emailid", ""); // default 값 설정 가능
                String nickname = jsonObject.optString("nickname", ""); // nullpoint exception 발생할 수 있으므로, 빈문자열로 두기
                get_chatting_list(emailId);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("채팅 프래그먼트 쉐어드비었음", "쉐어드왜비었죠? : 진짜 비었나? >>>  " + signin_email_id_value);
//            Toast.makeText(getApplicationContext(), " 저장 액티비티 진입 실패. 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
//            finish();
        }

    }

    // UTC -> local time 현지 시간으로 변환
    String convertUTCtoLocaltime (String utcTime) {
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


    // method 2. 브로드캐스트 리시버 수신
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // 서버에서 보내줄 때 type 확인하고 처리하기
            String type = intent.getStringExtra("type");

            switch (type) {
                case "MESSAGE" :
                    // ui 작업 해주기 ..
                    // 방번호,상대 유저 닉넴, 프사랑 가져오기
                    // 마지막 보낸 톡 내용은 어케하지? ㅠ 아니다 !! 필요 업승ㅁ
                    // 받은 메시지 하나니까 부조건 한개 !!!
                    // 방 번호만 있으면 된다 !! 받아온 시간이랑 문자 띄우면 됨.

                    // 안 읽은 숫자를 어디서 가져와야 하지 ??
                    // 메시지 받은 채팅방 있으면??
                    // 그 채팅방을 목록에서 찾아서 remove -> add 0번에 해주기.
                    String roomId = intent.getStringExtra("roomId");
                    String sender_number = intent.getStringExtra("sender_number");
                    String sender_emailId = intent.getStringExtra("sender_emailId");
                    String sender_nickname = intent.getStringExtra("sender_nickname");
                    String receiver_number = intent.getStringExtra("receiver_number");
                    String receiver_emailId = intent.getStringExtra("receiver_emailId");
                    String receiver_nickname = intent.getStringExtra("receiver_nickname");
                    String message_content = intent.getStringExtra("message_content");
                    String local_time= null;
                    String message_time = intent.getStringExtra("message_time");
                    local_time = convertUTCtoLocaltime(message_time);

                    int room_number_int = Integer.parseInt(roomId);
                    int sender_number_int = Integer.parseInt(sender_number);
                    int receiver_number_int = Integer.parseInt(receiver_number);

                    // 만약 리사이클러뷰에서 내용 그대로 get item 해서 그 아이템 정보를 가져올 수있으면
                    // 거기서 상대 프사, 등등 가져오고
                    // 그게 안되면 레트로핏 가야함

                    Log.d("채팅목록, 서버로부터 메시지받음", "메시지온방번호: " + room_number_int + ", 보낸사람: " + sender_number_int + "chat room items 상태 : "  );

                    Log.d("채팅목록", " chatRoomItems.size(): "+ chatRoomItems.size() + ",chattingListAdapter.getItemCount(): " + chattingListAdapter.getItemCount()  );

                    // 리사이클러뷰 아이템 for 문으로 돌리면서 일치하는 방 찾기

                    if (chatRoomItems!=null && chatRoomItems.size() > 0) {
                        Log.d("기존에 채팅 목록이 있음 ", "채팅목록 있음. 지금 받은 메시지 이외의 채팅이 존재 한다는 뜻, chatRoomItems.size(): "+ chatRoomItems.size()   );

                        boolean need_new = true;

                        for (int i=0; i <chatRoomItems.size(); i++) {
                            Log.d("새로 생긴 채팅방 찾는중 ", "현재 채팅중인 방개수만큼 출력됩니다 => 현재 몇번째 턴인지 : " + i + "번째"  );
                            if (room_number_int == chattingListAdapter.getItem(i).getRoom_number()) {
                                need_new = false;
                                // 기존에 채팅방이랑 일치하면 그 채팅방을 최상단에 올려주고, 안읽은 메시지 개수 업데이트 해주면 됨!!!

                                // 일치 하는 아이템에서 필요한 정보 가져오기
                                other_user_profile_img = chattingListAdapter.getItem(i).getOther_user_profile_img();
                                other_user_nickname = chattingListAdapter.getItem(i).getOther_user_nickname();

                                // 안 읽은 메시지 개수 늘려주기

                                int unread_msg_cnt = chattingListAdapter.getItem(i).getUnread_msg_cnt();

                                // 일치 하는 아이템 삭제
                                chattingListAdapter.deleteItem(i);

                                // 새로운 아이템 추가
                                chattingListAdapter.addItem(0, new ChattingListData(room_number_int,receiver_number_int,receiver_emailId,sender_number_int, sender_emailId,other_user_profile_img,other_user_nickname, message_content, local_time,unread_msg_cnt+1, 1)); // 새로운 정보와 함께 0번으로 이동시키기 // 메시지가 왔다는것도 보내는 쪽에서 맞팔 임을 확인했기 때문에 일단 1로 넣음..
                                return; // 여기서 return 이 뭐지 ?
                                // 매칭이 되는 방을 찾고 처리한 후에 일찍 종료하는 로직임.
                            } else {
                                Log.d("매칭되는방 아님.", "현재 채팅중인 방개수만큼 출력됩니다 => 현재 몇번째 턴인지 : " + i + "번째"  );
                                // 여기
                            }
                        } // for 문 끝


                        // for 문이 끝날 때까지 boolean 값이 바뀌지 않으면,
                        if(need_new) {
                            other_profile_img(sender_number_int, room_number_int, receiver_number_int, receiver_emailId, sender_emailId, sender_nickname, message_content,local_time);
                        }

                    } else {
                        // 만약 리스트에 해당 방이 없으면 새로 추가 (예: 새로운 채팅방 생성)

                        // 서버에서 이미지 가져오기 ....
                        other_profile_img(sender_number_int, room_number_int, receiver_number_int, receiver_emailId, sender_emailId, sender_nickname, message_content,local_time);
//                         상대 유저번호, 방번호int, 받는이번호int, 받는이이메일아이디,보내는이 이메일아이디, 프사, 보내는이 닉, 메시지, 메시지 시간, 1

//                         이미지 가져와서 넣어주기
//                        ChattingListData newItem = new ChattingListData(room_number_int, receiver_number_int, receiver_emailId, sender_number_int, sender_emailId, img, sender_nickname, message_content, message_time, 1);
//                        chattingListAdapter.addItem(0, newItem);
//                        chattingListAdapter.notifyDataSetChanged();


                        // 채팅방 생성하는 코드가 없네요

                        break;
                    }





            }

        }
    };


    // ========================================================== Activity 생명주기  ==========================================================



    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(context).registerReceiver((mMessageReceiver), new IntentFilter("NEW_MESSAGE"));

    }

    @Override
    public void onResume() { // resume 은 해당 화면이 백그라운드로 빠졌다가 다시 포그라운드로 돌아오면
        //시작된다. 그니까 다른곳 갔다가 다시 돌아오는 back 의 경우 !
        // on create 다시 진입하지 않고 on resume 으로 들어옴.
        super.onResume();
        chatting_list_update(); // 채팅방 목록 로딩하는 메소드
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onStop() {
        super.onStop();
        // 브로드캐스트 리시버 해지
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    // 상대 유저 이미지 가져오는 레트로핏,, ㅎ

    public void other_profile_img (int sender_number,int room_number_int, int receiver_number_int, String receiver_emailId, String sender_emailId, String sender_nickname, String message_content, String message_time) {
        // 상대 유저번호, 방번호int, 받는이번호int, 받는이이메일아이디,보내는이 이메일아이디, 프사, 보내는이 닉, 메시지, 메시지 시간, 1
        service.get_user_img(sender_number).enqueue(new Callback<ChattingRoomResponse>() {
            @Override
            public void onResponse(Call<ChattingRoomResponse> call, Response<ChattingRoomResponse> response) {
                Log.e("상대 유저 이미지 가져오기 성공 !! ", "code: " + response.code()); //서버가 보내는 http 통신 응답코드

                ChattingRoomResponse result = response.body();
                Log.e("상대프사 php 메시지", "message: " + result.getMessage()); //서버가 보내는 http 통신 응답코드
                String img = result.getProfile_img();

                if (img !=null) {
                    Log.e("상대유저프사있음 ", "img: " + img);
                    img = result.getProfile_img();
                } else {
                    Log.e("상대유저프사없음 ", "img: " + img);
                    img = null;
                }

                // 이미지 가져와서 넣어주기
                ChattingListData newItem = new ChattingListData(room_number_int, receiver_number_int, receiver_emailId, sender_number, sender_emailId, img, sender_nickname, message_content, message_time, 1, 1); // 이 경우 무조건 서로 맞팔이 된 상태이기 때문이기에.. 1을 넣어둠.
                chattingListAdapter.addItem(0, newItem);
                chattingListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Call<ChattingRoomResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e(" 상대 유저 이미지 가져오는 레트로핏 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("다른 유저 팔로우 실패. ", "onFailure: " + throwable.getCause());
            }
        });

    }


} // 채팅 프래그먼트
