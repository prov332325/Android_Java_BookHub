package com.example.myapplication.layout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.UserNumberCallback;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginPasswordForgotActivity extends AppCompatActivity {

    // import
    RetrofitService service; // 레트로핏 서비스


    // view
    Toolbar toolbar; // 툴바
    EditText using_email, verify_number; // 비번 찾고자 하는 이메일 주소 입력칸, 인증번호 입력칸
    Button send_btn, verify_btn; // 인증번호 전송버튼, 인증하기 버튼.

    // 인증번호 발송 절차 넘어갔을때 리니어 레이아웃 보이게끔하기 위해..
    LinearLayout numberVerifyContainer;


    // string
    String user_email_now ; // 현재 로그인 중인 유저 넘버.




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password1); // 비밀번호 찾고자 하는 이메일 입력하는 곳.

        // ======================= 초기화 zone 시작 =================================

        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의


        // 툴바
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }


        // 모든 뷰에 대한 초기화 find
        using_email = findViewById(R.id.using_email);
        verify_number = findViewById(R.id.verify_number);
        send_btn = findViewById(R.id.send_btn);
        verify_btn = findViewById(R.id.verify_btn);

        numberVerifyContainer = findViewById(R.id.number_verify_container);



        // ======================= 초기화 zone 끝 =================================

        // on create 1. 툴바 뒤로 가기 클릭시, 이전 화면으로 (library fragment)
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // 뒤로 가기 동작 수행
            }
        }); // bs1 끝


        // 1. 이메일 입력 받는다
        // 2. 서버에서 이메일 사용 중인지 아닌지 확인한다
        // 3. 인증번호 전송하는 로직 사용하여 인증번호 보내기. (3분 뒤에 만료되는 시스템?? 나중에 넣기)
        // 3. 그러나 무조건 사용자에게는 이메일 보냈으니 확인하라고 해줌.


        // on create 2. 이메일 입력받아서 중복 검사 후에, 인증 번호 전송하기
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String written_email = using_email.getText().toString();
                emailDupleCheckAndVerify(written_email);

                // 여기서 이런식으로 !! 코드를 짜도 되는 것인지.
                // 이렇게 스트링을 굳이 여기서 변수로 만드는게 나은지에 대한... 궁금증

            }
        });


        // 인증번호 입력 후, 인증하기 버튼 누르기. (무조건 인증번호를 전송했습니다. 확인해 주세요 출력하기.)

        // on create 3.
        verify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String written_verify_number = verify_number.getText().toString();
                if(!user_email_now.isEmpty()) {
                    verify_check(user_email_now, written_verify_number);
                }

            }
        });


        // 뒤로 가기 활성화 시키기
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("비밀번호 찾기");


    } // on create

    // 뒤로가기 함수
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }



    // 서버에서 이메일 유효한지, 사용 중인지 확인 하고, 인증번호 보내기 (php emailer)
    // 메소드 2.
    public void emailDupleCheckAndVerify (String user_email) {
        Log.e("이메일중복,인증번호발송 메소드진입",   "비번찾을이메일: "+ user_email);
        service.emailDupleCheckAndVerify(user_email).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("이메일중복,인증번호발송 통신 성공 !!! ", " code: " + result.getCode() + ", 메시지: " + result.getMessage());

                // 여기서 다음 화면으로 넘어가는 인텐트 작성하기.

                // 일단 응답이 오면, code 가 200 일때에는 무조건, 즉 이메일이 없어도 일단 아래 메시지 띄우기.
                // 인증 번호 발송했습니다. 확인해주세요.

                if(result.getCode()==200) {
                    if(result.getMessage().equals("이메일X")){
                        Toast.makeText(getApplicationContext(), "해당 이메일이 존재하지 않습니다", Toast.LENGTH_LONG).show();
                        using_email.requestFocus(); // 다시 이메일 주소를 입력할 수 있도록.
                        //텍스트를 비우지는 말자. 오타일 수도 있잖아..

                        // 소프트 키보드 활성화.
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(using_email, InputMethodManager.SHOW_IMPLICIT);

                    } else {
                        user_email_now = user_email;
                        Toast.makeText(getApplicationContext(), "인증번호를 발송했습니다. 메일함을 확인해주세요", Toast.LENGTH_LONG).show();
//                    Retrofit의 onResponse가 실행되는 스레드는 기본적으로 **메인 스레드에서 동작하므로 여기서 number_verify_container의 비저빌리티를 비저블로 바꿔준다 !! 오예
                        numberVerifyContainer.setVisibility(View.VISIBLE); // visibility 변경
                    }


                } else if (result.getCode() == 41) {
                    Toast.makeText(getApplicationContext(), "올바른 이메일 형식이 아닙니다", Toast.LENGTH_LONG).show();
                    using_email.requestFocus(); // 다시 이메일 주소를 입력할 수 있도록.


                    // 소프트 키보드 활성화.
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(using_email, InputMethodManager.SHOW_IMPLICIT);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("이메일중복,인증번호발송 실패: ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();

                // 다시 시도해주세요. 안내.
                Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 다시 시도해주세요", Toast.LENGTH_SHORT).show();

            }
        });
    } // 메소드2끝



    // 이메일로 받은 인증번호 확인하기
    // 메소드 3.
    public void verify_check (String user_email, String verify_code) {
        Log.e("인증번호확인!! 메소드진입",   "인증중인 이메일: "+ user_email + ", 입력한 인증번호: " + verify_code);
        service.emailVerifyCheck(user_email, verify_code).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("인증번호확인 통신 성공 !!! ", " code: " + result.getCode() + ", 메시지: " + result.getMessage());

                if(result.getCode()==200) {
                    Toast.makeText(getApplicationContext(), "이메일 인증이 완료되었습니다", Toast.LENGTH_LONG).show();
                    // 비밀번호 재설정 화면으로 넘어갑니다. !!
                    Intent intent = new Intent(getApplicationContext(), LoginNewPasswordActivity.class);
                    intent.putExtra("user_email", user_email);
                    startActivity(intent);
                }





            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("인증번호확인 실패: ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();

            }
        });
    }




} // 액티비티 끝
