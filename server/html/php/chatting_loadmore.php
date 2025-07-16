<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../php/db_connection.php");


// 채팅방 내부 페이징 !! 


// 현재 방 번호
if (isset($_POST['room_id'])) { // int 로 넘김.
    $room_id = $_POST['room_id'];
} else {
    $room_id = null;
}


// 마지막 메시지 (position 0번의) message number (고유 번호) 
if (isset($_POST['first_message_number'])) { // int 로 넘김.
    $first_message_number = $_POST['first_message_number'];
} else {
    $first_message_number = null;
}


// 응답 
$response = array();


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$select_success_msg = "select 완성";
$select_error_msg = "select 실행 오류";


if (empty($room_id) || empty($first_message_number)) {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    error_log(json_last_error_msg($_POST));
} else {
    $chat_load_more_stmt = $conn->prepare("SELECT * 
FROM CHAT_MESSAGES 
WHERE ROOMS_ID = ? AND MESSAGE_ID < ?
ORDER BY MESSAGE_SENT_TIME DESC 
LIMIT 20");
    $chat_load_more_stmt->bind_param("ii", $room_id, $first_message_number);
    if ($chat_load_more_stmt->execute()) {
        $loadmore_result = $chat_load_more_stmt->get_result();


        if ($loadmore_result->num_rows > 0) {
            $response['code'] = 200; // response code 세팅 
            $response['message'] = $select_success_msg; // message  
            $response['chat_room_number'] = (string) $room_id;
            $response['chat_items'] = array();

            while ($row = $loadmore_result->fetch_assoc()) {
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
        } else {
            $response['code'] = 200;
            $response['message'] = "내용없음";
            echo json_encode($response, JSON_UNESCAPED_UNICODE);
            exit; // 스크립트 종료
        }
    } else {
        // select 쿼리 오류 
        $response['code'] = 400;
        $response['message'] = "기존 대화내용 select 쿼리 실행 자체를 실패함";
        echo json_encode($response, JSON_UNESCAPED_UNICODE);
        exit; // 스크립트 종료
    }
}

echo json_encode($response, JSON_UNESCAPED_UNICODE);
exit; 