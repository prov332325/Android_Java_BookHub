<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");



if (isset($_POST['user_number'])) { // int 로 넘김.
    $user_number = $_POST['user_number'];
} else {
    $user_number = null;
}


if (isset($_POST['edited_user_nickname'])) { // String 으로 넘김. 
    $edited_user_nickname = $_POST['edited_user_nickname'];
} else {
    $edited_user_nickname = null;
} 


// 응답 
$response = array();


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$select_success_msg = "select 완성";
$select_error_msg = "select 실행 오류";


// 유효성 식 
$nickname_pattern =  "/^[가-힣a-zA-Z0-9]+$/";



if(  $user_number == '' || $edited_user_nickname == '' ) {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit;
} else { 

    // 닉네임 유효성 검사 
    if (preg_match($nickname_pattern, $edited_user_nickname)) {
        // 유효성 통과됨. 중복 검사 실시  
        $duplicate_nickname_stmt = $conn->prepare("SELECT * FROM USERS WHERE USER_NICKNAME LIKE ? AND USER_NUMBER != ?");
        $duplicate_nickname_stmt->bind_param("si", $edited_user_nickname, $user_number);
        $duplicate_nickname_stmt->execute();

        $nickname_result = $duplicate_nickname_stmt->get_result(); 

        if ($nickname_result->num_rows>0) {
            $response['code'] = 200;
            $response['message'] = "존재";
        } else {
            $response['code'] = 200;
            $response['message'] = "사용가능";
        }

    } else { // 유효성 못 통과함. 400. 
        $response['code'] = 200;
        $response['message'] = "유효성미통과";
        echo json_encode($response, JSON_UNESCAPED_UNICODE);
        exit;
    }
} // 끝 


echo json_encode($response, JSON_UNESCAPED_UNICODE);
