package com.example.myapplication.data.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
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

public class MylibraryWholeAdapter extends RecyclerView.Adapter<MylibraryWholeAdapter.MylibraryWholeViewHoler> {

    // 내 서재 <전체 보기>에 대한  adapter 구성순서
    // 1. 생성자
    // 2. view holder
    // 3. on create view holder - 레이아웃 setting
    // 4. bind view holder - 상세보기 클릭 이벤트 구현하는 곳.

    // context
    private Activity mContext;

    // 쉐어드 프리퍼런스
    PreferenceManager pref; // 쉐어드 프리퍼런스 - 로그인 유지용
    String key = "signin_email_id";
    String current_login_memberInfo; // 쉐어드에 저장된 이메일/아이디, 닉네임 키값쌍.
    String emailId ; // 이메일/아이디만 파싱한 값.


    // 뷰 타입
    private static final int TYPE_ALREADY = 1;
    private static final int TYPE_READING = 2;
    private static final int TYPE_WANT = 3;


    private ArrayList<MylibraryAlreadyData> already_arrayList = new ArrayList<>();
    private ArrayList<MylibraryReadingData> reading_arrayList = new ArrayList<>();
    private ArrayList<MylibraryWantData> want_arrayList = new ArrayList<>();

    // 합친 리스트
    private ArrayList<MylibraryBookItemData> bookItemData = new ArrayList<>();


    // 1. 생성자
    public MylibraryWholeAdapter(Activity context) {mContext = context;}



    // 2. 뷰 홀더
    public class MylibraryWholeViewHoler extends RecyclerView.ViewHolder {

        // 책 정보를 위한 view - 책 표지, 제목, 작가
        // 초기화 안한거 다 초기화 해놓기.


        public ImageView book_cover_img;
        public TextView book_title_tv, book_author_tv;

        // 읽은 책
        public RatingBar already_rating;
        public TextView already_started, already_finished;

        // 읽고 있는 책
        public TextView reading_read_page, reading_started;

        // 읽고 싶은 책
        public RatingBar want_rating;
        public TextView want_preview;


        // 각 아이템마다 분기처리 해줘야함.

        public MylibraryWholeViewHoler (View view, int viewType) {
            super(view);

            // 공통된 책 정보
            book_cover_img = view.findViewById(R.id.book_cover_img);
            book_title_tv = view.findViewById(R.id.book_title_tv);
            book_author_tv = view.findViewById(R.id.book_author_tv);

            if (viewType == TYPE_ALREADY){
                // 읽은 책
                already_rating = view.findViewById(R.id.already_list_rating);
                already_started = view.findViewById(R.id.already_list_started);
                already_finished = view.findViewById(R.id.already_list_finished);

            } else if (viewType == TYPE_READING) {
                // 읽고 있는 책
                reading_read_page = view.findViewById(R.id.reading_read_page);
                reading_started = view.findViewById(R.id.reading_list_started);

            } else if (viewType == TYPE_WANT) {
                // 읽고 싶은 책
                want_rating = view.findViewById(R.id.want_list_rating);
                want_preview = view.findViewById(R.id.want_list_preview);
            }
        }

        public void setItem (MylibraryBookItemData item, int viewType) {

            // 아이템 세팅에 대한 분기처리.
            // 책 제목, 작가
            book_title_tv.setText(item.getBookTitle());
            book_author_tv.setText(item.getBookAuthor());


            Log.e("책저장타입!! setitem ", "setitem: " + item.getSaveType() + ", 이미지: " + "http://3.39.255.234/php/img/"+ item.getBookCover());
            // 책 커버
            if(item.getSaveType() != null && item.getSaveType().equals("SELF") && item.getBookCover()!=null) {

                Glide.with(itemView.getContext())
                        .load("http://3.39.255.234/php/img/"+item.getBookCover())
                        .into(book_cover_img);
            } else {
                // 책 커버
                Glide.with(itemView.getContext())
                        .load(item.getBookCover())
                        .into(book_cover_img);

            }



            if(viewType == TYPE_ALREADY) {
                // 읽은 책
                //rating
                float itemRating = 0.0f;
                try {
                    itemRating = Float.parseFloat(item.getRating());
                } catch (NumberFormatException e) {
                    Log.e("내서재 어댑터!! 평점 float 변환", "읽은책 평점 float 오류남 이유:  " + e.getMessage());
                }
                already_rating.setRating(itemRating);
                already_started.setText(item.getStarted());
                already_finished.setText(item.getFinished());

            } else if (viewType == TYPE_READING) {
                // 읽고 있는 책
                reading_read_page.setText(item.getReadPage());
                reading_started.setText(item.getStarted());

            } else if (viewType == TYPE_WANT) {
                // 읽고 싶은 책
                float wantRating = 0.0f;
                try {
                    wantRating = Float.parseFloat(item.getRating());
                } catch (NumberFormatException e) {
                    Log.e("내서재 어댑터!! 평점 float 변환", "읽고 싶은 책 평점 float 오류남 이유: " + e.getMessage());
                }
                want_rating.setRating(wantRating);
                want_preview.setText(item.getPreview());

            }
        }
    } // 뷰 홀더


    //  ++++ 뷰타입 가져오기
    @Override
    public int getItemViewType(int position) {
      return bookItemData.get(position).getType();
    }

    //  3. on create view holder
    // 하나의 뷰가 생성될때 마다 호출됨 !! 아이템 하나하나 마다 호출됨
    // 그렇기 때문에 item 레이아웃 분기처리를 여기서 해줘야 한다. 각 아이템 특성에 맞는 레이아웃과 연결해줘야 함.
    @NonNull
    @Override
    public MylibraryWholeAdapter.MylibraryWholeViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // 리사이클러뷰의 on create 같은 것
        Context context = parent.getContext();

        // 반환할 뷰는 ??
        View view;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        // 여기서 쉐어드의 값 가져올 수 있음.
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

        // 뷰타입에 따른 반환뷰 분기처리
        switch (viewType) {
            case TYPE_ALREADY:
                view = inflater.inflate(R.layout.itemlist_book_already, parent, false);
                break;
            case TYPE_READING:
                view = inflater.inflate(R.layout.itemlist_book_reading, parent, false);
                break;
            case TYPE_WANT:
                view = inflater.inflate(R.layout.itemlist_book_want, parent, false);
                break;
            default:
                throw new IllegalArgumentException("Invalid view type");
        }

        MylibraryWholeAdapter.MylibraryWholeViewHoler holder = new MylibraryWholeAdapter.MylibraryWholeViewHoler(view, viewType);
        return holder;
    } // onCreateViewHolder 끝




    // 4. bind view holder
    @Override
    public void onBindViewHolder (@NonNull MylibraryWholeAdapter.MylibraryWholeViewHoler holder, final int position) {
        // 1. 아이템 세팅 하기
        MylibraryBookItemData item = bookItemData.get(position);
        holder.setItem(item, getItemViewType(position));


        // 아이템 상세보기
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("내 서재<전체보기> 상세보기 클릭 시도", "상세보기진입 시도 ");

                if(emailId.equals("")) { // 로그인한 상태인지 체크
                    Toast.makeText(mContext, " 상세페이지 진입 실패. 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(mContext, MyLibraryViewActivity.class);
                    Log.d("내 서재<전체보기> 상세보기 클릭 intent값", "책번호: " + item.getBookNumber() + ", 타입: "+ item.getType() + ", 서재번호: " + item.getMylibrary_number());

                    String booknumber= String.valueOf(item.getBookNumber());
                    String type = String.valueOf(item.getType());
                    String librarynumber = String.valueOf(item.getMylibrary_number());
                    String lib_user_number = String.valueOf(item.getLib_user_number());
                    intent.putExtra("book_number", booknumber); // 책 번호
                    intent.putExtra("type", type); // 내 서재 타입
                    intent.putExtra("mylibrary_number", librarynumber); // 내 서재 번호
                    intent.putExtra("lib_user_number", lib_user_number); // 서재 작성자.
                    Log.d("내 서재<전체보기> 상세보기 클릭 intent값", "책번호: " + item.getBookNumber() + ", 타입: "+ item.getType() + ", 서재번호: " + item.getMylibrary_number() + ", 작성자: " + item.getLib_user_number());

                    mContext.startActivity(intent);

                }
            }
        });
    }


    // 5. get item count 필수
    @Override
    public int getItemCount() {
        return bookItemData.size();
    }


    // 6. add item
    public void addItem (MylibraryBookItemData wholeitem) {
        bookItemData.add(wholeitem);
        notifyDataSetChanged();
        // 시간순 정렬 어케 함?
    }


    // 아이템 삭제
    public void clearItem() {
        bookItemData.clear();
        notifyDataSetChanged();
    }




}
