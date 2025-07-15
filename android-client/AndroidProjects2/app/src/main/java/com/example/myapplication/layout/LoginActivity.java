package com.example.myapplication.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.R;
import com.example.myapplication.socket.SocketService;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import com.example.myapplication.data.retrofit.datamodel.KakaoJoinData;
import com.example.myapplication.data.retrofit.responsemodel.KakaoJoinResponse;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    // 쉐어드 프리퍼런스 - 로그인 유지용
    PreferenceManager pref;
    String key = "signin_email_id";

    // 뷰 가져오기
    EditText email_input;  // 이메일 input
    EditText pw_input;  // 비밀번호 input
    Button emailLogin_btn;  // 이메일 로그인 btn
    ImageButton gmailLogin;
    TextView email_join_btn, password_forgot;  // 이메일 회원가입 이동하기.
    CheckBox auto_signin_check; // 이메일 자동 로그인 체크 박스
    RetrofitService service; // 레트로핏 서비스 객체 초기화


    // 카카오톡 로그인
    private static final String TAG = "카카오로그인";
    private View kakako_loginBtn;
    private TextView kakao_nickName, kakao_email;
    private ImageView kakao_profile;
    private Button kakao_logoutBtn;


    // 비번 보이게 하기
    boolean isPasswordVisible = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 쉐어드
        pref = new PreferenceManager();


        PackageInfo packageInfo = null;
        // 카카오톡 로그인 api 사용을 위한 키해시 코드 추출
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }

        // 이메일 로그인
        email_input = findViewById(R.id.email_editText);
        pw_input = findViewById(R.id.pw_editText);
        emailLogin_btn = findViewById(R.id.email_login_btn);
        auto_signin_check = findViewById(R.id.auto_signin_check);
        password_forgot = findViewById(R.id.password_forgot); //비번 찾기로 이동하기

        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의

        // 로그인 하기
        emailLogin_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                attemptLogin();
            }
        });


        // 비밀번호 보이게 하기.
        pw_input.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_UP) {

                    // 터치한 위치가 drawable end 인지 확인 !!
                    if (event.getRawX() >= (pw_input.getRight() - pw_input.getCompoundDrawables()[2].getBounds().width())) {
                        // end 일때 (눈을 눌렀을때 )
                        // boolean 값을 반대로 바꿔줌.
                        isPasswordVisible = !isPasswordVisible;
                        // 이미지
                        if (isPasswordVisible) {
                            // 비밀번호를 보이게 설정하고 이미지도 변경해줌
                            pw_input.setTransformationMethod(null);
                            pw_input.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_eyeopen, 0);

                        } else  {
                            // 비밀번호를 숨기게 설정
                            pw_input.setTransformationMethod(new PasswordTransformationMethod());
                            pw_input.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_eyeslash1, 0);
                        }
                        // 커서 위치 조정
                        pw_input.setSelection(pw_input.getText().length());
                        return true; // 이벤트가 처리되었음을 반환

                    }
                }
                return false;
            }
        });

        // 이메일 회원가입으로 이동
        email_join_btn = findViewById(R.id.email_join_btn);
        email_join_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EmailJoinActivity.class);
                startActivity(intent);
            }
        });



        // 카카오톡 로그인 버튼
        kakako_loginBtn = findViewById(R.id.kakao2_login);
        kakao_logoutBtn = findViewById(R.id.logout);


        // 카카오톡 설치돼 있는지 확인 하는 메서드 call back 객체 이름임.
        Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                Log.e(TAG,"콜백 메소드 실행 !! ");
                //oAuthToken != null 이라면 로그인 성공
                if(oAuthToken!=null){
                    Log.d(TAG, "Login successful with token: " + oAuthToken.getAccessToken());
                    // 토큰이 전달된다면 로그인이 성공한 것(검증서버로부터 토큰을 무사히 받아왔다는 뜻. )
                    // 이고 토큰이 전달되지 않으면 로그인 실패한다.

                    // 토큰 사용하여서, 사용자의 정보를 가져온다.
                    UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                        @Override
                        public Unit invoke(User user, Throwable throwable) {
                            if (user != null) {
                                // user가 존재할때 정보 가져오기

                                String kakao_id = Long.toString(user.getId());
                                String kakao_email = user.getKakaoAccount().getEmail();
                                String kakao_nickname = user.getKakaoAccount().getProfile().getNickname();
                                String kakao_imgUrl = user.getKakaoAccount().getProfile().getProfileImageUrl();

                                // 요청 데이터 모델 생성
                                KakaoJoinData kakaoJoinData = new KakaoJoinData(kakao_id,kakao_email,kakao_nickname,kakao_imgUrl);

                                // db 에 저장하는 retrofit 사용해주기
                                // KakaoUserJoin(kakao_id, kakao_email, kakao_nickname, kakao_imgUrl);
                                KakaoUserJoin(kakaoJoinData);

                            } else {
                                Log.e(TAG, "로그인은성공BUT,카카오회원정보없음=DB 전송실패");
                            }
                            return null;
                        }
                    });
                    updateKakaoLoginUi();

                }else {
                    //로그인 실패
                    Log.e(TAG, "Login failㄷed");
                    if (throwable != null) {
                        Log.e(TAG, "Error during login", throwable);
                    } else {
                        Log.e(TAG, "No token and no error info");
                    }
                    Log.e(TAG, "invoke: login fail" );
                }
                return null;
            }
        };

        // 카카오톡 로그인 버튼 클릭 리스너
        kakako_loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "카카오 로그인 버튼 클릭");

                // callback 사용해서 해당 기기에 카톡 설치돼 있는지 확인하기
                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)) { // 설치 돼 있는 경우
                    Log.d(TAG, "카카오 톡 설치 돼 있음 ");
                    UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this, callback);

                } else {
                    // 설치 안된 경우
                    Log.d(TAG, "카카오 톡 설치 안 되어 있음 ");
                    UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, callback);
                }
            }
        });
        updateKakaoLoginUi();


        // 카카오톡 로그아웃 클릭 이벤트. 나중에 마이페이지 생기면 마이페이지로 옮겨갈 내용.
        kakao_logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 토큰 무효화 - kakao logout 기능
                UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        if(throwable == null) {
                            Log.d("카톡로그아웃성공1! ", "로그아웃 성공!!");

                            // 쉐어드에 있는 카카오 고유 id 삭제.
                            SharedPreferences sharedPreferences = getSharedPreferences("session_contain", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.remove(key); // 현재 쉐어드에 있는 카카오 고유 id = 로그인 중인 id 삭제

                            editor.apply(); // 변경 사항 바로 적용 (비동기적)

                            updateKakaoLoginUi();
                        } else {
                            Log.e("카톡로그아웃 실패! ", "로그아웃 실패!!!");
                        }
                        return null;
                    }
                });


            }
        });


        // 비밀 번호 찾기 이동 하기
        password_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 비번 찾는 액티비티로 이동하기
                Intent intent = new Intent(getApplicationContext(), LoginPasswordForgotActivity.class);
                startActivity(intent);
            }
        });


    } // on create

    // 카카오톡 로그인
    private void updateKakaoLoginUi() {
        // 로그인 여부에 따른 ui 설정
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null) {

                    // 카톡에서 주는 유저 아이디
                    Log.d(TAG, "invoke: 카톡 유저아이디(고유번호): "+ user.getId());
                    // 유저의 닉네임
                    Log.d(TAG, "invoke: 닉네임: "+ user.getKakaoAccount().getProfile().getNickname());
                    // 유저 이메일
                    Log.d(TAG, "invoke: 이메일: " + user.getKakaoAccount().getEmail());
                    // 유저의 닉네임
                    Log.d(TAG, "invoke: 이미지: "+ user.getKakaoAccount().getProfile().getProfileImageUrl());

                    kakako_loginBtn.setVisibility(View.GONE);
                    kakao_logoutBtn.setVisibility(View.VISIBLE);

                } else {
                    // 로그인이 되어있지 않다면 위와 반대로 다시 해주기.
                    kakako_loginBtn.setVisibility(View.VISIBLE);
                    kakao_logoutBtn.setVisibility(View.GONE);
                }
                return null;
            }
        });
    }

    // 로그인 메소드 1) 입력값 검사 메소드
    private void attemptLogin() {
        // null 값 검사, 에러메시지 세팅
        email_input.setError(null);
        pw_input.setError(null);

        boolean cancel = false; //
        View focusView = null; // 얘는 뭐지. 일단 해보기.

        // 입력 값 가져오기
        String emailText = email_input.getText().toString();
        String pwText = pw_input.getText().toString();
        Log.d("현재 login acticity", "입력한 이메일값: " + emailText );
        Log.d("현재 login acticity", "입력한 비번값: " + pwText );

        // 이메일 null 값 검사
        if(emailText.isEmpty()) {
            email_input.setError("이메일을 입력해주세요");
            focusView = email_input;
            cancel = true;
        } else {
            Log.d("이메일null검사", "이메일값들어옴");
        }

        // 비번 null 값 검사.
        if(pwText.isEmpty()) {
            pw_input.setError("비밀번호를 입력해주세요");
            focusView = pw_input;
            cancel = true;
        } else {
            Log.d("비번null검사", "비번 값 들어옴");
        }

        // 로그인 처리하기 = php로 넘기기 = http 통신 !!!!
        if(cancel) {
            focusView.requestFocus(); // true 가 나온 곳으로 포커스 이동함.
        } else {
            // 로그인 php 통신 보낼 것임. 유효성 검사 끝낸 데이터를 파라미터에 넣어 준다.
            startLogin(emailText, pwText);
        }
    } //attemptLogin 끝

    // 로그인 메소드 2) http 통신 보내기 retrofit
    private void startLogin(String email, String password){
        Log.d("로그인 액티비티", "startLogin의 이메일 값 : " + email + "startLogin의 비밀번호 값 : " + password );
        service.userLogin(email, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body(); // php 에서 받아온 값.
                Log.d("http 통신 성공!! reponse 진입함", "onResponse 코드: " + result.getCode());
                Log.d("http 통신 성공!! reponse 진입함", "onResponse 메시지: " + result.getMessage());

                Log.d("result.getCode()", "result.getCode() : " + result.getCode());
                // response에다가 code를 굳이 담는 이유가 뭐지  ?
                // A. 이거는 내가 code를 담는게 아니고 http 통신과정에서 담겨오는 것이다. http 의 응답 방법인거임. 내가 한게 아님. 200, 400 등등

                Toast.makeText(LoginActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show(); // php 메시지에 담아놓은 메시지 내용 토스트로 띄우기.

                if (result.getCode() == 200) {

                    // 통신연결하고 서비스 시작하기
                    Intent serviceIntent = new Intent(LoginActivity.this, SocketService.class);
                    startService(serviceIntent);

                    String save_form = "{\"emailid\":\""+result.getUser_email()+"\",\"nickname\":\""+result.getUser_nickname()+"\"}"; // value
                    pref.setString(getApplication(), key, save_form);

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class); // 현재 로그인 액티비티에서 main activity로 이동함.
                    intent.putExtra("user_emailid", result.getUser_email());
                    intent.putExtra("user_nickname",result.getUser_nickname());
                    startActivity(intent);
                    finish();

                   // finish(); // 현재 activity를 종료하여서 뒤로가기 버튼 눌렀을때 다시 로그인 화면이 나타나지 않도록 함.
                } else if (result.getMessage().equals("이메일 또는 비밀번호가 잘못되었습니다.")) {
                    Toast.makeText(LoginActivity.this, "이메일 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("http 통신 실패!!", "실패한 원인: " + result.getMessage());
                    Toast.makeText(LoginActivity.this, "로그인실패 : 통신은 받아왔으나 false 반환함.", Toast.LENGTH_SHORT).show();
                }
            } // on response 끝

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Log.d("http 통신실패 response 진입", "onFailure: " + throwable.getMessage(), throwable);

            } // on failure 끝
        }); // 로그인 요청
    }// start login - retrofit 이메일 로그인 완료



    // 카카오 로그인 - 회원정보 DB 저장위해 서버로 request 보내기 (Retrofit)
    private void  KakaoUserJoin(KakaoJoinData kakaoJoinData ) {
      //  Log.d("카카오로그인-DB", "KakaoUserJoin id 값 : " + kakao_id + " KakaoUserJoin 이메일 값 : " + kakao_email + " 닉네임: "+kakao_nickname + " 이미지url: " + kakao_imgUrl );

        // Call 인터페이스
        // enqueue 메소드
        service.kakaoJoin(kakaoJoinData).enqueue(new Callback<KakaoJoinResponse>() {
            @Override
            public void onResponse(Call<KakaoJoinResponse> call, Response<KakaoJoinResponse> response) {

                Log.d("카카오로그인DB저장response", "서버자체에서 보낸code: " + response.code());
                KakaoJoinResponse result = response.body(); // 카카오 DB 저장하고 넘어온 값.
                Log.d("통신성공!!! 카카오정보 DB 저장response", ": " + result.getCode() );

                // 성공했을때 쉐어드에도 담기.
                if (result.getCode() == 200) {
                    if (result.getMessage().equals("이미가입됨")) {
                        Log.d("카카오정보 이미 가입됨.", "고유 아이디: " + result.getKakao_id());
                    } else if(result.getMessage().equals("카카오소셜가입완료"))  {
                        Log.d("카카오정보 insert 완료!!", "고유 아이디: " + result.getKakao_id());
                    }
                    // 쉐어드 저장하기. id와 닉네임을 쌍으로 저장하기...
                    String kakaoLoginId = kakaoJoinData.getKakao_id();
                    String kakaoLoginNickname = kakaoJoinData.getKakao_nickname();
                    String save_form = "{\"emailid\":\""+kakaoLoginId+"\",\"nickname\":\""+kakaoLoginNickname+"\"}"; // value
                    pref.setString(getApplication(), key, save_form);
                    Log.d("카카오정보 쉐어드", "쉐어드 string: " + save_form);


                    Intent intent = new Intent(LoginActivity.this, MainActivity.class); // 현재 로그인 액티비티에서 main activity로 이동함.
                    intent.putExtra("kakao_id", result.getKakao_id());

                    startActivity(intent);
                    finish();
                    // finish(); // 현재 activity를 종료하여서 뒤로가기 버튼 눌렀을때 다시 로그인 화면이 나타나지 않도록 함.

                } else {
                    Log.d("http 통신 실패!!", "실패한 응답: " + response.code());
                    Toast.makeText(LoginActivity.this, "카카오계정로그인실패 : 통신은 받아왔으나 false 반환함.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<KakaoJoinResponse> call, Throwable throwable) {
                Log.d("kakao회원가입 통신실패 fail진입", "onFailure: " + throwable.getMessage(), throwable);
            }
        });

    }

} // 전체 액티비티
