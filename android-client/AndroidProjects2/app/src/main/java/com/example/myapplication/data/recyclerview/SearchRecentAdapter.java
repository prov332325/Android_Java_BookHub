package com.example.myapplication.data.recyclerview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class SearchRecentAdapter extends RecyclerView.Adapter<SearchRecentAdapter.ViewHolder> {


    // 최근 검색어 리스트를 담을 데이터 리스트
    private ArrayList<String> mData =null;
    public Context mContext = null;
    private OnSearchItemClickListener_deleted mListener;
    private OnSearchItemClickListener_clicked cListener;


    // 1. 생성자
    public SearchRecentAdapter(ArrayList<String> list, Context context, OnSearchItemClickListener_deleted deleted_listener, OnSearchItemClickListener_clicked clicked_listener) {
        this.mData = list;
        this.mContext = context;
        this.mListener = deleted_listener;
        this.cListener = clicked_listener;

    }



    // 2. 뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView recent_keyword; // 최근 검색어 목록 아이텐
        public ImageView remove_btn; // 삭제 버튼

        // 뷰 초기화
        public ViewHolder(View itemView) {
            super(itemView);
            // 뷰 객채에 대한 참조 find
            recent_keyword = itemView.findViewById(R.id.recent_keyword);
            remove_btn = itemView.findViewById(R.id.remove_btn);
        } // 초기화 끝

//        public void setItem (String keyword) {
//            recent_keyword.setText(keyword);
//        }


    }

    // onCreateViewHolder(): 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    // LayoutInflater: XML에 정의된 Resource(자원) 들을 View의 형태로 반환.


    // 3. on create view holder
    @NonNull
    @Override
    public SearchRecentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //context에서 LayoutInflater 객체를 얻는다.
        View view = inflater.inflate(R.layout.itemlist_recent_search_book, parent, false);
        SearchRecentAdapter.ViewHolder vh = new SearchRecentAdapter.ViewHolder(view);
        Log.d("onCreateViewHolder", "들어옴 !!  ");
        return vh;
    } // on create view holder

    // 4. bind view holder
    @Override
    public void onBindViewHolder(@NonNull SearchRecentAdapter.ViewHolder holder, int position) {
        Log.d("뷰홀더.", "최근 검색 뷰홀더 들어옴");

        //0. 검색어 리스트의 현재 position에 해당하는 데이터를 TextView에 바인딩
        String recentSearch = mData.get(position);
        holder.recent_keyword.setText(recentSearch);

        //1. 아이템 x 버튼 눌렀을때 삭제하는 로직
        holder.remove_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(position);
            }
        });

        // 2. 아이템 x 버튼 눌렀을때 삭제하는 로직
        // 1) 어댑터에서 인터페이스 통해 프래그먼트로 삭제 요청
        // 2) delete item
        holder.remove_btn.setOnClickListener(v -> {

            deleteItem(position); // 리사이클러뷰에서 데이터 삭제하고 갱신까지.
            // 이걸 먼저 해줘야 함... 왜지 ?

            if (mListener != null) {
                mListener.onSearchItemDeleted(recentSearch);
            }

        });

        // 3. 아이템 클릭 시
        holder.recent_keyword.setOnClickListener(v -> {
            cListener.onSearchItemClicked(position, recentSearch);
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }



    // 유저가 검색을 할때마다 리사이클러뷰 업데이트가 필요하기 때문에 업데이트 메소드를 정의해줌.
    public void updateData(List<String> newData) {
        mData.clear();
        mData.addAll(newData);
        notifyDataSetChanged();
    }



    // 아이템 삭제
    public void deleteItem(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mData.size()); // 아이템 개수 갱신한다.



    }


    // 아이템 모조리 삭제
    public void clearItem() {
        mData.clear();
        notifyDataSetChanged();
    }


}
