drop table if exists `server`;
create table `server` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT comment '主键',
  `ip` int unsigned NOT NULL DEFAULT 0 comment 'server ip',
  `port` int unsigned NOT NULL DEFAULT 0 comment 'server port',
  `room` varchar(20) NOT NULL DEFAULT '' comment 'server机房',
  PRIMARY KEY (`id`),
  unique key `uniq_ip_port` (`ip`, `port`)
) engine=innodb default charset=utf8mb4 comment 'server信息表';