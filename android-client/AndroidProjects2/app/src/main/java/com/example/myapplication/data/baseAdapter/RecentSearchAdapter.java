package com.example.myapplication.data.baseAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.HashSet;
import java.util.List;

public class RecentSearchAdapter extends ArrayAdapter<String> {

    // base adpater 란?

    private Activity mContext;
    private List<String> recentSearches;
    private SharedPreferences sharedPreferences;


    public RecentSearchAdapter(Activity context, List<String> recentSearches) {
        super(context, android.R.layout.simple_dropdown_item_1line, recentSearches);
        this.mContext = context;
        this.recentSearches = recentSearches;
        this.sharedPreferences = context.getSharedPreferences("RecentSearches", Context.MODE_PRIVATE);
    }

    @Override
    public int getCount() {
        return recentSearches.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    // get view
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_autocomplete, parent, false);
        }

        // 텍스트 아이템
        TextView textView = convertView.findViewById(R.id.keyword_text);
        textView.setText(recentSearches.get(position));

        // 삭제 버튼
        ImageView deleteButton = convertView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> {
            // 삭제할 항목을 리스트에서 제거
            String itemToRemove = recentSearches.get(position);
            recentSearches.remove(position);

            // 리스트에서 아이템을 제거한 후 어댑터에 변경 사항 반영
            notifyDataSetChanged();

            // 쉐어드에서 삭제된 항목 제거
            saveRecentSearches();
        });

        return convertView;
    }


    private void saveRecentSearches() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("recent_searches", new HashSet<>(recentSearches));
        editor.apply();
    }

}
