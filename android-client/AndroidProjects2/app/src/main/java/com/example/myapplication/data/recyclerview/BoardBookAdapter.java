package com.example.myapplication.data.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.retrofit.responsemodel.ImageUploadResponse;

import java.util.ArrayList;
import java.util.List;

public class BoardBookAdapter extends RecyclerView.Adapter<BoardBookAdapter.ViewHolder> {


    // 이 어댑터는, 게시글 검색 시, 책 추가할때 선택한 책이 board create 액티비티에 추가될때 사용되는 리사이클러뷰임.
    // 삭제도 할 수 있어야 함.

    // board book adapter 구성순서
    // 1. 생성자
    // 2. view holder
    // 3. on create view holder - 레이아웃 setting
    // 4. bind view holder


    private Context mContext = null;

    private List<SearchBookData> BoardBookList = new ArrayList<SearchBookData>();



    // 1. 생성자
    public BoardBookAdapter (Activity context) {mContext = context;}

    public BoardBookAdapter(List<SearchBookData> list, Context context) {
        BoardBookList = list ;
        mContext = context;
    }


    // 2. 뷰홀더 먼저
    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView book_cover_img;
        public TextView book_title_tv, book_author_tv, book_description_tv;

        public LinearLayout book_delete_btn;

        public ViewHolder (View view) {
            super(view);
            book_cover_img = view.findViewById(R.id.book_cover);
            book_title_tv = view.findViewById(R.id.book_title);
            book_author_tv = view.findViewById(R.id.book_author);
            book_description_tv =  view.findViewById(R.id.book_description);

            // 삭제 버튼
            book_delete_btn = view.findViewById(R.id.book_delete_btn);
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
    public BoardBookAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.itemlist_searchbook_board, parent, false);
        BoardBookAdapter.ViewHolder holder = new BoardBookAdapter.ViewHolder(view);
        return holder;
    } // on create 같은거


    // 4. bind view holder
    @Override
    public void onBindViewHolder (@NonNull BoardBookAdapter.ViewHolder holder, final int position ) {

        // bh 1. view에 아이템 setting 하기
        SearchBookData item = BoardBookList.get(position);
        holder.setItem(item);


        // bh 2. 아이템 클릭 이벤트 !! 삭제 버튼 클릭 이벤트 추가하기

        holder.book_delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("게시판 create, 책 삭제버튼누름", "추가한 책 삭제 버튼 누름 !! " );
                BoardBookList.remove(position);
                notifyDataSetChanged();
                // 추가하고 책 목록 뽑아보기

                if (BoardBookList.size()>0) {
                    for (SearchBookData item : BoardBookList) {
                        Log.i("update책 삭제함 ",position +"번째 책제목: " + item.getTitle()+", 작가: " + item.getAuthor());
                    }
                }
            } // on click 메소드
        }); // on click listener
    } // onBindViewHolder


    // 필수 override !!
    @Override
    public int getItemCount() {
        return BoardBookList.size();
    }


    // 아이템 추가
    public void addItem(SearchBookData bookitem) {
        Log.i("게시판 책추가 add item ", "책 제목: " + bookitem.getTitle() + "책 작가: " + bookitem.getAuthor());
        BoardBookList.add(bookitem);
        notifyDataSetChanged(); // 리사이클러뷰 갱신
    }


    // 아이템 삭제
    public void clearItem() {BoardBookList.clear();}


    // 특정 위치의 아이템 가져오기
    public SearchBookData getItem (int position) {
        return BoardBookList.get(position);
    }





}
