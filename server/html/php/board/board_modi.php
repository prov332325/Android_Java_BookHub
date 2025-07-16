<?php

use function PHPSTORM_META\elementType;
use function PHPSTORM_META\type;

header('Content-Type: application/json; charset=UTF-8');
include("../db_connection.php");


// 가지고 올 것
// 유저 번호, 제목, 내용, 카테고리.  

// 1) 유저 넘버 
if (isset($_POST["user_number"])) {
    $user_number =  intval($_POST['user_number']);
} else {
    $user_number = null;
}


// 1-2) 게시글 번호 
if (isset($_POST["board_number"])) {
    $board_number =  intval($_POST['board_number']);
} else {
    $board_number = null;
}


// 2) 게시글 제목 
if (isset($_POST["edited_title"])) {
    $edited_title = $_POST['edited_title'];
} else {
    $edited_title = null;
}

// 3) 게시글 내용 
if (isset($_POST["edited_content"])) {
    $edited_content = $_POST['edited_content'];
} else {
    $edited_content = null;
}

// 4) 게시글 카테고리 
if (isset($_POST["edited_category"])) {
    $edited_category = $_POST['edited_category'];
} else {
    $edited_category = null;
}

// 응답 
$response = array();

// 수정하고자 하는 책 제목들 
$update_booktitles = array();

// 기존 db에 있던 책 제목들 
$db_booktitles = array();


// 메시지 모음 
$error_msg = "필수 인자가 부족합니다";
$update_success_msg = "게시판 수정이 완료되었습니다";
$board_update_error_msg = "게시판 저장 update 실행 오류";


if (
    $user_number == '' || $board_number == '' ||
    $edited_title == '' || $edited_content == '' || $edited_category == ''
) { // 필수 인자가 null 일 경우 
    $response['code'] = 400;
    $response['message'] = $error_msg;
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit;
} else { // 필수 인자가 다 존재하는 경우   
    


    // 게시글 update 
    $board_update_stmt = $conn->prepare("UPDATE BOARD SET BOARD_TITLE = ?, BOARD_CONTENT = ? , BOARD_CATEGORY = ? WHERE BOARD_ID = ? AND USER_NUMBER = ? ");
    $board_update_stmt->bind_param("sssii", $edited_title, $edited_content, $edited_category, $board_number, $user_number);
    if ($board_update_stmt->execute()) { // update 완료  

        // 책 수정하기. 

        // 수정하고자 하는 책 목록 가져옴. 있을때 없을때 분기 처리. 있을 때에만 기존꺼 가져와서 비교해주기. 

        if (isset($_POST['books'])) {
            // 수정하려고 넘어온 책 정보를 배열에 담아줌. 
            $books_json = $_POST['books'];
            $books = json_decode($books_json, true);

            // 오류 예외 처리해주기. 
            if (json_last_error() !== JSON_ERROR_NONE) {
                $response['code'] = 400;
                $response['message'] = "책 정보 JSON 형식 오류";
                echo json_encode($response, JSON_UNESCAPED_UNICODE);
                exit;
            }

            // 기존 db의 책 내용을 select 해온다. 
            $board_book_stmt = $conn->prepare("SELECT BOOK_TITLE, BOOK_AUTHOR, BOOK_DESCRIPTION, BOOK_COVER, BOARD_NUMBER FROM BOOK_INFO WHERE BOARD_NUMBER = ? ");
            $board_book_stmt->bind_param("i", $board_number);

            if ($board_book_stmt->execute()) { // 게시글 책 정보 쿼리 실행 성공  

                $book_result = $board_book_stmt->get_result(); // result 는 배열임.  

                if ($book_result->num_rows > 0) {


                    while ($row = $book_result->fetch_assoc()) {
                        // db에 있는 책의 제목들 
                        $db_booktitles[] = $row['BOOK_TITLE'];
                        // [ ] 대괄호는 새로운 배열을 추가할때 사용하는 표현법임. 
                    } //while문 끝  


                    // db_booktitles 배열을 문자열로 변환하여 메시지에 포함
                    $db_booktitles_str = implode(", ", $db_booktitles);

                    // $response['code'] = 200;
                    // $response['message'] = "게시글에 기존 책 정보 존재함: " . $db_booktitles_str;
                    // echo json_encode($response, JSON_UNESCAPED_UNICODE);
                    // exit;


                    // 넘어온 책의 제목들.  
                    if (!empty($books)) {


                        foreach ($books as $book) {
                            $title = $conn->real_escape_string($book['title']);
                            $update_booktitles[] = $title;
                        } // for문 종료 

                        // $update_booktitles = array_map(function ($book) {
                        //     return $book['title'];
                        // }, $books); 

                        $update_booktitles_str = implode(", ", $update_booktitles);

                        // $response['code'] = 200;
                        // $response['message'] = "update 하려고 넘긴 책 제목들: " . $update_booktitles_str;
                        // echo json_encode($response, JSON_UNESCAPED_UNICODE);
                        // exit;


                        // 이제 2개의 배열이 생겼음. a: db titles / b: update titles
                        // 비교하기 
                        // db에는 있고 update에는 없는 것. - delete하기 
                        $titles_to_delete  = array_diff($db_booktitles, $update_booktitles); 

                        // $titles_to_delete_str = implode(", ", $titles_to_delete); 
                        //  $response['code'] = 200;
                        // $response['message'] = "db에서 삭제해야하는책 " . $titles_to_delete_str;
                        // echo json_encode($response, JSON_UNESCAPED_UNICODE);
                        // exit;

                        if (!empty($titles_to_delete)) {
                            $book_delete_stmt = $conn->prepare("DELETE FROM BOOK_INFO WHERE USER_NUMBER = ? AND BOARD_NUMBER = ? AND BOOK_TITLE = ?");

                            foreach ($titles_to_delete  as $title) {
                                $book_delete_stmt->bind_param("iis", $user_number, $board_number, $title);
                                if ($book_delete_stmt->execute()) { 

                                } else {
                                    $response['code'] = 400;
                                    $response['message'] = "삭제된 책 정보 DB에서 삭제하기 실패";
                                }
                            } // 삭제 끝 

                            // $update_success_msg .= "사용자가 삭제한 책 정보 삭제 완료"; 
                            // $response['code'] = 200;
                            // $response['message'] = "사용자가 삭제한 책 정보 삭제 완료";
                            // echo json_encode($response, JSON_UNESCAPED_UNICODE);
                            // exit;

                        }


                        // 그럼 반대로 새로 넘어온 배열에는 있고 db에는 없으면 insert 해줘야하넹. 
                        $titles_to_insert = array_diff($update_booktitles, $db_booktitles);
                        if (!empty($titles_to_insert)) {
                            $book_insert_stmt = $conn->prepare("INSERT INTO BOOK_INFO (USER_NUMBER, BOOK_TITLE, BOOK_AUTHOR, BOOK_DESCRIPTION, BOOK_COVER, BOARD_NUMBER) VALUES (?, ?, ?, ?, ?, ?)");
                            foreach ($books as $book) {
                                if (in_array($book['title'], $titles_to_insert)) {
                                    $title = $conn->real_escape_string($book['title']);
                                    $author = $conn->real_escape_string($book['author']);
                                    $description = $conn->real_escape_string($book['description']);
                                    $cover = $conn->real_escape_string($book['cover']);
                                    $book_insert_stmt->bind_param("issssi", $user_number, $title, $author, $description, $cover, $board_number);
                                    $book_insert_stmt->execute();
                                }
                            } // for문 종료 

                            $update_success_msg .= " 새로운 책 정보 DB에 추가 완료";
                        }
                        $response['code'] = 200;
                        $response['message'] = $update_success_msg;
                    } else { // 넘어온 목록은 있는데 기존 책이 없을때 !!!! insert 해줘야 함. 
                        // book 목록 inser 해줘야 함. 
                        // 다른 delete 할 작업이 필요 없으니까 여기에 예외 주기. 

                        if (!empty($books)) {
                            foreach ($books as $book) {
                                $title = $conn->real_escape_string($book->title);
                                $author = $conn->real_escape_string($book->author);
                                $description = $conn->real_escape_string($book->description);
                                $cover = $conn->real_escape_string($book->cover);

                                // db에 저장하기. 
                                $book_insert_stmt = $conn->prepare("INSERT INTO BOOK_INFO (USER_NUMBER, BOOK_TITLE, BOOK_AUTHOR, BOOK_DESCRIPTION, BOOK_COVER, BOARD_NUMBER ) VALUES (?, ?, ?,?, ?, ?)");
                                $book_insert_stmt->bind_param("issssi",$user_number, $title, $author, $description, $cover, $board_number);

                                if ($book_insert_stmt->execute()) {
                                    $update_success_msg .= ", 쿼리 실행 완+1";
                                } else {
                                    $response['code'] = 400;
                                    $response['message'] = "새 책정보 insert 실패";
                                }
                            } // for문 끝
                        } else {
                            $update_success_msg .= ", post book값은 있는데 배열에 넣으니 안되는 경우.. 뭐지";
                        }
                    } // 넘어온 목록은 있는데 기존 책이 없을때 !!!! insert 해줌. 끝 

                    // 수정한 책 내용을 가져온다.  


                    // A: 기존 DB에 저장된내용
                    // B: 수정하려고 들고온 내용 

                    // 1. A에 있는데 B에없는 경우 - 삭제한것이므로 DB에서 delete 
                    // 2. B에 있는데 A에 없는 경우 - 새로 추가한 것이므로 DB에 insert - 완료 
                    // 3. A에도 있고 B에도 있는 경우 - 변경 사항 없으므로 그대로 유지하기 


                } else { // select문 실패 
                    $response['code'] = 400;
                    $response['message'] = "기존 책 가져오는 select문 실패";
                }
            } else { // 넘어온 책 정보가 없음, 기존 데이터와 비교할 이유가 없음. 
                $response['code'] = 200;
                $response['message'] = $update_success_msg . ", 넘어온 책 정보가 아예 없음";
            } // else문 끝  
        } else { // 게시글 update 쿼리 실패 - 게시글 제목, 내용, 카테고리 
            $response['code'] = 400;
            $response['message'] = "게시글 update 쿼리 실패";
        } 

        
    } // board update 완료 
} // null 확인 완료

echo json_encode($response, JSON_UNESCAPED_UNICODE);
