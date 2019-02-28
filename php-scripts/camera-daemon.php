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
	if(file_exists(__FILE__.".lock")) {
		flock($hLock, LOCK_UN);
		fclose($hLock);
		unlink(__FILE__.".lock");
	}
};
register_shutdown_function($callback);

while(file_exists(__FILE__.".lock"))
{
	saveAllCams();
    //avoid CPU exhaustion, adjust as necessary
    usleep(29000000);//29 seconds
}

?>
