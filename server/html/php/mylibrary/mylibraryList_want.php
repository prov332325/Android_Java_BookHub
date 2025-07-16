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
    $want_list_stmt = $conn->prepare("SELECT 
    BOOK_INFO.BOOK_INFO_NUMBER,
        BOOK_INFO.BOOK_TITLE, 
        BOOK_INFO.BOOK_AUTHOR, 
        BOOK_INFO.BOOK_COVER,
        BOOK_WANT.BOOK_WANT_NUMBER,
        BOOK_WANT.BOOK_WANT_USER_NUMBER,
        BOOK_WANT.WANT_RATING, 
        BOOK_WANT.WANT_PREVIEW,
        BOOK_WANT.CREATE_TIME AS WANT_CREATE_TIME 
    FROM 
    BOOK_INFO
    LEFT JOIN 
    BOOK_WANT ON BOOK_INFO.BOOK_INFO_NUMBER = BOOK_WANT.BOOK_INFO_NUMBER
    WHERE 
    BOOK_WANT.BOOK_WANT_USER_NUMBER = ?");
    $want_list_stmt->bind_param("i", $user_number);

    if ($want_list_stmt->execute()) {
        $result = $want_list_stmt->get_result();

        if ($result->num_rows > 0) {
            $response['code'] = 200; // Response code 세팅
            $response['message'] = $yes_result; // Response message 세팅
            $response['item'] = array(); // items 배열 초기화

            while ($row = $result->fetch_assoc()) {
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
                    'type' => "3"
                );
                $response['item'][] = $library_data;
            }
        } else {
            $response['code'] = 200;
            $response['message'] = $no_result;
        }  // '게시글 없는 경우' 닫는 괄호
    } else { // select 쿼리문 실패
        $response['code'] = 400;
        $response['message'] = $query_failed;
        echo json_encode($response, JSON_UNESCAPED_UNICODE);
        exit; // 스크립트 종료
    }
}
echo json_encode($response, JSON_UNESCAPED_UNICODE);
?>