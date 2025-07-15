package com.example.myapplication.layout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.example.myapplication.R;
import com.example.myapplication.data.volley.MySingleton;

public class EmailJoinActivity extends AppCompatActivity {


    // 뷰 가져오기.
    // 이메일, 닉네임, 비밀번호, 비밀번호 재입력

    EditText email_input,number_verify_input,nickname_input, password_input, password2_input;
    Button email_join_btn, email_send_btn, email_verify_btn;

    TextView verify_done ;

    // 이메일 인증번호 입력칸
    LinearLayout number_verify_container;

    String verified_email = null;  // 이메일 인증하고자 하는 이메일 (유효성, 중복검사 통과)
    Boolean email_verified_done = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_join);

        email_input = findViewById(R.id.join_email);
        number_verify_input = findViewById(R.id.number_verify_input); // 인증번호 입력 칸
        verify_done = findViewById(R.id.verify_done);
        nickname_input = findViewById(R.id.join_nickname);
        password_input = findViewById(R.id.join_password);
        password2_input = findViewById(R.id.join_password_check);
        email_join_btn = findViewById(R.id.join_btn);
        email_send_btn = findViewById(R.id.email_send_btn); // 이메일 인증번호 전송 버튼
        email_verify_btn = findViewById(R.id.email_verify_btn); // 인증번호 확인 버튼
        number_verify_container = findViewById(R.id.number_verify_container); // 이메일 인증번호 입력칸 container


        // 인증번호 전송
        email_send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 에러 메시지 null 로 초기화 시키기
                email_input.setError(null);
                Log.d("이메일 인증번호 전송버튼", "버튼누름");

                String new_emailText = email_input.getText().toString();
                Log.d("이메일 인증번호 전송", "작성한이메일: " + new_emailText);

                if (new_emailText.isEmpty()) {  // 인증번호 보낼때 이메일이 null 인 경우
                    email_input.setError("이메일을 입력해주세요");
                    email_input.requestFocus(); // 포커스 이동 시켜주기
                } else {  // 이메일 입력 값이 있는 경우
                    new EmailVerifyTask(email_send_btn).execute(new_emailText);
                }
            }
        });


        // 인증번호 확인
        email_verify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 인증번호 입력칸 에러 메시지 null 로 초기화 시키기
                number_verify_input.setError(null);
                Log.d("이메일 인증번호 확인버튼", "버튼누름");

                if(verified_email ==null ) {
                    Log.e("인증verified_email null값뜸.", " verified_email 가 null");
                } else {
                    String verify_numbers = number_verify_input.getText().toString();
                    Log.d("인증번호 확인 가능함.", " 작성한 인증번호: " + verify_numbers);

                    if (verify_numbers.isEmpty()) {  // 인증번호 확인시, 입력값 없을때
                        number_verify_input.setError("인증번호를 입력해주세요");
                        number_verify_input.requestFocus(); // 포커스 이동 시켜주기
                    } else {  // 인증번호 있을때
                        new NumberVerifyTask(email_verify_btn).execute(verified_email, verify_numbers);
                    }
                }


            }
        });


        // 회원가입 버튼 클릭 이벤트
        email_join_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 에러 메시지 null 로 초기화 시키기
                email_input.setError(null);
                nickname_input.setError(null);
                password_input.setError(null);
                password2_input.setError(null);


                // 사용자가 작성한 내용 String 에 담기
                String email_text = email_input.getText().toString();
                String nickname_text = nickname_input.getText().toString();
                String password_text = password_input.getText().toString();
                String pwCheck_text = password2_input.getText().toString();


                boolean cancel = false; //
                View focusView = null; // 얘는 뭐지. 일단 해보기.


                // 이메일 null 값 검사
                if(email_text.isEmpty()) {
                    email_input.setError("이메일을 입력해주세요");
                    focusView = email_input;
                    cancel = true;
                } else {
                    Log.d("이메일null검사", "이메일작성완");
                }

                // 닉네임 null 값 검사
                if(nickname_text.isEmpty()) {
                    nickname_input.setError("닉네임을 입력해주세요");
                    focusView = nickname_input;
                    cancel = true;
                } else {
                    Log.d("닉네임null검사", "닉네임작성완");
                }

                // 비밀번호 null 검사
                if(pwCheck_text.isEmpty()) {
                    password_input.setError("비밀번호를 입력해주세요");
                    focusView = password_input;
                    cancel = true;
                } else {
                    Log.d("비번 null검사", "비번 작성완");
                }

                // 비밀번호 확인 null 검사
                if(password_text.isEmpty()) {
                    password2_input.setError("비밀번호를 한번 더 입력해주세요");
                    focusView = password2_input;
                    cancel = true;
                } else {
                    Log.d("비번재입력 null검사", "비번2 작성완");
                }

                // 전부 null 이 아닐때
                if (cancel){
                    focusView.requestFocus(); // 해당 위치로 포커스 이동
                } else {
                    // 들어온 내용에 대해서 유효성 검사 마친 다음에 넘기기 !!
                    // 나중에 하기.
                    if (email_verified_done) { // 이메일 인증 했는지 확인.
                        joinNewAccount(email_text, nickname_text, password_text, pwCheck_text);
                    } else {
                        Toast.makeText(EmailJoinActivity.this, "이메일 인증을 완료해주세요", Toast.LENGTH_SHORT).show();
                    }

                }
            } // 클릭 이벤트
        });

        // 뒤로 가기 활성화 시키기
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("이메일 회원가입");


    }  // on create 끝

    // 뒤로가기 함수
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }


    // 이메일 인증 번호 전송 함수 !!
    public class EmailVerifyTask extends AsyncTask<String, Void, String> {
        private Button sendEmail_btn;

        // 생성자
        public EmailVerifyTask(Button sendEmail_btn) {
            this.sendEmail_btn = sendEmail_btn; // 이 클래스에 만들어놓은 속성 = 넘어온 매개변수를 할당해줌
        }

        // // execute로부터 실행되는 함수
        protected String doInBackground(String... params) {
            String email = params[0]; // execute 안에 넘어온 파라미터 / 기본설계가 배열이다. 여러개를 보낼 수 있게 하기 위해서.
            String result = "";

            try {
                // URL 설정
                URL url = new URL("http://ec2-3-39-255-234.ap-northeast-2.compute.amazonaws.com/php/android_email_verify.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();


                // HTTP POST 설정 / 기본적으로는 get임.
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");


                // 파라미터 설정
                String data = "email=" + email;


                // 데이터 전송
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(data.getBytes()); // 파라미터를 담고
                    os.flush(); // 전송
                }

                // 응답 받기
                // 데이터 전송 - 응답 받는 것을 모두 스레드에서 진행하므로써 메인 스레드의 과부하를 줄인다.

                String response = "";
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) !=null) { // 응답 받은 값이 있을때
                        response += line; //
                        Log.d("요청후 응답받음!!!", "이메일인증 요청 응답내용: " + response);
                    }
                }

                if (response.equals("Message sent")) {
                    result = response + ":" + email;  // 이메일을 결과에 추가
                    Log.d("이멜전송완료!", "결과(이메일 : 결과내용)" + result);
                } else {
                    result = response;
                }
                conn.disconnect(); // 연결 해제해주기


            } catch (IOException ie) { // http 통신을 위한 url 생성, post 정의, 데이터 전송, 응답 수신 zone
                Log.e("이메일 인증번호보내기", "post전송오류", ie);
                result= "Error";

            }
            return result;
        }  // do it background 끝나는 곳


        // 응답
        protected void onPostExecute(String result) {
            // 이메일 주소 추출
            String[] parts = result.split(":");  // ':'를 구분자로 사용
            String message = parts[0];
            String email = parts.length > 1 ? parts[1] : null;


            Log.e("인증번호 전송 후 post", "내용(message:email) : " + result.toString());
            Log.e("인증번호post", "내용(message) : " + message.toString());

            if(message.equals("이미 인증 번호가 전송되었습니다.")) {
                Toast.makeText(EmailJoinActivity.this, "이미 인증 번호가 전송되었습니다.", Toast.LENGTH_SHORT).show();
                View focusView = null;
                focusView = email_input;
            } // 이미 인증번호 전송함.

            if(message.equals("이미 사용 중인 이메일 입니다.")) {
                Toast.makeText(EmailJoinActivity.this, "이미 사용 중인 이메일 입니다.", Toast.LENGTH_SHORT).show();
                View focusView = null;
                focusView = email_input;
            } // 이미 사용 중인 이메일 입니다

            if(message.equals("잘못된 이메일 형식입니다")) {
                Toast.makeText(EmailJoinActivity.this, message.toString(), Toast.LENGTH_SHORT).show();
                View focusView = null;
                focusView = email_input;
            }

            if (message.equals("Mailer Error")) {
                Toast.makeText(EmailJoinActivity.this, "인증번호 전송에 실패했습니다. 다시 버튼을 눌러주세요.", Toast.LENGTH_SHORT).show();
                View focusView = null;
                focusView = email_input;
            } // 메일 발송 실패

            if (message.equals("Message sent")) {
                if (email != null) {
                    verified_email = email;  // 전송된 이메일을 여기에 저장

                    Toast.makeText(EmailJoinActivity.this, "인증번호가 전송되었습니다. 메일함을 확인해 주세요.", Toast.LENGTH_SHORT).show();

                    // 인증 번호 입력창 띄우기
                    number_verify_container.setVisibility(View.VISIBLE); // int 값임 (0X00000000)

                    // 이메일 입력칸 비활성화
                email_input.setClickable(false);
                email_input.setFocusable(false);
                email_input.setTextColor(R.color.hint_text); // inㅔt 값임. 왤까.

                    // 인증번호 재전송으로 바꾸기
                    sendEmail_btn.setText("인증번호 재전송");

                } else {
                    Toast.makeText(EmailJoinActivity.this, "응답받은 이메일 주소값이 비었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    // 이메일 인증번호 인증 함수 !!
    // 이메일 인증번호 인증 함수 !!
    public class NumberVerifyTask extends AsyncTask<String, Void, String> {
        private Button number_verify_btn;
        public NumberVerifyTask(Button NumbersVerifyTask) {
            this.number_verify_btn = NumbersVerifyTask; // 이 클래스에 만들어놓은 속성 = 넘어온 매개변수를 할당해줌
        }

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String numbers = params[1]; // execute 안에 넘어온 파라미터 / 기본설계가 배열이다. 여러개를 보낼 수 있게 하기 위해서.

            String result = "";

            try {
                // URL 설정
                URL url = new URL("http://ec2-3-39-255-234.ap-northeast-2.compute.amazonaws.com/php/email_verify_check.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // HTTP POST 설정
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // URL 인코딩을 위해 java.net.URLEncoder를 사용합니다.
                String encodedEmail = URLEncoder.encode(email, "UTF-8");
                String encodedNumbers = URLEncoder.encode(numbers, "UTF-8");


                // 이메일, 인증번호 파라미터 설정
                String data = "email=" + encodedEmail + "&verify_numbers=" + encodedNumbers;
                Log.d("인증번호확인파라미터", "백그라운드 data: " + data);

                // 데이터 전송
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(data.getBytes()); // 파라미터를 담고
                    os.flush(); // 전송
                }



                // 응답 받기
                // 데이터 전송 - 응답 받는 것을 모두 스레드에서 진행하므로써 메인 스레드의 과부하를 줄인다.
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) !=null) { // 응답 받은 값이 있을때
                        result += line; //
                        Log.d("인증번호 확인 응답받음.", "인증번호 확인 응답: " +"["+result+"]");
                    }
                }
                conn.disconnect(); // 연결 해제해주기


            } catch (IOException ie) { // http 통신을 위한 url 생성, post 정의, 데이터 전송, 응답 수신 zone
                Log.e("이메일 인증번호 인증하기", "post전송오류", ie);
                result= "Error";

            }
            return result;
        }

        // 인증번호 확인 응답
        // 이메일이 존재하지 않을 경우 : 인증번호 전송을 눌러주세요
        // 이메일이 있는데 인증번호가 틀릴 경우 : 인증번호가 잘못 되었습니다.
        // 이메일과 인증번호가 일치할 경우 : 이메일 인증이 완료 되었습니다.
        protected void onPostExecute(String result) {

            result = result.trim(); // 공백 제거

            Log.d("인증번호확인결과", "onPostExecute: " +"["+result+"]");
            if("이메일없음".equals(result)) {
                Toast.makeText(EmailJoinActivity.this, "인증번호 전송을 눌러주세요.", Toast.LENGTH_SHORT).show();
                View focusView = null;
                focusView = email_input;
            } //이메일이 존재하지 않을 경우


           else if ("인증번호불일치".equals(result)) {
                Toast.makeText(EmailJoinActivity.this, "인증번호가 잘못 되었습니다. 다시 입력해 주세요.", Toast.LENGTH_SHORT).show();
                View focusView = null;
                focusView = number_verify_input;
            } // 인증번호 틀림, 다시 입력하기

            else if ("done".equals(result)) {
                Log.e("인증번호 완료 !!!", "이멜인증 완료 !!! ");
                Toast.makeText(EmailJoinActivity.this, "이메일 인증이 완료 되었습니다.", Toast.LENGTH_LONG).show();
                // 이메일 인증번호 확인 버튼 비활성화, 인증이 완료되었습니다 text 보이게.
                email_verify_btn.setEnabled(false); // 이메일 인증 버튼 비활성화
                email_send_btn.setEnabled(false); // 이메일 인증번호 전송 버튼 비활성화
                verify_done.setVisibility(View.VISIBLE); // 인증 후, 인증이 완료되었습니다


                //
                email_verified_done = true; // 이메일 인증 전부 완료 !!!
            }
        }
    } // async 인증번호 확인 함수


    // 회원가입 함수 !!! null 검사, 유효성 검사 끝나면 실행됨.
    public void joinNewAccount (final String user_email, final String user_nickname,
                                final String user_pw, final String user_pwCheck) {

        Log.d("파라미터이메일", "userEmail: " + user_email);
        Log.d("파라미터닉네임", "user nickname: " + user_nickname);
        Log.d("파라미터비번", "userEmail: " + user_pw);
        Log.d("파라미터비번확인", "userEmail: " + user_pwCheck);

        // php 연결할 주소
        String uRl = "http://ec2-3-39-255-234.ap-northeast-2.compute.amazonaws.com/php/android_join.php";
        StringRequest request = new StringRequest(Request.Method.POST, uRl, new Response.Listener<String>() {
            // http 요청 생성해줌. 이 내용을 singletone 객체 생성해서 request 넣어줌.
            // 재활용이 가능함.
            @Override
            public void onResponse(String response) {
                response = response.trim(); // 공백 제거
                if (response.equals("회원가입성공")) {
                    Toast.makeText(EmailJoinActivity.this, response, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EmailJoinActivity.this, LoginActivity.class));
                    finish();
                } else {  // 회원가입 실패했을 경우, php 에서 받아온 원인을 뿌려줌.
                    Log.d("회원가입실패", "회원가입실패원인: " + response);
                    Toast.makeText(EmailJoinActivity.this, response, Toast.LENGTH_SHORT).show();
                }


                // 비밀번호가 일치하지 않을때, 비번 재입력 칸에 포커스 주기
                if(response.equals("비밀번호가 일치하지 않습니다")) {
                    View focusView = null;
                    focusView = password2_input;
                }

                // 이메일 중복일 때
                if(response.equals("이메일 형식을 확인해 주세요.") ||response.equals("이미 사용 중인 이메일 주소입니다.")) {
                    View focusView = null;
                    focusView = email_input;
                }

                // 닉네임 중복일 때
                if(  response.equals("닉네임은 한글, 영문 대소문자, 숫자만 사용가능합니다.") || response.equals("이미 사용 중인 닉네임 입니다.")) {
                    View focusView = null;
                    focusView = nickname_input;
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(EmailJoinActivity.this, error.toString(), Toast.LENGTH_SHORT).show(); // 에러 내용
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> param = new HashMap<>();
                param.put("userEmail", user_email);
                param.put("userNickname", user_nickname);
                param.put("userPw", user_pw);
                param.put("userPwCheck", user_pwCheck);

                // param 내용 출력
                Log.d("파라미터 저장값", "Params: " + param.toString());
                return param;


            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(EmailJoinActivity.this).addToRequestQueue(request);
    }
}
