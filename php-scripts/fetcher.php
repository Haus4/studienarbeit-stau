<?php
include 'db-connection.php';
header('Content-type:application/octet-stream');
$conn = OpenCon();
$query = "SELECT image, inserttimestamp FROM images ORDER BY inserttimestamp ASC";
if ($result = $conn->query($query)) {
    while($row = $result->fetch_assoc()) {
        $image = $row["image"];
        $timestamp = strtotime($row["inserttimestamp"]);
        $time = pack("V", $timestamp);
        $nullByte = pack("V", 0);
        $data = $time . $nullByte . $image;
        echo pack("V",mb_strlen($image,'8bit'));
        echo $data;
    }
    $result->free();
} else {
    echo $conn->error;
}
CloseCon($conn);
?>