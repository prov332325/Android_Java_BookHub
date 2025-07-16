<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");

// 유저번호, 카테고리, 보드번호(int)

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


// 카테고리  String 값으로 넘어옴. 
if (isset($_POST["category"])) {
    $category = $_POST['category'];
} else {
    $category = null;
}


// board 번호 - int 
if (isset($_POST["board_number"])) {
    $board_number =  intval($_POST['board_number']);
} else {
    $board_number = null;
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



// 응답 
$response = array(); 

// 책 정보 
// $book_data = array();


if (
    $user_number == '' || $category == '' ||
    $board_number == ''
) {
    $response['code'] = 400;
    $response['message'] = $error_msg;
} else {

    // null 값이 아니면, board table에서 가져오기 
    $board_view_stmt = $conn->prepare(
        "SELECT b.BOARD_ID, b.USER_NUMBER, u.USER_NICKNAME, u.USER_PROFILE_IMG, u.USER_EMAIL_ID, b.BOARD_TITLE, b.BOARD_CONTENT, b.BOARD_CATEGORY, b.CREATE_TIME
        FROM BOARD b 
        JOIN USERS u ON b.USER_NUMBER = u.USER_NUMBER
        WHERE b.BOARD_ID = ? AND b.BOARD_CATEGORY = ?");
    $board_view_stmt->bind_param("is", $board_number, $category);
    if ($board_view_stmt->execute()) { // 쿼리 실행 성공 
        $result = $board_view_stmt->get_result();
        if ($result->num_rows > 0) {
            while ($row = $result->fetch_assoc()) {
                $response['code'] = 200;
                $response['message'] = $success;
                $response['user_number'] =  $row['USER_NUMBER'];
                $response['user_nickname'] = $row['USER_NICKNAME'];
                $response['user_profile_img'] = $row['USER_PROFILE_IMG'];
                $response['user_emailID'] = $row['USER_EMAIL_ID'];
                $response['board_number'] = $board_number;
                $response['title'] = $row['BOARD_TITLE'];
                $response['content'] = $row['BOARD_CONTENT'];
                $response['category'] = $row['BOARD_CATEGORY'];
                $response['createdTime'] = $row['CREATE_TIME'];
            } // while 끝
        } // 결과값 있는 경우 끝 

        // 추가한 책 있는지. 있으면 $book_data 에다가 담기. 
        // $response['item'] = array();
        // $response['item'][] = $book_data

        $board_book_stmt = $conn->prepare("SELECT BOOK_TITLE, BOOK_AUTHOR, BOOK_DESCRIPTION, BOOK_COVER, BOARD_NUMBER FROM BOOK_INFO WHERE BOARD_NUMBER = ? ");
        $board_book_stmt->bind_param("i", $board_number);

        if($board_book_stmt->execute()){ // 게시글 책 정보 쿼리 실행 성공  
            $book_result = $board_book_stmt -> get_result(); // result 는 배열임. 
            if($book_result-> num_rows>0) { // 책 정보 있음. 
                $response['item'] = array();
                while ($row = $book_result -> fetch_assoc()) {
                    $book_data = array (
                        // 책 제목, 작가, 커버, 책 소개
                        'title' => $row['BOOK_TITLE'], 
                        'author' =>  $row['BOOK_AUTHOR'],
                        'description' => $row['BOOK_DESCRIPTION'],
                        'cover' => $row['BOOK_COVER']
                        // 아악 책 제목도 title이고, 게시글 제목도 title임.. 아오
                        // 아니야 둘은 데이터 타입 (클래스) 자체가 달라서 괜찮아. 
                    );
                    $response['item'][] = $book_data;
                }
            } else { // 해당 게시글에는 책 없음. 
                $success .=", 저장한 책 없음.";
            }
        } else {
            $response['code'] = 400;
            $response['message'] = $query_failed . ", 책 정보 쿼리 실패.";
        }

        // 댓글 정보도 가지고 가기........  
        $board_comments_stmt = $conn->prepare("SELECT u.USER_NUMBER, u.USER_NICKNAME, u.USER_PROFILE_IMG, bc.BOARD_COMMENT_NUMBER, bc.BOARD_ID, bc.BOARD_COMMENT_CONTENT, bc.CREATE_TIME, bc.UPDATE_TIME
                                                FROM BOARD_COMMENTS bc
                                                JOIN USERS u
                                                ON u.USER_NUMBER = bc.USER_NUMBER
                                                WHERE BOARD_ID = ?");
        $board_comments_stmt -> bind_param("i", $board_number); 
        
        if($board_comments_stmt->execute()){ // 게시글 댓글 정보 쿼리 실행 성공 
            $comment_result = $board_comments_stmt-> get_result(); // result 는 배열임 
            $comment_count = $comment_result->num_rows; // 댓글 개수 !!! 
            $response['comment_cnt'] = $comment_count;
            if($comment_result -> num_rows > 0) { // 책 정보 1개 이상임. 
                $response['comment_item'] = array(); 
                
                while ($row = $comment_result -> fetch_assoc()) {
                    $comment_data = array (
                        // 댓글 작성자 번호, 댓글 작성자 닉네임, 댓글 작성자 프사, 댓글 번호, 댓글 내용, 댓글 작성 날짜시간 , (게시글번호는 없어도됨) 
                        'user_number' => $row['USER_NUMBER'],
                        'user_nickname' => $row['USER_NICKNAME'], 
                        'user_profileImg' => $row['USER_PROFILE_IMG'], 
                        'comment_number' => $row['BOARD_COMMENT_NUMBER'], 
                        'comment_content' => $row['BOARD_COMMENT_CONTENT'], 
                        'createdTime' => $row['CREATE_TIME'],
                        'updateTime' => $row['UPDATE_TIME']
                    );
                    $response['comment_item'][] = $comment_data;

                }
            } else { // 해당 게시글에는 댓글 없음
                $success .=", 댓글 없음.";
            }
        }              
        
        
        // 좋아요 개수 들고가기 
        $like_cnt_stmt = $conn->prepare("SELECT COUNT(*) FROM BOARD_LIKE WHERE BOARD_ID = ? AND LIKE_STATUS= true");
        $like_cnt_stmt->bind_param("i", $board_number); 
        
        if ($like_cnt_stmt->execute()) {
            $like_cnt_stmt->bind_result($cnt);
            $like_cnt_stmt->fetch();
            $like_cnt_stmt->close();

            $response['like_cnt'] = $cnt;
        }


        // 내가 좋아요 눌렀는지 안눌렀는지 
        $like_check_stmt = $conn->prepare("SELECT EXISTS (SELECT * FROM BOARD_LIKE 
                                        WHERE USER_NUMBER = ? AND BOARD_ID = ? AND LIKE_STATUS= true)");
        $like_check_stmt->bind_param("ii", $user_number, $board_number);
        if ($like_check_stmt->execute()) {

            $like_check_stmt->bind_result(($exists));
            $like_check_stmt->fetch();
            $like_check_stmt->close();

            if ($exists) {
                $response['like_or_not'] = 1; // 이미 눌렀음
            } else {
                $response['like_or_not'] = 2;
            }
        }                                


    } else { // 게시글 내용가져오는 쿼리 실행 실패 
        $response['code'] = 400;
        $response['message'] = $query_failed;
    }
}

echo json_encode($response, JSON_UNESCAPED_UNICODE);
