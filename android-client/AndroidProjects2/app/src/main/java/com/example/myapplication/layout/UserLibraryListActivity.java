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
import com.example.myapplication.data.recyclerview.MylibraryAlreadyAdapter;
import com.example.myapplication.data.recyclerview.MylibraryAlreadyData;
import com.example.myapplication.data.recyclerview.MylibraryReadingAdapter;
import com.example.myapplication.data.recyclerview.MylibraryReadingData;
import com.example.myapplication.data.recyclerview.MylibraryWantAdapter;
import com.example.myapplication.data.recyclerview.MylibraryWantData;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.MyLibraryListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserLibraryListActivity extends AppCompatActivity {

    // import
    RetrofitService service; // 레트로핏 서비스

    // view
    Toolbar toolbar; // 툴바
    TextView library_type;


    // 리사이클러뷰 및 어댑터

    private RecyclerView library_recyclerview;

    MylibraryAlreadyAdapter alreadyAdapter; // 읽은 책
    MylibraryReadingAdapter readingAdapter; // 읽고 있는 책
    MylibraryWantAdapter wantAdapter; // 읽고 싶은 책


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_library_list);
        Log.d("생명주기확인 onCreate !! ", "UserLibraryListActivity on create 들어옴 ");


        // ======================= 뷰 초기화 zone 시작 =================================

        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의

        // view 연결
        toolbar = findViewById(R.id.toolbar);
        library_type = findViewById(R.id.library_type);


        // 리사이클러뷰 find view 해주기
        library_recyclerview = findViewById(R.id.library_list_recyclerview);

        // 리사이클러뷰 어댑터
        alreadyAdapter = new MylibraryAlreadyAdapter(UserLibraryListActivity.this);
        readingAdapter = new MylibraryReadingAdapter(UserLibraryListActivity.this);
        wantAdapter = new MylibraryWantAdapter(UserLibraryListActivity.this);


        // ======================= 뷰 초기화 zone 끝 =================================


        // on create 1. 툴바 뒤로가기 클릭 시, 이전 화면으로
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserLibraryListActivity.this, MainActivity.class);
                intent.putExtra("fragment", "myProfile");
                startActivity(intent);
                finish();
            } });


        // on create 2. intent 로 타입 받아서
        Intent getIntent = getIntent();
        String type = getIntent.getStringExtra("type");
        String user_number = getIntent.getStringExtra("user_number");


        // 타입 별로 분류 하기.
        if(type.equals("read")) {
            library_type.setText("읽은 책");
            already_list_Setting(user_number);
        } else if (type.equals("reading")) {
            library_type.setText("읽고 있는 책");
            reading_list_Setting(user_number);
        } else if (type.equals("want")) {
            library_type.setText("읽고 싶은 책");
            want_list_Setting(user_number);
        }
    } // on create 끝


    // method 1-1) library_list - 읽은 책
    public void already_list_Setting(String user_number) {
        Log.e("내서재 <읽은 책> 레트로핏 메소드진입", "매개변수 유저넘버: " + user_number);
        service.mylibrary_alreadysetting(user_number).enqueue(new Callback<MyLibraryListResponse.MyLibraryResponse2>() {
            @Override
            public void onResponse(Call<MyLibraryListResponse.MyLibraryResponse2> call, Response<MyLibraryListResponse.MyLibraryResponse2> response) {
                Log.e("내서재 읽은 책 http 통신 성공", " reponse 진입함" );
                MyLibraryListResponse.MyLibraryResponse2 result = response.body();
                // response2에 있는 getter 사용한 것임.
                List<MyLibraryListResponse> library_item = result.getLibrary_items();

                if ( result.getMessage().equals("게시글이 없습니다")){
                    Log.e("게시글이 없음", " reponse 진입함" );
                    alreadyAdapter.clearItem();
                } else {

                    // 레이아웃 매니저 세팅
                    library_recyclerview.setLayoutManager(new LinearLayoutManager(UserLibraryListActivity.this, LinearLayoutManager.VERTICAL, false));
                    library_recyclerview.setAdapter(alreadyAdapter);

                    // <읽은 책> 내 서재 목록 출력
                    alreadyAdapter.clearItem();
                    for (MyLibraryListResponse item : library_item) {

                        if (item.getType().equals("1")) { // 읽은 책
                            Log.i(" already <읽은 책> 결과 >>>  ", "내서재 각테이블번호"+ item.getMylibrary_number()+  "title: " + item.getTitle() + ", Author: " + item.getAuthor() +
                                    "Description: " + item.getCover() + ", 읽은책 시작: " + item.getStarted() + ", 읽은책 종료: " + item.getFinished() +
                                    ", 평점: " + item.getRating());
                            int library_number = item.getMylibrary_number();
                            int book_number = item.getBook_number();
                            int lib_user_number = item.getMylibrary_user_number();
                            String bookSaveType = item.getSave_type();
                            String bookCover = item.getCover();
                            String bookTitle = item.getTitle();
                            String bookAuthor = item.getAuthor();
                            String started = item.getStarted();
                            String finished = item.getFinished();
                            String rating = item.getRating();
                            String created = item.getCreatedTime();
                            int type = Integer.parseInt(item.getType());
                            alreadyAdapter.addItem(new MylibraryAlreadyData(library_number,lib_user_number, book_number, bookCover, bookTitle, bookAuthor, rating, started, finished, created, type, bookSaveType));
                        }
                    } // for문 끝 - 읽은 책 가져오기 from response2
                }
            }

            @Override
            public void onFailure(Call<MyLibraryListResponse.MyLibraryResponse2> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("읽은 책 목록 가져오기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("읽은 책 목록 가져오기 실패. ", "onFailure: " + throwable.getCause());
            }
        });
    } // 읽은 책 끝


    // method 1-1) library_list - 읽은 책
    public void reading_list_Setting (String user_number) {
        Log.e("내서재 <읽고 있는 책> 레트로핏 메소드진입", "매개변수 유저넘버: " + user_number);
        service.mylibrary_readingsetting(user_number).enqueue(new Callback<MyLibraryListResponse.MyLibraryResponse2>() {
            @Override
            public void onResponse(Call<MyLibraryListResponse.MyLibraryResponse2> call, Response<MyLibraryListResponse.MyLibraryResponse2> response) {
                Log.e("내서재 읽고 있는 책 http 통신 성공", " reponse 진입함" );
                MyLibraryListResponse.MyLibraryResponse2 result = response.body();
                // response2에 있는 getter 사용한 것임.
                List<MyLibraryListResponse> library_item = result.getLibrary_items();

                if ( result.getMessage().equals("게시글이 없습니다")){
                    Log.e("게시글이 없음", " reponse 진입함" );
                    readingAdapter.clearItem();
                } else {

                    // 레이아웃 매니저 세팅
                    library_recyclerview.setLayoutManager(new LinearLayoutManager(UserLibraryListActivity.this, LinearLayoutManager.VERTICAL, false));
                    library_recyclerview.setAdapter(readingAdapter);


                    // <읽고 있는 책> 내 서재 목록 출력
                    readingAdapter.clearItem();
                    for (MyLibraryListResponse item : library_item) {
                        if (item.getType().equals("2")) {
                            Log.i("reading  <읽고 있는 책> 결과 >>>  ", "내서재 각테이블번호"+ item.getMylibrary_number()+  "title: " + item.getTitle() + ", Author: " + item.getAuthor() +
                                    "Description: " + item.getCover() + ", 읽고 있는 책 시작: " + item.getStarted() + ", 읽은 페이지: " + item.getReadPage());
                            int library_number = item.getMylibrary_number();
                            int book_number = item.getBook_number();
                            int lib_user_number = item.getMylibrary_user_number();
                            String bookCover = item.getCover();
                            String bookTitle = item.getTitle();
                            String bookAuthor = item.getAuthor();
                            String started = item.getStarted();
                            String readPage = item.getReadPage();
                            String created = item.getCreatedTime();
                            int type = Integer.parseInt(item.getType());
                            readingAdapter.addItem(new MylibraryReadingData(library_number,lib_user_number, book_number, bookCover, bookTitle, bookAuthor, readPage, started, created, type ));
                        }
                    } // for문 종료
                }
            }

            @Override
            public void onFailure(Call<MyLibraryListResponse.MyLibraryResponse2> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("읽고 있는 책 목록 가져오기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("읽고 있는 책 목록 가져오기 실패. ", "onFailure: " + throwable.getCause());
            }
        });
    } // 읽고 있는 책 끝


    // method 1-3) library_list - 읽고 싶은 책
    public void want_list_Setting (String user_number) {
        Log.e("내서재 읽고 싶은책 목록 레트로핏 메소드진입", "매개변수 유저넘버: " + user_number);
        service.mylibrary_wantsetting(user_number).enqueue(new Callback<MyLibraryListResponse.MyLibraryResponse2>() {
            @Override
            public void onResponse(Call<MyLibraryListResponse.MyLibraryResponse2> call, Response<MyLibraryListResponse.MyLibraryResponse2> response) {
                Log.e("내서재 읽고 싶은 책 http 통신 성공", " reponse 진입함" );
                MyLibraryListResponse.MyLibraryResponse2 result = response.body();
                // response2에 있는 getter 사용한 것임.
                List<MyLibraryListResponse> library_item = result.getLibrary_items();


                if ( result.getMessage().equals("게시글이 없습니다")){
                    Log.e("게시글이 없음", " reponse 진입함" );
                    wantAdapter.clearItem();
                } else {
                    wantAdapter.clearItem();
                    // <읽고 싶은 책> 내 서재 목록 출력

                    // 레이아웃 매니저 세팅
                    library_recyclerview.setLayoutManager(new LinearLayoutManager(UserLibraryListActivity.this, LinearLayoutManager.VERTICAL, false));
                    library_recyclerview.setAdapter(wantAdapter);


                    for (MyLibraryListResponse item : library_item) {
                        if (item.getType().equals("3")) {
                            Log.i("전체목록  <읽고 싶은 책> 결과 >>>  ", "내서재 각테이블번호"+ item.getMylibrary_number()+  "title: " + item.getTitle() + ", Author: " + item.getAuthor() +
                                    "Description: " + item.getCover() + ", 읽고 싶은 책 기대지수: " + item.getRating() + ", 기대평: " + item.getPreview());
                            int library_number = item.getMylibrary_number();
                            int book_number = item.getBook_number();
                            int lib_user_number = item.getMylibrary_user_number();
                            String bookCover = item.getCover();
                            String bookTitle = item.getTitle();
                            String bookAuthor = item.getAuthor();
                            String rating = item.getRating();
                            String preview = item.getPreview();
                            String created = item.getCreatedTime();
                            int type = Integer.parseInt(item.getType());
                            wantAdapter.addItem(new MylibraryWantData(library_number,lib_user_number, book_number, bookCover, bookTitle, bookAuthor, rating, preview, created, type));
                        }
                    } // for문 종료
                }


            }

            @Override
            public void onFailure(Call<MyLibraryListResponse.MyLibraryResponse2> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("읽고 싶은 목록 가져오기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("읽고 싶은 책 목록 가져오기 실패. ", "onFailure: " + throwable.getCause());
            }
        });

    }

}
