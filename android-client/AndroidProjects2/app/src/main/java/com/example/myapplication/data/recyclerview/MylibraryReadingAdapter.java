package com.example.myapplication.data.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.layout.MyLibraryViewActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MylibraryReadingAdapter extends RecyclerView.Adapter<MylibraryReadingAdapter.MylibraryReadingViewHolder> {

    // 내 서재 <전체 보기>에 대한  adapter 구성순서
    // 1. 생성자
    // 2. view holder
    // 3. on create view holder - 레이아웃 setting
    // 4. bind view holder


    // context
    private Activity mContext;

    // 쉐어드 프리퍼런스
    PreferenceManager pref; // 쉐어드 프리퍼런스 - 로그인 유지용
    String key = "signin_email_id";
    String current_login_memberInfo; // 쉐어드에 저장된 이메일/아이디, 닉네임 키값쌍.
    String emailId ; // 이메일/아이디만 파싱한 값.

    private ArrayList<MylibraryReadingData> readingData = new ArrayList<>();


    // 1. 생성자
    public MylibraryReadingAdapter(Activity context) {mContext = context;}


    // 2. 뷰 홀더 클래스
    public class MylibraryReadingViewHolder extends RecyclerView.ViewHolder {

        // view
        public ImageView book_cover_img;
        public TextView book_title_tv, book_author_tv;

        // 읽고 있는 책
        public TextView reading_read_page, reading_started;


        // 뷰 홀더의 생성자
        public MylibraryReadingViewHolder (View view, int viewType) {
            super(view);

            book_cover_img = view.findViewById(R.id.book_cover_img);
            book_title_tv = view.findViewById(R.id.book_title_tv);
            book_author_tv = view.findViewById(R.id.book_author_tv);

            // 읽고 있는 책
            reading_read_page = view.findViewById(R.id.reading_read_page);
            reading_started = view.findViewById(R.id.reading_list_started);
        }


        // 아이템 세팅을 위한 메소드
        public void setItem (MylibraryReadingData item) {

            // 책 제목, 작가
            book_title_tv.setText(item.getBookTitle());
            book_author_tv.setText(item.getBookAuthor());

            // 책 커버
            Glide.with(itemView.getContext())
                    .load(item.getBookCover())
                    .into(book_cover_img);


            // 읽고 있는 책
            reading_read_page.setText(item.getReadPage());
            reading_started.setText(item.getStarted());

        } // set item 끝
    } //  2. 뷰 홀더 view holder 끝


    // 3. On create view holder
    // 하나의 아이템 마다 호출됨.

    @NonNull
    @Override
    public MylibraryReadingAdapter.MylibraryReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();

        // 반환할 뷰
        View view;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 쉐어드 값 가져오기
        // 쉐어드 - 로그인 정보 확인.
        pref = new PreferenceManager();
        current_login_memberInfo =   pref.getString(context, key);
        Log.d("my library whole 어댑터", "로그인 회원정보from 쉐어드:  " + current_login_memberInfo);

        try {
            JSONObject jsonObject = new JSONObject(current_login_memberInfo);
            emailId = jsonObject.optString("emailid", "");
            Log.d("my library whole 어댑터", "로그인 회원 이메일: " + emailId);
        } catch (JSONException e) {
            e.printStackTrace();
            emailId = ""; // 기본값 설정
        }


        // 읽고 있는 책 레이아웃 연결
        view = inflater.inflate(R.layout.itemlist_book_reading, parent, false);
        MylibraryReadingAdapter.MylibraryReadingViewHolder holder = new MylibraryReadingAdapter.MylibraryReadingViewHolder(view, viewType);
        return holder;
    } // onCreateViewHolder 끝


    // 4. bind view holder
    @Override
    public void onBindViewHolder (@NonNull MylibraryReadingAdapter.MylibraryReadingViewHolder holder, final int position) {
        MylibraryReadingData item = readingData.get(position);
        holder.setItem(item);


        // 아이템 상세보기
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("내 서재<읽은 책> 상세보기 클릭 시도", "상세보기진입 시도 ");

                if(emailId.equals("")) { // 로그인한 상태인지 체크
                    Toast.makeText(mContext, " 상세페이지 진입 실패. 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(mContext, MyLibraryViewActivity.class);
                    Log.d("내 서재<읽은 책> 상세보기 클릭 intent값", "책번호: " + item.getBookNumber() + ", 타입: "+ item.getType() + ", 서재번호: " + item.getMylibrary_number());

                    String booknumber= String.valueOf(item.getBookNumber());
                    String type = String.valueOf(item.getType());
                    String librarynumber = String.valueOf(item.getMylibrary_number());
                    String lib_user_number = String.valueOf(item.getLib_user_number());

                    intent.putExtra("book_number", booknumber); // 책 번호
                    intent.putExtra("type", type); // 내 서재 타입
                    intent.putExtra("mylibrary_number", librarynumber); // 내 서재 번호
                    intent.putExtra("lib_user_number", lib_user_number); // 내 서재 작성자
                    mContext.startActivity(intent);
                }
            }
        });
    }

    // 5. get item count 필수
    @Override
    public int getItemCount() {
        return readingData.size();
    }


    // 6. add item
    public void addItem (MylibraryReadingData readingItem) {
        readingData.add(readingItem);
        notifyDataSetChanged();
        // 시간순 정렬??? 어떻게 해야할지.
    }


    //7 . clear
    // 아이템 삭제
    public void clearItem() {
        readingData.clear();
        notifyDataSetChanged();
    }





}
