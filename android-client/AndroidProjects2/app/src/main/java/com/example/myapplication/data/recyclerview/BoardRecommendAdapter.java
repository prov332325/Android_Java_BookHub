package com.example.myapplication.data.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.layout.BoardViewActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class BoardRecommendAdapter extends RecyclerView.Adapter<BoardRecommendAdapter.RecommendViewHolder>  {
    // 1. 생성자
    // 2. view holder
    // 3. on create view holder - 레이아웃 setting
    // 4. bind view holder

    // context
    private Activity mContext;
    private ArrayList<BoardData> boardData = new ArrayList<>();

    // 쉐어드 프리퍼런스
    PreferenceManager pref; // 쉐어드 프리퍼런스 - 로그인 유지용
    String key = "signin_email_id";
    String current_login_memberInfo; // 쉐어드에 저장된 이메일/아이디, 닉네임 키값쌍.
    String emailId ; // 이메일/아이디만 파싱한 값.


    // 1. 생성자
    public BoardRecommendAdapter(Activity context) {mContext = context;}


    // 2. 뷰홀더 클래스
    public class RecommendViewHolder extends RecyclerView.ViewHolder {


        // 아이템 view
        public TextView category, title, content, date; // 카테고리, 제목, 내용, 사진, 생성시간
        public  TextView like_cnt, comment_cnt ; // 좋아요, 댓글 수

        // public ImageView board_main_img;



        // 뷰홀더 생성자
        public RecommendViewHolder (@NonNull View itemView) {
            super(itemView);
            // 뷰 홀더에 필요한 아이템 데이터 각각 view에 연결해주기.
            category = itemView.findViewById(R.id.board_category);
            title = itemView.findViewById(R.id.board_title);
            content = itemView.findViewById(R.id.board_content);
            like_cnt = itemView.findViewById(R.id.board_like_cnt);
            comment_cnt = itemView.findViewById(R.id.board_comment_cnt);
            date = itemView.findViewById(R.id.board_date_text);
        }

        // 아이템 세팅을 위한 메소드
        public void setItem (BoardData item) {
            // 게시글 제목, 내용, 카테고리, 날짜
            title.setText(item.getTitle());
            content.setText(item.getContent());
            category.setText(item.getCategory());
            String beeddays =been_days(item.getDate());
            date.setText(beeddays +"일 전");


            // 좋아요 댓글 갯수
            like_cnt.setText("공감해요 " + item.getLike_cnt() + "개");
            comment_cnt.setText("댓글 " + item.getComment_cnt() + "개");



        } // set item 끝
    } // 뷰홀더 끝

    // 3. on create view holder
    // 아이템 하나하나마다 호출됨 !!!

    public BoardRecommendAdapter.RecommendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        // 쉐어드 값 가져오기. - 상세보기로 아이템 클릭할 때 들고 들어가야함 !!
        pref = new PreferenceManager();
        current_login_memberInfo =   pref.getString(context, key);
        Log.d("게시판 recommned 어댑터", "로그인 회원정보from 쉐어드:  " + current_login_memberInfo);

        // 이메일만 파싱해주기
        try {
            JSONObject jsonObject = new JSONObject(current_login_memberInfo);
            emailId = jsonObject.optString("emailid", "");
            Log.d("my library whole 어댑터", "로그인 회원 이메일: " + emailId);
        } catch (JSONException e) {
            e.printStackTrace();
            emailId = ""; // 기본값 설정
        }


        View view = inflater.inflate(R.layout.itemlist_board, parent, false);
        BoardRecommendAdapter.RecommendViewHolder holder = new BoardRecommendAdapter.RecommendViewHolder(view);
        return holder;
    } // on create view holder


    // 4. bind view holder
    @Override
    public void onBindViewHolder(@NonNull BoardRecommendAdapter.RecommendViewHolder holder, int position) {
        // 아이템 데이터 클래스
        BoardData item = boardData.get(position);
        // 바인드 뷰 홀더도 아이템 하나마다 호출되기 때문에 현재 아이템의 포지션을 가져오는 것임.
        holder.setItem(item);


        // 아이템 상세보기
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("게시판 <책 추천> 상세보기 클릭 시도", "상세보기진입 시도 ");
                // 로그인한 상태인지 체크하기
                if (emailId.equals("")) {
                    Toast.makeText(mContext, " 상세페이지 진입 실패. 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(mContext, BoardViewActivity.class);
                    Log.d("게시판 <책 추천>상세보기클릭", "게시판번호: " + item.getBoard_number() + ", 제목: "+ item.getTitle() + ", 카테고리: " + item.getCategory() + ",유저 이메일 아이디: " + emailId) ;
                    String boardNumber = String.valueOf(item.getBoard_number());
                    intent.putExtra("category", item.getCategory());
                    intent.putExtra("board_number", boardNumber);
                    String userNumber = String.valueOf(item.getUser_number());
                    intent.putExtra("user_number",userNumber);
                    mContext.startActivity(intent);
                    //여기서는 스택 삭제 안함.
                }
            }
        });



    }


    // 5. 필수 over ride 해야하는 메소드 !!! get item count !!
    @Override
    public int getItemCount() {
        return boardData.size();
    }

    // 6. add item 함수
    public void addItem (BoardData boarditem) {
        boardData.add(0,boarditem);
        notifyDataSetChanged();
    }


    //7 . clear
    // 아이템 삭제
    public void clearItem() {
        boardData.clear();
        notifyDataSetChanged();
    }


    // 8. 현재 시간, been days 구하기

    // 메소드 8. 오늘 날짜
    private String get_today () {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = new Date();
        return format.format(today);
    } // 메소드 8. 오늘 날짜 끝


    // 메소드 8-1) 날짜 며칠 됐는지 반환하기.
    private String been_days(String created_time) {

        // 날짜 포맷을 정해줌.
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        // 넘겨 받은 날짜를 포맷에 맞게 포매팅 한다음. Calender 객체에 넣어줌.
        try {
            Date started = format.parse(created_time); // 포맷팅
            Calendar started_calendar = Calendar.getInstance();
            started_calendar.setTime(started); // 포맷팅한 날짜를 캘린더 객체에 넣어줌.

            String today_date = get_today();
            Date today = format.parse(today_date);
            Calendar today_calendar = Calendar.getInstance();
            today_calendar.setTime(today);


            // 시간 정보는 비교에서 제외하기 위해 시, 분, 초를 0으로 설정
            today_calendar.set(Calendar.HOUR_OF_DAY, 0);
            today_calendar.set(Calendar.MINUTE, 0);
            today_calendar.set(Calendar.SECOND, 0);


            // 두개를 비교함. diffInDays
            long diffInMillis = today_calendar.getTimeInMillis() - started_calendar.getTimeInMillis(); // 오늘날짜에서, 전달받은 날짜 빼줌.
            long diffInDays = diffInMillis / (1000 * 60 * 60 * 24); // 날짜 day 로 변환해줌.
            return String.valueOf(diffInDays+1);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


    } // 메소드 8-1) 날짜 며칠 됐는지 반환하기.






} // 끝

