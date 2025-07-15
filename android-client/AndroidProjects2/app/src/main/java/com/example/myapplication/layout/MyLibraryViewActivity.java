package com.example.myapplication.layout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.UserNumberCallback;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.data.retrofit.responsemodel.MyLibraryListResponse;
import com.example.myapplication.data.retrofit.responsemodel.MylibraryResponse;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyLibraryViewActivity extends AppCompatActivity implements UserNumberCallback {


    // import
    RetrofitService service; // 레트로핏 서비스
     // AlertDialog alertDialog; // 삭제 컨펌 알람 띄우기.


    // 쉐어드 프리퍼런스
    PreferenceManager pref; // 쉐어드 프리퍼런스 - 로그인 유지용
    String key = "signin_email_id";
    String current_login_memberInfo; // 쉐어드에 저장된 이메일/아이디, 닉네임 키값쌍.
    String emailId ; // 이메일/아이디만 파싱한 값.



    // view
    Toolbar toolbar; // 툴바

    // 내 서재 상세보기 뷰 초기화 !!!

    // 공통

    LinearLayout already_layout, reading_layout, want_layout;
    Button modify_btn, delete_btn; // 수정, 삭제 버튼

    TextView view_title, view_author, view_type;
    // 책 제목, 작가, 내 서재 타입, 책 소개, 출판사, isbn

    ImageView view_cover; // 책 커버 사진


    // 읽은 책
    TextView already_howlong, already_started, already_finished;
    // 읽은 날짜(기간_일수), 독서 시작일, 독서 종료일

    TextView already_view_bookDescription, already_view_bookPublish, already_view_bookIsbn;
   // 읽은 책의 책 소개, 출판사, isbn

    RatingBar already_ratingstar;


    // 읽고 있는 책

    TextView reading_been, reading_started, reading_page_cnt;
    // 읽고 있는 책 (기간-일수), 시작일, 읽은 페이지

    TextView reading_view_bookDescription, reading_view_bookPublish, reading_view_bookIsbn;
    // 읽고 있는 책의 책 소개, 출판사, isbn



    // 읽고 싶은 책

    RatingBar want_rating;
    TextView want_preview;
    TextView want_view_bookDescription, want_view_bookPublish, want_view_bookIsbn;
    // 읽고 싶은 책의 책 소개, 출판사, isbn




    // string
    String user_number ; // 현재 로그인 중인 유저 넘버.
    String lib_user_numberString; // 서재 글쓴이

    // 현재 게시글에 대한 정보 intent
    int book_number, mylibrary_number; // 책번호, 내 서재 번호.
    String type; // 게시글 타입 1: 읽은책, 2: 읽고 있는책, 3: 읽고 싶은 책




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mylibrary_view);


        // ======================= 초기화 zone 시작 =================================

        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의


        // 툴바
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }


        // 모든 뷰에 대한 초기화 find

        // 공통 정보 - 버튼, 책 정보, 레이아웃
        modify_btn = findViewById(R.id.mylibrary_modify); // 수정
        delete_btn = findViewById(R.id.mylibrary_delete); // 삭제

        view_title  = findViewById(R.id.mylibrary_title); // 책 제목
        view_cover = findViewById(R.id.mylibrary_cover); // 책 커버 사진
        view_author = findViewById(R.id.mylibrary_author); // 책 작가
        view_type = findViewById(R.id.mylibrary_type); // 내 서재 타입

        // 레이아웃
        already_layout = findViewById(R.id.mylibrary_view_already);
        reading_layout = findViewById(R.id.mylibrary_view_reading);
        want_layout = findViewById(R.id.mylibrary_view_want);


        // 읽은 책
        already_howlong = findViewById(R.id.already_howlong); // 읽은 기간 세팅, "~일 동안 읽었어요"
        already_started = findViewById(R.id.already_started); // 시작했던 날짜: 2024.08.10
        already_finished = findViewById(R.id.already_finished); // 다 읽은 날짜: 2024.10.11
        already_view_bookDescription = findViewById(R.id.already_description);
        already_view_bookPublish = findViewById(R.id.already_publish);
        already_view_bookIsbn = findViewById(R.id.already_isbn);
        already_ratingstar = findViewById(R.id.already_ratingstar);


        // 읽고 있는 책
        reading_been = findViewById(R.id.reading_been);
        reading_started = findViewById(R.id.reading_started);
        reading_page_cnt = findViewById(R.id.reading_pageCnt);
        reading_view_bookDescription = findViewById(R.id.reading_description);
        reading_view_bookPublish = findViewById(R.id.reading_publish);
        reading_view_bookIsbn = findViewById(R.id.reading_isbn);


        // 읽고 싶은 책
        want_rating = findViewById(R.id.want_ratingstar);
        want_preview = findViewById(R.id.want_preview);

        want_view_bookDescription = findViewById(R.id.want_description);
        want_view_bookPublish = findViewById(R.id.want_publish);
        want_view_bookIsbn = findViewById(R.id.want_isbn);





        // on create 1. 툴바 뒤로 가기 클릭시, 이전 화면으로 (library fragment)
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // 뒤로 가기 동작 수행
            }
        }); // bs1 끝


        // on create 2. 쉐어드에서 사용자 이메일 아이디 가져오기
        pref = new PreferenceManager();
        current_login_memberInfo =   pref.getString(this, key);
        Log.d("내 서재 상세보기", "로그인 회원정보from 쉐어드:  " + current_login_memberInfo);

        try {
            JSONObject jsonObject = new JSONObject(current_login_memberInfo);
            emailId = jsonObject.optString("emailid", "");
            Log.d("내 서재 상세보기", "로그인 회원 이메일: " + emailId);
            get_userNumber(emailId, this); // 유저 번호 가져오기.

        } catch (JSONException e) {
            e.printStackTrace();
            emailId = ""; // 기본값 설정
        }

        // on create 3. 인텐트로 넘어온 값 확인하기.
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        String bookNumberString = intent.getStringExtra("book_number");
        String mylibraryNumberString = intent.getStringExtra("mylibrary_number");
        lib_user_numberString = intent.getStringExtra("lib_user_number");



        if (bookNumberString != null && mylibraryNumberString != null) {
            book_number = Integer.parseInt(bookNumberString);
            mylibrary_number = Integer.parseInt(mylibraryNumberString);
            Log.d("내 서재 상세보기", "책번호:  " + book_number + ", 내서재 번호: " + mylibrary_number);
        } else {
            Log.e("내 서재 상세보기", "책번호나 내서재 번호가 null 입니다.");
        }

        // on create 4. 내 서재 게시글 삭제하기
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("내 서재 삭제 버튼 클릭 on click진입", "유저넘버string:  " + user_number + ", 게시글번호int: "+ mylibrary_number + ", 책번호int: " + book_number + ", 타입string: " + type);

             // 정말 삭제하시겠습니까 ? 물어보깅.
                AlertDialog.Builder builder = new AlertDialog.Builder(MyLibraryViewActivity.this);
                builder.setTitle("내 서재에서 삭제")
                                .setMessage("내 서재에서 삭제 시, 복구가 불가능합니다. \n 정말 내 서재에서 삭제할까요?")
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mylibrary_delete(user_number, book_number, mylibrary_number, type);
                                            }
                                        })
                                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();

            }
        });


        // on create 5. 내 서재 게시글 수정하기
        modify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("내 서재 수정 버튼 클릭 on click진입", "유저넘버string:  " + user_number + ", 게시글번호int: "+ mylibrary_number + ", 책번호int: " + book_number + ", 타입string: " + type);

                // 정말 수정하시겠습니까? ? 물어보깅.
                AlertDialog.Builder builder = new AlertDialog.Builder(MyLibraryViewActivity.this);
                builder.setTitle("게시글을 수정할까요?")
                        .setMessage("수정을 원할 시, 확인 버튼을 눌러주세요")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MyLibraryViewActivity.this, MyLibraryUpdateActivity.class);
                                intent.putExtra("user_number", user_number);
                                intent.putExtra("mylibrary_number", String.valueOf(mylibrary_number)); // String으로 변환
                                intent.putExtra("book_number", String.valueOf(book_number)); // String으로 변환
                                intent.putExtra("type", type);
                                startActivity(intent);
                                // 이때 뒤로 가기 해도 되는지 ?? 아예 finish를 해야하는지 ??
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
    } // on create 끝


    // 메소드1. 유저 넘버 가져오는 메소드 (레트로핏)
    public void get_userNumber (String user_emailid, UserNumberCallback callback) {
        Log.i("내 서재 상세보기에서 유저넘버가져오기", "유저의 emailId: " + user_emailid);
        service.user_emailId(user_emailid).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("내 서재 상세보기에서-유저 number 체크 response 진입", "onResponse: 응답메시지: " + result.getMessage()); // echo로 보내주는 메시지.
                Log.e("내 서재 상세보기에서-유저 number 체크 response 진입", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.

                if(result.getCode() == 200 ) {
                    Log.e("유저 number 성공 첫번째증거", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                    callback.onUserNumberReceived(result.getUser_number()); // 콜백 메소드 실행 !!
                } else {
                    Log.e("php에서 뭔가 잘못됨. / code==400!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Log.e("내 서재 상세보기에서-유저넘버찾기 통신 failed", "실패원인: " +throwable.getMessage());
            }
        });
    }


    // 메소드 2. 데이터베이스에서 내용 가져오는 메소드 (레트로핏)

    public void mylibrary_view_setting(String user_number, int book_number, int mylibrary_number, String type) {
        Log.e("내서재 <전체 리스트>에서 상세보기 http통신 메소드진입", " 유저번호: " + user_number + ", 책번호: " + book_number + ", 내서재 번호: " + mylibrary_number + ", 타입: " + type);
        service.mylibrary_view(lib_user_numberString, book_number, mylibrary_number, type).enqueue(new Callback<MyLibraryListResponse>() {
            @Override
            public void onResponse(Call<MyLibraryListResponse> call, Response<MyLibraryListResponse> response) {
                // 가져온 내용 뿌려주기 !
                MyLibraryListResponse result = response.body();
                Log.e("내서재 <전체> 상세보기 http 성공!!", " reponse 진입함" );
                Log.e("내서재 <전체> 상세보기 http 성공!!", " php 응답코드: " + result.getCode() + ", 메시지: " + result.getMessage());
                Log.i("내서재 <전체> 상세보기 http 성공!!", "유저번호: "+ result.getUser_number() + ", 서재 타입: "+ result.getType() + ", 책번호: " + result.getBook_number() + ", 서재번호: " + result.getMylibrary_number()
                        + ", title: " + result.getTitle() + ", Author: " + result.getAuthor() + "Description: " + result.getCover() + ", isbn: " + result.getIsbn() );

                // 책 정보 세팅하기.
                view_title.setText(result.getTitle());
                view_author.setText(result.getAuthor());



                if(result.getSave_type() !=null && result.getSave_type().equals("SELF") && result.getCover() !=null) {
                    // Glide 책 커버 추가
                    Glide.with(MyLibraryViewActivity.this) // 현재 View의 context
                            .load("http://3.39.255.234/php/img/"+ result.getCover())  // 서버 사진 이미지
                            .into(view_cover); // Imgage View에 표시해주기.
                } else  {
                    // Glide 책 커버 추가
                    Glide.with(MyLibraryViewActivity.this) // 현재 View의 context
                            .load(result.getCover())  // 서버 사진 이미지
                            .into(view_cover); // Imgage View에 표시해주기.


                }






                // type에 따라 뷰 다르게 세팅 해주기
                if(result.getType().equals("1")) { // 읽은 책 일 경우,
                    // 내 서재 타입 세팅
                    view_type.setText("읽은 책");

                    // visibility
                    already_layout.setVisibility(View.VISIBLE);
                    reading_layout.setVisibility(View.GONE);
                    want_layout.setVisibility(View.GONE);

                    // already_howlong.setText(""); // 읽은 기간 - 날짜 (오늘이랑 시작 날짜의 차이)
                    already_started.setText(result.getStarted()); // 시작 날짜
                    already_finished.setText(result.getFinished()); // 종료 날짜

                   already_view_bookDescription.setText(result.getDescription());
                   already_view_bookPublish.setText(result.getPublisher());
                   already_view_bookIsbn.setText(result.getIsbn());

                    // 읽은 책
                    float alreadyRating = 0.0f;
                    try {
                        alreadyRating = Float.parseFloat(result.getRating());
                    } catch (NumberFormatException e) {
                        Log.e("내서재 어댑터!! 평점 float 변환", "읽고 싶은 책 평점 float 오류남 이유: " + e.getMessage());
                    }
                    already_ratingstar.setRating(alreadyRating);


                } else if (result.getType().equals("2")) { // 읽고 있는 책
                    // 내 서재 타입 세팅
                    view_type.setText("읽고 있는 책");

                    // visibility
                    already_layout.setVisibility(View.GONE);
                    reading_layout.setVisibility(View.VISIBLE);
                    want_layout.setVisibility(View.GONE);

                    // reading_been.setText("");
                    reading_started.setText(result.getStarted());
                    reading_page_cnt.setText(result.getReadPage());

                    reading_view_bookDescription.setText(result.getDescription());
                    reading_view_bookPublish.setText(result.getPublisher());
                    reading_view_bookIsbn.setText(result.getIsbn());

                } else if (result.getType().equals("3")) { // 읽고 싶은 책
                    // 내 서재 타입 세팅
                    view_type.setText("읽고 싶은 책");

                    // visibility
                    already_layout.setVisibility(View.GONE);
                    reading_layout.setVisibility(View.GONE);
                    want_layout.setVisibility(View.VISIBLE);

                    // 읽고 싶은 책
                    float wantRating = 0.0f;
                    try {
                        wantRating = Float.parseFloat(result.getRating());
                    } catch (NumberFormatException e) {
                        Log.e("내서재 어댑터!! 평점 float 변환", "읽고 싶은 책 평점 float 오류남 이유: " + e.getMessage());
                    }
                    want_rating.setRating(wantRating);
                    want_preview.setText(result.getPreview());

                    want_view_bookDescription.setText(result.getDescription());
                    want_view_bookPublish.setText(result.getPublisher());
                    want_view_bookIsbn.setText(result.getIsbn());
                }
            }
            @Override
            public void onFailure(Call<MyLibraryListResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("전체에서 상세보기 실패 원인: ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
            }
        });
    } // my library view setting 레트로핏 메소드 끝


    // 메소드 3. 내 서재 게시글 삭제 (레트로핏)
    public void mylibrary_delete (String user_number, int book_number, int mylibrary_number, String type) {
        Log.e("내서재 <상세보기>에서 게시글삭제!!!  http통신 메소드진입", " 유저번호: " + user_number + ", 책번호: " + book_number + ", 내서재 번호: " + mylibrary_number + ", 타입: " + type);
        service.mylibrary_delete(user_number, book_number, mylibrary_number, type).enqueue(new Callback<MylibraryResponse>() {
            @Override
            public void onResponse(Call<MylibraryResponse> call, Response<MylibraryResponse> response) {
                MylibraryResponse result = response.body();
                Log.e("내서재 <상세보기>에서 게시글삭제!!! 통신 성공 !!! ", " code: " + result.getCode() + ", 메시지: " + result.getMessage());
                finish(); // 이러면 view에서 목록으로 돌아가야함!! view activity를 종료하기 때문에 !!
            }

            @Override
            public void onFailure(Call<MylibraryResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("상세보기에서 삭제 실패: ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
            }
        });
    }



    // = =================================== 유저 넘버 가져오는 레트로핏에 대한 call back 인터페이스 메소드 !!
    @Override
    public void onUserNumberReceived(String userNumber) {
        this.user_number = userNumber;
        Log.e("유저 number 성공 두번째증거", "유저넘버: " + userNumber); // echo로 보내주는 메시지.
        Log.d("유저 number 성공 두번째증거", "책번호:  " + book_number + ", 내서재 번호: " + mylibrary_number);

        // 서버에서 내용가져오기.
        mylibrary_view_setting(user_number, book_number, mylibrary_number, type);
        // user number = String / book number = int / mylibrary number = int


//        // 인텐트 확인 후, 현재 로그인 한 유저랑, 서재 글쓴이랑 다르면 수정 삭제 안보이게 하기.
        if (!user_number.equals(lib_user_numberString)) {
            // 현재 다른 유저 서재 들어옴.
            modify_btn.setVisibility(View.GONE);
            delete_btn.setVisibility(View.GONE);
        }
    } // onUserNumberReceived 끝

    @Override
    public void onError(String errorMessage) {
        Log.d("유저 number 실패", "에러메시지:  " + errorMessage);

    }

    // = =================================== 유저 넘버 가져오는 레트로핏에 대한 call back 메소드 끝 끝 끝  !!





} // 내 서재 상세보기 액티비티
