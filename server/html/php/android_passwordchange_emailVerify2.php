<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../php/db_connection.php");


// 인증번호 입력값과, 이메일값 가져오기. 

// 1) 유저 이메일 
if (isset($_POST['user_email'])) {
    $user_email = $_POST['user_email'];
} else {
    $user_email = null;
}


// 2) verify_code
if (isset($_POST['verify_code'])) {
    $verify_code = $_POST['verify_code'];
} else {
    $verify_code = null;
}


// 응답 
$response = array();


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$select_success_msg = "select 실행 성공";
$select_error_msg = "select 실행 오류";



// 1. select 로 이메일 일치하는 한줄이 있는지 확인. 가장 최근 줄 한개  
// 2. 그 행의 code 가 일치하는지 확인하기 

// 맞으면 맞다고 보내고 아니면 틀렸다고 보내주면, 비번 변경 화면으로 넘어갈 것임.. .

if ($user_email == '' || $verify_code == '') {
    echo json_encode(array(
        "code" => 400,
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE);
    exit;
} else {
    // select 문 

    $check_verifycode_stmt = $conn->prepare("SELECT * FROM AUTH_KEY WHERE  EMAIL_ADDRESS = ? ORDER BY CREATE_TIME DESC LIMIT 1");
    // 전부 선택 후, 시간순으로 desc 한 뒤에 하나만 가져온다 = 가장 최근에 보낸 이메일 내용만.   
    $check_verifycode_stmt->bind_param("s", $user_email);
    $check_verifycode_stmt->execute();
    $result = $check_verifycode_stmt->get_result();
    if ($result->num_rows > 0) {
        // 이메일이 존재할때   
        $row = $result->fetch_assoc(); // 첫 번째 행 가져오기

        if($user_email==$row['EMAIL_ADDRESS'] && $verify_code == $row['CODE_NUMBERS']) {
            echo json_encode(array(
                "code" => 200,
                "message" => "인증번호일치!!"
            ), JSON_UNESCAPED_UNICODE);
            exit;
        } else {
            echo json_encode(array(
                "code" => 400,
                "message" => "인증번호불일치!! 다시입력해주세욤"
            ), JSON_UNESCAPED_UNICODE);
            exit;
        }

    } else {
        // 값이 아예 존재하지 않을때 ?? 이메일이 안맞다는건데 말이 안됨..
        //  이메일이 존재하지 않습니다?? 
        echo json_encode(array(
            "code" => 400,
            "message" => "이메일이 존재하지 않는다??말이안됨"
        ), JSON_UNESCAPED_UNICODE);
        exit;
    }
}
