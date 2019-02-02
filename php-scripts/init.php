<?php
$cx=stream_context_create(
    array(
        "http"=>array(
            "timeout" => 1, //at least PHP 5.2.1
            "ignore_errors" => true
        )
    )
);
@file_get_contents("http://localhost/camera-daemon.php", false, $cx);
?>