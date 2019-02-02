<?php
include 'constants.php';
include 'db-connection.php';
function saveToDb() {
    $ref = sprintf("Referer: %s", Urls::REFERER);
    $opts = array(
        'http'=>array(
          'method'=>"GET",
          'header'=> $ref
        )
      );
      
      $context = stream_context_create($opts);
      
      // Open the file using the HTTP headers set above
      $url = sprintf(Urls::CAMERA_TEMPLATE, "KA061", "KA061");
      $file = file_get_contents($url, false, $context);
      $lastModified = strtotime(getLastModified($http_response_header));
      $conn = OpenCon();
      if ($conn->connect_errno) {
          echo "Errno: " . $conn->connect_errno . "\n";
      }
	  clearOldFiles($conn);
      $dataTime = date("Y-m-d H:i:s", $lastModified);
      $image = $conn->real_escape_string($file);
      //Insert image content into database
      $insert = $conn->query("INSERT into images (camera_id, inserttimestamp, image) VALUES ('KA061', '$dataTime', '$image')");
      if($insert){
        echo "File uploaded successfully.";
        }else{
        echo $conn->error;
        } 
      CloseCon($conn);
      
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

function clearOldfiles($conn)
{
    $count = countRows($conn);
    if($count > 5) {
        $rowCount = $count - 5;
        $query = "DELETE FROM images ORDER BY inserttimestamp ASC LIMIT " . $rowCount;
        $test = $conn->query($query);
    }
}

function countRows($conn)
{
    $query = "SELECT COUNT(*) AS count FROM images";
    if ($result = $conn->query($query)) {
        $row = $result->fetch_assoc();
        return $row["count"];
    }
    return 0;
}
?>