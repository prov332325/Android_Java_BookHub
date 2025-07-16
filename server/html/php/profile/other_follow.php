<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");


// 현재 로그인한 유저 - 팔로우/언팔로우 행동 주체 
if (isset($_POST['user_number'])) { // int 로 넘김.
    $user_number = $_POST['user_number'];
} else {
    $user_number = null;
}



// 다른 유저 번호 - 팔로우/언팔로우 당하는 사람 
if (isset($_POST['this_user_number'])) { // int 로 넘김.
    $this_user_number = $_POST['this_user_number'];
} else {
    $this_user_number = null;
}


// 현재 상태 - status tag (following, not_following) 둘 중 하나 string값 
if (isset($_POST['status_tag'])) {
    $status_tag = $_POST['status_tag'];
} else {
    $status_tag = null;
}



// 응답 
$response = array();


// 메시지 모음 
$error_msg = "필수 인자가 부족하거나, 팔로우할 수없습니다.";


// 넘어온 정보를 가지고 팔로우 팔로잉을 하는 순서. 

// 먼저 테이블에 존재하는지 확인하기. 

if ($user_number == '' || $this_user_number == '' || $status_tag == '' || $user_number == $this_user_number) {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
} else { // 필수 인자가 다 있는 경우   

    $status_true=true;
    $status_false=false;

    // 기존에 팔로우 기록 존재하는지 확인하기 
    $follow_check_stmt = $conn->prepare("SELECT EXISTS (SELECT * FROM USERS_FOLLOW_LIST 
                                        WHERE FOLLOW_FROM_USER = ? AND FOLLOW_TO_USER = ?)");
    $follow_check_stmt->bind_param("ii", $user_number, $this_user_number);

    if ($follow_check_stmt->execute()) { // 팔로우 기록 확인 쿼리 성공 

        $follow_check_stmt->bind_result($exists);
        $follow_check_stmt->fetch();
        $follow_check_stmt->close();

        if ($exists) {
            // 존재함. update 해줘야함 
            // $response['code'] = 200;
            // $response['message'] = "기록 존재함";
            // 여기에 update 로직 추가 
            $follow_update_stmt = $conn->prepare("UPDATE USERS_FOLLOW_LIST  SET FOLLOW_STATUS = ? WHERE FOLLOW_FROM_USER = ? AND FOLLOW_TO_USER = ?"); 
            if( $status_tag =='following') {
                $follow_update_stmt->bind_param("iii", $status_true, $user_number, $this_user_number);
            } elseif ($status_tag == 'not_following') {
                $follow_update_stmt->bind_param("iii", $status_false, $user_number, $this_user_number);
            } 
            
            // update 쿼리 실행
            if($follow_update_stmt->execute()) { // update 쿼리 성공
                $response['code'] = 200;
                $response['message'] = "기존기록 update완료"; 
                $response['user_number'] = $user_number;
                $response['this_user_number'] = $this_user_number;
                $response['status_now'] = $status_tag;
            } else { // update 쿼리 실패 
                $response['code'] = 400;
                $response['message'] = "기존기록 update 쿼리 execute 실패"; 
            }

        } else {
            // 존재하지 않음. insert 해줘야함 
            // $response['code'] = 200;
            // $response['message'] = "기록 없음 (insert), 현재 상태(안드에서 변경하고자하는 상태를 넘겨줌. 이렇게 처리해주면됨): " . $status_tag;
            // // 여기에 insert 로직 추가 

            $follow_insert_stmt = $conn->prepare("INSERT INTO USERS_FOLLOW_LIST (FOLLOW_FROM_USER, FOLLOW_STATUS, FOLLOW_TO_USER) VALUES (?, ?, ?)"); 

             if( $status_tag =='following') {
                $follow_insert_stmt->bind_param("iii", $user_number, $status_true, $this_user_number);
            } elseif ($status_tag == 'not_following') {
                $follow_insert_stmt->bind_param("iii", $user_number, $status_true, $this_user_number);
            } 

             // insert 쿼리 실행
             if($follow_insert_stmt->execute()) { // insert 쿼리 성공
                $response['code'] = 200;
                $response['message'] = "새 기록 insert 완료"; 
                $response['user_number'] = $user_number;
                $response['this_user_number'] = $this_user_number; 
                $response['status_now'] = $status_tag;
            } else { // insert 쿼리 실패 
                $response['code'] = 400;
                $response['message'] = "새 기록 insert 실패"; 
            }
        }

        // 팔로우 작업을 완료하고 마지막으로 서로 맞팔 관계인지 확인하기. 

        $following_check_stmt = $conn->prepare(
            "SELECT COUNT(*) AS follow_count 
             FROM USERS_FOLLOW_LIST 
             WHERE 
                (FOLLOW_FROM_USER = ? AND FOLLOW_TO_USER = ? AND FOLLOW_STATUS = 1) 
                OR 
                (FOLLOW_FROM_USER = ? AND FOLLOW_TO_USER = ? AND FOLLOW_STATUS = 1)"
        );
        $following_check_stmt->bind_param("iiii", $user_number, $this_user_number, $this_user_number, $user_number); 

        if($following_check_stmt->execute()) {
            $following_check_stmt->store_result(); 
            $following_check_stmt->bind_result($follow_cnt);
            $following_check_stmt->fetch();

            // 맞팔인 경우 
            if($follow_cnt == 2) {
                $response['follow_each_other'] = 1; 
            } else {
                $response['follow_each_other'] = 2; 
            }
        } else {
            error_log(json_last_error_msg($_POST));
        }



    } else {
        // 팔로우 기록 확인 select 쿼리 실패 
        $response['code'] = 400;
        $response['message'] = "기존 기록 확인 select 쿼리 실패"; 
    }
}


echo json_encode($response, JSON_UNESCAPED_UNICODE);
