<?php

include("db_connection.php");

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;


require '../vendor/autoload.php';

$mail = new PHPMailer(true);
$mail->CharSet = 'UTF-8';



// post로 사용자가 작성한 이메일 넘겨 받기 
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $email = $_POST['email'] ?? '';

    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        echo "잘못된 이메일 형식입니다";
    } else {
        // 형식이 올바를 경우 
        // 이메일 중복 검사. 



        // 이미 인증 번호를 보낸 경우를 확인한다 
        // $verify_check_stmt = $conn->prepare("SELECT * FROM AUTH_KEY WHERE EMAIL_ADDRESS = ? ");
        // $verify_check_stmt->bind_param("s", $email);
        // $verify_check_stmt->execute();
        // $verify_check_result = $verify_check_stmt->get_result();
        // if($verify_check_result->num_rows > 0 ){  // 이메일 인증 이미 한경우는 그대로 반환함. 
        //     echo "이미 인증 번호가 전송되었습니다."; 
        // } else { // 이메일 인증 안 한경우 -> 이미 회원인지 확인후 아닐 경우 응답 전송. 
 
              // 이메일 검색 
        $email_check_stmt = $conn->prepare("SELECT * FROM USERS WHERE USER_EMAIL_ID = ?");
        $email_check_stmt->bind_param("s", $email);
        $email_check_stmt->execute();
        $result = $email_check_stmt->get_result();
        if ($result->num_rows > 0) {
            echo "이미 사용 중인 이메일 입니다.";
        } else {

            // 중복 검사 통과 한 경우에 이메일 보내기 


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
            $mail->addAddress($email, null);


            // 메일 제목, 내용 세팅하기 
            $mail->Subject = '[북허브] 회원가입 인증번호';

            // 인증번호 난수 생성하기. 
            $verified_numbers =  str_pad(random_int(0, 999999), 6, '0', STR_PAD_LEFT); // 6자리 0부터 999999 사이의 난수
            // str_pad는 결과를 문자열로 반환함 !!! 숫자라고 int 아님

            $mail->Body = "
                            <b>[북허브] 가입을 위한 인증번호 입니다. <br/> 아래 인증번호를 확인하여 이메일 주소 인증을 완료해 주세요.</b>
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
            } else {
                // 이메일 생성하면 DB 에 저장하기. 
                // 인증번호 확인 시에 삭제 ?? 자동 삭제 되므로, 냅둬도 됨. 왜냐면 어차피 인증 번호 전송 누를 때 중복확인도 진행하기 때문에. 

                $insert_stmt = $conn->prepare("INSERT INTO AUTH_KEY (EMAIL_ADDRESS, CODE_NUMBERS) VALUES (?, ?)");
                $insert_stmt->bind_param("ss", $email, $verified_numbers);
                $insert_stmt->execute();
                echo "Message sent";
            }
        }
      //  }


        
      
    }
}
