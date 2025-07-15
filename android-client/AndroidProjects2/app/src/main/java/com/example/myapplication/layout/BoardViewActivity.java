package com.example.myapplication.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.recyclerview.BoardBookAdapter;
import com.example.myapplication.data.recyclerview.BoardCommentAdapter;
import com.example.myapplication.data.recyclerview.BoardCommentData;
import com.example.myapplication.data.recyclerview.BoardViewImgAdapter;
import com.example.myapplication.data.recyclerview.SearchBookAdapter;
import com.example.myapplication.data.recyclerview.SearchBookData;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.UserNumberCallback;
import com.example.myapplication.data.retrofit.responsemodel.AladinResponse;
import com.example.myapplication.data.retrofit.responsemodel.BoardCommentResponse;
import com.example.myapplication.data.retrofit.responsemodel.BoardLikeResponse;
import com.example.myapplication.data.retrofit.responsemodel.BoardListResponse;
import com.example.myapplication.data.retrofit.responsemodel.BoardResponse;
import com.example.myapplication.data.retrofit.responsemodel.ImageUploadResponse;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.data.retrofit.responsemodel.MyLibraryListResponse;
import com.example.myapplication.data.retrofit.responsemodel.MylibraryResponse;
import com.example.myapplication.data.retrofit.responsemodel.ProfileFollowResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardViewActivity extends AppCompatActivity implements UserNumberCallback  {


    // import
    RetrofitService service; // 레트로핏 서비스

    // 상수
    private static final int MENU_EDIT_ID = R.id.menu_edit;
    private static final int MENU_DELETE_ID = R.id.menu_delete;


    // 쉐어드 프리퍼런스
    PreferenceManager pref; // 쉐어드 프리퍼런스 - 로그인 유지용
    String key = "signin_email_id";
    String current_login_memberInfo; // 쉐어드에 저장된 이메일/아이디, 닉네임 키값쌍.
    String emailId ; // 이메일/아이디만 파싱한 값.




    // 이미지, 책, 댓글 리사이클러뷰
    private RecyclerView img_recyclerView;
    private RecyclerView book_recyclerview;

    private RecyclerView comment_recyclerview;
        
    
    // 이미지 어댑터, 리스트 
    BoardViewImgAdapter imgAdapter; //게시글 상세보기 이미지 리사이클러뷰 어댑터
    ArrayList<ImageUploadResponse> imgList = new ArrayList<>(); 
    
    
    // 책 어댑터, 리스트 
    SearchBookAdapter bookAdapter; // 게시글 상세보기 - 책 어댑터 / 책 검색 어댑터랑 똑같은 거 쓰기.. 음. 일단 그렇게 해봐
    List<SearchBookData> bookList = new ArrayList<SearchBookData>(); // 서버로부터 이미지 가져오기. 


    // 댓글 어댑터, 리스트
    BoardCommentAdapter commentAdapter;
    List<BoardCommentData> commentDataList = new ArrayList<>();



    // view 초기화

    Toolbar toolbar; // 툴바

    // view 초기화 하기 - 제목, 카테고리, 유저 닉네임, 작성일로부터 며칠, 내용, 수정삭제위한 점3 버튼 !!

    TextView board_category, board_title, board_nickname, board_beendays, board_content;
    // 게시판 카테고리, 게시판 제목, 유저 닉네임, 작성일로부터 며칠, 게시판 내용

    // 게시판 댓글 개수
    TextView comment_total_cnt;

    ImageView user_profile_img; // 유저 프사
    ImageView board_more; // 더보기 - 수정, 삭제

    ImageView like_btn; // 좋아요 버튼

    TextView comment_like_cnt;



    // 댓글
    EditText comment_input; // 댓글 작성란
    Button comment_save; // 댓글 작성완료 버튼


    // String
    String user_number ; // 현재 로그인 중인 유저 넘버.
    String nickname; // 유저 닉네임 (현재 로그인한 유저)
    String writer_nickname; // 글쓴이 닉네임
    String writer_emailId; // 글쓴이 이메일 아이디
    String userNumber_string;

    // int
    int board_number, writer_number;
    String category;

    int comment_cnt_int;

    int user_number_int; // 유저넘버 서버에서 가져오는 call back 메소드 안에서 string user number 를 int 값으로 파싱해줌


    // 좋아요 수, 눌렀는지 여부
    int comment_like_cnt_int;
    int like_or_not;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_view);

        // ======================= 초기화 zone 시작 =================================

        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의

        // 툴바
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }

        // view 연결
        board_category = findViewById(R.id.boardview_category);
        board_title = findViewById(R.id.boardview_title);
        board_nickname = findViewById(R.id.boardview_nickname);
        board_beendays = findViewById(R.id.boardview_beenDays);
        board_content = findViewById(R.id.boardview_content);
        comment_total_cnt = findViewById(R.id.comment_total_cnt); // 댓글 개수

        board_more = findViewById(R.id.dropdown_icon); // 더보기 - 수정, 삭제

        user_profile_img = findViewById(R.id.boardview_userImg); // 유저 프사

        // 좋아요 버튼
        like_btn = findViewById(R.id.like_btn);

        // 좋아요 수
        comment_like_cnt = findViewById(R.id.comment_like_cnt);

        // 댓글
        comment_input = findViewById(R.id.comment_input);
        comment_save = findViewById(R.id.comment_send);


        //이미지 리사이클러뷰
        img_recyclerView = findViewById(R.id.board_view_img_recyclerview); 
        

        // 이미지 리사이클러뷰 레이아웃 매니저
        img_recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false ));


        // 이미지 어댑터 setting
        imgAdapter = new BoardViewImgAdapter(imgList, getApplicationContext());
        img_recyclerView.setAdapter(imgAdapter);
        img_recyclerView.scrollToPosition(0);

        
        // ================ 
        
        
        // 책 리사이클러뷰 
        book_recyclerview = findViewById(R.id.board_view_book_recyclerview); 
        
        // 책 레이아웃 매니저 
        book_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // 책 어댑터 setting
      //   book_recyclerview.setVisibility(View.VISIBLE); // 이거를 왜 여기서해 ??? 책 가져와서 해야지.
        bookAdapter = new SearchBookAdapter(bookList, BoardViewActivity.this);
        book_recyclerview.setAdapter(bookAdapter);



        // ================

        // 댓글 리사이클러뷰 !!! 랑 어댑터 연결해주기 ~!!
        comment_recyclerview = findViewById(R.id.board_comment_recyclerview);


        // 댓글 레이아웃 매니저
        comment_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // 댓글 어댑터 setting
        commentAdapter = new BoardCommentAdapter(BoardViewActivity.this,commentDataList );
        comment_recyclerview.setAdapter(commentAdapter);






        // on create 1. 툴바 뒤로가기 클릭 시, 이전 화면으로 (프래그먼트로 나가게 됨.)
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onBackPressed();} });
        // on create 1 끝


        // on create 2. 쉐어드에서 사용자 이메일 아이디 가져오기
        pref = new PreferenceManager();
        current_login_memberInfo = pref.getString(this, key);
        Log.d("게시판 상세보기", "로그인 회원정보from 쉐어드:  " + current_login_memberInfo);

        try {
            JSONObject jsonObject = new JSONObject(current_login_memberInfo);
            nickname = jsonObject.optString("nickname", ""); // nullpoint exception 발생할 수 있으므로, 빈문자열로 두기
            emailId = jsonObject.optString("emailid", "");
            Log.d("내 서재 상세보기", "로그인 회원 이메일: " + emailId);
            get_userNumber(emailId, (UserNumberCallback) this); // 유저 번호 가져오기.

        } catch (JSONException e) {
            e.printStackTrace();
            emailId = ""; // 기본값 설정
        }


        //on create 3. intent 로 넘겨 받은 값  - 게시판 number, 카테고리
        Intent intent = getIntent();
        category = intent.getStringExtra("category");
        String boardNumber_string = intent.getStringExtra("board_number");
         userNumber_string = intent.getStringExtra("user_number");

        Log.e("게시판상세 intent값", "카테고리: " + category + ", 보드번호: " + boardNumber_string + ", 유저번호: " + userNumber_string);

        if (boardNumber_string != null && userNumber_string !=null) {
            board_number = Integer.parseInt(boardNumber_string);
            writer_number = Integer.parseInt(userNumber_string);
            Log.d("게시판 상세보기", "게시판번호:  " + board_number );
        } else {
            Log.e("게시판상세-게시판번호int변환실패", "게시판 번호가 null 입니다.");
        }


        // on create 4. 더보기 버튼 클릭 리스너
        board_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }

        });


        // on create 5. 유저 클릭 이벤트
        View.OnClickListener userInfoClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 보내줘야 할 것.. 유저 닉네임, 유저 번호 ?
                if (writer_nickname.equals(nickname)) {
                    // 본인 게시글의 본인 프로필을 눌렀을 경우.
                    // 메인 액티비티로 보내기. -> 마이페이지로 보내기
                    Intent intent = new Intent(BoardViewActivity.this, MainActivity.class);
                    intent.putExtra("fragment", "myProfile");
                    String writer_number_string = String.valueOf(writer_number);
                    intent.putExtra("user_number", writer_number_string);
                  //  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 위 플래그를 설정하면 스택에 있는 기존 액티비티가 전부 제거됨.
                    startActivity(intent);
//                    finish(); 이거 해줘야 하나 ??

                } else {
                    // 다른 사람의 프로필을 눌렀을 경우.
                    Intent intent = new Intent(BoardViewActivity.this, OthersProfileActivity.class);
                    if(writer_nickname !=null) { // view에 뿌려주는 게시글 내용에 들어갈 닉네임 = 글쓴이 이 null 이 아닌 경우에만. 인텐트에 넣어줌
                        intent.putExtra("writer_nickname",writer_nickname );
                        intent.putExtra("writer_emailId", writer_emailId);
                    } else {
                        Log.e("게시판상세-글쓴이 닉네임 없음", "글쓴이 닉네임 null 입니다.");
                    }
                    String writerNumber_string = String.valueOf(writer_number);
                    intent.putExtra("writer_number", writerNumber_string);
                    startActivity(intent);
                }
            } // on click
        };

        // on create 5-2. 유저 클릭 이벤트 연결해주기 (프사, 닉네임에만)
        user_profile_img.setOnClickListener(userInfoClickListener); // 글쓴이 프사
        board_nickname.setOnClickListener(userInfoClickListener); // 글쓴이 닉네임


        // on create 6. 댓글 작성 완료 버튼 - 레트로핏 호출
        comment_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("댓글 작성 완료 버튼 누름", "글쓴이 number string: " + user_number_int + ", 글쓴이 닉넴: " + nickname);
                // 댓글 내용 작성 됐는지 확인
                // 현재 로그인 중인지 - 확인. 유저 넘버 가져가기 int
                // 작성자 번호, 작성한 내용가져가기
                if(user_number ==null) { // user number 서버로부터 가져온 유저 넘버임.
                    // 토스트 - 로그인 후 댓글 작성이 가능합니다.
                    Toast.makeText(BoardViewActivity.this, "로그인 후 댓글 작성이 가능 합니다.", Toast.LENGTH_SHORT).show();
                } else {

                    // 로그인 완료. 댓글 작성 가능함.
                    if(comment_input.getText() == null) { // 댓글 내용 비었을 경우
                        // 토스트 - 댓글을 작성 해주세요
                        Toast.makeText(BoardViewActivity.this, "댓글을 작성해 주세요", Toast.LENGTH_SHORT).show();

                    } else {
                        Log.e("댓글 작성 가능- 글쓴이번호", "글쓴이번호: " + user_number_int);
                        Log.e("댓글 작성 가능- 댓글 내용", "댓글 작성 내용: " + comment_input.getText());
                        String comment_content = comment_input.getText().toString();
                        Log.e("댓글 작성 가능- 게시글번호", "intent로넘어온 게시글번호int값: " + board_number);

                        // 댓글 작성 레트로핏 메소드 실행하기
                        board_comment_insert(user_number_int, board_number,comment_content);
                    }
                } // 로그인 확인 됨.

            }
        });


        // on create 7. 게시글 글쓴이, 현재 로그인한 유저가 다를 때는 수정 버튼 안보이게
        if(!nickname.equals(writer_nickname)) {
            board_more.setVisibility(View.GONE); // 안보이게
        }




        // on create 8. 좋아요 버튼 클릭 이벤트쓰
        like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BoardViewActivity.this, "조아요 클릭쓰", Toast.LENGTH_SHORT).show();

                Drawable currentDrawable = like_btn.getDrawable();
                Drawable iconLineHeart = getResources().getDrawable(R.drawable.icon_line_heart);
                Drawable iconFilledHeart = getResources().getDrawable(R.drawable.icon_filled_heart);

                if(currentDrawable.getConstantState().equals(iconLineHeart.getConstantState())) { // 좋아요 누름
                    Toast.makeText(BoardViewActivity.this, "조아요 클릭쓰", Toast.LENGTH_SHORT).show();


                    like_btn.setTag("clicked_like");
                    // 태그도 보내기 - 좋아요를 눌렀을때.
                    board_like(user_number_int, board_number,like_btn.getTag().toString());


                } else if (currentDrawable.getConstantState().equals(iconFilledHeart.getConstantState())) { // 좋아요 취소 누름
                    Toast.makeText(BoardViewActivity.this, "조아요 취소 클릭쓰", Toast.LENGTH_SHORT).show();


                    like_btn.setTag("clicked_unlike");
                    // 태그도 보내기 - 좋아요 취소를 눌렀을때.
                    board_like(user_number_int, board_number,like_btn.getTag().toString());

                }


            }
        });


    } // on create 끝


    // 메소드1. 유저 넘버 가져오는 메소드 (레트로핏)
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
    }


    // 메소드 2-1). DB에서 내용 가져오는 메소드
    public void board_view_setting (String user_number, int board_number, String category) {
        Log.e("게시판<전체>에서 상세보기 http통신 메소드진입", " 유저번호: " + user_number + ", 게시판번호: " + board_number + ", 카테고리: " + category);
        service.board_view(user_number,board_number, category).enqueue(new Callback<BoardListResponse>() {
            @Override
            public void onResponse(Call<BoardListResponse> call, Response<BoardListResponse> response) {

                // 가져온 내용 뿌려주기 !
                BoardListResponse result = response.body();
                Log.e("게시판 <전체> 상세보기 http 성공!!", " reponse 진입함" );
                Log.e("게시판 <전체> 상세보기 http 성공!!", " php 응답코드: " + result.getCode() + ", 메시지: " + result.getMessage());
                Log.i("게시판 <전체> 상세보기 http 성공!!", "유저번호: "+ result.getUser_number() + ", 유저 이멜아이디: " + result.getUser_emailID() +  ",유저 글쓴이 프사: "+ result.getUser_profile_img() +  ",게시판번호: "+ result.getBoard_number() + ", 카테고리: " + result.getCategory()
                        + ", title: " + result.getTitle() + ", 내용: " + result.getContent() + "작성날짜: " + result.getCreatedTime() + ", 댓글개수: " + result.getComment_cnt());

                // 게시판 상세보기 setting 하기
                board_category.setText(result.getCategory()); // 게시판 상세 카테고리
                board_title.setText(result.getTitle()); // 게시판 제목
                writer_nickname = result.getUser_nickname();
                writer_emailId = result.getUser_emailID();
                board_nickname.setText(writer_nickname); // 글쓴이 닉네임
                board_beendays.setText(result.getCreatedTime());
                board_content.setText(result.getContent());
                comment_cnt_int = result.getComment_cnt(); // 댓글 개수
                comment_like_cnt_int = result.getLike_cnt(); // 좋아요 개수


                // 글쓴이 프사 있을때
                if (result.getUser_profile_img() != null) {
                    Log.e("글쓴이 유저 프사 있음 ", "프사 있음" );

                    Glide.with(BoardViewActivity.this)
                            .load((String) null) // 기존 이미지를 초기화
                            .into(user_profile_img);

                    String img_serverAddress = "http://3.39.255.234/php/img/";
                    String userProfileImg = result.getUser_profile_img();
                    String img_url = img_serverAddress+userProfileImg;

                    Glide.with(BoardViewActivity.this)
                            .load(img_url)
                            .apply(RequestOptions.circleCropTransform()) // 원형 변환 적용
                            .into(user_profile_img);


                }

                // 좋아요 여부
                like_or_not = result.isLike_or_not(); // 좋아요 눌렀는지 안눌렀는지
                if (like_or_not ==1) { // 이미 눌렀음.
                    like_btn.setImageResource(R.drawable.icon_filled_heart);
                } else { // 아직 안 눌렀음.
                    like_btn.setImageResource(R.drawable.icon_line_heart);
                }


                comment_total_cnt.setText("댓글 " + comment_cnt_int + "개") ; // 댓글 개수. 0일수도 있음
                comment_like_cnt.setText("공감해요 " + comment_like_cnt_int + "개");
                List<AladinResponse> bookItem = result.getBooks();
                List<BoardCommentResponse> commentItem = result.getComments();
                bookAdapter.clearItem(); // 일단 한번 지워줌
                book_recyclerview.setVisibility(View.VISIBLE); // 게시글 상세보기에 있는 리사이클러뷰 visiblity


                // 내 게시글일때에만 수정, 삭제 가능하게. ㄷ
                if(nickname.equals(writer_nickname)) {
                Log.e(" 내 글임!! ,로그인,작성자 닉넴같", "로그인닉넴: " + nickname + ", 게시글작성자닉넴: " + writer_nickname);
                    board_more.setVisibility(View.VISIBLE);
                } else {
                    Log.e(" 내 글 아님,로그인,작성자 닉넴 다름!!", "로그인닉넴: " + nickname + ", 게시글작성자닉넴: " + writer_nickname);
                    board_more.setVisibility(View.GONE);
                }


                if (bookItem !=null) { // 책이 없는 경우도 있음
                    for (AladinResponse item : bookItem ) {
                        String bookTitle = item.getTitle();
                        String bookAuthor = item.getAuthor();
                        String bookDescription = item.getDescription();
                        String bookCover = item.getCover();
                        bookList.add(new SearchBookData(bookTitle,bookAuthor,bookDescription,null,null,bookCover,null));
                        bookAdapter.notifyDataSetChanged();
                    }
                }
                commentAdapter.clearItem(); // 일단 한번 지워줌. ㄷㅂ
                // 댓글 있는 경우
                if(commentItem != null) {
                    for (BoardCommentResponse item : commentItem ) {
                        int comment_number = item.getComment_number();
                        String profile_img = item.getUser_profileImg();
                        int com_user_number = item.getUser_number();
                        String user_nick = item.getUser_nickname();
                        String comment_content = item.getComment_content();
                        String comment_created = item.getCreatedTime();
                        String comment_update = item.getUpdateTime();
                        commentDataList.add(new BoardCommentData(comment_number, profile_img, com_user_number, user_nick, comment_content, comment_created, comment_update));
                        commentAdapter.notifyDataSetChanged();
                    }
                    Log.e("현재게시글 댓글개수", "댓글개수: " + comment_cnt_int + ", commentDataList 길이: " + commentDataList.size());
                }

            } // 게시판 기존 내용 + 책 내용 가져오는 on response 끝

            @Override
            public void onFailure(Call<BoardListResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("게시판 <전체> 상세보기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("게시판 <전체> 상세보기 실패. ", "onFailure: " + throwable.getCause());
            }
        });
    } // on failure 끝



    // 메소드 2-2). DB에서 사진 가져오는 메소드
    public void boardImage_setting (String user_number, int board_number) {
        Log.e("게시글 이미지 가져오기 레트로핏 들어옴", "레트로핏 들어옴 보드번호: " + board_number );

        service.boardView_image(user_number, board_number).enqueue(new Callback<ImageUploadResponse.ImageUploadResponse2>() {
            @Override
            public void onResponse(Call<ImageUploadResponse.ImageUploadResponse2> call, Response<ImageUploadResponse.ImageUploadResponse2> response) {
                // 이미지 가져오기. 목록으로 가져올 것임.
                //사진이 없을 때는 그냥 아무일도 일어나지 않고, 사진이 있을때에만 리사이클러뷰 세팅 되도록.
                // 목록에 대해 리사이클러뷰 setting 하기.
                Log.e("게시글 이미지 가져오기 통신성공", " reponse 진입함" );
                ImageUploadResponse.ImageUploadResponse2 result = response.body();

                if(result.getMessage().equals("사진 없습니다")) {
                  //걍 아무 일도 일어나지 않음...로그 찍기
                    Log.e("이 게시글에는 이미지 없음", "이미지 없는 게시글임" );
                    imgAdapter.clearItem();
                } else {
                    Log.e("이 게시글에는 이미지 있어 !!! ", "이미지 있음!!!" );
                    // 각각 이미지의 내용들에 대한 상세정보-게시글번호,글쓴이, url(이미지 이름)
                    List<ImageUploadResponse> image_item = result.getImages();
                    imgAdapter.clearItem();
                    // 리사이클러뷰에 이미지 출력하기.
                    img_recyclerView.setVisibility(View.VISIBLE);

//                  img_recyclerView.smoothScrollToPosition(0); it's not working
                    for (ImageUploadResponse item : image_item) {
                        // 이미지 고유 번호, 게시글 번호, 이미지 서버 url(name만 !!)
                        int board_image_number = item.getBoard_image_number();
                        int board_number = item.getBoard_number();
                        String imageURL = item.getImageUrl();
                        imgAdapter.addItem(new ImageUploadResponse(imageURL, null, board_image_number, board_number));
                        Log.e("게시글에 있는 이미지 정보", "이미지번호: " + board_image_number + ",게시글번호: " + "이미지url: " + imageURL );

                    } // for문 종료

                } // 사진이 있을 때
            } // on response 끝

            @Override
            public void onFailure(Call<ImageUploadResponse.ImageUploadResponse2> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("사진 가져오기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("사진 가져오기 실패. ", "onFailure: " + throwable.getCause());
            }
        });
    }



    // 메소드 3. 드롭 다운 메뉴 띄우기
    private void showPopupMenu(View board_more) {
        PopupMenu popupMenu = new PopupMenu(this, board_more);
        popupMenu.getMenuInflater().inflate(R.menu.dropdown_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == MENU_EDIT_ID) {// 수정 버튼 클릭 시 동작
                    Toast.makeText(BoardViewActivity.this, "수정 버튼 클릭됨", Toast.LENGTH_SHORT).show();

                    // Alert Dialog 사용하기
                    // 정말 수정하시겠습니까 ? 물어보기
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardViewActivity.this);
                    builder.setTitle("게시글을 수정할까요?")
                            .setMessage("수정을 원할 시 확인 버튼을 눌러주세요")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 수정 페이지로 이동하기.
                                    Toast.makeText(BoardViewActivity.this, "수정 페이지로 이동합니다!! 보드번호: " + board_number, Toast.LENGTH_SHORT).show();

                                    // 수정 페이지로 이동할때, 게시글 번호랑 카테고리 들고가깅.
                                    Intent intent = new Intent(BoardViewActivity.this, BoardUpdateActivity.class);
                                    intent.putExtra("board_number", board_number);
                                    intent.putExtra("category", category);
                                    startActivity(intent);

                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                    return true;
                } else if (itemId == MENU_DELETE_ID) {// 삭제 버튼 클릭 시 동작
                    Toast.makeText(BoardViewActivity.this, "삭제 버튼 클릭됨", Toast.LENGTH_SHORT).show();
                    // 정말 삭제하시겠습니까? 물어보기
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardViewActivity.this);
                    builder.setTitle("게시글 삭제")
                            .setMessage("게시판에서 삭제 시, 복구가 불가능합니다. \n 정말 게시글을 삭제할까요?")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 삭제하는 메소드 호출하기
                                    board_delete(user_number, board_number, category);
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }


    // 메소드 4.
    // = =================================== 유저 넘버 가져오는 레트로핏에 대한 call back 인터페이스 메소드 !!
    @Override
    public void onUserNumberReceived(String userNumber) {
        this.user_number = userNumber;
        Log.e("유저 number 성공 두번째증거", "유저넘버: " + userNumber); // echo로 보내주는 메시지.

        user_number_int = Integer.parseInt(userNumber);
        // 서버에서 내용가져오기.
       board_view_setting(user_number, board_number, category);
        // user number = String / book number = int / mylibrary number = int

        // 서버에서 이미지 가져오기.
        boardImage_setting(user_number, board_number);



    } // onUserNumberReceived 끝

    @Override
    public void onError(String errorMessage) {
        Log.d("유저 number 실패", "에러메시지:  " + errorMessage);

    }

    // = =================================== 유저 넘버 가져오는 레트로핏에 대한 call back 메소드 끝 끝 끝  !!


    // 메소드 5. 게시글 삭제하기
    public void board_delete (String user_number, int board_number, String category) {
        Log.d("게시판삭제 진입", "현재유저번호: " + user_number + ", 게시글번호: "+ board_number + ", 카테고리: " + category );
        service.board_delete(user_number, board_number, category).enqueue(new Callback<BoardResponse>() {
            @Override
            public void onResponse(Call<BoardResponse> call, Response<BoardResponse> response) {
                BoardResponse result = response.body();
                Log.e("게시판 <상세>에서 게시글삭제!!! 통신 성공 !!! ", " php가 보낸응답: " + result.getCode() + ", 메시지: " + result.getMessage());
                finish(); // 이러면 view에서 목록으로 돌아가야함!! view activity를 종료하기 때문에 !!
                // on resume으로 다시 돌아감.

            }

            @Override
            public void onFailure(Call<BoardResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("게시판 상세보기에서 삭제 실패: ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
            }
        });
    } // 게시판 delete 함수 끝



    // 메소드 6. 댓글 작성하기
    public void board_comment_insert (int user_number, int board_number, String comment_content) {
        Log.d("댓글 작성 진입", "현재유저번호: " + user_number + ", 게시글번호: "+ board_number + ", 댓글내용: " + comment_content );

        service.board_comment_insert(user_number, board_number, comment_content).enqueue(new Callback<BoardCommentResponse>() {
            @Override
            public void onResponse(Call<BoardCommentResponse> call, Response<BoardCommentResponse> response) {
                Log.e("게시판 댓글작성!!! 통신 성공 !!! ", " 서버가 보낸응답: " + response.message());

                BoardCommentResponse result = response.body();
                Log.e("게시판 댓글작성!!! 통신 성공 !!!!! ", " php보낸 메시지: " + result.getMessage() );
                Log.e("게시판 댓글작성!!! 통신 성공 !!! ", " 댓글 작성자번호: " + result.getUser_number() );
                Log.e("게시판 댓글작성!!! 통신 성공 !!!! ", " 댓글 작성자 nickname: " + result.getUser_nickname() );

                comment_input.setText(""); // 댓글 작성 완료 후에 댓글 입력창 클리어 해주기

                // 리사이클러뷰에 세팅해주기.
               int com_user_number = result.getUser_number(); // 작성자 번호
                int comment_number = result.getComment_number();
               String profile_img = result.getUser_profileImg();  // 작성자 프사
                if (profile_img == null) {
                    Log.e("프사 없는 작성자 ", " 작성자 프사없음" );
                    profile_img = null;
                }
                String user_nick = result.getUser_nickname(); // 작성자 닉넴
                String comment_content = result.getComment_content(); // 작성 댓글 내용
                String comment_created = result.getCreatedTime(); // 댓글 작성 시간
                String comment_update = result.getUpdateTime(); // 댓글 수정 시간.

                comment_cnt_int++;
                comment_total_cnt.setText("댓글 "+ comment_cnt_int + "개");

                if(comment_content != null) {
                    commentDataList.add(new BoardCommentData(comment_number, profile_img, com_user_number, user_nick, comment_content, comment_created, comment_update));
                   commentAdapter.notifyDataSetChanged();

                    // 최근 추가된 댓글로 이동하기.
                   // commentAdapter.notifyItemInserted(commentDataList.size()-1);
                    comment_recyclerview.scrollToPosition(commentDataList.size()-1);
                }

            }

            @Override
            public void onFailure(Call<BoardCommentResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("게시판 댓글 작성 실패: ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
            }
        });
    } // 메소드 6 끝


    // 메소드 7. 게시글 좋아요
    public void board_like (int user_number, int board_number, String status_tag) {
        Log.e("좋아요 기능 레트로핏 메소드진입", "현재 유저번호: " + user_number + ", 현재게시글번호: " + board_number + ", 좋아요상태: " + status_tag);
        service.board_like(user_number, board_number,status_tag).enqueue(new Callback<BoardLikeResponse>() {
            @Override
            public void onResponse(Call<BoardLikeResponse> call, Response<BoardLikeResponse> response) {
                //좋아요 수 업데이트해야함. 음. 이건 어떻게 하는거지 ???
                Log.e("좋아요 통신성공! 서버가 보내는 코드", "code: " + response.code()); //서버가 보내는 http 통신 응답코드
                BoardLikeResponse result = response.body();
                Log.e("좋아요 php 메시지", "message: " + result.getMessage()); //서버가 보내는 http 통신 응답코드
                Log.e("좋아요 php 현재 상태", "status: " + result.getStatus_now());

                // 좋아요 한경우
                if (result.getStatus_now().equals("clicked_like")) { // 좋아요
                    like_btn.setImageResource(R.drawable.icon_filled_heart);
                    comment_like_cnt_int++;
                    comment_like_cnt.setText("공감해요 " + comment_like_cnt_int + "개");


                } else if (result.getStatus_now().equals("clicked_unlike")) { // 좋아요 취소
                    like_btn.setImageResource(R.drawable.icon_line_heart);
                    comment_like_cnt_int--;
                    comment_like_cnt.setText("공감해요 " + comment_like_cnt_int + "개");

                }


            }

            @Override
            public void onFailure(Call<BoardLikeResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("좋아요 실패: ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
            }
        });

    }
    




}
