package com.example.myapplication.layout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BookAddSelfActivity extends AppCompatActivity {


    // import
    RetrofitService service; // 레트로핏 서비스
    // 툴바
    Toolbar toolbar;

    //btn
    Button save_btn; // 저장 버튼

    // 이미지
    ImageView book_cover_view, book_cover_edit_btn; // 책 표지, 책 표지

    // uri
    Uri imgUri;

    // edit text
    EditText title_edit, author_edit, pub_edit, isbn_edit, discrip_edit; // 책 제목, 작가, 출판사, isbn, 페이지수

    // 책정보 값 넣어놓기
    String str_title, str_author, str_pub, str_isbn, str_discrip, str_bookcover;

    // 현재 로그인한 이메일 아이디
    String emailId;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mylibrary_book_selfadd);

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


        // 현재 로그인 중인 아이디
        Intent getIntent = getIntent();
        emailId = getIntent.getStringExtra("emailId");
        Log.d("book add self의 온크리에잇 로그인아이디", "emailId: " + emailId);

        save_btn = findViewById(R.id.book_selfadd_save_btn);
        book_cover_view = findViewById(R.id.book_selfadd_cover);
        book_cover_edit_btn = findViewById(R.id.book_selfadd_edit_img_btn);


        // 책 제목, 작가, 출판사, isbn
        title_edit = findViewById(R.id.book_selfadd_title_edit);
        author_edit = findViewById(R.id.book_selfadd_author_txt);
        pub_edit = findViewById(R.id.book_selfadd_pub_txt);
        isbn_edit = findViewById(R.id.book_selfadd_isbn_txt);
        discrip_edit = findViewById(R.id.book_selfadd_discrip_txt);


        // 입력 받아서 넘기기.
        // 사진은 어떻게 넘기면 좋을깡.
        // 내 서재에서 받아서 처리하는 정보들은? (인텐트) - null 이면 안되는 로직 있다면 삭제하기.
        // 내 서재 상세보기에서 null 값인 애들 오류 안나도록 - php=> 사진이랑 isbn null 이어도 됨.

        // 일단 사진 받아서 파일로 만드는 작업을 한다음에,, 그걸 서버로 가져가서 서버에 저장하고 서버의 저장한 주소를 디비에 저장함.
        // 제목이랑 작가는 null 안되게 클릭 이벤트에 조건 걸어놓기, 출판사는,,, 개인 출판일 수도 있잖아. 그러니까 null 허용하기.
        // null 인애들은 저장하기 전에 "빈값 EMPTY 뭐 이런걸로 바꾸어서 넣어주기.
        // 내가 저장한 책에 대해서 수정 할 수 있어야 하잖아. ? 선택한 책들도 수정 가능한가 ?
        // 총 페이지 수 가져오는 방향으로 수정하기. 근데 그럼 염병 다 수정해야 하는데... 일단 없이 가면 되나 ? ㅋ
        // 얼마나 걸릴 지 생각해보고 아닌거같으면 버려. 다른거 할거 많잖아. ㅋ
        // 그리고 발표할때 어떻게 할건지. 이게 왜 문제인지에 대해서 어떻게 설명할지 가져가기.

        // ======================= 초기화 zone 끝 =================================

        // 클릭 이벤트 작성할때 람다식 사용하기.
        // ==> 람다식으로 변경하면 더 간단하게 코드를 쓸 수 있음.
        // 명시적으로 override 를 표시하지 않지만, 내부적으로는 여전히 단일 추상 메소드를 구현하는 방식으로 작동한다.


        //c1. 사진 추가하기 - intent 로 갤러리 열기
        book_cover_edit_btn.setOnClickListener(v -> {
            Log.d("게시글 이미지 추가 버튼 누름", "");
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imageSelectionLauncher.launch(intent);
        });


        //c2. 저장 클릭 이벤트 - MyLibraryCreateActivity 로 이동하기. NULL 값 확인하기. 제목, 작ㄱ, 출판사,
        save_btn.setOnClickListener(v -> {
            Log.i("내서재에 저장버튼 클릭", "저장할책info>> 책제목: "+title_edit.getText().toString()+", 작가: "+author_edit.getText().toString()+", 출판사: "+ pub_edit.getText().toString() + ", isbn: " + isbn_edit.getText().toString() + "책소개: " + discrip_edit.getText().toString() );


            // 저장 클릭 시 실행될 코드
            // 비어 있는 edit text 확인하기 - 제목, 작가, 페이지 수, 출판사 필수.
            boolean hasEmptyFields = false;

            // EditText들 배열로 저장
            EditText[] editTexts = {
                    title_edit,
                    author_edit,
                    pub_edit,
                    discrip_edit
            };


            //반복문을 사용해 각 EditText의 값이 비어있는지 확인하고 "정보없음" 설정
            // 저장을 누르는 동시에 이렇게 되면 좀 이상할 것 같음... 음...
            // 여기서는 알림 팝업만 띄우는 걸로 하고. 저장하는 로직, 즉 인텐트로 MyLibraryCreateActivity 넘어가는 건 메소드로 만들어서 소환하기.
            for (EditText editText : editTexts) {
                if (editText.getText().toString().trim().isEmpty()) {
                    hasEmptyFields = true;
                    break;
                }
            }

            // 알림 팝업 띄우기
            if(hasEmptyFields) {
                // 커스텀 레이아웃을 Inflate
                LayoutInflater inflater = getLayoutInflater();
                View customView = inflater.inflate(R.layout.custom_alert_message, null);


                new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                        .setTitle("정보를 마저 기입해주세요!")
                        .setView(customView)
                        .setPositiveButton("확인", ((dialog, which) -> dialog.dismiss()))
                        .show();
            } else {
                // 모든 필드가 채워져 있으면 저장 수행하기.
                str_title = title_edit.getText().toString();
                str_author = author_edit.getText().toString();
                str_pub = pub_edit.getText().toString();
                str_discrip = discrip_edit.getText().toString();
                str_isbn = isbn_edit.getText().toString();

                // 이미지를 넘길때 string 으로 넘길 수가 있나? 말이 안되자나 => base 64로 하기.
                // is empty랑 null 은 다르다
                if (str_bookcover == null || str_bookcover.isEmpty())  { str_bookcover = "정보없음"; }
                if (str_isbn == null || str_isbn.isEmpty()) { str_isbn = "정보없음"; }

                // isEmpty()는 빈 문자열("")을 체크 // 문자열 안이 비어있는지
                //  문자열이 null인 경우 ? 아예 값이 없는건지. 체크

                // 전체 로그 한번 더 찍기
                Log.i("내서재에 저장버튼 클릭2", "현재 로그인한이메일아이디: " + emailId + "저장할책info>> 책제목: "+str_title+", 작가: "+str_author+", 출판사: "+ str_pub + ", isbn: " + str_isbn + "책소개: " + str_discrip + ", 책표지: " + str_bookcover);
                Intent intent = new Intent(getApplicationContext(), MyLibraryCreateActivity.class);
                intent.putExtra("emailId", emailId);
                intent.putExtra("title", str_title);
                intent.putExtra("author", str_author);
                intent.putExtra("description", str_discrip);
                intent.putExtra("publisher", str_pub);
                intent.putExtra("pubDate", "정보없음");
                intent.putExtra("cover", str_bookcover);
                intent.putExtra("isbn", str_isbn);
                intent.putExtra("FROM_FLAG", "BOOK_SELF");
                startActivity(intent);

            }
       }); // 클릭 이벤트 끝


    }


    // method1.
    // 엥 여기서 서버로 안가자나 !!!
    // 검색한 책 저장하는 순간은 언제인지?
    // 읽은, 읽고있는, 읽고싶은책 선택후 한번에 서버로 이동함.


    // method2.
    // 사진 가져오는 메소드
    private boolean isProcessingImage = false;
    private ActivityResultLauncher<Intent> imageSelectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && !isProcessingImage) {
                    isProcessingImage = true; // 이미지를 처리 중임을 표시
                    Intent data = result.getData();
                    if (data == null) {
                        Toast.makeText(getApplicationContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        imgUri = data.getData();
                        if (imgUri != null) {
                            Glide.with(BookAddSelfActivity.this)
                                    .load((String) null) // 기존 이미지를 초기화
                                    .into(book_cover_view);

                            Log.e("사진 고름!! 1개고름", "이미지 uri: " + imgUri.toString());
                            Glide.with(BookAddSelfActivity.this)
                                    .load(imgUri)
                                    .into(book_cover_view);
                            Log.e("사진 넣음 1개고름", "이미지 uri: " + imgUri.toString());
//                            new_profile_img = String.valueOf(imgUri);  // 새로운 이미지 고르면 넣어줌.

                            // image를 base 64로 변경하기
                            try {
                                InputStream inputStream = getContentResolver().openInputStream(imgUri);
                                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

                                // 이미지 리사이징
                                int newWidth = 300; // 원하는 폭 (예: 300px)
                                int newHeight = (int) ((double) originalBitmap.getHeight() / originalBitmap.getWidth() * newWidth);
                                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);


                                String base64String = bitmapToBase64(resizedBitmap);
                                Log.e("비트맵64변환함", "base64String: " + base64String.substring(0, 100) + "...");
                                Log.e("비트맵64변환함", "base64String 길이: " + base64String.length()); // 갤러리 사진 한 장: 424218임. -> 리사이징 하면 8만 7천 정도로 줄음
                                str_bookcover = base64String;
                             //   Log.e("비트맵64변환함 str_bookcover", "str_bookcover: " + str_bookcover);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "이미지 변환 중 오류가 발생 했습니다.", Toast.LENGTH_LONG).show();
                            } finally {
                                isProcessingImage = false; // 처리 완료 후 다시 플래그 해제
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "사진은 총 10장까지 추가 가능 합니다.", Toast.LENGTH_LONG).show();
                            isProcessingImage = false;
                        }
                    }
                }
            }
    ); // imageSelectionLauncher 끝



    // method 3.
    private String bitmapToBase64 (Bitmap bitmap) {
        // 비트맵 이미지를 Base64 문자열로 변환하는 함수
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }




}
