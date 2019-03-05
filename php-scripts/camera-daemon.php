<?php
include'./downloader.php';
ignore_user_abort(true);//if caller closes the connection (if initiating with cURL from another PHP, this allows you to end the calling PHP script without ending this one)
set_time_limit(0);

$hLock=fopen(__FILE__.".lock", "w+");
if(!flock($hLock, LOCK_EX | LOCK_NB))
    die("Already running. Exiting...");

$conn = OpenCon();
if ($conn->connect_errno) {
    echo "Errno: " . $conn->connect_errno . "\n";
}
//cleanup on fatal error
$callback = function() use($hLock) {
	$e = error_get_last();
	if($e !== NULL && $e['type'] === E_ERROR) {
		removeLockIfExists($hLock);
		$log=fopen(__FILE__."-error.log", "a+");
		if(!flock($log, LOCK_EX))
			sleep(1);
		fputs($log, "[".date(DATE_RFC822)."] unexpected daemon shutdown: ".$e['message']."\r\n"
		."in ".$e['file']." on line ".$e['line']."\r\n");
		flock($log, LOCK_UN);
		fclose($log);
	}
};
register_shutdown_function($callback);

$i = 0;
$notUserKilled = file_exists(__FILE__.".lock");

while($notUserKilled && $i<30)
{
	saveAllCams();
    //avoid CPU exhaustion, adjust as necessary
    usleep(29000000);//29 seconds
	$i++;
	$notUserKilled = file_exists(__FILE__.".lock");
}

removeLockIfExists($hLock);
if($notUserKilled) {
	$cx=stream_context_create(
		array(
			"http"=>array(
				"timeout" => 1, //at least PHP 5.2.1
				"ignore_errors" => true
			)
		)
	);
	@file_get_contents("http://stau.bomhardt.de/camera-daemon.php", false, $cx);
}
exit;

function removeLockIfExists($hLock) {
	if(file_exists(__FILE__.".lock")) {
		flock($hLock, LOCK_UN);
		fclose($hLock);
		unlink(__FILE__.".lock");
	}
}

?>
