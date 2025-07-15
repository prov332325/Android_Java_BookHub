package com.example.myapplication.layout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginNewPasswordActivity extends AppCompatActivity {


    // import
    RetrofitService service; // 레트로핏 서비스


    // view
    Toolbar toolbar; // 툴바
    EditText NewPassword1, NewPassword2; // 새로운 비번 입력칸.
    Button password_change_btn;


    // 비번 보이게 하기
    boolean isPasswordVisible1 = false;
    boolean isPasswordVisible2 = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password2);

        // 1. 비번들을 먼저 입력을 받는다.
        // 2. 그리고 데이터 베이스에서 고쳐주면 됨... ㅎㅎ update !
        // 3. 오전 중에이거랑, 검색어 안나오는거까지 끝내기.
        // 4. 사진 첨부시, 사진 촬영도 가능하게 하는 것도 빨리 쳐내고..

        // 5. 최근 검색 기록 쉐어드에 하는 방법으로 빨리 해결하고.
        // 6. 문제의 정의에 대해서 다시 고민하는 시간갖고,, 일본어 공부하기.

        // ======================= 초기화 zone 시작 =================================

        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의

        // 넘어온 인텐트 값 가져오기
        Intent intent = getIntent();
        String user_email = intent.getStringExtra("user_email");

        // 툴바
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }


        // 모든 뷰에 대한 초기화 find
        NewPassword1 = findViewById(R.id.NewPassword1);
        NewPassword2 = findViewById(R.id.NewPassword2);

        password_change_btn = findViewById(R.id.password_change_btn);

        // ======================= 초기화 zone 끝 =================================


        // on create 1. 툴바 뒤로 가기 클릭시, 이전 화면으로 (library fragment)
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // 뒤로 가기 동작 수행
            }
        }); // oc1 끝


        // on create 2. 비밀번호 입력칸 비번 보이게 하기 - NewPassword1
        NewPassword1.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    // 터치한 위치가 drawable end 인지 ??
                    if (event.getRawX() >= (NewPassword1.getRight() - NewPassword1.getCompoundDrawables()[2].getBounds().width())) {
                        // boolean 값 바꿔보기
                        isPasswordVisible1 = !isPasswordVisible1;
                        // 이미지 바꿔주기
                        if (isPasswordVisible1) {
                            // 비밀번호를 보이게 설정하고 이미지도 변경해줌
                            NewPassword1.setTransformationMethod(null);
                            NewPassword1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_eyeopen, 0);

                        } else {
                            // 비밀번호를 숨기게 설정
                            NewPassword1.setTransformationMethod(new PasswordTransformationMethod());
                            NewPassword1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_eyeslash1, 0);
                        }
                        // 커서 위치 조정
                        NewPassword1.setSelection(NewPassword1.getText().length());
                        return true; // 이벤트가 처리되었음을 반환
                    }
                }
                return false;
            }
        });

        // on create 3. 비밀번호 재입력칸 비밀번호 보이게 하기 - NewPassword2
        NewPassword2.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    // 터치한 위치가 drawable end 인지 ??
                    if (event.getRawX() >= (NewPassword2.getRight() - NewPassword2.getCompoundDrawables()[2].getBounds().width())) {
                        // boolean 값 바꿔보기
                        isPasswordVisible2 = !isPasswordVisible2;
                        // 이미지 바꿔주기
                        if (isPasswordVisible2) {
                            // 비밀번호를 보이게 설정하고 이미지도 변경해줌
                            NewPassword2.setTransformationMethod(null);
                            NewPassword2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_eyeopen, 0);

                        } else {
                            // 비밀번호를 숨기게 설정
                            NewPassword2.setTransformationMethod(new PasswordTransformationMethod());
                            NewPassword2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_eyeslash1, 0);
                        }
                        // 커서 위치 조정
                        NewPassword2.setSelection(NewPassword2.getText().length());
                        return true; // 이벤트가 처리되었음을 반환
                    }
                }
                return false;
            }
        });


        // on create 4. 비밀번호 변경 버튼 누르기
        password_change_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 비밀번호 변경 버튼 누름 !!
                // 변경 버튼 누를때 비밀번호 유효성 검사, 일치하는지 검사 하기
                String new_password1 = NewPassword1.getText().toString();
                String new_password2 = NewPassword2.getText().toString();
                new_password_change(new_password1, new_password2, user_email);
            }
        });


    } // on create 끝

    // 비밀번호 변경 메소드 - retrofit
    private void new_password_change(String new_password1, String new_password2, String user_email) {
        service.password_change(new_password1, new_password2, user_email).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();

                if (result.getCode() == 200) {
                    // 비번 변경이 무사히 이뤄졌으면, 로그인 화면으로 이동한다.
                    Toast.makeText(getApplicationContext(), "비밀번호가 변경되었습니다. 다시 로그인 해주세요", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                    intent.putExtra("user_email", user_email);
                    startActivity(intent);

                } else if (result.getCode() == 41) {
                    // 올바른 비밀번호 형식이 아님.
                } else if (result.getCode() == 42) {
                    // 비밀번호가 일치하지 않음. 2번으로 포커스 옮기기
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
    } // method 1 끝

}
