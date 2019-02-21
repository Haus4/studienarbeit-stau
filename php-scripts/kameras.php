<?php
include 'db-connection.php';
$conn = OpenCon();
$tscondition = "";
$query = "SELECT DISTINCT masks.camera_id FROM masks, images WHERE masks.camera_id=images.camera_id";
$result = $conn->query($query);
if ($result) {
    while($row = $result->fetch_assoc()) {
		echo $row["camera_id"] . "\r\n";
    }
} else {
    echo $conn->error;
}
CloseCon($conn);
?>