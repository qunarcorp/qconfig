 create table `reference_last_snapshot` (
   `id` int unsigned not null auto_increment comment '主键',
   `ref_group_id` varchar(50) not null comment '被引用组',
   `ref_data_id` varchar(50) not null comment '被引用文件名',
   `ref_profile` varchar(10) not null comment '被引用环境',
   `last_version` smallint unsigned not null comment '被引用的文件在被取消公共时的版本号',
   primary key (`id`),
   unique key `uniq_ref_group_id_data_id_profile`(`ref_group_id`,`ref_data_id`,`ref_profile`)
) engine=innodb default charset=utf8 comment '引用文件最后一次快照表';