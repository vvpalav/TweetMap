drop database tweetrecorder;
create database tweetrecorder;
use tweetrecorder;

create table tweetrecorder.tweets (
    id   				BIGINT     		primary key,
    username			varchar(30) 	not null,
    text				varchar(200),
    latitude    		DOUBLE     		not null,
    longitude   		DOUBLE     		not null,
    timestamp   		TIMESTAMP  		not null
);

select * from tweets;