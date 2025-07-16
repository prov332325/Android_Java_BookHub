<?php

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");

if (isset($_POST["user_number"]))
{
  $user_number =  intval($_POST['user_number']);
 // echo $user_number;
 // echo " is your user_number"; // 이거 띄우면 계속 오류  뜸 
 // 오류 내용: 
 //     E  onFailure: java.lang.IllegalStateException: Expected BEGIN_OBJECT but was NUMBER at line 1 column 2 path $
} 
else 
{
  $user_number = null;
 // echo "no username supplied";
}

error_reporting(E_ALL);
ini_set("display_errors", 1 );

// 에러가 발생한 시간 기록
error_log("에러가 발생한 시간: " . date("Y-m-d H:i:s"));


// 결과 값 분기 처리해서 type 정해서 데이터 응답 json에 담아
// response 해주기. 

// message - 유저 number 안넘어온경우, select문 실패할 경우, 결과가 없는 경우, 결과가 읽은책, 읽고 있는 책, 읽고 싶은 책인 경우, 
$error_msg = "필수 인자 user number 안넘어옴";
$query_failed = "select 쿼리문 실행 실패";
$yes_result = "게시글 존재!";
$no_result = "게시글이 없습니다";

// 응답 
$response = array();

// null 값 확인하기. 
if ($user_number == '') { 
    $response['code'] = 400;
    $response['message'] = $error_msg;
    error_log(json_last_error_msg($_POST));
} else {
    $whole_list_stmt = $conn->prepare("SELECT 
BOOK_INFO.BOOK_INFO_NUMBER,
    BOOK_INFO.BOOK_TITLE, 
    BOOK_INFO.BOOK_AUTHOR, 
    BOOK_INFO.BOOK_COVER,
    BOOK_INFO.BOOK_SAVE_TYPE,
    BOOK_ALREADY.BOOK_ALREADY_NUMBER, 
    BOOK_ALREADY.BOOK_ALREADY_USER_NUMBER,
    BOOK_ALREADY.ALREADY_STARTED_DATE, 
    BOOK_ALREADY.ALREADY_FINISHED_DATE, 
    BOOK_ALREADY.ALREADY_RATING,
    BOOK_ALREADY.CREATE_TIME AS ALREADY_CREATE_TIME,
    BOOK_READING.BOOK_READING_NUMBER, 
    BOOK_READING.BOOK_READING_USER_NUMBER,
    BOOK_READING.READING_STARTED_DATE, 
    BOOK_READING.READING_PAGE, 
    BOOK_READING.CREATE_TIME AS READING_CREATE_TIME,
    BOOK_WANT.BOOK_WANT_NUMBER,
    BOOK_WANT.BOOK_WANT_USER_NUMBER,
    BOOK_WANT.WANT_RATING, 
    BOOK_WANT.WANT_PREVIEW,
    BOOK_WANT.CREATE_TIME AS WANT_CREATE_TIME
FROM 
BOOK_INFO
LEFT JOIN 
BOOK_ALREADY ON BOOK_INFO.BOOK_INFO_NUMBER = BOOK_ALREADY.BOOK_INFO_NUMBER
LEFT JOIN 
BOOK_READING ON BOOK_INFO.BOOK_INFO_NUMBER = BOOK_READING.BOOK_INFO_NUMBER
LEFT JOIN 
BOOK_WANT ON BOOK_INFO.BOOK_INFO_NUMBER = BOOK_WANT.BOOK_INFO_NUMBER
WHERE 
BOOK_ALREADY.BOOK_ALREADY_USER_NUMBER = ? or
BOOK_READING.BOOK_READING_USER_NUMBER = ? or
BOOK_WANT.BOOK_WANT_USER_NUMBER = ?");
    $whole_list_stmt->bind_param("iii", $user_number,$user_number,$user_number);
    if ($whole_list_stmt->execute()) {
        $result = $whole_list_stmt->get_result();
        if ($result->num_rows > 0) {
            $response['code'] = 200; // Response code 세팅
            $response['message'] = $yes_result; // Response message 세팅
            $response['item'] = array(); // items 배열 초기화
            while ($row = $result->fetch_assoc()) {
                if ( empty($row['READING_STARTED_DATE'] ) && empty($row['WANT_RATING']) ) {
                    $library_data = array(
                        'code' => 200,
                        'message' => "내서재 읽은책",
                        'mylibrary_number' => $row['BOOK_ALREADY_NUMBER'],
                        'mylibrary_user_number' => $row['BOOK_ALREADY_USER_NUMBER'],
                        'book_number' => $row['BOOK_INFO_NUMBER'],
                        'cover' => $row['BOOK_COVER'],
                        'title' => $row['BOOK_TITLE'],
                        'author' => $row['BOOK_AUTHOR'],
                        'started' => $row['ALREADY_STARTED_DATE'],
                        'finished' => $row['ALREADY_FINISHED_DATE'],
                        'rating' => $row['ALREADY_RATING'],
                        'createdTime' => $row['ALREADY_CREATE_TIME'],
                        'type' => "1",
                        'save_type' => $row['BOOK_SAVE_TYPE']
                    );
        
                } else if ( 
                    empty($row['ALREADY_STARTED_DATE']) && empty($row['WANT_RATING']))
                    { $library_data = array(
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
                        'type' => "2",
                        'save_type' => $row['BOOK_SAVE_TYPE']
                    );

                } else if ( // 읽고 싶은 책
                   empty ($row['ALREADY_STARTED_DATE'] )&& empty ($row['ALREADY_FINISHED_DATE'])  &&
                   empty( $row['ALREADY_RATING']) && empty ($row['READING_STARTED_DATE']) && empty( $row['READING_PAGE'])
                ) {
                    $library_data = array(
                        'code' => 200,
                        'message' => "내서재 읽고 싶은 책 ",
                        'mylibrary_number' => $row['BOOK_WANT_NUMBER'],
                     'mylibrary_user_number' => $row['BOOK_WANT_USER_NUMBER'],
                        'book_number' => $row['BOOK_INFO_NUMBER'],
                        'cover' => $row['BOOK_COVER'],
                        'title' => $row['BOOK_TITLE'],
                        'author' => $row['BOOK_AUTHOR'],
                        'rating' => $row['WANT_RATING'],
                        'preview' => $row['WANT_PREVIEW'],
                        'createdTime' => $row['WANT_CREATE_TIME'],
                        'type' => "3",
                        'save_type' => $row['BOOK_SAVE_TYPE'] );

                } // else if 끝 

                  // 각 행의 정보를 배열에 추가
                  $response['item'][] = $library_data;

            } // while 문 끝 
        } else {
            $response['code'] = 200;
            $response['message'] = $no_result;
        } // 게시글 없는 경우 끝. 
    } else { 
        $response['code'] = 400;
        $response['message'] = $query_failed;
    }
}  

echo json_encode($response, JSON_UNESCAPED_UNICODE);

?>