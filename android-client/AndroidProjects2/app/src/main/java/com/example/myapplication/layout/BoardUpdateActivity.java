package com.example.myapplication.layout;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.recyclerview.BoardBookAdapter;
import com.example.myapplication.data.recyclerview.BoardImageAdapter;
import com.example.myapplication.data.recyclerview.BoardViewImgAdapter;
import com.example.myapplication.data.recyclerview.SearchBookData;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.UserNumberCallback;
import com.example.myapplication.data.retrofit.responsemodel.AladinResponse;
import com.example.myapplication.data.retrofit.responsemodel.BoardListResponse;
import com.example.myapplication.data.retrofit.responsemodel.BoardResponse;
import com.example.myapplication.data.retrofit.responsemodel.ImageUploadResponse;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import kotlin.jvm.internal.Ref;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardUpdateActivity extends AppCompatActivity implements UserNumberCallback  {

    // import
    RetrofitService service; // 레트로핏 서비스
    Toolbar toolbar;



    // 쉐어드 프리퍼런스
    SharedPreferences sharedPreferences;

    // 쉐어드 id, email 각각에 대한 키값
    String key = "signin_email_id";
    String signin_email_id_value ;


    // 이미지, 책 리사이클러뷰
    private RecyclerView img_recyclerView;
    private RecyclerView book_recyclerview;


// 이미지 어댑터 관련
    BoardImageAdapter boardViewImgAdapter; //이미지 어댑터임!!

    ArrayList<ImageUploadResponse> imgList2 = new ArrayList<>();
    List<ImageUploadResponse> image_item;
    ImageUploadResponse imgUri;


    // 책 어댑터 관련

    BoardBookAdapter boardBookAdapter; // 책 어댑터
    List<SearchBookData> bookList = new ArrayList<SearchBookData>();

    ActivityResultLauncher<Intent> bookSearchLauncher;



    private static final int REQUEST_CODE_BOOK_SEARCH = 1011;
    private static final int REQUEST_CODE_IMAGE_SELECTION = 1022;






    // 레트로핏으로 가져온 user numnber
    String user_number;


    // view
    EditText edit_title, edit_content; // 게시글 수정제목, 수정 내용
    Button modi_save_btn; // 게시글 수정 완료 버튼
    TextView board_image_btn; // 사진 추가버튼
    TextView board_book_btn; // 책 추가 버튼


    // string
    String before_category = null;
    String selected_board_category = null; // 선택된 게시글 카테고리
    String emailId = null;

    // int
    int board_number ;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_update);

        // ==================================== view  초기화 시작 =============================================

        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의

        // 툴바
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }


        // view
        edit_title = findViewById(R.id.board_update_title);
        edit_content = findViewById(R.id.board_update_content);
        modi_save_btn = findViewById(R.id.board_update_save_btn);
        board_image_btn = findViewById(R.id.board_update_pictures); // 사진 추가 버튼 - 갤러리 open
        board_book_btn = findViewById(R.id.board_update_books); // 책 추가 버튼 - 알라딘 api

        // 이미지, 책 리사이클러뷰
        img_recyclerView = findViewById(R.id.pictures_update_recyclerview);
        book_recyclerview = findViewById(R.id.book_update_recyclerview);


        // 이미지 어댑터 setting
        boardViewImgAdapter = new BoardImageAdapter(imgList2, getApplicationContext());
        // set adapter !! 어댑터 세팅 !!!!
        img_recyclerView.setAdapter(boardViewImgAdapter);

        // 이미지 레이아웃 매니저
        img_recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));


        // 책 어댑터 setting
        boardBookAdapter = new BoardBookAdapter(bookList, BoardUpdateActivity.this);
        book_recyclerview.setAdapter(boardBookAdapter);

        // 책 레이아웃 매니저
        book_recyclerview.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));



        // intent로 넘어온 아이템을 setting 하기 !!
        Intent intent = getIntent();
        before_category= intent.getStringExtra("category");
        board_number = intent.getIntExtra("board_number", board_number);

        Log.d("수정페이지 넘어옴", "넘어온 게시글번호: "+board_number);



        // ==================================== view  초기화 끝  =============================================
        // 뒤로 가기
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // 뒤로 가기 동작 수행
            }
        }); //


        //  on create 0. 쉐어드에서 닉네임 가져오기
        sharedPreferences = getSharedPreferences("session_contain", Context.MODE_PRIVATE);
        signin_email_id_value = sharedPreferences.getString(key, null);

        // email or id 있는지 !
        if (signin_email_id_value==null) {  // 쉐어드가 아예 비었을때 !! 로그인 하러 가기
            Log.d("게시글 작성 - 쉐어드 비어있음", "?? 왜죠 ? ");
            Toast.makeText(BoardUpdateActivity.this, "로그인 후 게시글 작성이 가능합니다.", Toast.LENGTH_SHORT).show();

        } else { // 쉐어드에 값이 있을때 !!
            Log.d("쉐어드에 값이 있음.", "게시글 작성 액티비티임. key:signin_email_id  value: "+signin_email_id_value);
            // JSON 파싱하여 특정 키 값 추출
            try {
                JSONObject jsonObject = new JSONObject(signin_email_id_value);
                emailId = jsonObject.optString("emailid", ""); // default 값 설정 가능
                String nickname = jsonObject.optString("nickname", "");
                // 파싱 완료하면 !!! null이 아닐때 유저의 넘버 가져오기 !!
                get_userNumber(emailId, (UserNumberCallback) this);


                if (emailId.isEmpty()) {
                    Log.d("json아이디없음", ". key:signin_email_id  value: "+signin_email_id_value);
                } else {
                    Log.d("json아이디있음.", ". key:signin_email_id  value: "+signin_email_id_value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } // 쉐어드에서 이메일아이디 파싱하기.


        // on create 1. 게시글 카테고리 spinner 설정

        Spinner spinner = findViewById(R.id.board_category_spinner);
        // spinner item 리스트
        List<String> items = new ArrayList<>();
        items.add("게시글 유형");
        items.add("책 추천");
        items.add("잡담");
        items.add("가입인사");

        // 어댑터 설정
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.board_spinner_dropdown_item, items) {
            @Override
            public boolean isEnabled(int position) {
                return position !=0;
            }

            public View getDropDownView (int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Hint 스타일 적용
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(R.layout.board_spinner_dropdown_item);
        spinner.setAdapter(adapter);




        // 가져온 카테고리를 기본값으로 설정하기
        if (before_category != null && items.contains(before_category)) {
            spinner.setSelection(items.indexOf(before_category));
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    // 첫 번째 항목이 아닌 다른 항목이 선택된 경우
                    selected_board_category = parent.getItemAtPosition(position).toString();
                    Log.d("게시글 작성 create activity", "선택된 카테고리:  " + selected_board_category);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // on create 2. 기존 수정 전 내용 세팅하기
        // 유저 번호를 가져오는 메소드, 콜백 메소드 안에 있음.


        // on create 3. 게시글 수정 완료 버튼
        modi_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("수정 버튼 누름! 게시글 수정 activity", "유저넘버존재함 :  " + user_number);

                // 기존 카테고리, 수정된 카테고리를 출력한다 !!
                // 그러나 !! 기존 카테고리를 드롭다운의 기본값으로 설정했기 때문에 !! 수정 안할때에도 수정된카테고리에 기존값이 들어감!!
                Log.d("수정 버튼 누름! 게시글 수정 activity", "기존카테고리 :  " + before_category +", 수정된카테고리: " + selected_board_category);

                // 수정 완료 되었을 때. 가지고 갈 것은??
                // 제목, 내용, 카테고리 !! (선택된 아이들을 가지고 가깅. )
                String edited_title = edit_title.getText().toString();
                String edited_content = edit_content.getText().toString();


                // 저장할 책 목록
                for (int i=0; i<bookList.size(); i++) {
                    Log.d("수정할 게시글 책 목록!! ", "책 " + i+"번째제목: "+ bookList.get(i).getTitle());
                }


                // 필수 값 null인지 확인하기 !!
                if(selected_board_category !=null && edited_title !=null && edited_content !=null) {
                    Log.d("게시글 수정내용!!", "제목 :  " + edited_title + ", 내용: " + edited_content + ", 게시글 유형: " + selected_board_category);
                    // 세개 다 null이 아닐 때
                    // 수정 메소드 실행
                    board_update(user_number, edited_title, edited_content, selected_board_category, bookList);
                    // 수정하려는 이미지 내용 출력하기
                    for(int i=0; i<imgList2.size(); i++) {
                        // uri가 null = DB에서 가져온 사진일 때
                        if (imgList2.get(i).getImageUri()==null) {
                            Log.d("이미지 수정내용!! -DB사진", "array " + i+"번째는 url DB :  " + imgList2.get(i).getImageUrl());
                        } else if (imgList2.get(i).getImageUrl()==null) {
                            Log.d("이미지 수정내용!! -갤러리사진", "array " + i+"번째는 uri 갤러리추가 :  "  + imgList2.get(i).getImageUri());
                        }
                    } // for문 끝이미지 수정내용 출력 !!






                }
                if (edited_title  == null) {
                    Log.d("게시글 작성 내용!!", "제목 :  " + edited_title + ", 내용: " + edited_content + ", 게시글 유형: " + selected_board_category);
                    Toast.makeText(BoardUpdateActivity.this, "게시글 제목을 입력해주세요", Toast.LENGTH_SHORT).show();
                }

                if (edited_content == null) {
                    Toast.makeText(BoardUpdateActivity.this, "게시글 내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                }

                if (selected_board_category == null) {
                    Toast.makeText(BoardUpdateActivity.this, "게시글 유형을 입력해주세요", Toast.LENGTH_SHORT).show();

                }
            }
        });


        // on create 4. 갤러리에서 이미지 가져오는 메소드
        board_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 갤러리 호출하기
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                intent.setAction(Intent.ACTION_PICK);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 사진 여러장 가능
                activityResultLauncher.launch(intent);
            }
        });

        // on create 5. 책 추가 메소드
        board_book_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("수정 - 책검색버튼 누름!! ", "책 추가 !!  " );

                // intent 로 넘기기. board의 책 검색 화면으로.
                Intent intent = new Intent(BoardUpdateActivity.this, BoardBookSearchActivity.class);
                bookSearchLauncher.launch(intent);
            }
        });


        // on create 5-2.책 검색 액티비티로부터 선택한 책 정보를 받아옴.
        bookSearchLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == REQUEST_CODE_BOOK_SEARCH ) {
                        Intent data = result.getData(); // get data 도 인텐트에서 기본적으로 제공하는 것임.
                        if (data != null) {
                            String bookTitle = data.getStringExtra("title");
                            String bookAuthor = data.getStringExtra("author");
                            String bookCover = data.getStringExtra("cover");
                            String description = data.getStringExtra("description");

                            bookList.add(new SearchBookData(bookTitle, bookAuthor,description, null, null, bookCover, null ));
                            boardBookAdapter.notifyDataSetChanged(); // 책 아이템이 추가되었음을 알리기

                            // 추가하고 책 목록 뽑아보기
                            for (SearchBookData item : bookList) {
                                int cnt=0;
                                cnt +=1;
                                Log.i("update책 추가함 ",cnt +"번째 책제목: " + item.getTitle()+", 작가: " + item.getAuthor());
                            }

                            // 여기서 책 정보 어댑터 가져와서 리사이클러뷰 add 해주기. 위해 list 에만 add한다. 자동으로 리사이클러뷰에 반영됨 !!
                            // 책을 하나씩만 선택할 수 있기 때문에 for문 안돌려도됨 헤헷 그냥 어댑터.add (new data ) 하면됨 !!!
                            Log.i("수정-책검색 정보 create로넘어옴 !! ", "책제목: " + bookTitle);

                        }
                    }
                }
        );




    } // on create 끝


    // 갤러리 Activity Result Launcher
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {


                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData(); // data를 인텐트 타입으로 반환함.
                        // intent란?
                        // 컴포넌트를 실행하기 위해 시스템에 전달하는 메시지 객체로,
                        // 기능을 수행하는 함수를 제공하지 않고, 데이터를 담는 클래스이다.


                        // clip data 란??
                        // null 인 경우가 없는 것인가 ??
                        Log.d("게시글수정 에서이미지 가져옴", "이미지getdataㄷㅈ() :  " +  result.getData() + ", intent: " + intent);
//                        String imgString = intent.getDataString().toString();
//                        Uri img_uri = Uri.parse(imgString);
//                        Log.d("게시글수정 에서이미지 한장??", "이미지getdata() :  " +    imgString);
                        Log.e("기존이미지 개수:  ", "imgList2.size(): "+imgList2.size()); // 기존 사진 개수 같이 출력



                        if(intent == null) {
                            Log.d("게시글수정 에서이미지 안가져옴!", "이미지getdata() :  " +  result.getData() + ", intent: " + intent);
                            // 이미지 안가져오면 아예 반환을 안하는건가요 ??

                        } else { // 이미지를 하나라도 선택한 경우

                            if (intent.getClipData() != null) { // 이미지를 선택한 경우
                                // 일단 뭐라도 반환을 해야지 !!! 선택이 됨.
                                Log.e("이미지를 가져옴 ~~! ", "클립데이터: "+ intent.getClipData().toString()); // 사진 개수 같이 출력
                                ClipData clipData = intent.getClipData();

                                // 만약 새로 추가 선택한 사진과 기존 DB 사진을 합쳤을때 10장이 넘으면
                                // 한번 튕겨냄.
                                // for문
                                if ((clipData.getItemCount()+imgList2.size()) > 10) { // 기존
                                    Toast.makeText(getApplicationContext(), "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
                                } else { // 기존 사진 + 추가한 사진의 합이 10이 넘지 않을 경우
                                    Log.e("총10장이하로 가져옴", "클립데이터아이템수: "+ clipData.getItemCount());
                                    Log.e("총10장이하로 가져옴", "기존이미지수 : "+ imgList2.size());
                                     int newImage_cnt = clipData.getItemCount();
                                     int boardImage_cnt = imgList2.size();
                                    Log.e("총10장이하로 가져옴", "클립데이터아이템수+기존이미지수: "+ (clipData.getItemCount()+imgList2.size())); // 여기에 괄호로 두개를 안 묶어주면 string+int+int 라서 문자열 연결됨.

                                    int selectCount = 0;
                                    // for문을 사용해서 사진의 uri를 가져온다.
                                    for (int i=0; i<clipData.getItemCount(); i++) { //
                                      //String imgString = intent.getDataString().toString();
                                        Uri imageUri = clipData.getItemAt(i).getUri();
                                        Log.e("가져온이미지의 uri string", "uri string: "+ imageUri.toString()); // 여기에 괄호로 두개를 안 묶어주면 string+int+int 라서 문자열 연결됨.

                                      //Uri img_uri = Uri.parse(imgString);
                                         imgUri = new ImageUploadResponse(null,imageUri,0,0 );
                                        imgList2.add(imgUri);
                                      //  boardViewImgAdapter.addItem(imgUri);
                                        Log.e("for문 몇번째인가요? ", "i값+1: "+ (i+1)); // 여기에 괄호로 두개를 안 묶어주면 string+int+int 라서 문자열 연결됨.
                                        boardViewImgAdapter.notifyDataSetChanged();
                                        Log.e("새로운 이미지 추가한 imgList2 ", "길이: "+ imgList2.size()); // 여기에 괄호로 두개를 안 묶어주면 string+int+int 라서 문자열 연결됨.


                                    } // for문 끝
                                } // 기존이미지+추가한 이미지가 10장 이내일 경우

                                Log.e("사진 추가하고 리사이클러뷰길이", "get count: "+ boardViewImgAdapter.getItemCount()); // 여기에 괄호로 두개를 안 묶어주면 string+int+int 라서 문자열 연결됨.


                            }
//                            else {
//                                // 이미지를 여러장 선택한 경우
//                            }
                        }

                    }

                }
            }
    );




    // method 1. 유저 이메일아이디를 가지고 유저의 number 가져오기
    public void get_userNumber (String user_emailid, UserNumberCallback callback) {
        Log.i("게시판상세- 유저넘버가져오기", "유저의 emailId: " + user_emailid);
        service.user_emailId(user_emailid).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("게시판상세-유저 number 체크 response 진입", "onResponse: 응답메시지: " + result.getMessage()); // echo로 보내주는 메시지.
                Log.e("게시판상세-유저 number 체크 response 진입", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.

                if(result.getCode() == 200 ) {
                    Log.e("유저 number 성공 첫번째증거", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                    callback.onUserNumberReceived(result.getUser_number()); // 콜백 메소드 실행 !!
                } else {
                    Log.e("php에서 뭔가 잘못됨. / code==400!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Log.e("게시판상세-유저넘버찾기 통신 failed", "실패원인: " +throwable.getMessage());
            }
        });
    } // method 1 끝


    // method 1-1)
    // = =================================== 유저 넘버 가져오는 레트로핏에 대한 call back 인터페이스 메소드 !!
    @Override
    public void onUserNumberReceived(String userNumber) {
        this.user_number = userNumber;
        Log.e("유저 number 성공 두번째증거", "유저넘버: " + userNumber); // echo로 보내주는 메시지.
        // 서버에서 내용가져오기.
        // 기존 게시글 가져오는 메소드 호출 !! board view 그대로 사용하기.
        board_update_setting(user_number, board_number, before_category);
        // 기존 게시글의 이미지 가져오는 메소드 !!! view에 있는거 그대로 가져옴 !!
        boardImage_setting(user_number, board_number);
    } // onUserNumberReceived 끝

    @Override
    public void onError(String errorMessage) {
        Log.d("유저 number 실패", "에러메시지:  " + errorMessage);
    }

    // = =================================== 유저 넘버 가져오는 레트로핏에 대한 call back 메소드 끝 끝 끝  !!




    // method 2-1) 기존 내용 가져오기
    public void board_update_setting (String user_number, int board_number, String before_category) {
        Log.e("게시판 수정 보기 http통신 메소드진입", " 유저번호: " + user_number + ", 게시판번호: " + board_number + ", 수정 전 카테고리: " + before_category);
        service.board_view(user_number, board_number, before_category).enqueue(new Callback<BoardListResponse>() {
            @Override
            public void onResponse(Call<BoardListResponse> call, Response<BoardListResponse> response) {

                // 가져온 내용 뿌려주기 !
                BoardListResponse result = response.body();
                Log.e("게시판 수정전 세팅 http 성공!!", " reponse 진입함" );
                Log.e("게시판 수정전 세팅 http 성공!!", " php 응답코드: " + result.getCode() + ", 메시지: " + result.getMessage());
                Log.i("게시판 수정전 세팅 http 성공!!", "유저번호: "+ result.getUser_number() + ",게시판번호: "+ result.getBoard_number() + ", 카테고리: " + result.getCategory()
                        + ", title: " + result.getTitle() + ", 내용: " + result.getContent() + "작성날짜: " + result.getCreatedTime());

                // 게시판 수정 전 내용 뿌려주기
                edit_title.setText(result.getTitle()); //제목
                edit_content.setText(result.getContent()); // 내용


                // 책 내용 뿌려주기
                List<AladinResponse> bookItem = result.getBooks();
                boardBookAdapter.clearItem(); // 일단 한번 지워줌
                book_recyclerview.setVisibility(View.VISIBLE); // 게시글 상세보기에 있는 리사이클러뷰 visiblity


                if (bookItem !=null) {// 책이 없는 경우도 있음
                    for (AladinResponse item : bookItem ) {
                        String bookTitle = item.getTitle();
                        String bookAuthor = item.getAuthor();
                        String bookDescription = item.getDescription();
                        String bookCover = item.getCover();
                        bookList.add(new SearchBookData(bookTitle,bookAuthor,bookDescription,null,null,bookCover,null));
                        boardBookAdapter.notifyDataSetChanged();
                    } // for 문 종료
                }
            } // on response 끝

            @Override
            public void onFailure(Call<BoardListResponse> call, Throwable throwable) {
                Log.d("게시판 수정 setting 실패", "에러메시지:  " + throwable.getMessage());
            }
        });

    } // 기존 내용 가져오기 끝




    // method 2-2) 기존 이미지 가져오기
    public void boardImage_setting (String user_number, int board_number) {
        Log.e("게시글 수정할 이미지 가져오기 레트로핏 들어옴", "레트로핏 들어옴 보드번호: " + board_number );
        service.boardView_image(user_number, board_number).enqueue(new Callback<ImageUploadResponse.ImageUploadResponse2>() {
            @Override
            public void onResponse(Call<ImageUploadResponse.ImageUploadResponse2> call, Response<ImageUploadResponse.ImageUploadResponse2> response) {
                // 이미지를 목록으로 가져오기
                // 사진이 없다면 아무일도 일어나지 않고, 사진이 있을 때에만 리사이클러뷰 세팅되도록.
                Log.e("게시글 상세보기 기존 이미지 가져오기 통신성공", " reponse 진입함" );
                ImageUploadResponse.ImageUploadResponse2 result = response.body();

                if(result.getMessage().equals("사진 없습니다")) {
                    //걍 아무 일도 일어나지 않음...로그 찍기
                    Log.e("이 게시글에는 이미지 없음", "이미지 없는 게시글임" );
                    boardViewImgAdapter.clearItem();
                } else {
                    Log.e("이 게시글에는 이미지 있어 !!! ", "이미지 있음!!!" );
                    // 각각 이미지의 내용들에 대한 상세정보-게시글번호,글쓴이, url(이미지 이름)
                    image_item = result.getImages();
                    boardViewImgAdapter.clearItem();
                    // 리사이클러뷰에 이미지 출력하기.
                    img_recyclerView.setVisibility(View.VISIBLE);
                    Log.e("리스트에 사진 들어간건가 ?", "imgList2 리스트: " + image_item.size() + "리스트내용: " );

                    for (ImageUploadResponse item : image_item) { // 현재 이 로직이 이미지를 아이템으로 받아와서 그 중
                        // 이미지 고유 번호, 게시글 번호, 이미지 서버 url(name만 !!)
                        int board_image_number = item.getBoard_image_number();
                        int board_number = item.getBoard_number();
                        String imageURL = item.getImageUrl();
                        Log.e("각이미지의 정보들 ", "이미지번호: "+ board_image_number + ", 게시글번호: " + board_number + ", url: " + imageURL );
//                        boardViewImgAdapter.addItem(new ImageUploadResponse(imageURL,null,board_image_number, board_number));
                        imgList2.add(new ImageUploadResponse(imageURL, null, board_image_number, board_number));

                    } // for문 종료
                    Log.e("array list에 사진 들어간건가 ?", "array 리스트: " +   imgList2.size());

                    // 여기서 이미지 따로 리사이클러뷰에 add item 할 필요 없음. on  create 에서 이미지 리사이클러뷰 세팅할때 이미 사진 있으면  리사이클러뷰에 세팅하기 때문에...
                    // 기존 게시글에 저장된 이미지가 없을때에는 왜 오류가 안나는지 확인하기.
                    // A. 왜냐면 여기서 분기처리 해놓음 DB에 없으면 어댑터에서 리사이클러뷰 clear 시키기만 함.
                } // 사진이 있을 때
            }

            @Override
            public void onFailure(Call<ImageUploadResponse.ImageUploadResponse2> call, Throwable throwable) {

            }
        });

    }



    // method 3-1) 게시판 수정 (내용)
    public void board_update (String user_number, String edited_title, String edited_content, String edited_category, List<SearchBookData> bookList) {
        Log.i("게시판 수정 레트로핏 실행 ", "유저 번호: " + user_number + ",수정 제목: " + edited_title + ", 수정 내용: " + edited_content + ", 수정 카테고리: " + edited_category );

        String booksJson =null;
        if (bookList.size() == 0) {
            booksJson =null;
        } else {
            booksJson = convertBookToJson(bookList);
            Log.i("게시판 수정 - 책정보 ", "책 목록 gson 사용한거 목록: " + booksJson );
        }

        service.board_modi(user_number, board_number, edited_title,  edited_content, edited_category, booksJson).enqueue(new Callback<BoardResponse>() {
            @Override
            public void onResponse(Call<BoardResponse> call, Response<BoardResponse> response) {
                Log.e("게시판 수정 통신성공!! 서버가 보내는 코드", "code: " + response.code()); //서버가 보내는 http 통신 응답코드
                BoardResponse result = response.body();
                Log.e("php 메시지", "message: " + result.getMessage()); //서버가 보내는 http 통신 응답코드


                if(result.getCode() == 200 ) {
                    // 여기서 이미지 업데이트하는 레트로핏 메소드 호출하기.
                    // imgLis2의 size가 0보다 클 경우 !!!에만 호출하기.
//                    if(imgList2.size() > 0) {
                    Log.e("게시판 수정 성공 메시지", "message: " + result.getMessage()); //서버가 보내는 http 통신 응답코드

                    updateImage(imgList2, board_number);
//                    }

                    Toast.makeText(BoardUpdateActivity.this, "게시판 수정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    // code: 200 일때 내 서재 프래그먼트로 이동 !! 뒤에 있는 스택 지우고가기
//                    Intent intent = new Intent(BoardUpdateActivity.this, MainActivity.class);
//                    intent.putExtra("category", selected_board_category);
//                    intent.putExtra("fragment", "board");
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    // 위 플래그를 설정하면 스택에 있는 기존 액티비티가 전부 제거됨.
//                    startActivity(intent);
//                    finish();
                } else {
                    Log.e("게시판 수정 성공 메시지", "message: " + result.getMessage()); //서버가 보내는 http 통신 응답코드

                    Toast.makeText(BoardUpdateActivity.this, "게시글 수정에 실패하였습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BoardResponse> call, Throwable throwable) {
                throwable.getMessage();
                Log.e("게시판 수정 통신 실패", "onFailure:실패한이유: " + throwable.getMessage());

            }
        });

    } // board update - 게시판 제목, 내용, 카테고리 insert 끝



    // method 3-2) 게시판 수정 (이미지)
    public void updateImage (ArrayList<ImageUploadResponse> imgList, int board_number){ // 이미지 업데이트 하는 메소드
        // 여기로 보낸 array list 가 imgList2인데, 해당 메소드 호출할 때 한번 더 보내기 위해서..
        Log.e("게시판 이미지 수정 메소드 호출", "리스트왔나요? : " + imgList.size() + ", 게시글 번호: " + board_number);

        // 서버로 전송할 이미지를 처리하기 위해 해야하는 일 3가지
        // 1.
        ContentResolver resolver = getContentResolver();
        // 안드로이드에서 애플리케이션이 다른 애플리케이션의 데이터에
        // 접근할 수 있도록 하는 클래스이다.
        // 주로 content provider 를 통해 데이터를 읽고 쓸 수 있게 한다.

        // 2.
        // MultipartBody.Part 를 타입으로 가지는 리스트를 만들어서
        // 이미지 file 을 생성해서 리스트에 add 해주면 됨 !!!
        List<MultipartBody.Part> imgParts = new ArrayList<>();

        // 3.
        //RequestBody 타입 리스트를 만들어서 DB에서 가져온 이미지의 url을 저장해줌.
        List<RequestBody> imgUrls = new ArrayList<>();


        // for문 돌려서, 전송하고자 하는 이미지에 대해 분기처리함.
        for(int i=0; i<imgList.size(); i++) {
            // DB에서 가져온 이미지일 경우
            if(imgList.get(i).getImageUri()==null && imgList.get(i).getImageUrl() !=null ) {
                Log.e("게시판이미지 기존 db 이미지수정", "이미지url : " + imgList.get(i).getImageUrl());

                RequestBody urlBody = RequestBody.create(MediaType.parse("text/plain"),imgList.get(i).getImageUrl());
                imgUrls.add(urlBody); // list 에다가 만들어줌.

            } // 갤러리에서 가져온 이미지일 경우
            else if (imgList.get(i).getImageUrl()==null && imgList.get(i).getImageUri() !=null) {
                Log.e("게시판이미지 갤러리에서이미지추가", "이미지uri : " + imgList.get(i).getImageUri());

                Uri imageUri =  imgList.get(i).getImageUri();
                // 파일 객체 만들어줌.
                try (InputStream inputStream = resolver.openInputStream(imageUri)) {
                    String originalFileName = getFileNameFromUri(imageUri);
                    String uniqueFileName = java.util.UUID.randomUUID().toString() + "_" + originalFileName;

                    File file = createFileFromInputStream(inputStream, uniqueFileName);
                    String mimeType = getMimeType(file); // file의 mime type 을 반환하는 메소드.

                    // http 요청할때 요청 본문의 내용을 담는 클래스임.
                    // 주로 파일, 문자열, json데이터 등을 서버로 전송할 때 사용됨.
                    RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
                    // file을 포함하는 멀티파트폼 데이터로 서버로 전송할 수 있게 됨. -  for문 전
                    MultipartBody.Part body = MultipartBody.Part.createFormData("images[]", file.getName(), requestFile); // 실제 이미지 파일을 포함하고 있음. name = image
                    imgParts.add(body);

                    // 이미지는 한장 한장 저장되기 때문에 !??
                    Log.e("서버 이미지 저장  ", "서버 이미지저장 요청 body: " + body.headers().toString());
                    Log.d("RequestBody 요청body", requestFile.toString()); // requestFile은 RequestBody 객체

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } // for문 반복문 끝

        // 문자열 데이터를 Request body로 반환 - 한번만 보내면 되는데 create activity 에서는 사진 한장마다 같이 보내졌던 유저 넘버랑 게시글 넘버.
        // 여기서는 한번에 보냄.
        RequestBody boardNumber = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(board_number));
        RequestBody userNumber = RequestBody.create(MediaType.parse("text/plain"), user_number);
        Log.e("서버 요청body boardNumber ", "boardNumber: " + boardNumber.toString());



        // 서버에 이미지 update를 하기 위해서 서버로 전달하는 http 통신 보내기.
        Call<ImageUploadResponse> call = service.UpdateImage(boardNumber, userNumber, imgParts, imgUrls);

        Log.e("이미지 업데이트 onresponse 들어옴 imgUrls","imgUrls : " + imgUrls.size() );

        Log.e("이미지 업데이트 onresponse 들어옴 imgParts", "imgParts : " + imgParts.size());
        for (MultipartBody.Part part : imgParts) {
            Log.e("이미지 업데이트 body", "파일 이름: " + part.headers().toString());
        }
        Log.e("이미지 업데이트 body", "imgParts: " + imgParts.toString());

        call.enqueue(new Callback<ImageUploadResponse>() {
            @Override
            public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                Log.e("이미지 업데이트 onresponse 들어옴","트루임" );

                if(response.isSuccessful()) { // 서버로 부터의 응답이 successful 할때
                    ImageUploadResponse result = response.body(); // result 객체에다가 답변 json body 담기

                    if (result.isSuccess()) {
                        Log.e("이미지 서버저장 업데이트의 boolean값이 true","트루임" );
                    } else {
                        Log.e("이미지 서버저장 업데이트의 boolean값이 false","폴스임" );
                    }
                    Log.e("이미지서버업데이트 메시지: ", result.getMessage() );
                   // Log.e("이미지서버업데이트 어레이 : ", result.getImageUrl() );

//                    List<String> filePaths = result.getFilePaths();
//                    for (String path : filePaths) {
//                        Log.e("이미지 서버업로드한경로: ", path);
//                    }


                    // code: 200 일때 내 서재 프래그먼트로 이동 !! 뒤에 있는 스택 지우고가기
                    Intent intent = new Intent(BoardUpdateActivity.this, MainActivity.class);
                    intent.putExtra("category", selected_board_category);
                    intent.putExtra("fragment", "board");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 위 플래그를 설정하면 스택에 있는 기존 액티비티가 전부 제거됨. ㅇㅎ
                    startActivity(intent);
                    finish();




                }
            }

            @Override
            public void onFailure(Call<ImageUploadResponse> call, Throwable throwable) {
                Log.e("이미지 서버에 업데이트 에러남!! ", throwable.getMessage());
            }
        });

    } // 게시판 수정 - 이미지 (끝)


    // method 4) 이미지의 원래 파일 이름을 얻는 메서드
    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }



    // method 5) 파일 uri 를 가지고 파일을 생성해서 업로드하기. input stream으로 파일을 만들고, content resolver 사용하기.
    private File createFileFromInputStream (InputStream inputStream, String fileName) {
        try {
            File file = new File(getCacheDir(), fileName);
            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // method 6) 이미지 확장자 반환하는 메서드
    private String getMimeType(File file) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getPath());
        if(extension !=null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        Log.e("현재이미지의 타입은? ", "type: " + type);
        return type;
    }


    // method 7) 책 목록 book list (list) 를 json 문자열로 변환하는 메소드
    private String convertBookToJson (List<SearchBookData> bookList) {
        Gson gson = new Gson();
        return gson.toJson(bookList);
    }



}
