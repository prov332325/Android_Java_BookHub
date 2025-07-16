<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../php/db_connection.php");


// 기존 채팅 기록이 없는 2명의 유저가 채팅을 시작해서
// 메시지를 받는 유저가 채팅방 목록에 위치할때 
// 채팅방이 새로 생겨야 한다 !! 
// 이때 상대의 프사를 어디서도 가져올수 없어서,, 
// http 통신을 다시 한번 사용함 ㅋㅋ ㅜㅜ 


// 상대 유저 번호
if (isset($_POST['sender_number'])) {
    $sender_number = intval($_POST['sender_number']);
} else {
    $sender_number = null;
}


// 응답 
$response = array();


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$select_success_msg = "select 완성";
$select_error_msg = "select 실행 오류";



if ($sender_number == '') {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    error_log(json_last_error_msg());
} else {

    // 가져와야 하는 것 
    // 유저 프사. 
    $user_img_stmt = $conn->prepare("SELECT USER_PROFILE_IMG FROM USERS WHERE USER_NUMBER = ?");
    $user_img_stmt->bind_param("i", $sender_number);
    if ($user_img_stmt->execute()) {
        $result = $user_img_stmt->get_result();
        if ($result->num_rows > 0) {
            while ($row = $result->fetch_assoc()) {
                $response['code'] = 200;
                $response['message'] = "프사 있음";
                $response['profile_img'] = $row['USER_PROFILE_IMG'];
            }
        } else { // 프사 없음 
            $response['code'] = 200;
            $response['message'] = "프사 없음";
            exit;
        }
    } else { // select 쿼리 실패 
        $response['code'] = 400;
        $response['message'] = $select_error_msg . ", 채팅방 조회 쿼리 실패";
        error_log(json_last_error_msg());
        exit;
    }
}


echo json_encode($response, JSON_UNESCAPED_UNICODE);

