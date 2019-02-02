CREATE DATABASE stauapp;
CREATE TABLE cameras (ID char(5), description varchar(100), latitude int, longitude int);
CREATE TABLE images (inserttimestamp DateTime NOT NULL UNIQUE, camera_id char(5), image blob, PRIMARY KEY (inserttimestamp));