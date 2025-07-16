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


// 게시글 번호, 유저 번호 
if (isset($_POST["board_number"])) {
    $board_number =  intval($_POST['board_number']);
} else {
    $board_number = null;
}



if (isset($_POST["user_number"])) {
    $user_number =  intval($_POST['user_number']);
} else {
    $user_number = null;
}



// message 
$success = "기존 이미지 서버업데이트중, "; // 삭제도 해야하나? 
$db_success = "이미지 DB 업데이트 완료";
$db_fail = "이미지 DB 업데이트 실패";
// $type_error = "사진타입 jpg, png 아님";
$upload_error = "새로운 사진 서버 업로드 실패";
$select_query_failed = "기존 이미지 가져오는 select 쿼리문 실패";
$delete_query_failed = "기존 이미지중 삭제된 이미지 delete 실패";



// 경우의 수 따져보기 
// 첫번째 경우, 기존 리스트에 대해서는 DB와 하나씩 비교해본다. 

// 두번째 경우, 넘어온 새로운 이미지 file에 대해서는 DB에 저장한다. 
// 1) DB에서 새로 추가된 경우는 없음  
// 2) 그대로인 경우. 아무일도 일어나지 않음. 
// 3) 삭제된 경우 -> DB에서 delete 하기 / 서버에서도 delete 하기. 


// 서버로 넘어온 파일 받기. 
// list 의 경우, 어떻게 넘어오는가 ?? 
// 새로추가한 이미지 list, 기존에 있던 이미지 url list, 게시글번호 string, 게시글 작성자 string. 
// 새이미지 list, 기존 이미지 url list 중 둘중 하나는 null 일 수도 있음. 둘다 null 일 수는 없음. 
// 서버에 새이미지를 저장하거나 기존 이미지를 DB에 있는 이미지와 비교하기 전에, 각각 array()가 null 인지 확인먼저하기. 




// 기존 이미지 array - null 일 경우에 대해 분기처리 완료. 
$img_urls = array();
$img_urls = isset($_POST['imgUrls']) ? $_POST['imgUrls'] : array();
// 서버로 넘어오는 post 값은 key value 쌍의 배열 형태로  수신됨. 따라서 서버단 php 에서는 post 로 넘어오면 배열로 받음. 
$db_imgs = array();


// Ensure $img_urls is an array of strings
if (!is_array($img_urls)) {
    $img_urls = array();
    $delete_query_failed .= ", img url은 array아님 ," . $_POST['imgUrls'];
}

// echo json_encode(array(
//     "success" => true,
//     "message" => $delete_query_failed. $success,
//     "image_urls" => $img_urls
// ), JSON_UNESCAPED_UNICODE); 


// 기존 이미지에 대한 처리 먼저 해주기  


if (empty($img_urls)) { // 기존 이미지 없음
    $success .= ", UPDATE시 기존에 DB에 저장된 이미지없음 ";
} else { // 업데이트할 때 기존 이미지 있음 
    // DB에서 이미지 가져오기.   
    $img_list_stmt = $conn->prepare("SELECT * FROM BOARD_IMG WHERE BOARD_ID = ? AND USER_NUMBER = ?");
    $img_list_stmt->bind_param("ii", $board_number, $user_number);
    if ($img_list_stmt->execute()) {
        $result = $img_list_stmt->get_result();
        if ($result->num_rows > 0) {
            // 한장씩 img urls 와 비교하기.  
            while ($row = $result->fetch_assoc()) {
                // 각각의 row가 img urls에 있는가 ?? 
                $db_imgs[] = $row['IMG_URL'];
                // [ ] 대괄호는 새로운 배열을 추가할때 사용하는 표현법임. 
            }

            // echo json_encode(array(
            //     "success" => true,
            //     "message" => $delete_query_failed,
            //     "image_urls" => $db_imgs
            // ), JSON_UNESCAPED_UNICODE);

            //     // $db_imgs 랑 $img_urls 비교하기 
            $intena = array_diff($db_imgs, $img_urls);
            // intena 는 array()임. 
            // 교집합이 아닌 아이들만 모여있음. 
            // db img에 없는게 img url 에 있을 수 없음.    

            // A에는 있고, B에는 없는 애들. 삭제해야 하는애들.


            if (is_array($intena)) {
                // echo json_encode(array(
                //     "success" => true,
                //     "message" => "intena 가 배열은 맞음",
                //     "image_urls" => array_values($intena)
                // ), JSON_UNESCAPED_UNICODE);
                // } else {

                //     echo json_encode(array(
                //         "success" => true,
                //         "message" => "배열아님 "
                //     ), JSON_UNESCAPED_UNICODE);    
                // }

                //     // delete 
                if (!empty($intena)) {
                    $img_delete_stmt = $conn->prepare("DELETE FROM BOARD_IMG WHERE IMG_URL = ? AND BOARD_ID = ? AND USER_NUMBER = ?");
                    // 삭제 실행을 반복문으로 돌리기!! 돌리면서 서버에서도 지우기. 

                    foreach ($intena as $value) {
                        $img_delete_stmt->bind_param("sii", $value, $board_number, $user_number);
                        if ($img_delete_stmt->execute()) {
                            // 사진 하나씩 delete 하는 중  

                        } else {
                            // 이때도 삭제해야 할게 있는데 삭제를 못하는 경우라서 response 만들기 
                            echo json_encode(array(
                                "success" => false,
                                "message" => $delete_query_failed
                            ), JSON_UNESCAPED_UNICODE);

                            // 
                        }
                    } // foreach 문 끝 
                    $success .= "사용자가 삭제한 이미지 db 삭제완료, ";
                    // 삭제끝. 
                    // echo json_encode(array(
                    //     "success" => true,
                    //     "message" => "사용자가 삭제한 이미지 db 삭제완료"
                    // ), JSON_UNESCAPED_UNICODE);
                } else {
                    // 삭제할 이미지가 없음. 
                    $success .= ", 삭제할 이미지가 없음";
                }
                $success .= ", 기존사진과 서버로부터 넘어온 사진 비교 완료";
            } else {
                // 기존 DB에 이미지 없을때
                // 처리 안해줘도 됨.
                $success .= ", UPDATE시 기존에 DB에 저장된 이미지없음. DB체크함 ";
            }
        } else {
            // 기존이미지가 있어서 서버에서 가져오려고 하는데 쿼리 오류. 이 경우에는 response 만들어서
            // 중단시키기 
            echo json_encode(array(
                "success" => false,
                "message" => $select_query_failed
            ), JSON_UNESCAPED_UNICODE);
        }
    } // 기존이미지가 존재하는 경우 끝 


    $filePaths = array();
    // $img_files = array();
    // $img_files = isset($_POST['images']) ? $_POST['images'] : array();

    // if (is_array($_FILES['images'])) {
    //     echo "<pre>";
    //     print_r($_FILES['images']);
    //     echo "</pre>";
    //     $message = "file 자체는 어레이임!!: " . json_encode($_FILES['images']);
    //     echo json_encode(array(
    //         "success" => true,
    //         "message" => $message
    //     ), JSON_UNESCAPED_UNICODE);
    // }



    // // 새로운 이미지 서버 및 DB에 저장하기. 
    if (isset($_FILES['images'])) { // 새로운 이미지에 대한 list가 null 이 아닐때   
        foreach ($_FILES['images']['name'] as $key => $name) {
            $fileName =  $_FILES['images']['name'][$key];
            $imgFullName = strtolower($fileName);
            $imgNameSlice = explode(".", $imgFullName); // . 기준으로 쪼개서 확장자 확인하기 
            $imgName = $imgNameSlice[0];
            $imgType = $imgNameSlice[1];
            //  $imgExt = array('jpg', 'png');

            $dates = date("mdhis", time());
            $newImgName = chr(rand(97, 122)) . chr(rand(97, 122)) . $dates . rand(1, 9) . "." . $imgType;

            $dir = "/var/www/html/php/img/";

            // 이미지를 서버에 저장한다. 
            $imgTmpName =  $_FILES['images']['tmp_name'][$key];
            if (move_uploaded_file($imgTmpName, $dir . $newImgName)) {
                // 이미지가 서버에 잘 저장됐을때 
                $uploadPath = $newImgName;

                // 파일 업로드 성공
                $filePaths[] = $uploadPath;

                // 이미지 insert 해줌. 
                $board_img_inser_stmt = $conn->prepare("INSERT INTO BOARD_IMG (BOARD_ID, USER_NUMBER, IMG_URL) VALUES (?, ?,?)");
                $board_img_inser_stmt->bind_param("iis", $board_number, $user_number, $newImgName);

                if ($board_img_inser_stmt->execute()) { // 이미지 DB 업로드 완료. 
                    $success .= "이미지 db저장중";
                } else { // 이미지 DB 저장 실패 
                    echo json_encode(array(
                        "success" => false,
                        "message" => $db_fail
                    ), JSON_UNESCAPED_UNICODE);
                }
            } else { // 이미지 서버 업로드 실패 
                echo json_encode(array(
                    "success" => false,
                    "message" => $upload_error
                ), JSON_UNESCAPED_UNICODE);
            }
        } // 파일 하나씩 이름 붙이고, 서버에 저장하는 foreach 문 끝 
        $success .= ", 새로운 이미지 insert 까지 완료";
        echo json_encode(array(
            "success" => true,
            "message" => $success 
        ), JSON_UNESCAPED_UNICODE);

    } else {
        // 사용자가 게시글 수정 시, 새롭게 이미지를 추가하지 않음. 
        // 이때에는 기존 내용에 대해서만 이미지를 업데이트하기. 
        // 여기서는 새로 추 가한 이미지가 없다는 메시지만 남기고. 최종 success 에다가 같이 보내ㅏㅁ. 
        // 기존 이미지를 비교하는 로직이 끝났을때 이 메시지를 안드에 같이 보냄.  
        $success .= "추가된 이미지 없음";
        echo json_encode(array(
            "success" => true,
            "message" => $success
        ), JSON_UNESCAPED_UNICODE);
    } 


}
