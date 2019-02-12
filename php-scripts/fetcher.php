<?php
include 'db-connection.php';
header('Content-type:application/octet-stream');
$conn = OpenCon();
$cameraId = $_GET["camera"];
$tscondition = "";
if(array_key_exists("time", $_GET)){
    $datetime = date("Y-m-d H:i:s", $_GET["time"]);
    $tscondition = " AND inserttimestamp > '" . $datetime . "'";
}
$query = "SELECT image, inserttimestamp FROM images WHERE camera_id=?" . $tscondition . " ORDER BY inserttimestamp ASC";
$stmt = $conn->prepare($query);
$stmt->bind_param("s", $cameraId);
if ($stmt->execute()) {
    $image = null;
    $inserttimestamp = null;
	$stmt->bind_result($image, $inserttimestamp);
    while($stmt->fetch()) {
        $timestamp = strtotime($inserttimestamp);
        $time = pack("V", $timestamp);
        $nullByte = pack("V", 0);
        $data = $time . $nullByte . $image;
        echo pack("V",mb_strlen($image,'8bit'));
        echo $data;
    }
    $stmt->close();
} else {
    echo $conn->error;
}
CloseCon($conn);
?>