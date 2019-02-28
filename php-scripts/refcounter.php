<?php
include 'db-connection.php';

$conn = OpenCon();

if(array_key_exists("register", $_GET)){
	$cameras = explode(",",$_GET["register"]);
	updateRefcount($cameras,$conn,true);
}
if(array_key_exists("deregister", $_GET)){
	$cameras = explode(",",$_GET["deregister"]);
	updateRefcount($cameras,$conn,false);
}
function updateRefcount($cameras, $conn, $inc) {
	$refCameras = makeValuesReferenced($cameras);
	$op = $inc ? "+ 1" : "- 1"; 
	$sql = "UPDATE cameras SET refcount = refcount ".$op." WHERE id=?";
	$type = "s";
	extendQuery($sql, $cameras, $type);
	if(!$inc) $sql.= " AND refcount > 0";
	$stmt = $conn->prepare($sql);
	call_user_func_array(array($stmt, "bind_param"), array_merge(array($type), $refCameras));
	$stmt->execute();
	$stmt->close();
}
function makeValuesReferenced(&$arr){ 
    $refs = array(); 
    foreach($arr as $key => $value) 
        $refs[$key] = &$arr[$key]; 
    return $refs; 
}
function extendQuery(&$sql, $cameras, &$type) {
	$skippedFirst=false;
	foreach($cameras as $camera) {
		if(!$skippedFirst){
			$skippedFirst=true;
			continue;
		}
		$sql.=" OR id=?";
		$type.="s";
	}
}
CloseCon($conn);
?>