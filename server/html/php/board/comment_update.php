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


// 3) 댓글 수정 내용
if (isset($_POST["edit_comment_content"])) {
    $edit_comment_content = $_POST['edit_comment_content'];
} else {
    $edit_comment_content = null;
}
 
// 응답 
$response = array();

// message 
$error_msg = "필수 인자 안넘어옴";
$success = "댓글 수정 update 쿼리 실행 성공";
$comment_update_query_failed = "댓글 수정 update 쿼리문 실행 실패"; 


if (
    $user_number == '' || $comment_number == '' || $edit_comment_content == ''
) {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit;
} else {  // 이 안에서 response채워야함 

        // 필수 인자가 다 있는 경우 update 작업 해주기 - 
        $comment_update_stmt = $conn->prepare("UPDATE BOARD_COMMENTS SET BOARD_COMMENT_CONTENT = ? WHERE BOARD_COMMENT_NUMBER = ? AND USER_NUMBER = ?");
        $comment_update_stmt -> bind_param("sii", $edit_comment_content, $comment_number, $user_number);  
        if($comment_update_stmt->execute()) {

            // 업데이트 시간 가져오기 
            $update_time_stmt = $conn->prepare("SELECT UPDATE_TIME FROM BOARD_COMMENTS WHERE BOARD_COMMENT_NUMBER =?"); 
            $update_time_stmt -> bind_param("i", $comment_number); 
            if ($update_time_stmt->execute()) {

                $response['code'] = 200; 
                $response['message'] = $success; 
                $response['comment_content'] = $edit_comment_content;
                $response['user_number'] = $user_number;


                $result = $update_time_stmt->get_result(); 
                if($result->num_rows>0) {
                    $row = $result->fetch_assoc();
                    $update_time = $row['UPDATE_TIME'];
                    $response['updateTime'] = $update_time;
                }
            }


        } else {
            $response['code'] = 400;
            $response['message'] = $comment_update_query_failed;
            echo json_encode($response, JSON_UNESCAPED_UNICODE);
            exit;
        }
}

echo json_encode($response, JSON_UNESCAPED_UNICODE);