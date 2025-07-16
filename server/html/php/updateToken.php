<?php
header('Content-Type: application/json; charset=UTF-8');
include("db_connection.php");




// http 통신으로 email 혹은 id 값 가져오기 
if (isset($_POST['emailId'])) {
    $emailId = $_POST['emailId'];
} else {
    $emailId = null;
}


// 기기에 할당 받은 fcm 토큰 값 
if (isset($_POST['firebase_token'])) {
    $firebase_token = $_POST['firebase_token'];
} else {
    $firebase_token = null;
}



// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$success_msg = "일치"; // 있음.
$failed_msg = "불일치"; // 없음

if ($emailId == '' ||  $firebase_token == '') {
    echo json_encode(array(
        "code" => 400,
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE);
} else {


    $update_token_stmt = $conn->prepare("UPDATE USERS SET USER_TOKEN = ? WHERE USER_EMAIL_ID = ?");
    $update_token_stmt->bind_param("ss", $firebase_token, $email_id); 

    if ($update_token_stmt->execute()) {
        echo json_encode(array(
            "code" => 200,
            "message" => "토큰 업데이트 완료"
        ), JSON_UNESCAPED_UNICODE);
    } else {
        echo json_encode(array(
            "code" => 500,
            "message" => "토큰 업데이트 실패"
        ), JSON_UNESCAPED_UNICODE);
    }




}
?> 