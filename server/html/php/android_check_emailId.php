<?php
header('Content-Type: application/json; charset=UTF-8');
include("db_connection.php"); 


// http 통신을 통해 폼 데이터 읽기 
$user_emailId = $_POST['emailId'];


// 회원번호 변수 초기화 
$current_user_number = null; 


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다"; 
$success_msg ="유저넘버완료"; 
$failed_msg = "유저넘버체크실패";


if($user_emailId =='') {
    echo json_encode(array(
        "code" => 400, 
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE); 
} else {
// 넘겨받은 이메일에 알맞는 유저 번호가 있는지 확인하기. 
$check_user_number_stmt = $conn -> prepare("SELECT * FROM USERS WHERE USER_EMAIL_ID = ? "); 
$check_user_number_stmt -> bind_param("s", $user_emailId); 
$check_user_number_stmt -> execute(); 

$result = $check_user_number_stmt -> get_result();
if ($result -> num_rows > 0) {

    // 결과가 있음 ! 배열로 담아  
    $current_user = $result->fetch_assoc(); 
   // $current_user_number = $current_user['USER_NUMBER'];
    echo json_encode(array(
        "code" => 200, 
        "message" => $success_msg, 
        "user_number" => $current_user['USER_NUMBER']
    ), JSON_UNESCAPED_UNICODE); 


} else { // 결과가 없음. 
    echo json_encode(array(
        "code" => 400, 
        "message" => $failed_msg
    ), JSON_UNESCAPED_UNICODE); 
}



}




?> 