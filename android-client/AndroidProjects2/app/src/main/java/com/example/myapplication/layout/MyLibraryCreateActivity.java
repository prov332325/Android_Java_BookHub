package com.example.myapplication.layout;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.BoringLayout;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.data.retrofit.responsemodel.MylibraryResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyLibraryCreateActivity extends AppCompatActivity {


    // import
    RetrofitService service; // 레트로핏 서비스
    // 툴바
    Toolbar toolbar;


    // 읽은책인지, 읽고 있는 책인지, 읽고 싶은 책인지 boolean
    Boolean alreay_read = false;
    Boolean is_reading = false;
    Boolean want_read = false;


    // View
    Button mylibrary_save_btn; // 제출 버튼
    Button book_alreadyread_btn, book_reading_btn, book_wantread_btn; // 읽은책, 읽고 있는 책, 읽고 싶은 책. button
    RatingBar ar_ratingstar, want_ratingstar; // 읽은책 평점, 읽고 싶은 책 기대평점 Rating bar

    // visibility 조절하는데도 뷰 초기화 해야하나? 네.
    LinearLayout book_alreay_lay, book_reading_lay, book_want_lay; // Linear bar

    // String
    // 인텐트로 넘겨받은 유저 및 책 정보
    String current_user_emailId, saving_title, saving_author, saving_description, saving_publisher, saving_pubDate, saving_cover, saving_isbn, flag;
    String current_userNumber;


    // 읽은 책
    TextView ar_date_started, ar_date_finished, ratingValue; // 읽은 책의 시작일, 종료일, 평점


    // 읽고 있는 책
    EditText reading_page; // 읽고 있는 책 - 읽은 페이지
    TextView reading_started, reading_been; // 읽고 있는 책 - 시작일, 며칠됐는지


    // 읽고 싶은 책
    EditText want_preview; // 기대지수rating bar-want_ratingstar, 기대평
    TextView ratingValue_want; // 기대지수 text view


    // 직접 추가한 책에 추가된 사진.
    File book_self_imgFile = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mylibrary_create);


        // 툴바
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }


        // 레트로핏 초기화
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의


        // bvc0. 툴바 뒤로 가기 클릭시, 이전 화면으로 (home fragment)
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // 뒤로 가기 동작 수행
            }
        }); // bs1 끝


        // ======================= 초기화 zone 시작 =================================


        // mylic1. 인텐트로 넘어온 값 확인하기.
        current_user_emailId = getIntent().getStringExtra("emailId");
        // 넘어온 값이 null 이 아닐때 !! 회원 user number 구하는 http 통신 보내기.
        if (current_user_emailId.equals("")) {
            // 뒤로 가기.
            Toast.makeText(getApplicationContext(), " 저장 액티비티 진입 실패. 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // 유저의 user number 가져오기.
            Log.i("내 서재 create 유저", "현재 유저의 번호!!가져오려고합니다?: " + current_user_emailId);
            get_userNumber(current_user_emailId);
        }

        flag = getIntent().getStringExtra("FROM_FLAG"); // 직접 추가한 책인지, 알라딘 사용한 것인지
        saving_title = getIntent().getStringExtra("title");
        saving_author = getIntent().getStringExtra("author"); // 책 작가.
        saving_description = getIntent().getStringExtra("description");// 책 소개
        saving_publisher = getIntent().getStringExtra("publisher");// 출판사
        saving_pubDate = getIntent().getStringExtra("pubDate"); // 출판 날짜 (지은이 옆에 같이 setting 해주기)
        saving_cover = getIntent().getStringExtra("cover"); // 표지
        saving_isbn = getIntent().getStringExtra("isbn"); // 책 고유 번호
        Log.i("내 서재 create 현재유저", "현재 유저 emailId: " + current_user_emailId);
        Log.i("내 서재 create 넘어옴", "saving_title: " + saving_title + ",saving_author: " + saving_author + "이미지 사진 길이: " + saving_cover.length() + ", savingisbn: " + saving_isbn);

        // mylic2. view 공통버튼 초기화하기

        mylibrary_save_btn = findViewById(R.id.mylibrary_save_btn); // 저장 버튼 !!

        book_alreadyread_btn = findViewById(R.id.book_alreadyread_btn); // 읽은 책
        book_reading_btn = findViewById(R.id.book_reading_btn); // 읽고 있는 책
        book_wantread_btn = findViewById(R.id.book_wantread_btn); // 읽고 싶은 책

        // 공통 rating bar 2개 - 기본값은 float.
        ar_ratingstar = findViewById(R.id.ar_ratingstar); // 읽은 책 평점
        want_ratingstar = findViewById(R.id.want_ratingstar); // 읽고 싶은 책 기대평점


        // 공통 레이아웃 세개 초기화.
        book_alreay_lay = findViewById(R.id.book_already_layout);
        book_reading_lay = findViewById(R.id.book_reading_layout);
        book_want_lay = findViewById(R.id.book_want_layout);

        // 1.읽은 책 =============================================

        // 1. 읽은책 view 요소
        ar_date_started = findViewById(R.id.ar_date_started); // 시작일 - 날짜 관련된 text view는 항상 현재 날짜 setting하기.
        ar_date_finished = findViewById(R.id.ar_date_finished); // 종료일

        // 1.오늘날짜 바로 setting
        ar_date_started.setText(get_today());
        ar_date_finished.setText(get_today());

        ratingValue = findViewById(R.id.ratingValue); // 읽은책 평점.


        // 2.읽고 있는 책 = ===========================================
        // 쪽수 edit text, 읽기시작한 날짜 text view, 읽은지 며칠 됐는지 text view
        reading_page = findViewById(R.id.reading_page);
        reading_started = findViewById(R.id.reading_started);
        reading_been = findViewById(R.id.reading_been);

        // 2. 읽기 시작한 날짜에 오늘 날짜 setting
        reading_started.setText(get_today());

        // 2. 읽은지 며칠 됐는지.
        //on create에서는 '날짜를 선택해주세요' 띄우기


        // 3. 읽고 싶은 책 view 요소 ============================
        want_preview = findViewById(R.id.want_preview); // 기대평
        ratingValue_want = findViewById(R.id.ratingValue_want);


        // 처음 진입시 "읽은 책" 으로 setting
        setting_already();


        // ======================= 초기화 zone 끝 =================================


        // 직접 추가한 책일 경우, cover 사진에 대한 파일 생성하는 로직.
        if (flag.equals("BOOK_SELF") && !saving_cover.equals("정보없음")) {
            Log.i("BOOK_SELF이고사진추가됨 ", "saving_cover길이 " + saving_cover.length());
            book_self_imgFile = createFileFromBase64(saving_cover);
            Log.i("BOOK_SELF이고사진추가됨 ", "saving_cover파일명 " + book_self_imgFile.getName().toString());

        } else if (flag.equals("BOOK_SELF") && saving_cover.equals("정보없음")) {
            Log.i("BOOK_SELF이고사진추가됨 - 없음", "saving_cover길이 " + saving_cover.length() + ",book_self_imgFile: " + book_self_imgFile); // book_self_imgFile 는 null 값이다.
        }


        // ========================== <<<<<<< 읽은 책 - 시작 !!!!!!!!>>>>>>> ==========================

        // 읽은책1. 읽은 책 버튼 눌렀을때
        book_alreadyread_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 색상 변경
                setting_already();
            }
        });

        // 읽은책2. 시작일 텍뷰 누르면 날짜 선택할 수 있도록.
        ar_date_started.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("시작일클릭함!! ", "onClick진입");
                showDatePickerDialog(ar_date_started);
                Log.i("시작일 세팅완료 ", "시작일: " + ar_date_started.getText().toString());
            }
        });

        // 읽은책3. 종료일 텍뷰
        ar_date_finished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("종료일 클릭함!!!! ", "onClick진입");
                showDatePickerDialog(ar_date_finished);
                Log.i("종료일 세팅완료 ", "종료일: " + ar_date_finished.getText().toString());
            }
        });


        // 읽은책 4. 평점
        ar_ratingstar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                // 사용자가 등급을 변경할 때마다 호출됩니다.
                float ratingValues = ar_ratingstar.getRating();
                Log.i("읽은책평점!!!", "평점몇점인가: " + rating);
                ratingValue.setText("(총 " + ratingValues + "점 / 5점)");
                Log.i("읽은책평점 setting!!", "읽은책평점 text view: " + ratingValue.getText().toString());


            }
        });

        // ========================== <<<<<<< 읽은 책 - 끝 !!!!!!!!>>>>>>> ==========================


        // ========================== <<<<<<< 읽고 있는 !!!! 책 - 시작 !!!!!!!!>>>>>>> ==========================

        // 읽고 있는책1. 읽고 있는 책 버튼 눌렀을때
        book_reading_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting_reading();
            }
        });


        // 읽고 있는책2. 읽은 책 쪽수
        // 이렇게 on create 에서 담은 걸 저장하려고 하면 오류남.
        // String read_page_cnt = reading_page.getText().toString();


        // 읽고 있는책3. 읽기 시작한 날짜.
        reading_started.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("읽고있는책-시작한날짜!! ", "onClick진입");
                showDatePickerDialog(reading_started);
            }
        });

        // 읽고 있는책4. 시작 날짜 text 변경을 감지해서, 변경되면, 시작한지 며칠됐는지 알려주기.
        reading_started.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 텍스트 변경 후
                String beendays = been_days(reading_started.getText().toString());
                Log.i("읽고있는책- ", "며칠됐나요: " + beendays);
            }
        });


        // ========================== <<<<<<< 읽고 """싶은""" 책 - 시작 !!!!!!!!>>>>>>> ==========================

        // 읽고 싶은책1. 읽고 싶은 책 버튼 눌렀을때
        book_wantread_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting_want();
            }
        });


        // 읽고 싶은 책 2. 기대 평점
        // 읽은책 4. 평점
        want_ratingstar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                // 사용자가 등급을 변경할 때마다 호출됩니다.
                float ratingValues = want_ratingstar.getRating();
                Log.i("읽고 싶은책 기대평점!!!", "기대평점: " + rating);
                ratingValue_want.setText("(총 " + ratingValues + "점 / 5점)");
                Log.i("읽고싶은책기대평점 setting!!", "읽고싶은책기대평점 text view: " + ratingValue_want.getText().toString());
            }
        });


        // ========================== <<<<<<< 읽고 """싶은""" 책 - 끝 !!!!!!!!>>>>>>> ==========================


        // 저장 버튼 클릭 !!! - 종류 별로 분기처리해서 http 통신 보내기 !!
        mylibrary_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 1. 세개 중 어떤게, visible인지 확인
                if (book_alreay_lay.getVisibility() == View.VISIBLE) {
                    alreay_read = true;
                    is_reading = false;
                    want_read = false;
                } else if (book_reading_lay.getVisibility() == View.VISIBLE) {
                    is_reading = true;
                    alreay_read = false;
                    want_read = false;
                } else if (book_want_lay.getVisibility() == View.VISIBLE) {
                    want_read = true;
                    alreay_read = false;
                    is_reading = false;
                }

                Log.i("저장눌렀을때 값은? ", ": ");


                // 저장 메소드 호출 시 확인할 것
                // 1. 각종 시작, 종료 데이터들의 관계가 맞는지.

                //   saving_title, saving_author, saving_description, saving_publisher, saving_pubDate, saving_cover,saving_isbn
                // 2. 위 변수가 전부 null 이 아닐때. cover 사진은 null 이어도 됨.

                // 3. 각각 값에 대한 null 을 어디까지 허용할 것인지 ?? A.사진만 null 가능!!일단!(0521)
                // 4. 평점의 기본 값이 2.5인데 이대로 저장할 것인지 ?? A. 넵(0521)

                // 1) 읽은 책인 경우
                if (alreay_read && !is_reading && !want_read) {
                    String started_date = ar_date_started.getText().toString();
                    Log.i("읽은책 저장버튼!!", "1.읽기 시작한날짜: " + ar_date_started.getText().toString());
                    String finished_date = ar_date_finished.getText().toString();
                    Log.i("읽은책 저장버튼!!", "2. 종료날짜: " + ar_date_finished.getText().toString());
                    String already_rating = String.valueOf(ar_ratingstar.getRating());
                    Log.i("읽은책 저장버튼!!", "3. 평점rating bar의 getRating(): " + already_rating); // string으로 바꾸기
                    //Log.i("읽은책 저장버튼!!", "4. 평점 text view : " + ratingValue.getText().toString());
                    // 읽은 책, insert 를 위한 http 통신 요청 메소드 호출
//                    book_already_insert(started_date, finished_date, already_rating);

                    if (flag.equals("BOOK_SELF") && book_self_imgFile != null ) {
                        // 플래그 값을 같이 보내도록 레트로핏에 변수 추가해야겠다. - X 그냥 파일을 하나
                        // 그냥 flag 값 intent 로 받은걸 그대로 넘기면됨. 그럼 받는 쪽에서 추가하면 되니까?
                        // 직접 추가이면서, 사진이 있는 경우
                        // 아니.. 사진도 넘겨야함.ㅋ
                        // 즉, 레트로핏 메소드가 3개 더 추가로 있어야함.
                        // 기존거 그대로 보내고, flag 가 book self 일 경우에 파일도 같이 보낼 수 있는 메소드로 연결해준다.
                        // 읽은 책2, 일고 있는책2, 읽고싶은책 2

                        // 플래그 추가하지 말고 그냥 php 파일을 추가하자.. 응ㅇ응...
                        saving_pubDate="정보없음";
                        book_already_insert2(started_date, finished_date, already_rating);
                    } else if(flag.equals("BOOK_SELF") && book_self_imgFile == null ) {
                        Log.i("읽은책저장 - 직접,사진없음", "3. 읽은지며칠? : " + reading_been.getText().toString());
                        book_already_insert(started_date, finished_date, already_rating, "SELF");
                    } else if (flag.equals("BOOK_SEARCH")) {
                        Log.i("읽은책저장 - 검색", "3. 읽은지며칠? : " + reading_been.getText().toString());
                        book_already_insert(started_date, finished_date, already_rating, "API");
                    }


                } // 2) 읽고 있는 책인 경우
                else if (is_reading && !alreay_read && !want_read) {
                    String read_page_cnt = reading_page.getText().toString();
                    Log.i("읽고 있는책 저장버튼 !!", "1.읽은 쪽수: " + read_page_cnt + "쪽"); // string 변수에 담아뒀던 것.은 안되지. on create 때 담아둔건데 ?? null 이죠?
                    String started_date = reading_started.getText().toString();
                    Log.i("읽고 있는책 저장버튼 !!", "2.시작일: " + reading_started.getText().toString()); // 시작 날짜 보여지는 text view
                    Log.i("읽고 있는책 저장버튼 !!", "3. 읽은지며칠? : " + reading_been.getText().toString()); // 시작 날짜 보여지는 text view //
                    // 읽고 있는 책, insert 를 위한 http 통신 요청 메소드 호출
                    book_reading_insert(read_page_cnt, started_date);

                } // 3) 읽고 싶은 책인 경우
                else if (want_read && !alreay_read && !is_reading) {
                    Log.i("읽고 싶은책 저장버튼!!", "1. 기대평점rating bar의 getRating(): " + want_ratingstar.getRating()); // string으로 바꾸기
                    String want_rating = String.valueOf(want_ratingstar.getRating());
                    Log.i("읽고 싶은책 저장버튼!!", "2. 기대평평점rating bar의 getRating(): " + ratingValue_want.getText().toString());
                    String want_preview_text = want_preview.getText().toString();
                    Log.i("읽고 싶은책 저장버튼!!", "3. 기대평 : " + want_preview.getText().toString());
                    // 읽고 싶은 책, insert를 위한 http 통신 요청 메소드 호출
                    book_want_insert(want_rating, want_preview_text);
                }
            }
        });

    } // on create


    // My library Method 1.
    // date picker dialog로 날짜를 표시하는 메소드 !!
    // 시작일, 종료일 등등에 실행 시켜주기.
    private void showDatePickerDialog(final TextView textView) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);


        // 기존 텍스트뷰에 작성된 날짜 가져와서 달력 시스템에 세팅해줌.
        String[] dateParts = textView.getText().toString().split("\\.");
        // 백슬레시 두개 "\\"는 이스케이프 문자이다. 뒤에 오는 문자를 그대로 표현, 인식하게 하기 위해서 사용
        // 즉, . 온점 자체를 문자 그대로 나타내기 위한 장치이다.
        if (dateParts.length == 3) {
            year = Integer.parseInt(dateParts[0]);
            month = Integer.parseInt(dateParts[1]) - 1; // date picker를 세팅하기 위해 1을 빼줌. 그러나, 사용자가 보는 값은 +1되어있음.
            dayOfMonth = Integer.parseInt(dateParts[2]);

        }

        // Date pick dialog 생성 및 설정
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) { // month 가 0부터 시작함. 항상 1 더해주기.

                        String realMonth;
                        if (month + 1 < 10) {
                            realMonth = "0" + (month + 1);
                        } else {
                            realMonth = String.valueOf(month + 1);
                        }

                        String realDate;
                        if (dayOfMonth + 1 < 10) {
                            realDate = "0" + (dayOfMonth);
                        } else {
                            realDate = String.valueOf(dayOfMonth);
                        }

                        Log.i("date picker날짜선택", "선택한날짜 년: " + year + ", 월: " + realMonth + ", 일: " + realDate);
                        String selected_date = year + "." + realMonth + "." + realDate;
                        textView.setText(selected_date); // 시작일, 혹은 종료일 세팅해주기.
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show(); // Q.이거 스피너야 ?? A. 아니야. 걍 캘린더형임. 나쁘지 않은 것 같음. 그대로하기.

    } // My library Method 1. 끝


    // My library Method 2. 오늘 날짜 반환하는 메소드.
    private String get_today() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        String realMonth;
        if (month + 1 < 10) {
            realMonth = "0" + (month + 1);
        } else {
            realMonth = String.valueOf(month + 1);
        }

        String realDate;
        if (dayOfMonth + 1 < 10) {
            realDate = "0" + (dayOfMonth);
        } else {
            realDate = String.valueOf(dayOfMonth);
        }

        String todayDate = year + "." + realMonth + "." + realDate;
        return todayDate;
    } // My library Method 2. 끝


    //  My library Method 3. 현재 오늘 날짜 기준으로, 며칠됐는지 계산하는 법
    private String been_days(String started_date) {
        // 날짜 포맷을 정해줌.
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");

        // 넘겨 받은 날짜를 포맷에 맞게 포매팅 한다음. Calender 객체에 넣어줌.
        try {
            Date started = format.parse(started_date); // 포맷팅
            Calendar started_calendar = Calendar.getInstance();
            started_calendar.setTime(started); // 포맷팅한 날짜를 캘린더 객체에 넣어줌.

            String today_date = get_today();
            Date today = format.parse(today_date);
            Calendar today_calendar = Calendar.getInstance();
            today_calendar.setTime(today);

            // 두개를 비교함. diffInDays
            long diffInMillis = today_calendar.getTimeInMillis() - started_calendar.getTimeInMillis(); // 오늘날짜에서, 전달받은 날짜 빼줌.
            long diffInDays = diffInMillis / (1000 * 60 * 60 * 24); // 날짜 day 로 변환해줌.

            // 만약 선택한 날짜가 오늘 이후날짜라면 set text "다시 선택해 주세요"
            //  맞다면 계산 후 setting
            if (0 > diffInDays) { // 오늘 보다 이후 날짜를 선택했을때. 마이너스됨.
                Log.i("날짜 계산결과", "0>diffInDays일때 diffInDays: " + diffInDays);
                reading_been.setText("날짜를 다시 선택해 주세요.");
                return null;
            } else {
                Log.i("날짜 계산결과", "else 일때 diffInDays: " + diffInDays);
                reading_been.setText("읽기 시작한 지" + String.valueOf(diffInDays + 1) + "일 됐어요");
                return String.valueOf(diffInDays);
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


    } //  My library Method 3. been days 끝


    //  My library Method 4. 레이아웃 세팅
    // view setting
    private void setting_already() {
        book_alreadyread_btn.setBackground(getDrawable(R.drawable.shape_rectangle_gray_round)); // 읽은책 연회
        book_reading_btn.setBackground(getDrawable(R.drawable.shape_rectangle_moregray_round)); // 읽고 있는 책 진회
        book_wantread_btn.setBackground(getDrawable(R.drawable.shape_rectangle_moregray_round)); // 읽고 싶은 책 진회

        // 해당 내용 변경. visible.
        book_alreay_lay.setVisibility(View.VISIBLE); // 읽은책 visible
        book_reading_lay.setVisibility(View.GONE); // 읽고 있는 책 gone
        book_want_lay.setVisibility(View.GONE); // 읽고 싶은 책 gone
    }

    private void setting_reading() {
        book_alreadyread_btn.setBackground(getDrawable(R.drawable.shape_rectangle_moregray_round)); // 읽은책 진회
        book_reading_btn.setBackground(getDrawable(R.drawable.shape_rectangle_gray_round)); // 읽고 있는 책 연회
        book_wantread_btn.setBackground(getDrawable(R.drawable.shape_rectangle_moregray_round)); // 읽고 싶은 책 진회

        // 해당 내용 변경. visible.
        book_alreay_lay.setVisibility(View.GONE); // 읽은책 gone
        book_reading_lay.setVisibility(View.VISIBLE); // 읽고 있는 책 visible
        book_want_lay.setVisibility(View.GONE); // 읽고 싶은 책 gone
    }


    private void setting_want() {
        // 버튼 색상 변경
        book_alreadyread_btn.setBackground(getDrawable(R.drawable.shape_rectangle_moregray_round)); // 읽은책 진회
        book_reading_btn.setBackground(getDrawable(R.drawable.shape_rectangle_moregray_round)); // 읽고 있는 책 진회
        book_wantread_btn.setBackground(getDrawable(R.drawable.shape_rectangle_gray_round)); // 읽고 싶은 책 연회

        // 해당 내용 변경. visible.
        book_alreay_lay.setVisibility(View.GONE); // 읽은책 gone
        book_reading_lay.setVisibility(View.GONE); // 읽고 있는 책 gone
        book_want_lay.setVisibility(View.VISIBLE); // 읽고 싶은 책 visible
    }


    //  My library Method 4. 레이아웃 세팅 끝
    ///////////////////////////////////////////
    //////////////////////////////////////////
    // 내 서재 저장하기 위한 통신 보내기.


    //  My library Method 5.
    // 1) 읽은 책 - 알라딘 추가했거나, 직접 추가했지만 사진 첨부 안한경우.
    public void book_already_insert(String started_date, String finished_date, String already_rating, String book_save_type) {
        Log.i("읽은 책 저장메소드실행 ", "시작날짜: " + started_date + ", 종료날짜: " + finished_date + ", 평점: " + already_rating);
        Log.i("book_already_insert 저장하려는 책내용 ", "제목: " + saving_title + ", 작가: " + saving_author + ", 책소개: " + saving_description + ", 저장타입: " +book_save_type + ",사진cover: " + saving_cover);

        Log.i("읽은 책 저장메소드실행 ", "현재 유저 넘버: " + current_userNumber);



        //   saving_title, saving_author, saving_description, saving_publisher, saving_pubDate, saving_cover,saving_isbn
        service.mylibrary_already(current_userNumber, saving_title, saving_author, saving_description, saving_publisher, saving_pubDate, saving_cover, saving_isbn,
                started_date, finished_date, already_rating, book_save_type).enqueue(new Callback<MylibraryResponse>() {
            @Override
            public void onResponse(Call<MylibraryResponse> call, Response<MylibraryResponse> response) {
                Log.e("통신성공!! 서버가 보내는 코드", "code: " + response.code()); //서버가 보내는 http 통신 응답코드
                MylibraryResponse result = response.body();
                Log.e("읽은 책 통신성공- 코드", "onResponse: " + result.getCode()); // echo로 보내주는 메시지.

                Log.e("읽은 책 통신성공", "onResponse: " + result.getMessage()); // echo로 보내주는 메시지.

                if (result.getCode() == 200) {
                    // 내 서재 "읽은 책" 저장 성공
                    Toast.makeText(MyLibraryCreateActivity.this, "내 서재에 읽은 책 저장이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MyLibraryCreateActivity.this, MainActivity.class);
                    intent.putExtra("fragment", "myLibrary");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 위 플래그를 설정하면 스택에 있는 기존 액티비티가 전부 제거됨.
                    startActivity(intent);
                    finish();
                } else {  // 코드 경우의 수는 200,400 두개임. 현재 400일 경우 = 실패일경우
                    Toast.makeText(MyLibraryCreateActivity.this, "내 서재에 읽은 책 저장을 실패하였습니다", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<MylibraryResponse> call, Throwable throwable) {
                // 통신 실패했을 때 원인을 출력하기 위한 메소드.
                throwable.getMessage();
                Log.e("읽은 책 통신 실패", "onFailure:실패한이유: " + throwable.getMessage());

            }
        });
    }  // 1) 읽은 책 끝


    // 1-1) 읽은 책 - 직접추가한 경우
    public void book_already_insert2(String started_date, String finished_date, String already_rating) {
        Log.i("book_already_insert2 메소드실행 ", "시작날짜: " + started_date + ", 종료날짜: " + finished_date + ", 평점: " + already_rating);
        Log.i("book_already_insert2 저장하려는 책내용 ", "제목: " + saving_title + ", 작가: " + saving_author + ", 책소개: " + saving_description);
        Log.i("book_already_insert2 저장메소드실행 ", "현재 유저 넘버: " + current_userNumber + ", isbn저장되니: " + saving_isbn);

        String mimeType = getMimeType(book_self_imgFile); // 선택한 사진이 png일 경우, jpeg일 경우에 대한 분기처리하는 메소드 호출.

        // 파일 데이터를 RequestBody로 변환
        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), book_self_imgFile);
        MultipartBody.Part cover = MultipartBody.Part.createFormData("cover", book_self_imgFile.getName(), requestFile);

        // 문자열 데이터를 RequestBody로 변환
        RequestBody userNumber = RequestBody.create(MediaType.parse("text/plain"), current_userNumber);
        RequestBody title = RequestBody.create(MediaType.parse("text/plain"), saving_title);
        RequestBody author = RequestBody.create(MediaType.parse("text/plain"), saving_author);
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), saving_description);
        RequestBody publisher = RequestBody.create(MediaType.parse("text/plain"), saving_publisher);
        RequestBody pubDate = RequestBody.create(MediaType.parse("text/plain"), saving_pubDate);
        RequestBody isbn = RequestBody.create(MediaType.parse("text/plain"), saving_isbn);
        RequestBody startedDate = RequestBody.create(MediaType.parse("text/plain"), started_date);
        RequestBody finishedDate = RequestBody.create(MediaType.parse("text/plain"), finished_date);
        RequestBody alreadyRating = RequestBody.create(MediaType.parse("text/plain"), already_rating);
        RequestBody book_save_type = RequestBody.create(MediaType.parse("text/plain"), "SELF");


        // 레트로핏 호출

        service.mylibrary_already2(userNumber, title, author, description, publisher, pubDate, cover, isbn, startedDate, finishedDate, alreadyRating, book_save_type)
                .enqueue(new Callback<MylibraryResponse>() {
                    @Override
                    public void onResponse(Call<MylibraryResponse> call, Response<MylibraryResponse> response) {
                        Log.e("직접추가-통신성공!! 서버가 보내는 코드", "code: " + response.code()); // 서버가 보내는 HTTP 응답 코드
                        MylibraryResponse result = response.body();
                        Log.e("직접추가-읽은 책 통신성공- 코드", "onResponse: " + result.getCode());
                        Log.e("직접추가-읽은 책 통신성공", "onResponse: " + result.getMessage());

                        if (result.getCode() == 200) {
                            // 내 서재 "읽은 책" 저장 성공
                            Toast.makeText(MyLibraryCreateActivity.this, "내 서재에 읽은 책 저장이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MyLibraryCreateActivity.this, MainActivity.class);
                            intent.putExtra("fragment", "myLibrary");
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MyLibraryCreateActivity.this, "내 서재에 읽은 책 저장을 실패하였습니다", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MylibraryResponse> call, Throwable throwable) {
                        Log.e("직접추가-읽은 책 통신 실패", "onFailure:실패한이유: " + throwable.getMessage());

                    }
                });



    }


    // 2) 읽고 있는 책
    public void book_reading_insert(String read_page_cnt, String started_date) {
        Log.i("읽고 있는 책 저장메소드실행 ", "읽은 페이지: " + read_page_cnt + ",시작날짜: " + started_date);
        Log.i("book_reading_insert 저장하려는 책내용 ", "제목: " + saving_title + ", 작가: " + saving_author);
        // Log.i("읽고 있는 책 저장메소드실행 ", "현재 유저 넘버: " + current_userNumber + ", saving_description: " + saving_description+ ",saving_publisher: " + saving_publisher + ",saving_pubDate: "+ saving_pubDate + "cover 길이: "+ saving_cover.length() + ",saving_cover: " +saving_cover +",saving_isbn: " + saving_isbn+", read_page_cnt: " + read_page_cnt+ ", started_date: " + started_date);


        service.mylibrary_reading(current_userNumber, saving_title, saving_author, saving_description, saving_publisher, saving_pubDate, saving_cover, saving_isbn,
                read_page_cnt, started_date).enqueue(new Callback<MylibraryResponse>() {
            @Override
            public void onResponse(Call<MylibraryResponse> call, Response<MylibraryResponse> response) {
                Log.e("통신성공!! 서버가 보내는 코드", "code: " + response.code()); //서버가 보내는 http 통신 응답코드
                MylibraryResponse result = response.body();
                Log.e("읽고 있는 책 통신성공- 코드", "onResponse: " + result.getCode()); // echo로 보내주는 메시지.
                Log.e("읽고 있는 책 통신성공", "onResponse: " + result.getMessage()); // echo로 보내주는 메시지.

                if (result.getCode() == 200) {
                    // 내 서재 "읽은 책" 저장 성공
//                    finish(); // 저장하는 액티비티 끝냄 == > 목록이 아니라, 책 상세보기로 감 ㅜㅜ
                    // 여기서 목록으로 이동한다면, 상세보기까지 스택 삭제할 수 있나 ??
                    // 안드로이드 스택에 대한 공부가 필요함 ㅜ

                    Toast.makeText(MyLibraryCreateActivity.this, "내 서재에 읽고 있는 책 저장이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    // code: 200 일때 내 서재 프래그먼트로 이동 !! 뒤에 있는 스택 지우고가기
                    Intent intent = new Intent(MyLibraryCreateActivity.this, MainActivity.class);
                    intent.putExtra("fragment", "myLibrary");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 위 플래그를 설정하면 스택에 있는 기존 액티비티가 전부 제거됨.
                    startActivity(intent);
                    finish();

                    // 서재 직접 보내야 하는가 ?
                } else {  // 코드 경우의 수는 200,400 두개임. 현재 400일 경우 = 실패일경우
                    Toast.makeText(MyLibraryCreateActivity.this, "내 서재에 읽고 있는 책 저장을 실패하였습니다", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<MylibraryResponse> call, Throwable throwable) {
                // 통신 실패했을 때 원인을 출력하기 위한 메소드.
                throwable.getMessage();
                Log.e("읽고 있는 책 통신 실패", "onFailure:실패한이유: " + throwable.getMessage());

            }
        });
    }   // 2) 읽고 있는 책 끝


    // 2-2) 읽고 있는 책 직접 추가한 경우


    // 3) 읽고 싶은 책
    public void book_want_insert(String want_rating, String want_preview) {
        Log.i("읽고 싶은 책 저장메소드실행 ", "기대지수: " + want_rating + ",기대평: " + want_preview);
        Log.i("book_want_insert 저장하려는 책내용 ", "제목: " + saving_title + ", 작가: " + saving_author);

        Log.i("읽고 싶은 책 저장메소드실행 ", "현재 유저 넘버: " + current_userNumber);

        service.mylibrary_want(current_userNumber, saving_title, saving_author, saving_description, saving_publisher, saving_pubDate, saving_cover, saving_isbn,
                want_rating, want_preview).enqueue(new Callback<MylibraryResponse>() {
            @Override
            public void onResponse(Call<MylibraryResponse> call, Response<MylibraryResponse> response) {
                Log.e("통신성공!! 서버가 보내는 코드", "code: " + response.code()); //서버가 보내는 http 통신 응답코드
                MylibraryResponse result = response.body();
                Log.e("읽고 싶은 책 통신성공- 코드", "onResponse: " + result.getCode()); // echo로 보내주는 메시지.
                Log.e("읽고 싶은 책 통신성공", "onResponse: " + result.getMessage()); // echo로 보내주는 메시지.

                if (result.getCode() == 200) {

                    Toast.makeText(MyLibraryCreateActivity.this, "게시글 작성이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                    // code: 200 일때 내 서재 프래그먼트로 이동 !! 뒤에 있는 스택 지우고가기
                    Intent intent = new Intent(MyLibraryCreateActivity.this, MainActivity.class);
                    intent.putExtra("fragment", "myLibrary");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 위 플래그를 설정하면 스택에 있는 기존 액티비티가 전부 제거됨.
                    startActivity(intent);
                    finish();

                } else {  // 코드 경우의 수는 200,400 두개임. 현재 400일 경우 = 실패일경우
                    Toast.makeText(MyLibraryCreateActivity.this, "내 서재에 읽고 싶은 책 저장을 실패하였습니다", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<MylibraryResponse> call, Throwable throwable) {

            }
        });


    } // 3) 읽고 싶은 책 끝


    // 3) 읽고 싶은 책 직접 추가한 경우


    //  My library Method 6. 유저의 넘버 가져오는 메소드
    public void get_userNumber(String user_emailId) {
        Log.i("유저 email,id로 넘버가져오는 레트로핏", "유저의 emailId: " + user_emailId);
        service.user_emailId(user_emailId).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("유저 number 체크 response 진입", "onResponse: 응답메시지: " + result.getMessage()); // echo로 보내주는 메시지.
                Log.e("유저 number 체크 response 진입", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.

                if (result.getCode() == 200) {
                    Log.e("유저 number 체크성공 / code==200!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                    current_userNumber = result.getUser_number();

                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {

            }
        });
    }


    // My library Method 7. 이미지 생성하기. base 64 를 가지고, file 생성하기.
    public File createFileFromBase64(String base64Stirng) {
        File file = null;
        try {
            // base64 문자열을 디코딩하여 바이트 배열로 변환
            byte[] decodeBytes = Base64.decode(base64Stirng, Base64.DEFAULT);

            // UUID 로 고유한 파일명 생성
            String filename = "image" + UUID.randomUUID().toString() + ".jpg";

            // 파일 생성
            file = new File(getCacheDir(), filename);
            try (OutputStream outputStream = new FileOutputStream(file)) {
                // 바이트 배열을 파일로 쓰기
                outputStream.write(decodeBytes);
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }


    // My library Method 8. 이미지 확장자 반환하는 메소드
    private String getMimeType(File file) {

        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getPath());
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        Log.e("현재 프사이미지의 타입은? ", "type: " + type);
        return type;
    }


}
