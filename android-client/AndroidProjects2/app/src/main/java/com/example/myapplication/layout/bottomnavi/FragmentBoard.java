package com.example.myapplication.layout.bottomnavi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.recyclerview.BoardChatAdapter;
import com.example.myapplication.data.recyclerview.BoardData;
import com.example.myapplication.data.recyclerview.BoardRecommendAdapter;
import com.example.myapplication.data.recyclerview.BoardWelcomeAdapter;
import com.example.myapplication.data.recyclerview.BoardWholeAdapter;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.BoardLikeResponse;
import com.example.myapplication.data.retrofit.responsemodel.BoardListResponse;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.layout.BoardCreateActivity;
import com.example.myapplication.layout.MainActivity;
import com.example.myapplication.layout.onLikeBtnChanged;
import com.example.myapplication.layout.onLikeBtnClick;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentBoard extends Fragment implements onLikeBtnClick {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentBoard(){

    }


    // import
    SharedPreferences sharedPreferences; // 쉐어드 초기화
    PreferenceManager pref;  // 쉐어드 매니저 초기화
    RetrofitService service; // 레트로핏 서비스
    private RecyclerView whole_list_recyclerview, recommend_recyclerview, chat_recyclerview, welcome_recyclerview;
    private NavigationBarView bottomNavigationView; // 바텀 네비게이션 인스턴스 변수



    // 리사이클러뷰 어댑터 모음
    BoardWholeAdapter wholeAdapter;
    BoardRecommendAdapter recommendAdapter;
    BoardChatAdapter chatAdapter;
    BoardWelcomeAdapter welcomeAdapter;



    // view
    Toolbar toolbar; // 툴바
    ImageView board_add_btn; // 게시판 글 작성하기 btn
    Button whole_btn, recommend_btn, chat_btn, welcome_btn; // 전체보기, 책추천, 잡담, 가입인사 버튼
    EditText board_search;


    // 좋아요 button

    Button like_btn;



    // 게시글 작성 내용에 대한 view 초기화


    // string
    String user_number; // 쉐어드에서 가져온 이메일 아이디로, 서버에서 가져온 user number !!
    String key = "signin_email_id"; // 쉐어드 키
    String signin_email_id_value ; // 쉐어드 값 (이메일아이디, 닉네임 json string 값 - 파싱 전)




    // 화면이 전환되었다가 다시 돌아올때 !!
    // 아예 화면이 종료되는 것이 아니라, 게시글 상세보기 들어갔다가 다시 fragment 로 나오는 상황에서 on resume 에서 복원하기 위해. 아래 내용을 만들어주고 on resume 에서 코드 작성한다.
    //  onSaveInstanceState 메소드는 단순히 화면 전환되었다가 돌아올때에는 호출되지 않는다.
    // 즉, 프래그먼트가 메모리에서 파괴되지 않을 경우에는 호출 x
    private Parcelable wholeListState, recommendState, chatState, welcomeState;



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyassetFragment.
     */
    // TODO: Rename and change types and number of parameters

    public static FragmentBoard newInstance(String param1, String param2) {
        FragmentBoard fragmentBoard = new FragmentBoard();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragmentBoard.setArguments(args);
        return fragmentBoard;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Log.d("FragmentBoard생명주기", "onCreate");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("FragmentBoard생명주기", "onCreateView");
        // Inflate the layout for this fragment
        ViewGroup rootView= (ViewGroup) inflater.inflate(R.layout.bmnavi_board, container, false);

        // ==================================== 초기화 시작 =============================================


        // tool bar 초기화 및, 타이틀 표시 비활성화
        // 툴바
        // Fragment에서는  ((MainActivity) getActivity()) 종속 액티비티를 참조해줘야 함.
        toolbar = rootView.findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);

        if (((MainActivity) getActivity()).getSupportActionBar() != null) {
            ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }




        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의


        // 쉐어드 관련 클래스 및 값 초기화
        pref = new PreferenceManager();
        sharedPreferences = getActivity().getSharedPreferences("session_contain", Context.MODE_PRIVATE);
        signin_email_id_value = sharedPreferences.getString(key, "");


        // 게시판 작성
        board_add_btn = rootView.findViewById(R.id.board_add_btn);

        // 검색창
        board_search = rootView.findViewById(R.id.board_search);


        // 리사이클러뷰 변경 버튼
        whole_btn = rootView.findViewById(R.id.board_whole_btn);
        recommend_btn = rootView.findViewById(R.id.board_recommend_btn);
        chat_btn = rootView.findViewById(R.id.board_chat_btn);
        welcome_btn = rootView.findViewById(R.id.board_welcome_btn);

        //리사이클러뷰
        whole_list_recyclerview = rootView.findViewById(R.id.recyclerView_board_whole);
        recommend_recyclerview = rootView.findViewById(R.id.recyclerView_board_recommend);
        chat_recyclerview = rootView.findViewById(R.id.recyclerView_board_chat);
        welcome_recyclerview = rootView.findViewById(R.id.recyclerView_board_welcome);


        // 어댑터
        wholeAdapter = new BoardWholeAdapter(getActivity(), whole_list_recyclerview, this);
        recommendAdapter = new BoardRecommendAdapter(getActivity());
        chatAdapter = new BoardChatAdapter(getActivity());
        welcomeAdapter = new BoardWelcomeAdapter(getActivity());

        // 어댑터 레이아웃 매니저 연결해주기
        // 1) 게시판 전체 목록
        whole_list_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        whole_list_recyclerview.setAdapter(wholeAdapter);

        //2) 게시판 책추천 목록
        recommend_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recommend_recyclerview.setAdapter(recommendAdapter);

        //3) 게시판 잡담
        chat_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        chat_recyclerview.setAdapter(chatAdapter);

        //4) 가입 인사
        welcome_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        welcome_recyclerview.setAdapter(welcomeAdapter);



        // 첫 진입 시, whole list로 세팅
        setting_whole();

        // ==================================== 초기화 끝 ===========================================


        //  액티비티에서 바텀 네비게이션 가져오기
        if (getActivity() instanceof MainActivity) {  // MainActivity로부터 가져오기 // 이 과정이 꼭 필요함 !!!
            // 내가 지금 활동하는 액티비티가 올바른 액티비티인지 꼭 확인 !!
            MainActivity mainActivity = (MainActivity) getActivity();
            bottomNavigationView = mainActivity.findViewById(R.id.bottomNavigationView);
        }



        // on create 0. 각 카테고리 클릭 시, 리사이클러뷰 변경
        // <전체 >
        whole_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 전체 리사이클러뷰 세팅
                setting_whole();
                // 전체 목록 가져오기 실행.  !!
                whole_setting(user_number);
            }
        });


        // < 책 추천>
        recommend_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting_recommend();
                recommend_setting(user_number);
                // 책 추천 목록 가져오기 실행 !!
            }
        });

        // <잡담 >
        chat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting_chat();
                chat_setting(user_number);
                // 잡담 목록 가져오기 실행 !!
            }
        });

        // < 가입인사 >
        welcome_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting_welcome();
                welcome_setting(user_number);
                // 가입인사 목록 가져오기 실행!!
            }
        });



        // on create view 1. 툴바 홈 이모티콘 클릭 시, 홈으로 이동하기
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


        // on create view 2. 로그인 한 사용자 닉네임 가져오기!! 완료하면 유저 넘버 가져오기 !!
        if(!signin_email_id_value.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(signin_email_id_value);
                String emailId = jsonObject.optString("emailid", ""); // default 값 설정 가능
                 String nickname = jsonObject.optString("nickname", ""); // nullpoint exception 발생할 수 있으므로, 빈문자열로 두기
               get_userNumber(emailId);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("보드 프래그먼트 쉐어드 비었음", "쉐어드왜비었죠? : 진짜 비었나? >>>  " + signin_email_id_value);
//            Toast.makeText(getApplicationContext(), " 저장 액티비티 진입 실패. 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
//            finish();
        }



        // on create view 3. 게시판 작성 이모티콘 클릭시 Board Create 액티비티로 이동하기
        board_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // board create activity 로 이동하기.
                Intent intent = new Intent(getActivity(), BoardCreateActivity.class);
                startActivity(intent);
            }
        }); // 게시판 작성 btn 클릭 이벤트
        return rootView;
    } // on create view 끝



    // 게시글 상세보기 하고 돌아올때마다 보고 있던 리사이클러뷰의 위치에 있을 수 있도록 상태 저장하기.
//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        // 리사이클러뷰 상태 저장.
//        // 현재 보이는 리사이클러뷰??
//        // 리사이클러뷰 상태 저장
//
//        // 현재 어떤 레이아웃이 visible인지에 따라서 리사이클러뷰 다시 로딩하기.
//        if(whole_list_recyclerview.getVisibility()== View.VISIBLE) {
//            Log.d("프래그먼트의 on resume !!  리사이클러뷰가져오기 ", "");
//            Parcelable layoutManagerState = whole_list_recyclerview.getLayoutManager().onSaveInstanceState();
//            outState.putParcelable("WHOLE_LIST_STATE", layoutManagerState);
//
//        } else if (recommend_recyclerview.getVisibility() == View.VISIBLE) {
//            Parcelable layoutManagerState = recommend_recyclerview.getLayoutManager().onSaveInstanceState();
//            outState.putParcelable("RECOMMEND_STATE", layoutManagerState);
//
//        }
//        else if (chat_recyclerview.getVisibility() == View.VISIBLE) {
//            Parcelable layoutManagerState = chat_recyclerview.getLayoutManager().onSaveInstanceState();
//            outState.putParcelable("CHAT_STATE", layoutManagerState);
//
//        }
//        else if (welcome_recyclerview.getVisibility() == View.VISIBLE) {
//            Parcelable layoutManagerState = welcome_recyclerview.getLayoutManager().onSaveInstanceState();
//            outState.putParcelable("WELCOME_STATE", layoutManagerState);
//        }
//    } // onSaveInstanceState 끝


    // method 1. 전체 세팅 = 초기 세팅해주기
    private void setting_whole () {
        whole_btn.setBackgroundColor(getResources().getColor(R.color.main));
        recommend_btn.setBackground(null);
        chat_btn.setBackground(null);
        welcome_btn.setBackground(null);

        // 리사이클러뷰
        whole_list_recyclerview.setVisibility(View.VISIBLE);
        recommend_recyclerview.setVisibility(View.GONE);
        chat_recyclerview.setVisibility(View.GONE);
        welcome_recyclerview.setVisibility(View.GONE);
    }


    private void setting_recommend(){
        whole_btn.setBackground(null);
        recommend_btn.setBackgroundColor(getResources().getColor(R.color.main));
        chat_btn.setBackground(null);
        welcome_btn.setBackground(null);

        // 리사이클러뷰
        whole_list_recyclerview.setVisibility(View.GONE);
        recommend_recyclerview.setVisibility(View.VISIBLE);
        chat_recyclerview.setVisibility(View.GONE);
        welcome_recyclerview.setVisibility(View.GONE);

    }


    private void setting_chat () {
        whole_btn.setBackground(null);
        recommend_btn.setBackground(null);
        chat_btn.setBackgroundColor(getResources().getColor(R.color.main));
        welcome_btn.setBackground(null);

        whole_list_recyclerview.setVisibility(View.GONE);
        recommend_recyclerview.setVisibility(View.GONE);
        chat_recyclerview.setVisibility(View.VISIBLE);
        welcome_recyclerview.setVisibility(View.GONE);
    }

    private void setting_welcome() {
        whole_btn.setBackground(null);
        recommend_btn.setBackground(null);
        chat_btn.setBackground(null);
        welcome_btn.setBackgroundColor(getResources().getColor(R.color.main));

        whole_list_recyclerview.setVisibility(View.GONE);
        recommend_recyclerview.setVisibility(View.GONE);
        chat_recyclerview.setVisibility(View.GONE);
        welcome_recyclerview.setVisibility(View.VISIBLE);
    }




    // method 2. 유저 넘버 가져오기.
    public void get_userNumber (String user_emailid) {
        Log.i("게시판 프래그먼트에서 유저 넘버가져오기", "유저의 emailId: " + user_emailid);
        service.user_emailId(user_emailid).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("메인-유저 number 체크 response 진입", "onResponse: 응답메시지: " + result.getMessage()); // echo로 보내주는 메시지.
                Log.e("메인-유저 number 체크 response 진입", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.

                if(result.getCode() == 200 ) {
                    Log.e("유저 number 체크성공 / code==200!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                    user_number = result.getUser_number();
//                    // view model - userNumber 값 설정
//                    userViewModel.setUserNumber(user_number);
                    Log.d("프래그먼트의 on create의 get userNumber 메소드 안 !! 리사이클러뷰가져오기 ", "");
                    wholeAdapter.clearItem(); // 혹시 모르니 삭제하고 가져오기.
                    whole_setting(user_number);

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

    // = =============== <<<< 리사이클러뷰 전체 목록 setting >>>>

    // method 3. 게시판 전체목록
    public void whole_setting (String user_number) {
        Log.e("게시판 전체목록 레트로핏 메소드진입", "매개변수 유저넘버: " + user_number);
        service.boardList_whole(user_number).enqueue(new Callback<BoardListResponse.BoardResponse2>() {
            @Override
            public void onResponse(Call<BoardListResponse.BoardResponse2> call, Response<BoardListResponse.BoardResponse2> response) {
                Log.e("게시판 전체목록 http 통신 성공", " reponse 진입함" );
                BoardListResponse.BoardResponse2 result = response.body();
                Log.e("게시판 전체목록 http 통신 성공", "결과 메시지: " + result.getMessage());

                // response 2 에 있는 getter 사용해서 목록 반복문으로 출력하기.
                List<BoardListResponse> board_item = result.getBoard_items();

                if (result.getMessage().equals("게시글이 없습니다")) {
                    Log.e("게시판 전체 게시글이 없음", " reponse 진입함" );
                    wholeAdapter.clearItem();

                } else {
                    wholeAdapter.clearItem(); // 게시글 전체 목록 리사이클러뷰 clear
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
                        Log.e("좋아요 댓글 개수", "좋아요: " + item.getLike_cnt() + ", 댓글: " + item.getComment_cnt() + ", 좋아요 여부: " + item.isLike_or_not());

                        wholeAdapter.addItem(new BoardData(board_number,user_number, title, content,category, date, like_cnt, comment_cnt, like_or_not));
                    } // for문 끝
                }
            } // on response 끝

            @Override
            public void onFailure(Call<BoardListResponse.BoardResponse2> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("게시글 전체 목록 가져오기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("게시글 전체 목록 가져오기 실패. ", "onFailure: " + throwable.getCause());
            }
        });
    }



    // method 4. = =============== <<<< 리사이클러뷰 책추천 목록 setting >>>>
    public void recommend_setting (String user_number) {
        Log.e("게시판 책추천 레트로핏 메소드진입", "매개변수 유저넘버: " + user_number);
        service.boardList_recommend(user_number).enqueue(new Callback<BoardListResponse.BoardResponse2>() {
            @Override
            public void onResponse(Call<BoardListResponse.BoardResponse2> call, Response<BoardListResponse.BoardResponse2> response) {
                Log.e("게시판 책추천 http 통신 성공", " reponse 진입함" );
                BoardListResponse.BoardResponse2 result = response.body();
                Log.e("게시판 책추천 http 통신 성공", "결과 메시지: " + result.getMessage());

                // response 2 에 있는 getter 사용해서 목록 반복문으로 출력하기.
                List<BoardListResponse> board_item = result.getBoard_items();

                if (result.getMessage().equals("게시글이 없습니다")) {
                    Log.e("게시판 책추천 게시글이 없음", " reponse 진입함" );
                    recommendAdapter.clearItem();
                } else {
                    recommendAdapter.clearItem(); // 게시글 책 추천 목록 리사이클러뷰 clear
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
                        recommendAdapter.addItem(new BoardData(board_number,user_number, title, content,category, date, like_cnt, comment_cnt, like_or_not));
                    } // for문 끝
                }
            } // on response 끝

            @Override
            public void onFailure(Call<BoardListResponse.BoardResponse2> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("게시글 책추천 목록 가져오기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("게시글 책추천 목록 가져오기 실패. ", "onFailure: " + throwable.getCause());
            }
        });
    } // 책추천 목록 불러오기 끝



    // method 5. = =============== <<<< 리사이클러뷰 잡담 목록 setting >>>>
    public void chat_setting (String user_number) {
        Log.e("게시판 잡담 레트로핏 메소드진입", "매개변수 유저넘버: " + user_number);
        service.boardList_chat(user_number).enqueue(new Callback<BoardListResponse.BoardResponse2>() {
            @Override
            public void onResponse(Call<BoardListResponse.BoardResponse2> call, Response<BoardListResponse.BoardResponse2> response) {
                Log.e("게시판 잡담 http 통신 성공", " reponse 진입함" );
                BoardListResponse.BoardResponse2 result = response.body();
                Log.e("게시판 잡담 http 통신 성공", "결과 메시지: " + result.getMessage());

                // response 2 에 있는 getter 사용해서 목록 반복문으로 출력하기.
                List<BoardListResponse> board_item = result.getBoard_items();

                if (result.getMessage().equals("게시글이 없습니다")) {
                    Log.e("게시판 잡담 게시글이 없음", " reponse 진입함" );
                    chatAdapter.clearItem();
                } else {
                    chatAdapter.clearItem(); // 게시글 책 추천 목록 리사이클러뷰 clear
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
                        chatAdapter.addItem(new BoardData(board_number, user_number,title, content,category, date, like_cnt, comment_cnt, like_or_not));
                    } // for문 끝
                }
            } // on response 끝

            @Override
            public void onFailure(Call<BoardListResponse.BoardResponse2> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("게시글 잡담 목록 가져오기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("게시글 잡담 목록 가져오기 실패. ", "onFailure: " + throwable.getCause());

            }
        });
    } // chat_setting 끝



    // method 6. = =============== <<<< 리사이클러뷰 가입인사 목록 setting >>>>
    public void welcome_setting (String user_number) {
        Log.e("게시판 가입인사 레트로핏 메소드진입", "매개변수 유저넘버: " + user_number);
        service.boardList_welcome(user_number).enqueue(new Callback<BoardListResponse.BoardResponse2>() {
            @Override
            public void onResponse(Call<BoardListResponse.BoardResponse2> call, Response<BoardListResponse.BoardResponse2> response) {
                Log.e("게시판 가입인사 http 통신 성공", " reponse 진입함" );
                BoardListResponse.BoardResponse2 result = response.body();
                Log.e("게시판 가입인사 http 통신 성공", "결과 메시지: " + result.getMessage());

                // response 2 에 있는 getter 사용해서 목록 반복문으로 출력하기.
                List<BoardListResponse> board_item = result.getBoard_items();

                if (result.getMessage().equals("게시글이 없습니다")) {
                    Log.e("게시판 가입인사 게시글이 없음", " reponse 진입함" );
                    welcomeAdapter.clearItem();
                } else {
                    welcomeAdapter.clearItem(); // 게시글 책 추천 목록 리사이클러뷰 clear
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
                        welcomeAdapter.addItem(new BoardData(board_number,user_number, title, content,category, date, like_cnt, comment_cnt, like_or_not));
                    } // for문 끝
                }
            } // on response 끝

            @Override
            public void onFailure(Call<BoardListResponse.BoardResponse2> call, Throwable throwable) {

            }
        });
    }




    // method 7. on like btn click 인터페이스 메소드 오버라이딩
    @Override
    public void onLikeBtnClicked(int board_number, String status_tag){
//        Toast.makeText(getContext(), "좋아요 인터페이스메소드 실행->" + status_tag, Toast.LENGTH_SHORT).show();

        // 좋아요 메소드 호출
        int this_user_number = Integer.parseInt(user_number);
        board_like(this_user_number, board_number, status_tag);

    }


    // method 7. on like btn click 인터페이스 메소드 오버라이딩
//    @Override
//    public void onLikeBtnChanged(int board_number, String newStatus, int Like_cnt) {
//        // 빈 메소드 - board 목록 adapter (whole, recommend, welcome, chat) 에서 구현할거임.
//    }



    // method 8.
    public void board_like (int user_number, int board_number, String status_tag) {
//        Toast.makeText(getContext(), "좋아요 기능 레트로핏 메소드진입" + status_tag, Toast.LENGTH_SHORT).show();
        Log.e("좋아요 기능 레트로핏 메소드진입", "현재 유저번호: " + user_number + ", 현재게시글번호: " + board_number + ", 좋아요상태: " + status_tag);
        service.board_like(user_number, board_number,status_tag).enqueue(new Callback<BoardLikeResponse>() {
            @Override
            public void onResponse(Call<BoardLikeResponse> call, Response<BoardLikeResponse> response) {
                //좋아요 수 업데이트해야함. 음. 이건 어떻게 하는거지 ???
                Log.e("좋아요 통신성공! 서버가 보내는 코드", "code: " + response.code()); //서버가 보내는 http 통신 응답코드
                BoardLikeResponse result = response.body();
                Log.e("좋아요 php 메시지", "message: " + result.getMessage()); //서버가 보내는 http 통신 응답코드
                Log.e("좋아요 php 현재 상태", "status: " + result.getStatus_now());

                Log.e("좋아요 php 현재 개수", "status: " + result.getLike_cnt());

                wholeAdapter.onLikeBtnChanged(result.getBoard_number(), result.getStatus_now(), result.getLike_cnt());

                // 좋아요 한경우
//                if (result.getStatus_now().equals("clicked_like")) { // 좋아요
//                    like_btn.setImageResource(R.drawable.icon_filled_heart);
//                    comment_like_cnt_int++;
//                    comment_like_cnt.setText("공감해요 " + comment_like_cnt_int + "개");
//
//
//                } else if (result.getStatus_now().equals("clicked_unlike")) { // 좋아요 취소
//                    like_btn.setImageResource(R.drawable.icon_line_heart);
//                    comment_like_cnt_int--;
//                    comment_like_cnt.setText("공감해요 " + comment_like_cnt_int + "개");
//
//                }
            }

            @Override
            public void onFailure(Call<BoardLikeResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("좋아요 실패: ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("FragmentBoard생명주기", "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("FragmentBoard생명주기", "onResume");

        // 데이터 로딩 - 현재 어떤 레이아웃이 visible인지에 따라서 리사이클러뷰 다시 로딩하기.
        if(whole_list_recyclerview.getVisibility()== View.VISIBLE) {
            Log.d("프래그먼트의 on resume !!  리사이클러뷰가져오기 ", "");
//            wholeAdapter.clearItem();
            whole_setting(user_number);

        } else if (recommend_recyclerview.getVisibility() == View.VISIBLE) {
//            recommendAdapter.clearItem();
            recommend_setting(user_number);

        }
        else if (chat_recyclerview.getVisibility() == View.VISIBLE) {
//            chatAdapter.clearItem();
            chat_setting(user_number);

        }
        else if (welcome_recyclerview.getVisibility() == View.VISIBLE) {
//            welcomeAdapter.clearItem();
            welcome_setting(user_number);
        }

        // 각 RecyclerView의 상태 복원
        if (wholeListState != null && whole_list_recyclerview.getVisibility() == View.VISIBLE) {
            whole_list_recyclerview.getLayoutManager().onRestoreInstanceState(wholeListState);
        } else if (recommendState != null && recommend_recyclerview.getVisibility() == View.VISIBLE) {
            recommend_recyclerview.getLayoutManager().onRestoreInstanceState(recommendState);
        } else if (chatState != null && chat_recyclerview.getVisibility() == View.VISIBLE) {
            chat_recyclerview.getLayoutManager().onRestoreInstanceState(chatState);
        } else if (welcomeState != null && welcome_recyclerview.getVisibility() == View.VISIBLE) {
            welcome_recyclerview.getLayoutManager().onRestoreInstanceState(welcomeState);
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("FragmentBoard생명주기", "onPause");

        // 게시글 상세보기로 전환하거나, 화면이 바뀔때에 현재 리사이클러뷰의 스크롤을 저장한다.

        // 각 RecyclerView의 상태 저장
        if (whole_list_recyclerview.getVisibility() == View.VISIBLE) {
            wholeListState = whole_list_recyclerview.getLayoutManager().onSaveInstanceState();
        } else if (recommend_recyclerview.getVisibility() == View.VISIBLE) {
            recommendState = recommend_recyclerview.getLayoutManager().onSaveInstanceState();
        } else if (chat_recyclerview.getVisibility() == View.VISIBLE) {
            chatState = chat_recyclerview.getLayoutManager().onSaveInstanceState();
        } else if (welcome_recyclerview.getVisibility() == View.VISIBLE) {
            welcomeState = welcome_recyclerview.getLayoutManager().onSaveInstanceState();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("FragmentBoard생명주기", "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("FragmentBoard생명주기", "onDestroy");
    }


} // 현재 프래그먼트
