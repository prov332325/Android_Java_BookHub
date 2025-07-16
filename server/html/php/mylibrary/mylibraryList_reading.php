<?php

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");


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

// message - 유저 number 안넘어온경우, select문 실패할 경우, 결과가 없는 경우, 결과가 읽은책, 읽고 있는 책, 읽고 싶은 책인 경우, 
$error_msg = "필수 인자 user number 안넘어옴";
$query_failed = "select 쿼리문 실행 실패";
$yes_result = "게시글 존재!";
$no_result = "게시글이 없습니다";

// 응답 
$response = array();
 


if ($user_number === null) {
    // 오류 처리
    $response['code'] = 400;
    $response['message'] = $error_msg;
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit; // 스크립트 종료
} else {

    // stmt 
    $reading_list_stmt = $conn->prepare("SELECT 
    BOOK_INFO.BOOK_INFO_NUMBER,
	BOOK_INFO.BOOK_TITLE, 
	BOOK_INFO.BOOK_AUTHOR, 
	BOOK_INFO.BOOK_COVER,
    BOOK_READING.BOOK_READING_NUMBER, 
    BOOK_READING.BOOK_READING_USER_NUMBER, 
	BOOK_READING.READING_STARTED_DATE, 
    BOOK_READING.READING_PAGE, 
    BOOK_READING.CREATE_TIME AS READING_CREATE_TIME
    FROM 
    BOOK_INFO
LEFT JOIN 
BOOK_READING ON BOOK_INFO.BOOK_INFO_NUMBER = BOOK_READING.BOOK_INFO_NUMBER
    WHERE 
  BOOK_READING.BOOK_READING_USER_NUMBER = ?");
    $reading_list_stmt->bind_param("i", $user_number);

    if ($reading_list_stmt->execute()) {
        $result = $reading_list_stmt->get_result();

        if ($result->num_rows > 0) {
            $response['code'] = 200; // Response code 세팅
            $response['message'] = $yes_result; // Response message 세팅
            $response['item'] = array(); // items 배열 초기화

            // 결과 가져와서 result 에 담기. 
            while ($row = $result->fetch_assoc()) {
                $library_data = array(
                    'code' => 200,
                    'message' => "내서재 읽고 있는책 ",
                    'mylibrary_number' => $row['BOOK_READING_NUMBER'],
                    'mylibrary_user_number' => $row['BOOK_READING_USER_NUMBER'],
                    'book_number' => $row['BOOK_INFO_NUMBER'],
                    'cover' => $row['BOOK_COVER'],
                    'title' => $row['BOOK_TITLE'],
                    'author' => $row['BOOK_AUTHOR'],
                    'started' => $row['READING_STARTED_DATE'],
                    'readPage' => $row['READING_PAGE'],
                    'createdTime' => $row['READING_CREATE_TIME'],
                    'type' => "2"
                );
                // 각 행의 정보를 배열에 추가
                $response['item'][] = $library_data;
            } // while문 종료 
        } else {
            $response['code'] = 200;
            $response['message'] = $no_result;
        } // // '게시글 없는 경우' 닫는 괄호

    }
}
echo json_encode($response, JSON_UNESCAPED_UNICODE);
