package com.example.myapplication.layout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.recyclerview.SearchBookAdapter;
import com.example.myapplication.data.recyclerview.SearchBookData;
import com.example.myapplication.data.retrofit.RetrofitClient_aladin;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.AladinResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookSearchActivity extends AppCompatActivity {



    // 쉐어드 프리퍼런스
    PreferenceManager pref; // 쉐어드 프리퍼런스 - 로그인 유지용
    String key = "signin_email_id";
    String current_login_memberInfo; // 쉐어드에 저장된 이메일/아이디, 닉네임 키값쌍.
    String emailId ; // 이메일/아이디만 파싱한 값.


    // import
    RetrofitService service; // 레트로핏 서비스
    SearchBookAdapter bookAdapter; // 책검색 결과 리사이클러뷰 어댑터
    private LinearLayoutManager linearLayoutManager; // 리사이클러뷰를 위한 리니어 레이아웃
    private RecyclerView recyclerView;

    // view
    Toolbar toolbar; // 툴바
    EditText book_search; // 책 검색어 입력창
    TextView search_no_result; // 검색 결과 없을 때

    // int
    private int currentPage = 1; // 현재 페이지 번호
    private final int maxResults = 10; // 한 페이지당 결과 개수
    int total_page =0; // 결과 값에 대한 총 페이지 수. // 문제가 생겼을때 결과값이 0이 되도록.

    // boolean
    private boolean isLoading = false; // 페이징 로딩 상태를 추적

    String ttbkey = "ttbnncn98631409001";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchbook_list);


        // 쉐어드에서 로그인 정보 존재 확인하기
        // 쉐어드
        // 쉐어드 - 로그인 정보 확인.
        pref = new PreferenceManager();
        current_login_memberInfo =   pref.getString(BookSearchActivity.this, key);
        Log.d("내서재저장oncreate 로그인회원", "로그인 회원정보from 쉐어드:  " + current_login_memberInfo);

        try {
            JSONObject jsonObject = new JSONObject(current_login_memberInfo);
            emailId = jsonObject.optString("emailid", "");
            Log.d("내서재저장oncreate 로그인회원", "로그인 회원 이메일: " + emailId);
        } catch (JSONException e) {
            e.printStackTrace();
            emailId = ""; // 기본값 설정
        }

        // ======================= 초기화 zone 시작 =================================

        // 알라딘 레트로핏 객체 정의
        service = RetrofitClient_aladin.getClient().create(RetrofitService.class);

        // 툴바
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }

        // 넘어온 검색어 intent
        Intent intent = getIntent();
        String whatIsearched = intent.getStringExtra("SEARCH_QUERY");
        Toast.makeText(BookSearchActivity.this, "입력한 검색어 : " + whatIsearched, Toast.LENGTH_SHORT).show();

        // 넘어온 검색어를 가지고, search_send 호출.
        search_send(ttbkey, whatIsearched, currentPage);

        // view
        book_search = findViewById(R.id.book_search); // 책 검색어 입력창
        book_search.setText(whatIsearched); // 화면 넘어왔을때 사용자가 입력한 검색어 setting 해주기.
        bookAdapter = new SearchBookAdapter(BookSearchActivity.this);
        search_no_result = findViewById(R.id.search_no_result); // 책 검색 결과 없을때 메시지 띄우기


        // recycler view
        recyclerView = (RecyclerView) findViewById(R.id.searched_booklist);
        linearLayoutManager = new LinearLayoutManager(BookSearchActivity.this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(linearLayoutManager); // 이거는 왜 하는거임??
        recyclerView.setAdapter(bookAdapter);


        // ======================= 초기화 zone 끝 =================================


        // bs1. 툴바 뒤로 가기 클릭시, 이전 화면으로 (home fragment)
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // 뒤로 가기 동작 수행
            }
        }); // bs1 끝


        // bs2. 책 검색 기능 활성화.
        book_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String searchQuery = book_search.getText().toString().trim(); // 검색어 입력값
                    if(!searchQuery.isEmpty()) { // 입력값 있을 때 엔터 누를 경우.
                        // 에디트 텍스트 "" 빈값 만들지 않기.
                        bookAdapter.clearItem(); // 재검색시 리사이클러뷰 지워주기.
                        currentPage = 1;
                        total_page = 0; // 재검색시 결과 페이지 다시 0으로.

                        // 이전 결과에 대해서 다시 돌려놓기
                        search_no_result.setText("");
                        search_no_result.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        search_send(ttbkey, searchQuery, currentPage);
                        Toast.makeText(BookSearchActivity.this, "입력한 검색어2 : " + searchQuery, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(BookSearchActivity.this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
                    }
                    return true; // 엔터키 처리 완료
                }
                return false; //  엔터 키 처리 안함.
            }
        }); // bs2 끝


        // bs3. 책 검색 결과 페이징 처리 (스크롤 리스너 사용)
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!isLoading) { // 로딩 중이 아닐 때만 다음 페이지 요청
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    Log.i("스크롤리스너!!로딩중아닐때", "마지막보이는아이템position: " + lastVisibleItemPosition);
                    // 실시간으로 따라서셈.
                    int totalItemCount = layoutManager.getItemCount();
                    Log.i("스크롤리스너!!로딩중아닐때", "현재 전체아이템개수: " + totalItemCount);

                    if (lastVisibleItemPosition >= totalItemCount - 1) {
                        Log.i("여기이해안됨.", "last visible position AND total item cnt " + lastVisibleItemPosition + " AND " + totalItemCount );
                        // 그니까 last visible은 현재 사용자가 보는 페이지에서 마지막줄 아이템의 position을 가져옴. 현재 4개가 한화면에 나오니
                        // 4부터 내릴때마다 5,6,7, 이렇게 증가함.

                        // total item cnt는 리사이클러뷰에 담긴 아이템의 수임.
                        // 10개씩 가져오기 때문에 페이지 로드할때마다 10개, 20개, 이렇게 10씩 증가함.

                        isLoading = true; // 로딩 시작
                        loadNextPage(); // 다음 페이지 요청
                    }
                }
            }
        }); // bs3 끝


        // bs 4. // 검색된 아이템 클릭 이벤트 - 상세보기 넘어가는 인터페이스 메소드 구현하기
        bookAdapter.setOnItemClickListener(new SearchBookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                SearchBookData item = bookAdapter.getItem(position);

                if( emailId.equals("")) {
                    Toast.makeText(BookSearchActivity.this, " 상세페이지 진입 실패. 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                } else { // 이메일이나 아이디 값이 있어야지, 책 상세보기 진입가능.
                    Intent intent = new Intent(BookSearchActivity.this, BookViewActivity.class);
                    intent.putExtra("emailId", emailId);
                    intent.putExtra("title", item.getTitle());
                    intent.putExtra("author", item.getAuthor());
                    intent.putExtra("description", item.getDescription());
                    intent.putExtra("publisher", item.getPublisher());
                    intent.putExtra("pubDate", item.getPubDate());
                    intent.putExtra("cover", item.getCover());
                    intent.putExtra("isbn", item.getIsbn());
                    BookSearchActivity.this.startActivity(intent);
                }

            }
        });


    }  // on create 끝.


    // bs method 1. 알라딘 api 호출
    private void search_send(String ttbkey, String keyword, int page) {
        Log.e("알라딘검색메소드", "키: " + ttbkey + " 검색어 keyword : "+keyword + "요청 페이지: " + page);
        service.getSearchBook(ttbkey,
                        keyword,
                        "Keyword",
                        maxResults,
                        page,
                        "Book",
                        "JS",
                        "20131101").enqueue(new Callback<AladinResponse.AladinResponse2>() {
            @Override
            public void onResponse(Call<AladinResponse.AladinResponse2> call, Response<AladinResponse.AladinResponse2> response) {
                AladinResponse.AladinResponse2 result = response.body();
                Log.e("알라딘api 성공", "onResponse: " + response.body());

                // 책 목록 가져오기.
                List<AladinResponse> books = result.getBooks();
                int result_total = result.getTotalResults();
                int item_perpage_cnt = result.getItemsPerPage();
                Log.i("검색결과 개수 및 페이지수 ", "총 개수: " + result_total + " , 페이지당 아이템개수: " + item_perpage_cnt );

                if(result_total == 0) {

                    // 리사이클러뷰 안보이게하기.
                    recyclerView.setVisibility(View.GONE);

                    // 검색 결과가 없습니다 text view 보이게 하기
                    search_no_result.setVisibility(View.VISIBLE);
                    // 작은 따옴표를 포함하여 텍스트 설정
                    search_no_result.setText("'" + keyword + "'에 대한 검색 결과가 없습니다");


                } else { // 결과 값이 1개 이상일 때
                    // 총 페이지수
                    total_page = (int) Math.ceil((double) result_total/(double) maxResults);

                    // 책 목록 - 각 책 정보 출력
                    for (AladinResponse book : books ) {
                        Log.i("검색결과책 정보 >>>  ", "Title: " + book.getTitle() + ", Author: " + book.getAuthor() +
                                "Description: " + book.getDescription() + ", Publisher: " + book.getPublisher() +
                                ", PubDate: " + book.getPubDate() + ", Cover: " + book.getCover() + ", ISBN: "+ book.getIsbn());
                        String title = book.getTitle();
                        String author =  book.getAuthor();
                        String description = book.getDescription();
                        String publisher =  book.getPublisher();
                        String pubDate = book.getPubDate();
                        String cover = book.getCover();
                        String isbn = book.getIsbn();
                        // add search book adapter
                        bookAdapter.addItem(new SearchBookData(title, author, description, publisher, pubDate, cover, isbn));
                        Log.i("검색책목록개수!! ", "getItemCount: " + bookAdapter.getItemCount());
                    }
                    isLoading = false; // 로딩 완료 후 플래그 초기화.
                }

            }

            @Override
            public void onFailure(Call<AladinResponse.AladinResponse2> call, Throwable throwable) {
                throwable.getMessage();
                Log.e("알라딘api 실패", "onFailure: " + throwable.getMessage());
            }
        });

    } // search send 끝



    // bs method 1. 페이지 load more
    private void loadNextPage(){
        currentPage++; //  전역 변수이므로 1씩 더하면 됨.
        String searching_word = book_search.getText().toString();
        Log.i("다음페이지 요청!!", "요청페이지: " + currentPage + ", 전체페이지수: " + total_page);
        // 1을 더한 값이 total page 보다 클때. 안보내기.
        if (currentPage>total_page) {

            Toast.makeText(BookSearchActivity.this, "마지막 페이지 입니다.", Toast.LENGTH_SHORT).show();
        } else {
            search_send(ttbkey,searching_word,currentPage );
        }


    }







}
