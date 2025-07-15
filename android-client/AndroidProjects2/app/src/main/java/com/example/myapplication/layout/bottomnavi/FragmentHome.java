package com.example.myapplication.layout.bottomnavi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.recyclerview.BoardData;
import com.example.myapplication.data.recyclerview.BoardWholeAdapter;
import com.example.myapplication.data.recyclerview.OnSearchItemClickListener_clicked;
import com.example.myapplication.data.recyclerview.OnSearchItemClickListener_deleted;
import com.example.myapplication.data.recyclerview.SearchRecentAdapter;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.BoardListResponse;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.layout.BookAddSelfActivity;
import com.example.myapplication.layout.BookSearchActivity;
import com.example.myapplication.layout.MainActivity;
import com.example.myapplication.layout.onLikeBtnClick;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHome extends Fragment implements OnSearchItemClickListener_deleted, OnSearchItemClickListener_clicked, onLikeBtnClick {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    // import
    SharedPreferences sharedPreferences; // 쉐어드 초기화
    PreferenceManager pref;  // 쉐어드 매니저 초기화


    private BottomNavigationView bottomNavigationView; // 바텀 네비게이션 인스턴스 변수

    RetrofitService service; // 레트로핏 서비스


    // 최근 검색어 목록, 어댑터 - on create 안에 있음
    // 어댑터 설정



    // 게시판 전체보기 리사이클러뷰, 최근 검색어 리사이클러뷰
    private RecyclerView whole_list_recyclerview, recent_search_recyclerview;


    // 리사이클러뷰 어댑터 모음
    BoardWholeAdapter wholeAdapter; // 게시글 전체 보기 리사이클러뷰
    SearchRecentAdapter searchRecentAdapter; // 최근 검색어 리사이클러뷰


    // view
    TextView nickname_text; // 현재 로그인한 사람 닉네임
    Button to_lirary_btn; // 내 서재 바로가기 버튼
    Button to_board_btn; // 게시판 바로가기 버튼
    ImageView book_add_btn; // 책 직접 추가 버튼

//    EditText book_search; // 책 검색 - 알라딘 api
    AutoCompleteTextView book_search; // 포커스를 받았을때


    // 검색어 목록
    ArrayList<String> recent_keyword_list = new ArrayList<>();

    // string
    // 쉐어드 상수 1.
    String key = "signin_email_id"; // 쉐어드 키
    String signin_email_id_value ; // 쉐어드 값 (이메일아이디, 닉네임 json string 값 - 파싱 전)

    // string
    String user_number; // 쉐어드에서 가져온 이메일 아이디로, 서버에서 가져온 user number !!

    String emailId, nickname; // 쉐어드


    // 쉐어드 상수 2.
    private static final String PREF_NAME = "RecentSearches";
    private static final String SEARCH_KEY = "recent_searches";
    public FragmentHome() { }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordFragment.
     */
    // TODO: Rename and change types and number of parameters

    public static FragmentHome newInstance(String param1, String param2) {
        FragmentHome fragmentHome = new FragmentHome();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragmentHome.setArguments(args);
        return fragmentHome;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.bmnavi_home, container, false);


        // ==================================== 초기화 시작 =============================================


        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의


        // 쉐어드 관련 클래스 및 값 초기화
//        pref = new PreferenceManager();

        // 쉐어드 1. 현재 사용자의 아이디, 닉네임.
        sharedPreferences = getActivity().getSharedPreferences("session_contain", Context.MODE_PRIVATE);
        signin_email_id_value = sharedPreferences.getString(key, "");


        // 쉐어드 2. 최근 검색어 기록 (책 검색 시)
        // 최근 검색어 (저장된 것 불러와서 리사이클러뷰 연결해주기 위함)

        // on resume 에서 해주기 !!

//        recent_keyword_list = getRecentSearches();
        Log.d("onCreateView - recentSearches", "리스트 길이: " + recent_keyword_list.size());




        // " 로그인한 사용자 닉네임 " 님 안녕하세요 !!
        nickname_text = rootView.findViewById(R.id.nickname_text);

        // 상단 툴바
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);

        // 내 서재 바로가기 버튼
        to_lirary_btn = rootView.findViewById(R.id.to_lirary_btn);
        to_board_btn = rootView.findViewById(R.id.to_board_btn);

        // 책 검색어 입력 AutoCompleteTextView
        book_search = rootView.findViewById(R.id.book_search);


        // 책 직접 추가 버튼
        book_add_btn = rootView.findViewById(R.id.book_add_btn);

        //리사이클러뷰
        whole_list_recyclerview = rootView.findViewById(R.id.recyclerView_board_whole);
        recent_search_recyclerview = rootView.findViewById(R.id.recent_search_recyclerview);

        // 어댑터
        wholeAdapter = new BoardWholeAdapter(getActivity(), whole_list_recyclerview, this);




        // 어댑터 레이아웃 매니저 연결해주기
        // 1) 게시판 전체 목록
        whole_list_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        whole_list_recyclerview.setAdapter(wholeAdapter);


        // 2) 최근 검색어 목록
        searchRecentAdapter = new SearchRecentAdapter(recent_keyword_list, getActivity(), this, this);
        recent_search_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false)); // false 부터 해야. 아이템 왼쪽에서부터 시작함.

        // 레이아웃 방향 명시 (LTR: Left-to-Right)
        recent_search_recyclerview.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        recent_search_recyclerview.setAdapter(searchRecentAdapter);


        //2) 최근 검색어 관련 쉐어드랑 AutoCompleteTextView 드롭다운에 들어갈 내용에 대한 리스트
        // shared 이름: RecentSearches
        // shared 키 값: recent_searches

        // ArrayList 로 해야지 !! - 데이터가 적고 렌더링과 성능이 중요하다면, array list, 데이터 삽입 삭제가 자주 발생하고, 성능이 중요하다면 linked list.
        // 최근 검색어 같은 경우, 데이터 크기가 크지 않고, 정렬과 순차 접근이 많으므로 array list 가 적합함.

        // 불러오기 - on create 에서 들어오자마자 불러오는거임 !!
//        List<String> recentSearchList = getRecentSearches(); // 최근 검색어 쉐어드에서 전부 가져옴
//        // 어댑터 설정
//        RecentSearchAdapter adapter = new RecentSearchAdapter(getActivity(), recentSearchList);
//        book_search.setAdapter(adapter);
//
//        // 최근 검색 기록 어댑터 잘 연결됐는지 확인하기.
//        if (book_search.getAdapter() != null) {
//            Toast.makeText(getActivity(), "book_search adapter 연결성공 !!!", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getActivity(), "book_search adapter 연결실 패 ㅜㅜ !!! ", Toast.LENGTH_SHORT).show();
//
//        }
        // ==================================== 초기화 끝 =============================================



        // cv1. 로그인 사용자 닉네임 추출
        nickname_text = rootView.findViewById(R.id.nickname_text);
        if(!signin_email_id_value.equals("")) {
            // JSON 파싱하여 특정 키 값 추출
            try {
                JSONObject jsonObject = new JSONObject(signin_email_id_value);
                emailId = jsonObject.optString("emailid", ""); // default 값 설정 가능
                nickname = jsonObject.optString("nickname", ""); // nullpoint exception 발생할 수 있으므로, 빈문자열로 두기.

                get_userNumber(emailId);
                if(!nickname.equals("")) { // 빈값이 아니라면 textview에 값 넣어주기
                    Log.d("로그인한사용자아이디세팅됨", "현재로그인한 사용자 닉네임: " + nickname);
                    nickname_text.setText(nickname + "님, 안녕하세요");
                } else { //
                    Log.d("로그인한사용자아이디없음", "현재로그인한 사용자 닉네임없어???>> " + nickname);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else { // 쉐어드에 값이 없을 경우에 대한 예외처리
            Log.d("메인화면쉐어드비었음", "쉐어드왜비었죠? : 진짜 비었나? >>>  " + signin_email_id_value);
        }


        // cv2. 액티비티에서 바텀 네비게이션 가져오기
        if (getActivity() instanceof MainActivity) {  // MainActivity로부터 가져오기 // 이 과정이 꼭 필요함 !!!
            // 내가 지금 활동하는 액티비티가 올바른 액티비티인지 꼭 확인 !!
            MainActivity mainActivity = (MainActivity) getActivity();
            bottomNavigationView = mainActivity.findViewById(R.id.bottomNavigationView);
        }

        //cv3. 내 서재 바로 가기 - 클릭 이벤트
        to_lirary_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity().getApplicationContext() !=null) {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    FragmentMyLibrary fragmentMyLibrary = new FragmentMyLibrary();
                    transaction.replace(R.id.main_layout, fragmentMyLibrary).commit();

                    // bottom navi 아이템 선택
                    if(bottomNavigationView !=null ) {
                        bottomNavigationView.setSelectedItemId(R.id.mylabrary);
                    }
                } else {
                    Log.d("내서재 클릭-context오류", "main activity의 context없음.");
                }
            }
        });

        //cv3. 게시판 바로 가기 - 클릭 이벤트
        to_board_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity().getApplicationContext() !=null) {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    FragmentBoard fragmentBoard = new FragmentBoard();
                    transaction.replace(R.id.main_layout, fragmentBoard).commit();

                    // bottom navi 아이템 선택
                    if(bottomNavigationView !=null ) {
                        bottomNavigationView.setSelectedItemId(R.id.board);
                    }
                } else {
                    Log.d("게시판더보기-context오류", "main activity의 context없음.");
                }
            }
        });

        //cv4. '책 검색' 검색어 입력 후 엔터 입력시 book search activity 로 이동

        book_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String searchQuery = book_search.getText().toString().trim(); // 검색어 입력값

                    if(!searchQuery.isEmpty()) {

                        // 1. 검색어 SharedPreferences에 저장
                        saveRecentSearch(searchQuery);


                        // 2. 검색 결과 있는 곳으로 이동하기.
                        Intent intent = new Intent(getActivity(), BookSearchActivity.class);
                        intent.putExtra("SEARCH_QUERY", searchQuery); // 검색어 intent 에 추가
                        startActivity(intent);
                        // 입력했던 텍스트창 클리어하기.
                        v.setText("");


                    } else {
                        Toast.makeText(getActivity(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
                    }
                    return true; // 엔터키 처리 완료
                }
                return false; //  엔터 키 처리 안함.
            }
        });  // cv4 끝


        // cv5. 책 직접 추가 버튼 - 새로운 액티비티
        book_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BookAddSelfActivity.class);
                intent.putExtra("emailId", emailId);
                startActivity(intent);
            }
        });


//        book_add_btn.setOnLongClickListener(view -> {
//            TooltipCompat.setTooltipText(view, "책 직접 추가하기");
//            return true;
//        });





        // 검색어에 포커스된것을 철회하기 위해서
        // 레이아웃에 터치 리스너 추가하기
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboardAndClearFocus();
            }
            return false;
        });


        // AutoCompleteTextView에 포커스 이벤트 리스너 추가
        book_search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 포커스가 생기면 RecyclerView 보이기
                    // 조건 !! 걸어주깅 !!!쉐어드 empty 가 아닐때
                    ArrayList<String>  now_recent = new ArrayList<>();
                    now_recent = getRecentSearches();
                    if(now_recent.isEmpty()) {
                        recent_search_recyclerview.setVisibility(View.GONE);
                    } else {
                        recent_search_recyclerview.setVisibility(View.VISIBLE);
                    }
                } else {
                    // 포커스를 잃으면 RecyclerView 숨기기
                    recent_search_recyclerview.setVisibility(View.GONE);
                }
            }
        });




        return rootView;
    } // on create view


    // ===================================== methods =====================================

    private void hideKeyboardAndClearFocus() {
        View currentFocus = getActivity().getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0); // 키보드 닫기
            currentFocus.clearFocus(); // 포커스 해제
        }
    }



    // 프래그먼트홈) method 1. 유저 넘버 가져오기.
    public void get_userNumber (String user_emailid) {
        Log.i("홈프레그먼트에서 유저 넘버가져오기", "유저의 emailId: " + user_emailid);
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
                    Log.d("홈프레그먼트에서 on create의 get userNumber 메소드 안 !! 리사이클러뷰가져오기 ", "");
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


    // 프래그먼트홈) method 2.게시판 전체목록
    public void whole_setting (String user_number) {
        Log.e("게시판 전체목록 레트로핏 메소드진입", "매개변수 유저넘버: " + user_number);
        service.boardList_whole_paging(user_number).enqueue(new Callback<BoardListResponse.BoardResponse2>() {
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
                        Log.e("좋아요 댓글 개수", "좋아요: " + item.getLike_cnt() + ", 댓글: " + item.getComment_cnt());

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


    // 프래그먼트홈) method 3
    // 검색 할때마다 쉐어드에 저장하는 로직.
    private void saveRecentSearch(String searchQuery) {
        Toast.makeText(getActivity(), "saveRecentSearch 쉐어드저장 시도!", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 1. 기존 데이터 가져오기 (json 문자열)
        String jsonString = sharedPreferences.getString(SEARCH_KEY, "[]");

        // 2. json 문자열 -> array list <Map<String, String>> 변환
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        ArrayList<Map<String, String>> recentSearches = gson.fromJson(jsonString, listType);

        // 없으면 새로 만들어주기
        if (recentSearches == null) {
            recentSearches = new ArrayList<>();
        }

        // 3. 새 검색어 추가 (키워드랑 타임 스탬프 )
        Map<String, String> newSearch = new HashMap<>();
        newSearch.put("keyword", searchQuery);
        newSearch.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date()));
        recentSearches.add(newSearch);


        // 4. 최대 저장 개수를 설정 (예: 최근 10개만 유지)
        if (recentSearches.size() > 10) {
            recentSearches.remove(0); // 가장 오래된 항목 제거
        }

        String updatedJsonString = gson.toJson(recentSearches);

        // 최근 검색어 목록을 SharedPreferences에 다시 저장
        editor.putString(SEARCH_KEY, updatedJsonString);
        editor.apply();


        // 저장된 내용 로그로 출력
        Log.d("SharedPreferences", "updatedJsonString: " + recentSearches.toString());

    }


    // 프래그먼트홈) method 4.
    // 기존 저장된 쉐어드에서  불러오는 로직.
    private  ArrayList<String>  getRecentSearches(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

       // 1. 저장된 문자열 가져오기
        String jsonString= sharedPreferences.getString(SEARCH_KEY, "[]");  // Set<String>으로 가져오기

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        ArrayList<Map<String, String>> recentSearches = gson.fromJson(jsonString, listType);

        if (recentSearches == null) {
            recentSearches = new ArrayList<>();
        }

        // 여기서 파싱해줌 !! keyword 만 추출해줌
        ArrayList<String> keywords = new ArrayList<>();
        for (Map<String, String> search : recentSearches) {
            if (search.containsKey("keyword")) {
                keywords.add(search.get("keyword")); // 키워드값만 리스트에 추가하기
            }
        }


        // 로그 출력
        Log.d("getRecentSearches", "Recent Searches: " + recentSearches.toString());
        return keywords;
        // 키워드만 리턴하기 !!
        // 리사이클러뷰에 추가하기 위해서 !!

    }



    // 프래그먼트홈) method 5. 최근검색어 삭제. 인터페이스 메소드 오버라이드 !!

    @Override
    public void onSearchItemDeleted(String keyword) {

        // 쉐어드에서 삭제하기
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString(SEARCH_KEY, "[]"); // 기본값은 빈 배열

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        ArrayList<Map<String, String>> recentSearches = gson.fromJson(jsonString, listType);

        if (recentSearches == null) {
            recentSearches = new ArrayList<>();
        }

        // 해당 키워드에 맞는 항목을 찾아서 삭제
        Iterator<Map<String, String>> iterator = recentSearches.iterator();
        while (iterator.hasNext()) {
            Map<String, String> search = iterator.next();
            if (search.containsKey("keyword") && search.get("keyword").equals(keyword)) {
                iterator.remove();  // 해당 항목 삭제
            }
        }

        // 삭제된 후의 리스트를 다시 SharedPreferences에 저장
        String updatedJsonString = gson.toJson(recentSearches);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SEARCH_KEY, updatedJsonString);
        editor.apply();

        // 최근 검색어 갱신.
        ArrayList<String> re_recentSearches = getRecentSearches();
        // 어댑터 데이터 갱신
        searchRecentAdapter.updateData(re_recentSearches);


        // 최근 검색어가 비었을때 리사이클러뷰 안보이게 하기
        if (re_recentSearches.isEmpty()) {
            recent_search_recyclerview.setVisibility(View.GONE);
        } else {
            recent_search_recyclerview.setVisibility(View.VISIBLE);
        }
    }


    // 프래그먼트홈) method 6. 최근검색어 클릭. 인터페이스 메소드 오버라이드 !!

    @Override
    public void onSearchItemClicked(int position, String keyword) {

        // 포지션으로 json 에서 해당 키워드 가져온다. 그리고 그걸 검색창에 넣음.
        // position 만으로 가능하면 string keyword 안 가져와도 됨.
        // 반대야.. 걍 검색어를 가져오면 된거잖아 ? 일을 어렵게 만들지마.

        Log.d("onSearchItemClicked", "클릭 검색어: " + keyword);
        // 이 검색어를 가지고 !!!! 검색하러 가세요
        // 2. 검색 결과 있는 곳으로 이동하기.
        Intent intent = new Intent(getActivity(), BookSearchActivity.class);
        intent.putExtra("SEARCH_QUERY", keyword); // 검색어 intent 에 추가
        startActivity(intent);

    }



    // 프래그먼트홈) method 7. 좋아요 클릭
    @Override
    public void onLikeBtnClicked(int board_number, String status_tag) {

    }


    // ===================================== 생명 주기 초기화 =====================================

    // on start
    @Override
    public void onStart() {
        super.onStart();
    }

    // on resume
    @Override
    public void onResume() {
        super.onResume();

        // 최근 검색어 갱신.
        ArrayList<String> recentSearches = getRecentSearches();

        // 어댑터 데이터 갱신
        searchRecentAdapter.updateData(recentSearches);
        searchRecentAdapter.notifyDataSetChanged();

        Log.d("onResume", "최근 검색어 갱신됨: " + recentSearches);
    }

    // on pause
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
