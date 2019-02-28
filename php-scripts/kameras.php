<?php
include 'db-connection.php';
$conn = OpenCon();
$tscondition = "";
$query = "SELECT DISTINCT masks.camera_id FROM masks, cameras WHERE masks.camera_id=cameras.id AND cameras.refcount > 0";
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