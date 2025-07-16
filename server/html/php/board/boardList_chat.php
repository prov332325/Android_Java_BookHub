<?php

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");


if (isset($_POST["user_number"])) {
    $user_number =  intval($_POST['user_number']);
} else {
    $user_number = null;
}



error_reporting(E_ALL);
ini_set("display_errors", 1);

// 에러가 발생한 시간 기록
error_log("에러가 발생한 시간: " . date("Y-m-d H:i:s"));


// message - 유저 number 안넘어온경우, select문 실패할 경우, 결과가 없는 경우, 결과가 읽은책, 읽고 있는 책, 읽고 싶은 책인 경우, 
$error_msg = "필수 인자 user number 안넘어옴";
$query_failed = "select 쿼리문 실행 실패";
$yes_result = "게시글 존재!";
$no_result = "게시글이 없습니다"; 


// 응답 
$response = array();
 
// user number 값 확인하기 
if ($user_number == '') {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    error_log(json_last_error_msg($_POST));

} else { 

    $chat_stmt = $conn->prepare("SELECT 
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
WHERE  
    b.BOARD_CATEGORY = '잡담'");
    // $chat_stmt->bind_param("i", $user_number); 
    if ($chat_stmt->execute()) { // 게시글 전체 select 문 성공
        $result = $chat_stmt->get_result();
        if ($result->num_rows > 0) { // 게시글 존재 
            $response['code'] = 200; // response code 세팅 
            $response['message'] = $yes_result; // message 
            $response['item'] = array(); // items 배열 초기화   
            while ($row = $result->fetch_assoc()) {
                $board_data = array(
                    'code' => 200,
                    'message' => "잡담 게시글 불러오기성공",
                    'user_number' => $row['USER_NUMBER'],
                    'board_number' => $row['BOARD_ID'],
                    'title' => $row['BOARD_TITLE'],
                    'content' => $row['BOARD_CONTENT'],
                    'category' => $row['BOARD_CATEGORY'],
                    'createdTime' => $row['CREATE_TIME'],
                    'comment_cnt' =>$row['comment_count'],
                    'like_cnt' => $row['like_count']
                );
                $response['item'][] = $board_data;
            }
        } else { // 게시글 없음 
            $response['code'] = 400;
            $response['message'] = $no_result;
        }
    } else { // 게시글 쿼리 실패
        $response['code'] = 400;
        $response['message'] = $query_failed;
    }
}

echo json_encode($response, JSON_UNESCAPED_UNICODE);


?>
