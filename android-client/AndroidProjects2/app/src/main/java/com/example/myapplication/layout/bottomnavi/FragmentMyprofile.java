package com.example.myapplication.layout.bottomnavi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import com.example.myapplication.data.retrofit.responsemodel.ProfileViewResponse;
import com.example.myapplication.layout.EditProfileActivity;
import com.example.myapplication.layout.LoginActivity;
import com.example.myapplication.layout.MainActivity;
import com.example.myapplication.layout.UserFollowFollowingListActivity;
import com.example.myapplication.layout.UserLibraryListActivity;
import com.example.myapplication.layout.onLikeBtnClick;
import com.example.myapplication.socket.SocketService;
import com.google.android.material.navigation.NavigationBarView;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentMyprofile extends Fragment implements UserNumberCallback, onLikeBtnClick {
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

    RetrofitService service; // 레트로핏 서비스
    SharedPreferences sharedPreferences; // 쉐어드 초기화
    PreferenceManager pref;  // 쉐어드 매니저 초기화
    private NavigationBarView bottomNavigationView; // 바텀 네비게이션 인스턴스 변수

    SocketService socketService; // 소켓 서비스 - 로그아웃 시 서비스, 소켓 닫기 위함


    // view
    Button logout_btn; // 로그아웃 버튼
    Toolbar toolbar;


    ImageView user_profile_img; // 내 프사
    TextView user_nickname, user_follower_cnt, user_following_cnt, user_description, read_cnt, reading_cnt, want_cnt;
    // 닉네임, 팔로워수, 팔로잉수, 유저 소개글, 읽은책, 읽고 있는책, 읽고 싶은책

    TextView profile_board_cnt; // 작성한 게시글 전체 개수

    Button profile_edit_btn; // 프로필 편집 버튼


    // string
    String key = "signin_email_id"; // 쉐어드 키
    String signin_email_id_value; // 쉐어드 값 (이메일아이디, 닉네임 json string 값 - 파싱 전)
    String emailId; // 이메일/아이디만 파싱한 값.
    String nickname; // 로그인한 유저 닉네임

    String user_number; // 통신으로 user number 가져온거 넣어줌.
    int user_number_int; // int 로 바꿔줌


    // 팔로워 수, 팔로잉 수 - 레트로핏에서 가져와서 setting 해준 수
    int follower_cnt, following_cnt;


    // int
    // 내가 작성한 게시글 페이징
    int last_position = 0;  // 초기 오프셋 값
    private final int ITEM_COUNT = 5;  // 한 번에 가져올 아이템 수


    // boolean
    private boolean isLoading = false; // 페이징 로딩 상태를 추적


    // 작성한 게시글 리사이클러뷰 및 어댑터
    RecyclerView profile_board_list_recyclerview;
    BoardWholeAdapter wholeAdapter;


    public FragmentMyprofile() {
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentMyprofile newInstance(String param1, String param2) {
        FragmentMyprofile fragmentMyprofile = new FragmentMyprofile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragmentMyprofile.setArguments(args);
        return fragmentMyprofile;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.bmnavi_myprofile, container, false);


        Log.e("FragmentMyprofile생명주기 onCreateView", "onCreateView");


        // ==================================== 초기화 시작 =============================================

        // 서비스 - 서비스 죽이면 소켓 죽음.
        socketService = new SocketService();


        // context
        Context context = getActivity();


        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의

        // 쉐어드 관련 클래스 및 값 초기화
        pref = new PreferenceManager();
        sharedPreferences = getActivity().getSharedPreferences("session_contain", Context.MODE_PRIVATE);
        signin_email_id_value = sharedPreferences.getString(key, "");

        // 로그아웃 버튼
        logout_btn = rootView.findViewById(R.id.logout_btn);

        // 상단 툴바
        toolbar = rootView.findViewById(R.id.toolbar);

        // tool bar 초기화 및, 타이틀 표시 비활성화
        // 툴바
        // Fragment에서는  ((MainActivity) getActivity()) 종속 액티비티를 참조해줘야 함.
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);

        if (((MainActivity) getActivity()).getSupportActionBar() != null) {
            ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }

        setHasOptionsMenu(true); // 상단 오른쪽 메뉴


        // view 연결해주기
        user_profile_img = rootView.findViewById(R.id.profile_img);
        user_nickname = rootView.findViewById(R.id.profile_nickname);
        user_follower_cnt = rootView.findViewById(R.id.profile_follower_cnt);
        user_following_cnt = rootView.findViewById(R.id.profile_following_cnt);
        user_description = rootView.findViewById(R.id.profile_user_description);

        read_cnt = rootView.findViewById(R.id.profile_read_cnt);
        reading_cnt = rootView.findViewById(R.id.profile_reading_cnt);
        want_cnt = rootView.findViewById(R.id.profile_want_cnt);

        profile_board_cnt = rootView.findViewById(R.id.profile_board_cnt); // 내가 작성한 게시글 전체 개수

        // button
        profile_edit_btn = rootView.findViewById(R.id.profile_edit_btn);

        // 리사이클러뷰
        profile_board_list_recyclerview = rootView.findViewById(R.id.profile_board_list_recyclerview);

        // 어댑터
        wholeAdapter = new BoardWholeAdapter(getActivity(), profile_board_list_recyclerview, this);

        profile_board_list_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        profile_board_list_recyclerview.setAdapter(wholeAdapter);

        // ==================================== 초기화 끝 ===========================================

        //  on create 1. 액티비티에서 바텀 네비게이션 가져오기
        if (getActivity() instanceof MainActivity) {  // MainActivity로부터 가져오기 // 이 과정이 꼭 필요함 !!!
            // 내가 지금 활동하는 액티비티가 올바른 액티비티인지 꼭 확인 !!
            MainActivity mainActivity = (MainActivity) getActivity();
            bottomNavigationView = mainActivity.findViewById(R.id.bottomNavigationView);
        }


        // on create 2. 툴바 홈 이모티콘 클릭 시, 홈으로 이동하기.
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


        // on create 3. 내 프로필 편집으로 이동
        profile_edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getActivity();  // 액티비티의 컨텍스트 가져오기
                Intent intent = new Intent(context, EditProfileActivity.class); // 이동하기.
                intent.putExtra("user_number", user_number); // 유저 넘버 보내주기.
                intent.putExtra("user_nickname", nickname);
                startActivity(intent);
            }
        });

        // on create 4-1) 팔로워 팔로잉 목록 보기 - 팔로워
        user_follower_cnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("팔로워목록보기!!클릭", "팔로워목록 주인번호(string값): " + user_number); // 레트로핏으로 가져온 유저 넘버.
                Context context = getActivity();
                Intent intent = new Intent(context, UserFollowFollowingListActivity.class); // 이동하기
                intent.putExtra("user_number", user_number);
                intent.putExtra("type", "follower");
                startActivity(intent);
            }
        });


        // on create 4-2) 팔로워 팔로잉 목록 보기 - 팔로잉
        user_following_cnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("팔로잉목록보기!!클릭", "팔로워목록 주인번호(string값): " + user_number); // 레트로핏으로 가져온 유저 넘버.
                Context context = getActivity();
                Intent intent = new Intent(context, UserFollowFollowingListActivity.class); // 이동하기
                intent.putExtra("user_number", user_number);
                intent.putExtra("type", "following");
                startActivity(intent);
            }
        });


        // on create 5-1) 서재 목록 - 읽은 책
        read_cnt.setOnClickListener(v -> {
            Log.d("읽은 책 목록보기!!클릭", "책목록 주인번호(string값): " + user_number); // 레트로핏으로 가져온 유저 넘버.
            Intent intent = new Intent(context, UserLibraryListActivity.class);
            intent.putExtra("user_number", user_number);
            intent.putExtra("type", "read");
            startActivity(intent);
        });

        // on create 5-2) 서재 목록 - 읽고 있는 책
        reading_cnt.setOnClickListener(v -> {
            Log.d("읽고 있는 책 목록보기!!클릭", "책목록 주인번호(string값): " + user_number); // 레트로핏으로 가져온 유저 넘버.
            Intent intent = new Intent(context, UserLibraryListActivity.class);
            intent.putExtra("user_number", user_number);
            intent.putExtra("type", "reading");
            startActivity(intent);
        });

        // on create 5-3) 서재 목록 - 읽고 싶은 책
        want_cnt.setOnClickListener(v -> {
            Log.d("읽고 싶은 책 목록보기!!클릭", "책목록 주인번호(string값): " + user_number); // 레트로핏으로 가져온 유저 넘버.
            Intent intent = new Intent(context, UserLibraryListActivity.class);
            intent.putExtra("user_number", user_number);
            intent.putExtra("type", "want");
            startActivity(intent);
        });


        // onStart 내가 작성한 글 페이징
//        profile_board_list_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                if(!isLoading) {
//                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
//                    Log.i("스크롤리스너!!로딩중아닐때", "마지막보이는아이템position: " + lastVisibleItemPosition);
//                    // 실시간으로 따라서셈.
//                    int totalItemCount = layoutManager.getItemCount();
//                    Log.i("스크롤리스너!!로딩중아닐때", "현재 전체아이템개수: " + totalItemCount);
//
//                    if (lastVisibleItemPosition >= totalItemCount - 1) {
//                        Log.i("여기이해안됨.", "last visible position AND total item cnt " + lastVisibleItemPosition + " AND " + totalItemCount );
//                        // 그니까 last visible은 현재 사용자가 보는 페이지에서 마지막줄 아이템의 position을 가져옴. 현재 4개가 한화면에 나오니
//                        // 4부터 내릴때마다 5,6,7, 이렇게 증가함.
//
//                        // total item cnt는 리사이클러뷰에 담긴 아이템의 수임.
//                        // 10개씩 가져오기 때문에 페이지 로드할때마다 10개, 20개, 이렇게 10씩 증가함.
//
//                        // 다음 페이지 요청하기.
//                        isLoading = true; // 로딩 중으로 상태 변경
//                        last_position += ITEM_COUNT; // 다음 페이지를 위한 오프셋 계산
//                        profile_board_list(user_number_int, false);
//                    }
//                }
//            }
//        });
        return rootView;
    } // on create view


    // m0. tool bar 메뉴 적용하기.
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.dropdown_menu_profile, menu);
    }


    // m0. tool bar 메뉴 선택 됐을 때 이벤트 수신하는 메소드
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_signout) {
            signout();
            return true;
        } else if (id == R.id.menu_leave) {
            Toast.makeText(context, "탈퇴 버튼 누름", Toast.LENGTH_SHORT).show();
            // 진짜 탈퇴할건지 다시 물어보기.
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("북허브 회원 탈퇴")
                    .setMessage("회원 탈퇴 시, 모든 데이터는 복구가 불가능합니다. \n 정말 탈퇴하시나요?")
                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 탈퇴 하는 http 통신 메소드 호출
                            withdraw();
                           // socketService.onDestroy();
                        }
                    })
                    .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    // m1. 로그인 화면 이동
    private void navigateToLoginScreen() {
        Context context = getActivity();  // 액티비티의 컨텍스트 가져오기
        Intent intent = new Intent(context, LoginActivity.class);
        startActivity(intent);
        getActivity().finish(); // 액티비티의 finish를 가져온다.
    }


    // m2. 로그아웃
    public void signout() {
        // 쉐어드에 로그인 정보 있으면 지우고, 로그인 화면으로 가기.
        // 없으면 그냥 로그인화면으로 가기.
        Log.d("로그아웃쉐어드체크", "자동로그인 정보있나요: " + signin_email_id_value);

        // 카카오 토큰이 있는 경우 (카카오로그인상태) vs 토큰 없는 경우 (이메일로그인 상태)
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null) {
                    // 카카오 토큰이 있을 경우 로그 아웃
                    UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                        @Override
                        public Unit invoke(Throwable throwable) {
                            if (throwable == null) // 오류 없
                            {
                                Log.d("카카오 로그아웃 성공", "토큰 무효화 성공");

                                // SharedPreferences 값 제거
                                PreferenceManager.removeKey(getActivity().getApplicationContext(), key);
                                Log.d("카카오 로그아웃 성공", "쉐어드 삭제 성공");
                                // 로그인 화면으로 이동
                                Log.d("카카오 로그아웃 성공", "로그인 화면으로 이동");
                                navigateToLoginScreen();
                            } else {
                                Log.e("카카오 로그아웃 실패", "로그아웃에 실패했습니다.", throwable);
                            }
                            return null;
                        }
                    });

                    // 서비스 닫고 소켓 종료
                    socketService.onDestroy();

                } else {
                    // 카카오 토큰이 없을 경우.
                    Log.d("카카오 토큰 없음", "이메일로그인 상태");
                    // 값이 있으면 지우고 로그인 화면 감.
                    pref.removeKey(getActivity().getApplicationContext(), key);
                    Log.d("이메일쉐어드삭제", "쉐어드 삭제 성공");
                    navigateToLoginScreen();
                    // 서비스 닫고 소켓 종료
                    socketService.onDestroy();

                }
                return null;
            }
        });
    } // 로그아웃 끝

    // m3 . 유저 넘버 가져오는 메소드 (레트로핏)
    public void get_userNumber(String user_emailid, UserNumberCallback callback) {
        Log.i("게시판상세- 유저넘버가져오기", "유저의 emailId: " + user_emailid);
        service.user_emailId(user_emailid).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("게시판상세-유저 number 체크 response 진입", "onResponse: 응답메시지: " + result.getMessage()); // echo로 보내주는 메시지.
                Log.e("게시판상세-유저 number 체크 response 진입", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.

                if (result.getCode() == 200) {
                    Log.e("유저 number 성공 첫번째증거", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
//                    user_number = result.getUser_number();
                    callback.onUserNumberReceived(result.getUser_number()); // 콜백 메소드 실행 !!
                } else {
                    Log.e("php에서 뭔가 잘못됨. / code==400!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Log.e("게시판상세-유저넘버찾기 통신 failed", "실패원인: " + throwable.getMessage());
            }
        });
    } // get user number 끝


    // =================================== 유저 넘버 가져오는 레트로핏에 대한 call back 인터페이스 메소드 !!
// 메소드 4. 현재 로그인 중인 유저 넘버 가져오는 call  back 메소드
    @Override
    public void onUserNumberReceived(String userNumber) {
        this.user_number = userNumber;
        Log.e("유저 number 성공 두번째증거", "유저넘버: " + userNumber); // echo로 보내주는 메시지.
        user_number_int = Integer.parseInt(userNumber); // int 값도 setting 해주기
        get_users_info(user_number_int, user_number_int, nickname);
        profile_board_list(user_number_int); // 유저가 작성한 게시글. 첫 게시글 가져 오는 것이기 때문에 true 값 넘겨 주기.

        // 내 게시글도 가져 오기.


    } // onUserNumberReceived 끝
//

    // 메소드 4-2. call back 인터페이스에 정의해놓은 메소드. 필수로 구현해줘야함.
    @Override
    public void onError(String errorMessage) {
        Log.d("유저 number 실패", "에러메시지:  " + errorMessage);
    }

    // 메소드 5 - 다른 유저 프로필 내용 가져올때 사용한 로직이기 때문에 매개변수에 있는 유저번호 둘다 내 번호임.
    public void get_users_info(int user_number, int this_users_number, String this_nickname) {
        Log.i("내정보가져오기 레트로핏", "둘다 내번호임!! : " + this_users_number + ", 로그인한유저번호: " + user_number);
        service.other_profile_info(user_number, this_users_number, this_nickname).enqueue(new Callback<ProfileViewResponse>() {
            @Override
            public void onResponse(Call<ProfileViewResponse> call, Response<ProfileViewResponse> response) {
                ProfileViewResponse result = response.body();
                Log.e("내정보가져오기 성공 ", " reponse 진입함");
                Log.e("내정보가져오기 응답코드php ", " reponse 코드: " + response.message());
                Log.e("내정보가져오기 정보 ", " 유저번호: " + result.getMessage() + ", 유저닉네임: " + result.getUser_nickname() + ", 유저 읽은책: " + result.getUser_readBook_cnt() + ", 읽고싶: " + result.getUser_wantBook_cnt() + ",읽고 있는: " + result.getUser_readingBook_cnt());
                if (result.getProfile_img() == null) {
                    Log.e("내정보가져오기 -프사없음 ", "프사없음");
                } else { // 있을 때만 setting 해주기

                    Glide.with(getActivity())
                            .load((String) null) // 기존 이미지를 초기화
                            .into(user_profile_img);

                    Log.e("내정보가져오기 -프사있음 ", "프사있음");

                    String img_serverAddress = "http://3.39.255.234/php/img/";
                    String userProfileImg = result.getProfile_img();
                    String img_url = img_serverAddress + userProfileImg;
                    Glide.with(getActivity())
                            .load(img_url)
                            .apply(RequestOptions.circleCropTransform()) // 원형 변환 적용
                            .into(user_profile_img);
                }

                Log.e("내정보 정보 가져오기 팔로우수", "팔로우수: " + result.getFollowing_cnt());
                Log.e("내정보 정보 가져오기 팔로워 수", "팔로워수: " + result.getFollower_cnt());

                follower_cnt = result.getFollower_cnt();
                following_cnt = result.getFollowing_cnt();

                // view 세팅 해주기
                Log.e("FragmentMyprofile생명주기 닉네임!!", "닉: " + result.getUser_nickname());

                user_nickname.setText(result.getUser_nickname());
                user_description.setText(result.getProfile_bio());
                read_cnt.setText(String.valueOf(result.getUser_readBook_cnt()));
                reading_cnt.setText(String.valueOf(result.getUser_readingBook_cnt()));
                want_cnt.setText(String.valueOf(result.getUser_wantBook_cnt()));
                Log.e("내정보,현재유저는 ", "현재 유저는??? 유저넘버(string): " + user_number);

                user_follower_cnt.setText("팔로워 " + follower_cnt + "명");
                user_following_cnt.setText("팔로잉 " + following_cnt + "명");
            }

            @Override
            public void onFailure(Call<ProfileViewResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("내정보 가져오기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("내정보 가져오기 실패. ", "onFailure: " + throwable.getCause());

            }
        });


    } // 메소드 5 끝

    // 메소드 6. 내가 작성한 게시글 가져오기. - 원래 5개만 가져와서 페이징 하려고 했는데
    // 일단 다 가져오기 !!

    public void profile_board_list(int user_number) {
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
                    Log.e("게시판 전체 게시글이 없음", " reponse 진입함");
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

                        wholeAdapter.addItem(new BoardData(board_number, user_number, title, content, category, date, like_cnt, comment_cnt, like_or_not));
                    } // for문 끝
                    isLoading = false;
                    // 작성한 게시글 총 개수 setting
                    profile_board_cnt.setText("총 " + total_cnt + "개");
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


    // 메소드 7. 탈퇴
    public void withdraw() {
        Log.e("탈퇴 메소드 진입 ", "회원번호: " + user_number + ", user number int: " + user_number_int);
        service.withdraw(user_number_int).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("탈퇴 성공 ", "php code: " + result.getCode() + ", php 메시지: " + result.getMessage());
                pref.removeKey(getActivity().getApplicationContext(), key);
                Log.d("이메일쉐어드삭제", "쉐어드 삭제 성공");
                navigateToLoginScreen();

                // 예: 탈퇴 요청 처리 시 로그 추가
                Log.d("SocketService", "Handling client logout...");
                if (socketService != null) {
                    Log.d("SocketService", "SocketManager is active");
                } else {
                    Log.e("SocketService", "SocketManager is null during logout");
                }

                Intent intent = new Intent(context, SocketService.class);
                context.stopService(intent);

            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("회원탈퇴하기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("회원탈퇴하기 실패. ", "onFailure: " + throwable.getCause());
            }
        });

    }


    //  메소드 8. 게시글 좋아요 기능
    @Override
    public void onLikeBtnClicked(int board_number, String status_tag) {

    }

    @Override
    public void onStart() {

        // 쉐어드
        pref = new PreferenceManager();
        signin_email_id_value = pref.getString(getActivity().getApplicationContext(), key);

        try {
            JSONObject jsonObject = new JSONObject(signin_email_id_value);
            nickname = jsonObject.optString("nickname", ""); // nullpoint exception 발생할 수 있으므로, 빈문자열로 두기
            emailId = jsonObject.optString("emailid", "");
            Log.d("내 서재 상세보기", "로그인 회원 이메일: " + emailId);
            get_userNumber(emailId, (UserNumberCallback) this); // 유저 번호 가져오기.

        } catch (JSONException e) {
            e.printStackTrace();
            emailId = ""; // 기본값 설정
        }

        Log.e("FragmentMyprofile생명주기 onStart", "onStart");
        get_users_info(user_number_int, user_number_int, nickname);
        super.onStart();

    }

    @Override
    public void onResume() {
        Log.e("FragmentMyprofile생명주기 onResume", "onResume");
        super.onResume();
        get_users_info(user_number_int, user_number_int, nickname);
    }

}
