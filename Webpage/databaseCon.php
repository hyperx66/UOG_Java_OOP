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

$sql = "SELECT * FROM caseFindings WHERE timeStamp LIKE '%".$dateToday."%'";
$result = mysqli_query($conn, $sql);
$data = array();
foreach ($result as $row) {
    $data[] = $row;
}
mysqli_close($conn);

echo json_encode($data);
