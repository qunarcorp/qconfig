alter table `config_snapshot` add column `based_version` smallint unsigned NOT NULL DEFAULT 0 COMMENT '配置基于的版本号' after `profile`;

alter table `config_log` add column `based_version` int NOT NULL DEFAULT -1 COMMENT '配置基于的版本号' after `profile`;

alter table `config_snapshot` drop index `uniq_group_id_data_id_profile_version`;





alter table `config_snapshot` add unique key `uniq_group_id_data_id_profile_version` (`group_id`,`data_id`,`profile`,`version`);