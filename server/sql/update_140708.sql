alter table `config_used_log` add column `port` smallint unsigned not null default 0 comment 'client port' after `ip`,add unique key `uniq_source_group_id_data_id_profile_ip_port` (`source_group_id`,`source_data_id`,`source_profile`,`ip`,`port`),drop index `uniq_source_group_id_data_id_profile_ip`;

alter table `config_log` add column `port` smallint unsigned not null default 0 comment 'client port' after `ip`;

alter table `config_reference` add column `status` tinyint unsigned not null default 0 comment '状态,0：正常，1：删除' after `operator`;

alter table `config_used_log` add column `status` tinyint unsigned not null default 0 comment '状态,0：正常，1：删除' after `remarks`;