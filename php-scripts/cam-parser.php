<?php
include './constants.php';
include './db-connection.php';
header("Content-Type: text/plain");
$data = file_get_contents(Urls::CAMS_A);
//echo $data . "\r\n";
$data = explode(PHP_EOL, $data);
$dataArray = [[]];
$conn = OpenCon();
if ($conn->connect_errno) {
    echo "Errno: " . $conn->connect_errno . "\n";
}
for($i=0; $i<count($data); $i++) {
    $dataArray[$i] = explode("\t", $data[$i]);
}
$header = false;
$chars = $conn->query("SET NAMES 'utf8'");
if($chars) echo $conn->error;
foreach($dataArray as $subArray) {
    if(!$header) {
        $header = true;
        continue;
    }
    if(count($subArray) != 8) {
        echo "error: expected 8 parameters but found " . count($subArray);
        echo "\r\n\r\n";
        foreach($subArray as $sth) echo "found: " .  $sth . " ";
        continue;
    }
    echo "lon=" . $subArray[0] . ";";
    echo "lat=" . $subArray[1] . ";";
    preg_match("/(&Ouml;\-)?A[0-9]+/", $subArray[2], $aid);
	$aid[0] = html_entity_decode($aid[0]);
    echo "aId=" . $aid[0] . ";";
    preg_match("/(&Ouml;\-)?A[0-9]+ ((?! \- km| \- Km)[a-zA-Z &;()\-1-9\/])+/", $subArray[2], $desc);
    $desc[0] = html_entity_decode($desc[0]);
	echo "desc=" . $desc[0] . ";";
    preg_match("/ftpdata\/[A-Z]+[0-9]+\//", $subArray[3], $camIdReg);
    echo "camid=" . substr($camIdReg[0],8,-1) . "\r\n";
	$abid = $aid[0];//utf8_encode($aid[0]);
	$camid = substr($camIdReg[0],8,-1);
	$desc = $desc[0]; //utf8_encode($desc[0]);
	$lat = $subArray[0];
	$lon = $subArray[1];
	if(!$conn->query("INSERT INTO cameras(id,abid, description, latitude,longitude) VALUES('$camid', '$abid', '$desc', $lat, $lon)")) {
		echo $conn->error;
	}
}
CloseCon($conn);
?>