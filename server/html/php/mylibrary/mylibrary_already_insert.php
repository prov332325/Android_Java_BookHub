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

$already_title = $_POST['title'];
$already_author = $_POST['author'];
$already_description = mysqli_real_escape_string($conn, $_POST['description']);
$already_publisher = $_POST['publisher'];
$already_pubDate = $_POST['pubDate'];
$already_cover = mysqli_real_escape_string($conn, $_POST['cover']);
$already_isbn = $_POST['isbn'];
$already_started_date = $_POST['started_date'];
$already_finished_date = $_POST['finished_date'];
$already_rating = $_POST['already_rating'];
$book_save_type = isset($_POST['book_save_type']) ? $_POST['book_save_type'] : null;

error_log("전달받은 읽은 책 저장 데이터 ! POST data: " . json_encode($_POST));


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$insert_success_msg = "저장이 완료되었습니다";
$info_insert_error_msg = "책 정보 BOOK INFO 저장 insert 실행 오류"; 
$mylibrary_insert_error_msg = "읽은 책 BOOK ALREADY 저장 insert 실행 오류";
$mylibrary_insert_success_msg = "읽은 책 BOOK ALREADY 완료";


// 사진은 일단 없어도 되는걸로 설정해놓음. 
if($user_number =='' || $already_title =='' || $already_author =='' || $already_description =='' || 
$already_publisher ==''|| $already_pubDate == '' || $already_started_date =='' ||
$already_finished_date  =='' || $already_rating ==''  || $book_save_type == '' ) {
    echo json_encode(array( 
        "code" => 400, 
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE); 
} else {

    // null 값이 없다면 저장하기. 
    //1. 책 저장 book info 
    $book_info_insert_stmt = $conn->prepare("INSERT INTO BOOK_INFO (USER_NUMBER, BOOK_TITLE, BOOK_AUTHOR, BOOK_DESCRIPTION, BOOK_PUBLISHER, BOOK_PUBLISH_DATE, BOOK_COVER, BOOK_ISBN, BOOK_SAVE_TYPE) 
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $book_info_insert_stmt -> bind_param("issssssss", $user_number ,$already_title, $already_author, $already_description, $already_publisher, $already_pubDate, $already_cover, $already_isbn, $book_save_type);
    if($book_info_insert_stmt->execute()) { // 책정보(BOOK_INFO) insert 성공 
    //2. 책 저장이 완료되면, 인덱스 번호 가져오기. 
    $book_info_number = $conn->insert_id;  
    $mylibrary_already_insert_stmt = $conn -> prepare("INSERT INTO BOOK_ALREADY (BOOK_ALREADY_USER_NUMBER, BOOK_INFO_NUMBER, ALREADY_STARTED_DATE, ALREADY_FINISHED_DATE, ALREADY_RATING) VALUES (?, ?, ?, ?, ?)");
    $mylibrary_already_insert_stmt -> bind_param("iisss", $user_number, $book_info_number, $already_started_date, $already_finished_date, $already_rating);
    if($mylibrary_already_insert_stmt->execute()) { // 내 서재까지 저장 성공. 
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
    

    } else {  // 책정보(BOOK_INFO) insert 실패
          //책 정보 insert 실패한 경우 
          echo json_encode(array(
            "code" => 400, 
            "message" => $info_insert_error_msg
        ), JSON_UNESCAPED_UNICODE);       
      
    }


 } // 입력 값이 null이 아닐때 
?>
