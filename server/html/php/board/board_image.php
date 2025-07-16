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



// message 
$success = "이미지 서버 저장 완료";
$db_success = "이미지 DB 저장 완료";
$db_fail = "이미지 DB 저장 실패";
$type_error = "사진타입 jpg, png 아님";
$upload_error = "사진 서버 업로드 실패"; 


// board create 에서는 이미지를 한장씩 http로 파일을 보냄. 
// 한장씩오기 때문에 array 가 필요 없음. 
if (
    isset($_FILES['image']) && $_FILES['image']['name'] != ""
) {
    $fileName = $_FILES['image']['name'];

    $board_number =  intval($_POST['board_number']);
    $user_number =  intval($_POST['user_number']);

    $imgFullName = strtolower($fileName);
    $imgNameSlice = explode(".", $imgFullName); // . 기준으로 쪼개서 확장자 확인하기 
    $imgName = $imgNameSlice[0];
    $imgType = $imgNameSlice[1];
    $imgExt = array('jpg', 'png');

    // if (!in_array($imgType, $imgExt)) { // array search 보단 in array로 하는게 나음. 
    //     echo json_encode(array(
    //         "success" => false,
    //         "message" => $type_error
    //     ), JSON_UNESCAPED_UNICODE);
    //     exit;
    // }


    $dates = date("mdhis", time());
    $newImgName = chr(rand(97, 122)) . chr(rand(97, 122)) . $dates . rand(1, 9) . "." . $imgType;

    $dir = "/var/www/html/php/img/";


    // echo json_encode(array( 
    //     "success" => true, 
    //     "message" => $success,
    //     "image_url" => $dir . $newImgName . ", 게시글번호: " . $this_board_number
    // ), JSON_UNESCAPED_UNICODE); 



    // ============================
    // 이미지를 서버에 저장한다. 
    $imgTmpName = $_FILES['image']['tmp_name'];
    if (move_uploaded_file($imgTmpName, $dir . $newImgName)) {


        // 데이터 베이스에 저장함.  
        // BOARD_ID, USER_NUMBER, IMG_URL
        // 이거 하나씩 넘어오는거라서 배열처리 안해도됨.  

        // 이미지 insert 
        $board_img_inser_stmt = $conn->prepare("INSERT INTO BOARD_IMG (BOARD_ID, USER_NUMBER, IMG_URL) VALUES (?, ?,?)");
        $board_img_inser_stmt->bind_param("iis", $board_number, $user_number, $newImgName);

        if ($board_img_inser_stmt->execute()) { // board img insert 성공 !! 
            echo json_encode(array(
                "success" => true,
                "message" => $db_success,
                "image_url" => $newImgName
            ), JSON_UNESCAPED_UNICODE);
        } else { // 이미지 db 저장 실패
            echo json_encode(array(
                "success" => false,
                "message" => $db_fail
            ), JSON_UNESCAPED_UNICODE);
        }
     
    } else { // 서버 업로드 실패
        echo json_encode(array(
            "success" => false,
            "message" => $upload_error
        ), JSON_UNESCAPED_UNICODE);
    }
}
