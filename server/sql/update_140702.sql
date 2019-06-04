alter table config add column `public_status` tinyint not null default 0 comment '文件类型：0为私有文件，1为公共文件, 2为已删除文件';

create table `config_reference` (
  `id` int(11) unsigned not null auto_increment comment '主键',
  `group_id` varchar(50) not null comment '组',
  `alias` varchar(50) not null comment '文件别名',
  `profile` varchar(10) not null comment '区分各环境的标识',
  `ref_group_id` varchar(50) not null comment '被引用组',
  `ref_data_id` varchar(50) not null comment '被引用文件名',
  `ref_profile` varchar(10) not null comment '被引用环境',
  `operator` varchar(30) NOT NULL comment '操作人',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
  primary key (`id`) ,
  unique key `uniq_group_id_profile_ref_group_id_ref_data_id_ref_profile` (`group_id`,`profile`,`ref_group_id`,`ref_data_id`,`ref_profile`),
  unique key `uniq_group_id_profile_alias` (`group_id`,`profile`,`alias`),
  index `idx_ref_group_id_data_id_profile` (`ref_group_id`,`ref_data_id`,`ref_profile`)
)engine=innodb default charset=utf8 comment '引用文件表';

alter table `config_used_log` add column `source_group_id` varchar(50) not null default '' comment '来源组',add column `source_data_id` varchar(50) not null default '' comment '来源文件名',add column `source_profile` varchar(50) not null default '' comment '来源环境标识';



update `config_used_log` set `source_group_id`=`group_id`,`source_data_id`=`data_id`,`source_profile`=`profile`,`update_time`=`update_time`;


alter table `config_used_log` add unique key `uniq_source_group_id_data_id_profile_ip` (`source_group_id`,`source_data_id`,`source_profile`,`ip`),drop index `uniq_group_id_data_id_profile_ip`;