package com.example.myapplication.data.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.retrofit.responsemodel.ImageUploadResponse;

import java.util.ArrayList;

public class BoardViewImgAdapter extends RecyclerView.Adapter<BoardViewImgAdapter.BoardViewImageViewHolder> {

    // adapter 구성순서
    // 1. 생성자
    // 2. view holder
    // 3. on create view holder - 레이아웃 setting
    // 4. bind view holder - 상세보기 클릭 이벤트 구현하는 곳.


    // array list - String
    private ArrayList<ImageUploadResponse> mData = null;

    // context
    private Context mContext = null ;



    // 생성자에서 데이터 리스트 객체와, context 를 전달 받음.
    public BoardViewImgAdapter (ArrayList<ImageUploadResponse> list, Context context) {
        mData = list;
        mContext = context;
    }


    // 뷰 홀더
    public class BoardViewImageViewHolder extends RecyclerView.ViewHolder {

        // 이미지
        ImageView image;

        BoardViewImageViewHolder (View itemView) {
            super(itemView);
            // 뷰 객체에 대한 참조
            image = itemView.findViewById(R.id.board_image);
        }
    }

    // onCreateViewHolder(): 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    // LayoutInflater: XML에 정의된 Resource(자원) 들을 View의 형태로 반환.
    public BoardViewImgAdapter.BoardViewImageViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        Log.d("게시판이미지 onCreateViewHolder", "온 크리에이트 뷰홀더 들어옴");
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //  context에서 LayoutInflater 객체를 얻는다.
        View view = inflater.inflate(R.layout.itemlist_boar_view_image, parent, false);
        BoardViewImgAdapter.BoardViewImageViewHolder vh = new BoardViewImgAdapter.BoardViewImageViewHolder(view);
        return vh;
    } // on create view holder


    // bind view holder
    @Override
    public void onBindViewHolder(BoardViewImgAdapter.BoardViewImageViewHolder holder, int position) {
        Log.d("게시판이미지 바인드 뷰홀더~!.", "이미지 뷰홀더 들어옴");

        String img_serverAddress = "http://3.39.255.234/php/img/";
        String board_img_name = mData.get(position).getImageUrl() ; // db에 이미지의 이름이 들어가있음.

        Log.d("게시판이미지 바인드 뷰홀더~! 이미지경로!!", "이미지경로!!: " + img_serverAddress + board_img_name);
        // 얘도 사진 하나씩불러옴.
        // 혹시 모르니까 10개로 제한줌. 열개 넘을 일은 없음.
        if (board_img_name !=null) {
            String imgUrl = img_serverAddress + board_img_name;
            Glide.with(mContext)
                    .load(imgUrl)
                    .into(holder.image);
        }



    } // bind view holder 끝

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }

    // 아이템 추가
    public void addItem (ImageUploadResponse imageitem) {
        Log.e("게시글 상세 이미지 보기 들어옴", "이미지 있음!!: " + imageitem.getBoard_image_number() );
        Log.e("게시글 상세 이미지 보기 들어옴", "이미지 있음!!: " + imageitem.getImageUrl() );
        imageitem.getBoard_image_number();
        mData.add(imageitem);
        notifyDataSetChanged();
    }

    // 아이템 삭제
    public void deleteItem(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mData.size());

    }

    // 리사이클러뷰 전부 삭제 clear
    // 7. 아이템 전체 삭제
    public void clearItem() {
        mData.clear();
        notifyDataSetChanged();
    }



} // BoardViewImgAdapter 어댑터 전체 끝
