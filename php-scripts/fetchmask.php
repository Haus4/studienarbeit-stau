<?php
include 'db-connection.php';
header('Content-type:image/png');
$conn = OpenCon();
$cameraId = $_GET["camera"];
$orientationId = $_GET["orientation"];
$tscondition = "";
if(array_key_exists("time", $_GET)){
    $datetime = date("Y-m-d H:i:s", $_GET["time"]);
    $tscondition = " AND inserttimestamp > '" . $datetime . "'";
}
$query = "SELECT image FROM masks WHERE camera_id=? AND orientation=?";
$stmt = $conn->prepare($query);
$stmt->bind_param("si", $cameraId, $orientationId);
if ($stmt->execute()) {
    $image = null;
	$stmt->bind_result($image);
    if($stmt->fetch()) {
		echo $conn->error;
        echo $image;
    } else {
		echo $conn->error;
	}
    $stmt->close();
} else {
    echo $conn->error;
}
CloseCon($conn);
?>