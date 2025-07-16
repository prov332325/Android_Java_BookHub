<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");

// 5개만 가져오는 로직. 
// 전체 건수도 가져오기 
// 내가 쓴 게시글, 상대가 쓴 게시글 전부 이걸로 가져오게끔 !! user number 만 잘 넘겨주기. 


if (isset($_POST["user_number"])) {
    $user_number =  $_POST['user_number'];
} else {
    $user_number = null;
}


if (isset($_POST["offset"])) {
    $offset =  $_POST['offset'];
} else {
    $offset = null;
}


if (isset($_POST["limit"])) {
    $limit =  $_POST['limit'];
} else {
    $limit = null;
}



// message - 유저 number 안넘어온경우, select문 실패할 경우, 결과가 없는 경우, 결과가 읽은책, 읽고 있는 책, 읽고 싶은 책인 경우, 
$error_msg = "필수 인자 user number 안넘어옴";
$query_failed = "select 쿼리문 실행 실패";
$yes_result = "게시글 존재!";
$no_result = "게시글이 없습니다";


// 응답 
$response = array();


// user_number 값 확인하기 
if (empty($user_number)) {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    error_log(json_last_error_msg($_POST));
} else {


    // 전체 게시글 수 가져오기
$total_posts_stmt = $conn->prepare("
SELECT COUNT(*) AS total_count
FROM BOARD b
WHERE b.USER_NUMBER = ?
");

$total_posts_stmt->bind_param("i", $user_number);

if ($total_posts_stmt->execute()) {
$total_posts_result = $total_posts_stmt->get_result();
$total_posts_row = $total_posts_result->fetch_assoc();
$total_posts_count = $total_posts_row['total_count'];
} else {
$response['code'] = 400;
$response['message'] = $query_failed;
echo json_encode($response, JSON_UNESCAPED_UNICODE);
exit;
}




    // 게시글 먼저 !! 5개만 불러오기 
    $profile_board_list_stmt = $conn->prepare("SELECT 
    b.BOARD_ID, 
    b.USER_NUMBER, 
    b.BOARD_TITLE, 
    b.BOARD_CONTENT, 
    b.BOARD_CATEGORY, 
    b.CREATE_TIME,
    COALESCE(c.comment_count, 0) AS comment_count,
    COALESCE(l.like_count, 0) AS like_count
FROM 
    BOARD b
LEFT JOIN 
    (SELECT BOARD_ID, COUNT(*) AS comment_count 
     FROM BOARD_COMMENTS 
     GROUP BY BOARD_ID) c ON b.BOARD_ID = c.BOARD_ID
LEFT JOIN 
    (SELECT BOARD_ID, COUNT(*) AS like_count 
     FROM BOARD_LIKE 
     WHERE LIKE_STATUS = TRUE 
     GROUP BY BOARD_ID) l ON b.BOARD_ID = l.BOARD_ID
     WHERE b.USER_NUMBER = ? 
    ");

    $profile_board_list_stmt->bind_param("i", $user_number);

    if ($profile_board_list_stmt->execute()) {
        $result = $profile_board_list_stmt->get_result();

        if ($result->num_rows > 0) { // 게시글 존재 
            $response['code'] = 200; // response code 세팅 
            $response['message'] = $yes_result; // message 
            $response['total_cnt'] = $total_posts_count;
            $response['item'] = array(); // items 배열 초기화 

            while ($row = $result->fetch_assoc()) {
                $data = array(
                    'code' => 200,
                    'message' => "게시글 전체 불러오기성공",
                    'user_number' => $row['USER_NUMBER'],
                    'board_number' => $row['BOARD_ID'],
                    'title' => $row['BOARD_TITLE'],
                    'content' => $row['BOARD_CONTENT'],
                    'category' => $row['BOARD_CATEGORY'],
                    'createdTime' => $row['CREATE_TIME'],
                    'comment_cnt' => $row['comment_count'],
                    'like_cnt' => $row['like_count']
                );
                $response['item'][] = $data;
            }
        } else { // 해당 유저가 작성한 게시글 없음. 
            $response['code'] = 400;
            $response['message'] = $no_result;
        }
    } else { // 쿼리문 execute 실패 
        $response['code'] = 400;
        $response['message'] = $query_failed;
    }
} // user number 있을때 !
echo json_encode($response, JSON_UNESCAPED_UNICODE);
