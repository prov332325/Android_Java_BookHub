<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../php/db_connection.php");


//현재 로그인한 유저
if (isset($_POST['login_user_number'])) { // int 로 넘김.
    $login_user_number = $_POST['login_user_number'];
} else {
    $login_user_number = null;
}

// 상대 유저 
if (isset($_POST['this_user_number'])) { // int 로 넘김.
    $this_user_number = intval($_POST['this_user_number']);
} else {
    $this_user_number = null;
}



// 응답 
$response = array();


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$select_success_msg = "select 완성";
$select_error_msg = "select 실행 오류";

// 가져와야 하는 정보... 음 
// 유저 번호, 유저 닉네임, 유저 이미지 
if ($this_user_number == '' || $login_user_number == '') {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    error_log(json_last_error_msg($_POST));
} else {

    $user_info_stmt = $conn->prepare("SELECT USER_NUMBER, USER_NICKNAME, USER_PROFILE_IMG FROM USERS WHERE USER_NUMBER = ?");
    $user_info_stmt->bind_param("i", $this_user_number);

    if ($user_info_stmt->execute()) { // 상대 유저 정보 가져오기 성공 
        $result = $user_info_stmt->get_result();
        if ($result->num_rows > 0) {
            while ($row = $result->fetch_assoc()) {
                // $response['code'] = 200; // response code 세팅 
                // $response['message'] = $select_success_msg; // message 
                $response['user_number'] = $row['USER_NUMBER'];
                $response['user_nickname'] = $row['USER_NICKNAME'];
                $response['profile_img'] =  $row['USER_PROFILE_IMG'];
            } //while 끝 


            // 상대랑 나랑 맞팔 관계인지 !! 
            // 2개 조건에 맞는 행의 개수를 가져온다. 2개여야만 참, 아니면 맞팔이 아님.. 
            $following_check_stmt = $conn->prepare(
                "SELECT COUNT(*) AS follow_count 
                 FROM USERS_FOLLOW_LIST 
                 WHERE 
                    (FOLLOW_FROM_USER = ? AND FOLLOW_TO_USER = ? AND FOLLOW_STATUS = 1) 
                    OR 
                    (FOLLOW_FROM_USER = ? AND FOLLOW_TO_USER = ? AND FOLLOW_STATUS = 1)"
            );
            $following_check_stmt->bind_param("iiii", $login_user_number, $this_user_number, $this_user_number, $login_user_number); 

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

            // 채팅방이 있는지 ? 있으면 기존 채팅 내용가져가기 
            // 없으면 code, message, 맞팔 여부까지 !! 추가해서 나가면 됨.  
            $room_exist_check_stmt = $conn->prepare("SELECT ROOMS_ID FROM CHAT_ROOMS 
            WHERE LEAST(USER_NUMBER1, USER_NUMBER2 ) = LEAST(?, ?) 
            AND GREATEST(USER_NUMBER1, USER_NUMBER2 ) = GREATEST(?, ?)");
            $room_exist_check_stmt->bind_param("iiii", $login_user_number, $this_user_number, $login_user_number, $this_user_number);

            if ($room_exist_check_stmt->execute()) { // 방 존재 체크 쿼리 실행 성공 

                // 존재 여부 확인하기 
                $room_exist_check_stmt->store_result();

                if ($room_exist_check_stmt->num_rows > 0) {
                    // 기존 방이 존재함. 
                    // 방번호 가져오기  

                    $room_exist_check_stmt->bind_result($exist_room_number);
                    $room_exist_check_stmt->fetch();

                    // 이전 대화 내용 가져오기. 
                    $chat_data_stmt = $conn->prepare("SELECT * FROM CHAT_MESSAGES WHERE ROOMS_ID = ? ORDER BY MESSAGE_SENT_TIME DESC LIMIT 20");
                    $chat_data_stmt->bind_param("i", $exist_room_number);

                    if ($chat_data_stmt->execute()) {
                        $chat_data_result = $chat_data_stmt->get_result();

                        if ($chat_data_result->num_rows > 0) {
                            $response['code'] = 200; // response code 세팅 
                            $response['message'] = $select_success_msg; // message  
                            $response['chat_room_number'] = (String) $exist_room_number;  
                            // 맞팔 여부.  
                            $response['chat_items'] = array();

                            while ($row = $chat_data_result->fetch_assoc()) {
                                $data = array(
                                    'message_id' => $row['MESSAGE_ID'],
                                    'rooms_id' => $row['ROOMS_ID'],
                                    'sender_id' => $row['SENDER_ID'],
                                    'receiver_id' => $row['RECEIVER_ID'],
                                    'message_content' => $row['MESSAGE_CONTENT'],
                                    'message_sent_time' => $row['MESSAGE_SENT_TIME'],
                                    'is_read' => $row['IS_READ']
                                );
                                $response['chat_items'][] = $data;
                            }
                        }
                    } else {
                        $response['code'] = 400;
                        $response['message'] = "기존 대화내용 select 쿼리 실행 자체를 실패함";
                        echo json_encode($response, JSON_UNESCAPED_UNICODE);
                        exit; // 스크립트 종료
                    }
                } else if ($exist_result->num_rows > 1) {
                    // 에러임. 방이 두개면 안됨
                    $response['code'] = 400;
                    $response['message'] = "방이 1개보다 많음. ";
                    echo json_encode($response, JSON_UNESCAPED_UNICODE);
                    exit; // 스크립트 종료
                } else {
                    // 기존 대화가 없는 것임. 
                    // 상대방의 정보만 가지고 나가기. 
                    $response['code'] = 200; // response code 세팅 
                    $response['message'] = $select_success_msg + ", 기존 대화 없음"; // message 
                    $response['user_number'] = $row['USER_NUMBER'];
                    // 맞팔 여부도.. 
                    $response['chat_items'] = array(); // 빈 배열 반환
                }
            }
        } // 결과값 있는 경우 끝 
        else {
            $response['code'] = 400;
            $response['message'] = "상대 유저 정보가 없습니다.";
        }
    } else { // 게시글 내용가져오는 쿼리 실행 실패 
        $response['code'] = 400;
        $response['message'] = $select_error_msg;
    }
}

echo json_encode($response, JSON_UNESCAPED_UNICODE);
exit; 