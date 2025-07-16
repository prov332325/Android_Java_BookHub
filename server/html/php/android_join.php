<?php
header('Content-Type: application/json; charset=UTF-8');
include("db_connection.php");

$user_email = $_POST['userEmail'];
$user_nickname = $_POST['userNickname'];
$user_pw = $_POST['userPw'];
$user_pwCheck = $_POST['userPwCheck'];

// 
$checked_nickname = "";
$checked_password = "";

// 유효성 식 
$nickname_pattern =  "/^[가-힣a-zA-Z0-9]+$/";
$password_pattern = "/^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()\-+=]).{8,}$/";

error_log("POST Data: " . print_r($_POST, true));
// 이메일 유효성 
$checked_email = filter_var($user_email, FILTER_VALIDATE_EMAIL);


// 닉네임 유효성 검사 
if (preg_match($nickname_pattern, $user_nickname)) {
    $checked_nickname = $user_nickname;
} else {
    echo "닉네임은 한글, 영문 대소문자, 숫자만 사용가능합니다.";
}

// 비번 유효성 검사 
if (preg_match($password_pattern, $user_pw)) {
    $checked_password = $user_pw;
} else {
    echo "비밀번호는 영문, 숫자, 특수문자가 각각 1개 이상 포함되어야 합니다.";
}




if ($conn) {
    if ($checked_email === false) {
        echo "이메일 형식을 확인해 주세요.";
    } else {  // 이메일 유효성 검사 통과한 경우
        if ($checked_nickname !== "" && $checked_password !== "") { // 닉네임, 비번 유효성검사 완료했을때. 
            // 이메일 중복 검사 
            $duplicate_email_stmt = $conn->prepare("SELECT * FROM USERS WHERE USER_EMAIL_ID LIKE ? ");
            $duplicate_email_stmt->bind_param("s", $checked_email);
            $duplicate_email_stmt->execute();
            $email_result = $duplicate_email_stmt->get_result();
            if ($email_result->num_rows > 0) {
                echo "이미 사용 중인 이메일 주소입니다.";
            } else {
                // 닉네임 유효성 검사 
                $duplicate_nickname_stmt = $conn->prepare("SELECT * FROM USERS WHERE USER_NICKNAME LIKE ? ");
                $duplicate_nickname_stmt->bind_param("s", $checked_nickname);
                $duplicate_nickname_stmt->execute();
                $nickname_result = $duplicate_nickname_stmt->get_result();

                if ($nickname_result->num_rows > 0) {
                    echo "이미 사용 중인 닉네임 입니다.";
                } else {
                    // 비번 일치하는지. 
                    if ($user_pw === $user_pwCheck) {

                        // 회원가입하기. 
                        $insert_stmt = $conn->prepare("INSERT INTO USERS (USER_EMAIL_ID, USER_PASSWORD, USER_NICKNAME) VALUES (?, ?, ?)");
                        $insert_stmt->bind_param("sss", $checked_email, $checked_password, $checked_nickname);
                        if ($insert_stmt->execute()) {
                            echo "회원가입성공";

                            // 성공하면 인증 테이블에서 내용 지우기. 
                            $authKey_delete_stmt = $conn->prepare("DELETE FROM AUTH_KEY WHERE EMAIL_ADDRESS = ?");
                            $authKey_delete_stmt->bind_param("s", $checked_email);
                            $authKey_delete_stmt->execute();

                        } else {
                            echo "회원가입에 실패하였습니다. (insert 쿼리 실행 실패)";
                        }
                    } // 비번 일치 
                    else {
                        echo "비밀번호가 일치하지 않습니다";
                    } // 비번 불일치 
                } // 닉네임 중복검사 통과  

            } // 이메일 중복 검사 통과 

        } else {
            echo "닉네임 or 비밀번호 유효성 검사 오류";
        }
    }
}
