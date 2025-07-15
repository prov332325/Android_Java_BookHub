package com.example.myapplication.data.recyclerview;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.layout.BoardViewActivity;
import com.example.myapplication.layout.onLikeBtnChanged;
import com.example.myapplication.layout.onLikeBtnClick;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardWholeAdapter extends RecyclerView.Adapter<BoardWholeAdapter.ViewHolder> implements onLikeBtnClick, onLikeBtnChanged {


    // 1. 생성자
    // 2. view holder
    // 3. on create view holder - 레이아웃 setting
    // 4. bind view holder

    // context
    private Activity mContext;
    private ArrayList<BoardData> boardData = new ArrayList<>();




    // 좋아요 리스너
    private onLikeBtnClick mOnLikeListener;


    // 쉐어드 프리퍼런스
    PreferenceManager pref; // 쉐어드 프리퍼런스 - 로그인 유지용
    String key = "signin_email_id";
    String current_login_memberInfo; // 쉐어드에 저장된 이메일/아이디, 닉네임 키값쌍.
    String emailId ; // 이메일/아이디만 파싱한 값.



    //리사이클러뷰
    private RecyclerView mRecyclerView;




    // 1. 생성자
    public BoardWholeAdapter(Activity context, RecyclerView recyclerView, onLikeBtnClick OnLikeListener) {
        mContext = context;
        mRecyclerView = recyclerView;
        mOnLikeListener = OnLikeListener;
    }


    // 2. 뷰홀더 클래스
    public class ViewHolder extends RecyclerView.ViewHolder {

        // 뷰홀더(ViewHolder) 클래스 안에 구현된 메서드는 해당 뷰홀더가 관리하고 있는 특정 아이템 하나에만 초점을 맞춥니다.
        // 뷰홀더가 담당하는 특정 뷰와 데이터에 대해 작업을 수행가능.


        // 아이템 view
        public TextView category, title, content, date; // 카테고리, 제목, 내용, 사진, 생성시간
        public  TextView like_cnt, comment_cnt ; // 좋아요, 댓글 수
        public LinearLayout titleContentLinear;

        // public ImageView board_main_img;

        // 좋아요 버튼
        ImageView like_btn;



        // 뷰홀더 생성자
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 뷰 홀더에 필요한 아이템 데이터 각각 view에 연결해주기.
            category = itemView.findViewById(R.id.board_category);
            title = itemView.findViewById(R.id.board_title);
            content = itemView.findViewById(R.id.board_content);
            like_cnt = itemView.findViewById(R.id.board_like_cnt);
            comment_cnt = itemView.findViewById(R.id.board_comment_cnt);
            date = itemView.findViewById(R.id.board_date_text);
            titleContentLinear = itemView.findViewById(R.id.title_ccontent_linear);
            like_btn = itemView.findViewById(R.id.item_like_btn); // 좋아요 버튼 살리기
        }


        // 아이템 세팅을 위한 메소드
        public void setItem (BoardData item) {

            // 게시글 제목, 내용, 카테고리, 날짜
            title.setText(item.getTitle());
            content.setText(item.getContent());
            category.setText(item.getCategory());
            String beeddays =been_days(item.getDate());
            date.setText(beeddays +"일 전");

            // 좋아요 상태 !!!
            //
            if (item.getLike_or_not()==1) {
                like_btn.setImageResource(R.drawable.icon_filled_heart);
            }else {
                like_btn.setImageResource(R.drawable.icon_line_heart);
            }

            // 좋아요 댓글 갯수
            like_cnt.setText("공감해요 " + item.getLike_cnt() + "개");
            comment_cnt.setText("댓글 " + item.getComment_cnt() + "개");
        } // set item 끝


        // 좋아요 눌렀을때 ui 상태 변화에 대한 업데이트... 메소드.. 여기에 따로 메소드 추가한적 처음임.
        public void updateLikeStatus(String newStatus, int newBoardLikeCnt) {
            if ("clicked_like".equals(newStatus)) {
                like_btn.setImageResource(R.drawable.icon_filled_heart);

            } else if ("clicked_unlike".equals(newStatus)) {
                like_btn.setImageResource(R.drawable.icon_line_heart);
            }
            Log.d("좋아요클릭후ui변경", "공감해요 개수:  " + newBoardLikeCnt);

            like_cnt.setText("공감해요 " + newBoardLikeCnt + "개");
        }


    } // 뷰홀더 끝


    // 3. on create view holder
    // 아이템 하나하나마다 호출됨 !!!

    @NonNull
    @Override
    public BoardWholeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        // 쉐어드 값 가져오기. - 상세보기로 아이템 클릭할 때 들고 들어가야함 !! 아니약.
        pref = new PreferenceManager();
        current_login_memberInfo =   pref.getString(context, key);
        Log.d("게시판 whole 어댑터", "로그인 회원정보from 쉐어드:  " + current_login_memberInfo);

        // 이메일만 파싱해주기
        try {
            JSONObject jsonObject = new JSONObject(current_login_memberInfo);
            emailId = jsonObject.optString("emailid", "");
            Log.d("my library whole 어댑터", "로그인 회원 이메일: " + emailId);
        } catch (JSONException e) {
            e.printStackTrace();
            emailId = ""; // 기본값 설정
        }

        // 게시판 전체보기를 위한 아이템 레이아웃 연결하기. - 하나의 아이템 쓸거임!!
        View view = inflater.inflate(R.layout.itemlist_board, parent, false);
        BoardWholeAdapter.ViewHolder holder = new BoardWholeAdapter.ViewHolder(view);
        return holder;
    } // on create view holder 끝.


    // 4. bind view holder
    @Override
    public void onBindViewHolder(@NonNull BoardWholeAdapter.ViewHolder holder, int position) {
        // 아이템 데이터 클래스
        BoardData item = boardData.get(position);
        // 바인드 뷰 홀더도 아이템 하나마다 호출되기 때문에 현재 아이템의 포지션을 가져오는 것임.
        holder.setItem(item);


        // 좋아요 버튼 클릭 이벤트
        holder.like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 좋아요 클릭 처리
               // Toast.makeText(mContext, "프래그보드 좋아요 클릭함..", Toast.LENGTH_SHORT).show();

                Drawable currentDrawable = holder.like_btn.getDrawable();
                Drawable iconLineHeart = mContext.getResources().getDrawable(R.drawable.icon_line_heart);
             //   Drawable iconFilledHeart = mContext.getResources().getDrawable(R.drawable.icon_filled_heart);

                if(currentDrawable.getConstantState().equals(iconLineHeart.getConstantState())) { // 좋아요 누름
                    holder.like_btn.setTag("clicked_like");
                    // 태그도 보내기 - 좋아요를 눌렀을때.
                    if (mOnLikeListener != null) {
                        mOnLikeListener.onLikeBtnClicked(item.getBoard_number(), "clicked_like"); // 좋아요를 누름
                    }
                } else {
                    holder.like_btn.setTag("clicked_like");
                    // 태그도 보내기 - 좋아요를 눌렀을때.
                    if (mOnLikeListener != null) {
                        mOnLikeListener.onLikeBtnClicked(item.getBoard_number(), "clicked_unlike");// 좋아요 취소
                    }
                }

            }
        });


        // 아이템 상세보기 클릭 이벤트할 때, 카테고리랑, 게시판 번호
        // 아이템 상세보기
        holder.titleContentLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("게시판 <전체보기> 상세보기 클릭 시도", "상세보기진입 시도 ");
                // 로그인한 상태인지 체크하기
                if (emailId.equals("")) {
                    Toast.makeText(mContext, " 상세페이지 진입 실패. 로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(mContext, BoardViewActivity.class);
                    Log.d("게시판 <전체>상세보기클릭", "게시판번호: " + item.getBoard_number() + ", 제목: "+ item.getTitle() + ", 카테고리: " + item.getCategory() + ",유저 이메일 아이디: " + emailId + ", 글쓴이번호: " + item.getUser_number()) ;
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


        // 중요: 하위 뷰 클릭 이벤트가 상위 뷰에 의해 가려지지 않도록 방지
//        holder.like_cnt.setFocusableInTouchMode(true);
//        holder.like_cnt.setOnTouchListener((v, event) -> {
//            v.performClick();
//            return true; // 이벤트 소비
//        });
    }


    // 5. 필수 over ride 해야하는 메소드 !!! get item count !!
    @Override
    public int getItemCount() {
        return boardData.size();
    }

    // add item 함수
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



    // 게시글 좋아요 기능
    @Override
    public void onLikeBtnClicked(int board_number, String status_tag) {
        // 이거는 목록이 존재하는 화면 (프래그먼트, 액티비티) 에서 해줄거임
        // 프래그먼트 게시판, 프래그먼트 홈, 프래그먼트 마이프로필, 다른 사람 프로필 액티비티 에서.
    }

    // 게시글 좋아요 기능 2
    @Override
    public void onLikeBtnChanged(int board_number, String newStatus, int Like_cnt)  {
        // 빈 메소드 - board 목록 adapter (whole, recommend, welcome, chat) 에서 구현할거임.
        Toast.makeText(mContext, "onLikeBtnChanged 진입" + newStatus, Toast.LENGTH_SHORT).show();
        // new status에 따라서,, 좋아요 바꿔줄거임... 후 ^^
        // 24 12 19 기준 프래그먼트 게시판에서만 구현함.

        for(int i=0; i<boardData.size(); i++) {
            if (boardData.get(i).getBoard_number() == board_number) {
                // 데이터 업데이트
                RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(i);
                if (viewHolder instanceof BoardWholeAdapter.ViewHolder) {
                    ((BoardWholeAdapter.ViewHolder) viewHolder).updateLikeStatus(newStatus, Like_cnt);

                }
                break;
            }
        }
    }





}
