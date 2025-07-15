package com.example.myapplication.layout.bottomnavi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.recyclerview.MylibraryAlreadyAdapter;
import com.example.myapplication.data.recyclerview.MylibraryAlreadyData;
import com.example.myapplication.data.recyclerview.MylibraryBookItemData;
import com.example.myapplication.data.recyclerview.MylibraryReadingAdapter;
import com.example.myapplication.data.recyclerview.MylibraryReadingData;
import com.example.myapplication.data.recyclerview.MylibraryWantAdapter;
import com.example.myapplication.data.recyclerview.MylibraryWantData;
import com.example.myapplication.data.recyclerview.MylibraryWholeAdapter;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.data.retrofit.responsemodel.MyLibraryListResponse;
import com.example.myapplication.layout.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentMyLibrary extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    // import

    private NavigationBarView bottomNavigationView; // 바텀 네비게이션 인스턴스 변수

    SharedPreferences sharedPreferences; // 쉐어드 초기화
    PreferenceManager pref;  // 쉐어드 매니저 초기화
    RetrofitService service; // 레트로핏 서비스
    //private LinearLayoutManager linearLayoutManager; // 리사이클러뷰를 위한 리니어 레이아웃
    private RecyclerView whole_list_recyclerview, already_list_recyclerview, reading_list_recyclerview, want_list_recyclerview;



    // 리사이클러뷰 어댑터 모음
    MylibraryWholeAdapter wholeAdapter; // 전체
    MylibraryAlreadyAdapter alreadyAdapter; // 읽은 책
    MylibraryReadingAdapter readingAdapter; // 읽고 있는 책
    MylibraryWantAdapter wantAdapter; // 읽고 싶은 책



    // view
    Toolbar toolbar; // 툴바
    Button whole_btn, already_btn, reading_btn, want_btn;
    EditText mylibrary_search;


    // string
    String user_number;


    // string
    String key = "signin_email_id"; // 쉐어드 키
    String signin_email_id_value ; // 쉐어드 값 (이메일아이디, 닉네임 json string 값 - 파싱 전)



    public FragmentMyLibrary() {
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
    public static FragmentMyLibrary newInstance(String param1, String param2) {
        FragmentMyLibrary fragmentMyLibrary = new FragmentMyLibrary();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragmentMyLibrary.setArguments(args);
        return fragmentMyLibrary;
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

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.bmnavi_mylibrary, container, false);

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

        // 쉐어드
        // 쉐어드 관련 클래스 및 값 초기화
     //   pref = new PreferenceManager();
        sharedPreferences = getActivity().getSharedPreferences("session_contain", Context.MODE_PRIVATE);
        signin_email_id_value = sharedPreferences.getString(key, "");

        // 검색창
        mylibrary_search = rootView.findViewById(R.id.mylibary_search);

        // 버튼
        whole_btn = rootView.findViewById(R.id.whole_btn);
        already_btn = rootView.findViewById(R.id.already_btn);
        reading_btn = rootView.findViewById(R.id.reading_btn);
        want_btn = rootView.findViewById(R.id.want_btn);

        // 리사이클러뷰 <전체, 읽은, 읽고 있는, 읽고 싶은 >어댑터, context 를 초기화하는 생성자가 있어서 아래 생성할 수 있음.
        wholeAdapter = new MylibraryWholeAdapter(getActivity());
        alreadyAdapter = new MylibraryAlreadyAdapter(getActivity());
        readingAdapter = new MylibraryReadingAdapter(getActivity());
        wantAdapter = new MylibraryWantAdapter(getActivity());



        // 리사이클러뷰
        whole_list_recyclerview = rootView.findViewById(R.id.recyclerView_whole);
        already_list_recyclerview = rootView.findViewById(R.id.recyclerView_already);
        reading_list_recyclerview = rootView.findViewById(R.id.recyclerView_reading);
        want_list_recyclerview = rootView.findViewById(R.id.recyclerView_want);

        // 전체 보기 레이아웃 매니저, 어댑터 setting
        whole_list_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        whole_list_recyclerview.setAdapter(wholeAdapter);

        // 읽은 책 어댑터 setting
        already_list_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        already_list_recyclerview.setAdapter(alreadyAdapter);

        // 읽고 있는 책 어댑터
        reading_list_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        reading_list_recyclerview.setAdapter(readingAdapter);

        // 읽고 싶은 책 어댑터
        want_list_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        want_list_recyclerview.setAdapter(wantAdapter);



        // 메인 액티비티로부터 현재 user number 가져오기.
       // userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // 처음 진입했을 때에는 "전체" 리사이클러뷰를 띄워줌.
        setting_whole();

        // 사용 XXXXXXXXXXXXXXXXXXXX
        // ViewModel에서 userNumber 값 가져오기
      // user_number = userViewModel.getUserNumber();
     //  Log.e("내서재 프래그먼트 on create", " main activity로부터 유저넘버: " + user_number);


        // ==================================== 초기화 끝 ===========================================

        //  액티비티에서 바텀 네비게이션 가져오기
        if (getActivity() instanceof MainActivity) {  // MainActivity로부터 가져오기 // 이 과정이 꼭 필요함 !!!
            // 내가 지금 활동하는 액티비티가 올바른 액티비티인지 꼭 확인 !!
            MainActivity mainActivity = (MainActivity) getActivity();
            bottomNavigationView = mainActivity.findViewById(R.id.bottomNavigationView);
        }



        // 툴바 홈 이모티콘 클릭 시, 홈으로 이동하기.
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



        // 로그인 한 사용자 닉네임
        if(!signin_email_id_value.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(signin_email_id_value);
                String emailId = jsonObject.optString("emailid", ""); // default 값 설정 가능
               // String nickname = jsonObject.optString("nickname", ""); // nullpoint exception 발생할 수 있으므로, 빈문자열로 두기
                get_userNumber(emailId);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("메인화면쉐어드비었음", "쉐어드왜비었죠? : 진짜 비었나? >>>  " + signin_email_id_value);
//            Toast.makeText(getApplicationContext(), " 저장 액티비티 진입 실패. 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
//            finish();
        }



        // < 전체 > 버튼 클릭 이벤트.
        // 클릭시 !! 스크롤 맨위로 옮기는법 - 같은 페이지에서 로딩할 경우를 위해.

        whole_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting_whole(); // <전체> 버튼, 리사이클러뷰 visibility visible
                Log.d("전체 목록 클릭함!! 리사이클러뷰가져오기 ", "");
                wholeAdapter.clearItem(); // 다시 뷰 로딩할때 기존 리사이클러뷰 삭제하고 로딩.
                whole_Setting(user_number); // 리사이클러뷰에 전체 목록 가져오기
            }
        });


        // <읽은 책> 버튼 클릭 이벤트

        already_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting_already();
                // 읽은 책 목록 가져오기
                alreadyAdapter.clearItem();
                already_list_Setting(user_number);
            }
        });


        // <읽고 있는 책> 버튼 클릭 이벤트
        reading_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting_reading();
                // 읽고 있는 책 목록 가져오기
                readingAdapter.clearItem();
                reading_list_Setting(user_number);
            }
        });


        // <읽고 싶은 책> 버튼 클릭 이벤트
        want_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting_want();
                // 읽고 싶은 책 목록
                wantAdapter.clearItem();
                want_list_Setting(user_number);
            }
        });


        return rootView;

    } // on create view


    // 전체 세팅 - 초기에도 이걸로 세팅해주기.
    private void setting_whole() {
        // 버튼
        whole_btn.setBackgroundColor(getResources().getColor(R.color.main)); // 기존 색상 설정
        already_btn.setBackground(null); // 색상 없음
        reading_btn.setBackground(null);
        want_btn.setBackground(null);

        // 리사이클러뷰
        whole_list_recyclerview.setVisibility(View.VISIBLE);
        already_list_recyclerview.setVisibility(View.GONE);
        reading_list_recyclerview.setVisibility(View.GONE);
        want_list_recyclerview.setVisibility(View.GONE);
    }


    // 읽은 책 세팅
    private void setting_already() {
        // 버튼
        whole_btn.setBackground(null);
        already_btn.setBackgroundColor(getResources().getColor(R.color.main)); // 기존 색상 설정 // 색상 없음
        reading_btn.setBackground(null);
        want_btn.setBackground(null);

        // 리사이클러뷰
        whole_list_recyclerview.setVisibility(View.GONE);
        already_list_recyclerview.setVisibility(View.VISIBLE);
        reading_list_recyclerview.setVisibility(View.GONE);
        want_list_recyclerview.setVisibility(View.GONE);
    }

    // 읽고 있는 책 세팅
    private void setting_reading() {
        // 버튼
        whole_btn.setBackground(null);
        already_btn.setBackground(null);
        reading_btn.setBackgroundColor(getResources().getColor(R.color.main)); // 기존 색상 설정
        want_btn.setBackground(null);

        // 리사이클러뷰
        whole_list_recyclerview.setVisibility(View.GONE);
        already_list_recyclerview.setVisibility(View.GONE);
        reading_list_recyclerview.setVisibility(View.VISIBLE);
        want_list_recyclerview.setVisibility(View.GONE);
    }


    // 읽고 싶은 책
    private void setting_want() {
        // 버튼
        whole_btn.setBackground(null);
        already_btn.setBackground(null); // 색상 없음
        reading_btn.setBackground(null);
        want_btn.setBackgroundColor(getResources().getColor(R.color.main)); // 기존 색상 설정

        // 리사이클러뷰
        whole_list_recyclerview.setVisibility(View.GONE);
        already_list_recyclerview.setVisibility(View.GONE);
        reading_list_recyclerview.setVisibility(View.GONE);
        want_list_recyclerview.setVisibility(View.VISIBLE);
    }

    // DB 에서 가져와서 리사이클러뷰 세팅.



    // = =============== <<<< 전 체 보 기 리 사 이 클 러 뷰 setting >>>>
    public void whole_Setting(String user_number) {
        Log.e("내서재 전체목록 레트로핏 메소드진입", "매개변수 유저넘버: " + user_number);
        service.mylibrary_wholesetting(user_number).enqueue(new Callback<MyLibraryListResponse.MyLibraryResponse2>() {
            @Override
            public void onResponse(Call<MyLibraryListResponse.MyLibraryResponse2> call, Response<MyLibraryListResponse.MyLibraryResponse2> response) {
                Log.e("내서재 전체목록 http 통신 성공", " reponse 진입함" );
                MyLibraryListResponse.MyLibraryResponse2 result = response.body();
                Log.e("내서재 전체목록 http 통신 성공", "결과 메시지: " + result.getMessage());
                // response2에 있는 getter 사용한 것임.
                List<MyLibraryListResponse> library_item = result.getLibrary_items();

                if ( result.getMessage().equals("게시글이 없습니다")){
                    Log.e("게시글이 없음", " reponse 진입함" );
                    wholeAdapter.clearItem();

                } else {
                    wholeAdapter.clearItem(); // 한번 삭제하고 가져오기. 전체적으로 add 하기 전에 삭제 !!

                    // 전체 내 서재 목록 출력
                    for (MyLibraryListResponse item : library_item) {
                        // 가져온 데이터가 무엇인지... add 하기. /
                        // 타입 유형에 따라서 add 따로 해주기 타입
                        // type도 string에 담겨있습니다.

                        // type별 저장하는 목록이 다름 ~!
                        if (item.getType().equals("1")) { // 읽은 책
                            Log.i("전체목록 <읽은 책> 결과 >>>  ", "내서재 각테이블번호"+ item.getMylibrary_number()+  "title: " + item.getTitle() + ", Author: " + item.getAuthor() +
                                    "Description: " + item.getCover() + ", 읽은책 시작: " + item.getStarted() + ", 읽은책 종료: " + item.getFinished() +
                                    ", 평점: " + item.getRating() + ", 서재 작성자: " + item.getMylibrary_user_number() + ", 책저장타입: " + item.getSave_type() ) ;
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
                            wholeAdapter.addItem(new MylibraryBookItemData(library_number, lib_user_number, book_number, bookCover, bookTitle, bookAuthor, null, started, finished, rating, null, created, type, bookSaveType));


                        } else if (item.getType().equals("2")) { // 읽고 있는 책
                            Log.i("전체목록  <읽고 있는 책> 결과 >>>  ", "내서재 각테이블번호"+ item.getMylibrary_number()+  "title: " + item.getTitle() + ", Author: " + item.getAuthor() +
                                    "Description: " + item.getCover() + ", 읽고 있는 책 시작: " + item.getStarted() + ", 읽은 페이지: " + item.getReadPage() + ", 서재 작성자: " + item.getMylibrary_user_number());
                            int library_number = item.getMylibrary_number();
                            int book_number = item.getBook_number();
                            int lib_user_number = item.getMylibrary_user_number();
                            String bookSaveType = item.getSave_type();
                            String bookCover = item.getCover();
                            String bookTitle = item.getTitle();
                            String bookAuthor = item.getAuthor();
                            String started = item.getStarted();
                            String readPage = item.getReadPage();
                            String created = item.getCreatedTime();
                            int type = Integer.parseInt(item.getType());
                            wholeAdapter.addItem(new MylibraryBookItemData(library_number,lib_user_number, book_number, bookCover, bookTitle, bookAuthor, readPage, started, null, null, null, created, type,bookSaveType ));

                        } else if (item.getType().equals("3")) {
                            Log.i("전체목록  <읽고 싶은 책> 결과 >>>  ", "내서재 각테이블번호"+ item.getMylibrary_number()+  "title: " + item.getTitle() + ", Author: " + item.getAuthor() +
                                    "Description: " + item.getCover() + ", 읽고 싶은 책 기대지수: " + item.getRating() + ", 기대평: " + item.getPreview() + ", 서재 작성자: " + item.getMylibrary_user_number());
                            int library_number = item.getMylibrary_number();
                            int book_number = item.getBook_number();
                            int lib_user_number = item.getMylibrary_user_number();
                            String bookSaveType = item.getSave_type();
                            String bookCover = item.getCover();
                            String bookTitle = item.getTitle();
                            String bookAuthor = item.getAuthor();
                            String rating = item.getRating();
                            String preview = item.getPreview();
                            String created = item.getCreatedTime();
                            int type = Integer.parseInt(item.getType());
                            wholeAdapter.addItem(new MylibraryBookItemData(library_number,lib_user_number, book_number, bookCover, bookTitle, bookAuthor, null, null, null, rating, preview, created, type, bookSaveType));
                        }  // 전체 목록 카테고리 나누기 끝 if else
                    } // for문 끝 - 전체 목록 가져오기 from response2
                }


            } // on response 끝
            @Override
            public void onFailure(Call<MyLibraryListResponse.MyLibraryResponse2> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("전체 목록 가져오기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("전체 목록 가져오기 실패. ", "onFailure: " + throwable.getCause());
            }
        });
    }



    // = =============== <<<< 읽은 책  리 사 이 클 러 뷰 setting >>>>

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
                           alreadyAdapter.addItem(new MylibraryAlreadyData(library_number, lib_user_number, book_number, bookCover, bookTitle, bookAuthor, rating, started, finished, created, type, bookSaveType));
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
    }

    // = =============== <<<< 읽고 있는 책 - 리 사 이 클 러 뷰 setting >>>>

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
                            readingAdapter.addItem(new MylibraryReadingData(library_number, lib_user_number, book_number, bookCover, bookTitle, bookAuthor, readPage, started, created, type ));
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
    }


    // =============== <<<< 읽고 싶은 책 - 리 사 이 클 러 뷰 setting >>>>
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





    // 유저 넘버 가져오는 메소드.
    public void get_userNumber (String user_emailid) {
        Log.i("내서재 프래그먼트에서 유저넘버가져오기", "유저의 emailId: " + user_emailid);
        service.user_emailId(user_emailid).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("내서재-유저 number 체크 response 진입", "onResponse: 응답메시지: " + result.getMessage()); // echo로 보내주는 메시지.
                Log.e("내서재-유저 number 체크 response 진입", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.

                if(result.getCode() == 200 ) {
                    Log.e("유저 number 체크성공 / code==200!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                   user_number = result.getUser_number();
//                    // view model - userNumber 값 설정
//                    userViewModel.setUserNumber(user_number);
                    Log.d("내서재 프래그먼트의 on create의 get userNumber 메소드 안 !! 리사이클러뷰가져오기 ", "");
                    wholeAdapter.clearItem(); // 혹시 모르니 삭제하고 가져오기.
                    whole_Setting(user_number);

                } else {
                    Log.e("php에서 뭔가 잘못됨. / code==400!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Log.e("내서재-유저넘버찾기 통신 failed", "실패원인: " +throwable.getMessage());
            }
        });
    }


    // 생명 주기 초기화


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        // 현재 어떤 레이아웃이 visible인지에 따라서 리사이클러뷰 다시 로딩하기.
        if(whole_list_recyclerview.getVisibility()== View.VISIBLE) {
            Log.d("프래그먼트의 on resume !!  리사이클러뷰가져오기 ", "");
            wholeAdapter.clearItem();
            whole_Setting(user_number);

        } else if (already_list_recyclerview.getVisibility() == View.VISIBLE) {
            alreadyAdapter.clearItem();
            already_list_Setting(user_number);

        } else if (reading_list_recyclerview.getVisibility() == View.VISIBLE) {
            readingAdapter.clearItem();
            reading_list_Setting(user_number);

        } else if (want_list_recyclerview.getVisibility() == View.VISIBLE) {
            wantAdapter.clearItem();
            want_list_Setting(user_number);
        }

    }

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
