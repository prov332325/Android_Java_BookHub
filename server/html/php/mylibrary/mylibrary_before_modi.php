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

// error_reporting(E_ALL);
// ini_set("display_errors", 1);

// // 에러가 발생한 시간 기록
// error_log("에러가 발생한 시간: " . date("Y-m-d H:i:s"));

// message 
$success = "before 수정 json 응답 성공 ";
$error_msg = "필수 인자 user number 안넘어옴"; 
$no_result = "before 수정 결과 row 없습니다";
$query_failed = "select 쿼리문 실행 실패";

if (
    $user_number == '' || $type == '' ||
    $book_number == '' || $mylibrary_number == ''
) {
    echo json_encode(array(
        "code" => 400,
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE);
} else {
    
    if($type == '1') { // 읽은 책 
        $already_stmt = $conn->prepare("SELECT * FROM BOOK_ALREADY WHERE BOOK_ALREADY_USER_NUMBER = ? AND BOOK_INFO_NUMBER = ? AND BOOK_ALREADY_NUMBER = ?");
        $already_stmt->bind_param("iii",$user_number, $book_number, $mylibrary_number);
        if ($already_stmt->execute()) { // 읽은 책 쿼리 실행 완료 
            $already_result = $already_stmt->get_result(); 
            if($already_result->num_rows >0) { 
                while ($row = $already_result->fetch_assoc()) {
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $success,
                        'user_number' => $user_number,
                        'mylibrary_number' => $mylibrary_number,
                        'type' => '1',
                        'started' => $row['ALREADY_STARTED_DATE'],
                        'finished' => $row['ALREADY_FINISHED_DATE'],
                        'rating' => $row['ALREADY_RATING'],
                        'createdTime' => $row['CREATE_TIME']
                    ), JSON_UNESCAPED_UNICODE);

                } // while 끝 
            } // 검색 결과 없음. 
            else {
                echo json_encode(array(
                    "code" => 400,
                    "message" => $no_result
                ), JSON_UNESCAPED_UNICODE);
            }
        } else { // 쿼리 실행 실패 
            echo json_encode(array(
                "code" => 400,
                "message" => $query_failed
            ), JSON_UNESCAPED_UNICODE);

        }

    } else if($type == '2') { // 읽고 있는 책일 경우 
        $reading_stmt = $conn->prepare("SELECT * FROM BOOK_READING WHERE BOOK_READING_USER_NUMBER = ? AND BOOK_INFO_NUMBER = ? AND BOOK_READING_NUMBER = ?");
        $reading_stmt->bind_param("iii",$user_number, $book_number, $mylibrary_number);
        if ($reading_stmt->execute()) { // 쿼리 실행 완료 
            $reading_result = $reading_stmt->get_result(); 
            if($reading_result -> num_rows > 0) {
                while ($row = $reading_result -> fetch_assoc()) {
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $success,
                        'user_number' => $user_number,
                        'mylibrary_number' => $mylibrary_number,
                        'type' => '2',
                        'started' => $row['READING_STARTED_DATE'],
                        'readPage' => $row['READING_PAGE'],
                        'createdTime' => $row['CREATE_TIME']
                    ), JSON_UNESCAPED_UNICODE);
                } // while 문 끝 
            } else { // 검색 결과 없음
                echo json_encode(array(
                    "code" => 400,
                    "message" => $no_result
                ), JSON_UNESCAPED_UNICODE);
            }

        } else { // 쿼리 실행 실패 
            echo json_encode(array(
                "code" => 400,
                "message" => $query_failed
            ), JSON_UNESCAPED_UNICODE);
        } 

    } else if ($type == '3') { // 읽고 싶은 책일 경우  
        $want_stmt = $conn->prepare("SELECT * FROM BOOK_WANT WHERE BOOK_WANT_USER_NUMBER = ? AND BOOK_INFO_NUMBER = ? AND BOOK_WANT_NUMBER = ?");
        $want_stmt->bind_param("iii",$user_number, $book_number, $mylibrary_number);
        if ($want_stmt->execute()) {
            $want_result = $want_stmt->get_result(); 
            if ($want_result->num_rows > 0) { 
                while ($row = $want_result->fetch_assoc()) {
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $success,
                        'user_number' => $user_number,
                        'mylibrary_number' => $mylibrary_number,
                        'type' => '3',
                        'rating' => $row['WANT_RATING'],
                        'preview' => $row['WANT_PREVIEW'],
                        'createdTime' => $row['CREATE_TIME']
                    ), JSON_UNESCAPED_UNICODE);
                } // while 문 끝 
            } else { // 결과 없음 
                echo json_encode(array(
                    "code" => 400,
                    "message" => $no_result
                ), JSON_UNESCAPED_UNICODE);
            }
        } else { // 쿼리 실행 실패 
            echo json_encode(array(
                "code" => 400,
                "message" => $query_failed
            ), JSON_UNESCAPED_UNICODE);
        }
    } // else if 끝 


}