package com.example.myapplication.data.retrofit;
import com.example.myapplication.data.retrofit.responsemodel.AladinResponse;
import com.example.myapplication.data.retrofit.responsemodel.BoardCommentResponse;
import com.example.myapplication.data.retrofit.responsemodel.BoardLikeResponse;
import com.example.myapplication.data.retrofit.responsemodel.BoardListResponse;
import com.example.myapplication.data.retrofit.responsemodel.BoardResponse;
import com.example.myapplication.data.retrofit.responsemodel.ChattingResponse;
import com.example.myapplication.data.retrofit.responsemodel.ChattingRoomResponse;
import com.example.myapplication.data.retrofit.responsemodel.ImageUploadResponse;
import com.example.myapplication.data.retrofit.responsemodel.KakaoJoinResponse;
import com.example.myapplication.data.retrofit.responsemodel.LoginResponse;
import com.example.myapplication.data.retrofit.datamodel.KakaoJoinData;
import com.example.myapplication.data.retrofit.responsemodel.MyLibraryListResponse;
import com.example.myapplication.data.retrofit.responsemodel.MylibraryResponse;
import com.example.myapplication.data.retrofit.responsemodel.ProfileFollowResponse;
import com.example.myapplication.data.retrofit.responsemodel.ProfileViewResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RetrofitService {
    // 로그인
    @FormUrlEncoded
    @POST("/php/android_login.php")
    Call<LoginResponse> userLogin(
            @Field("email") String email,
            @Field("password") String password
    );


    // 소셜로그인 유저정보DB저장 (카카오)
    // @FormUrlEncoded
//    @POST("/php/android_kakaoJoin.php")
//    Call<KakaoJoinResponse> kakaoJoin (
//            @Field("kakao_id") String kakao_id,
//            @Field("kakao_email") String kakao_email,
//            @Field("kakao_nickname") String kakao_nickname,
//            @Field("kakao_imgUrl") String kakao_imgUrl
//            );
    @POST("/php/android_kakaoJoin.php")
    Call<KakaoJoinResponse> kakaoJoin(@Body KakaoJoinData kakaoJoinData);


    // 앱 진입 시, 자동로그인 (쉐어드) 설정한 id(카카오) 혹은 email이 DB와 일치하는지 확인
    @FormUrlEncoded
    @POST("/php/android_autoSigninCheck.php")
    Call<LoginResponse> autoSignin (
            @Field("emailId") String emailId,
            @Field("firebase_token") String firebase_token
            );


    // 알라딘 책 검색 api
    @GET("ItemSearch.aspx")
    Call<AladinResponse.AladinResponse2> getSearchBook(
            @Query("ttbkey") String ttbkey,
            @Query("Query") String Query,
            @Query("QueryType") String queryType,
            @Query("MaxResults") int maxResults,
            @Query("start") int start,
            @Query("SearchTarget") String searchTarget,
            @Query("output") String output,
            @Query("Version") String version
    );



    // ================== My Libraary ================================


    // 유저의 number 가져오기.
    @FormUrlEncoded
    @POST("/php/android_check_emailId.php")
    Call<LoginResponse> user_emailId (
            @Field("emailId") String emailId
    );



    // 읽은 책 저장
    @FormUrlEncoded
    @POST("/php/mylibrary/mylibrary_already_insert.php")
    Call<MylibraryResponse> mylibrary_already (
            @Field("user_number") String userNumber,
            @Field("title") String title,
            @Field("author") String author,
            @Field("description") String description,
            @Field("publisher") String publisher,
            @Field("pubDate") String pubDate,
            @Field("cover") String cover,
            @Field("isbn") String isbn,
            @Field("started_date") String started_date,
            @Field("finished_date") String finished_date,
            @Field("already_rating") String already_rating,
            @Field("book_save_type") String book_save_type
            );


    // 읽은 책 저장22
    @Multipart
    @POST("/php/mylibrary/mylibrary_already_insert2.php")
    Call<MylibraryResponse> mylibrary_already2 (
            @Part("user_number") RequestBody userNumber,
            @Part("title") RequestBody title,
            @Part("author") RequestBody author,
            @Part("description") RequestBody description,
            @Part("publisher") RequestBody publisher,
            @Part("pubDate") RequestBody pubDate,
            @Part MultipartBody.Part cover, // 표지 이미지를 파일로 전송
            @Part("isbn") RequestBody isbn,
            @Part("started_date") RequestBody startedDate,
            @Part("finished_date") RequestBody finishedDate,
            @Part("already_rating") RequestBody alreadyRating,
            @Part("book_save_type") RequestBody book_save_type
    );


    // 읽고 있는 책 저장
    @FormUrlEncoded
    @POST("/php/mylibrary/mylibrary_reading_insert.php")
    Call<MylibraryResponse> mylibrary_reading (
            @Field("user_number") String userNumber,
            @Field("title") String title,
            @Field("author") String author,
            @Field("description") String description,
            @Field("publisher") String publisher,
            @Field("pubDate") String pubDate,
            @Field("cover") String cover,
            @Field("isbn") String isbn,
            @Field("read_page") String read_page,
            @Field("started_date") String started_date
    );


    // 3) 읽고싶은 책 저장
    @FormUrlEncoded
    @POST("/php/mylibrary/mylibrary_want_insert.php")
    Call<MylibraryResponse> mylibrary_want (
            @Field("user_number") String userNumber,
            @Field("title") String title,
            @Field("author") String author,
            @Field("description") String description,
            @Field("publisher") String publisher,
            @Field("pubDate") String pubDate,
            @Field("cover") String cover,
            @Field("isbn") String isbn,
            @Field("want_rating") String want_rating,
            @Field("want_preview") String want_preview
    );


    // 내 서재 전체 목록 가져오기 - 파라미터는 1개임!
    @FormUrlEncoded
    @POST("/php/mylibrary/mylibraryList_whole.php")
    Call<MyLibraryListResponse.MyLibraryResponse2> mylibrary_wholesetting (
            @Field("user_number") String user_number
    );




    // 내 서재 <읽은 책 >
    @FormUrlEncoded
    @POST("/php/mylibrary/mylibraryList_already.php")
    Call<MyLibraryListResponse.MyLibraryResponse2> mylibrary_alreadysetting (
            @Field("user_number") String user_number
    );


    // 내 서재 <읽고 있는 책 >
    @FormUrlEncoded
    @POST("/php/mylibrary/mylibraryList_reading.php")
    Call<MyLibraryListResponse.MyLibraryResponse2> mylibrary_readingsetting (
            @Field("user_number") String user_number
    );


    // 내 서재 <읽고 싶은 책 >
    @FormUrlEncoded
    @POST("/php/mylibrary/mylibraryList_want.php")
    Call<MyLibraryListResponse.MyLibraryResponse2> mylibrary_wantsetting (
            @Field("user_number") String user_number
    );

    // 내 서재 < 상세보기 setting !!!  전체, 읽은책, 읽고 있는책, 읽고 싶은책 에서 다 씀. >
    @FormUrlEncoded
    @POST("/php/mylibrary/mylibraryList_view.php")
    Call<MyLibraryListResponse> mylibrary_view (
            @Field("user_number") String user_number,
             @Field("book_number") int book_number,
            @Field("mylibrary_number") int mylibrary_number,
            @Field("type") String type
    );


    // 내 서재 <삭제하기>
    @FormUrlEncoded
    @POST("/php/mylibrary/mylibrary_delete.php")
    Call<MylibraryResponse> mylibrary_delete (
            @Field("user_number") String user_number,
            @Field("book_number") int book_number,
            @Field("mylibrary_number") int mylibrary_number,
            @Field("type") String type
    );


    // 내 서재 수정하기, 수정 전 내용 가져오기
    @FormUrlEncoded
    @POST("/php/mylibrary/mylibrary_before_modi.php")
    Call<MyLibraryListResponse> mylibrary_before_modi (
            @Field("user_number") String user_number,
            @Field("book_number") int book_number,
            @Field("mylibrary_number") int mylibrary_number,
            @Field("type") String type
    );


    // 내 서재 수정 - 1) 읽은 책으로 수정
    @FormUrlEncoded
    @POST("/php/mylibrary/mylibrary_modi.php")
    Call<MylibraryResponse> already_modi (
            @Field("user_number") String user_number,
            @Field("book_number") int book_number,
            @Field("before_library_number") int mylibrary_number,
            @Field("before_type") String before_type,
            @Field("modi_type") String modi_type,
            @Field("modi_started_date") String started_date,
            @Field("modi_finished_date") String finished_date,
            @Field("modi_already_rating") String already_rating
    );



    // 내 서재 수정 - 2) 읽고 있는 책으로 수정
    @FormUrlEncoded
    @POST("/php/mylibrary/mylibrary_modi.php")
    Call<MylibraryResponse> mylibrary_reading_modi (
            @Field("user_number") String user_number,
            @Field("book_number") int book_number,
            @Field("before_library_number") int mylibrary_number,
            @Field("before_type") String before_type,
            @Field("modi_type") String modi_type,
            @Field("modi_started_date") String started_date,
            @Field("modi_read_page") String read_page
    );


    // 내 서재 수정 - 3) 읽고 싶은 책으로 수정

    @FormUrlEncoded
    @POST("/php/mylibrary/mylibrary_modi.php")
    Call<MylibraryResponse> mylibrary_want_modi (
            @Field("user_number") String user_number,
            @Field("book_number") int book_number,
            @Field("before_library_number") int mylibrary_number,
            @Field("before_type") String before_type,
            @Field("modi_type") String modi_type,
            @Field("modi_want_rating") String want_rating,
            @Field("modi_want_preview") String want_preview
    );


    // 게시판 저장
    @FormUrlEncoded
    @POST("/php/board/board_insert.php")
    Call<BoardResponse> board_insert (
            @Field("user_number") String user_number,
            @Field("board_title") String board_title,
            @Field("board_content") String board_content,
            @Field("board_category") String board_category,
            @Field("books") String booksJson // 책 목록을 JSON 문자열로 전송
    );


    // 게시판 전체 목록 가져오기. - 유저 넘버
    @FormUrlEncoded
    @POST("/php/board/boardList_whole.php")
    Call<BoardListResponse.BoardResponse2> boardList_whole (
            @Field("user_number") String user_number);



    // 게시판 전체 목록 5개만 가져오기. - 유저 넘버
    @FormUrlEncoded
    @POST("/php/board/boardList_whole_paging.php")
    Call<BoardListResponse.BoardResponse2> boardList_whole_paging (
            @Field("user_number") String user_number);




    // 게시판 책추천 가져오기. - 유저 넘버
    @FormUrlEncoded
    @POST("/php/board/boardList_recommend.php")
    Call<BoardListResponse.BoardResponse2> boardList_recommend (
            @Field("user_number") String user_number);


    // 게시판 잡담 가져오기. - 유저 넘버
    @FormUrlEncoded
    @POST("/php/board/boardList_chat.php")
    Call<BoardListResponse.BoardResponse2> boardList_chat (
            @Field("user_number") String user_number);


    // 게시판 가입인사 가져오기. - 유저 넘버
    @FormUrlEncoded
    @POST("/php/board/boardList_welcome.php")
    Call<BoardListResponse.BoardResponse2> boardList_welcome (
            @Field("user_number") String user_number);


    // 게시판 상세보기, 수정하기 - 유저번호, 게시글번호, 카테고리
    @FormUrlEncoded
    @POST("/php/board/board_view.php")
    Call<BoardListResponse> board_view (
            @Field("user_number") String user_number,
            @Field("board_number") int board_number,
            @Field("category") String category
    );


    // 게시판 삭제하기 - 유저번호, 게시글 번호, 카테고리
    @FormUrlEncoded
    @POST("/php/board/board_delete.php")
    Call<BoardResponse> board_delete (
            @Field("user_number") String user_number,
            @Field("board_number") int board_number,
            @Field("category") String category
    );

    // 게시판 수정하기
    @FormUrlEncoded
    @POST("/php/board/board_modi.php")
    Call<BoardResponse> board_modi (
            @Field("user_number") String user_number,
            @Field("board_number") int board_number,
            @Field("edited_title") String edited_title,
            @Field("edited_content") String edited_content,
            @Field("edited_category") String category,
            @Field("books") String booksJson // 책 목록을 JSON 문자열로 전송

    );


    // 게시판 이미지를 서버에 업로드
    @Multipart
    @POST("/php/board/board_image.php")
    Call<ImageUploadResponse> uploadImage(
            @Part MultipartBody.Part image,
            @Part("board_number")RequestBody boardNumber,
            @Part("user_number")RequestBody user_number
            );


    // 게시판 이미지 가져오기
    @FormUrlEncoded
    @POST("/php/board/boardView_image.php")
    Call<ImageUploadResponse.ImageUploadResponse2> boardView_image (
            @Field("user_number") String user_number,
            @Field("board_number") int board_number

    );


    // 게시판 이미지를 서버에서 업데이트 하기
    @Multipart
    @POST("/php/board/board_imageUpdate.php")
    Call<ImageUploadResponse>  UpdateImage(
            @Part("board_number")RequestBody boardNumber,
            @Part("user_number")RequestBody user_number,
            @Part List<MultipartBody.Part> images,
            @Part ("imgUrls[]") List<RequestBody> imgUrls
            );

    // 어노테이션에서 name을 지정하지 않으면 변수명, images, imgUrls 를 그대로 사용하여
    // 요청 파라미터의 이름으로 매핑한다.
    // List 여도 각 항목이 개별적인 part 로 전송되기 때문에 [] 배열을 표시하지 않아도 됨.



    // 내 정보, 다른 유저의 정보 가져오기 - Others profile activity
    @FormUrlEncoded
    @POST("/php/profile/others_profile_info.php")
    Call<ProfileViewResponse> other_profile_info (
            @Field("user_number") int user_number,
            @Field("this_user_number") int this_users_number,
            @Field("this_user_nickname") String this_users_nickname
    );


    // 팔로우 언팔로우
    @FormUrlEncoded
    @POST("/php/profile/other_follow.php")
    Call<ProfileFollowResponse> other_follow (
            @Field("user_number") int users_number,
            @Field("this_user_number") int this_users_number,
            @Field("status_tag") String status_tag
    );



    // 댓글 작성 완료
    @FormUrlEncoded
    @POST("/php/board/comment_insert.php")
    Call<BoardCommentResponse> board_comment_insert (
            @Field("user_number") int users_number,
            @Field("board_number") int board_number,
            @Field("comment_content") String comment_content
    );


    // 댓글 목록 - board view setting 시.. -게시글 상세내용 가져올때 list 로 가져옴 !!


    // 댓글 수정 완료
    @FormUrlEncoded
    @POST("/php/board/comment_update.php")
    Call<BoardCommentResponse> board_comment_update (
            @Field("user_number") int user_number,
            @Field("comment_number") int comment_number,
            @Field("edit_comment_content") String edit_comment_content
    );


    // 댓글 삭제 완료
    @FormUrlEncoded
    @POST("/php/board/comment_delete.php")
    Call<BoardCommentResponse> board_comment_delete (
            @Field("user_number") int user_number,
            @Field("comment_number") int comment_number
    );


    // 게시글 좋아요
    @FormUrlEncoded
    @POST("/php/board/board_like.php")
    Call<BoardLikeResponse> board_like (
            @Field("user_number") int users_number,
            @Field("board_number") int board_number,
            @Field("status_tag") String status_tag
    );


    // 채팅방에서 상대 유저 정보 가져오기
    @FormUrlEncoded
    @POST("/php/chatting_this_userInfo.php")
    Call<ChattingResponse> chatting_room_record (
            @Field("login_user_number") int login_user_number,
            @Field("this_user_number") int this_user_number
    );


    // 유저 번호랑, 채팅방 목록 가져오기
    @FormUrlEncoded
    @POST("/php/chatting_list.php")
    Call<ChattingRoomResponse> get_chatting_list (
            @Field("emailId") String emailId
    );



    // 채팅목록에서 새로 생성되는 유저 이미지 가져오기
    @FormUrlEncoded
    @POST("/php/chatting_list_img.php")
    Call<ChattingRoomResponse> get_user_img (
            @Field("sender_number") int sender_number
    );


    // fcm token update
    // LoginResponse 로 하는 이유는 없음. 안에 내용이 간결하기에 해봤다.
    @FormUrlEncoded
    @POST("/php/updateToken.php")
    Call<LoginResponse> updateToken (
            @Field("emailId") String emailId,
            @Field("firebase_token") String firebase_token
    );


    @FormUrlEncoded
    @POST("/php/profile/edit_nickname_check.php")
    Call<LoginResponse> edit_nickname_check (
            @Field("user_number") int user_number,
            @Field("edited_user_nickname") String edited_user_nickname
    );


    @FormUrlEncoded
    @POST("/php/profile/edit_nick_bio.php")
    Call<LoginResponse> profile_edit (
            @Field("user_number") int user_number,
            @Field("edited_nickname") String edited_nickname,
            @Field("edited_bio") String edited_bio
    );



    // 프로필 사진을 서버에 업로드
    @Multipart
    @POST("/php/profile/profile_image.php")
    Call<ImageUploadResponse> uploadProfileImage(
            @Part MultipartBody.Part image,
            @Part("user_number")RequestBody user_number,
            @Part("isDefaultImage")RequestBody isDefaultImage
    );



// 팔로워 팔로잉 목록 가져오기
    @FormUrlEncoded
    @POST("/php/profile/follower_following_list.php")
    Call<ProfileViewResponse.ProfileViewResponse2> follower_following_list (
            @Field("user_number") int user_number,
            @Field("type") String type // 팔로잉인지, 팔로우인지
    );



    // 프로필에서 작성자가 작성한 게시글 가져오기
    @FormUrlEncoded
    @POST("/php/profile/profile_board_list.php")
    Call<BoardListResponse.BoardResponse2> profile_board_list (
            @Field("user_number") int user_number
            );



    // 채팅방 페이징
    @FormUrlEncoded
    @POST("/php/chatting_loadmore.php")
    Call<ChattingResponse> loadMoreMessage (
            @Field("room_id") int room_id,
            @Field("first_message_number") int first_message_number
    );


    // 탈퇴
    @FormUrlEncoded
    @POST("/php/anroid_withdraw.php")
    Call<LoginResponse> withdraw (
            @Field("user_number") int user_number
    );


    // 비밀번호 찾기
    // 이메일 중복체크 및 이메일 인증번호 보내기
    @FormUrlEncoded
    @POST("/php/android_passwordchange_emailVerify.php")
    Call<LoginResponse> emailDupleCheckAndVerify (
            @Field("user_email") String user_email
    );

    // 이메일 인증번호 확인
    @FormUrlEncoded
    @POST("/php/android_passwordchange_emailVerify2.php")
    Call<LoginResponse> emailVerifyCheck (
            @Field("user_email") String user_email,
            @Field("verify_code") String verify_code
            );



    // 비번 변경하기.
    @FormUrlEncoded
    @POST("/php/android_passwordchange.php")
    Call<LoginResponse> password_change (
            @Field("new_password1") String new_password1,
            @Field("new_password2") String new_password2,
            @Field("user_email") String user_email
            );


} // 인터페이스 끝

