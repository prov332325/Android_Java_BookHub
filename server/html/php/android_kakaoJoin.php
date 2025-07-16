<?php
header('Content-Type: application/json; charset=UTF-8');
include("db_connection.php");


// 카카오 로그인 api 으로 받아온 유저의 정보를 
// DB에 넣을 때 확인해야할 것 

// 1. 먼저 id 가 db에 있는지 확인하기, 있으면 반환해서 쉐어드에만 넣기. 
// 2. 
// 3. 



// http 통신을 통해 폼 데이터 읽기 
// $kakao_id = $_POST['kakao_id'];
// $kakao_email = $_POST['kakao_email'];
// $kakao_nickname = $_POST['kakao_nickname'];
// $kakao_imgUrl = $_POST['kakao_imgUrl']; 

$json_data = file_get_contents('php://input');
$data = json_decode($json_data, true);

$kakao_id = $data['kakao_id'];
$kakao_email = $data['kakao_email'];
$kakao_nickname = $data['kakao_nickname'];
$kakao_imgUrl = $data['kakao_imgUrl'];


  // 메시지 모음
  $error_msg= "필수 인자가 부족합니다"; 
  $exist_msg= "이미가입됨";
  $join_success= "카카오소셜가입완료"; 
  $join_failed= "카카오소셜가입실패"; 


error_log("전달받은 카카오로그인정보(가입용) ! POST data: " . json_encode($_POST));


if($kakao_id == '' || $kakao_email == ''  
|| $kakao_nickname == ''  || $kakao_imgUrl == '') {

    echo json_encode(array(
        "code" => 400, 
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE); 

} else {  // 전달받은 인자가 다 채워졌을때 !! null 이 아닐때 !! 

    // 데이터베이스에 id 존재하는지 확인하기. 
    $kakaoId_check_stmt = $conn->prepare("SELECT * FROM USERS WHERE USER_EMAIL_ID = ?");
    $kakaoId_check_stmt->bind_param("s", $kakao_id);
    $kakaoId_check_stmt->execute();

    $result = $kakaoId_check_stmt -> get_result();
    if($result->num_rows > 0) {
        // 이미 가입 내용이 존재하므로 반환한다. 쉐어드에만 저장하기. 
        echo json_encode(array(
            "code" => 200, 
            "message" => $exist_msg, 
            "kakao_id" => $kakao_id
        ), JSON_UNESCAPED_UNICODE);
    } else {
        // 존재하지 않음. insert 해준다. 
        $kakao_insert_stmt = $conn->prepare("INSERT INTO USERS (USER_EMAIL_ID, USER_NICKNAME, USER_PROFILE_IMG)
        VALUES (?, ? ,? )");
        $kakao_insert_stmt->bind_param("sss",$kakao_id, $kakao_nickname, $kakao_imgUrl);
        if ($kakao_insert_stmt->execute()) {

            // 회원가입 성공 
            echo json_encode(array(
                "code" => 200, 
                "message" => $join_success, 
                "kakao_id" => $kakao_id
            ), JSON_UNESCAPED_UNICODE);
        } else {

            //insert 실패한 경우 
            echo json_encode(array(
                "code" => 400, 
                "message" => $join_failed
            ), JSON_UNESCAPED_UNICODE); 
        }

        
    } // 기존 가입 내용 없어서 insert 

    
}

?>