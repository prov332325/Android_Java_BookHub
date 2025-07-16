<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../php/db_connection.php");


//현재 로그인한 유저의 이메일 닉네임 
if (isset($_POST['emailId'])) {
    $emailId = $_POST['emailId'];
} else {
    $emailId = null;
}

// 회원번호 변수 초기화 
$current_user_number = null;

// 응답 
$response = array();

// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$select_success_msg = "select 완성";
$select_error_msg = "select 실행 오류";





if ($emailId == '') {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    error_log(json_last_error_msg());
} else {

    // 가져와야 하는 것. 
    // 현재 유저 정보를 가져와서 채팅방 목록 가져오기. 
    $user_number_stmt = $conn->prepare("SELECT USER_NUMBER FROM USERS WHERE USER_EMAIL_ID= ?");
    $user_number_stmt->bind_param("s", $emailId);
    if ($user_number_stmt->execute()) {
        $user_number_result = $user_number_stmt->get_result();
        if ($user_number_result->num_rows > 0) {
            while ($row = $user_number_result->fetch_assoc()) {
                $current_user_number = $row['USER_NUMBER'];
            }

            // 채팅방 목록가져오기 쿼리 실행 
            $chatting_roomList_stmt = $conn->prepare("SELECT
    CR.ROOMS_ID,
    CR.LAST_MESSAGE_TEXT,
    CR.LAST_MESSAGE_TIME,
    CR.USER_NUMBER1,
    CR.USER_NUMBER2,
    CASE
        WHEN CR.USER_NUMBER1 = ? THEN U2.USER_NICKNAME
        ELSE U1.USER_NICKNAME
    END AS other_user_nickname,
    CASE
        WHEN CR.USER_NUMBER1 = ? THEN U2.USER_PROFILE_IMG
        ELSE U1.USER_PROFILE_IMG
    END AS other_user_profile_img,
    CASE
        WHEN CR.USER_NUMBER1 = ? THEN U2.USER_NUMBER
        ELSE U1.USER_NUMBER
    END AS other_user_number,
    CASE
        WHEN CR.USER_NUMBER1 = ? THEN U2.USER_EMAIL_ID
        ELSE U1.USER_EMAIL_ID
    END AS other_user_emailId,
    COALESCE(UM.UNREAD_COUNT, 0) AS unread_message_count
FROM
    CHAT_ROOMS CR
    LEFT JOIN USERS U1 ON CR.USER_NUMBER1 = U1.USER_NUMBER
    LEFT JOIN USERS U2 ON CR.USER_NUMBER2 = U2.USER_NUMBER
    LEFT JOIN (
        SELECT
            ROOMS_ID,
            COUNT(*) AS UNREAD_COUNT
        FROM
            CHAT_MESSAGES
        WHERE
            IS_READ = 0
            AND RECEIVER_ID = ?
        GROUP BY
            ROOMS_ID
    ) UM ON CR.ROOMS_ID = UM.ROOMS_ID
WHERE
    CR.USER_NUMBER1 = ? OR CR.USER_NUMBER2 = ?
ORDER BY
    CR.LAST_MESSAGE_TIME DESC");

            $chatting_roomList_stmt->bind_param("iiiiiii", $current_user_number, $current_user_number, $current_user_number, $current_user_number, $current_user_number, $current_user_number, $current_user_number);

            if ($chatting_roomList_stmt->execute()) {
                $list_result = $chatting_roomList_stmt->get_result();
                if ($list_result->num_rows > 0) {
                    $response['code'] = 200;
                    $response['message'] = "기존 채팅내역 존재합니다!";
                    $response['my_user_number'] = $current_user_number;
                    $response['chat_room_items'] = array();

                    while ($list_row = $list_result->fetch_assoc()) {
                        $other_user_number = $list_row['other_user_number'];

                        // 맞팔 여부를 확인하는 쿼리
                        $following_check_stmt = $conn->prepare(
                            "SELECT COUNT(*) AS follow_count 
                             FROM USERS_FOLLOW_LIST 
                             WHERE 
                                (FOLLOW_FROM_USER = ? AND FOLLOW_TO_USER = ? AND FOLLOW_STATUS = 1) 
                                OR 
                                (FOLLOW_FROM_USER = ? AND FOLLOW_TO_USER = ? AND FOLLOW_STATUS = 1)"
                        );
                        $following_check_stmt->bind_param("iiii", $other_user_number, $current_user_number, $current_user_number, $other_user_number); 
            
                        if ($following_check_stmt->execute()) {
                            $following_check_stmt->store_result(); 
                            $following_check_stmt->bind_result($follow_cnt);
                            $following_check_stmt->fetch();
                            
                            // 디버깅을 위해 쿼리 결과를 로그에 기록
                            error_log("Follow Count: " . $follow_cnt);

                            $is_following_each_other = 200; // 기본값 설정
                            if ($follow_cnt == 2) {
                               $is_following_each_other = 1; // 맞팔
                            } else {
                                $is_following_each_other = 2; // 맞팔 아님
                            }
                        } else {
                            // 쿼리 오류를 로그에 기록
                            error_log("Query Error: " . $conn->error);
                        }

                        $data = array(
                            'chat_room_number' => $list_row['ROOMS_ID'],
                            'last_sent_message' => $list_row['LAST_MESSAGE_TEXT'],
                            'last_sent_time' => $list_row['LAST_MESSAGE_TIME'],
                            'user_number' => $list_row['other_user_number'], // 상대 유저 번호 
                            'user_emailId' => $list_row['other_user_emailId'],
                            'user_nickname' => $list_row['other_user_nickname'], // 상대 유저 닉네임
                            'profile_img' => $list_row['other_user_profile_img'], // 상대 유저 프로필 사진
                            'unread_cnt' => $list_row['unread_message_count'],
                            'follow_each_other' => $is_following_each_other // 맞팔 여부 추가
                        );
                        $response['chat_room_items'][] = $data;
                    } // while 문 끝 

                } else { // 채팅방 없음. 
                    // code 200, 기존 채팅 내역 없음, 현재 로그인 유저 번호만 넘겨주기
                    $response['code'] = 200;
                    $response['message'] = "기존 채팅 내역없음!";
                    $response['my_user_number'] = $current_user_number;
                    error_log(json_last_error_msg());
                }
            } else { // 채팅방 조회 쿼리 실행 실패 
                $response['code'] = 400;
                $response['message'] = $select_error_msg . ", 채팅방 조회 쿼리 실패";
                error_log(json_last_error_msg());
                exit;
            }
        } else { // 유저 정보 조회 결과 없음 
            $response['code'] = 400;
            $response['message'] = "유저 번호가 없음";
            error_log(json_last_error_msg());
            exit;
        }
    } else { // 유저 정보 조회 쿼리 실행 실패 
        $response['code'] = 400;
        $response['message'] = $select_error_msg . ", 유저 정보 조회쿼리 실패";
        error_log(json_last_error_msg());
        exit;
    }
} // 필수 인자 다 있을때 !! 끝 

echo json_encode($response, JSON_UNESCAPED_UNICODE);
