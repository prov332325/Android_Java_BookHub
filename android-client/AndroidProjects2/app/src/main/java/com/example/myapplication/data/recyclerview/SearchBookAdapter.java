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
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.AladinResponse;
import com.example.myapplication.layout.BookViewActivity;
import com.example.myapplication.layout.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.SearchBookViewHolder> {

    // search book adapter 구성순서
    // 1. 생성자
    // 2. view holder
    // 3. on create view holder - 레이아웃 setting
    // 4. bind view holder



    // 쉐어드 프리퍼런스
    PreferenceManager pref; // 쉐어드 프리퍼런스 - 로그인 유지용
    String key = "signin_email_id";
    String current_login_memberInfo; // 쉐어드에 저장된 이메일/아이디, 닉네임 키값쌍.
    String emailId ; // 이메일/아이디만 파싱한 값.



    private Context mContext;

    private List<SearchBookData> bookList = new ArrayList<SearchBookData>();
    // list 와 array list


    // ========================= interface 시작 =========================
    // 클릭 이벤트를 각각 board book search, book search 액티비티에서 다르게 처리 하기 위해
    // 인터페이스에 클릭 이벤트 를 정의한다. 각각의 액티비티에서 사용할 예정.
    // 인터페이스 구현하고 바로 import 해줄 수 있음.
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    // 각 액티비티에서 구현해야하는 인터페이스 메소드
    public void setOnItemClickListener (OnItemClickListener listener) {
        this.listener = listener;
    }



    // ========================= interface 끝 =========================


    // 1. 생성자
    public SearchBookAdapter(Activity context) { mContext = context; } // 기본생성자

    public SearchBookAdapter(List<SearchBookData> list, Context context) {
        bookList = list ;
        mContext = context;
    }




    // 2. 뷰홀더 먼저
    public class SearchBookViewHolder extends RecyclerView.ViewHolder {
        public ImageView book_cover_img;
        public TextView book_title_tv, book_author_tv, book_description_tv;

        public SearchBookViewHolder(View view) {
            super(view);
            book_cover_img = view.findViewById(R.id.book_cover);
            book_title_tv = view.findViewById(R.id.book_title);
            book_author_tv = view.findViewById(R.id.book_author);
            book_description_tv =  view.findViewById(R.id.book_description);
        }

        public void setItem (SearchBookData item) {
            //book_cover_img.setImageResource(item.getCover());
            book_title_tv.setText(item.getTitle());
            book_author_tv.setText(item.getAuthor());
            book_description_tv.setText(item.getDescription());

            // Glide 추가
            Glide.with(itemView.getContext()) // 현재 View의 context
                    .load(item.getCover())  // 서버 사진 이미지
                    .into(book_cover_img); // Imgage View에 표시해주기.

        }
    } // 뷰홀더




    // 3. on create view holder
    @NonNull
    @Override
    public SearchBookAdapter.SearchBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        // 리스트뷰의 on create 같은 것
        // 쉐어드
        // 쉐어드 - 로그인 정보 확인.
        pref = new PreferenceManager();
        current_login_memberInfo =   pref.getString(context, key);
        Log.d("내서재저장oncreate 로그인회원", "로그인 회원정보from 쉐어드:  " + current_login_memberInfo);

        try {
            JSONObject jsonObject = new JSONObject(current_login_memberInfo);
            emailId = jsonObject.optString("emailid", "");
            Log.d("내서재저장oncreate 로그인회원", "로그인 회원 이메일: " + emailId);
        } catch (JSONException e) {
            e.printStackTrace();
            emailId = ""; // 기본값 설정
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.itemlist_searchbook, parent, false);
        SearchBookAdapter.SearchBookViewHolder holder = new SearchBookAdapter.SearchBookViewHolder(view);
        return holder;
    } // on create 같은거


    // 4. bind view holder
    @Override
    public void onBindViewHolder (@NonNull SearchBookAdapter.SearchBookViewHolder holder, final int position) {
        // bh1. view에 아이템 setting 하기
        SearchBookData item = bookList.get(position);
        holder.setItem(item);



        // 여기서 클릭 이벤트에 분기 처리 해줌.


        // bh2. 책 정보 상세보기로 이동하기.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 각 액티비티에서 정의 내릴 인터페이스가 실현 되도록 함.
                if(listener !=null) {
                    listener.onItemClick(v, position);
                }
            }
        });// on click listener
    }

    // 필수 override
    @Override
    public int getItemCount() {
        return bookList.size();
    }


    // 아이템 추가
    public void addItem(SearchBookData bookitem) {
        Log.i("어댑터의 additem소환", "책 제목: " + bookitem.getTitle() + "책 작가: " + bookitem.getAuthor());
        bookList.add(bookitem);
        notifyDataSetChanged(); // 리사이클러뷰 갱신
    }

    // 아이템 삭제
    public void clearItem() {bookList.clear();}

    // 특정 위치의 아이템 가져오기
    public SearchBookData getItem (int position) {
        return bookList.get(position);
    }



}


