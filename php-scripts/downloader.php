<?php
include 'constants.php';
include 'db-connection.php';
function saveAllCams() {
    $conn = OpenCon();
    if ($conn->connect_errno) {
        echo "Errno: " . $conn->connect_errno . "\n";
    }
    if ($result = $conn->query("SELECT id FROM cameras")) {
        while($row = $result->fetch_assoc()) {
            $kameraId = $row["id"];
            saveToDb($conn, $kameraId);
        }
    }
    CloseCon($conn);
}

function saveToDb($conn, $kameraId) {
      // Open the file using the HTTP headers set above
      $url = sprintf(Urls::CAMERA_TEMPLATE, $kameraId, $kameraId);
      $ch = curl_init($url);
      curl_setopt($ch, CURLOPT_HEADER, 1);
      curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
      curl_setopt($ch, CURLOPT_REFERER, Urls::REFERER);
      curl_setopt($ch, CURLOPT_TIMEOUT, 1000);
      curl_setopt($ch, CURLOPT_BINARYTRANSFER,1);
      curl_setopt($ch, CURLOPT_FILETIME, 1);
      //curl_setopt($ch, CURLOPT_PROXY, '127.0.0.1:8888');
      $response=curl_exec($ch);
      $info = curl_getinfo($ch);
      $header_size = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
      $file = substr($response, $header_size);
      $lastModified = $info["filetime"];
      $dataTime = date("Y-m-d H:i:s", $lastModified);
      $image = $conn->real_escape_string($file);
      //Insert image content into database
      $insert = $conn->query("INSERT into images (camera_id, inserttimestamp, image) VALUES ('$kameraId', '$dataTime', '$image')");
      if($insert){
        echo "File uploaded successfully.";
        }else{
        echo $conn->error;
        } 
      curl_close ($ch);
      clearOldFiles($conn, $kameraId);
}

function clearOldfiles($conn, $kameraId)
{
    $count = countRows($conn, $kameraId);
    if($count > 10) {
        $rowCount = $count - 10;
        $query = "DELETE FROM images WHERE camera_id='$kameraId' ORDER BY inserttimestamp ASC LIMIT $rowCount";
        $test = $conn->query($query);
    }
}

function countRows($conn, $kameraId)
{
    $query = "SELECT COUNT(*) AS c FROM images WHERE camera_id='$kameraId'";
    if ($result = $conn->query($query)) {
        $row = $result->fetch_assoc();
        return $row["c"];
    }
    return 0;
}
?>