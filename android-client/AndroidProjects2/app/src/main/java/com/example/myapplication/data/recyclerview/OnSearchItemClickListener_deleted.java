package com.example.myapplication.data.recyclerview;

public interface OnSearchItemClickListener_deleted {
    void onSearchItemDeleted(String keyword);
}

// 최근 검색어 목록이 검색창 하단에 떴을 때, 사용자는 삭제 버튼을 눌러서 아이템 삭제 가능
// 아이템 삭제되면, 쉐어드에서도 삭제되어야 함.
// 어댑터에 쉐어드 객체 생성해서 아이템 삭제하지 않고 인터페이스 활용함.


// 어댑터: 리사이클러뷰에 이벤트 발생 시, ui 관리. 검색어 목록 아이템을 표시하고, 삭제 버튼 표시함.
// 프래그먼트: OnSearchItemClickListener 의 내용을 구현해서 전달받은 키워드에 대해,
// 쉐어드에서 바르게 삭제될 수 있도록 함. = 데이터 갱신.

// 어댑터는 ui 와 이벤트 처리를 담당하고 프래그먼트는 비즈니스 로직, 데이터 처리 담당.
// 즉, 리사이클러뷰 아이템이 삭제 되었을때 그 이벤트를 프래그먼트에 전달하고 그에 맞는 역할을 할 수 있도록
// 중개자 역할을 한다.