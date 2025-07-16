<?php
header('Content-Type: application/json; charset=UTF-8');
include("db_connection.php");


// http 통신을 통해 폼 데이터 읽기 
$input_email = $_POST['email'];
$input_password = $_POST['password'];

//error_log("전달받은이멜 비번 ! POST data: " . json_encode($_POST));


  // 회원번호, 이메일, 닉네임 변수 초기화 
  $login_usernumber = null;
  $login_email = null; 
  $login_nickname = null; 


  // 메시지 모음
  $error_msg= "필수 인자가 부족합니다";
  $login_success_msg = "로그인이 완료되었습니다";
  $wrong_info_msg = "이메일 또는 비밀번호가 잘못되었습니다.";

if($input_email == '' || $input_password == '') {

  
    echo json_encode(array(
        "code" => 400, 
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE); 
} else {
    // 이메일 있는지 DB 확인하기  
    $email_check_stmt = $conn->prepare("SELECT USER_NUMBER, USER_EMAIL_ID, USER_NICKNAME, USER_PASSWORD FROM USERS WHERE USER_EMAIL_ID = ? ");
    $email_check_stmt->bind_param("s", $input_email);
    $email_check_stmt->execute();

    $result= $email_check_stmt -> get_result();
    if($result-> num_rows > 0) {

        // 결과를 배열로 담기. 
        $user = $result -> fetch_assoc();

        // 가져온 비밀번호 and 입력한 비밀번호가 맞는지 확인하기. 

        if($user['USER_PASSWORD'] == $input_password) {
            // 일치할 경우 로그인 성공.  select 결과값들 json 에 인코딩하기 
            echo json_encode(array(
                "code" => 200, 
                "message" => $login_success_msg, 
                "user_number" => $user['USER_NUMBER'], 
                "user_email" => $user['USER_EMAIL_ID'],
                "user_nickname" => $user['USER_NICKNAME']
            ), JSON_UNESCAPED_UNICODE); 
        } else {
            // 비번 잘못된 경우 
            // 입력 비번과 DB 저장된 비번이 일치하지 않음 = 로그인 실패 
            echo json_encode(array(
                "code" => 400, 
                "message" => $wrong_info_msg
            ), JSON_UNESCAPED_UNICODE); 
        }
    } else { // 결과가 없음
        // 결과 없는 경우 = email 잘못된 경우 (where 절에 email )
        echo json_encode(array(
            "code" => 400, 
            "message" => $wrong_info_msg
        ), JSON_UNESCAPED_UNICODE); 
    } // else 문 끝 
} // 입력값이 null이 아닌 경우 (else문끝)

?> 