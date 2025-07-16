<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");


// 가지고 올 것
// 유저 번호, 게시글 번호 , 카테고리.   

// 1) 유저 넘버 
if (isset($_POST["user_number"])) {
    $user_number =  intval($_POST['user_number']);
} else {
    $user_number = null;
}


// 2) 게시글 번호
if (isset($_POST["board_number"])) {
    $board_number =  intval($_POST['board_number']);
} else {
    $board_number = null;
}


// 3) 게시글 카테고리 
if (isset($_POST["category"])) {
    $category = $_POST['category'];
} else {
    $category = null;
}


// message 
$error_msg = "필수 인자 안넘어옴";
$delete_query_failed = "게시판 내용 delete 쿼리문 실행 실패";
$img_delete_query_failed = "게시판 이미지 delete 쿼리문 실행 실패";
$book_delete_query_failed = "게시판 책 delete 쿼리문 실행 실패";
// $success = "게시판 내용+사진 삭제하기 성공";
$success = "댓글까지 삭제됨? ";

if (
    $user_number == '' || $board_number == '' || $category == ''
) {
    echo json_encode(array(
        "code" => 400,
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE);
} else {
    // null 값이 아닐때 삭제 진행하기. 

    // 이미지 먼저 삭제해야함 !!! 
    $img_delete_stmt = $conn->prepare("DELETE FROM BOARD_IMG WHERE USER_NUMBER =? AND BOARD_ID = ? ");
    $img_delete_stmt->bind_param("ii", $user_number, $board_number);
    if ($img_delete_stmt->execute()) { // 이미지 삭제 완료되면

        // 책 삭제 
        $book_delete_stmt = $conn->prepare("DELETE FROM BOOK_INFO WHERE BOARD_NUMBER =? AND USER_NUMBER = ?");
        $book_delete_stmt->bind_param("ii", $board_number, $user_number);
        if ($book_delete_stmt->execute()) { // 책 삭제 완료 되면 

            // 댓글 삭제 해줘야함. 
            // $comment_delete_stmt
            $board_delete_stmt = $conn->prepare("DELETE FROM BOARD WHERE USER_NUMBER = ? AND BOARD_ID = ? AND BOARD_CATEGORY = ?");
            $board_delete_stmt->bind_param("iis", $user_number, $board_number, $category);
            if ($board_delete_stmt->execute()) {
                echo json_encode(array(
                    "code" => 200,
                    "message" => $success
                ), JSON_UNESCAPED_UNICODE);
            } else { // 게시글 내용 delete 쿼리문 실패 
                echo json_encode(array(
                    "code" => 400,
                    "message" => $delete_query_failed
                ), JSON_UNESCAPED_UNICODE);
            } 
        } else {  // 책 삭제 쿼리문 실패 
            echo json_encode(array(
                "code" => 400,
                "message" => $img_delete_query_failed
            ), JSON_UNESCAPED_UNICODE);
        }
    } else { // 이미지 삭제 쿼리문 execute 실패
        echo json_encode(array(
            "code" => 400,
            "message" => $img_delete_query_failed
        ), JSON_UNESCAPED_UNICODE);
    }
}
