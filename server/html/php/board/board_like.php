<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");





// 유저 넘버 - 좋아요, 좋취 실행하는 주체 
if (isset($_POST['user_number'])) { // int 로 넘김.
    $user_number = $_POST['user_number'];
} else {
    $user_number = null;
}



// 현재 게시글번호 
if (isset($_POST['board_number'])) { // int 로 넘김.
    $board_number = $_POST['board_number'];
} else {
    $board_number = null;
}


// 현재 상태 - status tag (clicked_like, cliked_unlike) 둘 중 하나 string값 
if (isset($_POST['status_tag'])) {
    $status_tag = $_POST['status_tag'];
} else {
    $status_tag = null;
}




// 응답 
$response = array();


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";



// 먼저 테이블에 존재하는지 확인하기 user number 랑 board number 가 일치하는 row 있는지. 

if ($user_number == '' || $board_number == '' || $status_tag == '') {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
} else {

    $status_true = true;
    $status_false = false;


    // 기존 좋아요 기록 존재하는지 확인하기 
    $like_check_stmt = $conn->prepare("SELECT EXISTS (SELECT * FROM BOARD_LIKE 
                                        WHERE USER_NUMBER = ? AND BOARD_ID = ?)");

    $like_check_stmt->bind_param("ii", $user_number, $board_number);
    if ($like_check_stmt->execute()) { // 좋아요 기록 확인 쿼리 성공 

        $like_check_stmt->bind_result(($exists));
        $like_check_stmt->fetch();
        $like_check_stmt->close();

        if ($exists) {
            // 존재함. update 해줘야함 
            // update 로직 추가하기 
            $like_update_stmt = $conn->prepare("UPDATE BOARD_LIKE  SET LIKE_STATUS = ? WHERE USER_NUMBER = ? AND BOARD_ID = ?");
            if ($status_tag == 'clicked_like') { // 좋아요를 누른 경우 
                $like_update_stmt->bind_param("iii", $status_true, $user_number, $board_number);
            } else if ($status_tag == 'clicked_unlike') { // 좋아요 취소한 경우 
                $like_update_stmt->bind_param("iii", $status_false, $user_number, $board_number);
            }




            // update 쿼리 실행 
            if ($like_update_stmt->execute()) { // update 쿼리 성공


                $like_cnt_stmt = $conn->prepare("SELECT COUNT(*) FROM BOARD_LIKE WHERE BOARD_ID = ? AND LIKE_STATUS = true");
                $like_cnt_stmt->bind_param("i", $board_number);
                if ($like_cnt_stmt->execute()) {
                    $like_cnt_stmt->bind_result($cnt);
                    $like_cnt_stmt->fetch();
                    $like_cnt_stmt->close();
                    $response['like_cnt'] = $cnt;
                }


                $response['code'] = 200;
                $response['message'] = "기존 좋아요 기록 update완료";
                $response['user_number'] = $user_number;
                $response['board_number'] = $board_number;
                $response['status_now'] = $status_tag;
            } else { // update 쿼리 실패 
                $response['code'] = 400;
                $response['message'] = "기존 좋아요 기록 update 쿼리 execute 실패";
            }
        } else {
            // 좋아요 했던 기록 존재하지 않음. insert 해줘야함. 
            $like_insert_stmt = $conn->prepare("INSERT INTO BOARD_LIKE (USER_NUMBER, BOARD_ID, LIKE_STATUS) VALUES (?, ?, ?)");

            if ($status_tag == 'clicked_like') { // 좋아요를 누른 경우 
                $like_insert_stmt->bind_param("iii", $user_number, $board_number, $status_true);
            } else if ($status_tag == 'clicked_unlike') { // 좋아요 취소한 경우 
                $like_insert_stmt->bind_param("iii", $user_number, $board_number, $status_false);
            }



            // insert 쿼리 실행
            if ($like_insert_stmt->execute()) {



                $like_cnt_stmt = $conn->prepare("SELECT COUNT(*) FROM BOARD_LIKE WHERE BOARD_ID = ? AND LIKE_STATUS = true");
                $like_cnt_stmt->bind_param("i", $board_number);
                if ($like_cnt_stmt->execute()) {
                    $like_cnt_stmt->bind_result($cnt);
                    $like_cnt_stmt->fetch();
                    $like_cnt_stmt->close();
                    $response['like_cnt'] = $cnt;
                }


                
                $response['code'] = 200;
                $response['message'] = "기존 좋아요 기록 insert 완";
                $response['user_number'] = $user_number;
                $response['board_number'] = $board_number;
                $response['status_now'] = $status_tag;
            } else { // insert 쿼리 실패 
                $response['code'] = 400;
                $response['message'] = "새 좋아요 기록 insert 실패"; 

            }
        }
    } else {
         // 좋아요 기록 확인 select 쿼리 실패 
         $response['code'] = 400;
         $response['message'] = "기존 기록 확인 select 쿼리 실패"; 
    } 

    // 맨 마지막 여기서 쿼리 돌리고 추가!! 
}


echo json_encode($response, JSON_UNESCAPED_UNICODE);
