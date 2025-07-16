<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");


// 현재 유저 번호 
if (isset($_POST['user_number'])) { // int 로 넘김.
    $user_number = $_POST['user_number'];
} else {
    $user_number = null;
}



// 다른 유저 번호 
if (isset($_POST['this_user_number'])) { // int 로 넘김.
    $this_user_number = $_POST['this_user_number'];
} else {
    $this_user_number = null;
}

// 다른 유저 닉네임  
if (isset($_POST['this_user_nickname'])) { // String 으로 넘김. 
    $this_user_nickname = $_POST['this_user_nickname'];
} else {
    $this_user_nickname = null;
}

// 응답 
$response = array();


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$select_success_msg = "select 완성";
$select_error_msg = "select 실행 오류";



// 우선은 닉네임, 유저 번호, 읽은 책 읽고 있는책, 읽고 싶은 책의 수? 만 가져가자. ?


if ($this_user_number == '' || $this_user_nickname == '') {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit;
} else { // 필수 인자가 다 있는 경우 

    // 유저 정보 가져오기 
    $this_user_stmt = $conn->prepare("SELECT u.USER_NUMBER, u.USER_EMAIL_ID, u.USER_NICKNAME, u.USER_PROFILE_IMG, u.USER_PROFILE_BIO,
(SELECT COUNT(*) FROM BOOK_ALREADY a WHERE BOOK_ALREADY_USER_NUMBER = u.USER_NUMBER) as count_already, 
(SELECT COUNT(*) FROM BOOK_READING WHERE BOOK_READING_USER_NUMBER = u.USER_NUMBER) AS count_reading,
(SELECT COUNT(*) FROM BOOK_WANT WHERE BOOK_WANT_USER_NUMBER = u.USER_NUMBER) AS count_want
FROM USERS u WHERE u.USER_NUMBER = ? AND u.USER_NICKNAME = ?");
    $this_user_stmt->bind_param("is", $this_user_number, $this_user_nickname);

    if ($this_user_stmt->execute()) { // 다른 유저 정보 select문 성공 
        $result = $this_user_stmt->get_result(); // result 는 배열이다. 
        if ($result->num_rows > 0) {
            while ($row = $result->fetch_assoc()) {
                $response['code'] = 200;
                $response['message'] = $select_success_msg;
                $response['user_number'] = $row['USER_NUMBER'];
                $response['user_emailId'] = $row['USER_EMAIL_ID'];
                $response['user_nickname'] = $row['USER_NICKNAME'];
                $response['profile_img'] = $row['USER_PROFILE_IMG'];
                $response['profile_bio'] = $row['USER_PROFILE_BIO'];
                $response['user_readBook_cnt'] = $row['count_already'];
                $response['user_readingBook_cnt'] = $row['count_reading'];
                $response['user_wantBook_cnt'] = $row['count_want'];
            } // while문 끝 


            // 현재 내 프로필 진입이 아니라, 다른 유저 프로필 진입일때만 팔로우 상태 가져가기 
            if ($user_number != $this_user_number) {
                     // 팔로우 상태 확인하기. 기존 값이 존재할 경우에만. 
            $follow_check_stmt = $conn->prepare("SELECT FOLLOW_STATUS FROM USERS_FOLLOW_LIST 
            WHERE FOLLOW_FROM_USER = ? AND FOLLOW_TO_USER = ?");
            $follow_check_stmt->bind_param("ii", $user_number, $this_user_number);

            if ($follow_check_stmt->execute()) {
                $result = $follow_check_stmt->get_result();
                if ($result->num_rows > 0) {
                    $row = $result->fetch_assoc();
                    $db_status = (string) $row['FOLLOW_STATUS'];
                    $response['follow_status_now'] = $db_status;
                }
            }

            // 다른 유저 프로필일때 서로 맞팔인지도 확인하기. 
            $following_check_stmt = $conn->prepare(
                "SELECT COUNT(*) AS follow_count 
                 FROM USERS_FOLLOW_LIST 
                 WHERE 
                    (FOLLOW_FROM_USER = ? AND FOLLOW_TO_USER = ? AND FOLLOW_STATUS = 1) 
                    OR 
                    (FOLLOW_FROM_USER = ? AND FOLLOW_TO_USER = ?  AND FOLLOW_STATUS = 1)"
            );
            $following_check_stmt->bind_param("iiii", $user_number, $this_user_number, $this_user_number, $user_number); 

            if($following_check_stmt->execute()) {
                $following_check_stmt->store_result(); 
                $following_check_stmt->bind_result($follow_cnt);
                $following_check_stmt->fetch();

                // 맞팔인 경우 
                if($follow_cnt == 2) {
                    $response['follow_each_other'] = 1; 
                } else {
                    $response['follow_each_other'] = 2; 
                }
            } else {
                error_log(json_last_error_msg($_POST));
            }


            }
       


            // 현재 보고 있는 유저의 팔로우 수, 팔로잉 수 가져가기  
            // this user가 팔로우 하는 수(from 유저가 this이면서, status가 1,true인경우), this user를 팔로우 하는 수 (to user가 this이면서 status가 true인 경우) 
            $follow_cnt_stmt = $conn->prepare("SELECT 
                    (SELECT count(*) FROM USERS_FOLLOW_LIST WHERE FOLLOW_FROM_USER = ? AND FOLLOW_STATUS = true) AS follow_cnt, 
                    (SELECT count(*) FROM USERS_FOLLOW_LIST WHERE FOLLOW_TO_USER = ? AND FOLLOW_STATUS = true ) AS follower_cnt"); 
            $follow_cnt_stmt->bind_param("ii", $this_user_number, $this_user_number); 
            if ($follow_cnt_stmt->execute()) { 
                $result = $follow_cnt_stmt->get_result();
                $row = $result -> fetch_assoc();
                $response['following_cnt'] = $row['follow_cnt'];
                $response['follower_cnt'] = $row['follower_cnt']; 

            }
        } // 결과값 다 있는 경우 
    }
} // 필수 인자가 다 있는 경우  

echo json_encode($response, JSON_UNESCAPED_UNICODE);
