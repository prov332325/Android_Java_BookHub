<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");



// 오류 확인하는 방법 !!!! 
// power shell 에서 
// cd /var/log/apache2/error.log -> 잘려 나옴 

// 오류 확인하는 방법 !!!! 
// power shell 에서 
// cd /var/log/apache2/error.log -> 잘려 나옴 

//  + 오류 내용 전체 출력하는 방법 
// sudo tail error.log !! 


// 문자열 한번에 변경하는 방법 
// 문자 검색 ctrl + H => 찾기, 바꾸기

// 정렬 - php formatter 설치 돼 있음 
// shift + Alt + F 

// 응답 
$response = array();

// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$insert_success_msg = "저장이 완료되었습니다";
$info_insert_error_msg = "책 정보 BOOK INFO 저장 insert 실행 오류"; 
$mylibrary_insert_error_msg = "읽은 책 BOOK ALREADY 저장 insert 실행 오류";
$mylibrary_insert_success_msg = "읽은 책 BOOK ALREADY 완료";

$success = "이미지 서버 저장 완료";
$db_success = "이미지 DB 저장 완료";
$db_fail = "이미지 DB 저장 실패";
$type_error = "사진타입 jpg, png 아님";
$upload_error = "사진 서버 업로드 실패";



// http 통신을 통해 폼 데이터 읽기 
$user_number = isset($_POST['user_number']) ? intval($_POST['user_number']) : 0;

$already_title = isset($_POST['title']) ? $_POST['title'] : null;
$already_author = isset($_POST['author']) ? $_POST['author'] : null;

$already_description = mysqli_real_escape_string($conn, $_POST['description']);
$already_publisher = isset($_POST['publisher']) ? $_POST['publisher'] : null;
$already_pubDate = isset($_POST['pubDate']) ? $_POST['pubDate'] : null;


$already_cover = null; 
if ( isset($_FILES['cover']) && $_FILES['cover']['name'] != "") {
    $fileName = $_FILES['cover']['name']; 

    $imgFullName = strtolower($fileName);
    $imgNameSlice = explode(".", $imgFullName); // . 기준으로 쪼개서 확장자 확인하기 
    $imgName = $imgNameSlice[0];
    $imgType = $imgNameSlice[1];
    $imgExt = array('jpg', 'png');

    // 이미지를 서버에 저장할때 사용할 이미지 파일 name 
    $dates = date("mdhis", time());
    $newImgName = chr(rand(97, 122)) . chr(rand(97, 122)) . $dates . rand(1, 9) . "." . $imgType;
    $dir = "/var/www/html/php/img/";

    $imgTmpName = $_FILES['cover']['tmp_name'];
    if (move_uploaded_file($imgTmpName, $dir . $newImgName)) {
        $already_cover = $newImgName; 
    }

}

// $already_cover = mysqli_real_escape_string($conn, $_POST['cover']);
// 여기에는 string 값으로 서버에 저장한 주소 값이 들어가야함.
$already_isbn = isset($_POST['isbn']) ? $_POST['isbn'] : null;
$already_started_date = isset($_POST['started_date']) ? $_POST['started_date'] : null;
$already_finished_date = isset($_POST['finished_date']) ? $_POST['finished_date'] : null;
$already_rating = isset($_POST['already_rating']) ? $_POST['already_rating'] : null;
$book_save_type = isset($_POST['book_save_type']) ? $_POST['book_save_type'] : null;
error_log("전달받은 읽은 책 저장 데이터 ! POST data: " . json_encode($_POST)); 


// 사진은 일단 없어도 되는걸로 설정해놓음. 
if($user_number =='' || $already_title =='' || $already_author =='' || $already_description =='' || 
$already_publisher ==''|| $already_pubDate == '' || $already_started_date =='' ||
$already_finished_date  =='' || $already_rating =='' || $book_save_type == '' ) {
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
