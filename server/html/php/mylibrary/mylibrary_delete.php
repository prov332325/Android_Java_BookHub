<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");


// 유저 넘버 user number 가져오기 
if (isset($_POST["user_number"])) {
    $user_number =  intval($_POST['user_number']);
    // echo $user_number;
    // echo " is your user_number"; // 이거 띄우면 계속 오류  뜸 
    // 오류 내용: 
    //     E  onFailure: java.lang.IllegalStateException: Expected BEGIN_OBJECT but was NUMBER at line 1 column 2 path $
} else {
    $user_number = null;
    // echo "no username supplied";
}

// 서재 타입 가져오기 - 1,2,3, String 값으로 넘어옴. 
if (isset($_POST["type"])) {
    $type = $_POST['type'];
} else {
    $type = null;
}


// 책 번호 가져오기 
if (isset($_POST["book_number"])) {
    $book_number =  intval($_POST['book_number']);
} else {
    $book_number = null;
}

// 서재 번호 가져오기 
if (isset($_POST["mylibrary_number"])) {
    $mylibrary_number =  intval($_POST['mylibrary_number']);
} else {
    $mylibrary_number = null;
}


error_reporting(E_ALL);
ini_set("display_errors", 1);

// 에러가 발생한 시간 기록
error_log("에러가 발생한 시간: " . date("Y-m-d H:i:s"));


// message 
$error_msg = "필수 인자 user number 안넘어옴";
$library_query_failed = "서재 delete 쿼리문 실행 실패";
$bookinfo_query_failed = "책 정보 delete 쿼리문 실행 실패";
$success = "내 서재 삭제하기 성공";


if (
    $user_number == '' || $type == '' ||
    $book_number == '' || $mylibrary_number == ''
) {
    echo json_encode(array(
        "code" => 400,
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE);
} else {
    // null 값이 아닐때 type별로 삭제 진행하기. 
    if ($type == '1') {
        $alreadyLib_delete_stmt = $conn->prepare("DELETE FROM BOOK_ALREADY WHERE BOOK_ALREADY_NUMBER = ?");
        $alreadyLib_delete_stmt->bind_param("i", $mylibrary_number);
        if ($alreadyLib_delete_stmt->execute()) { // 내 서재 삭제 성공
            $alreadyInfo_delete_stmt = $conn->prepare("DELETE FROM BOOK_INFO WHERE BOOK_INFO_NUMBER = ? AND USER_NUMBER = ?");
            $alreadyInfo_delete_stmt->bind_param("ii", $book_number, $user_number);
            if ($alreadyInfo_delete_stmt->execute()) {  // 책 정보까지 삭제 성공
                echo json_encode(array(
                    "code" => 200,
                    "message" => $success
                ), JSON_UNESCAPED_UNICODE);
            } else { // 책 정보 삭제 실패
                echo json_encode(array(
                    "code" => 400,
                    "message" => $bookinfo_query_failed
                ), JSON_UNESCAPED_UNICODE);
            }
        } else { // 내 서재 삭제 실패
            echo json_encode(array(
                "code" => 400,
                "message" => $library_query_failed
            ), JSON_UNESCAPED_UNICODE);
        }
    } else if ($type == '2') {
        $readingLib_delete_stmt = $conn->prepare("DELETE FROM BOOK_READING WHERE BOOK_READING_NUMBER = ?");
        $readingLib_delete_stmt->bind_param("i", $mylibrary_number);
        if ($readingLib_delete_stmt->execute()) { // 내 서재 읽고있는책 삭제 성공
            $readingInfo_delete_stmt = $conn->prepare("DELETE FROM BOOK_INFO WHERE BOOK_INFO_NUMBER = ? AND USER_NUMBER = ?");
            $readingInfo_delete_stmt->bind_param("ii", $book_number, $user_number);
            if ($readingInfo_delete_stmt->execute()) { // 책 정보까지 삭제 성공 
                echo json_encode(array(
                    "code" => 200,
                    "message" => $success
                ), JSON_UNESCAPED_UNICODE);
            } else { // 책 정보 삭제 실패 
                echo json_encode(array(
                    "code" => 400,
                    "message" => $bookinfo_query_failed
                ), JSON_UNESCAPED_UNICODE);
            }
        } else { // 읽고 있는 책 삭제 실패 
            echo json_encode(array(
                "code" => 400,
                "message" => $library_query_failed
            ), JSON_UNESCAPED_UNICODE);
        }

    } else if ($type == '3') { 
        $wantreadingLib_delete_stmt = $conn -> prepare("DELETE FROM BOOK_WANT WHERE BOOK_WANT_NUMBER = ?");
        $wantreadingLib_delete_stmt -> bind_param("i", $mylibrary_number); 
        if ($wantreadingLib_delete_stmt->execute()) { // 읽고 싶은 책 삭제 성공 
            $wantreadingInfo_delete_stmt = $conn -> prepare("DELETE FROM BOOK_INFO WHERE BOOK_INFO_NUMBER = ? AND USER_NUMBER = ?");
            $wantreadingInfo_delete_stmt-> bind_param("ii", $book_number, $user_number); 
            if($wantreadingInfo_delete_stmt->execute()) { // 책 정보까지 삭제 성공 
                echo json_encode(array(
                    "code" => 200,
                    "message" => $success
                ), JSON_UNESCAPED_UNICODE);
            } else {// 책 정보 삭제 실패 
                echo json_encode(array(
                    "code" => 400,
                    "message" => $bookinfo_query_failed
                ), JSON_UNESCAPED_UNICODE);
            }
        } else {  // 읽고 있는 책 삭제 실패 
            echo json_encode(array(
                "code" => 400,
                "message" => $library_query_failed
            ), JSON_UNESCAPED_UNICODE);
        }

    }
}



?> 