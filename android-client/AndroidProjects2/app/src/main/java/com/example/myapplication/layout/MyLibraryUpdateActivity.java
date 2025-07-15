package com.example.myapplication.layout;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.MyLibraryListResponse;
import com.example.myapplication.data.retrofit.responsemodel.MylibraryResponse;
import com.example.myapplication.layout.bottomnavi.FragmentMyLibrary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyLibraryUpdateActivity extends AppCompatActivity {


    // import
    RetrofitService service; // 레트로핏 서비스
    // 툴바
    Toolbar toolbar;



    // 읽은책인지, 읽고 있는 책인지, 읽고 싶은 책인지 boolean
    Boolean alreay_read = false;
    Boolean is_reading = false;
    Boolean want_read = false;



    // View
    Button mylibrary_modify_btn; // 수정 버튼
    Button book_alreadyread_btn, book_reading_btn, book_wantread_btn; // 읽은책, 읽고 있는 책, 읽고 싶은 책. button
    RatingBar ar_ratingstar, want_ratingstar; // 읽은책 평점, 읽고 싶은 책 기대평점 Rating bar

    // visibility 조절하는데도 뷰 초기화 해야하나? 네.
    LinearLayout book_alreay_lay, book_reading_lay, book_want_lay; // Linear bar

    // String


    // 읽은 책
    TextView ar_date_started, ar_date_finished, ratingValue; // 읽은 책의 시작일, 종료일, 평점

    // 수정된 내용 담기!!  - 시작, 종료, 평점
    String m_already_started_date, m_already_finished_date, m_already_rating;


    // 읽고 있는 책
    EditText reading_page; // 읽고 있는 책 - 읽은 페이지
    TextView reading_started, reading_been; // 읽고 있는 책 - 시작일, 며칠됐는지

    // 수정된 내용 담기 !! - 읽은 페이지, 시작일
    String m_reading_started_date, m_reading_readPage;


    // 읽고 싶은 책
    EditText want_preview; // 기대지수rating bar-want_ratingstar, 기대평
    TextView ratingValue_want; // 기대지수 text view

    // 수정된 내용 담기 !! - 기대 평점, 기대평
    String m_want_rating, m_want_preview;


    // 수정된 타입
    String m_modified_type;





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mylibrary_update);

        // 툴바
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }


        // 레트로핏 초기화
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의


        // c0. 툴바 뒤로 가기 클릭시, 이전 화면으로 (home fragment)
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // 뒤로 가기 동작 수행
            }
        }); // bs1 끝

        // ======================= 초기화 zone 시작 =================================


        // c1. view 공통버튼 초기화하기
        mylibrary_modify_btn = findViewById(R.id.mylibrary_save_btn); // 수정 !!  버튼 !!

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


        // 2.읽고 있는 책 =============================================

        // 쪽수 edit text, 읽기시작한 날짜 text view, 읽은지 며칠 됐는지 text view
        reading_page = findViewById(R.id.reading_page);
        reading_started = findViewById(R.id.reading_started);
        reading_been = findViewById(R.id.reading_been);

        // 2. 읽기 시작한 날짜에 오늘 날짜 setting
        reading_started.setText(get_today());

        // 2. 읽은지 며칠 됐는지.
        // 선택날짜 바뀔 때 자동으로 계산되도록 해놓음 !!


        // 3.읽고 싶은 책 =============================================

        want_preview = findViewById(R.id.want_preview); // 기대평
        ratingValue_want = findViewById(R.id.ratingValue_want);


        // 가지고 넘어온 값 = 유저 넘버, 게시글번호, 책번호, 타입을 가지고
        // 타입에 따라 읽은책, 읽고 있는책, 읽고 싶은 책에 따라서 다르게 뷰 보여주기.


        // c2. intent 로 넘어온 값 초기화. / before 수정 전 기존 내용임을 알려줌.
        String current_user_number = getIntent().getStringExtra("user_number");
        String intent_mylibrary_number = getIntent().getStringExtra("mylibrary_number");
        String intent_book_number = getIntent().getStringExtra("book_number");
        String before_type = getIntent().getStringExtra("type");
        Log.d("내 서재 수정액티비티 진입! 인텐트값은?", "유저넘버string:  " + current_user_number + ", 게시글번호string: "+ intent_mylibrary_number + ", 책번호string: " + intent_book_number + ", 타입string: " + before_type);

        int before_mylibrary_number = Integer.parseInt(intent_mylibrary_number);
        int before_book_number = Integer.parseInt(intent_book_number);
        Log.d("내 서재 수정액티비티 진입", "게시글번호int: "+ before_mylibrary_number + ", 책번호int: " + before_book_number);

        // 기존 내용가져와서 꽂아주기.
        mylibrary_before_modi(current_user_number, before_book_number, before_mylibrary_number, before_type);

        // ======================= 초기화 zone 끝 =================================




        // 각 타입별 input 기능
        //1. 읽은책 ===========================================
        book_alreadyread_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 색상 변경
                setting_already();
            }
        });
        // 1. 읽은책 - 평점
        ar_ratingstar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                // 사용자가 등급을 변경할 때마다 호출됩니다.
                float ratingValues = ar_ratingstar.getRating();
                Log.i("(수정) 읽은책평점!!!", "평점몇점인가: " + rating);
                ratingValue.setText("(총 "+ ratingValues +"점 / 5점)");
                Log.i("(수정) 읽은책평점 setting!!", "읽은책평점 text view: " + ratingValue.getText().toString());


            }
        });

        // 1. 읽은책 - 시작일 텍뷰 누르면 날짜 선택할 수 있도록.
        ar_date_started.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("(수정) 시작일클릭함!! ", "onClick진입");
                showDatePickerDialog(ar_date_started);
                Log.i("(수정) 시작일 세팅완료 ", "시작일: " + ar_date_started.getText().toString());
            }
        });

        //  1. 읽은책 - 종료일 텍뷰
        ar_date_finished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("(수정) 종료일 클릭함!!!! ", "onClick진입");
                showDatePickerDialog(ar_date_finished);
                Log.i("(수정) 종료일 세팅완료 ", "종료일: " + ar_date_finished.getText().toString());
            }
        });





        // 2. 읽고 있는 책  ===========================================
        book_reading_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting_reading();
            }
        });



        // 2. 읽고 있는 책 - 읽기 시작한 날짜.
        reading_started.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("(수정) 읽고있는책-시작한날짜!! ", "onClick진입");
                showDatePickerDialog(reading_started);
            }
        });

        //  2. 읽고 있는 책 -  시작 날짜 text 변경을 감지해서, 변경되면, 시작한지 며칠됐는지 알려주기.
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
                Log.i("수정 - 읽고있는책- ", "며칠됐나요: " + beendays);
            }
        });




        // 3. 읽고 싶은책 ============================================
        book_wantread_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting_want();
            }
        });
        //  3. 읽고 싶은책 -  읽고 싶은 책 평점
        want_ratingstar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                // 사용자가 등급을 변경할 때마다 호출됩니다.
                float ratingValues = want_ratingstar.getRating();
                Log.i("수정 - 읽고 싶은책 기대평점!!!", "기대평점: " + rating);
                ratingValue_want.setText("(총 "+ ratingValues +"점 / 5점)");
                Log.i("수정 - 읽고 싶은책기대평점 setting!!", "읽고싶은책기대평점 text view: " + ratingValue_want.getText().toString());
            }
        });

        // 3. 읽고 싶은 책 기대평점 저장.
        // 저장될때 바로 get text 해가도록 함


        // ============================================================
        // ==================== 수정하기 버튼 !!! =====================
        // ============================================================
        mylibrary_modify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. 세개 중 어떤게, visible인지 확인

                Log.i("수정버튼 클릭", "수정되었나요?");


                if(book_alreay_lay.getVisibility()==View.VISIBLE) {
                    alreay_read = true;
                    is_reading = false;
                    want_read = false;
                } else if (book_reading_lay.getVisibility()==View.VISIBLE){
                    is_reading = true;
                    alreay_read = false;
                    want_read = false;
                } else if (book_want_lay.getVisibility() == View.VISIBLE) {
                    want_read = true;
                    alreay_read = false;
                    is_reading = false;
                }

                // 1) 읽은 책인 경우.
                if(alreay_read && !is_reading && !want_read) {
                    // 기존 type, book number, my library number
                    Log.i("읽은책으로 수정버튼!!", "기존 타입: " + before_type + ", 기존책번호: "+ before_book_number + ", 기존서재번호: " + before_mylibrary_number);
                    m_modified_type = "1";
                    Log.i("읽은책으로 수정버튼!!", "수정한 타입: " + m_modified_type + ", 책번호는 그대로: "+ before_book_number );
                    m_already_started_date = ar_date_started.getText().toString();
                    Log.i("읽은책으로 수정버튼!!", "1.읽기 시작한날짜: " + ar_date_started.getText().toString());
                    m_already_finished_date = ar_date_finished.getText().toString();
                    Log.i("읽은책으로 수정버튼!!", "2. 종료날짜: " + ar_date_finished.getText().toString());
                    m_already_rating = String.valueOf(ar_ratingstar.getRating());
                    Log.i("읽은책으로 수정버튼!!", "3. 평점rating bar의 getRating(): " + m_already_rating); // string으로 바꾸기
                    // 메소드 호출, 기존 타입, 기존책번호, 기존 서재번호 + 원래 insert 내용. !!

                    already_modify(current_user_number,before_book_number, before_mylibrary_number, before_type, m_modified_type);

                } // 2) 읽고 있는 책인 경우
                else if (is_reading && !alreay_read && !want_read) {
                    Log.i("읽고있는책으로 수정버튼!!", "기존 타입: " + before_type + ", 기존책번호: "+ before_book_number + ", 기존서재번호: " + before_mylibrary_number);
                    m_modified_type = "2";
                    Log.i("읽고있는책으로 수정버튼!!", "수정한 타입: " + m_modified_type + ", 책번호는 그대로: "+ before_book_number );

                    m_reading_readPage = reading_page.getText().toString();
                    Log.i("읽고있는책으로 수정버튼 !!", "1.읽은 쪽수: " + m_reading_readPage + "쪽"); // string 변수에 담아뒀던 것.은 안되지. on create 때 담아둔건데 ?? null 이죠?
                    m_reading_started_date = reading_started.getText().toString();
                    Log.i("읽고있는책으로 수정버튼 !!", "2.시작일: " + reading_started.getText().toString()); // 시작 날짜 보여지는 text view
                    Log.i("읽고있는책으로 수정버튼 !!", "3. 읽은지며칠? : " + reading_been.getText().toString()); // 시작 날짜 보여지는 text view //

                    // 읽고 있는 책, update 를 위한 http 통신 요청 메소드 호출
                    reading_modify(current_user_number,before_book_number, before_mylibrary_number, before_type, m_modified_type);


                } else if (want_read && !alreay_read && !is_reading) {
                    Log.i("읽고 싶은책으로 수정버튼!!", "기존 타입: " + before_type + ", 기존책번호: "+ before_book_number + ", 기존서재번호: " + before_mylibrary_number);
                    m_modified_type= "3";
                    Log.i("읽고 싶은책으로 수정버튼!!", "수정한 타입: " + m_modified_type + ", 책번호는 그대로: "+ before_book_number );

                    Log.i("읽고 싶은책으로 수정버튼!!", "1. 기대평점rating bar의 getRating(): " + want_ratingstar.getRating()); // string으로 바꾸기
                    m_want_rating = String.valueOf(want_ratingstar.getRating());
                    Log.i("읽고 싶은책으로 수정버튼!!", "2. 기대평평점rating bar의 getRating(): " + ratingValue_want.getText().toString());
                    m_want_preview = want_preview.getText().toString();
                    Log.i("읽고 싶은책으로 수정버튼!!", "3. 기대평 : " + want_preview.getText().toString());

                    // 읽고 싶은 책, insert를 위한 http 통신 요청 메소드 호출
                    want_modify(current_user_number,before_book_number, before_mylibrary_number, before_type, m_modified_type);

                } // else if 끝

            } // on click 끝
        });




    } // on create




    // 0-1) 오늘 날짜 반환하는 메소드
    private String get_today () {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        String realMonth;
        if(month+1<10) {
            realMonth = "0"+(month+1);
        } else {
            realMonth = String.valueOf(month+1);
        }

        String realDate;
        if(dayOfMonth+1<10) {
            realDate = "0"+(dayOfMonth);
        } else {
            realDate = String.valueOf(dayOfMonth);
        }

        String todayDate = year + "." + realMonth + "." + realDate;
        return todayDate;
    } // My library Method 2. 끝

    // 0-2) 달력 !!
    private void showDatePickerDialog(final TextView textView){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);


        // 기존 텍스트뷰에 작성된 날짜 가져오기.
        String[] dateParts = textView.getText().toString().split("\\.");
        // 백슬레시 두개 "\\"는 이스케이프 문자이다. 뒤에 오는 문자를 그대로 표현, 인식하게 하기 위해서 사용
        // 즉, . 온점 자체를 문자 그대로 나타내기 위한 장치이다.
        if (dateParts.length ==3) {
            year = Integer.parseInt(dateParts[0]);
            month = Integer.parseInt(dateParts[1]) -1; // date picker를 세팅하기 위해 1을 빼줌. 그러나, 사용자가 보는 값은 +1되어있음.
            dayOfMonth = Integer.parseInt(dateParts[2]);

        }

        // Date pick dialog 생성 및 설정
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) { // month 가 0부터 시작함. 항상 1 더해주기.

                        String realMonth;
                        if(month+1<10) {
                            realMonth = "0"+(month+1);
                        } else {
                            realMonth = String.valueOf(month+1);
                        }

                        String realDate;
                        if(dayOfMonth+1<10) {
                            realDate = "0"+(dayOfMonth);
                        } else {
                            realDate = String.valueOf(dayOfMonth);
                        }

                        Log.i("date picker날짜선택", "선택한날짜 년: "+ year + ", 월: " + realMonth + ", 일: " + realDate);
                        String selected_date = year+"."+realMonth+"."+realDate;
                        textView.setText(selected_date); // 시작일, 혹은 종료일 세팅해주기.
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show(); // Q.이거 스피너야 ?? A. 아니야. 걍 캘린더형임. 나쁘지 않은 것 같음. 그대로하기.

    } // 날짜 선택 !!


    // 0-3) 읽은 책 - 날짜 계산 메소드 !!
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
            if(0>diffInDays) { // 오늘 보다 이후 날짜를 선택했을때. 마이너스됨.
                Log.i("날짜 계산결과", "0>diffInDays일때 diffInDays: " + diffInDays);
                reading_been.setText("날짜를 다시 선택해 주세요.");
                return null;
            } else {
                Log.i("날짜 계산결과", "else 일때 diffInDays: " + diffInDays);
                reading_been.setText("읽기 시작한 지" + String.valueOf(diffInDays+1) + "일 됐어요");
                return String.valueOf(diffInDays);
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    } //  0-3) been days 끝




    // 1) setting 해주기.
    private void setting_already(){
        book_alreadyread_btn.setBackground(getDrawable(R.drawable.shape_rectangle_gray_round)); // 읽은책 연회
        book_reading_btn.setBackground(getDrawable(R.drawable.shape_rectangle_moregray_round)); // 읽고 있는 책 진회
        book_wantread_btn.setBackground(getDrawable(R.drawable.shape_rectangle_moregray_round)); // 읽고 싶은 책 진회

        // 해당 내용 변경. visible.
        book_alreay_lay.setVisibility(View.VISIBLE); // 읽은책 visible
        book_reading_lay.setVisibility(View.GONE); // 읽고 있는 책 gone
        book_want_lay.setVisibility(View.GONE); // 읽고 싶은 책 gone
    }

    private void setting_reading () {
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


    // 2) 기존 내용 가져오기!!
    public void mylibrary_before_modi (String user_number, int book_number, int mylibrary_number, String before_type) {
        Log.e("내서재 게시글 수정을 위한 기존 내용가져오기!!!  http통신 메소드진입", " 유저번호: " + user_number + ", 책번호: " + book_number + ", 내서재 번호: " + mylibrary_number + ", 타입: " + before_type);

        service.mylibrary_before_modi(user_number, book_number, mylibrary_number, before_type).enqueue(new Callback<MyLibraryListResponse>() {
            @Override
            public void onResponse(Call<MyLibraryListResponse> call, Response<MyLibraryListResponse> response) {
                // 가져온 내용 setting 하기.
                MyLibraryListResponse result = response.body();
                Log.e("내서재 before modi http 성공!!", " reponse 진입함" );
                Log.e("내서재 before modi http 성공!!", " php 응답코드: " + result.getCode() + ", 메시지: " + result.getMessage() + ", 서재 type: " + result.getType());

                // 책 정보 세팅하기.
                // type별로 먼저 setting 메소드 불러줌
                if(result.getType().equals("1")) { // 읽은 책
                    setting_already();
                    ar_date_started.setText(result.getStarted());
                    ar_date_finished.setText(result.getFinished());
                    // 읽은 책 평점 setting
                    float alreadyRating = 0.0f;
                    try {
                        alreadyRating = Float.parseFloat(result.getRating());
                    } catch (NumberFormatException e) {
                        Log.e("(수정 진입) 읽은책 기존 평점!! 오류!", "읽고 싶은 책 평점 float 오류남 이유: " + e.getMessage());
                    }
                    ar_ratingstar.setRating(alreadyRating);

                } else if (result.getType().equals("2")) { // 읽고 있는 책
                    setting_reading();
                    reading_started.setText(result.getStarted());
                    reading_page.setText(result.getReadPage());
                    been_days(result.getStarted()); // 읽은지 며칠 됐는지 setting

                } else if (result.getType().equals("3")) { // 읽고 싶은 책
                    setting_want();

                    // 읽고 싶은 책 기대 평점 setting
                    float wantRating = 0.0f;
                    try {
                        wantRating = Float.parseFloat(result.getRating());
                    } catch (NumberFormatException e) {
                        Log.e("내서재 어댑터!! 평점 float 변환", "읽고 싶은 책 평점 float 오류남 이유: " + e.getMessage());
                    }
                    want_ratingstar.setRating(wantRating);
                    want_preview.setText(result.getPreview());
                } // else if 끝
            } // on response 끝

            @Override
            public void onFailure(Call<MyLibraryListResponse> call, Throwable throwable) {
                // 통신 실패했을 때 원인을 출력하기 위한 메소드.
                throwable.getMessage();
                Log.e("내서재 before modi http 실패!", "onFailure:실패한이유: " + throwable.getMessage());

            }
        });
    } // 기존 내용 가져오기 끝



    // 3-1) <읽은 책> 으로 수정 !!! - 수정한 내용이 읽은 책인 경우.
    //  기존 책 넘버, 기존 서재 넘버, 기존 타입
    public void already_modify(String user_number, int book_number, int before_library_number, String before_type, String modi_type ){
        Log.e("내서재 수정", "호출된 메서드: already_modify");
        Log.e("내서재 <읽은 책>으로 게시글 수정!!  http통신 메소드진입", " 유저번호: " + user_number + ", 책번호: " + book_number + ", 기존 내서재 번호: " + before_library_number + ", 기존 내서재 타입: " + before_type + ", modi_type : " + modi_type + ", m_already_started_date: " + m_already_started_date + ", m_already_finished_date: " + m_already_finished_date + ",m_already_rating:  " + m_already_rating );
        service.already_modi(user_number, book_number, before_library_number, before_type, modi_type, m_already_started_date, m_already_finished_date, m_already_rating).enqueue(new Callback<MylibraryResponse>() {
            @Override
            public void onResponse(Call<MylibraryResponse> call, Response<MylibraryResponse> response) {

                MylibraryResponse result = response.body();
                Log.e("내서재 <읽은 책>으로 수정 성공 !!", " reponse 진입함" );
                Log.e("내서재 <읽은 책>으로 수정 성공 !!", " php 응답코드: " + result.getCode() + ", 메시지: " + result.getMessage() );

                // code: 200 일때 내 서재 프래그먼트로 이동 !! 뒤에 있는 스택 지우고가기
//                if(result.getCode() == 200) {
                    Intent intent = new Intent(MyLibraryUpdateActivity.this, MainActivity.class);
                    intent.putExtra("fragment", "myLibrary");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 위 플래그를 설정하면 스택에 있는 기존 액티비티가 전부 제거됨.
                    startActivity(intent);
                    finish();
//                } else {
//                    Log.e("코드 400 !! 무언가 실패함", " 메시지: " + result.getMessage() );
//
//                }
            }

            @Override
            public void onFailure(Call<MylibraryResponse> call, Throwable throwable) {
                Log.e("내서재 <읽은 책>으로 수정 실패", throwable.getMessage());
            }
        });


    } // 읽은 책 수정 http 메소드 완료



    // 3-2) <읽고 있는 책> 으로 수정 !!!
    public void reading_modify(String user_number, int book_number, int before_library_number, String before_type, String modi_type){
        Log.e("내서재 <읽고 있는 책>으로 게시글 수정!!  http통신 메소드진입", " 유저번호: " + user_number + ", 책번호: " + book_number + ", 기존 내서재 번호: " + before_library_number + ", 기존 내서재 타입: " + before_type);

        service.mylibrary_reading_modi(user_number, book_number, before_library_number, before_type, modi_type, m_reading_started_date, m_reading_readPage).enqueue(new Callback<MylibraryResponse>() {
            @Override
            public void onResponse(Call<MylibraryResponse> call, Response<MylibraryResponse> response) {
                MylibraryResponse result = response.body();
                Log.e("내서재 <읽고있는책>으로 수정 성공 !!", " reponse 진입함" );
                Log.e("내서재 <읽고있는책>으로 수정 성공 !!", " php 응답코드: " + result.getCode() + ", 메시지: " + result.getMessage() );

                // code: 200 일때 내 서재 프래그먼트로 이동 !! 뒤에 있는 스택 지우고가기

                // code: 200 일때 내 서재 프래그먼트로 이동 !! 뒤에 있는 스택 지우고가기
//                if(result.getCode() == 200) {
                Intent intent = new Intent(MyLibraryUpdateActivity.this, MainActivity.class);
                intent.putExtra("fragment", "myLibrary");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                // 위 플래그를 설정하면 스택에 있는 기존 액티비티가 전부 제거됨.
                startActivity(intent);
                finish();
//                } else {
//                    Log.e("코드 400 !! 무언가 실패함", " 메시지: " + result.getMessage() );
//
//                }
            }
            @Override
            public void onFailure(Call<MylibraryResponse> call, Throwable throwable) {
                Log.e("내서재 <읽고 있는 책>으로 수정 실패", throwable.getMessage());
            }
        });
    } // 읽고 있는 책 수정 http 메소드 완료



    // 3-3) <읽고 싶은 책> 으로 수정 !!!
    public void want_modify(String user_number, int book_number, int before_library_number, String before_type, String modi_type){
        Log.e("내서재 <읽고 싶은 책>으로 게시글 수정!!  http통신 메소드진입", " 유저번호: " + user_number + ", 책번호: " + book_number + ", 기존 내서재 번호: " + before_library_number + ", 기존 내서재 타입: " + before_type);
        service.mylibrary_want_modi(user_number, book_number, before_library_number, before_type, modi_type, m_want_rating, m_want_preview).enqueue(new Callback<MylibraryResponse>() {
            @Override
            public void onResponse(Call<MylibraryResponse> call, Response<MylibraryResponse> response) {
                MylibraryResponse result = response.body();
                Log.e("내서재 <읽고싶은책>으로 수정 성공 !!", " reponse 진입함" );
                Log.e("내서재 <읽고싶은책>으로 수정 성공 !!", " php 응답코드: " + result.getCode() + ", 메시지: " + result.getMessage() );

                // code: 200 일때 내 서재 프래그먼트로 이동 !! 뒤에 있는 스택 지우고가기
                Intent intent = new Intent(MyLibraryUpdateActivity.this, MainActivity.class);
                intent.putExtra("fragment", "myLibrary");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                // 위 플래그를 설정하면 스택에 있는 기존 액티비티가 전부 제거됨.
                startActivity(intent);
                finish();

            }

            @Override
            public void onFailure(Call<MylibraryResponse> call, Throwable throwable) {
                Log.e("내서재 <읽고 싶은 책>으로 수정 실패", throwable.getMessage());
            }
        });
    } // 읽고 있는 책 수정 http 메소드 완료




}
