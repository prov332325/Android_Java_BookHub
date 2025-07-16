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


// 응답 
$response = array();

// message 
$success = "이미지 서버 저장 완료";
$db_success = "이미지 DB 저장 완료";
$db_fail = "이미지 DB 저장 실패";
$type_error = "사진타입 jpg, png 아님";
$upload_error = "사진 서버 업로드 실패";



// POST 요청에서 isDefaultImage 값 확인
$isDefaultImage = isset($_POST['isDefaultImage']) ? $_POST['isDefaultImage'] === 'true' : false;
$user_number = isset($_POST['user_number']) ? intval($_POST['user_number']) : 0;

if ($isDefaultImage) {
    // 기본 이미지 처리 로직
    $defaultImageName = "icon_basic_profile4.jpg"; // 기본 이미지 파일명

    // 유저 프로필 이미지를 기본 이미지로 설정
    $default_img_stmt = $conn->prepare("UPDATE USERS SET USER_PROFILE_IMG = ? WHERE USER_NUMBER = ?");
    $default_img_stmt->bind_param("si", $defaultImageName, $user_number);

    if ($default_img_stmt->execute()) {
        $response['success'] = true;
        $response['message'] = $default_image_success;
        $response['image_url'] = $defaultImageName;
    } else {
        $response['success'] = false;
        $response['message'] = $default_image_fail;
        error_log(json_last_error_msg());
    }
} elseif ( isset($_FILES['image']) && $_FILES['image']['name'] != "") {
    $fileName = $_FILES['image']['name'];

    $user_number =  intval($_POST['user_number']);

    $imgFullName = strtolower($fileName);
    $imgNameSlice = explode(".", $imgFullName); // . 기준으로 쪼개서 확장자 확인하기 
    $imgName = $imgNameSlice[0];
    $imgType = $imgNameSlice[1];
    $imgExt = array('jpg', 'png');


    // 이미지를 서버에 저장할때 사용할 이미지 파일 name 
    $dates = date("mdhis", time());
    $newImgName = chr(rand(97, 122)) . chr(rand(97, 122)) . $dates . rand(1, 9) . "." . $imgType;
    $dir = "/var/www/html/php/img/";


    // 이미지를 서버에 저장한다. 
    $imgTmpName = $_FILES['image']['tmp_name'];
    if (move_uploaded_file($imgTmpName, $dir . $newImgName)) {
        // 이미지가 서버에 저장되면 DB에도 저장한다. 

        // 유저 프로필 이미지에 update 
        $user_img_stmt = $conn->prepare("UPDATE USERS SET USER_PROFILE_IMG = ? WHERE USER_NUMBER= ?");
        $user_img_stmt->bind_param("si", $newImgName, $user_number);


        if ($user_img_stmt->execute()) {
            $response['success'] = true;
            $response['message'] = $db_success;
            $response['image_url'] = $newImgName;
        } else {
            $response['success'] = false;
            $response['message'] = $db_fail;
            error_log(json_last_error_msg());
        }
    } else { // 서버 업로드 실패 
        $response['success'] = false;
        $response['message'] = $upload_error;
        error_log(json_last_error_msg());
        exit;
    }
}

echo json_encode($response, JSON_UNESCAPED_UNICODE);
