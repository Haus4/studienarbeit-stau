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

while(true)
{
	saveToDb();
    //avoid CPU exhaustion, adjust as necessary
    usleep(60000000);//0.002 seconds
}

flock($hLock, LOCK_UN);
fclose($hLock);
unlink(__FILE__.".lock");
?>
