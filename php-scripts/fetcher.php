<?php
include 'db-connection.php';
header('Content-type:application/octet-stream');
$conn = OpenCon();
$query = "SELECT image FROM images ORDER BY inserttimestamp LIMIT 5";
if ($result = $conn->query($query)) {
    while($row = $result->fetch_assoc()) {
        $value = $row["image"];
        echo pack("V",mb_strlen($value,'8bit'));
        echo $value;
    }
    $result->free();
}
CloseCon($conn);
?>