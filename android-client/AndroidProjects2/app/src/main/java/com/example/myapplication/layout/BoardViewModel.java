package com.example.myapplication.layout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.data.recyclerview.BoardData;

import java.util.ArrayList;
import java.util.List;



public class BoardViewModel extends ViewModel {
    private final MutableLiveData<List<BoardData>> boardList = new MutableLiveData<>();
//    private final MutableLiveData<String> likeStatus = new MutableLiveData<>();

    public LiveData<List<BoardData>> getBoardList() {
        return boardList;
    }


    // 데이터 로딩
    public void loadBoardData() {
        List<BoardData> dummyData = new ArrayList<>();
       boardList.setValue(dummyData); // LiveData에 데이터 설정
    }

}
