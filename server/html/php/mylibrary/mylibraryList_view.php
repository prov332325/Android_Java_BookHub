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
$query_failed = "select 쿼리문 실행 실패";
$yes_result = "상세보기 게시글 존재 num row 0보다큼!";
$no_result = "상세보기 결과 row 없습니다";
$success = "상세보기 select 결과 json 응답 성공 ";

if (
    $user_number == '' || $type == '' ||
    $book_number == '' || $mylibrary_number == ''
) {
    echo json_encode(array(
        "code" => 400,
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE);
} else {
    // null 값이 아니면 type별로 table select 분기처리.
    if ($type == '1') { // 읽은 책 
        $already_view_stmt = $conn->prepare("SELECT 
        BOOK_INFO.BOOK_INFO_NUMBER,
            BOOK_INFO.BOOK_TITLE, 
            BOOK_INFO.BOOK_AUTHOR, 
            BOOK_INFO.BOOK_COVER,
            BOOK_INFO.BOOK_DESCRIPTION,
            BOOK_INFO.BOOK_PUBLISHER,
            BOOK_INFO.BOOK_ISBN,
            BOOK_INFO.BOOK_SAVE_TYPE,
            BOOK_ALREADY.BOOK_ALREADY_USER_NUMBER,
            BOOK_ALREADY.ALREADY_STARTED_DATE, 
            BOOK_ALREADY.ALREADY_FINISHED_DATE, 
            BOOK_ALREADY.ALREADY_RATING,
            BOOK_ALREADY.CREATE_TIME
        FROM 
        BOOK_INFO
        LEFT JOIN 
        BOOK_ALREADY ON BOOK_INFO.BOOK_INFO_NUMBER = BOOK_ALREADY.BOOK_INFO_NUMBER
        WHERE 
        BOOK_ALREADY.BOOK_ALREADY_USER_NUMBER = ? AND BOOK_INFO.BOOK_INFO_NUMBER = ? AND BOOK_ALREADY.BOOK_ALREADY_NUMBER = ?");

        $already_view_stmt->bind_param("iii", $user_number, $book_number, $mylibrary_number);
        if ($already_view_stmt->execute()) { // 쿼리 실행 완료 
            $already_result = $already_view_stmt->get_result();
            if ($already_result->num_rows > 0) { // 위 세개 조건이 다 맞으면 결과는 무조건 1개임.  
                while ($row = $already_result->fetch_assoc()) {
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $success,
                        'user_number' => $user_number,
                        'book_number' => $book_number,
                        'mylibrary_number' => $mylibrary_number,
                        'type' => '1',
                        'cover' => $row['BOOK_COVER'],
                        'title' => $row['BOOK_TITLE'],
                        'author' => $row['BOOK_AUTHOR'],
                        'description' => $row['BOOK_DESCRIPTION'],
                        'publisher' => $row['BOOK_PUBLISHER'],
                        'isbn' => $row['BOOK_ISBN'], 
                        'save_type' => $row['BOOK_SAVE_TYPE'],
                        'started' => $row['ALREADY_STARTED_DATE'],
                        'finished' => $row['ALREADY_FINISHED_DATE'],
                        'rating' => $row['ALREADY_RATING'],
                        'createdTime' => $row['CREATE_TIME']
                    ), JSON_UNESCAPED_UNICODE);
                }
            } else {
                // 결과 없음. 
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
    } elseif ($type == '2') { // 읽고 있는 책 
        $reading_view_stmt = $conn->prepare("SELECT 
        BOOK_INFO.BOOK_INFO_NUMBER,
        BOOK_INFO.BOOK_TITLE, 
        BOOK_INFO.BOOK_AUTHOR, 
        BOOK_INFO.BOOK_COVER,
        BOOK_INFO.BOOK_DESCRIPTION,
        BOOK_INFO.BOOK_PUBLISHER,
        BOOK_INFO.BOOK_ISBN,
        BOOK_INFO.BOOK_SAVE_TYPE,
        BOOK_READING.BOOK_READING_USER_NUMBER,
        BOOK_READING.READING_STARTED_DATE, 
        BOOK_READING.READING_PAGE, 
        BOOK_READING.CREATE_TIME
        FROM 
        BOOK_INFO
    LEFT JOIN 
    BOOK_READING ON BOOK_INFO.BOOK_INFO_NUMBER = BOOK_READING.BOOK_INFO_NUMBER
        WHERE 
      BOOK_READING.BOOK_READING_USER_NUMBER = ? AND  BOOK_INFO.BOOK_INFO_NUMBER = ? AND BOOK_READING.BOOK_READING_NUMBER = ?");
        $reading_view_stmt->bind_param("iii",  $user_number, $book_number, $mylibrary_number);
        if ($reading_view_stmt->execute()) { // 쿼리 실행 성공. 
            $reading_result = $reading_view_stmt->get_result();
            if ($reading_result->num_rows > 0) {
                while ($row = $reading_result->fetch_assoc()) {
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $success,
                        'user_number' => $user_number,
                        'book_number' => $book_number,
                        'mylibrary_number' => $mylibrary_number,
                        'type' => '2',
                        'cover' => $row['BOOK_COVER'],
                        'title' => $row['BOOK_TITLE'],
                        'author' => $row['BOOK_AUTHOR'],
                        'description' => $row['BOOK_DESCRIPTION'],
                        'publisher' => $row['BOOK_PUBLISHER'],
                        'isbn' => $row['BOOK_ISBN'], 
                        'save_type' => $row['BOOK_SAVE_TYPE'],
                        'started' => $row['READING_STARTED_DATE'],
                        'readPage' => $row['READING_PAGE'],
                        'createdTime' => $row['CREATE_TIME']
                    ), JSON_UNESCAPED_UNICODE);
                } // while 끝 

            } else { // 결과 없음. 
                echo json_encode(array(
                    "code" => 400,
                    "message" => $no_result
                ), JSON_UNESCAPED_UNICODE);
            }
        } else { // 쿼리 실패 
            echo json_encode(array(
                "code" => 400,
                "message" => $query_failed
            ), JSON_UNESCAPED_UNICODE);
        }
    } elseif ($type == '3') { // 읽고 싶은 책
        $want_view_stmt = $conn->prepare("SELECT 
        BOOK_INFO.BOOK_INFO_NUMBER,
            BOOK_INFO.BOOK_TITLE, 
            BOOK_INFO.BOOK_AUTHOR, 
            BOOK_INFO.BOOK_COVER,
            BOOK_INFO.BOOK_DESCRIPTION,
            BOOK_INFO.BOOK_PUBLISHER,
            BOOK_INFO.BOOK_ISBN,
            BOOK_INFO.BOOK_SAVE_TYPE,
            BOOK_WANT.BOOK_WANT_USER_NUMBER, 
            BOOK_WANT.WANT_RATING, 
            BOOK_WANT.WANT_PREVIEW,
            BOOK_WANT.CREATE_TIME 
        FROM 
        BOOK_INFO
        LEFT JOIN 
        BOOK_WANT ON BOOK_INFO.BOOK_INFO_NUMBER = BOOK_WANT.BOOK_INFO_NUMBER
        WHERE 
         BOOK_WANT.BOOK_WANT_USER_NUMBER = ? AND  BOOK_WANT.BOOK_INFO_NUMBER = ? AND BOOK_WANT.BOOK_WANT_NUMBER = ?");
        $want_view_stmt->bind_param("iii", $user_number, $book_number, $mylibrary_number);
        if ($want_view_stmt->execute()) { // 쿼리 실행 완료 
            $want_result = $want_view_stmt->get_result();
            if ($want_result->num_rows > 0) {
                while ($row = $want_result->fetch_assoc()) {
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $success,
                        'user_number' => $user_number,
                        'book_number' => $book_number,
                        'mylibrary_number' => $mylibrary_number,
                        'type' => '3',
                        'cover' => $row['BOOK_COVER'],
                        'title' => $row['BOOK_TITLE'],
                        'author' => $row['BOOK_AUTHOR'],
                        'description' => $row['BOOK_DESCRIPTION'],
                        'publisher' => $row['BOOK_PUBLISHER'],
                        'isbn' => $row['BOOK_ISBN'], 
                        'save_type' => $row['BOOK_SAVE_TYPE'],
                        'rating' => $row['WANT_RATING'],
                        'preview' => $row['WANT_PREVIEW'],
                        'createdTime' => $row['CREATE_TIME']
                    ), JSON_UNESCAPED_UNICODE);
                }
            } else { // 결과 없음 
                echo json_encode(array(
                    "code" => 400,
                    "message" => $no_result
                ), JSON_UNESCAPED_UNICODE);
            }
        } else { // 쿼리 실패 
            echo json_encode(array(
                "code" => 400,
                "message" => $query_failed
            ), JSON_UNESCAPED_UNICODE);
        }
    } // 읽고 싶은 책 끝
}
