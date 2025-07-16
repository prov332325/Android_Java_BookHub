<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../php/db_connection.php");


// 입력한 비밀번호 두개 가져오기 

// 1) 새 비번 
if (isset($_POST['new_password1'])) {
    $new_password1 = $_POST['new_password1'];
} else {
    $new_password1 = null;
}


// 2) 새 비번확인 
if (isset($_POST['new_password2'])) {
    $new_password2 = $_POST['new_password2'];
} else {
    $new_password2 = null;
}


// 2) 유저 이메일  
if (isset($_POST['user_email'])) {
    $user_email = $_POST['user_email'];
} else {
    $user_email = null;
}


// 응답 
$response = array();


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$update_success_msg = "update 실행 성공";
$update_error_msg = "update 실행 오류";

// 유효성 식 
$password_pattern = "/^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()\-+=]).{8,}$/";


// 유저 이메일을 갖고 유저 번호 가져오기 
if (!empty($user_email)) {
    $user_number_stmt = $conn->prepare("SELECT USER_NUMBER FROM USERS WHERE USER_EMAIL_ID = ?");
    $user_number_stmt->bind_param("s", $user_email);
    $user_number_stmt->execute();
    $user_number_stmt->bind_result($user_number);
    if ($user_number_stmt->fetch()) {
        // 유저 번호가 있을경우. 
        $user_number_stmt->close(); // 이전 스테이트먼트 닫기
    }
} else { // 유저 이메일이 null 인 경우. 
    echo json_encode(array(
        "code" => 400,
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE);
    exit;
}


// 먼저 넘어온 값이 null 인지 아닌지 확인하기 
//1. 올바른 비밀번호 형식인지 확인하기 - 1번기준으로 확인한다. 
// --> 유저가 변경하고자하는 비밀번호가 1번임을 가정하고, 1번이 올바른지 확인, 그리고 2번이 1번이랑 일치하는지 확인. 
//2. 비밀번호 두개가 일치하는지 확인하기.  
//3. sql문 update 로 비번 바꿔줌. 

if ($new_password1 == '' || $new_password2 == '') {
    echo json_encode(array(
        "code" => 400,
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE);
    exit;
} else {
    // 1. 비밀번호 형식 확인하기 -> 형식 맞으면 재입력이랑 일치하는지 확인하기
    if (preg_match($password_pattern, $new_password1)) {
        if ($new_password1 === $new_password2) {

            // 비밀번호 변경하기 
            $password_change_stmt = $conn->prepare("UPDATE USERS SET USER_PASSWORD = ? WHERE USER_NUMBER = ?");
            $password_change_stmt->bind_param("si", $new_password1, $user_number);

            // 비번 변경문 실행 
            if ($password_change_stmt->execute()) {
                echo json_encode(array(
                    "code" => 200,
                    "message" => $update_success_msg . ", 비번 변경 완료"
                ), JSON_UNESCAPED_UNICODE);
                exit;
            } else { // 비번 변경 update 실행 실패. 
                echo json_encode(array(
                    "code" => 400,
                    "message" => $update_error_msg
                ), JSON_UNESCAPED_UNICODE);
                exit;
            }
        } else { // 비밀번호 확인과 일치하지 않을때 
            echo json_encode(array(
                "code" => 42,
                "message" => "비밀번호재확인과불일치"
            ), JSON_UNESCAPED_UNICODE);
            exit;
        }
    } else { // 올바른 비밀번호 형식이 아닐때 
        echo json_encode(array(
            "code" => 41,
            "message" => "비밀번호형식에러"
        ), JSON_UNESCAPED_UNICODE);
        exit;
    }
} // null 값이 아닐때. 끝 

$password_change_stmt->close();
exit;