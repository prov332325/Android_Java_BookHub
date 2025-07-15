package com.example.myapplication.layout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.CellIdentityLte;
import android.util.Log;
import android.view.MenuItem;

import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.layout.bottomnavi.FragmentChatting;
import com.example.myapplication.layout.bottomnavi.FragmentBoard;
import com.example.myapplication.R;
import com.example.myapplication.layout.bottomnavi.FragmentMyLibrary;
import com.example.myapplication.layout.bottomnavi.FragmentHome;
import com.example.myapplication.layout.bottomnavi.FragmentMyprofile;
import com.example.myapplication.socket.SocketService;
import com.google.android.material.navigation.NavigationBarView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    // bottom navi - 프래그먼트
    FragmentBoard fragmentBoard;
    FragmentMyprofile fragmentMyprofile;
    FragmentHome fragmentHome;
    FragmentChatting fragmentChatting;
    FragmentMyLibrary fragmentMyLibrary;


    // import
    RetrofitService service; // 레트로핏 서비스
    UserViewModel userViewModel;


    // string
    String user_emailid; // 유저 이메일 아이티
    String user_number; // 유저 넘버



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --------------- 서비스 실행중인지 확인 --------------------------
        if (isServiceRunning(SocketService.class)) {
            Log.d("MainActivity 서비스확인", "서비스 실행중임 !! ");
            // 추가 작업 처리
        } else {
            Log.d("MainActivity 서비스확인", "서비스 실행중 아님 !! ");
            // 서비스가 실행 중이 아니면 필요한 작업 처리
        }
        // --------------- 서비스 실행중인지 확인 --------------------------


        // ======================= 초기화 zone 시작 =================================
        // 레트로핏 객체 생성
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의


        // 뷰 모델
        // view model  객체 생성.
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // 네비게이션 바
        NavigationBarView navigationBarView = findViewById(R.id.bottomNavigationView);

        // 프래그먼트
        fragmentBoard = new FragmentBoard();
        fragmentMyprofile = new FragmentMyprofile();
        fragmentHome = new FragmentHome();
        fragmentChatting = new FragmentChatting();
        fragmentMyLibrary = new FragmentMyLibrary();


        // 인텐트로 넘어온 유저 email id
        Intent intent = getIntent();
        user_emailid = intent.getStringExtra("user_emailid");
        get_userNumber(user_emailid);




        // ======================= 초기화 zone 끝 =================================

        // c0. 상단 툴바 메인으로 이동하기.
        // 네비게이션에 있는 아이콘 클릭시 이동함. 아이콘 없으면 클릭 대상이 없으므로 이동할 수 없음.
//        toolbar.setNavigationOnClickListener( v -> {
//            // 홈 아이콘 클릭했을때 메인으로 오기.
//            Intent intent = new Intent(MainActivity.this, MainActivity.class);
//            startActivity(intent);
//        });

        // c1. fragment home이 메인이 되도록

        // intent 로 받아온 게 있는 경우 !!! 분기 처리 해주기 !!!
        if (intent != null && "myLibrary".equals(intent.getStringExtra("fragment"))) {
            // 내 서재 프래그먼트로 이동
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragmentMyLibrary).commit();
            navigationBarView.setSelectedItemId(R.id.mylabrary);
        } else if (intent != null && "board".equals(intent.getStringExtra("fragment"))) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragmentBoard).commit();
            navigationBarView.setSelectedItemId(R.id.board);
        } else if (intent != null && "myProfile".equals(intent.getStringExtra("fragment"))) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragmentMyprofile).commit();
            navigationBarView.setSelectedItemId(R.id.myprofile);
        } else if (intent != null && "chatting".equals(intent.getStringExtra("fragment"))) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragmentChatting).commit();
            navigationBarView.setSelectedItemId(R.id.chat);
        }

        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragmentHome).commit();
            navigationBarView.setSelectedItemId(R.id.home);
        }


        // c2. 네비게이션 바
        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragmentHome).commit();
                  // navigationBarView.setSelectedItemId(R.id.home);
                    return true;
                } else if (id == R.id.mylabrary) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragmentMyLibrary).commit();
                  //  navigationBarView.setSelectedItemId(R.id.mylabrary);
                    return true;
                } else if (id == R.id.board) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragmentBoard).commit();
                 //   navigationBarView.setSelectedItemId(id);
                    return true;
                } else if (id == R.id.chat) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragmentChatting).commit();
                   // navigationBarView.setSelectedItemId(R.id.chat);
                    return true;
                } else if (id == R.id.myprofile) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragmentMyprofile).commit();
                  //  navigationBarView.setSelectedItemId(R.id.myprofile);
                    return true;
                }
                return false;
            }
        });



    } // on create


    // 서비스 실행 확인하는 메소드
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    // 유저 넘버 가져오기.
    public void get_userNumber (String user_emailid) {
        Log.i("메인액티비티에서 유저넘버가져오기", "유저의 emailId: " + user_emailid);
        service.user_emailId(user_emailid).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("메인-유저 number 체크 response 진입", "onResponse: 응답메시지: " + result.getMessage()); // echo로 보내주는 메시지.
                Log.e("메인-유저 number 체크 response 진입", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.

                if(result.getCode() == 200 ) {
                    Log.e("유저 number 체크성공 / code==200!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                    user_number = result.getUser_number();
                    // view model - userNumber 값 설정
                    userViewModel.setUserNumber(user_number);

                } else {
                    Log.e("php에서 뭔가 잘못됨. / code==400!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Log.e("메인-유저넘버찾기 통신 failed", "실패원인: " +throwable.getMessage());
            }
        });
    }




}