<?php

// use function PHPSTORM_META\elementType;
// use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");

echo json_encode($_POST, JSON_UNESCAPED_UNICODE);


// 공통 요소 넘겨받기 
// user_number, book_number, before_library_number, before_type, modi_type 

// 1) 유저 넘버 
if (isset($_POST["user_number"])) {
    $user_number =  intval($_POST['user_number']);
} else {
    $user_number = null;
}


// 2) book_number 
if (isset($_POST["book_number"])) {
    $book_number = $_POST['book_number'];
} else {
    $book_number = null;
}


// 3) before_library_number 
// 서재 번호 가져오기 
if (isset($_POST["before_library_number"])) {
    $before_library_number =  $_POST['before_library_number'];
} else {
    $before_library_number = null;
}


// 수정 전 type, 수정 후 type 
if (isset($_POST["before_type"])) {
    $before_type = $_POST['before_type'];
} else {
    $before_type = null;
}

if (isset($_POST["modi_type"])) {
    $modi_type = $_POST['modi_type'];
} else {
    $modi_type = null;
}



// message 모음 
$error_msg = "필수 인자 안넘어옴";
$update_ok = "type그대로의 기존 내 서재 update 완료";
 // $delete_ok = "type변경되어서 기존 내용 delete 완료"; ==> delete 가 완료되었을 때에는 insert를 바로 해야하기 때문에 message를 보낼일이 없음
$insert_ok = "type변경돼서 기존내용지우고 insert 완료";
$update_query_failed = "update 쿼리문 실행 실패";
$delete_query_failed = "delete 쿼리문 실행 실패";
$insert_query_failed = "insert 쿼리문 실행 실패";


// ================================================== 

// 수정된 타입으로 분기처리하기. 

if (
    $user_number == '' || $book_number == '' ||
    $before_library_number == '' || $before_type == '' || $modi_type == ''
) {
    echo json_encode(array(
        "code" => 400,
        "message" => $error_msg . $user_number . $book_number .  $before_library_number . $before_type . $modi_type 
    ), JSON_UNESCAPED_UNICODE);
} else {

    // 필요한 내용 전부 있음. 
    // 수정된 타입 modi type 으로 분기처리하기. 

    if ($modi_type == '1') { // <<< 읽은 책 >>> 
        //  <<< 읽은 책 >>> 데이터 마저 가져오기.  
        // 시작, 종료, 평점.
        if (isset($_POST["modi_started_date"])) {
            $modi_started_date = $_POST['modi_started_date'];
        } else {
            $modi_started_date = null;
        }

        if (isset($_POST["modi_finished_date"])) {
            $modi_finished_date = $_POST['modi_finished_date'];
        } else {
            $modi_finished_date = null;
        }

        if (isset($_POST["modi_already_rating"])) {
            $modi_already_rating = $_POST['modi_already_rating'];
        } else {
            $modi_already_rating = null;
        }

        // 이 안에서도 $before type이랑 $modi_type이 
        if ($before_type === $modi_type) { // 읽은책-> 읽은책으로 변경 
            // 읽은 책 테이블에 있는 before library number 를 update해줌.            
            $already_update_stmt = $conn->prepare("UPDATE BOOK_ALREADY 
            SET ALREADY_STARTED_DATE = ?, 
            ALREADY_FINISHED_DATE = ?, 
            ALREADY_RATING = ?
            WHERE BOOK_ALREADY_NUMBER = ?
            AND BOOK_ALREADY_USER_NUMBER = ? AND BOOK_INFO_NUMBER = ?;
            ");
            $already_update_stmt->bind_param(
                "sssiii",
                $modi_started_date,
                $modi_finished_date,
                $modi_already_rating,
                $before_library_number,
                $user_number,
                $book_number
            );
            if ($already_update_stmt->execute()) { // 읽은책 -> 읽은책 update
                echo json_encode(array(
                    "code" => 200,
                    "message" => $update_ok
                ), JSON_UNESCAPED_UNICODE);
            } else { // 읽은책 -> 읽은 책에 대한 update 실패 
                echo json_encode(array(
                    "code" => 200,
                    "message" => $update_query_failed
                ), JSON_UNESCAPED_UNICODE);
            }
        } else if ($before_type == '2') { // ??다른type -> 읽은책으로 변경
            // before type이 2인 경우, 읽고 있는책 테이블에서 삭제 후 읽는 책에 insert

            // 읽고 있는 책 테이블 삭제. 
            $reading_delete_stmt = $conn->prepare("DELETE FROM BOOK_READING WHERE BOOK_READING_NUMBER = ?");
            $reading_delete_stmt->bind_param("i", $before_library_number);
            if ($reading_delete_stmt->execute()) {
                // 읽은 테이블에 추가. 
                $already_insert_stmt = $conn->prepare("INSERT INTO BOOK_ALREADY (USER_NUMBER, BOOK_INFO_NUMBER, ALREADY_STARTED_DATE, ALREADY_FINISHED_DATE, ALREADY_RATING) 
                VALUES (?, ?, ?, ?, ?)");
                $already_insert_stmt->bind_param("iisss", $user_number, $book_number, $modi_started_date, $modi_finished_date, $modi_already_rating);
                if ($already_insert_stmt->execute()) {
                    // 읽고 있는 책 -> 읽은책으로 변경 insert 성공 
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $insert_ok . ", 위치: 읽고 있는 책-> 읽은 책으로 변경하기 위한 insert 성공 !!"
                    ), JSON_UNESCAPED_UNICODE);
                } // insert execute 성공
                else {
                    // 읽고 있는 책 -> 읽은책으로 변경 insert 실패 
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $insert_query_failed . ",위치: 읽고 있는 책-> 읽은 책으로 변경하기 위한 insert문"
                    ), JSON_UNESCAPED_UNICODE);
                }
            } else {  // delete execute 실패 
                echo json_encode(array(
                    "code" => 200,
                    "message" => $delete_query_failed . ", 위치: 읽고 있는책->읽은책으로 변경하기 위한 delete문"
                ), JSON_UNESCAPED_UNICODE);
            }
        } else if ($before_type == '3') { // before type이 3인 경우, 읽고 싶은책 테이블에서 삭제 후 읽는 책에 insert 
            $want_delete_stmt = $conn->prepare("DELETE FROM BOOK_WANT WHERE BOOK_WANT_NUMBER = ?");
            $want_delete_stmt->bind_param("i", $before_library_number);
            if ($want_delete_stmt->execute()) {

                // 읽은 테이블 추가. 
                $already_insert_stmt = $conn->prepare("INSERT INTO BOOK_ALREADY (USER_NUMBER, BOOK_INFO_NUMBER, ALREADY_STARTED_DATE, ALREADY_FINISHED_DATE, ALREADY_RATING) 
                VALUES (?, ?, ?, ?,?)");
                $already_insert_stmt->bind_param("iiss", $user_number, $book_number, $modi_started_date, $modi_finished_date, $modi_already_rating);
                if ($already_insert_stmt->execute()) {
                    // 읽고 있는 책 -> 읽은책으로 변경 insert 성공 
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $insert_ok . ", 위치: 읽고 있는 책-> 읽은 책으로 변경하기 위한 insert 성공 !!"
                    ), JSON_UNESCAPED_UNICODE);
                } // insert execute 성공
                else {
                    // 읽고 있는 책 -> 읽은책으로 변경 insert 실패 
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $insert_query_failed . ",위치: 읽고 있는 책-> 읽은 책으로 변경하기 위한 insert문"
                    ), JSON_UNESCAPED_UNICODE);
                }
            }
        }
        // ??다른type -> 읽은책으로 변경 처리 끝. 
    } elseif ($modi_type == '2') { // <<< 읽고 있는 책 >>>  

        // <<< 읽고 있는 책 >>> 으로 수정할 데이터 마저 가져오기 
        if (isset($_POST["modi_started_date"])) {
            $modi_started_date = $_POST['modi_started_date'];
        } else {
            $modi_started_date = null;
        }

        if (isset($_POST["modi_read_page"])) {
            $modi_read_page = $_POST['modi_read_page'];
        } else {
            $modi_read_page = null;
        }

        // 수정전 before type도 2인지, 1혹은3인지 
        if ($before_type === $modi_type) { // 읽고 있는 책 -> 읽고 있는 책으로 변경 
            $reading_update_stmt = $conn->prepare("UPDATE BOOK_READING SET READING_STARTED_DATE = ?, READING_PAGE = ? 
            WHERE BOOK_READING_NUMBER = ? AND BOOK_READING_USER_NUMBER = ? AND BOOK_INFO_NUMBER = ?");
            $reading_update_stmt->bind_param("ssiii", $modi_started_date, $modi_read_page, $before_library_number, $user_number, $book_number);
            if ($reading_update_stmt->execute()) { // 읽고 있는 책 ->  읽고 있는 책 update 성공
                echo json_encode(array(
                    "code" => 200,
                    "message" => $update_ok . '위치: 읽고 있는 책에서 읽고 있는책으로 업데이트'
                ), JSON_UNESCAPED_UNICODE);
            } else { //  읽고 있는 책 ->  읽고 있는 책에 대한 update 실패 
                echo json_encode(array(
                    "code" => 200,
                    "message" => $update_query_failed . '읽고있는책에서 읽고있는책으로 update실패'
                ), JSON_UNESCAPED_UNICODE);
            }
        } else if ($before_type == '1') {
            // 읽은 책에서 삭제 delete 후, 읽고 있는 책에 insert
            $already_delete_stmt = $conn->prepare("DELETE FROM BOOK_ALREADY WHERE USER_NUMBER = ? AND BOOK_INFO_NUMBER = ? ");
            $already_delete_stmt->bind_param("ii", $user_number, $book_number);  // 두개가 fk 이기 때문에 where절에서 언급해줘야 함. 
            if ($already_delete_stmt->execute()) {
                // 읽은 책에서 삭제 성공, 읽고 있는 책에 insert 
                $reading_insert_stmt = $conn->prepare("INSERT INTO BOOK_READING (USER_NUMBER, BOOK_INFO_NUMBER, READING_STARTED_DATE, READING_PAGE) 
                VALUES (?, ?, ?, ?)");
                $reading_insert_stmt->bind_param("iiss", $user_number, $book_number, $modi_started_date, $modi_read_page);
                if ($reading_insert_stmt->execute()) {
                    // 읽은 책-> 읽고 있는 책으로 변경 성공 !!!! insert
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $insert_ok . ", 위치: 읽은 책-> 읽고 있는 책으로 변경하기 위한 insert 성공 !!"
                    ), JSON_UNESCAPED_UNICODE);
                } else {
                    // 읽은책-> 읽고 있는 책으로 변경 insert 실패 
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $insert_query_failed . ",위치: 읽은 책->  읽고 있는 책으로 변경하기 위한 insert문 실패"
                    ), JSON_UNESCAPED_UNICODE);
                }
            } else {
                echo json_encode(array(
                    "code" => 200,
                    "message" => $delete_query_failed . ", 위치: 읽은 책->읽고 있는 책으로 변경하기 위한 delete문 실패"
                ), JSON_UNESCAPED_UNICODE);
            }
        } elseif ($before_type == '3') {
            // 읽고 싶은 책에서 읽고 있는 책으로 변경 !! 
            $want_delete_stmt = $conn->prepare("DELETE FROM BOOK_WANT WHERE BOOK_WANT_NUMBER = ?");
            $want_delete_stmt->bind_param("i", $before_library_number);
            if ($want_delete_stmt->execute()) {
                // 읽고 싶은 책 want에서 insert 성공 -> 읽고 있는 책 insert하기 
                $reading_insert_stmt = $conn->prepare("INSERT INTO BOOK_READING (USER_NUMBER, BOOK_INFO_NUMBER, READING_STARTED_DATE, READING_PAGE) 
                VALUES (?, ?, ?, ?)");
                $reading_insert_stmt->bind_param("iiss", $user_number, $book_number, $modi_started_date, $modi_read_page);
                if ($reading_insert_stmt->execute()) { 
                       // 읽고 있는 책 insert까지 성공 !! 
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $insert_ok . ", 위치: 읽고 싶은 책-> 읽고 있는 책으로 변경하기 위한 insert 성공 !!"
                    ), JSON_UNESCAPED_UNICODE);
                } else {
                    // 읽고 싶은 책에서 읽고 있는 책 insert 실패 !!
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $insert_query_failed . ",위치: 읽고 싶은 책에서 읽고 있는 책으로 변경하기 위한 insert문 실패"
                    ), JSON_UNESCAPED_UNICODE);
                }
            } else { //  읽고 싶은 책 want 에서 delete 실패 
                echo json_encode(array(
                    "code" => 200,
                    "message" => $delete_query_failed . ", 위치: 읽고 싶은 책->읽고 있는 책으로 변경하기 위한 delete문 실패"
                ), JSON_UNESCAPED_UNICODE);
            }
        } // 다른 type에서 -> 읽고 싶은 책으로 변경 처리 끝. 


    } elseif ($modi_type == '3') { // <<< 읽고 싶은 책 >>> 
        // <<< 읽고 싶은 책 >>> 으로 수정할 데이터 마저 가져오기 

        if (isset($_POST["modi_want_rating"])) {
            $modi_want_rating = $_POST['modi_want_rating'];
        } else {
            $modi_want_rating = null;
        }

        if (isset($_POST["modi_want_preview"])) {
            $modi_want_preview = $_POST['modi_want_preview'];
        } else {
            $modi_want_preview = null;
        }


        // 수정전 before type도 3인지, 1혹은 2인지  

        if($before_type == $modi_type) { // 읽고 싶은 책 -> 읽고 싶은 책 
            $want_update_stmt = $conn -> prepare("UPDATE BOOK_WANT SET WANT_RATING = ?, WANT_PREVIEW = ? 
            WHERE BOOK_WANT_NUMBER = ? AND BOOK_WANT_USER_NUMBER = ? AND BOOK_INFO_NUMBER = ?"); 
            $want_update_stmt -> bind_param("ssiii", $modi_want_rating, $modi_want_preview, $before_library_number, $user_number, $book_number); 
            if ($want_update_stmt->execute()) {
                // 읽고 싶은책-> 읽고 싶은책 update 성공 
                echo json_encode(array(
                    "code" => 200,
                    "message" => $update_ok . '위치: 읽고 싶은 책에서 읽고 싶은 책으로 업데이트' . '수정한점수: '. $modi_want_rating . ', 수정한 기대평: ' . $modi_want_preview
                ), JSON_UNESCAPED_UNICODE);
            } else {
                // 읽고 싶-> 읽고 싶은 책으로 업데이트 update 실패 
                echo json_encode(array(
                    "code" => 200,
                    "message" => $update_query_failed . '읽고 싶은 책에서 읽고 싶은 책으로 update실패'
                ), JSON_UNESCAPED_UNICODE);

            } // 읽고 싶은 책-> 읽고 싶은책 끝 

        } else if ($before_type == '1') { // 읽은 책 -> 읽고 싶은 책 
            // 읽은 책 already 에서 delete후, want에 insert 
            $already_delete_stmt = $conn->prepare("DELETE FROM BOOK_ALREADY WHERE USER_NUMBER = ? AND BOOK_INFO_NUMBER = ? ");
            $already_delete_stmt->bind_param("ii", $user_number, $book_number); 
            if ($already_delete_stmt->execute()) {

                // already 삭제 완료 !!! want insert 해주기 
                $want_insert_stmt = $conn -> prepare("INSERT INTO BOOK_WANT (USER_NUMBER, BOOK_INFO_NUMBER, WANT_RATING, WANT_PREVIEW) 
                VALUES (?, ?, ?, ?)"); 
                $want_insert_stmt->bind_param("iiss", $user_number, $book_number, $modi_want_rating, $modi_want_preview); 
                if($want_insert_stmt->execute()) {
                    // 읽은 책 already 삭제 후 읽고 싶은 책 insert 까지 완료
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $insert_ok . ", 위치: 읽은 책-> 읽고 싶은 책으로 변경하기 위한 insert 성공 !!"
                    ), JSON_UNESCAPED_UNICODE);

                } else { 
                    // 읽은 책에서 읽고 싶은 책으로 insert 문 실패 
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $insert_query_failed . ",위치: 읽은 책에서 읽고 싶은 책으로 변경하기 위한 insert문 실패"
                    ), JSON_UNESCAPED_UNICODE);
                }
            } else {  // 읽은 책에서 읽고 싶은 책으로 변경 위한 읽은 책 delete 문 실패 
                echo json_encode(array(
                    "code" => 200,
                    "message" => $delete_query_failed . ", 위치: 읽은 책->읽고 싶은책 책으로 변경하기 위한 delete문 실패"
                ), JSON_UNESCAPED_UNICODE);
            }

        } else if ($before_type == '2') { // 읽고 있는 책 -> 읽고 싶은 책으로 변경한 경우 1 

            // 읽고 있는 책 reading 삭제 후, 읽고 싶은 책 want insert 
            $reading_delete_stmt = $conn->prepare("DELETE FROM BOOK_READING WHERE BOOK_READING_NUMBER = ?");
            $reading_delete_stmt->bind_param("i", $before_library_number); 
            if ($reading_delete_stmt->execute()) { // 읽고 있는 책 delete 성공 ! 
                // 읽고 싶은 책 insert 하기. 
                $want_insert_stmt = $conn -> prepare("INSERT INTO BOOK_WANT (USER_NUMBER, BOOK_INFO_NUMBER, WANT_RATING, WANT_PREVIEW) 
                VALUES (?, ?, ?, ?)"); 
                $want_insert_stmt->bind_param("iiss", $user_number, $book_number, $modi_want_rating, $modi_want_preview); 
                if($want_insert_stmt->execute()) { // 읽고 싶은 책 insert 성공 
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $insert_ok . ", 위치: 읽고 있는 책-> 읽고 싶은 책으로 변경하기 위한 insert 성공 !!"
                    ), JSON_UNESCAPED_UNICODE);
                } else {
                    // 읽고 싶은 책 insert 실패  
                    echo json_encode(array(
                        "code" => 200,
                        "message" => $insert_query_failed . ",위치: 읽고 있는 책에서 읽고 싶은 책으로 변경하기 위한 insert문 실패"
                    ), JSON_UNESCAPED_UNICODE);
                }
            } else { // 읽고 있는 책 delete 실패 
                echo json_encode(array(
                    "code" => 200,
                    "message" => $delete_query_failed . ", 위치: 읽고 있는 책->읽고 싶은책 책으로 변경하기 위한 delete문 실패"
                ), JSON_UNESCAPED_UNICODE);
            }
        }
    } // else if 끝 

}
