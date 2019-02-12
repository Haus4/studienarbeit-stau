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
    $ref = sprintf("Referer: %s", Urls::REFERER);
    $opts = array(
        'http'=>array(
          'method'=>"GET",
          'header'=> $ref
        )
      );
      
      $context = stream_context_create($opts);
      
      // Open the file using the HTTP headers set above
      $url = sprintf(Urls::CAMERA_TEMPLATE, $kameraId, $kameraId);
      $file = file_get_contents($url, false, $context);
      $lastModified = strtotime(getLastModified($http_response_header));
      $dataTime = date("Y-m-d H:i:s", $lastModified);
      $image = $conn->real_escape_string($file);
      //Insert image content into database
      $insert = $conn->query("INSERT into images (camera_id, inserttimestamp, image) VALUES ('$kameraId', '$dataTime', '$image')");
      if($insert){
        echo "File uploaded successfully.";
        }else{
        echo $conn->error;
        } 
      clearOldFiles($conn, $kameraId);
}

function getLastModified($headers)
{
    $i = 0;
    foreach( $headers as $header )
    {
        $t = explode(":",$header,2);
        if(count($t) >= 2 && !strcasecmp($t[0],"Last-Modified") ) {
            return trim( $t[1] );
        }
        $i++;
    }
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