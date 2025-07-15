package com.example.myapplication.layout;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.ImageUploadResponse;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.data.retrofit.responsemodel.ProfileViewResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    // import
    RetrofitService service; // 레트로핏 서비스


    // 쉐어드 프리퍼런스 - 로그인 유지용
    PreferenceManager pref;
    String key = "signin_email_id";


    // view 초기화
    Toolbar toolbar; // 툴바


    // 프사, 이메일아이디, 닉네임, 소개(bio)
    ImageView edit_img;
    EditText user_emailId, edit_nickname, edit_bio;

    // 수정 하기 버튼
    ImageView editBtn_image, editBtn_nickname, editBtn_bio;


    // 수정 완료 버튼
    Button edit_save_btn;

    // String - intent 로 넘어온 유저 번호, 유저 닉네임.
    String this_user_number;
    String this_user_nickname;


    // 기존 소개글
    String before_bio;
    int userNumber_int; // 유저 번호 int 값


    // 새 프사 골랐을때
    String new_profile_img = null;
    String new_profile_nickname = null;

    Uri imgUri;

    boolean basic_img = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit);


        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의

        // 쉐어드
        pref = new PreferenceManager();


        // ======================= 초기화 zone 시작 =================================

        // 툴바
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }


        // view 초기화
        // contents - 프사, 닉넴, 소개, 이멜아이디(수정x)
        edit_img = findViewById(R.id.user_profileImg);
        edit_nickname = findViewById(R.id.user_nickname);
        edit_bio = findViewById(R.id.user_description);
        user_emailId = findViewById(R.id.user_emailId);

        // buttons - 프사, 닉넴, 소개, 저장 버튼
        editBtn_image = findViewById(R.id.btn_edit_img);
        editBtn_nickname = findViewById(R.id.btn_edit_nickname);
        editBtn_bio = findViewById(R.id.btn_edit_description);
        edit_save_btn = findViewById(R.id.edit_save_btn);


        // on create 1. 툴바 뒤로가기 클릭 시, 이전 화면으로 (프래그먼트로 나가게 됨.)
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        // on create 1 끝


        // on create  1. intent 로 넘어온 user number 받기
        Intent getIntent = getIntent();
        this_user_number = getIntent.getStringExtra("user_number");
        this_user_nickname = getIntent.getStringExtra("user_nickname");


        // on create  2. 유저 넘버를 가지고 정보 가져오기
        userNumber_int = Integer.parseInt(this_user_number);
        get_users_info(userNumber_int, userNumber_int, this_user_nickname);


        // 저장할 때 한번에 php 에서 업데이트 하기 !! ??
        // 사진은 새로 업로드 하면 string 값에다가 담기 !!. null 일때는 아예 변경 안되게 하고..
        // 닉네임은 가져가서 유효성 검사하고, update 해주기.
        // 바이오는 그냥 업데이트.

        // on create 2-1) 프사 변경 버튼. 클릭 및 사진 업로드.
        editBtn_image.setOnClickListener(v -> {
            showImageSelectionDialog();

        });

        // on create 2-2) 닉네임 변경 버튼 클릭

        editBtn_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditProfileActivity.this, "닉넴 수정 버튼 클릭됨", Toast.LENGTH_SHORT).show();


                // 다이얼로그에 edit text 추가하기

                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle("새 닉네임을 입력하세요");

                // edit text 생성하기
                final EditText input = new EditText(EditProfileActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(this_user_nickname); // 기존 닉네임 가져 오기.
                builder.setView(input);

                // 변경, 취소 버튼
                // 변경 누르면 중복 확인 메소드 실행하기

                builder.setPositiveButton("변경", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userInput = input.getText().toString();
                        // 입력된 닉네임에 대해서, 유효성 및 중복 확인 실행 하기.
                        Log.e("닉네임변경시도!! ", "새 닉넴: " + userInput + ", 현재유저번호: " + userNumber_int);
                        edit_nickname_check(userNumber_int, userInput);
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                // 다이얼로그 표시
                builder.show();

            }
        });


        // on create 2-3) 소개 변경 버튼
        editBtn_bio.setOnClickListener(v -> {
            Toast.makeText(EditProfileActivity.this, "소개 수정 버튼 클릭됨", Toast.LENGTH_SHORT).show();

            // 다이얼로그에 edit text 추가하기

            AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
            builder.setTitle("소개글을 작성하세요");

            // edit text 생성하기
            final EditText input = new EditText(EditProfileActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(before_bio); // 기존 바이오 가져 오기.
            builder.setView(input);

            // 변경, 취소 버튼
            // 변경 누르면 중복 확인 메소드 실행하기

            builder.setPositiveButton("변경", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String userInput = input.getText().toString();
                    // 입력된 닉네임에 대해서, 유효성 및 중복 확인 실행 하기.
                    Log.e("소개글 변경 완료!! ", "새 소개글: " + userInput + ", 현재유저번호: " + userNumber_int);
                    edit_bio.setText(userInput);
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            // 다이얼로그 표시
            builder.show();
        }); //

        // new_profile_img 가 null 인지 아닌지 확인 !!
        // 닉네임도 바꿨는지, 안 바꿨는지 확인 하고 new_profile_nickname
        // bio 는 그냥 들고 들어가서 update


        // on create 3) 변경된 정보 저장 버튼
        edit_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("저장 버튼 클릭 ", "");
//                Toast.makeText(EditProfileActivity.this, "저장 버튼 클릭", Toast.LENGTH_SHORT).show();

                // 새로 저장될 닉네임, 바이오 정보.
                Log.e("변경될 소개글!! ", "새 닉넴(null 일수도): " + new_profile_nickname + ", 새 소개글: " + edit_bio.getText().toString());
                String new_profile_bio = edit_bio.getText().toString();
                if (new_profile_bio.isEmpty()) {
                    new_profile_bio = null;
                }
                // update 하는 레트로핏 소환
                profile_edit(new_profile_nickname, new_profile_bio);

            }
        });


    } // on create 끝


    // EditProfileActivity
    // method 1. 유저의 현재 정보 가져오기. - 마이페이지에서 유저 정보 가져올때 사용한 메소드 그대로 사용함
    // 필요한 정보 - 유저 프사, 닉네임, 이메일아이디, 바이오 총 4개임.
    public void get_users_info(int user_number, int this_users_number, String this_nickname) {
        Log.i("수정할 내 정보- 레트로핏", "내번호: " + this_users_number + ", 로그인한유저번호(내번호): " + user_number);
        service.other_profile_info(user_number, this_users_number, this_nickname).enqueue(new Callback<ProfileViewResponse>() {
            @Override
            public void onResponse(Call<ProfileViewResponse> call, Response<ProfileViewResponse> response) {
                ProfileViewResponse result = response.body();
                Log.e("내정보가져오기 성공 ", " reponse 진입함");
                Log.e("내정보가져오기 응답코드php ", " reponse 코드: " + response.message());
                Log.e("내정보가져오기 정보 ", " 유저번호: " + result.getMessage() + ", 유저닉네임: " + result.getUser_nickname() + ", 유저 읽은책: " + result.getUser_readBook_cnt() + ", 읽고싶: " + result.getUser_wantBook_cnt() + ",읽고 있는: " + result.getUser_readingBook_cnt());
                if (result.getProfile_img() == null) {
                    Log.e("내정보가져오기 -프사없음 ", "프사없음");
                } else { // 있을 때만 setting 해주기

                    Glide.with(EditProfileActivity.this)
                            .load((String) null) // 기존 이미지를 초기화
                            .into(edit_img);

                    Log.e("내정보가져오기 -프사있음 ", "프사있음");
                    String img_serverAddress = "http://3.39.255.234/php/img/";
                    String userProfileImg = result.getProfile_img();
                    String img_url = img_serverAddress + userProfileImg;
                    Glide.with(EditProfileActivity.this)
                            .load(img_url)
                            .apply(RequestOptions.circleCropTransform()) // 원형 변환 적용
                            .into(edit_img);
                } // 사진 골랐을 때 끝

//                // view 세팅 해주기

                edit_nickname.setText(result.getUser_nickname());
                before_bio = result.getProfile_bio();
                edit_bio.setText(before_bio);
                user_emailId.setText(result.getUser_emailId());
            }

            @Override
            public void onFailure(Call<ProfileViewResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("내정보 가져오기 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("내정보 가져오기 실패. ", "onFailure: " + throwable.getCause());
            }
        });
    } // 메소드 1 끝


    // method 2. 사진 고름
    private ActivityResultLauncher<Intent> imageSelectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null) {
                        Toast.makeText(getApplicationContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        imgUri = data.getData();
                        if (imgUri != null) {
                            basic_img = false;
                            Glide.with(EditProfileActivity.this)
                                    .load((String) null) // 기존 이미지를 초기화
                                    .into(edit_img);

                            Log.e("사진 고름!! 1개고름", "이미지 uri: " + imgUri.toString());
                            Glide.with(EditProfileActivity.this)
                                    .load(imgUri)
                                    .apply(RequestOptions.circleCropTransform()) // 원형 변환 적용
                                    .into(edit_img);
                            Log.e("사진 넣음 1개고름", "이미지 uri: " + imgUri.toString());
                            new_profile_img = String.valueOf(imgUri);  // 새로운 이미지 고르면 넣어줌.
                            basic_img = false;
                        } else {
                            Toast.makeText(getApplicationContext(), "사진은 총 10장까지 추가 가능 합니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
    );


    // method 3. 새 프사 서버에 저장 하는 코드 - 새로운 프사를 등록할 경우 에만 실행 하기.
    // string new image 가 null 이 아닐 때에만


    // method 4. 새 닉네임 중복 확인 하는 코드  - 현재 유저 number 와 바꾸고자 하는 닉네임을 가지고 들어가면 됨.
    public void edit_nickname_check(int user_number, String edited_user_nickname) {
        Log.e("마이페이지-새 닉넴 중복확인", " 유저번호: " + user_number + ", 변경할 유저닉네임: " + edited_user_nickname);
        service.edit_nickname_check(user_number, edited_user_nickname).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // 결과 가져오기
                LoginResponse result = response.body();
                Log.e("닉네임 중복확인성공!", " 코드: " + result.getCode() + ", 메시지: " + result.getMessage());

                if (result.getMessage().equals("존재")) {
                    Toast.makeText(EditProfileActivity.this, "이미 사용 중인 닉네임 입니다", Toast.LENGTH_SHORT).show();
                } else if (result.getMessage().equals("유효성미통과")) {
                    Toast.makeText(EditProfileActivity.this, "닉네임은 한글, 영문 대소문자, 숫자만 사용가능합니다.", Toast.LENGTH_SHORT).show();

                } else if (result.getMessage().equals("사용가능")) {
                    Toast.makeText(EditProfileActivity.this, "사용 가능한 닉네임 입니다.", Toast.LENGTH_SHORT).show();
                    edit_nickname.setText(edited_user_nickname);
                    // 새 닉네임 변수에 저장 - save 할때 필요
                    new_profile_nickname = edited_user_nickname;
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                throwable.getMessage();
                Log.e("닉네임 중복 확인 통신 실패", "onFailure:실패한이유: " + throwable.getMessage());

            }
        });
    } // edit_nickname_check 끝


    // method 5.
    public void profile_edit(String edited_nickname, String edited_bio) {
        // 유저 번호도 넘기기
        Log.i("내 정보 수정 레트로핏 메소드 실행 ", "수정할닉넴: " + edited_nickname + ",수정할 바이오: " + edited_bio);
        service.profile_edit(userNumber_int, edited_nickname, edited_bio).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("내 정보 수정 확인 성공!", " 코드: " + result.getCode() + ", 메시지: " + result.getMessage() + ", 닉네임: " + result.getUser_nickname());

                // 닉네임이 null 이 아닐때 !
                if (result.getUser_nickname() != null) {
                    // 쉐어드에 있는 닉네임 값 바꿔줌

                    SharedPreferences sharedPreferences = getSharedPreferences("session_contain", MODE_PRIVATE);
                    String jsonString = sharedPreferences.getString(key, null);

                    if (jsonString != null) {
                        try {
                            // JSON 문자열을 JSONObject로 변환
                            JSONObject jsonObject = new JSONObject(jsonString);

                            // nickname 수정
                            jsonObject.put("nickname", result.getUser_nickname()); // 새로운 닉네임으로 변경

                            // 수정된 JSON 객체를 문자열로 변환
                            String updatedJsonString = jsonObject.toString();

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(key, updatedJsonString);
                            editor.apply();
                            Log.d("쉐어드닉넴수정완!!", "nickname이 성공적으로 수정되었습니다. 새 닉넴 포함된 json: " + updatedJsonString);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JSON Update", "JSON 파싱 오류: " + e.getMessage());
                        }
                    } else {
                        Log.e("쉐어드 비었음!!", "저장된 JSON이 없습니다.");
                    }
                }
                // 수정 완료 됨 !!!
                // 이미지 null 인지 확인 - 수정할 프사가 없으므로 !! 뒤로 가기
                Log.e("내 정보 수정 레트로핏 메소드 실행 new_profile_img", "new_profile_img: " + new_profile_img);
                if (new_profile_img == null) {
                    Log.e("프로필 편집이 완료되었습니다!", "");
                    Toast.makeText(EditProfileActivity.this, "프로필 편집이 완료되었습니다! ", Toast.LENGTH_SHORT).show();
                    // 뒤로 가기.
                    onBackPressed();
                } else { // 수정할 프사가 존재함.

                    if(basic_img) {
                        uploadDefaultImage(this_user_number);
                    } else {
                       uploadImage(imgUri, this_user_number);
                    }
                      // 레트로핏 하나 더 들어감.
                    Toast.makeText(EditProfileActivity.this, "프로필 편집이 완료되었습니다! ", Toast.LENGTH_SHORT).show();
                    onBackPressed();

                }


            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                throwable.getMessage();
                Log.e("내 정보 수정 통신 실패", "onFailure:실패한이유: " + throwable.getMessage());

            }
        });

    } // 수정 메소드 끝 - 닉네임, 바이오


    // 사진 관련 메소드 3개

    // 사진 1) 이미지 확장자 반환하는 메소드
    private String getMimeType(File file) {

        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getPath());
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        Log.e("현재 프사이미지의 타입은? ", "type: " + type);
        return type;
    }


    // 사진 2) 파일 uri 가지고 파일 생성해서 반환하는 메소드!! 이걸 서버에 보낼 것임.
    private File createFileFromInputStream(InputStream inputStream, String fileName) {
        try {
            File file = new File(getCacheDir(), fileName);
            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 사진 3) input stream 으로 원래 파일의 이름을 얻는 메소드
    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }


    // method 6. 새 프사 이미지 서버에 업로드하는 메소드 - 닉, 바이오 수정 후에, 새 프사 유무에 따라 소환 !!
    private void uploadImage(Uri imageUri, String user_number) {
        Log.e("새 프사 이미지 서버에 업로드", "imageUri: " + imageUri);
        // 파일을 얻기 위해 uri 를 사용한다.
        // File file = new File(getRealPathFromURI(imageUri), ""); // File 객체를 생성하기 위해서는 '파일 시스템 경로'가 필요함  XXXXXX 이거 아님
        ContentResolver resolver = getContentResolver();
        try (InputStream inputStream = resolver.openInputStream(imageUri)) {
            String originalFileName = getFileNameFromUri(imageUri);
            String uniqueFileName = java.util.UUID.randomUUID().toString() + "_" + originalFileName;

            File file = createFileFromInputStream(inputStream, uniqueFileName);
            String mimeType = getMimeType(file); // 선택한 사진이 png일 경우, jpeg일 경우에 대한 분기처리하는 메소드 호출.

            // http 요청할때 요청 본문의 내용을 담는 클래스임.
            // 주로 파일, 문자열, json데이터 등을 서버로 전송할 때 사용됨.
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
            // file을 포함하는 멀티파트폼 데이터로 서버로 전송할 수 있게 됨. -  for문 전
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile); // 실제 이미지 파일을 포함하고 있음. name = image

            // board number를 Request body 로 변환하기
            RequestBody userNumber = RequestBody.create(MediaType.parse("text/plain"), user_number);


            // 기본 이미지 아님
            RequestBody isDefaultImage = RequestBody.create(MediaType.parse("text/plain"), "false");


            // 서버의 이미지를 저장하기 위해 서버로 전달하는 http 통신 요청문의 body part
            Log.e("서버 이미지 저장 요청body ", "서버 이미지저장 요청 body: " + body.toString());
            Log.e("서버 이미지 저장  ", "서버 이미지저장 요청 body: " + body.headers().toString());
            Log.d("RequestBody 요청body", requestFile.toString()); // requestFile은 RequestBody 객체

            // 기존의 Retrofit 클라이언트를 사용해 서버로 이미지를 업로드
            Call<ImageUploadResponse> call = service.uploadProfileImage(body, userNumber, isDefaultImage);
            call.enqueue(new Callback<ImageUploadResponse>() {
                @Override
                public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                    if (response.isSuccessful()) {
                        ImageUploadResponse result = response.body();

                        String imgURL = response.body().toString();
                        if (result.isSuccess()) {
                            Log.e("이미지 서버저장한 boolean", "트루임");
                        } else {
                            Log.e("이미지 서버저장한 boolean", "폴스임");
                        }
                        Log.e("이미지 서버저장한 메시지: ", result.getMessage());
                        Log.e("이미지 서버저장한 URL: ", result.getImageUrl());
                    }
                    new_profile_img= null;
                }

                @Override
                public void onFailure(Call<ImageUploadResponse> call, Throwable throwable) {
                    Log.e("이미지 서버에 업로드 에러남!! ", throwable.getMessage());
                    new_profile_img= null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // method 6-2. 새 프사 이미지 서버에 업로드하는 메소드
    // 파일 없이 기본 이미지 처리
    private void uploadDefaultImage(String userNumber) {
        RequestBody userNumberBody = RequestBody.create(MediaType.parse("text/plain"), userNumber);
        RequestBody isDefaultImage = RequestBody.create(MediaType.parse("text/plain"), "true");

        // 이미지 파일 부분을 null로 설정
        MultipartBody.Part imagePart = null;

        Call<ImageUploadResponse> call = service.uploadProfileImage(imagePart, userNumberBody, isDefaultImage);
        call.enqueue(new Callback<ImageUploadResponse>() {
            @Override
            public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                if (response.isSuccessful()) {
                    Log.e("응답", "기본 이미지 처리 성공: " + response.body().getMessage());
                } else {
                    Log.e("응답", "실패: " + response.message());
                }
                new_profile_img = null;
            }

            @Override
            public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
                Log.e("실패", t.getMessage());
                new_profile_img= null;
            }
        });
    }

    // method 7. 기본 이미지 or 갤러리 다이얼로그 호출 메소드
    private void showImageSelectionDialog() {
        String[] options = {"갤러리에서 선택", "기본 이미지로 변경", "취소"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("이미지 추가");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Log.d("게시글 이미지 추가 버튼 누름", "");
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                imageSelectionLauncher.launch(intent);

            } else if (which == 1) {
                // 기본이미지로 변경
                // 기본이미지로 변경
                basic_img = true;
                Uri defaultProfileImageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.icon_basic_profile4); // 기본 프로필 이미지 URI
                imgUri = defaultProfileImageUri;
                Glide.with(EditProfileActivity.this)
                        .load(defaultProfileImageUri)
                        .apply(RequestOptions.circleCropTransform()) // 원형 변환 적용
                        .into(edit_img);
                Log.e("사진 넣음 1개고름", "이미지 uri: " + defaultProfileImageUri.toString());
                new_profile_img = String.valueOf(defaultProfileImageUri);  // 기본이미지의 uri를 넣어줌...
                // icon_bagic_profile4.jpg
                basic_img = true;
            } else {
                // 다이얼로그 닫기
                dialog.dismiss(); // 다이얼로그를 닫는다.
            }
        });
        builder.show();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


}
