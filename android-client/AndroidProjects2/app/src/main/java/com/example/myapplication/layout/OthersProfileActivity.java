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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.recyclerview.BoardData;
import com.example.myapplication.data.recyclerview.BoardWholeAdapter;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.UserNumberCallback;
import com.example.myapplication.data.retrofit.responsemodel.BoardListResponse;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.data.retrofit.responsemodel.ProfileFollowResponse;
import com.example.myapplication.data.retrofit.responsemodel.ProfileViewResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OthersProfileActivity extends AppCompatActivity  implements UserNumberCallback, onLikeBtnClick {



    /// 인텐트로 넘어온 정보가, 현재 로그인 한 유저의 정보 (내 정보) 인지 아닌지 !!!
    // 내 정보라면, 마이 페이지로 보내버리고 아닐 경우에만 정보 가져와서
    // 각각 요소에 대한 클릭 이벤트, 팔로우 팔로잉 이벤트 진행 시키기 !!
    // 오늘 안에 끝날 것 같음 히히


    // import
    RetrofitService service; // 레트로핏 서비스


    // 쉐어드 프리퍼런스
    PreferenceManager pref; // 쉐어드 프리퍼런스 - 로그인 유지용
    String key = "signin_email_id";
    String current_login_memberInfo; // 쉐어드에 저장된 이메일/아이디, 닉네임 키값쌍.
    String emailId ; // 이메일/아이디만 파싱한 값.
    String nickname; // 로그인한 유저 닉네임

    String user_number;// 현재 로그인 중인 유저 넘버
    int user_number_int; // 로그인 중인 유저 넘버 int 값 - call back 메소드에서 값 넣어주기


    // 팔로워 수, 팔로잉 수 - 레트로핏에서 가져와서 setting 해준 수
    int follower_cnt, following_cnt;


    // view 초기화
    Toolbar toolbar; // 툴바
    // String
    String writer_nickname; // 해당 프로필 유저의 닉네임 from intent
    int writer_number; // 해당 프로필의 유저 넘버 from intent (parse int 해준거임)
    String writer_number_st;
    String writer_emailId;

    Button profile_follow_btn, profile_chatting_btn; // 팔로우 버튼, 1:1 채팅버튼


    ImageView user_profile_img; // 다른 유저 프사
    TextView user_nickname, user_follower_cnt, user_following_cnt, user_description, read_cnt, reading_cnt, want_cnt;
    // 유저 닉네임, 팔로워수, 팔로잉수, 유저 소개글, 읽은책, 읽고 있는책, 읽고 싶은책



    // 작성자 닉넴 추가, 작성한 게시글 전체 개수
    TextView board_board, profile_board_cnt;


    // 작성한 게시글 리사이클러뷰 및 어댑터
    RecyclerView profile_board_list_recyclerview;
    BoardWholeAdapter wholeAdapter;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.others_profile_activity);


        // ======================= 초기화 zone 시작 =================================

        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의


        // 툴바
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }


        // view 연결해주기
        user_profile_img = findViewById(R.id.profile_img); // 유저 프사
        user_nickname = findViewById(R.id.profile_nickname); // 유저 닉네임
        user_follower_cnt = findViewById(R.id.profile_follower_cnt); // 팔로워수
        user_following_cnt = findViewById(R.id.profile_following_cnt); // 팔로잉수
        user_description = findViewById(R.id.profile_user_description); // 유저 소개


        board_board = findViewById(R.id.board_board); // 현재 프로필 주인 게시글입니다
        profile_board_cnt = findViewById(R.id.profile_board_cnt); // 현재 프로필 주인 게시글 개수


        read_cnt = findViewById(R.id.profile_read_cnt); // 읽은 책 수
        reading_cnt = findViewById(R.id.profile_reading_cnt); // 읽고 있는 책 수
        want_cnt = findViewById(R.id.profile_want_cnt); // 읽고 싶은 책 수


        // button
        profile_follow_btn = findViewById(R.id.profile_follow_btn); // 팔로우 버튼
        profile_chatting_btn = findViewById(R.id.profile_chatting_btn); // 1:1 채팅 버튼


        // 팔로우 버튼 임시 세팅
        profile_follow_btn.setBackground(getDrawable(R.drawable.shape_rectangle_green_background));
        profile_follow_btn.setText("팔로우 하기");
        profile_follow_btn.setTag("not_following");


        // 리사이클러뷰
        profile_board_list_recyclerview = findViewById(R.id.profile_board_list_recyclerview);

        // 어댑터
        wholeAdapter = new BoardWholeAdapter(OthersProfileActivity.this, profile_board_list_recyclerview, this );

        profile_board_list_recyclerview.setLayoutManager(new LinearLayoutManager(OthersProfileActivity.this, LinearLayoutManager.VERTICAL, false));
        profile_board_list_recyclerview.setAdapter(wholeAdapter);




        // ======================= 초기화 zone 끝 =================================

        // on create 1. 툴바 뒤로가기 클릭 시, 이전 화면으로 (프래그먼트로 나가게 됨.)
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onBackPressed();} });


        // on create 2. intent 로 닉네임 넘어옴

        Intent getIntent = getIntent();

        if(getIntent != null) {
            writer_nickname = getIntent.getStringExtra("writer_nickname"); // String
            writer_number_st = getIntent.getStringExtra("writer_number");
            writer_number = Integer.parseInt(getIntent.getStringExtra("writer_number")); // int
            writer_emailId = getIntent.getStringExtra("writer_emailId"); // string
            Log.d("다른유저정보 인텐트 닉네임받음!", "writer nickname: " + writer_nickname + ", writer_number: " + writer_number + ", writer_emailId: " + writer_emailId );
            // 받은 유저번호, 닉네임을 가지고 !!! 통신으로 데이터 베이스에서 유저 정보 가져오기.. 가져와야 하는거 많당..
           // get_users_info(user_number_int, writer_number, writer_nickname); // 다른 유저의 정보 가져오기.
        } else {
            Log.d("다른유저정보 인텐트안넘어옴", "인텐트 안넘어옴 -> writer nickname:  " + writer_nickname);
        }



        // on create 3. 쉐어드에서 사용자 이메일 아이디 가져오기
        pref = new PreferenceManager();
        current_login_memberInfo = pref.getString(this, key);
        Log.d("게시판 상세보기", "로그인 회원정보from 쉐어드:  " + current_login_memberInfo);

        try {
            JSONObject jsonObject = new JSONObject(current_login_memberInfo);
            nickname = jsonObject.optString("nickname", ""); // nullpoint exception 발생할 수 있으므로, 빈문자열로 두기
            emailId = jsonObject.optString("emailid", "");
            Log.d("내 서재 상세보기", "로그인 회원 이메일: " + emailId);
            get_userNumber(emailId,  (UserNumberCallback) this); // 유저 번호 가져오기.

        } catch (JSONException e) {
            e.printStackTrace();
            emailId = ""; // 기본값 설정
        }


        // on create 4. 팔로우 버튼
        profile_follow_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("follow btn누름 !! ", "일단누름 " );
                // 버튼 모양 바뀌게
                // 현재 버튼 background 가 뭐냐에 따라 바뀌게끔
                if ("not_following".equals(profile_follow_btn.getTag())){
                    Log.d("팔로우 - 팔로우버튼 누름", "팔로우 누름-흰색으로변함 " );

                    profile_follow_btn.setTag("following");
                    follow(user_number_int, writer_number,profile_follow_btn.getTag().toString() );

                } else {
                    Log.d("언팔 - 팔로우버튼 누름", "팔로우 누름-초록색으로변함 " );

                    profile_follow_btn.setTag("not_following");
                    follow(user_number_int, writer_number,profile_follow_btn.getTag().toString() );
                }

                // 여기서, 태그를 들고 들어가야 하나 ?? 태그랑, 내번호 해당유저번호 세개 들고가기.
                // int: user_number_int, writer_number
                // String : tag
                // tag가 following일때 update insert 팔로우 상태를 true로
                // not following 일때 팔로우 상태를 false로.
            }
        });


        // on create 5. 채팅 버튼
        profile_chatting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("1:1채팅하기 버튼누름!!", "채팅하기 버튼 !! " );

                Intent intent = new Intent(getApplicationContext(), ChattingViewActivity.class);
                intent.putExtra("FROM_WHERE", "FROM_OTHER_PROFILE");
                intent.putExtra("this_user_number", writer_number_st);
                intent.putExtra("this_user_nickname", writer_nickname);
                intent.putExtra("this_user_emailID", writer_emailId);
                intent.putExtra("login_user_number", user_number);
                intent.putExtra("login_user_nickname", nickname);
                intent.putExtra("login_user_emailId", emailId);
                startActivity(intent);
            }
        });




        // 팔로우 목록 - 팔로워, 팔로잉
        // on create 6-1) 팔로워
        user_follower_cnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("팔로워목록보기!!클릭", "팔로워목록 주인번호(string값): " + user_number); // 레트로핏으로 가져온 유저 넘버.
                Intent intent = new Intent(OthersProfileActivity.this, UserFollowFollowingListActivity.class); // 이동하기
                intent.putExtra("user_number", writer_number_st);
                intent.putExtra("type", "follower");
                startActivity(intent);
            }
        });


        // on create 6-2) 팔로잉
        user_following_cnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("팔로잉목록보기!!클릭", "팔로워목록 주인번호(string값): " + user_number); // 레트로핏으로 가져온 유저 넘버.
                Intent intent = new Intent(OthersProfileActivity.this, UserFollowFollowingListActivity.class); // 이동하기
                intent.putExtra("user_number", writer_number_st);
                intent.putExtra("type", "following");
                startActivity(intent);
            }
        });


        // on create 7. 서재 목록
        // 7-1) 읽은 책
        read_cnt.setOnClickListener( v -> {
            Log.d("읽은 책 목록보기!!클릭", "책목록 주인번호(string값): " + user_number); // 레트로핏으로 가져온 유저 넘버.
            Intent intent = new Intent(OthersProfileActivity.this, UserLibraryListActivity.class);
            intent.putExtra("user_number", writer_number_st);
            intent.putExtra("type", "read");
            startActivity(intent);
        });

        // 7-2) 읽고 있는 책
        reading_cnt.setOnClickListener( v -> {
            Log.d("읽고 있는 책 목록보기!!클릭", "책목록 주인번호(string값): " + user_number); // 레트로핏으로 가져온 유저 넘버.
            Intent intent = new Intent(OthersProfileActivity.this, UserLibraryListActivity.class);
            intent.putExtra("user_number", writer_number_st);
            intent.putExtra("type", "reading");
            startActivity(intent);
        });

        // 7-3) 읽고 싶은 책
        want_cnt.setOnClickListener( v -> {
            Log.d("읽고 싶은 책 목록보기!!클릭", "책목록 주인번호(string값): " + user_number); // 레트로핏으로 가져온 유저 넘버.
            Intent intent = new Intent(OthersProfileActivity.this, UserLibraryListActivity.class);
            intent.putExtra("user_number", writer_number_st);
            intent.putExtra("type", "want");
            startActivity(intent);
        });

        // on create 8. 작성한 게시글 가져오기
        profile_board_list(writer_number);

    } // on create 끝



    // 메소드2. 유저 넘버 가져오는 메소드 (레트로핏)
    public void get_userNumber (String user_emailid,  UserNumberCallback callback) {
        Log.i("게시판상세- 유저넘버가져오기", "유저의 emailId: " + user_emailid);
        service.user_emailId(user_emailid).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("게시판상세-유저 number 체크 response 진입", "onResponse: 응답메시지: " + result.getMessage()); // echo로 보내주는 메시지.
                Log.e("게시판상세-유저 number 체크 response 진입", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.

                if(result.getCode() == 200 ) {
                    Log.e("유저 number 성공 첫번째증거", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
//                    user_number = result.getUser_number();
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


    // 메소드3. 다른 사용자에 대한 정보 가져오기.
    public void get_users_info (int user_number, int this_users_number, String this_nickname) {
        Log.i("다른유저정보_가져오기 레트로핏", "다른유저번호(내 번호아님. 달라야함): " + this_users_number + ", 로그인한유저번호: " + user_number);
        service.other_profile_info(user_number, this_users_number, this_nickname).enqueue(new Callback<ProfileViewResponse>() {
            @Override
            public void onResponse(Call<ProfileViewResponse> call, Response<ProfileViewResponse> response) {
                ProfileViewResponse result = response.body();
                Log.e("다른 유저 정보 가져오기 성공 ", " reponse 진입함" );
                Log.e("다른 유저 정보 가져오기 응답코드php "," reponse 코드: " + response.message() );
                Log.e("다른 유저 정보 가져오기 정보 "," 유저번호: " + result.getMessage() + ", 유저닉네임: " + result.getUser_nickname() + ", 유저 읽은책: " + result.getUser_readBook_cnt() + ", 읽고싶: " + result.getUser_wantBook_cnt() +",읽고 있는: " + result.getUser_readingBook_cnt() + ", 맞팔 여부: " + result.getFollow_each_other());
                if(result.getProfile_img() == null){
                    Log.e("다른 유저 정보-프사없음 ", "프사없음" );
                } else {
                    Log.e("다른 유저 정보-프사있음 ", "프사있음" );
                    // 있을 때만 setting 해주기

                    Glide.with(OthersProfileActivity.this)
                            .load((String) null) // 기존 이미지를 초기화
                            .into(user_profile_img);

                    Log.e("내정보가져오기 -프사있음 ", "프사있음" );

                    String img_serverAddress = "http://3.39.255.234/php/img/";
                    String userProfileImg = result.getProfile_img();
                    String img_url = img_serverAddress+userProfileImg;
                    Glide.with(OthersProfileActivity.this)
                            .load(img_url)
                            .apply(RequestOptions.circleCropTransform())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(user_profile_img);
                }

                if(result.getFollow_status_now() == null) {
                    Log.e("다른 유저 정보 가져오기 팔로우 상태 ","기존 팔로우 기록 없음.");
                } else {
                    Log.e("다른 유저 정보 가져오기 팔로우 상태 ", "기존기록있음 팔로우여부: " + result.getFollow_status_now().toString());


                    // 기존 기록에 따라서 팔로우 정보 setting 해주기.
                    if(result.getFollow_status_now().equals("0")) { // 팔로우 안돼있음.
                        Log.d("언팔 - 팔로우버튼 누름", "팔로우 누름-초록색으로변함 " );
                        profile_follow_btn.setBackground(getDrawable(R.drawable.shape_rectangle_green_background));
                        profile_follow_btn.setText("팔로우 하기");
                        profile_follow_btn.setTextColor(getColor(R.color.white));
                        profile_follow_btn.setTag("not_following");
                        profile_chatting_btn.setVisibility(View.GONE);
                   } else { // 이미 팔로우 돼 있음. "1"
                        profile_follow_btn.setBackground(getDrawable(R.drawable.shape_rectangle_black_line));
                        profile_follow_btn.setText("팔로잉 중");
                        profile_follow_btn.setTextColor(getColor(R.color.black));
                        profile_follow_btn.setTag("following");

                        // 내가 이미 팔로우 돼 있는 경우에는 상대도 나를 팔로우 했는지, 즉 맞팔인지 　
                        // 맞팔인 경우에 버튼 보이게 해줌.
                        if(result.getFollow_each_other() == 1) {
                            profile_chatting_btn.setVisibility(View.VISIBLE);
                        }

                    }
                } // 기존 기록이 있을때 끝
                Log.e("다른 유저 정보 가져오기 팔로우수", "팔로우수: " + result.getFollowing_cnt());
                Log.e("다른 유저 정보 가져오기 팔로워 수", "팔로워수: " + result.getFollower_cnt());

                follower_cnt = result.getFollower_cnt();
                following_cnt = result.getFollowing_cnt();

                // view 세팅 해주기
                user_nickname.setText(result.getUser_nickname());
                user_description.setText("테이블 추가해야함");
                read_cnt.setText(String.valueOf(result.getUser_readBook_cnt()));
                reading_cnt.setText(String.valueOf(result.getUser_readingBook_cnt()));
                want_cnt.setText(String.valueOf(result.getUser_wantBook_cnt()));



                Log.e("다른 유저 정보,현재유저는 ", "현재 유저는??? 유저넘버(string): " + user_number);

                    user_follower_cnt.setText("팔로워 " + follower_cnt+"명" );
                    user_following_cnt.setText("팔로잉 " +following_cnt+"명" );


            }

            @Override
            public void onFailure(Call<ProfileViewResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("다른 유저 정보 가져오기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("다른 유저 정보 가져오기 실패. ", "onFailure: " + throwable.getCause());

            }
        });


    } // 메소드 3 끝

 // =================================== 유저 넘버 가져오는 레트로핏에 대한 call back 인터페이스 메소드 !!
// 메소드 4. 현재 로그인 중인 유저 넘버 가져오는 call  back 메소드
    @Override
    public void onUserNumberReceived(String userNumber) {
        this.user_number = userNumber;
        Log.e("유저 number 성공 두번째증거", "유저넘버: " + userNumber); // echo로 보내주는 메시지.
        user_number_int = Integer.parseInt(userNumber); // int 값도 setting 해주기
        get_users_info(user_number_int, writer_number, writer_nickname);
    } // onUserNumberReceived 끝
//

// 메소드 4-2. call back 인터페이스에 정의해놓은 메소드. 필수로 구현해줘야함.
    @Override
    public void onError(String errorMessage) {
        Log.d("유저 number 실패", "에러메시지:  " + errorMessage);

    }



    // 메소드 5. 팔로우 기능 레트로핏
    public void follow (int user_number, int this_user_number, String status_tag) {
        Log.e("팔로우 기능 서버전송 메소드진입", "로그인유저번호: " + user_number + ", 상대유저번호: " + this_user_number + ", 팔로우상태: " + status_tag);

        service.other_follow(user_number, this_user_number,status_tag).enqueue(new Callback<ProfileFollowResponse>() {
            @Override
            public void onResponse(Call<ProfileFollowResponse> call, Response<ProfileFollowResponse> response) {
                // 팔로우 수도 업데이트해야함. 음. 이건 어떻게 하는거지 ???
                // 처음 view 데이터를 뿌려줄때 가져온 다음에 기존 데이터에도 플러스 1을 해줌.
                Log.e("팔로우 통신성공! 서버가 보내는 코드", "code: " + response.code()); //서버가 보내는 http 통신 응답코드
                ProfileFollowResponse result = response.body();
                Log.e("php 메시지", "message: " + result.getMessage()); //서버가 보내는 http 통신 응답코드
                Log.e("php 현재 상태", "status: " + result.getStatus_now());
                if (result.getStatus_now().equals("not_following")) { // 언팔함
                    profile_follow_btn.setBackground(getDrawable(R.drawable.shape_rectangle_green_background));
                    profile_follow_btn.setText("팔로우 하기");
                    profile_follow_btn.setTextColor(getColor(R.color.white));
                    user_follower_cnt.setText("팔로워 " + (follower_cnt-1)+"명" );
                    follower_cnt--;
                    profile_chatting_btn.setVisibility(View.GONE);

                } else if (result.getStatus_now().equals("following")) { // 팔로우 함
                    profile_follow_btn.setBackground(getDrawable(R.drawable.shape_rectangle_black_line));
                    profile_follow_btn.setText("팔로잉 중");
                    profile_follow_btn.setTextColor(getColor(R.color.black));
                    user_follower_cnt.setText("팔로워 " + (follower_cnt+1)+"명" );
                    follower_cnt++;
                    // 맞팔 관계일때 1:1 버튼 보이게
                    if(result.getFollow_each_other() == 1) {
                        profile_chatting_btn.setVisibility(View.VISIBLE);
                    } else {
                        profile_chatting_btn.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileFollowResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("다른 유저 팔로우 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("다른 유저 팔로우 실패. ", "onFailure: " + throwable.getCause());
            }
        });

    }


    // 메소드 6. 해당 유저가 작성한 글 가져오기
    public void profile_board_list (int user_number) {
        service.profile_board_list(user_number).enqueue(new Callback<BoardListResponse.BoardResponse2>() {
            @Override
            public void onResponse(Call<BoardListResponse.BoardResponse2> call, Response<BoardListResponse.BoardResponse2> response) {
                Log.e("프로필작성글불러오기 http 성공. ", "onFailure: " + response.message());

                BoardListResponse.BoardResponse2 result = response.body();
                Log.e("게시판 전체목록 http 통신 성공", "결과 메시지: " + result.getMessage());
                // response 2 에 있는 getter 사용해서 목록 반복문으로 출력하기.

                int total_cnt = result.getTotal_cnt();

                List<BoardListResponse> board_item = result.getBoard_items();

                wholeAdapter.clearItem();  // 초기 로드일 경우만 기존 데이터를 삭제

                if (result.getMessage().equals("게시글이 없습니다")) {
                    Log.e("게시판 전체 게시글이 없음", " reponse 진입함" );
                    wholeAdapter.clearItem();  // 초기 로드일 경우만 메시지 처리
                } else {
                    // 게시글 전체 목록 출력하기
                    for (BoardListResponse item : board_item) {
                        // 가져온 데이터를 add 해주기
                        int board_number = item.getBoard_number();
                        int user_number = item.getUser_number();
                        String title = item.getTitle();
                        String content = item.getContent();
                        String category = item.getCategory();
                        String date = item.getCreatedTime();
                        int like_cnt = item.getLike_cnt();
                        int comment_cnt = item.getComment_cnt();
                        int like_or_not = item.isLike_or_not();
                        Log.e("좋아요 댓글 개수", "좋아요: " + item.getLike_cnt() + ", 댓글: " + item.getComment_cnt());

                        wholeAdapter.addItem(new BoardData(board_number,user_number, title, content,category, date, like_cnt, comment_cnt, like_or_not));
                    } // for문 끝

                    // 작성한 게시글 총 개수 setting
                    profile_board_cnt.setText("총 " + total_cnt + "개");
                    board_board.setText( writer_nickname +  "님의 게시글");
                }
            }

            @Override
            public void onFailure(Call<BoardListResponse.BoardResponse2> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("프로필작성글불러오기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("프로필작성글불러오기 실패. ", "onFailure: " + throwable.getCause());
            }
        });




    } // profile_board_list 끝


    // 게시글 좋아요 기능
    @Override
    public void onLikeBtnClicked(int board_number, String status_tag) {

    }




}
