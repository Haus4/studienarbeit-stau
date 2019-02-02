<?php
include 'constants.php';
include 'db-connection.php';
function saveToDb() {
    echo Urls::REFERER;
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
      file_put_contents('./test.jpeg', $file);
      $conn = OpenCon();
      if ($conn->connect_errno) {
          echo "Errno: " . $conn->connect_errno . "\n";
      }
      $dataTime = date("Y-m-d H:i:s");
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
?>