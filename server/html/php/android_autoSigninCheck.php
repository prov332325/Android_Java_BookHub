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

//error_log("전달받은 자동로그인용 email 혹은 id => POST data: " . json_encode($_POST));


if ($emailId == '' ||  $firebase_token == '') {
    echo json_encode(array(
        "code" => 400,
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE);
} else {

    // null 값이 아니니까 확인해봐야징 

    $auto_check_stmt = $conn->prepare("SELECT * FROM USERS WHERE USER_EMAIL_ID = ?");
    $auto_check_stmt->bind_param("s", $emailId);
    $auto_check_stmt->execute();

    $result = $auto_check_stmt->get_result();
    if ($result->num_rows > 0) {
        // 존재함 
        $auto_user = $result->fetch_assoc();
        $user_number = $auto_user['USER_NUMBER'];
        $userEmailId = $auto_user['USER_EMAIL_ID'];
        $userNickname = $auto_user['USER_NICKNAME'];

        //token update 하기 

        $update_token_stmt = $conn->prepare("UPDATE USERS SET USER_TOKEN = ? WHERE USER_NUMBER = ? ");
        $update_token_stmt->bind_param("si", $firebase_token, $user_number);

        // 업데이트 
        if ($update_token_stmt->execute()) {
            // 성공시 보낼 메시지. 
            echo json_encode(array(
                "code" => 200,
                "message" => $success_msg . ', token update까지 완료 ! ',
                "user_email" => $userEmailId,
                "user_nickname" => $userNickname
            ), JSON_UNESCAPED_UNICODE);
        }
    } else {
        // 없음 
        echo json_encode(array(
            "code" => 200,
            "message" => $failed_msg
        ), JSON_UNESCAPED_UNICODE);
    }
}
