package com.example.myapplication.data.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ChattingContentAdapter extends RecyclerView.Adapter<ChattingContentAdapter.ViewHolder> {

    // 채팅방에서 채팅하기에 대한  adapter 구성순서
    // 1. 생성자
    // 2. view holder
    // 3. on create view holder - 레이아웃 setting
    // 4. bind view holder - 상세보기 클릭 이벤트 구현하는 곳.

    // context
    private Activity mContext;


    // 뷰 타입
    private static final int TYPE_FROM_ME = 1; // 내가 보내는 거
    private static final int TYPE_TO_ME = 2; // 나한테 보내는 거


    // 아이템 리스트
    private ArrayList<ChattingContentData> chatItemData = new ArrayList<>();


    // 1. 생성자
    public ChattingContentAdapter (Activity context) {mContext = context;}
    // 왜 이 생성자만 필요한걸까 ?? 모르는데 그냥 쓰는 중


    // 2. 뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {

        // 메시지에 들어가는 view - 이미지,
        // 초기화는 각각 다 해놓으면 매핑할때만 view type 나누면 됨.

        // 상대편 메시지
        ImageView this_user_img;
        TextView this_user_nickname, message_to_me, this_message_time, date_change_to; // 상대 프사, 닉넴, 메시지, 시간

        // 내가 보내는 메시지
        TextView message_from_me, message_time, date_change_from, is_read; // 내가 보낸 메시지, 메시지 시간, 읽음 표시



        public ViewHolder (View view, int viewType) {
            super(view);

            // 상대편 메시지 일때
            if (viewType == TYPE_TO_ME) {
                Log.d("기존 채팅 어댑터", "상대편 메시지일때! " );

                this_user_img = view.findViewById(R.id.chatting_img_tome); // 상대 프사
                this_user_nickname = view.findViewById(R.id.chatting_nickname_tome); // 상대 닉네임.
                message_to_me = view.findViewById(R.id.chatting_content_tome); // 상대가 보낸 메시지
                this_message_time = view.findViewById(R.id.chatting_time_tome); // 상대가 메시지 보낸 시간
                date_change_to = view.findViewById(R.id.chatting_date_change_to); // 상대편 날짜 바뀜
            } // 내가 보내는 메시지 일때
            else if (viewType == TYPE_FROM_ME) {
                message_from_me = view.findViewById(R.id.chatting_content_fromme); // 내가 보내는 메시지
                message_time = view.findViewById(R.id.chatting_time_fromme); // 내가 메시지 보낸 시간.
                date_change_from = view.findViewById(R.id.chatting_date_change_from); // 내 쪽에서 날짜 바뀜
                is_read = view.findViewById(R.id.is_read_text);
            }
        } // view holder 끝



        // set item
        public void setItem (ChattingContentData item, int viewType) {

            String img_serverAddress = "http://3.39.255.234/php/img/";
            String userProfileImg = item.getUser_profile_img();

            // 아이템 세팅에 대한 분기처리
            // 상대편 메시지일때
            if(viewType == TYPE_TO_ME) {// 상대가 보낸 메시지일 경우
//                // 상대 프사
                if (userProfileImg !=null) {
                    String img_url = img_serverAddress+userProfileImg;
                    Glide.with(itemView.getContext())
                            .load(img_url)
                            .apply(RequestOptions.circleCropTransform()) // 원형 변환 적용
                            .into(this_user_img);
                }

                // 상대 닉넴, 메시지, 시간.
                this_user_nickname.setText(item.getUser_nickname());
                message_to_me.setText(item.getMessage());
                this_message_time.setText(item.getMessage_time());


                // 날짜 구분선
                if (item.getShowTimeLine() != null && item.getShowTimeLine().equals("1")) {

                    date_change_to.setVisibility(View.VISIBLE);
                    date_change_to.setText(item.getMessage_time().substring(0, 10));
                } else {
                    date_change_to.setVisibility(View.GONE);
                }


            } // 내가 보낸 메시지일 때
            else if (viewType == TYPE_FROM_ME) {
                message_from_me.setText(item.getMessage());
                message_time.setText(item.getMessage_time());
                date_change_from.setVisibility(View.GONE);
                if (item.getIs_read()==1) { // 읽었을 때.
                    is_read.setText("");
                    Log.d("읽었을때 ", "item.getIs_read() 가 빈칸되어야함" );

                } else if (item.getIs_read() == 0) { // 안 읽었을 때
                    is_read.setText("1");
                    Log.d("읽었을때 ", "item.getIs_read() 가 1임" );
                }

            }
        } // set item
    } // view holder


    // 뷰타입 가져오기 !!! override
    public int getItemViewType (int position)  {
        return chatItemData.get(position).getType();
    }

    // 3. on create view holder
    // 하나의 뷰가 생성될 때 마다 호출됨 !! 아이템 하나 하나마다 호출됨 !
    // 그렇기 때문에 item 분기처리를 여기서 해준다.
    // inflate 해주는거임.
    @NonNull
    @Override
    public ChattingContentAdapter.ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {

        // 리사이클러뷰의 on create 같은 것?
        Context context = parent.getContext();

        // 반환할 뷰는?
        View view;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 뷰 타입에 따른 반환 뷰 분기처리하기.
        switch (viewType) {
            case TYPE_TO_ME:
                view = inflater.inflate(R.layout.itemlist_chatting_tome, parent, false);
                break;
            case TYPE_FROM_ME:
                view = inflater.inflate(R.layout.itemlist_chatting_fromme, parent, false);
                break;
            default:
                throw new IllegalArgumentException("올바르지않은타입!!! 어댑터속 뷰홀더임");
        }

        ChattingContentAdapter.ViewHolder holder = new ChattingContentAdapter.ViewHolder(view, viewType);
        return holder;
    } // on create 끝


    // 4. bind view holder
    @Override
    public void onBindViewHolder (@NonNull ChattingContentAdapter.ViewHolder holder, final int position) {

        // 1. 아이템 세팅하기
        ChattingContentData item = chatItemData.get(position);
        holder.setItem(item, getItemViewType(position));

        // glide 사용한 이미지 추가 이곳 에서 하기.




    } // onBindViewHolder 끝

    // 5. get item count 필수
    @Override
    public int getItemCount() {
        return chatItemData.size();
    }


    // 6. add item
    public void addItem (ChattingContentData item) {
//        chatItemData.add(item);
//        notifyDataSetChanged();
        chatItemData.add(0,item);
        notifyItemInserted(chatItemData.size() - 1);
    }

    public void addItem_NEW (ChattingContentData item) {
//        chatItemData.add(item);
//        notifyDataSetChanged();
        chatItemData.add(item);
        notifyItemInserted(chatItemData.size() - 1);
    }


    // 아이템 클리어
    public void clearItem() {
//        chatItemData.clear();
//        notifyDataSetChanged();
        int size = chatItemData.size();
        chatItemData.clear();
        notifyItemRangeRemoved(0, size);
    }


    // 아이템 삭제
    public void removeLastItem() {
        int itemCount = getItemCount();
        if (itemCount > 0) {
            int lastPosition = itemCount - 1;
            chatItemData.remove(lastPosition);
            notifyItemRemoved(lastPosition);
        }
    }


    // 특정 포지션 아이템의 고유번호 가져오기
    public int getMessageNumber (int position) {
       return chatItemData.get(position).getMessage_number();
    }


    // 메시지 읽음 처리 하는 메소드
    public void read_update (String read_time) {
        //아이템의 길이 만큼 반복문 돌면서 각각 아이템이 내가 작성한 메시지인 경우를
        // 가져온다음에 !!
        // 특정 시간보다 이전이면 is_read 의 1을 지워줌 ㅎㅎ
        int length = getItemCount();
        Log.d("리사이클러뷰 길이? ", length + "" );


        for (int i =0; i < chatItemData.size(); i++) {
            ChattingContentData item = chatItemData.get(i);
            Log.d("메시지 시간:  " + item.getMessage_time(), " 읽은시간: " + read_time );

            if(item.getMessage_time().compareTo(read_time) <= 0 && item.getType() == TYPE_FROM_ME) {
                item.setIs_read(1);
                notifyItemChanged(i);
            }
        }
    } // read update 메소드


    //local 시간 반환하는 메소드
    String convertUTCtoLocaltime (String utcTime) {
        // Z는 UTC 시간을 나타냄. ISO 8601 표준에 의거.
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date date = inputFormat.parse(utcTime); // 가지고 온 utc time string값을 date 로 형변환

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            outputFormat.setTimeZone(TimeZone.getDefault()); // 현지 시간을 반환하도록 기존 UTC 데이트 폼을 변경 해줌.


            return outputFormat.format(date); // 포맷팅 다시 시킴.
        } catch (ParseException e) {
            e.printStackTrace();
            return utcTime;
        }

    }

}
