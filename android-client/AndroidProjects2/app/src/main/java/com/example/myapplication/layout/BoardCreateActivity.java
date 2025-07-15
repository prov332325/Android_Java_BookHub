package com.example.myapplication.layout;

import static com.google.gson.internal.bind.TypeAdapters.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.recyclerview.BoardBookAdapter;
import com.example.myapplication.data.recyclerview.BoardImageAdapter;
import com.example.myapplication.data.recyclerview.SearchBookData;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.BoardResponse;
import com.example.myapplication.data.retrofit.responsemodel.ImageUploadResponse;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.data.retrofit.responsemodel.MylibraryResponse;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class BoardCreateActivity extends AppCompatActivity {



    private static final int REQUEST_CODE_BOOK_SEARCH = 1011;
    private static final int REQUEST_CODE_IMAGE_SELECTION = 1022;


    // 카메라 사용을 위한 런타임 권한 요청
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;



    // TAG
    private static final String TAG = "사진추가하는main 액티비티";

    // import
    RetrofitService service; // 레트로핏 서비스
    Toolbar toolbar;
    ArrayList<ImageUploadResponse> uriList = new ArrayList<>();

    List<SearchBookData> bookList = new ArrayList<SearchBookData>();


    // 사진, 책 리사이클러뷰 및 어댑터
    RecyclerView img_recyclerView, book_recyclerview; // 사진, 책 리사이클러뷰
    BoardImageAdapter boardImageAdapter; // 사진 어댑터

    BoardBookAdapter boardBookAdapter; // 책 어댑터




    // 쉐어드 프리퍼런스
    SharedPreferences sharedPreferences;

    // 쉐어드 id, email 각각에 대한 키값
    String key = "signin_email_id";
    String signin_email_id_value ;



    // 레트로핏으로 가져온 user numnber
    String user_number;


    // view
    EditText board_title, board_content; // 게시글 제목, 내용 view
    Button board_save_btn; // 게시글 저장 버튼
    TextView board_image_btn; // 사진 추가버튼
    TextView board_books_btn; // 책 추가 버튼


    // string
    String selected_board_category = null; // 선택된 게시글 카테고리
    String emailId = null; // 쉐어드로부터 파싱한 이메일아이디값.


    // 런타임 퍼미션 - READ_EXTERNAL_STORAGE
    private static final int REQUEST_CODE_PICK_IMAGE = 101;
    private static final int REQUEST_CODE_PERMISSIONS = 102;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };




    // 책 추가시 사용할 (도서 검색후 게시글에 추가할 책 정보를 받기 위함) 액티비티 리절트 런처
    ActivityResultLauncher<Intent> bookSearchLauncher;


    // 카메라
    private Uri photoUri;
    private String imageFilePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_create);

        // ==================================== view  초기화 시작 =============================================

        // 레트로핏
        service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의

        board_title = findViewById(R.id.board_create_title);
        board_content = findViewById(R.id.board_create_content);
        board_save_btn = findViewById(R.id.board_save_btn);

        // 툴바
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 표시 비활성화
        }

        // 사진추가 버튼 - text view에 클릭 이벤트 줄것임.
        board_image_btn = findViewById(R.id.board_create_pictures);

        // 사진 추가 - 리사이클러뷰 찾기
        img_recyclerView = findViewById(R.id.board_pictures_recyclerview);



        // 책 추가 버튼
        board_books_btn = findViewById(R.id.board_create_books);

        // 책 추가 - 리사이클러뷰
        book_recyclerview = findViewById(R.id.board_books_recyclerview);




        // ====================================view 초기화 끝 ===========================================

        // 뒤로 가기
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // 뒤로 가기 동작 수행
            }
        });


        //  on create 0. 쉐어드에서 닉네임 가져오기
        sharedPreferences = getSharedPreferences("session_contain", Context.MODE_PRIVATE);
        signin_email_id_value = sharedPreferences.getString(key, null);

        // email or id 있는지 !
        if (signin_email_id_value==null) {  // 쉐어드가 아예 비었을때 !! 로그인 하러 가기
            Log.d("게시글 작성 - 쉐어드 비어있음", "?? 왜죠 ? ");
            Toast.makeText(BoardCreateActivity.this, "로그인 후 게시글 작성이 가능합니다.", Toast.LENGTH_SHORT).show();

        } else { // 쉐어드에 값이 있을때 !!
            Log.d("쉐어드에 값이 있음.", "게시글 작성 액티비티임. key:signin_email_id  value: "+signin_email_id_value);
            // JSON 파싱하여 특정 키 값 추출
            try {
                JSONObject jsonObject = new JSONObject(signin_email_id_value);
                emailId = jsonObject.optString("emailid", ""); // default 값 설정 가능
                String nickname = jsonObject.optString("nickname", "");
                // 파싱 완료하면 !!! null이 아닐때 유저의 넘버 가져오기 !!
                get_userNumber(emailId);


                if (emailId.isEmpty()) {
                    Log.d("json아이디없음", ". key:signin_email_id  value: "+signin_email_id_value);
                } else {
                    Log.d("json아이디있음.", ". key:signin_email_id  value: "+signin_email_id_value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } // 쉐어드에서 이메일아이디 파싱하기.



        // on create 1. 게시글 카테고리 spinner 설정

        Spinner spinner = findViewById(R.id.board_category_spinner);
        // spinner item 리스트
        List<String> items = new ArrayList<>();
        items.add("게시글 유형");
        items.add("책 추천");
        items.add("잡담");
        items.add("가입인사");

        // 어댑터 설정
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.board_spinner_dropdown_item, items) {
            @Override
            public boolean isEnabled(int position) {
                return position !=0;
            }

            public View getDropDownView (int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Hint 스타일 적용
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
            };

        adapter.setDropDownViewResource(R.layout.board_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    // 첫 번째 항목이 아닌 다른 항목이 선택된 경우
                    selected_board_category = parent.getItemAtPosition(position).toString();
                    Log.d("게시글 작성 create activity", "선택된 카테고리:  " + selected_board_category);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // on create 2. 게시글 저장 버튼
        // user number 가 null 인지 확인 후, null이 아닐때 저장 액티비티로 넘어가기.
        board_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_number!=null) {
                    Log.d("저장버튼누름! 게시글 작성 activity", "유저넘버존재함 :  " + user_number);

                    String title_txt = board_title.getText().toString();
                    String content_txt = board_content.getText().toString();

                    if(selected_board_category!=null && title_txt !=null && content_txt !=null) {
                        Log.d("게시글 작성 내용!!", "제목 :  " + title_txt + ", 내용: " + content_txt + ", 게시글 유형: " + selected_board_category);

                        // 이미지 array list에 담긴 값 출력해보기
                       for(int i=0; i<uriList.size(); i++) {
                           Log.d("저장할 게시글 이미지 목록!! ", "uri " + i+"번째사진: "+ uriList.get(i));
                       }


                       // 저장할 책 목록
                        for (int i=0; i<bookList.size(); i++) {
                            Log.d("저장할 게시글 책 목록!! ", "책 " + i+"번째제목: "+ bookList.get(i).getTitle());
                        }

                        // board insert 레트로핏 호출
                         board_insert(user_number, title_txt, content_txt, selected_board_category, bookList);
                        // 위 게시글 제목,내용,카테고리에 대한 insert문이 성공했을 경우, on response에다가 board img insert 레트로핏 호출 !!

                        // insert 가 완료 되었을때 on response 에서 uploadImage 메소드를 출력하기 uri list를 for문으로 출력하기.


                    } else if (title_txt  == null) {
                        Log.d("게시글 작성 내용!!", "제목 :  " + title_txt + ", 내용: " + content_txt + ", 게시글 유형: " + selected_board_category);
                        Toast.makeText(BoardCreateActivity.this, "게시글 제목을 입력해주세요", Toast.LENGTH_SHORT).show();

                    } else if (content_txt == null) {
                        Toast.makeText(BoardCreateActivity.this, "게시글 내용을 입력해주세요", Toast.LENGTH_SHORT).show();

                    } else if (selected_board_category == null) {
                        Toast.makeText(BoardCreateActivity.this, "게시글 유형을 입력해주세요", Toast.LENGTH_SHORT).show();

                    }

                    //각각 입력한 값 가져와서, 로그 찍어보고 있으면, 레트로핏 만들기. !! insert니까 그냥 code랑 message만 있어도 될듯
                    // 입력값이 null이 아닐때 !!! 레트로핏 호출
                }
            }
        });


        // on create 3-1. 책 추가
        board_books_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("책검색버튼 누름!! ", "책 추가 !!  " );

                // intent 로 넘기기. board의 책 검색 화면으로.
                Intent intent = new Intent(BoardCreateActivity.this, BoardBookSearchActivity.class);
                bookSearchLauncher.launch(intent);
            }
        });


        // on create 3-2.책 검색 액티비티로부터 선택한 책 정보를 받아옴.
        bookSearchLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == REQUEST_CODE_BOOK_SEARCH ) {
                        Intent data = result.getData(); // get data 도 인텐트에서 기본적으로 제공하는 것임.
                        if (data != null) {
                            String bookTitle = data.getStringExtra("title");
                            String bookAuthor = data.getStringExtra("author");
                            String bookCover = data.getStringExtra("cover");
                            String description = data.getStringExtra("description");

                            bookList.add(new SearchBookData(bookTitle, bookAuthor,description, null, null, bookCover, null ));
                            updateBookRecyclerView();
                            // 여기서 책 정보 어댑터 가져와서 리사이클러뷰 add 해주기. 위해 list 에만 add한다. 자동으로 리사이클러뷰에 반영됨 !!
                            // 책을 하나씩만 선택할 수 있기 때문에 for문 안돌려도됨 헤헷 그냥 어댑터.add (new data ) 하면됨 !!!
                            Log.i("책검색 정보 create로넘어옴 !! ", "책제목: " + bookTitle);

                        }
                    }
                }
        );

        // on create 4. 이미지 추가 버튼 클릭 리스너 설정
        board_image_btn.setOnClickListener(v -> {
            Log.d("게시글 이미지 추가 버튼 누름", "");
            showImageSelectionDialog(); // 다이얼로그 호출
        });

    } // on create 끝


    // 사진 메소드0- 카메라? 갤러리? 다이얼로그 호출 메소드
    private void showImageSelectionDialog() {
        String[] options = {"카메라로 촬영", "갤러리에서 선택"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("이미지 추가");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // 카메라 선택
                Log.d("게시글 이미지 추가 버튼 누름", "");
                checkCameraPermission();
//                openCamera();
            } else if (which == 1) {
                // 갤러리 선택
                openGallery();
            }
        });
        builder.show();
    }


    // ============================== 카메라 사진 선택 로직 ==============================

    // 카메라 1.카메라 열기) open camera

    private Uri imageUri; // 카메라 촬영 이미지 URI
    private void openCamera() {
        Log.d("openCamera", "들어옴 !!  ");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            Log.d("openCamera", "null이 아님 !!  ");
            File photoFile = null;
            try { // 파일 쓰기를 할때는 항상 try catch 문을 적어야함 !
                photoFile = createImageFile_camera();
            } catch (IOException io) {
                io.printStackTrace();
            }
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityResult.launch(intent);
            }
        } else {
            Log.d("openCamera", "null");
            Log.d("openCamera", "Intent: " + intent.resolveActivity(getPackageManager()));
        }
    }

    // 사진 메소드3 - 사진 선택하고 나왔을때 result
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //원하는 기능 작성
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
                        ExifInterface exif = null;
                        // 여기서. 사진을 선택하면 어떻게 할건지에 대해.
                        // 선택할지 안할지를 여기서 고르는건가 ??
                        // 이곳에 오기 전에 해결됨..
                        // 그리고 목록에 넣어주깅. 갤러리 사진 처리하는 로직 가져오기.
                        uriList.add(new ImageUploadResponse(null, photoUri, 0, 0));
                        updateImgRecyclerView();


                        try {
                            exif = new ExifInterface(imageFilePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        int exifOrientation;
                        int exifDegree;

                        if (exif != null) {
                            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            exifDegree = exifOrientationToDegress(exifOrientation);
                        } else {
                            exifDegree = 0;
                        }
                    }
                }
            });

    private File createImageFile_camera() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }


    // method 3. 카메라에서 사진을 찍을 때 화면을 돌리는 것에 대한 이미지를 회전시켜주는 것
    // 이게 만약에 없으면 어떻게 되는데요 ?
    private int exifOrientationToDegress(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if ((exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)) {
            return 270;
        }
        return 0;
    }


    // method 4. 로테이트. 이게 뭐람.
    private Bitmap rotate(Bitmap bitmap, int exifDegree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(exifDegree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    // method 9.
    // 카메라 접근을 위한 런타임 권한
    // 카메라 권한 요청 메소드
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // 권한이 이미 허용됨
            Toast.makeText(this, "카메라 권한이 이미 허용되어 있습니다.", Toast.LENGTH_SHORT).show();
            openCamera();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // 추가 설명이 필요한 경우 (사용자가 이전에 거부했을 때)
            Toast.makeText(this, "카메라를 사용하려면 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            // 권한 요청
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // 권한 요청 런처 정의
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // 권한이 허용되었을 때 동작
                    Toast.makeText(this, "카메라 권한이 허용되었습니다!", Toast.LENGTH_SHORT).show();
                    openCamera();
                } else {
                    // 권한이 거부되었을 때 동작
                    Toast.makeText(this, "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                }
            });






    // ============================== 갤러리 사진 선택 로직 ==============================
    // 사진 메소드1-갤러리 열기) open gallery
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // action pick 은 사용자가 갤러리에서 이미지를 선택하도록 요청하는 부분이야
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // EXTRA_ALLOW_MULTIPLE 는 여러 이미지를 선택할 수있도록 설정해주는거야.
        imageSelectionLauncher.launch(intent); // 선택에 대한 결과를 imageSelectionLauncher 로 전달하는거임.
    }


    // method2. Initialize the image selection launcher
    private ActivityResultLauncher<Intent> imageSelectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    // 카메라 촬영 결과 처리
//                    if (imageUri != null) {
//                        Log.e("카메라 이미지 URI", imageUri.toString());
//                        if (uriList.size() < 10) {
//                            uriList.add(new ImageUploadResponse(null, imageUri, 0, 0));
//                            updateImgRecyclerView();
//                        } else {
//                            Toast.makeText(getApplicationContext(), "사진은 총 10장까지 추가 가능합니다.", Toast.LENGTH_LONG).show();
//                        }
//                    }

                    // 갤러리 선택 결과 처리
                    if (data == null) {
                        Toast.makeText(getApplicationContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        ClipData clipData = data.getClipData();
                        if (clipData != null) {
                            Log.e("사진 여러개고름(클립데이터): ", String.valueOf(clipData.getItemCount()));
                            if (clipData.getItemCount() > 10) {
                                Toast.makeText(getApplicationContext(), "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
                            } else {
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    if (uriList.size() < 10) {
                                        Uri imgUri = clipData.getItemAt(i).getUri();
                                        Log.e(i + "번째 사진 uri: ", imgUri.toString());
                                        uriList.add(new ImageUploadResponse(null, imgUri, 0, 0));
                                    } else {
                                        Toast.makeText(getApplicationContext(), "사진은 총 10장까지 추가 가능 합니다.", Toast.LENGTH_LONG).show();
                                        break;
                                    }
                                }
                                updateImgRecyclerView();
                            }
                        } else {
                            Uri imgUri = data.getData();
                            if (imgUri != null) {
                                Log.e("사진 고름!! 1개고름", "이미지 uri: " + imgUri.toString());
                                if (uriList.size() < 10) {
                                    uriList.add(new ImageUploadResponse(null, imgUri, 0, 0));
                                    updateImgRecyclerView();
                                } else {
                                    Toast.makeText(getApplicationContext(), "사진은 총 10장까지 추가 가능 합니다.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                }
            }
    );


    // method 0-1. 이미지 리사이클러뷰
    private void updateImgRecyclerView() {
        img_recyclerView.setVisibility(View.VISIBLE);
        boardImageAdapter = new BoardImageAdapter(uriList, getApplicationContext());
        img_recyclerView.setAdapter(boardImageAdapter);
        img_recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
    }


    // method 0-2. 책 리사이클러뷰
    private void updateBookRecyclerView() {
        book_recyclerview.setVisibility(View.VISIBLE);
        boardBookAdapter = new BoardBookAdapter(bookList,BoardCreateActivity.this);
        book_recyclerview.setAdapter(boardBookAdapter);
        book_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }



    // method 1. 유저 이메일아이디를 가지고 유저의 number 가져오기
    public void get_userNumber (String user_emailid) {
        Log.i("게시글create 유저넘버가져오기", "유저의 emailId: " + user_emailid);
        service.user_emailId(user_emailid).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("게시글create-유저 number 체크 response 진입", "onResponse: 응답메시지: " + result.getMessage()); // echo로 보내주는 메시지.
                Log.e("게시글create-유저 number 체크 response 진입", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.

                if(result.getCode() == 200 ) {
                    Log.e("게시글create 유저 number 체크성공 / code==200!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                    user_number = result.getUser_number();
                } else {
                    Log.e("게시글create php에서 뭔가 잘못됨. / code==400!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Log.e("메인-유저넘버찾기 통신 failed", "실패원인: " +throwable.getMessage());
            }
        });
    }


    // method 2. 게시글 작성 메소드 !!
    public void board_insert (String user_number, String board_title, String board_content, String board_category, List<SearchBookData> bookList) {
        Log.i("게시판 저장 레트로핏 실행 ", "유저번호: " + user_number + ",제목: " + board_title + ", 내용: " + board_content + ", 카테고리: " + board_category );

        // board insert 라는 자바의 메소드에서는 이렇게 List 를 매개변수로 넘기고 레트로핏
        // 실행하기 전에 !! string 으로 변경해줌.

        String booksJson = convertBookToJson(bookList);
        Log.i("게시판 저장 레트로핏 실행 ", "책 목록 gson 사용한거 목록: " + booksJson );


        service.board_insert(user_number, board_title, board_content, board_category, booksJson).enqueue(new Callback<BoardResponse>() {
            @Override
            public void onResponse(Call<BoardResponse> call, Response<BoardResponse> response) {
                Log.e("게시판 저장 통신성공!! 서버가 보내는 코드", "code: " + response.code()); //서버가 보내는 http 통신 응답코드
                BoardResponse result = response.body();

                if(result.getCode() == 200 ) {
                    Log.e("게시판저장-게시판번호", "board_number: " + result.getBoard_number());
                    int board_number = result.getBoard_number();
                    // 이미지 업로드 !!
                    // 게시글 수정할때에는 각각 분기처리해야겠네.. 그럼 사진 하나마다 지금 request 요청하고 있었던거니..
                    for (ImageUploadResponse imageUri : uriList) {
                        uploadImage(imageUri.getImageUri(), board_number);
                    }

                    //게시판 저장
                    Toast.makeText(BoardCreateActivity.this, "게시판 글 작성이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    // code: 200 일때 내 서재 프래그먼트로 이동 !! 뒤에 있는 스택 지우고가기
                    Intent intent = new Intent(BoardCreateActivity.this, MainActivity.class);
                    intent.putExtra("category", selected_board_category);
                    intent.putExtra("fragment", "board");
                    intent.putExtra("user_emailid", emailId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 위 플래그를 설정하면 스택에 있는 기존 액티비티가 전부 제거됨.
                    startActivity(intent);
                    finish();

                } else {  // 코드 경우의 수는 200,400 두개임. 현재 400일 경우 = 실패일경우
                    Toast.makeText(BoardCreateActivity.this, "게시글 저장을 실패하였습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BoardResponse> call, Throwable throwable) {
                throwable.getMessage();
                Log.e("게시판 저장  통신 실패", "onFailure:실패한이유: " + throwable.getMessage());
            }
        });
    }  // method 2 끝


    // method 3. 이미지 서버에 업로드하는 메소드 - url 로 변환함. 
    private void uploadImage (Uri imageUri, int board_number) {
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
            RequestBody boardNumber = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(board_number));
            RequestBody userNumber = RequestBody.create(MediaType.parse("text/plain"), user_number);

            // 서버의 이미지를 저장하기 위해 서버로 전달하는 http 통신 요청문의 body part
            Log.e("서버 이미지 저장 요청body ", "서버 이미지저장 요청 body: " + body.toString());
            Log.e("서버 이미지 저장  ", "서버 이미지저장 요청 body: " + body.headers().toString());
            Log.d("RequestBody 요청body", requestFile.toString()); // requestFile은 RequestBody 객체
            Log.e("서버 요청body boardNumber ", "boardNumber: " + boardNumber.toString());

            // 기존의 Retrofit 클라이언트를 사용해 서버로 이미지를 업로드
            Call<ImageUploadResponse> call = service.uploadImage(body, boardNumber, userNumber);
            call.enqueue(new Callback<ImageUploadResponse>() {
                @Override
                public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                    if (response.isSuccessful()) {
                        ImageUploadResponse result = response.body();

                        String imgURL = response.body().toString();
                        if(result.isSuccess()) {
                            Log.e("이미지 서버저장한 boolean","트루임" );
                        } else {
                            Log.e("이미지 서버저장한 boolean","폴스임" );
                        }
                        Log.e("이미지 서버저장한 메시지: ", result.getMessage() );
                        Log.e("이미지 서버저장한 URL: ", result.getImageUrl() );
                    }
                }
                @Override
                public void onFailure(Call<ImageUploadResponse> call, Throwable throwable) {
                    Log.e("이미지 서버에 업로드 에러남!! ", throwable.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Uri를 실제 경로 파일 시스템 경로로 변환하는 메소드 - uri는 그냥 안드로이드에서 이미지, 동영상, 리소스 등에 접근하기 위해 사용하는 참조임.
    // 시스템이 찾아갈 수 있는 주소를 말하는 것은 아님.



    // method 4. 이미지 확장자 반환하는 메서드
    private String getMimeType(File file) {

        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getPath());
        if(extension !=null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        Log.e("현재이미지의 타입은? ", "type: " + type);
        return type;
    }

    // method 5. 사진 파일의 확장자를 가져오는 메소드 - 서버로 넘기는 이미지의 mime 타입을 설정하는 getMimeType 메소드 내부에서 호출됨.
    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return ""; // Handle the exception
        }
    }


    // method 6. 파일 uri 를 가지고 파일을 생성해서 업로드하기. input stream으로 파일을 만들고, content resolver 사용하기.
    private File createFileFromInputStream (InputStream inputStream, String fileName) {
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

    // method 6-1) input stream으로 원래 파일의 이름을 얻는 메서드
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


    // method 7. 사용자에게 미디어  (갤러리) 접근을 위한 권한 요청
    // 7-1)
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) { // 클래스에 만들어놓은것.
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    } // all permission granted 끝


    // 7-3 퍼미션 허가 결과
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) { // 갤러리 접근 권한의 경우
            if (allPermissionsGranted()) {
                openGallery();
            } else {
                Toast.makeText(this, "갤러리에 접근하기 위해서는 권한이 필요합니다!", Toast.LENGTH_SHORT).show();
            }
        }
    } // onRequestPermissionsResult 끝



    // method 8. 책 목록 book list (list) 를 json 문자열로 변환하는 메소드
    private String convertBookToJson (List<SearchBookData> bookList) {
        Gson gson = new Gson();
        return gson.toJson(bookList);
    }





} // 클래스 끝
