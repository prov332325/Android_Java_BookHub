<?php
header('Content-Type: application/json; charset=UTF-8');
include("db_connection.php"); 




// 오류 확인하는 방법 !!!! 
// power shell 에서 
// cd /var/log/apache2/error.log -> 잘려 나옴 

// 전체 출력하는 방법 
// sudo tail error.log !! 


// 현재 탈퇴 하려는 유저 넘버 
if (isset($_POST['user_number'])) { // int 로 넘김.
    $user_number = $_POST['user_number'];
} else {
    $user_number = null;
}


// 응답 
$response = array();


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$delete_success_msg = "delete 실행 성공, 탈퇴 완료";
$delete_error_msg = "select 실행 오류";


if(empty($user_number)) {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    error_log(json_last_error_msg($_POST));
} else { 

    //  삭제하기. 
    $user_withdraw_stmt = $conn->prepare("DELETE FROM USERS  WHERE USER_NUMBER = ? "); 
    $user_withdraw_stmt -> bind_param("i", $user_number); 
    
    if($user_withdraw_stmt->execute()) { // delete 문 실행 
        $response['code'] = 200; 
        $response['message'] = $delete_success_msg; 

    } else {
        $response['code'] = 400;
        $response['message'] = $delete_error_msg;
        error_log(json_last_error_msg($_POST));
    }
}
echo json_encode($response, JSON_UNESCAPED_UNICODE);
exit; 