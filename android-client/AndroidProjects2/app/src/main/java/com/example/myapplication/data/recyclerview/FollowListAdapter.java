package com.example.myapplication.data.recyclerview;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;
import static androidx.core.app.NotificationCompat.getColor;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.data.PreferenceManager;
import com.example.myapplication.data.retrofit.RetrofitClient;
import com.example.myapplication.data.retrofit.RetrofitService;
import com.example.myapplication.data.retrofit.UserNumberCallback;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.data.retrofit.responsemodel.ProfileFollowResponse;
import com.example.myapplication.layout.BoardViewActivity;
import com.example.myapplication.layout.MainActivity;
import com.example.myapplication.layout.OthersProfileActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowListAdapter extends RecyclerView.Adapter<FollowListAdapter.ViewHolder> {


    // import
    RetrofitService service; // 레트로핏 서비스



    // 쉐어드 프리퍼런스
    PreferenceManager pref; // 쉐어드 프리퍼런스 - 로그인 유지용
    String key = "signin_email_id";
    String current_login_memberInfo; // 쉐어드에 저장된 이메일/아이디, 닉네임 키값쌍.
    String emailId ; // 이메일/아이디만 파싱한 값.
    String nickname; // 닉네임.


    String user_number;// 현재 로그인 중인 유저 넘버
    int user_number_int; // 로그인 중인 유저 넘버 int 값 - call back 메소드에서 값 넣어주기




    // context
    private Context context;

    // 아이템 리스트
    private ArrayList<FollowingListData> followingListData = new ArrayList<>();


    // 1. 생성자

    public FollowListAdapter(Context context) {
        this.context = context;
        this.followingListData = new ArrayList<>(); // 필요하지 않을 수도 있음.
        // retrofit
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
            get_userNumber(emailId);
        } catch (JSONException e) {
            e.printStackTrace();
            emailId = ""; // 기본값 설정
        }
        get_userNumber(emailId);
    }


    // 2. 뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {

        // view 요소 - 프사, 닉네임, 버튼에 있는 텍스트 ?
        ImageView user_profile;
        TextView user_nickname;
        Button user_following_status;

        public ViewHolder (View view) {
            super(view);

            // 뷰 연결
            user_profile = view.findViewById(R.id.user_profileImg);
            user_nickname = view.findViewById(R.id.user_nickname);
            user_following_status = view.findViewById(R.id.profile_follow_btn);

        } // view holder 끝


        // set item
        public void setItem (FollowingListData item) {

            String img_serverAddress = "http://3.39.255.234/php/img/";
            String userProfileImg = item.getUser_profileImg();


            // 유저 프사
            if (userProfileImg !=null) {
                String img_url = img_serverAddress+userProfileImg;
                Glide.with(itemView.getContext())
                        .load(img_url)
                        .apply(RequestOptions.circleCropTransform()) // 원형 변환 적용
                        .into(user_profile);
            } else {
                user_profile.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.mipmap.ic_launcher_round));
            }

            // 상대 닉네임, 버튼
            user_nickname.setText(item.getUser_nickname());

            if(user_number == null) {
                Log.d("현재본인넘버null임", "유저넘버 널입니다." );
            }else {
                Log.d("현재본인넘버null임 아님", "유저넘버: " + user_number );
                if(user_number.equals(String.valueOf(item.getUser_number()))) {
                    user_following_status.setVisibility(View.GONE);
                }
            }


            // follow status 에 따라서 버튼 세팅해주기.
            if(item.getFollowing_status().equals("0")) { // 팔로우 안돼있으면
                user_following_status.setBackgroundResource(R.drawable.shape_rectangle_green_background);
                user_following_status.setText("팔로우 하기");
                user_following_status.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                user_following_status.setTag("not_following");
            } else { // 이미 팔로우 돼있음.
                user_following_status.setBackgroundResource(R.drawable.shape_rectangle_black_line);
                user_following_status.setText("팔로잉 중");
                user_following_status.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
                user_following_status.setTag("following");
            }
        } // set item 끝



        // 팔로우 버튼 클릭 시 처리하기
        public void bindFollowButton (FollowingListData item) {
            user_following_status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String statusTag = user_following_status.getTag().toString(); // 이게 뭐야
                    int userNumber = item.getUser_number(); // 팔로우할 사용자의 번호.

                    if ("not_following".equals(statusTag)){
                        Log.d("팔로우 - 팔로우버튼 누름", "팔로우 누름-흰색으로변함 " );
                        follow(user_number_int, userNumber,"following", ViewHolder.this );
                    } else {
                        Log.d("언팔 - 팔로우버튼 누름", "팔로우 누름-초록색으로변함 " );
                        follow(user_number_int, userNumber,"not_following", ViewHolder.this );
                    }
                }
            });
        }



    } // view holder 끝

    @NonNull
    @Override
    public FollowListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // context
        Context context = parent.getContext();



        // 쉐어드 - 로그인 정보 확인.
        pref = new PreferenceManager();
        current_login_memberInfo =   pref.getString(context, key);
        Log.d("게시판 댓글 어댑터", "로그인 회원정보from 쉐어드(string아님.):  " + current_login_memberInfo);

        try {
            JSONObject jsonObject = new JSONObject(current_login_memberInfo);
            nickname = jsonObject.optString("nickname", ""); // nullpoint exception 발생할 수 있으므로, 빈문자열로 두기
            emailId = jsonObject.optString("emailid", "");
            Log.d("my library whole 어댑터", "로그인 회원 이메일: " + emailId);
            get_userNumber(emailId);
        } catch (JSONException e) {
            e.printStackTrace();
            emailId = ""; // 기본값 설정
        }





        // 반환할 뷰
        View view;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.itemlist_follow_list, parent, false);
        FollowListAdapter.ViewHolder holder = new FollowListAdapter.ViewHolder(view);
        return holder;
    } // on create 끝


    // 4. bind view holder
    public void onBindViewHolder(@NonNull FollowListAdapter.ViewHolder holder, final int position) {

        // 1. 아이템 setting 하기
        FollowingListData item = followingListData.get(position);
        holder.setItem(item);
        holder.bindFollowButton(item);

        String userNumber = String.valueOf(item.getUser_number()); // 클릭하는 아이템에 대한 유저 넘버 가져옴?
        String userEmailId = item.getUser_emailId();
        String userNickname = item.getUser_nickname();

        // 클릭 이벤트 (상세 페이지로 이동하기)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("팔로우 목록 상세보기클릭", "상세보기진입 시도 ");
                if (userNickname !=null){
                    Log.d("팔로우 목록 상세보기클릭", "현재 유저 닉네임 존재함");
                    if (!emailId.equals(userEmailId)){
                        Intent intent = new Intent(context, OthersProfileActivity.class);
                        Log.d("팔로우리스트클릭이벤트- 다른 사람프로필임 ", "상세보기진입 시도 ");
                        intent.putExtra("writer_number", userNumber);
                        intent.putExtra("writer_nickname", userNickname);
                        intent.putExtra("writer_emailId", userEmailId);
                        context.startActivity(intent);
                } else {
                    Log.d("팔로우리스트클릭이벤트- 내 프로필임 ", "상세보기진입 시도 ");
                        // 본인 게시글의 본인 프로필을 눌렀을 경우.
                        // 메인 액티비티로 보내기. -> 마이페이지로 보내기
                        Intent intent1 = new Intent(context, MainActivity.class);
                        intent1.putExtra("fragment", "myProfile");
                        String writer_number_string = String.valueOf(userNumber);
                        intent1.putExtra("user_number", writer_number_string);
                        //  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        // 위 플래그를 설정하면 스택에 있는 기존 액티비티가 전부 제거됨.
                        context.startActivity(intent1);
//                    finish(); 이거 해줘야 하나 ??
                }
                } else {
                    Log.e("게시판상세-글쓴이 닉네임 없음", "글쓴이 닉네임 null 입니다.");
                    Toast.makeText(context.getApplicationContext(), "프로필로 이동할 수 없습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 팔로우 이벤트. 레트로핏 연결하기.




    } // onBindViewHolder 끝


    // 5. get item count
    @Override
    public int getItemCount() {
        return followingListData.size();
    }


    // 6. add item
    public void addItem (FollowingListData item) {
        followingListData.add(0,item);
        notifyDataSetChanged();
    }

    // 7. 아이템 전체 삭제
    public void clearItem() {
        followingListData.clear();
    }


    // 8. 특정 위치 아이템 가져오기
    public FollowingListData getItem (int position) {
        return followingListData.get(position);
    }


    // 9. 전체 삭제
    public void deleteItem(int position) {
        followingListData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, followingListData.size());
    }

    // 10. 팔로우, 언팔로우 하기
    public void follow (int user_number, int this_user_number, String status_tag, ViewHolder holder) {
        Log.e("팔로우 기능 서버전송 메소드진입", "로그인유저번호: " + user_number + ", 상대유저번호: " + this_user_number + ", 팔로우상태: " + status_tag);

        service.other_follow(user_number, this_user_number,status_tag).enqueue(new Callback<ProfileFollowResponse>() {
            @Override
            public void onResponse(Call<ProfileFollowResponse> call, Response<ProfileFollowResponse> response) {
                // 팔로우 수도 업데이트해야함. 음. 이건 어떻게 하는거지 ???
                // 처음 view 데이터를 뿌려줄때 가져온 다음에 기존 데이터에도 플러스 1을 해줌.
                Log.e("팔로우 통신성공! 서버가 보내는 코드", "code: " + response.code()); //서버가 보내는 http 통신 응답코드

                if (response.isSuccessful()) {
                    ProfileFollowResponse result = response.body();
                    Log.e("php 메시지", "message: " + result.getMessage()); //서버가 보내는 http 통신 응답코드
                    Log.e("php 현재 상태", "status: " + result.getStatus_now());

                    if (result.getStatus_now().equals("not_following")) {
                        holder.user_following_status.setBackgroundResource(R.drawable.shape_rectangle_green_background);
                        holder.user_following_status.setText("팔로우 하기");
                        holder.user_following_status.setTextColor(ContextCompat.getColor(context, R.color.white));
                        holder.user_following_status.setTag("not_following");
                    }  else if (result.getStatus_now().equals("following")) { // 팔로우 함
                        holder.user_following_status.setBackgroundResource(R.drawable.shape_rectangle_black_line);
                        holder.user_following_status.setText("팔로잉 중");
                        holder.user_following_status.setTextColor(ContextCompat.getColor(context, R.color.black));
                        holder.user_following_status.setTag("following");
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileFollowResponse> call, Throwable throwable) {
                throwable.printStackTrace(); // 스택 트레이스를 출력하여 오류 원인을 확인
                Log.e("다른 유저 팔로우 실패 원인. ", "onFailure: " + throwable.getMessage());
                throwable.getMessage();
                Log.e("다른 유저 팔로우 실패. ", "onFailure: " + throwable.getCause());
            }
        });

    } // follow 끝


    //    // 11. 현재 유저 번호 가져오기.
    public void get_userNumber (String user_emailid) {
        Log.i("게시판상세- 유저넘버가져오기", "유저의 emailId: " + user_emailid);
        service.user_emailId(user_emailid).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Log.e("게시판상세-유저 number 체크 response 진입", "onResponse: 응답메시지: " + result.getMessage()); // echo로 보내주는 메시지.
                Log.e("게시판상세-유저 number 체크 response 진입", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.

                if(result.getCode() == 200 ) {
                    Log.e("유저 number 성공 첫번째증거", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                    user_number = result.getUser_number();
                    user_number_int = Integer.parseInt(user_number);
//                    callback.onUserNumberReceived(result.getUser_number()); // 콜백 메소드 실행 !!
                } else {
                    Log.e("php에서 뭔가 잘못됨. / code==400!!", "onResponse: 유저넘버: " + result.getUser_number()); // echo로 보내주는 메시지.
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Log.e("게시판상세-유저넘버찾기 통신 failed", "실패원인: " +throwable.getMessage());
            }
        });
    } // 유저 번호


    // = =================================== 유저 넘버 가져오는 레트로핏에 대한 call back 인터페이스 메소드 !!


} // 어댑터 끝.
