package com.example.myapplication.layout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.recyclerview.SearchBookData;

public class BookViewActivity extends AppCompatActivity {

    // view
    Toolbar toolbar; // 툴바
    Button bookView_save_btn;
    ImageView bookView_cover;
    TextView bookView_title_txt, bookView_author_txt, bookView_description_txt,
            bookView_pub_txt, bookView_isbn_txt;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchbook_view);


        // ======================= 초기화 zone 시작 =================================

        // 툴바
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }

        // bvc0. 툴바 뒤로 가기 클릭시, 이전 화면으로 (home fragment)
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // 뒤로 가기 동작 수행
            }
        }); // bs1 끝


        // bvc1. 뷰 초기화
        bookView_save_btn = findViewById(R.id.bookView_save_btn);
        bookView_cover = findViewById(R.id.bookView_cover);
        bookView_title_txt = findViewById(R.id.bookView_title_txt);
        bookView_author_txt = findViewById(R.id.bookView_author_txt);
        bookView_description_txt = findViewById(R.id.bookView_description_txt);
        bookView_pub_txt = findViewById(R.id.bookView_pub_txt);
        bookView_isbn_txt = findViewById(R.id.bookView_isbn_txt);


        // bvc2. 넘어온 데이터

        String emailId = getIntent().getStringExtra("emailId"); // 현재 로그인 중인 회원의 이메일 혹은 카카오 아이디 값.
        String title = getIntent().getStringExtra("title"); // 책 제목
        String author = getIntent().getStringExtra("author"); // 책 작가.
        String description = getIntent().getStringExtra("description");// 책 소개
        String publisher =getIntent().getStringExtra("publisher");// 출판사
        String pubDate =getIntent().getStringExtra("pubDate"); // 출판 날짜 (지은이 옆에 같이 setting 해주기)
        String cover = getIntent().getStringExtra("cover"); // 표지
        String isbn = getIntent().getStringExtra("isbn"); // 책 고유 번호

        Log.i("책상세보기페이지", "현재 로그인 회원 email혹은ID: " + emailId);

        Log.i("책상세보기페이지", "제목: " + title + ", 작가: " + author + ", 책소개: " + description
                + ", 출판사: " + publisher + ", 출판날짜: " +  pubDate + ", 책표지: " + cover+ ", isbn: " + isbn);

        // + 출판연도 분리 !!
        String[] datePart = pubDate.split("-");
        String pub_year = datePart[0];

        // bvc2. 데이터 뷰에 세팅
        bookView_title_txt.setText(title); // 책 제목
        bookView_author_txt.setText(author+" ["+pub_year+"]"); // 지은이, 출판연도
        bookView_description_txt.setText(description); // 책 소개
        bookView_pub_txt.setText(publisher); // 출판사
        bookView_isbn_txt.setText(isbn); //

        // Glide 추가
        Glide.with(BookViewActivity.this) // 현재 View의 context
                .load(cover)  // 서버 사진 이미지
                .into(bookView_cover); // Imgage View에 표시해주기.



        // bvc3. 저장 버튼 클릭 이벤트 - 저장하는 페이지로 이동하기.
        bookView_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("내서재에 저장버튼 클릭", "저장할책info>> 책제목: "+title+", 작가: "+author+", 소개: "+ description + ", isbn: " + isbn + "cover: " + cover );                // 이동시.. 인텐트로 책정보 다시 바리바리 싸들고감...?? 녱..
                Intent intent = new Intent(getApplicationContext(), MyLibraryCreateActivity.class);
                intent.putExtra("emailId", emailId);
                intent.putExtra("title", title);
                intent.putExtra("author", author);
                intent.putExtra("description", description);
                intent.putExtra("publisher", publisher);
                intent.putExtra("pubDate", pubDate);
                intent.putExtra("cover", cover);
                intent.putExtra("isbn", isbn);
                intent.putExtra("FROM_FLAG", "BOOK_SEARCH");
                startActivity(intent);
            }
        });


        // 저장 완료될때, 책 테이블 따로, 저장하는 테이블따로 해야하는지 ? =>> 따로 해서 isbn을 참조하자 !!!
        // 책 고유번호 isbn 으로


        // ======================= 초기화 zone 끝 =================================



    }
}
