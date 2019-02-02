CREATE DATABASE stauapp;
CREATE TABLE cameras (ID char(5), description varchar(100), latitude int, longitude int);
CREATE TABLE images (camera_id char(5), inserttimestamp DateTime, image blob);