<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");


// 가지고 온 것 
// 글쓴이 번호, 글번호, 댓글 내용 

// 1) 유저 넘버 
if (isset($_POST["user_number"])) {
    $user_number =  intval($_POST['user_number']);
} else {
    $user_number = null;
}


// 2) 유저 넘버 
if (isset($_POST["board_number"])) {
    $board_number =  intval($_POST['board_number']);
} else {
    $board_number = null;
}


// 3) 댓글 내용
if (isset($_POST["comment_content"])) {
    $comment_content = $_POST['comment_content'];
} else {
    $comment_content = null;
}

// 응답 
$response = array();


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$insert_success_msg = "댓글 저장이 완료되었습니다";
$comment_insert_error_msg = "댓글 저장 insert 실행 오류";


// ================================================== 댓글 작성 ==================================================


if (
    $user_number == '' || $board_number == '' ||
    $comment_content == ''
) {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit;
} else { // 이 안에서 response채워야함 

    // 필수 인자가 다 있는 경우 insert 작업 해주기 - 
    $comment_insert_stmt = $conn->prepare("INSERT INTO BOARD_COMMENTS (USER_NUMBER, BOARD_ID, BOARD_COMMENT_CONTENT) VALUES (?, ?, ?)"); 
    $comment_insert_stmt->bind_param("iis", $user_number, $board_number, $comment_content); 

    if ($comment_insert_stmt->execute()) { // insert 쿼리 실행 성공 
        $comment_number = $conn->insert_id; // 작성한 댓글의 번호 가져오기. 
    
        // 유저 닉네임이랑 유저 이미지 가져오깅... 뭔가 좀 이상한디. 여기다가 가져오는거 맞나 ? 
        $comment_user_info_stmt = $conn->prepare("SELECT USER_NICKNAME, USER_PROFILE_IMG FROM USERS WHERE USER_NUMBER = ?");
        $comment_user_info_stmt -> bind_param("i", $user_number);

        if($comment_user_info_stmt->execute()) {
            $response['code'] = 200; 
            $response['message'] = $insert_success_msg; 
            $response['comment_number'] =   $comment_number;
            $response['comment_content'] = $comment_content;
            $response['user_number'] = $user_number;
            $response['board_number'] = $board_number;

            $result = $comment_user_info_stmt->get_result(); // result 는 배열이다. 
            if($result -> num_rows > 0) {
                while ($row = $result -> fetch_assoc()) {
                    $response['user_nickname'] = $row['USER_NICKNAME'];
                    $response['user_profileImg'] = $row['USER_PROFILE_IMG'];
                }
            } // 결과가 있음
        } // 작성자 정보 가져오는 select문 성공     
        else {
            $response['code'] = 400;
            $response['message'] = "작성자 정보 가져오는 select 문 실패";
            echo json_encode($response, JSON_UNESCAPED_UNICODE);
            exit;
        } 

 
        // 방금 insert 한 댓글 정보 가져오기. 
        $comment_info_stmt = $conn->prepare("SELECT CREATE_TIME, UPDATE_TIME FROM BOARD_COMMENTS WHERE BOARD_COMMENT_NUMBER = ?");
        $comment_info_stmt->bind_param("i", $comment_number);

        if ($comment_info_stmt->execute()) {
            $result = $comment_info_stmt->get_result();
            if ($result->num_rows > 0) {
                $comment_info = $result->fetch_assoc();
                $response['createdTime'] = $comment_info['CREATE_TIME'];
                $response['updateTime']  = $comment_info['UPDATE_TIME'];
            }
        } else {
            $response['code'] = 400;
            $response['message'] = "댓글 정보 가져오는 select 문 실패";
            echo json_encode($response, JSON_UNESCAPED_UNICODE);
            exit;
        }




    } else { // insert 쿼리 실행 실패 
        $response['code'] = 400;
        $response['message'] = $comment_insert_error_msg;
        echo json_encode($response, JSON_UNESCAPED_UNICODE);
        exit;
    }
}
echo json_encode($response, JSON_UNESCAPED_UNICODE);
