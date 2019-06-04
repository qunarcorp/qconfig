create table `config` (
  `id` int(11) not null auto_increment comment '主键',
  `group_id` varchar(50) not null comment '组',
  `data_id` varchar(50) not null comment '数据id',
  `profile` varchar(10) not null comment '区分各环境的标识',
  `create_time` timestamp not null default '0000-00-00 00:00:00' comment '创建时间',
  `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
  `version` smallint unsigned not null default 1 comment '版本号',
  primary key (`id`) ,
  unique key `uniq_group_id_data_id_profile` (`group_id`,`data_id`,`profile`)
) engine=innodb default charset=utf8 comment '配置信息';

CREATE TABLE `config_snapshot` (
  `id` int(11) NOT NULL AUTO_INCREMENT comment '主键',
  `group_id` varchar(50) NOT NULL comment '组',
  `data_id` varchar(50) NOT NULL comment '数据Id',
  `profile` varchar(10) NOT NULL comment '区分各环境的标识',
  `version` smallint unsigned NOT NULL COMMENT '版本号',
  `checksum` char(32) NOT NULL comment '校验码',
  `content` text NOT NULL comment '配置',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
  PRIMARY KEY (`id`) ,
  unique key `uniq_group_id_data_id_profile_version` (`group_id`,`data_id`,`profile`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '配置信息快照';

CREATE TABLE `config_log` (
  `id` bigint NOT NULL AUTO_INCREMENT comment '主键',
  `group_id` varchar(50) NOT NULL comment '组',
  `data_id` varchar(50) NOT NULL comment '数据Id',
  `profile` varchar(10) NOT NULL comment '区分各环境的标识',
  `version` smallint unsigned NOT NULL COMMENT '版本号',
  `ip` int unsigned NOT NULL COMMENT 'client ip',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '访问日志';

-- 1.0.2

ALTER TABLE `config_log` ADD COLUMN `remarks` varchar(150) NOT NULL DEFAULT '' comment '备注' AFTER `ip`;

ALTER TABLE `config_log` ADD COLUMN `record_type` TINYINT NOT NULL DEFAULT 1 comment '记录的类型，1为客户端拉取成功，2为客户端拉取失败，3为客户端解析文件失败，4为客户端使用本地文件，5为使用远程文件' AFTER `ip`;

CREATE TABLE `config_used_log` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT comment '主键',
  `group_id` varchar(50) NOT NULL comment '被引用组',
  `data_id` varchar(50) NOT NULL comment '被引用数据Id',
  `profile` varchar(10) NOT NULL comment '被引用区分各环境的标识',
  `ip` int unsigned NOT NULL COMMENT 'client ip',
  `version` smallint unsigned NOT NULL COMMENT '版本号',
  `config_type` tinyint NOT NULL COMMENT '配置的类型，0为没有使用，1为使用远程文件，2为使用本地覆盖文件',
  `remarks` varchar(50) NOT NULL default '' comment '备注',
  `create_time` timestamp not null default '0000-00-00 00:00:00' comment '创建时间',
  `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_group_id_data_id_profile_ip` (`group_id`,`data_id`,`profile`,`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '当前使用的config表';

-- 1.0.2 end