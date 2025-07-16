<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../php/db_connection.php");

// 이메일로 인증번호보내기

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;


require '../vendor/autoload.php';

$mail = new PHPMailer(true);
$mail->CharSet = 'UTF-8';

// 오류 확인하는 방법 !!!! 
// power shell 에서 
// cd /var/log/apache2/error.log -> 잘려 나옴 

//  + 오류 내용 전체 출력하는 방법 
// sudo tail error.log !! 

// 문자열 한번에 변경하는 방법 
// 문자 검색 ctrl + H => 찾기, 바꾸기

// 정렬 - php formatter 설치 돼 있음 
// shift + Alt + F 


// 넘어온 값 string user_email 

// 1) 유저 이메일 
if (isset($_POST['user_email'])) {
    $user_email = $_POST['user_email'];
} else {
    $user_email = null;
}



// 응답 
$response = array();


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$select_success_msg = "select 실행 성공";
$select_error_msg = "select 실행 오류";
$no_result = "사용중인 이메일이 아닙니다";
$yes_result = "사용중인 이메일입니다";
$pattern_error = "유효한 이메일 형식이 아닙니다";


// 1. 먼저 이메일이 유효한 형식인지 확인한다. 
// 2. db 에서 이메일이 존재하는지 확인한다. 
// 3. 이메일이 존재하면, 이메일 인증번호 발송하고 
// 4. 인증번호 맞는지 확인하는 절차 어디서 하고 있었니.. 



if (empty($user_email)) {
    echo json_encode(array(
        "code" => 400,
        "message" => $error_msg
    ), JSON_UNESCAPED_UNICODE);
    exit;
} else {
    // 이메일 유효성 
    $checked_email = filter_var($user_email, FILTER_VALIDATE_EMAIL);

    // 이메일 형식 유효성 체크하기
    // 이메일 형식이 올바르지 않을 경우 
    if ($checked_email === false) {
        echo json_encode(array(
            "code" => 41,
            "message" => $pattern_error
        ), JSON_UNESCAPED_UNICODE);
        exit;
    } else { // 이메일 형식이 올바를 경우. 
        // db 에서 이메일이 존재하는지 확인한다. 

        $email_check_stmt = $conn->prepare("SELECT EXISTS(SELECT 1 FROM USERS WHERE USER_EMAIL_ID = ?)");
        $email_check_stmt->bind_param("s", $user_email);
        if ($email_check_stmt->execute()) {
            // 이메일 존재여부 확인을 위한 select 쿼리문 실행되었을때 

            $email_check_stmt->bind_result($exists);
            $email_check_stmt->fetch();
            // 여기서 fetch 라는 것은 sql 쿼리 실행 결과로 가져온 데이터를 
            // 하나씩 가져오는 동작을 의미한다. 

            // php 에서 mysqli_stmt 객체의 fetch() 메서드는 현재 쿼리 결과에서 결과의 
            // 한 행을 가져오고 바인딩된 변수에 값을 저장한다. 
            // 지금은 결과가 무조건 하나만 나오기 때문에 !! while 문 없이 fetch만. 


            // 결과를 사용한 후 리소스 정리-> 이 과정을 해줘야함 !! 
            $email_check_stmt->free_result(); // 결과 해제
            $email_check_stmt->close(); // 스테이트먼트 닫기



            if ($exists) {
                // 이메일이 존재하는 경우
                // echo json_encode(array(
                //     "code" => 200, 
                //     "message" => "이메일이 존재합니다"
                // ), JSON_UNESCAPED_UNICODE); 

                // 인증번호 보내기. 

                $mail->isSMTP();
                // $mail->SMTPDebug = SMTP::DEBUG_SERVER; // 디버그 모드, DEBUG_OFF 시 출력 없음
                $mail->SMTPDebug = SMTP::DEBUG_OFF;
                $mail->Host = 'smtp.gmail.com';

                $mail->Port = 587;
                //SMTP 고정 포트

                //encrypte 메커니즘 세팅
                $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;

                //SMTP AUTH 사용
                $mail->SMTPAuth = true;

                $mail->Username = 'prov332325@gmail.com';
                // gmail 패스워드
                $mail->Password = 'znlg focu xmoe emzo';


                //  보내는 사람 주소, 이름 세팅 - 보내는사람 주소은 추가 세팅을 해주지않으면 Username의 계정
                $mail->setFrom('prov332325@gmail.com', '북허브 Book Hub');


                // ======================= 받아온 정보 넣기 


                // 내가 보낼 이멜 주소, 이름(선택)
                $mail->addAddress($user_email, null);


                // 메일 제목, 내용 세팅하기 
                $mail->Subject = '[북허브] 이메일 인증번호';

                // 인증번호 난수 생성하기. 
                $verified_numbers =  str_pad(random_int(0, 999999), 6, '0', STR_PAD_LEFT); // 6자리 0부터 999999 사이의 난수
                // str_pad는 결과를 문자열로 반환함 !!! 숫자라고 int 아님

                $mail->Body = "
                            <b>[북허브] 비밀번호 재설정을 위한 이메일 인증 번호입니다. <br/> 아래 인증번호를 확인하여 이메일 주소 인증을 완료해 주세요.</b>
                            <br/>
                            <br/>
                            <br/>
                            <b>인증번호: {$verified_numbers}</b>
                           
                            <br/>
                            ";

                $mail->isHTML(true);
                if (!$mail->send()) {
                    echo "Mailer Error";
                    //$mail->ErrorInfo;

                    echo json_encode(array(
                        "code" => 400,
                        "message" => "인증번호 발송에러 mailer error"
                    ), JSON_UNESCAPED_UNICODE);
                } else {
                    // 이메일 인증번호 발송 성공 !!
                    // 이메일 생성하면 DB 에 저장하기. 
                    // 인증번호 확인 시에 삭제 ?? 자동 삭제 되므로, 냅둬도 됨. 왜냐면 어차피 인증 번호 전송 누를 때 중복확인도 진행하기 때문에. 

                    echo json_encode(array(
                        "code" => 200,
                        "message" => "인증번호 발송성공!!"
                    ), JSON_UNESCAPED_UNICODE);

                    $insert_stmt = $conn->prepare("INSERT INTO AUTH_KEY (EMAIL_ADDRESS, CODE_NUMBERS) VALUES (?, ?)");
                    $insert_stmt->bind_param("ss", $user_email, $verified_numbers);
                    $insert_stmt->execute();
                }
            } else { // 가입된 이메일이 없을때!! 없을때에도 일단 사용자에게는 
                // 인증번호를 발송했다고 보내야 한다 ?? 
                // 없으면 찾고자 하는 이메일이 없다는 것을 알려줘야 함. !!!

                echo json_encode(array(
                    "code" => 200,
                    "message" => "이메일X"
                ), JSON_UNESCAPED_UNICODE);
            }
        } else {
            // 이메일 존재여부 쿼리 실행 실패 
            echo json_encode(array(
                "code" => 400,
                "message" => $select_error_msg
            ), JSON_UNESCAPED_UNICODE);
            error_log(json_last_error_msg());
            exit;
        }
    } // 이메일 형식이 올바를 경우. 끝

} // 끝 
