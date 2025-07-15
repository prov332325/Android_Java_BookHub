package com.example.myapplication.data.recyclerview;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.retrofit.responsemodel.ImageUploadResponse;

import java.util.ArrayList;

public class BoardImageAdapter extends RecyclerView.Adapter<BoardImageAdapter.ViewHolder> {
    private ArrayList<ImageUploadResponse> mData = null ;
    private Context mContext = null ;


    // 생성자에서 데이터 리스트 객체와 context를 전달받음
    public BoardImageAdapter(ArrayList<ImageUploadResponse> list, Context context) {
        mData = list ;
        mContext = context;
    }

    // 뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image; // 이미지 넣을 곳
        ImageView remove_btn; // x 버튼
        ViewHolder (View itemView) {
            super(itemView);
            // 뷰 객체에 대한 참조 find
            image = itemView.findViewById(R.id.image);
            remove_btn = itemView.findViewById(R.id.removeImg);
        }
    }


    // onCreateViewHolder(): 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    // LayoutInflater: XML에 정의된 Resource(자원) 들을 View의 형태로 반환.
    public BoardImageAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //  context에서 LayoutInflater 객체를 얻는다.
        View view = inflater.inflate(R.layout.itemlist_boardimg, parent, false);
        BoardImageAdapter.ViewHolder vh = new BoardImageAdapter.ViewHolder(view);
        return vh;
    } // on create view holder

    // bind view holder
    @Override
    public void onBindViewHolder(BoardImageAdapter.ViewHolder holder, int position) {
        Log.d("뷰홀더.", "이미지 뷰홀더 들어옴");

        // 액티비티 - 사용자가 처음 갤러리 들어가서 10개 이상 선택한 경우 토스트 메시지. 처음부터 걸러짐.
        // 어댑터 - 추가적으로 사진 넣을때 이미 리사이클러뷰에 아이템이 몇개 있는지, 하나씩 넣을때마다 10개 넘는지 확인하고 추가할때 걸러짐.


        // 서버에서 가져온 사진일 경우 - url

        // 갤러리에서 가져온 사진일 경우 - uri

        if(mData.get(position).getImageUrl() == null) { //갤러리인 경우
            Log.d("뷰홀더 -갤러리인지서버인지", "갤러리사진임uri: " + mData.get(position).getImageUri());
            Uri image_uri = mData.get(position).getImageUri() ;
            // 10개 이하일때까지만 !! 10개 넘어가면 이미지는 10개까지만 가능하다고 해줌.
            if (image_uri != null) {
                // 10장 이하일 경우에만 한장씩 추가. glide로 !!
                Glide.with(mContext)
                        .load(image_uri)
                        .into(holder.image);
            }

        } else if (mData.get(position).getImageUri() == null) { // 서버인경우
            Log.d("뷰홀더 -갤러리인지서버인지", "서버 사진임url: " + mData.get(position).getImageUrl());

            String img_serverAddress = "http://3.39.255.234/php/img/";
            String board_img_name = mData.get(position).getImageUrl() ; // db에 이미지의 이름이 들어가있음.

            // 얘도 사진 하나씩불러옴.
            // 혹시 모르니까 10개로 제한줌. 열개 넘을 일은 없음.
            if (board_img_name !=null) {
                String imgUrl = img_serverAddress + board_img_name;
                Glide.with(mContext)
                        .load(imgUrl)
                        .into(holder.image);
            }

        }


        // 여기서 아이템 x 버튼 클릭시 리사이클러뷰에서 삭제 되게.
        holder.remove_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(position);
            }
        });



    } // bind view holder 끝


    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }


    // 6. add item
    public void addItem (ImageUploadResponse imgURI) {
        mData.add(imgURI);
        Log.e("이미지추가하기!!!", "이미지번호: "+ imgURI.getImageUri()); // 여기에 괄호로 두개를 안 묶어주면 string+int+int 라서 문자열 연결됨.
        notifyDataSetChanged();
        // 시간순 정렬 어케 함?
    }



    // 아이템 삭제
    public void deleteItem(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mData.size());

    }

    // 아이템 삭제
    public void clearItem() {
        mData.clear();
        notifyDataSetChanged();
    }


}
