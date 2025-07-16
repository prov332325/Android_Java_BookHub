<?php 
$servername = "ec2-3-39-255-234.ap-northeast-2.compute.amazonaws.com";
// $servername = "localhost";
$username = "eunsil1011";
$password = "Monica6451!";
$dbname = "book_hub";

$conn = new mysqli($servername, $username, $password, $dbname);

if($conn->connect_error) {
    die("MySQL 연결 실패: " . $conn->connect_error);
} 


?>