<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");


// 가지고 올 것
// 유저 번호, 제목, 내용, 카테고리.  

// 1) 유저 넘버 
if (isset($_POST["user_number"])) {
    $user_number =  intval($_POST['user_number']);
} else {
    $user_number = null;
}

// 이렇게 하는게 깔끔하고 좋음
// $user_number = isset($_POST['user_number']) ? intval($_POST['user_number']) : null; 


// 2) 게시글 제목 
if (isset($_POST["board_title"])) {
    $board_title = $_POST['board_title'];
} else {
    $board_title = null;
}

// 3) 게시글 내용 
if (isset($_POST["board_content"])) {
    $board_content = $_POST['board_content'];
} else {
    $board_content = null;
}

// 4) 게시글 카테고리 
if (isset($_POST["board_category"])) {
    $board_category = $_POST['board_category'];
} else {
    $board_category = null;
}




// 책 정보 !!! 
// if (isset($_POST['books'])) {
//     echo json_encode(array(
//         "code" => 200,
//         "message" => "책 있음, " . $_POST['books']
//     ), JSON_UNESCAPED_UNICODE);
// } else {
//     echo json_encode(array(
//         "code" => 200,
//         "message" => "책 없음"
//     ), JSON_UNESCAPED_UNICODE);
// }




// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$insert_success_msg = "게시판 저장이 완료되었습니다";
$board_insert_error_msg = "게시판 저장 insert 실행 오류";

// 전부 null 이 아닐때 insert 후에, json으로 반환하기. code랑 message만 !!  

// ================================================== 게시글 작성 ==================================================
if (
    $user_number == '' || $board_title == '' ||
    $board_content == '' || $board_category == ''
) {
    echo json_encode(array(
        "code" => 400,
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE); 
    exit;
}


// 게시글 insert 
$board_insert_stmt = $conn->prepare("INSERT INTO BOARD (USER_NUMBER, BOARD_TITLE, BOARD_CONTENT, BOARD_CATEGORY) 
    VALUES (?, ?, ?, ?)");
$board_insert_stmt->bind_param("isss", $user_number, $board_title, $board_content, $board_category);

if ($board_insert_stmt->execute()) {
    //방금 저장한 board number 가져오기 
    $board_number = $conn->insert_id;

    // 먼저, 넘어온 책이 있는지 확인 !! 안하면 에러남 

    if (isset($_POST['books'])) {
        $books = json_decode($_POST['books']);
        if (!empty($books)) {
            foreach ($books as $book) {
                $title = $conn->real_escape_string($book->title);
                $author = $conn->real_escape_string($book->author);
                $description = $conn->real_escape_string($book->description);
                $cover = $conn->real_escape_string($book->cover);

                // db에 저장하기. 
                $book_insert_stmt = $conn->prepare("INSERT INTO BOOK_INFO (USER_NUMBER, BOOK_TITLE, BOOK_AUTHOR, BOOK_DESCRIPTION, BOOK_COVER, BOARD_NUMBER ) VALUES (?, ?, ?, ?, ?, ?)");
                $book_insert_stmt->bind_param("issssi",$user_number, $title, $author, $description, $cover, $board_number);

                if ($book_insert_stmt->execute()) {
                    $insert_success_msg .= ", 쿼리 실행완+1, ";
                } else { // insert 쿼리 오류 
                    echo json_encode(array(
                        "code" => 400,
                        "message" => $board_insert_error_msg
                    ), JSON_UNESCAPED_UNICODE);
                    exit;
                }
            } // for 문 끝 
        } else {
            $insert_success_msg .= ", 책 정보 없음"; // 여기가 문제다. 
        }
        // 넘어온 책이 있는 경우 끝
    } else {
        $insert_success_msg .= ", 책 없음";
    }

    echo json_encode(array(
        "code" => 200,
        "message" => $insert_success_msg,
        "board_number" => $board_number
    ), JSON_UNESCAPED_UNICODE);
} else { // 게시글 저장 실패
    echo json_encode(array(
        "code" => 400,
        "message" => $board_insert_error_msg
    ), JSON_UNESCAPED_UNICODE);
} // 게시글 저장 if문 끝 
