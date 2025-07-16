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
 

// ============================================= 


//post 로 넘어오는 정보 - 유저 번호 string, 게시글 번호 int  

// 응답 array 
$response = array(); 


if(isset($_POST['user_number'])) {
    $user_number = intval($_POST['user_number']);
} else {
    $user_number = null;
} 


if(isset($_POST['board_number'])) {
    $board_number =  $_POST['board_number'];
} else {
    $board_number =null; 
} 



// message 모음 
$error_msg = "필수 인자 user number 혹은 board number 안넘어옴";
$query_failed = "select 쿼리문 실행 실패";
$yes_result = "사진 있음!";
$no_result = "사진 없습니다";




if($user_number == '' || $board_number == '' ) { 
        // 내용이 null 인 경우 
        $response['code'] = 400;
        $response['message'] = $error_msg;
        echo json_encode($response, JSON_UNESCAPED_UNICODE);
        exit; // 스크립트 종료

} else {
    // user number, board number 둘 다 있는 경우 
    
    // stmt 
    $board_images_stmt = $conn->prepare("SELECT
    BOARD_IMG_NUMBER, BOARD_ID, USER_NUMBER, IMG_URL, CREATE_TIME 
    FROM BOARD_IMG WHERE USER_NUMBER = ? AND BOARD_ID = ?");
    $board_images_stmt->bind_param("ii", $user_number, $board_number); 


    if($board_images_stmt->execute()) { 
        $result = $board_images_stmt -> get_result();

        if($result->num_rows > 0) {
            $response['code'] = 200;
            $response['message'] = $yes_result; 
            $response['item'] = array(); 

            while ($row = $result->fetch_assoc()) {
                $data = array(
                    'success' => true,
                    'message' => "게시글사진",
                    'image_url' => $row['IMG_URL'],
                    'board_image_number' => $row['BOARD_IMG_NUMBER'], 
                    'board_number' => $row['BOARD_ID']
                );
                $response['item'][] = $data;
            }

        } else { // 게시글 없음
            $response['code'] = 200;
            $response['message'] = $no_result;
            $response['item'] = array();

        }

    } else { // select 쿼리문 실패 
        $response['code'] = 500;
        $response['message'] = $query_failed;
    }   
}
echo json_encode($response, JSON_UNESCAPED_UNICODE);
?>