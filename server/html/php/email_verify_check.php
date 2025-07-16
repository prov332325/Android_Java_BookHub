<?php

include("db_connection.php");


// 인증번호 입력 값 가져오기
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $verify_numbers = $_POST['verify_numbers'] ?? null;
    $email = $_POST['email'] ?? null;


    // 만약 verify numbers와 email 이 null 값이 아닐때  
    if($verify_numbers!=null && $email!=null ) {
        
        // 쿼리문 돌리기. 
        
        // 이메일이 있는지에 대한 쿼리문 돌리고, 성공하면 인증번호 검사하기. 
        // 인증번호 틀렸을 경우, 인증번호까지 일치하는 경우. 

        $email_check_stmt = $conn->prepare("SELECT * FROM AUTH_KEY WHERE  EMAIL_ADDRESS = ? ORDER BY CREATE_TIME DESC LIMIT 1");
        // 전부 선택 후, 시간순으로 desc 한 뒤에 하나만 가져온다 = 가장 최근에 보낸 이메일 내용만.   
        $email_check_stmt->bind_param("s", $email); 
        $email_check_stmt->execute();
        $result = $email_check_stmt->get_result();
        if ($result->num_rows > 0) {
            // 이메일이 존재할때   

            $row = $result->fetch_assoc(); // 첫 번째 행 가져오기

            if ($email==$row['EMAIL_ADDRESS'] && $verify_numbers == $row['CODE_NUMBERS']) {
                echo "done";
            } else {
                echo "인증번호불일치";
            }
            // $numbers_stmt = $conn->prepare("SELECT * FROM AUTH_KEY WHERE EMAIL_ADDRESS = ? AND CODE_NUMBERS = ?");
            // $numbers_stmt->bind_param("ss", $email, $verify_numbers);
            // $numbers_stmt->execute();
            // $number_result = $numbers_stmt->get_result();
            // if ($number_result->num_rows>0) {
            //     echo "done";
            // } else {
            //     echo "인증번호불일치";
            // }

        } else {
            echo "이메일없음";
        }

    } else if ( $verify_numbers==null) { // 
        echo "varify_failed";
    } else if ($email == null) { // 
        echo "varify_failed";
    }
}

?> 