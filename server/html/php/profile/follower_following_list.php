<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");

// 오류 확인하는 방법 !!!! 
// power shell 에서 
// cd /var/log/apache2/error.log -> 잘려 나옴 

//  + 오류 내용 전체 출력하는 방법 
// sudo tail error.log !! 

// 문자열 한번에 변경하는 방법 
// 문자 검색 ctrl + H => 찾기, 바꾸기

// 정렬 - php formatter 설치 돼 있음 
// shift + Alt + F 


// 팔로워, 혹은 팔로우 하는 사람들의 정보 가져가는 로직
// 넘겨 받은 변수들이 null 혹은 빈문자열이 아닐때에만 로직이 작동하도록 하기. 
// 팔로워 stmt, 팔로잉 stmt 두개 만들어 놓고 넘겨 받는 type 에 따라 다르게 실행 + 매개변수 대입!! 
// 목록을 응답에 넣어주면 됨. 목록이 없으면 없음을 넘겨주면 됨. 


// 넘어온 값 int user number, string type 
// 1) 유저 넘버 
if (isset($_POST["user_number"])) {
    $user_number =  intval($_POST['user_number']);
} else {
    $user_number = null;
}

// 2) type 
if (isset($_POST['type'])) {
    $type = $_POST['type'];
} else {
    $type = null;
}


// 응답 
$response = array();

// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$select_success_msg = "select 완성";
$select_error_msg = "select 실행 오류";


if (empty($user_number) || empty($type)) {
    $response['code'] = 400;
    $response['message'] = $error_msg;
    error_log(json_last_error_msg());
    exit;
} else {

    // type 별로 나누기. 
    if ($type === "follower") {
        // stmt 다르게 넣어서 실행 시키기. 실행 실패 시에 exit.  
    $follow_stmt = $conn->prepare("SELECT 
    u.USER_NUMBER,
    u.USER_EMAIL_ID,
    u.USER_NICKNAME,
    u.USER_PROFILE_IMG,
    u.USER_PROFILE_BIO,
    u.CREATE_TIME,
    u.UPDATE_TIME,
    IF(f2.FOLLOW_STATUS IS NOT NULL, f2.FOLLOW_STATUS, 0) AS I_FOLLOW_THEM
FROM 
    USERS_FOLLOW_LIST f
JOIN 
    USERS u ON f.FOLLOW_FROM_USER = u.USER_NUMBER -- 나를 팔로우 하는 사람들에 대한 정보만 가져오겠다. 
LEFT JOIN 
    USERS_FOLLOW_LIST f2 ON f2.FOLLOW_FROM_USER = ? AND f2.FOLLOW_TO_USER = u.USER_NUMBER
WHERE 
    f.FOLLOW_TO_USER = ?
    AND f.FOLLOW_STATUS = 1");
    $follow_stmt->bind_param("ii", $user_number, $user_number);
    if(!$follow_stmt->execute()) {
        $response['code'] = 400;
        $response['message'] = $select_error_msg . ", 나를 팔로우하는팔로워 조회쿼리 실패";
        exit;
    }

    } elseif ($type === "following") {

        $follow_stmt = $conn->prepare("SELECT 
    u.USER_NUMBER,
    u.USER_EMAIL_ID,
    u.USER_NICKNAME,
    u.USER_PROFILE_IMG,
    u.USER_PROFILE_BIO,
    u.CREATE_TIME,
    u.UPDATE_TIME,
    IF(f2.FOLLOW_STATUS IS NOT NULL, f2.FOLLOW_STATUS, 0) AS I_FOLLOW_THEM
FROM 
    USERS_FOLLOW_LIST f
JOIN 
    USERS u ON f.FOLLOW_TO_USER = u.USER_NUMBER -- 내가 팔로우 하는 사람들(나에게 팔로우 당하는 사람들)에 대한 정보만 user 테이블에서 가져오겠다. 
LEFT JOIN 
    USERS_FOLLOW_LIST f2 ON f2.FOLLOW_FROM_USER = ? AND f2.FOLLOW_TO_USER = u.USER_NUMBER
WHERE 
    f.FOLLOW_FROM_USER = ? -- 내가 팔로우 하는 경우만 가져와라. 
    AND f.FOLLOW_STATUS = 1");
    $follow_stmt->bind_param("ii", $user_number, $user_number); 
    if(!$follow_stmt->execute()) {
        $response['code'] = 400;
        $response['message'] = $select_error_msg . ", 내가 팔로잉하는유저 조회쿼리 실패";
        error_log($follow_stmt->error); // 오류 메시지를 로그에 기록
        exit;
    }

    } // 팔로잉 끝 

    // 결과 담는 코드는 여기서하기.  

    $result = $follow_stmt->get_result(); 

    if($result->num_rows>0) { 
        $response['code'] = 200;
        $response['message'] = "목록있음";
        $response['item'] = array(); 
        while ($list_row = $result -> fetch_assoc()) {
            $data = array ( 
                'user_number' => $list_row['USER_NUMBER'],
                'user_nickname' => $list_row['USER_NICKNAME'],
                'user_emailId' => $list_row['USER_EMAIL_ID'],
                'profile_img' => $list_row['USER_PROFILE_IMG'],
                'follow_status_now' => (string) $list_row['I_FOLLOW_THEM']
            ); 
            $response['item'][] = $data; 
        } // while 

    } else {
        // 결과값 없을때 
        $response['code'] = 200;
        $response['message'] = "목록이 비었습니다";
        $response['item'] = [];
        exit; 
    }

}

echo json_encode($response, JSON_UNESCAPED_UNICODE);
exit; // 추가