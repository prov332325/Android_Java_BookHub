<?php
header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");



// 오류 확인하는 방법 !!!! 
// power shell 에서 
// cd /var/log/apache2/error.log -> 잘려 나옴 

// 전체 출력하는 방법 
// sudo tail error.log !! 



// http 통신을 통해 폼 데이터 읽기 
$user_number = intval($_POST['user_number']);

$reading_title = $_POST['title'];
$reading_author = $_POST['author'];
$reading_description = mysqli_real_escape_string($conn, $_POST['description']);
$reading_publisher = $_POST['publisher'];
$reading_pubDate = $_POST['pubDate'];
$reading_cover = mysqli_real_escape_string($conn, $_POST['cover']);
$reading_isbn = $_POST['isbn'];

$reading_read_page = $_POST['read_page'];
$reading_started_date = $_POST['started_date'];
$book_save_type = isset($_POST['book_save_type']) ? $_POST['book_save_type'] : null;

error_log("전달받은 읽은 책 저장 데이터 ! POST data: " . json_encode($_POST));


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$insert_success_msg = "저장이 완료되었습니다";
$mylibrary_insert_error_msg = "읽고 있는 책 BOOK READING 저장 insert 실행 오류";
$mylibrary_insert_success_msg = "읽고 있는 책 BOOK READING 완료";


// null 값 확인 
if($user_number =='' || $reading_title =='' || $reading_author =='' || $reading_description =='' || 
$reading_publisher ==''|| $reading_pubDate == '' || $reading_read_page =='' ||
$reading_started_date  =='' ) {

    echo json_encode(array( 
        "code" => 400, 
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE); 
} else {

    // null 값이 없다면 저장하기. 
    // 1. 책 저장 book info 
    $book_info_insert_stmt = $conn->prepare("INSERT INTO BOOK_INFO (USER_NUMBER, BOOK_TITLE, BOOK_AUTHOR, BOOK_DESCRIPTION, BOOK_PUBLISHER, BOOK_PUBLISH_DATE, BOOK_COVER, BOOK_ISBN) 
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
    $book_info_insert_stmt -> bind_param("isssssss", $user_number, $reading_title, $reading_author, $reading_description, $reading_publisher, $reading_pubDate, $reading_cover, $reading_isbn);
    if($book_info_insert_stmt->execute()) { // 책 정보 저장 완료 book info successfully inserted
    
    // 2. 내 서재 저장하기 - 읽고 있는 책  book reading (my library)
    $book_info_number = $conn->insert_id;  
    $mylibrary_reading_insert_stmt = $conn-> prepare("INSERT INTO BOOK_READING (BOOK_READING_USER_NUMBER, BOOK_INFO_NUMBER, READING_STARTED_DATE, READING_PAGE) VALUES (?, ?, ?, ?)");
    $mylibrary_reading_insert_stmt -> bind_param("iiss", $user_number, $book_info_number, $reading_started_date, $reading_read_page);
    if($mylibrary_reading_insert_stmt->execute()) { // 내 서재까지 저장 완료 
        echo json_encode(array(
            "code" => 200, 
            "message" => $insert_success_msg
        ), JSON_UNESCAPED_UNICODE); 
    } else { // 내 서재 저장 실패 
        echo json_encode(array(
            "code" => 400, 
            "message" => $mylibrary_insert_error_msg
        ), JSON_UNESCAPED_UNICODE); 
    }


    } else { // 책 정보 저장 실패 book 
     //책 정보 insert 실패한 경우 
     echo json_encode(array(
        "code" => 400, 
        "message" => $info_insert_error_msg
    ), JSON_UNESCAPED_UNICODE);    
    }

} // 입력 값이 null이 아닐때 끝 ! 

?>