<?php
header('Content-Type: application/json');
//Begin connection to server
$servername = "localhost";
$database = "u645071659_makerspace";
$username = "u645071659_hyperx66";
$password = "s9740499b";
// Create connection
$conn = mysqli_connect($servername, $username, $password, $database);
if (mysqli_connect_errno($conn)) {
    die('Failed to connect to MySQL: ' . mysqli_connect_error());
}

$dateToday = date('-m-Y');

$sql = "SELECT (SELECT COUNT(twitterEmotion) FROM assignment WHERE twitterEmotion = \"Positive\" AND  timeStamp LIKE '%".$dateToday."%') AS positive, (SELECT COUNT(twitterEmotion) FROM assignment WHERE twitterEmotion = \"Negative\" AND  timeStamp LIKE '%".$dateToday."%') AS negative, (SELECT COUNT(twitterEmotion) FROM assignment WHERE twitterEmotion = \"Neutral\" AND  timeStamp LIKE '%".$dateToday."%') AS neutral";
$result = mysqli_query($conn, $sql);
$data = array();
foreach ($result as $row) {
    $data[] = $row;
}
mysqli_close($conn);

echo json_encode($data);
