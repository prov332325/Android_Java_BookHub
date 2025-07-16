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

// 수정할 닉네임. 
if (isset($_POST['edited_nickname'])) { // String 으로 넘김. 
    $edited_nickname = $_POST['edited_nickname'];
} else {
    $edited_nickname = null;
}

// 수정할 바이오. 얘는 null 이어도 됨. 
if (isset($_POST['edited_bio'])) { // String 으로 넘김. 
    $edited_bio = $_POST['edited_bio'];
} else {
    $edited_bio = null;
}



// 응답 
$response = array();

// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$select_success_msg = "select 완성";
$select_error_msg = "select 실행 오류";



// 일단 바이오 업데이트 후, 닉네임은 null 이 아닐때에만 update 하기 

if ($user_number == '') {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit;
} else {
    // bio update 
    $bio_update_stmt = $conn->prepare("UPDATE USERS SET USER_PROFILE_BIO = ? WHERE USER_NUMBER = ?");
    $bio_update_stmt->bind_param("si", $edited_bio, $user_number);

    if ($bio_update_stmt->execute()) { // bio update 완료 

        // 새 닉네임이 null 일때 
        if ($edited_nickname === null) {
            $response['code'] = 200;
            $response['message'] = "바이오완료및닉네임수정없음";
        } else { // 새 닉네임이 null 이 아닐때 

            $nickname_update_stmt = $conn->prepare("UPDATE USERS SET USER_NICKNAME = ? WHERE USER_NUMBER = ?");
            $nickname_update_stmt->bind_param("si", $edited_nickname, $user_number);
            if ($nickname_update_stmt->execute()) { // 닉네임 update 완료 
                $response['code'] = 200;
                $response['message'] = "바이오완료및닉네임수정완료";
                $response['user_nickname'] = $edited_nickname;
            } else {
                $response['code'] = 400;
                $response['message'] = "바이오완료및닉네임수정오류";
            }
        }
    } else {
        $response['code'] = 400;
        $response['message'] = "바이오수정오류";
    }
} // 유저 번호 존재


echo json_encode($response, JSON_UNESCAPED_UNICODE);
