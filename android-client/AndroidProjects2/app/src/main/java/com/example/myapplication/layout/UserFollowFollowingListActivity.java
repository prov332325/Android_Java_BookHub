package com.example.myapplication.layout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.recyclerview.FollowListAdapter;
import com.example.myapplication.data.recyclerview.FollowingListData;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.ProfileFollowResponse;
import com.example.myapplication.data.retrofit.responsemodel.ProfileViewResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserFollowFollowingListActivity extends AppCompatActivity {



    // import
    RetrofitService service; // 레트로핏 서비스



    // 내 팔로워 팔로잉 뿐 아니라 다른 사람의 목록도 볼 수 있기 때문에
    // 쉐어드로 작업하지 말기. 유저 넘버로만.


    // 리사이클러뷰 - http 통신 보낼때에도 type 보내서 쿼리문 다르게 실행되도록 분기처리해주기
    // 어댑터
    // 어댑터 리스트

    RecyclerView follower_following_list_recyclerview; // 리사이클러뷰
    FollowListAdapter followListAdapter; // 리사이클러뷰 어댑터



    // view
    Toolbar toolbar; // 툴바
    TextView following_or_follow; // 팔로잉 or 팔로우



    // String
//    String type;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_follow_following_list);
        Log.d("생명주기확인 onCreate !! ", "on create 들어옴 ");


        // ======================= 뷰 초기화 zone 시작 =================================

        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의

        // view 연결
        toolbar = findViewById(R.id.toolbar);
        following_or_follow = findViewById(R.id.follow_or_following);

        // 리사이클러뷰
        follower_following_list_recyclerview = findViewById(R.id.follow_or_following_list_recyclerview);
        followListAdapter = new FollowListAdapter(UserFollowFollowingListActivity.this); // 어댑터
        follower_following_list_recyclerview.setLayoutManager(new LinearLayoutManager(UserFollowFollowingListActivity.this, RecyclerView.VERTICAL, false)); // 레이아웃 매니저
        follower_following_list_recyclerview.setAdapter(followListAdapter);


        // ======================= 뷰 초기화 zone 끝 =================================

        // on create 1. 툴바 뒤로가기 클릭 시, 이전 화면으로
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            } });


        // 내 프로필에서 올 수도 있지만 other profile 에서도 접근 가능함 !!! 접근 시 intent 잘 넘겨주도록
        // on create 2. intent 로 타입 받아서 현재 팔로잉 목록인지 팔로우 목록인지 확인하고
        // 목록 리스트 가져오는 레트로핏 실행하기 -> 레트로핏에서 리사이클러뷰 세팅하기.
        // 각각 아이템들에 대해, 상대 프로필로 이동하게 연결해주기. !! 4시까지 파이팅



    } // on create 끝


    // method 1. 팔로워 or 팔로잉 목록 가져 오기 - 리사이클러뷰에 추가 하기.
    // 페이징 할때에는 바닥에 닿으면 5개씩 더 가져오도록 하기.

    public void follower_following_list (int user_number, String type) {
        Log.d("팔로워팔로잉메소드 진입", "현재유저번호: " + user_number + ", 타입: " + type);

        service.follower_following_list(user_number, type).enqueue(new Callback<ProfileViewResponse.ProfileViewResponse2>() {
            @Override
            public void onResponse(Call<ProfileViewResponse.ProfileViewResponse2> call, Response<ProfileViewResponse.ProfileViewResponse2> response) {
                ProfileViewResponse.ProfileViewResponse2 result = response.body();

                // response 2 에 있는 getter 사용해서 반복문으로 팔로워 팔로잉 유저 목록 출력하기.
                List<ProfileViewResponse> user_item = result.getUser_list();

                if(result.getMessage().equals("목록이비었습니다")) {
                    Log.e("팔로워 팔로잉 유저 목록 없음", " reponse 진입함" );
                    // 리사이클러뷰 어댑터 클리어 해주기
                    followListAdapter.clearItem();
                } else {
                    // 리사이클러뷰 한번 클리어해주기

                    // 리사이클러뷰 어댑터 클리어 해주기
                    followListAdapter.clearItem();
                    for (ProfileViewResponse item : user_item) {
                        // 가져온 데이터를 item.get 으로 가져와서 리사이클러뷰에 넣어주깅
                        Log.i("팔로팔로잉유저목록", " 유저번호: " + item.getUser_number() + ", 유저닉네임: " + item.getUser_nickname() + ", 유저프사 : " + item.getProfile_img() + ", 팔로우여부: "+ item.getFollow_status_now());
                        int user_number = item.getUser_number();
                        String user_emailId = item.getUser_emailId();
                        String user_nickname = item.getUser_nickname();
                        String user_profileImg = item.getProfile_img();
                        String following_status = item.getFollow_status_now();
                        followListAdapter.addItem(new FollowingListData(user_number, user_emailId, user_nickname, user_profileImg, following_status));
                    } // for문 끝
                    followListAdapter.notifyDataSetChanged();
                }
            } // on response 끝

            @Override
            public void onFailure(Call<ProfileViewResponse.ProfileViewResponse2> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("팔로워,팔로잉 목록 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("팔로워,팔로잉 목록 실패. ", "onFailure: " + throwable.getCause());

            }
        });


    } // follower_following_list 끝


    @Override
    protected void onResume() {
        Intent getIntent = getIntent();
        String type = getIntent.getStringExtra("type");
        int this_user_number = Integer.parseInt(getIntent.getStringExtra("user_number"));

        Log.d("넘어온목록타입!!", "유저번호 " + this_user_number + ", 타입: " + type ); // 레트로핏으로 가져온 유저 넘버.

        if(type.equals("follower")) {
            // 팔로워 목록 가져 오기
            following_or_follow.setText("팔로워");
            type= "follower";
            // 레트로핏 넘기기.
        } else if (type.equals("following")) {
            // 팔로잉 목록 가져 오기
            following_or_follow.setText("팔로잉");
            type= "following";
        }

        // 팔로워, 팔로잉 목록 가져오기
        follower_following_list(this_user_number, type);

        super.onResume();
    }
}
