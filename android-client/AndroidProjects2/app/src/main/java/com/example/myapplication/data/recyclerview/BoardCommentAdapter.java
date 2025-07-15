package com.example.myapplication.data.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.responsemodel.BoardCommentResponse;
import com.example.myapplication.layout.BoardViewActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardCommentAdapter extends RecyclerView.Adapter<BoardCommentAdapter.ViewHolder> {

    // 이 어댑터는 댓글을 작성할때, 기존 작성된 댓글을 불러올 때 사용되는 게시글 댓글 어댑터임.
    // 삭제 이번트도 있어야함. 마이페이지에 있는 드롭다운 가져오기

    // board comment adapter 구성순서
    // 1. 생성자
    // 2. view holder
    // 3. on create view holder - 레이아웃 setting
    // 4. bind view holder

    private Context mContext = null;

    private Activity mActivity;


    //

    private List<BoardCommentData> BoardCommentList = new ArrayList<>();


    // import
    RetrofitService service; // 레트로핏 서비스


    // 쉐어드 프리퍼런스
    PreferenceManager pref; // 쉐어드 프리퍼런스 - 로그인 유지용
    String key = "signin_email_id";
    String current_login_memberInfo; // 쉐어드에 저장된 이메일/아이디, 닉네임 키값쌍.
    String emailId ; // 이메일/아이디만 파싱한 값.
    String nickname; // 닉네임.



    // 댓글 수정, 삭제 팝업 상수
    private static final int MENU_EDIT_ID = R.id.menu_edit;
    private static final int MENU_DELETE_ID = R.id.menu_delete;



    // 현재 댓글 내용
    String thisCommentText;





//    =========================================== 구성 시작 =====================================


    // 1. 생성자
    public BoardCommentAdapter (Activity context) {mContext = context;}


    // 기본 생성자
    public BoardCommentAdapter(Activity activity,List<BoardCommentData> boardCommentList ) {
        this.mActivity = activity;
        this.BoardCommentList = boardCommentList;
        this.mContext = activity.getApplicationContext();
    }




    // 2. 뷰홀더
    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView comment_user_profileImg;
        public TextView comment_writer_nickname, comment_content, comment_date ;
        // 댓글 글쓴이 프사, 댓글쓴이 닉네임, 댓글 내용, 댓글 작성날짜시간
        public TextView recomment_btn; //대댓글쓰기 버튼,
        public ImageView comment_more_btn; //  더보기(수정,삭제버튼)


        // view holder 뷰 초기화
        public ViewHolder (View view) {
            super(view);

            // 댓글 글쓴이 프사
            comment_user_profileImg = view.findViewById(R.id.comment_user_profileImg);
            comment_writer_nickname = view.findViewById(R.id.comment_writer_nickname); // 닉네임
            comment_content = view.findViewById(R.id.comment_content); // 댓글 내용
            comment_date = view.findViewById(R.id.comment_date); // 댓글 작성 날짜
            recomment_btn = view.findViewById(R.id.recomment_btn); // 대댓글 작성하기 버튼
            comment_more_btn = view.findViewById(R.id.comment_more_btn); // 더보기 버튼 (수정 삭제)


        } // 뷰 초기화 끝

        public void setItem (BoardCommentData item) {

            // 더보기 버튼의 경우, 쉐어드 닉네임이 item 닉네임과 같을 경우에만 !! 보여지기
            if (nickname ==null) { // null point exception 방지
                // 닉네임 null임.
                Log.d("게시판 댓글 어댑터, 로그인닉넴없음", "닉넴null임  " +nickname );
            } else  { // 로그인한 회원 닉네임 정보 있음. 더보기 버튼 visibility 조정해주기
                if (nickname.equals(item.getComment_user_nickname())) {
                    Log.d("게시판 댓글 어댑터, 닉넴같음", "쉐어드닉넴:  " +nickname+ ", 아이템닉넴: " + item.getComment_user_nickname());
                    comment_more_btn.setVisibility(View.VISIBLE);
                } else {
                    // 더보기 버튼 안보이게 해주기
                    Log.d("게시판 댓글 어댑터, 닉넴다름", "쉐어드닉넴:  " +nickname+ ", 아이템닉넴: " + item.getComment_user_nickname());
                    comment_more_btn.setVisibility(View.GONE);
                }
            }


            comment_writer_nickname.setText(item.getComment_user_nickname());
            comment_content.setText(item.getComment_content());



            // 댓글 생성날짜와 수정날짜가 다를때, 수정됨 텍스트 추가하기
            if(item.getComment_date().equals(item.getComment_update_date())) {
                // 생성 날짜랑 수정날짜 같을 때 = 수정 안한 댓글
              //  Toast.makeText(mActivity, "수정 안한 댓글임", Toast.LENGTH_SHORT).show();

                comment_date.setText(item.getComment_date());
            } else if (!item.getComment_date().equals(item.getComment_update_date())) {
                // 수정했던 댓글
             //   Toast.makeText(mActivity, "수정헀던 댓글임", Toast.LENGTH_SHORT).show();

                comment_date.setText("(수정됨) " + item.getComment_date());
            }



            // Glide 추가 - 프사 있으면 프사 setting 하기
            if (item.getCommnet_user_img() !=null) {
                Glide.with(itemView.getContext())
                        .load(item.getCommnet_user_img())
                        .into(comment_user_profileImg);
            }

        } // set item
    } // 뷰홀더


  // 3. on create view holder
  @NonNull
  @Override
  public BoardCommentAdapter.ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();



      // 레트로핏
      service = RetrofitClient.getClient().create(RetrofitService.class); // 레트로핏 서비스 객체 정의


      // 쉐어드 - 로그인 정보 확인.
      pref = new PreferenceManager();
      current_login_memberInfo =   pref.getString(context, key);
      Log.d("게시판 댓글 어댑터", "로그인 회원정보from 쉐어드(string아님.):  " + current_login_memberInfo);

      try {
          JSONObject jsonObject = new JSONObject(current_login_memberInfo);
          nickname = jsonObject.optString("nickname", ""); // nullpoint exception 발생할 수 있으므로, 빈문자열로 두기
          emailId = jsonObject.optString("emailid", "");
          Log.d("my library whole 어댑터", "로그인 회원 이메일: " + emailId);
      } catch (JSONException e) {
          e.printStackTrace();
          emailId = ""; // 기본값 설정
      }

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflater.inflate(R.layout.itemlist_board_comments, parent, false);
      BoardCommentAdapter.ViewHolder holder = new BoardCommentAdapter.ViewHolder(view);
      return holder;

  } // on create 같은거??


    // 4. bind view holder
    @Override
    public void onBindViewHolder (@NonNull BoardCommentAdapter.ViewHolder holder, final int position) {

        // on bind 1. view 에 아이템 setting 하기
        BoardCommentData item = BoardCommentList.get(position);
        holder.setItem(item); // 여기서 최종적으로 바인딩 해줌 !!!

        // 현재 댓글 내용 string값에 담기.


        // on bind 2. 더보기 클릭 이벤트

        holder.comment_more_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("댓글 더보기 버튼 !!! 클릭", "현재댓글 position: " + position );
                showPopupMenu(v, position);
            }
        });


        // on bind 3. 대댓글 작성 이벤트

    } // on bind view holder 끝


    // 필요한 메소드 구현하기
    @Override
    public int getItemCount() {return BoardCommentList.size();}

    // 아이템 추가 - 댓글 작성시 사용
    public void addItem (BoardCommentData commentItem) {
        Log.i("게시판 책추가 add item ", "댓글 작성자닉네임: " + commentItem.getComment_user_nickname() + "댓글내용: " + commentItem.getComment_content());
        BoardCommentList.add(commentItem);
        notifyDataSetChanged(); // 리사이클러뷰에 알리기. 갱신
    }


    // 아이템 삭제
    public void clearItem() {BoardCommentList.clear();}


    // 특정 위치의 아이템 가져오기
    public BoardCommentData getItem (int position) {
        return BoardCommentList.get(position);
    }



    // 댓글 수정 삭제 드롭 다운 메뉴 띄우기
    private void showPopupMenu (View board_more, int position) {
        PopupMenu popupMenu = new PopupMenu(mActivity, board_more);
        popupMenu.getMenuInflater().inflate(R.menu.dropdown_menu_comment, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if(itemId == MENU_EDIT_ID) { // 수정 버튼 클릭

                    // Alert Dialog 사용하기
                    // 댓글을 수정하시겠습니까 ? 물어보기
                    final LinearLayout linear = (LinearLayout) View.inflate(mActivity, R.layout.dialog_edit_comment, null);
                    EditText edit_comment =  linear.findViewById(R.id.comment_edit);

                    // Alert Dialog - edit text 추가 사용하기
                    // 댓글을 수정하시겠습니까 ? 물어보기
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setTitle("댓글을 수정할까요?")
                            .setMessage("수정을 원할 시 확인 버튼을 눌러 주세요")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 수정창 띄우기.
                                  //  Toast.makeText(mActivity, "수정하기 버튼 클릭됨", Toast.LENGTH_SHORT).show();
                                    BoardCommentData this_comment = getItem(position);
                                    String this_comment_content = this_comment.getComment_content().toString();

                                    edit_comment.setText(this_comment_content);
                                    edit_comment.requestFocus();

                                 //   InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)

                                    // 수정 완료, 취소 버튼 작업.
                                    new AlertDialog.Builder(mActivity)
                                            .setView(linear)
                                            .setPositiveButton("수정 완료", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                 //   Toast.makeText(mActivity, "수정완료 버튼 클릭됨", Toast.LENGTH_SHORT).show();
                                                    // 수정 하는 곳으로 가야지.
                                                    // 수정 내용
                                                    String edit_text =  edit_comment.getText().toString();
                                                    int this_comment_number = this_comment.getComment_number();
                                                    int this_comment_user_number = this_comment.getComment_user_number();
                                                    Log.i("게시판 댓글수정내용 ", "게시글번호: " +this_comment_number + "댓글 작성자 번호: " + this_comment_user_number  + "수정할 댓글내용: " + edit_text);

                                                    // 댓글 수정 레트로핏 메소드 호출
                                                    comment_update(this_comment_user_number, this_comment_number, edit_text, position);

                                                }
                                            })
                                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Toast.makeText(mActivity, "수정 취소 버튼 클릭됨", Toast.LENGTH_SHORT).show();
                                                    // 수정 취소.
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(mActivity, "수정취소 버튼 클릭됨", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }).show();
                    return true;
                }  else if (itemId == MENU_DELETE_ID) { // 삭제 버튼 클릭
                    // 정말 삭제하시겠습니까? 물어보기
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setTitle("댓글 삭제")
                            .setMessage("댓글 삭제를 원할 시 확인을 눌러 주세요")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                //   Toast.makeText(mActivity, "댓글삭제확인 버튼 클릭됨", Toast.LENGTH_SHORT).show();


                                    BoardCommentData this_comment = getItem(position);
                                    int this_comment_number = this_comment.getComment_number();
                                    int this_com_user_number = this_comment.getComment_user_number();
                                    String comment_text = this_comment.getComment_content();
                                    // 댓글 삭제 레트로핏 호출
                                    Log.i("게시판댓글 - 삭제할댓글 정보 ", "게시글번호: " +this_comment_number + "댓글 작성자 번호: " + this_com_user_number  + "삭제할 댓글내용: " + comment_text);

                                    comment_delete(this_com_user_number, this_comment_number, position);

                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(mActivity, "댓글삭제 취소 버튼 클릭됨", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();

                                }
                            }).show();
                    return true;
                }

                return false;
            }
        });
        popupMenu.show();
    }



    // 수정 레트로핏 - 댓글 번호, 댓글 작성자번호, 수정할 내용가지고 가서, 댓글번호, 작성자번호를 where 절에 주기
    public void comment_update (int com_user_number , int comment_number, String edit_comment_content, int position) {
        Log.i("댓글 수정 메소드 진입  ", "수정할댓글의 번호: " +comment_number + "수정할댓글 작성자 번호: " + com_user_number  + "수정할 댓글내용: " + edit_comment_content);
        service.board_comment_update(com_user_number, comment_number, edit_comment_content).enqueue(new Callback<BoardCommentResponse>() {
            @Override
            public void onResponse(Call<BoardCommentResponse> call, Response<BoardCommentResponse> response) {
                BoardCommentResponse result = response.body();
                Log.e("게시판 상세 댓글 수정 완료", " php가 보낸응답: " + result.getCode() + ", 메시지: " + result.getMessage());

                Log.e("댓글 수정완료", " 수정 댓글 내용 : " + result.getCode() + ", 메시지: " + result.getMessage());
                String edited_comment = result.getComment_content(); // 수정 완료된 댓글

                if (result !=null && result.getCode() ==200) {

                    BoardCommentData updateComment = BoardCommentList.get(position);
                    updateComment.setComment_content(result.getComment_content()); // 여기서 새로 세팅을해줌!!!
                    updateComment.setComment_date(result.getUpdateTime());
                    notifyDataSetChanged();



                    // 수정 완료 메시지 표시
                    Toast.makeText(mContext, "댓글이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    // 수정 완료 메시지 표시
                    Toast.makeText(mContext, "댓글 수정 실패하였습니다 !! 통신은 성공 ", Toast.LENGTH_SHORT).show();
                }
            } // response 끝

            @Override
            public void onFailure(Call<BoardCommentResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("게시판 댓글 수정 실패: ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
            }
        });

    } // 수정 retrofit 완료



    // 댓글 삭제
    public void comment_delete (int com_user_number , int comment_number, int position) {
        Log.i("댓글 삭제 메소드 진입  ", "게시판댓글 삭제 - 삭제할댓글의 번호: " +comment_number + "삭제할댓글의 작성자 번호: " + com_user_number + "댓글 포지션: " + position );
        service.board_comment_delete(com_user_number, comment_number).enqueue(new Callback<BoardCommentResponse>() {
            @Override
            public void onResponse(Call<BoardCommentResponse> call, Response<BoardCommentResponse> response) {
                BoardCommentResponse result = response.body();
                Log.e("게시판댓글 삭제 완료", " php가 보낸응답: " + result.getCode() + ", 메시지: " + result.getMessage());

                BoardCommentList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, BoardCommentList.size() - position);
                Log.e("게시판댓글 삭제 완료 댓글개수", " BoardCommentList size: " + BoardCommentList.size() );
            }

            @Override
            public void onFailure(Call<BoardCommentResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("게시판 댓글 삭제 실패: ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
            }
        });
    }



}
