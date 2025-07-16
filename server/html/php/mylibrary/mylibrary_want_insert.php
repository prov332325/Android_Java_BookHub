<?php
header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");


// 오류 확인하는 방법 !!!! 
// power shell 에서 
// cd /var/log/apache2/error.log -> 잘려 나옴 

//  + 오류 내용 전체 출력하는 방법 
// sudo tail error.log !! 


// 문자열 한번에 변경하는 방법 
// 문자 검색 ctrl + H => 찾기, 바꾸기


// 정렬 - php formatter 설치 돼 있음 
// shift + Alt + F 


// http 통신을 통해 폼 데이터 읽기 
$user_number = intval($_POST['user_number']);

$want_title = $_POST['title'];
$want_author = $_POST['author'];
$want_description = mysqli_real_escape_string($conn, $_POST['description']);
$want_publisher = $_POST['publisher'];
$want_pubDate = $_POST['pubDate'];
$want_cover = mysqli_real_escape_string($conn, $_POST['cover']);
$want_isbn = $_POST['isbn'];

$want_rating = $_POST['want_rating'];
$want_preview = $_POST['want_preview'];

error_log("전달받은 읽고 싶은 책 저장 데이터 ! POST data: " . json_encode($_POST));


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$insert_success_msg = "저장이 완료되었습니다";
$mylibrary_insert_error_msg = "읽고 싶은 책 BOOK WANT 저장 insert 실행 오류";
$mylibrary_insert_success_msg = "읽고 싶은 책 BOOK WANT 완료";


// null 값 확인
if (
    $user_number == '' || $want_title == '' || $want_author == '' || $want_description == '' ||
    $want_publisher == '' || $want_pubDate == '' || $want_rating == '' ||
    $want_preview  == ''
) {

    echo json_encode(array(
        "code" => 400,
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE);
} else {

    // null 값 없으면 저장하기. 

    // 1. 책 정보 저장 
    $book_info_insert_stmt = $conn->prepare("INSERT INTO BOOK_INFO (USER_NUMBER, BOOK_TITLE, BOOK_AUTHOR, BOOK_DESCRIPTION, BOOK_PUBLISHER, BOOK_PUBLISH_DATE, BOOK_COVER, BOOK_ISBN) 
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
    $book_info_insert_stmt->bind_param("isssssss", $user_number,$want_title, $want_author, $want_description, $want_publisher, $want_pubDate, $want_cover, $want_isbn);
    if ($book_info_insert_stmt->execute()) {

        // 2. 내 서재 저장하기 - 읽고 싶은 책 
        $book_info_number = $conn->insert_id;

        $mylibrary_want_insert_stmt = $conn->prepare("INSERT INTO BOOK_WANT (BOOK_WANT_USER_NUMBER, BOOK_INFO_NUMBER, WANT_RATING, WANT_PREVIEW) VALUES (?, ?, ?, ?)");
        $mylibrary_want_insert_stmt->bind_param("iiss", $user_number, $book_info_number, $want_rating, $want_preview);
        if ($mylibrary_want_insert_stmt->execute()) { // 내 서재까지 저장 완료 
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
    } else { // 책 정보 저장 실패 
        echo json_encode(array(
            "code" => 400,
            "message" => $info_insert_error_msg
        ), JSON_UNESCAPED_UNICODE);
    }
} // null 값 확인 후 저장 완료
