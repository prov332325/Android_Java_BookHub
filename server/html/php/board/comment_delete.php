<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");


// 가지고 올 것
// 유저 번호, 댓글 번호 , 댓글 수정내용.   



// 1) 유저 넘버 - int 로 넘어옴
if (isset($_POST["user_number"])) {
    $user_number =  $_POST['user_number'];
} else {
    $user_number = null;
}
 

// 2) 댓글 번호
if (isset($_POST["comment_number"])) {
    $comment_number =  $_POST['comment_number'];
} else {
    $comment_number = null;
}


// 응답 
$response = array();


// message 
$error_msg = "필수 인자 안넘어옴";
$success = "댓글 삭제 delete 쿼리 실행 성공";
$comment_delete_query_failed = "댓글 delete 삭제 쿼리문 실행 실패"; 

if (
    $user_number == '' || $comment_number == '' 
) {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit;
} else { 

    // 필수 인자가 다 있는 경우 delete 작업 해주기 
    $comment_delete_stmt = $conn -> prepare("DELETE FROM BOARD_COMMENTS WHERE BOARD_COMMENT_NUMBER = ? AND USER_NUMBER = ?");
    $comment_delete_stmt -> bind_param("ii", $comment_number, $user_number ); 
    if($comment_delete_stmt->execute()) {
        $response['code'] = 200; 
        $response['message'] = $success; 
    }

}


echo json_encode($response, JSON_UNESCAPED_UNICODE);