<?php
include 'db-connection.php';
$conn = OpenCon();
$abid = null;
$result = null;
$stmt = null;
$show = null;
if(array_key_exists("abid", $_GET)){
	$abid = $_GET["abid"];
}
if(array_key_exists("show", $_GET)){
	$show = $_GET["show"];
}
$query = "SELECT DISTINCT masks.camera_id FROM masks, cameras WHERE masks.camera_id=cameras.id AND cameras.refcount > 0";
if(isset($show)) {
	if(strcasecmp($show,"all") == 0) {
		$query = "SELECT DISTINCT id as camera_id from cameras WHERE 1=1";
	} else if(strcasecmp($show,"deactive") == 0) {
		$query = "SELECT DISTINCT masks.camera_id FROM masks, cameras WHERE masks.camera_id=cameras.id AND cameras.refcount = 0";
	} else if(strcasecmp($show,"unmasked") == 0) {
		$query = "SELECT DISTINCT cameras.id as camera_id from cameras LEFT JOIN masks ON cameras.id=masks.camera_id WHERE masks.camera_id IS NULL";
	}
}
if(isset($abid)) {
	$query.=" AND cameras.abid=?";
	$stmt = $conn->prepare($query);
	$stmt->bind_param("s", $abid);
	$stmt->execute();
	$result = $stmt->get_result();
} else {
	$result = $conn->query($query);
}
if ($result) {
    while($row = $result->fetch_assoc()) {
		echo $row["camera_id"] . "\r\n";
    }
} else {
    echo $conn->error;
}
if(isset($stmt)) $stmt->close();
CloseCon($conn);
?>
