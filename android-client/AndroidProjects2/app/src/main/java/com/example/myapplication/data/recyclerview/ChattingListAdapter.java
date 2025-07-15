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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.layout.ChattingViewActivity;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class ChattingListAdapter extends RecyclerView.Adapter<ChattingListAdapter.ViewHolder> {

    // 채팅방 목록에 대한 adapter 구성 순서

    // 1. 생성자
    // 2. view holder
    // 3. on create view holder - 레이아웃 setting 을 위한
    // 4. bind view holder - 상세보기 클릭 이벤트 구현하는 곳

    // context
    private Context context;

    // 아이템 리스트
    private ArrayList<ChattingListData> chattingListData = new ArrayList<>();

    // 1. 생성자
    public ChattingListAdapter(Context context) {
        this.context = context;
        this.chattingListData = new ArrayList<>();
    }

    // 2. 뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {

        // 채팅방 view
        ImageView other_user_profileImg;
        TextView other_user_nickname, last_sent_message, last_sent_time, unread_msg_count;


        public ViewHolder (View view, int viewType) {
            super(view);

            // 뷰 연결
            other_user_profileImg = view.findViewById(R.id.other_user_profileImg); // 상대 프사
            other_user_nickname = view.findViewById(R.id.other_user_nickname); // 상대 닉네임
            last_sent_message = view.findViewById(R.id.last_sent_message); // 마지막으로 보낸 메시지
            last_sent_time = view.findViewById(R.id.last_sent_time); // 마지막 메시지 보낸 시간
            unread_msg_count = view.findViewById(R.id.unread_message_cnt); // 안읽은 메시지 개수

        } // view holder 끝

        // set item
        public void setItem (ChattingListData item) {

            String img_serverAddress = "http://3.39.255.234/php/img/";
            String userProfileImg = item.getOther_user_profile_img();

            // 상대 유저 프사
            if (userProfileImg !=null) {
                String img_url = img_serverAddress+userProfileImg;
                Glide.with(itemView.getContext())
                        .load(img_url)
                        .apply(RequestOptions.circleCropTransform()) // 원형 변환 적용
                        .into(other_user_profileImg);
            } else {
                other_user_profileImg.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.mipmap.ic_launcher_round));
            }

            // 상대 닉네임, 마지막 메시지, 마지막 메시지 시간
            other_user_nickname.setText(item.getOther_user_nickname());
            last_sent_message.setText(item.getLast_message_txt());
            last_sent_time.setText(item.getLast_message_time());

            String last_sent_timeString = item.getLast_message_time();

            // 마지막 메시지 시간 월/일로 변경하기
            try {
//                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                Date date = format.parse(last_sent_timeString);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(last_sent_timeString, formatter);

                Log.d("시간 변경하러들어옴", " string 값 시간: " + last_sent_timeString + ", date 로 형변환한 시간: " + dateTime.toString());
                // string 값 시간: 2024-07-24 08:13:20, date 로 형변환한 시간: Wed Jul 24 08:13:20 GMT 2024
                // 이렇게 나옴.. 음. 여기서 캘린더 사용하기.
            } catch (Exception e) {
                e.printStackTrace();
            } // try catch 문 끝


            // 안읽은 메시지 개수 setting
            if (item.getUnread_msg_cnt() != 0 ) { // 값이 있는 경우에만!!
                unread_msg_count.setVisibility(View.VISIBLE);
                unread_msg_count.setText("" +item.getUnread_msg_cnt());
            } else {
                unread_msg_count.setVisibility(View.GONE);
                // 0일 때에는 안 보이게 해야함.
            }

            // 서로맞팔이 아닌 경우에는 !! last 메시지에 띄워주기.
            if (item.getFollow_each_other() != 1) {
                last_sent_message.setText("채팅이 불가능한 상대입니다. 맞팔 후 가능.");
                last_sent_time.setText("");
            }


        } // set item 끝
    } // view holder 끝



    // 3. on create view holder
    @NonNull
    @Override
    public ChattingListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Context
        Context context = parent.getContext();

        // 반환할 뷰
        View view;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.itemlist_chatting_list, parent,false);
        ChattingListAdapter.ViewHolder holder = new ChattingListAdapter.ViewHolder(view, viewType);
        return holder;
    } // on create 끝


    // 4. bind view holder

    @Override
    public void onBindViewHolder(@NonNull ChattingListAdapter.ViewHolder holder, final int position) {

        // 1. 아이템 setting 하기
        ChattingListData item = chattingListData.get(position);
        holder.setItem(item);

        String roomnumber = String.valueOf(item.getRoom_number());

        // 아이템 상세보기 click event 구현하기
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("채팅방 상세 보기 클릭 시도", "상세보기진입 시도 ");
                Intent intent = new Intent(context, ChattingViewActivity.class);
                // 인텐트에 들어가야 하는 값은?
                // 상대유저닉네임, 상대유저 번호, 상대 유저 아이디, 로그인유저 번호, 로그인유저 닉네임(null), 로그인유저 아이디
                intent.putExtra("FROM_WHERE", "FROM_FRAGMENT_CHAT");
                intent.putExtra("this_user_nickname", item.getOther_user_nickname());
                intent.putExtra("this_user_number",  String.valueOf(item.getOther_user_number()));
                intent.putExtra("this_user_emailID", item.getOther_user_emailId());
                intent.putExtra("login_user_number",  String.valueOf(item.getMy_user_number()));
                intent.putExtra("login_user_nickname", "");
                intent.putExtra("login_user_emailId", item.getMy_user_emailId());
                intent.putExtra("roomId", roomnumber);
                context.startActivity(intent);
            }
        });









    } // on bind view holder 끝

    // 5. get item count 필수

    @Override
    public int getItemCount() {
        return chattingListData.size();
    }


    // 6. add item
    public void addItem (ChattingListData item) {
        chattingListData.add(item);
        notifyDataSetChanged();
    }

    // 6-1. add item 2
    public void addItem(int index, ChattingListData item) {
        if (index < 0 || index > chattingListData.size()) {
            return; // Invalid index
        }
        chattingListData.add(index, item);
        notifyItemInserted(index);
    }



    // 7. 아이템 전체 삭제
    public void clearItem() {
        chattingListData.clear();
    }


    // 8. 특정 위치 아이템 가져오기
    public ChattingListData getItem (int position) {
        return chattingListData.get(position);
    }


    // 9. 아이템 삭제
    public void deleteItem(int position) {
        chattingListData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, chattingListData.size());

    }





}
