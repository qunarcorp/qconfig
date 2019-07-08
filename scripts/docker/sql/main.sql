SET NAMES 'utf8mb4';
create database if not exists qconfig default character set utf8mb4;
use qconfig;
drop table if exists api_groupid_permission_rel;
create table api_groupid_permission_rel
(
    id                  bigint auto_increment comment '自增主键'
        primary key,
    groupid_rel_id      bigint       default 0                    not null comment 'appid映射关系表id',
    permission_id       bigint       default 0                    not null comment '权限表id',
    datachange_lasttime timestamp(3) default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3) comment '更新时间'
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4 comment 'appid和permission对应关系';
create index ix_DataChange_LastTime
on api_groupid_permission_rel (datachange_lasttime);

drop table if exists api_groupid_rel;
create table api_groupid_rel
(
    id                  bigint auto_increment comment '主键'
        primary key,
    groupid             varchar(20)  default ''                   not null comment '应用',
    target_groupid      varchar(20)  default ''                   not null comment '目标appid',
    datachange_lasttime timestamp(3) default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3) comment '更新时间',
    token               varchar(128) default ''                   not null comment 'token'
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 comment 'appid是否有操作其他appid的权限关系表';

create index ix_DataChange_LastTime
    on api_groupid_rel (datachange_lasttime);

drop table if exists api_permission;
create table api_permission
(
    id                  bigint auto_increment comment '主键'
        primary key,
    url                 varchar(1024) default ''                   not null comment 'url',
    parentid            bigint        default 0                    not null comment '父节点id',
    method              varchar(32)   default ''                   not null comment 'http method',
    type                int           default 0                    not null comment '0目录，1权限',
    description         varchar(255)  default ''                   not null comment '描述',
    datachange_lasttime timestamp(3)  default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3) comment '更新时间'
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment 'api可配置权限表';

create index ix_DataChange_LastTime
    on api_permission (datachange_lasttime);

drop table if exists api_token;
create table api_token
(
    id                  bigint auto_increment comment '主键'
        primary key,
    token               varchar(32)  default ''                   not null comment 'token',
    group_id            varchar(20)                               null comment 'appid',
    owner               varchar(255) default ''                   not null comment '联系人',
    description         varchar(255) default ''                   not null comment '描述',
    datachange_lasttime timestamp(3) default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3) comment '更新时间'
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment 'token表';

create index ix_DataChange_LastTime
    on api_token (datachange_lasttime);


drop table if exists api_token_permission_rel;
create table api_token_permission_rel
(
    id                  bigint auto_increment comment '主键'
        primary key,
    token_id            bigint       default 0                    not null comment 'tokenid',
    permission_id       bigint       default 0                    not null comment 'permission id',
    datachange_lasttime timestamp(3) default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3) comment '更新时间'
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment 'toke和权限关系表';

create index ix_DataChange_LastTime
    on api_token_permission_rel (datachange_lasttime);


drop table if exists batch_push_task_mapping;
create table batch_push_task_mapping
(
    id           bigint(11) unsigned auto_increment comment '主键'
        primary key,
    group_id     varchar(50) default ''                not null comment '组',
    data_id      varchar(50) default ''                not null comment '数据id',
    profile      varchar(25) default ''                not null comment '区分各环境的标识',
    uuid         varchar(32) default ''                not null comment 'UUID,对应batch_push_task',
    lock_version int         default 0                 not null comment '乐观锁version',
    create_time  timestamp   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  timestamp   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '最后更新时间',
    constraint uniq_group_data_id_profile
        unique (group_id, data_id, profile)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '当前批量发布任务映射表';

create index idx_update_time
    on batch_push_task_mapping (update_time);

create index idx_uuid
    on batch_push_task_mapping (uuid);

drop table if exists batch_push_task;
create table batch_push_task_new
(
    id                 bigint(11) unsigned auto_increment comment '主键'
        primary key,
    uuid               varchar(32) default ''                not null comment '任务UUID',
    group_id           varchar(50) default ''                not null comment '组',
    profile            varchar(25) default ''                not null comment '区分各环境的标识',
    task_info          mediumtext                            null comment '任务信息json, 包含配置数据，批次机器列表等',
    status             tinyint     default 0                 not null comment '任务状态',
    finished_batch_num int         default -1                not null comment '已执行完的批次编号',
    last_push_time     timestamp   default CURRENT_TIMESTAMP not null comment '最后一次推送的时间',
    lock_version       int         default 0                 not null comment '乐观锁version',
    create_time        timestamp   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time        timestamp   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '最后更新时间',
    operator           varchar(30) default ''                not null comment '操作者',
    constraint uniq_uuid
        unique (uuid)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '批量发布任务表-新';

create index idx_group_profile
    on batch_push_task_new (group_id, profile);

drop table if exists config;
create table config
(
    id            int auto_increment comment '主键'
        primary key,
    group_id      varchar(50)                                        not null comment '组',
    data_id       varchar(50)                                        not null comment '数据id',
    profile       varchar(25)                                        not null comment '区分各环境的标识',
    create_time   timestamp            default '1970-01-01 08:00:01' not null comment '创建时间',
    update_time   timestamp            default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    version       smallint(5) unsigned default 1                     not null comment '版本号',
    public_status tinyint              default 0                     not null comment '文件类型：0为私有文件，1为公共文件, 2为已删除文件',
    constraint uniq_group_id_data_id_profile
        unique (group_id, data_id, profile)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '配置信息';

drop table if exists config_candidate;
create table config_candidate
(
    id            int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id      varchar(50)                                       not null comment '组',
    profile       varchar(25)                                       not null comment '区分各环境的标识',
    data_id       varchar(50)                                       not null comment '数据id',
    based_version smallint(5) unsigned                              not null comment '配置基于的版本号',
    edit_version  smallint(5) unsigned                              not null comment '编辑版本号',
    status        tinyint(1) unsigned default 1                     not null comment '状态：可编辑1，审核通过2，已发布3，拒绝4',
    create_time   timestamp           default '1970-01-01 08:00:01' not null comment '创建时间',
    update_time   timestamp           default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    constraint uniq_group_id_profile_data_id
        unique (group_id, profile, data_id)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '提交的配置信息表' ;

drop table if exists config_candidate_snapshot;
create table config_candidate_snapshot
(
    id            bigint unsigned auto_increment comment '主键'
        primary key,
    group_id      varchar(50)                                   not null comment '组',
    profile       varchar(25)                                   not null comment '区分各环境的标识',
    data_id       varchar(50)                                   not null comment '数据id',
    based_version smallint(5) unsigned                          not null comment '配置基于的版本号',
    edit_version  smallint(5) unsigned                          not null comment '编辑版本号',
    content       mediumtext                                    not null comment '配置内容',
    status        tinyint(1) unsigned default 1                 not null comment '状态：可编辑1，审核通过2，已发布3，拒绝4',
    operator      varchar(30)                                   not null comment '操作人',
    create_time   timestamp           default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uniq_group_id_profile_data_id_edit_version
        unique (group_id, profile, data_id, edit_version)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '提交的配置信息快照';

drop table if exists config_editor_settings;
create table config_editor_settings
(
    id                  bigint(11) unsigned auto_increment comment '主键'
        primary key,
    group_id            varchar(50) default ''                not null comment '应用名',
    data_id             varchar(50) default ''                not null comment '配置文件名',
    use_advanced_editor tinyint(1)  default 1                 not null comment '是否使用高级编辑器，默认开启',
    update_time         timestamp   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    constraint uniq_group_data
        unique (group_id, data_id)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件编辑页面的各项配置选项';

drop table if exists config_log;
create table config_log
(
    id            int auto_increment comment '主键'
        primary key,
    group_id      varchar(50)                                    not null comment '组',
    data_id       varchar(50)                                    not null comment '数据Id',
    profile       varchar(25)                                    not null comment '区分各环境的标识',
    based_version int                  default -1                not null comment '配置基于的版本号',
    version       smallint(5) unsigned                           not null comment '版本号',
    ip            int unsigned                                   not null comment 'client ip',
    port          smallint(5) unsigned default 0                 not null comment 'client port',
    record_type   tinyint              default 1                 not null comment '记录的类型，1为客户端拉取成功，2为客户端拉取失败，3为客户端解析文件失败，4为客户端使用本地文件，5为使用远程文件',
    remarks       varchar(150)         default ''                not null comment '备注',
    create_time   timestamp            default CURRENT_TIMESTAMP not null comment '创建时间'
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '访问日志' ;

create index idx_group_profile_data_based_version_version
    on config_log (group_id, profile, data_id, based_version, version);

drop table if exists config_op_log;
create table config_op_log
(
    id             bigint unsigned auto_increment comment '主键'
        primary key,
    group_id       varchar(50)                         not null comment '组',
    profile        varchar(25)                         not null comment '区分各环境的标识',
    data_id        varchar(50)                         not null comment '数据id',
    based_version  smallint(5) unsigned                not null comment '配置基于的版本号',
    edit_version   smallint(5) unsigned                not null comment '编辑版本号',
    operator       varchar(30)                         not null comment '操作人',
    operation_type tinyint(1)                          not null comment '操作类型',
    remarks        varchar(500)                        not null comment '备注',
    ip             int unsigned                        not null comment 'ip',
    operation_time timestamp default CURRENT_TIMESTAMP not null comment '操作时间'
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '配置操作记录表' ;

create index idx_group_data_profile_based_version_edit_version
    on config_op_log (group_id, data_id, profile, based_version, edit_version);

create index idx_operator_group_type
    on config_op_log (operator, group_id, operation_type);

drop table if exists config_profile;
create table config_profile
(
    id          int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50)                         not null comment '组',
    profile     varchar(25)                         not null comment '区分各环境的标识',
    operator    varchar(30)                         not null comment '操作人',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uniq_group_id_profile
        unique (group_id, profile)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '配置profile表' ;

drop table if exists config_reference;
create table config_reference
(
    id           int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id     varchar(50)                                not null comment '组',
    alias        varchar(50)                                not null comment '文件别名',
    profile      varchar(25)                                not null comment '区分各环境的标识',
    ref_group_id varchar(50)                                not null comment '被引用组',
    ref_data_id  varchar(50)                                not null comment '被引用文件名',
    ref_profile  varchar(25)                                not null comment '引用环境',
    operator     varchar(30)                                not null comment '操作人',
    status       tinyint unsigned default 0                 not null comment '状态,0：正常，1：删除',
    create_time  timestamp        default CURRENT_TIMESTAMP not null comment '创建时间',
    type         tinyint          default 0                 not null comment '0引用，1继承，后面可能会有其他类型',
    constraint uniq_group_id_profile_alias
        unique (group_id, profile, alias),
    constraint uniq_group_id_profile_ref_group_id_ref_data_id_ref_profile
        unique (group_id, profile, ref_group_id, ref_data_id, ref_profile)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '引用文件表' ;

create index idx_ref_group_id_data_id_profile
    on config_reference (ref_group_id, ref_data_id, ref_profile);

drop table if exists config_reference_log;
create table config_reference_log
(
    id             int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id       varchar(50)                         not null comment '组',
    alias          varchar(50)                         not null comment '文件别名',
    profile        varchar(25)                         not null comment '区分各环境的标识',
    ref_group_id   varchar(50)                         not null comment '引用组',
    ref_data_id    varchar(50)                         not null comment '引用文件名',
    ref_profile    varchar(25)                         not null comment '引用环境',
    operation_type tinyint   default 0                 not null comment '操作类型：0：增加引用，1：取消引用',
    operator       varchar(30)                         not null comment '操作人',
    create_time    timestamp default CURRENT_TIMESTAMP not null comment '创建时间'
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '引用文件日志表' ;

create index idx_ref_group_id_profile_data_id
    on config_reference_log (ref_group_id, ref_profile, ref_data_id);

drop table if exists config_snapshot;
create table config_snapshot
(
    id            int auto_increment comment '主键'
        primary key,
    group_id      varchar(50)                                    not null comment '组',
    data_id       varchar(50)                                    not null comment '数据Id',
    profile       varchar(25)                                    not null comment '区分各环境的标识',
    based_version smallint(5) unsigned default 0                 not null comment '配置基于的版本号',
    version       smallint(5) unsigned                           not null comment '版本号',
    checksum      char(32)                                       not null comment '校验码',
    content       mediumtext                                     not null comment '配置内容',
    create_time   timestamp            default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uniq_group_id_data_id_profile_version
        unique (group_id, data_id, profile, version)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '配置信息快照' ;

drop table if exists config_used_log;
create table config_used_log
(
    id               int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id         varchar(50)                                        not null comment '被引用组',
    data_id          varchar(50)                                        not null comment '被引用数据Id',
    profile          varchar(25)                                        not null comment '区分各环境的标识',
    ip               int unsigned                                       not null comment 'client ip',
    port             smallint(5) unsigned default 0                     not null comment 'client port',
    version          smallint(5) unsigned                               not null comment '版本号',
    config_type      tinyint                                            not null comment '配置的类型，0为没有使用，1为使用远程文件，2为使用本地覆盖文件',
    remarks          varchar(50)          default ''                    not null comment '备注',
    status           tinyint unsigned     default 0                     not null comment '状态,0：正常，1：删除',
    create_time      timestamp            default '1970-01-01 08:00:01' not null comment '创建时间',
    update_time      timestamp            default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    source_group_id  varchar(50)          default ''                    not null comment '来源组',
    source_data_id   varchar(50)          default ''                    not null comment '来源文件名',
    source_profile   varchar(50)          default ''                    not null comment '来源环境标识',
    consumer_profile varchar(50)          default ''                    not null comment 'consumer的具体profile',
    constraint uniq_source_group_id_data_id_ip_port
        unique (source_group_id, source_data_id, ip, port)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '当前使用的config表' ;

create index idx_group_dataid_profile
    on config_used_log (group_id, data_id, profile);

drop table if exists default_template_config;
create table default_template_config
(
    id          bigint(11) unsigned auto_increment comment '主键'
        primary key,
    config      varchar(2000) default ''                not null comment '文件的默认配置',
    create_time timestamp     default CURRENT_TIMESTAMP not null comment '创建时间'
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件模版默认配置表';


drop table if exists default_template_config_mapping;
create table default_template_config_mapping
(
    id        bigint(11) unsigned auto_increment comment '主键'
        primary key,
    group_id  varchar(50)         default '' not null comment '组',
    data_id   varchar(50)         default '' not null comment '数据id',
    profile   varchar(25)         default '' not null comment '区分各环境的标识',
    config_id bigint(11) unsigned default 0  not null comment '默认配置id',
    constraint uniq_group_id_data_id_profile
        unique (group_id, data_id, profile)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件模版默认配置映射表';

drop table if exists encrypt_key;
create table encrypt_key
(
    id          int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50)                                       not null comment '组',
    data_id     varchar(50)                                       not null comment '数据id',
    encrypt_key varchar(100)                                      not null comment '需要加密的key',
    operator    varchar(30)                                       not null comment '操作人',
    status      tinyint(1) unsigned default 1                     not null comment '状态：正常0，已删除1',
    create_time timestamp           default '1970-01-01 08:00:01' not null comment '创建时间',
    update_time timestamp           default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    constraint uniq_group_id_data_id_encrypt_key
        unique (group_id, data_id, encrypt_key)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '需要加密的key表' ;

drop table if exists file_comment;
create table file_comment
(
    id          bigint(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50)      default ''                not null comment '组',
    data_id     varchar(50)      default ''                not null comment '数据id',
    profile     varchar(25)      default ''                not null comment '区分各环境的标识',
    version     int(11) unsigned default 0                 not null comment '文件版本',
    comment     varchar(150)     default ''                not null comment '提交备注',
    create_time timestamp        default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uniq_group_dataid_profile_version
        unique (group_id, data_id, profile, version)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件提交备注表' ;

drop table if exists file_content_md5;
create table file_content_md5
(
    id          bigint(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50)      default ''                not null comment '组',
    data_id     varchar(50)      default ''                not null comment '数据id',
    profile     varchar(25)      default ''                not null comment '区分各环境的标识',
    version     int(11) unsigned default 0                 not null comment '配置版本号',
    md5         char(32)         default ''                not null comment '文件内容MD5',
    create_time timestamp        default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uniq_group_dataId_profile_version
        unique (group_id, data_id, profile, version)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件MD5映射表';

create index idx_md5
    on file_content_md5 (md5);

drop table if exists file_delete;
create table file_delete
(
    id       bigint(11) unsigned auto_increment comment '主键'
        primary key,
    group_id varchar(50)  default '' not null comment '组',
    data_id  varchar(50)  default '' not null comment '数据id',
    profile  varchar(25)  default '' not null comment '区分各环境的标识',
    ip       int unsigned default 0  not null comment 'server ip',
    constraint uniq_group_id_data_id_profile_ip
        unique (group_id, data_id, profile, ip)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件删除表，管理员删除的文件信息，需要server清理缓存';

create index idx_ip
    on file_delete (ip);

drop table if exists file_description;
create table file_description
(
    id          int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50)      default ''                    not null comment '组',
    data_id     varchar(50)      default ''                    not null comment '数据id',
    profile     varchar(25)                                    not null comment '区分各环境的标识',
    version     int(11) unsigned default 0                     not null comment '版本号',
    description varchar(150)     default ''                    not null comment '描述',
    create_time timestamp        default '1970-01-01 08:00:01' not null comment '创建时间',
    update_time timestamp        default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    constraint uniq_group_id_data_id_profile_version
        unique (group_id, data_id, profile, version)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件描述表';

drop table if exists file_permission;
create table file_permission
(
    id          int(11) unsigned auto_increment comment '主键'
        primary key,
    rtx_id      varchar(30)                         not null comment 'rtx id',
    group_id    varchar(50)                         not null comment '组',
    data_id     varchar(50)                         not null comment '数据id',
    permission  int                                 not null comment '权限',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uniq_rtx_group_data
        unique (rtx_id, group_id, data_id)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件权限表' ;

create index idx_group_data
    on file_permission (group_id, data_id);

drop table if exists file_public_status;
create table file_public_status
(
    id          int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50)  default ''                    not null comment '组',
    data_id     varchar(50)  default ''                    not null comment '数据id',
    create_time timestamp    default '1970-01-01 08:00:01' not null comment '创建时间',
    type        int unsigned default 1                     null comment '类型标记位',
    update_time timestamp    default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    constraint uniq_group_id_data_id
        unique (group_id, data_id)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '公开文件表';

create index type_index
    on file_public_status (type);

drop table if exists file_push_history;
create table file_push_history
(
    id          bigint(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50)          default ''                not null comment '组',
    data_id     varchar(50)          default ''                not null comment '数据id',
    profile     varchar(25)          default ''                not null comment '区分各环境的标识',
    version     int(11) unsigned     default 0                 not null comment '配置版本号',
    ip          int(11) unsigned     default 0                 not null comment 'IP',
    port        smallint(5) unsigned default 0                 not null comment '端口',
    md5         char(32)             default ''                not null comment '文件内容MD5',
    type        tinyint(1) unsigned  default 0                 not null comment '状态：灰度1，推送2，编辑后推送3',
    status      tinyint(1) unsigned  default 0                 not null comment '状态：完成1，取消2，推送中3， ',
    operator    varchar(30)          default ''                not null comment '操作者',
    create_time timestamp            default CURRENT_TIMESTAMP not null comment '创建时间'
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '配置推送历史' ;

create index idx_group_dataId_profile_version
    on file_push_history (group_id, data_id, profile, version);

drop table if exists file_template;
create table file_template
(
    id          int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50)          default ''                    not null comment '组',
    template    varchar(50)          default ''                    not null comment '模版名',
    detail      text                                               null comment '模版详情',
    operator    varchar(30)          default ''                    not null comment '操作人',
    version     smallint(5) unsigned default 1                     not null comment '编辑版本号',
    create_time timestamp            default '1970-01-01 08:00:01' not null comment '创建时间',
    update_time timestamp            default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    description varchar(150)         default ''                    not null comment '模版描述',
    type        tinyint unsigned     default 0                     not null comment '模版类型',
    constraint uniq_group_id_template
        unique (group_id, template)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件模版表';

drop table if exists file_template_mapping;
create table file_template_mapping
(
    id             int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id       varchar(50) default ''                    not null comment '组',
    data_id        varchar(50) default ''                    not null comment '数据id',
    template_group varchar(50) default ''                    not null comment '模版的组',
    template       varchar(50) default ''                    not null comment '模版名',
    create_time    timestamp   default '1970-01-01 08:00:01' not null comment '创建时间',
    update_time    timestamp   default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    constraint uniq_group_id_data_id
        unique (group_id, data_id)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件模版映射表';

drop table if exists file_template_snapshot;
create table file_template_snapshot
(
    id          int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50)          default ''                    not null comment '组',
    template    varchar(50)          default ''                    not null comment '模版名',
    detail      text                                               null comment '模版详情',
    operator    varchar(30)          default ''                    not null comment '操作人',
    version     smallint(5) unsigned default 1                     not null comment '编辑版本号',
    create_time timestamp            default '1970-01-01 08:00:01' not null comment '创建时间',
    update_time timestamp            default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    description varchar(150)         default ''                    not null comment '模版描述',
    type        tinyint unsigned     default 0                     not null comment '模版类型',
    constraint uniq_group_id_template_version
        unique (group_id, template, version)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件模版快照表';

drop table if exists file_template_version_mapping;
create table file_template_version_mapping
(
    id               bigint(11) unsigned auto_increment comment '主键'
        primary key,
    group_id         varchar(50)      default ''                not null comment '组',
    data_id          varchar(50)      default ''                not null comment '数据id',
    profile          varchar(25)      default ''                not null comment '区分各环境的标识',
    version          int(11) unsigned default 0                 not null comment '文件版本',
    template_group   varchar(50)      default ''                not null comment '模版的组',
    template         varchar(50)      default ''                not null comment '模版名',
    template_version int(11) unsigned default 0                 not null comment '模版版本',
    create_time      timestamp        default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      timestamp        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    constraint uniq_group_dataid_profile_version
        unique (group_id, data_id, profile, version)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件模版映射表' ;

create index idx_tgroup_template_tversion
    on file_template_version_mapping (template_group, template, template_version);

drop table if exists file_validate_url;
create table file_validate_url
(
    id          int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50)  default ''                    not null comment '组',
    profile     varchar(25)  default ''                    not null comment '区分各环境的标识',
    data_id     varchar(50)  default ''                    not null comment '数据id',
    url         varchar(255) default ''                    not null comment '校验url',
    operator    varchar(30)  default ''                    not null comment '操作人',
    create_time timestamp    default '1970-01-01 08:00:01' not null comment '创建时间',
    update_time timestamp    default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    constraint uniq_group_id_data_id_profile
        unique (group_id, data_id, profile)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件校验url表';


drop table if exists fixed_consumer_version;
create table fixed_consumer_version
(
    id          int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50)  default ''                    not null comment '被引用组',
    data_id     varchar(50)  default ''                    not null comment '被引用数据Id',
    profile     varchar(25)  default ''                    not null comment '区分各环境的标识',
    ip          int unsigned default 0                     not null comment 'client ip',
    version     int unsigned default 0                     not null comment '版本号',
    operator    varchar(30)  default ''                    not null comment '操作人',
    create_time timestamp    default '2017-01-01 00:00:00' not null comment '创建时间',
    update_time timestamp    default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    constraint uniq_group_id_data_id_profile_ip
        unique (group_id, data_id, profile, ip)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '当前使用的config表' ;

drop table if exists group_op_log;
create table group_op_log
(
    id             bigint unsigned auto_increment comment '主键'
        primary key,
    group_id       varchar(50)                         not null comment '组',
    operator       varchar(30)                         not null comment '操作人',
    remarks        varchar(500)                        not null comment '备注',
    operation_time timestamp default CURRENT_TIMESTAMP not null comment '操作时间'
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment 'group相关操作日志表' ;

create index idx_group_id_operation_time
    on group_op_log (group_id, operation_time);

drop table if exists pb_app;
create table pb_app
(
    id          int unsigned auto_increment
        primary key,
    code        varchar(50)                           not null comment '应用代号',
    name        varchar(50) default ''                not null comment '应用名称',
    group_code  varchar(50)                           not null comment '所属组编码',
    mail_group  varchar(100)                          not null comment '邮件组',
    status      tinyint                               not null comment '应用状态, 0=未审核，1=审核通过, 2=审核被拒绝, 3=已废弃',
    creator     varchar(50)                           not null comment '创建者',
    create_time timestamp   default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uniq_code
        unique (code)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '应用表' ;

create index idx_status
    on pb_app (status);

create index uniq_create_time
    on pb_app (create_time);

drop table if exists pb_user_app;
create table pb_user_app
(
    id          int unsigned auto_increment
        primary key,
    app_code    varchar(50)                         not null comment '应用代号',
    role_code   tinyint                             not null comment '角色',
    login_id    varchar(50)                         not null comment '用户标识',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uniq_app_role_user
        unique (app_code, role_code, login_id)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '用户应用表' ;

create index idx_app_code
    on pb_user_app (app_code);

create index idx_login_id
    on pb_user_app (login_id);

drop table if exists permission;
create table permission
(
    id          int(11) unsigned auto_increment comment '主键'
        primary key,
    rtx_id      varchar(30)                         not null comment 'rtx id',
    group_id    varchar(50)                         not null comment '组',
    permission  int                                 not null comment '权限',
    update_time timestamp default CURRENT_TIMESTAMP not null comment '更新时间',
    constraint uniq_rtx_group
        unique (rtx_id, group_id)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '权限表' ;

create index idx_group_id
    on permission (group_id);

drop table if exists properties_entries;
create table properties_entries
(
    id          int(11) unsigned auto_increment comment '主键'
        primary key,
    `key`       varchar(100) collate utf8_bin                     not null comment 'properties文件中的key',
    searchable  tinyint(1) unsigned default 1                     not null comment '是否能被搜索到，0：不能，1：能',
    group_id    varchar(50)                                       not null comment '组，即应用代号',
    profile     varchar(25)                                       not null,
    data_id     varchar(50)                                       not null comment '配置文件名称',
    value       mediumtext                                        not null,
    create_time timestamp           default '1970-01-01 08:00:01' not null comment '创建时间',
    update_time timestamp           default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    version     int                                               not null comment '当前properties的key所属config的版本',
    constraint uniq_key_group_id_profile_data_id
        unique (`key`, group_id, profile, data_id)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '所有properties文件相关的信息，用于搜索' ;

create index idx_group_id_profile_data_id_searchable
    on properties_entries (group_id, profile, data_id, searchable);

create index idx_searchable_update_time
    on properties_entries (searchable, update_time);

drop table if exists properties_entries_log;
create table properties_entries_log
(
    id           bigint(11) unsigned auto_increment comment '主键'
        primary key,
    group_id     varchar(50)      default ''                not null comment '组',
    data_id      varchar(50)      default ''                not null comment '数据id',
    profile      varchar(25)      default ''                not null comment '区分各环境的标识',
    entry_key    varchar(100)     default ''                not null comment 'properties entry key',
    version      int(11) unsigned default 0                 not null comment '当前版本号',
    last_version int(11) unsigned default 0                 not null comment '上次发布的版本号',
    value        mediumtext                                 null comment '当前版本entry value',
    `last_value` mediumtext                                 null comment '上次发布版本的entry value',
    comment      varchar(255)     default ''                not null comment '备注内容',
    type         tinyint          default 0                 not null comment '变更类型',
    create_time  timestamp        default CURRENT_TIMESTAMP not null comment '创建时间',
    operator     varchar(30)      default ''                not null comment '操作者',
    constraint uniq_group_dataId_profile_key_version
        unique (group_id, data_id, profile, entry_key, version)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment 'properties entry变更日志表' ;

drop table if exists properties_template;
create table properties_template
(
    id          int(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50) default ''                    not null comment '组',
    data_id     varchar(50) default ''                    not null comment '数据id',
    profile     varchar(25) default ''                    not null comment '区分各环境的标识',
    detail      text                                      null comment '模版详情',
    operator    varchar(30) default ''                    not null comment '操作人',
    create_time timestamp   default '1970-01-01 08:00:01' not null comment '创建时间',
    update_time timestamp   default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    constraint uniq_group_id_data_id_profile
        unique (group_id, data_id, profile)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment 'properties文件模版表';


drop table if exists publish_key_intercept_strategy;
create table publish_key_intercept_strategy
(
    id          bigint(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50)         default ''                    not null comment '组',
    strategy    tinyint(1) unsigned default 0                     not null comment '状态：不拦截0，beta有prod没有1，beta没有prod有2，不相同则拦截3',
    operator    varchar(30)         default ''                    not null comment '操作人',
    create_time timestamp           default '1970-01-01 08:00:01' not null comment '创建时间',
    update_time timestamp           default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '修改时间',
    constraint uniq_group_id
        unique (group_id)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '文件发布key拦截策略表';

drop table if exists push_config_version;
create table push_config_version
(
    id          bigint(11) unsigned auto_increment comment '主键'
        primary key,
    group_id    varchar(50)      default ''                not null comment '组',
    data_id     varchar(50)      default ''                not null comment '数据id',
    profile     varchar(25)      default ''                not null comment '区分各环境的标识',
    ip          int(11) unsigned default 0                 not null comment '版本号',
    version     int(11) unsigned default 0                 not null comment '版本号',
    create_time timestamp        default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '最后更新时间',
    constraint UNIQ_GROUP_DATA_PROFILE_IP
        unique (group_id, data_id, profile, ip)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '推送过的配置版本表';

drop table if exists reference_last_snapshot;
create table reference_last_snapshot
(
    id           int unsigned auto_increment comment '主键'
        primary key,
    ref_group_id varchar(50)          not null comment '被引用组',
    ref_data_id  varchar(50)          not null comment '被引用文件名',
    ref_profile  varchar(10)          not null comment '被引用环境',
    last_version smallint(5) unsigned not null comment '被引用的文件在被取消公共时的版本号',
    constraint uniq_ref_group_id_data_id_profile
        unique (ref_group_id, ref_data_id, ref_profile)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '引用文件最后一次快照表' ;

drop table if exists server;
create table server
(
    id   bigint(11) unsigned auto_increment comment '主键'
        primary key,
    ip   int unsigned default 0  not null comment 'server ip',
    port int unsigned default 0  not null comment 'server port',
    room varchar(20)  default '' not null comment 'server机房',
    constraint uniq_ip_port
        unique (ip, port)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment 'server信息表';

drop table if exists user_favorites;
create table user_favorites
(
    id          bigint(11) unsigned auto_increment comment '主键'
        primary key,
    user        varchar(30) default ''                not null comment '用户名',
    group_id    varchar(50) default ''                not null comment '组',
    data_id     varchar(50) default ''                not null comment '数据id',
    profile     varchar(25) default ''                not null comment '区分各环境的标识',
    type        tinyint     default 0                 not null comment '收藏类型',
    create_time timestamp   default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uniq_user_group_id_data_id_profile_type
        unique (user, group_id, data_id, profile, type)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
    comment '用户收藏表';
INSERT INTO qconfig.config (id, group_id, data_id, profile, create_time, update_time, version, public_status) VALUES (2711, ''qconfig'', ''config.properties'', ''dev:'', ''2019-05-31 18:07:47'', ''2019-05-31 19:45:21'', 3, 0);
INSERT INTO qconfig.config (id, group_id, data_id, profile, create_time, update_time, version, public_status) VALUES (2712, ''qconfig'', ''custom_entrypoint_mapping.properties'', ''dev:'', ''2019-05-31 18:08:48'', ''2019-05-31 19:45:21'', 3, 0);
INSERT INTO qconfig.config (id, group_id, data_id, profile, create_time, update_time, version, public_status) VALUES (2713, ''qconfig'', ''encrypt_key_blacklist.properties'', ''dev:'', ''2019-05-31 18:09:10'', ''2019-05-31 19:45:21'', 3, 0);
INSERT INTO qconfig.config (id, group_id, data_id, profile, create_time, update_time, version, public_status) VALUES (2714, ''qconfig'', ''env-mapping.properties'', ''dev:'', ''2019-05-31 18:09:47'', ''2019-05-31 19:45:21'', 3, 0);
INSERT INTO qconfig.config (id, group_id, data_id, profile, create_time, update_time, version, public_status) VALUES (2715, ''qconfig'', ''environment.properties'', ''dev:'', ''2019-05-31 18:10:05'', ''2019-05-31 19:45:21'', 3, 0);
INSERT INTO qconfig.config (id, group_id, data_id, profile, create_time, update_time, version, public_status) VALUES (2716, ''qconfig'', ''forbidden.app'', ''dev:'', ''2019-05-31 18:10:46'', ''2019-05-31 19:45:21'', 3, 0);
INSERT INTO qconfig.config (id, group_id, data_id, profile, create_time, update_time, version, public_status) VALUES (2717, ''qconfig'', ''limit-operate-apps'', ''dev:'', ''2019-05-31 18:11:16'', ''2019-05-31 19:45:21'', 3, 0);
INSERT INTO qconfig.config (id, group_id, data_id, profile, create_time, update_time, version, public_status) VALUES (2718, ''qconfig'', ''mysql.properties.properties'', ''dev:'', ''2019-05-31 18:16:44'', ''2019-05-31 19:45:21'', 3, 0);
INSERT INTO qconfig.config (id, group_id, data_id, profile, create_time, update_time, version, public_status) VALUES (2719, ''qconfig'', ''no-token-info'', ''dev:'', ''2019-05-31 18:16:52'', ''2019-05-31 19:45:21'', 3, 0);
INSERT INTO qconfig.config (id, group_id, data_id, profile, create_time, update_time, version, public_status) VALUES (2720, ''qconfig'', ''property-conflict-whitelist'', ''dev:'', ''2019-05-31 18:17:26'', ''2019-05-31 19:45:21'', 3, 0);
INSERT INTO qconfig.config (id, group_id, data_id, profile, create_time, update_time, version, public_status) VALUES (2721, ''qconfig'', ''qconfig-servers'', ''dev:'', ''2019-05-31 18:17:46'', ''2019-05-31 19:45:21'', 3, 0);
INSERT INTO qconfig.config (id, group_id, data_id, profile, create_time, update_time, version, public_status) VALUES (2722, ''qconfig'', ''qconfig_file_type.t'', ''dev:'', ''2019-05-31 19:52:34'', ''2019-05-31 19:52:34'', 3, 0);
INSERT INTO qconfig.config (id, group_id, data_id, profile, create_time, update_time, version, public_status) VALUES (2723, ''qconfig'', ''push_mail_switch.properties'', ''dev:'', ''2019-07-08 10:57:09'', ''2019-07-08 10:57:09'', 1, 0);
INSERT INTO qconfig.config_candidate (id, group_id, profile, data_id, based_version, edit_version, status, create_time, update_time) VALUES (1, ''qconfig'', ''dev:'', ''config.properties'', 0, 3, 3, ''2019-05-31 18:07:47'', ''2019-05-31 19:45:32'');
INSERT INTO qconfig.config_candidate (id, group_id, profile, data_id, based_version, edit_version, status, create_time, update_time) VALUES (2, ''qconfig'', ''dev:'', ''custom_entrypoint_mapping.properties'', 0, 3, 3, ''2019-05-31 18:08:48'', ''2019-05-31 19:45:32'');
INSERT INTO qconfig.config_candidate (id, group_id, profile, data_id, based_version, edit_version, status, create_time, update_time) VALUES (3, ''qconfig'', ''dev:'', ''encrypt_key_blacklist.properties'', 0, 3, 3, ''2019-05-31 18:09:10'', ''2019-05-31 19:45:32'');
INSERT INTO qconfig.config_candidate (id, group_id, profile, data_id, based_version, edit_version, status, create_time, update_time) VALUES (4, ''qconfig'', ''dev:'', ''env-mapping.properties'', 0, 3, 3, ''2019-05-31 18:09:47'', ''2019-05-31 19:45:32'');
INSERT INTO qconfig.config_candidate (id, group_id, profile, data_id, based_version, edit_version, status, create_time, update_time) VALUES (5, ''qconfig'', ''dev:'', ''environment.properties'', 0, 3, 3, ''2019-05-31 18:10:05'', ''2019-05-31 19:45:32'');
INSERT INTO qconfig.config_candidate (id, group_id, profile, data_id, based_version, edit_version, status, create_time, update_time) VALUES (6, ''qconfig'', ''dev:'', ''forbidden.app'', 0, 3, 3, ''2019-05-31 18:10:46'', ''2019-05-31 19:45:32'');
INSERT INTO qconfig.config_candidate (id, group_id, profile, data_id, based_version, edit_version, status, create_time, update_time) VALUES (7, ''qconfig'', ''dev:'', ''limit-operate-apps'', 0, 3, 3, ''2019-05-31 18:11:16'', ''2019-05-31 19:45:32'');
INSERT INTO qconfig.config_candidate (id, group_id, profile, data_id, based_version, edit_version, status, create_time, update_time) VALUES (8, ''qconfig'', ''dev:'', ''mysql.properties.properties'', 0, 3, 3, ''2019-05-31 18:16:44'', ''2019-05-31 19:45:32'');
INSERT INTO qconfig.config_candidate (id, group_id, profile, data_id, based_version, edit_version, status, create_time, update_time) VALUES (9, ''qconfig'', ''dev:'', ''no-token-info'', 0, 3, 3, ''2019-05-31 18:16:52'', ''2019-05-31 19:45:32'');
INSERT INTO qconfig.config_candidate (id, group_id, profile, data_id, based_version, edit_version, status, create_time, update_time) VALUES (10, ''qconfig'', ''dev:'', ''property-conflict-whitelist'', 0, 3, 3, ''2019-05-31 18:17:26'', ''2019-05-31 19:45:32'');
INSERT INTO qconfig.config_candidate (id, group_id, profile, data_id, based_version, edit_version, status, create_time, update_time) VALUES (11, ''qconfig'', ''dev:'', ''qconfig-servers'', 0, 3, 3, ''2019-05-31 18:17:46'', ''2019-05-31 19:45:32'');
INSERT INTO qconfig.config_candidate (id, group_id, profile, data_id, based_version, edit_version, status, create_time, update_time) VALUES (12, ''qconfig'', ''dev:'', ''qconfig_file_type.t'', 0, 3, 3, ''2019-05-31 19:52:34'', ''2019-05-31 19:52:34'');
INSERT INTO qconfig.config_candidate (id, group_id, profile, data_id, based_version, edit_version, status, create_time, update_time) VALUES (13, ''qconfig'', ''dev:'', ''push_mail_switch.properties'', 0, 1, 3, ''2019-07-08 10:57:09'', ''2019-07-08 10:57:09'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (1, ''qconfig'', ''dev:'', ''config.properties'', 0, 1, ''notify.url=notify
notifyPush.url=notifyPush
notifyIpPush.url=admin/notifyIpPush
notifyReference.url=notifyReference
notifyPublic.url=notifyPublic
configLog.showLength=50
permissionLog.showLength=50
clientLog.showLength=50
profileConfigLog.showLength=500
profileRefLog.showLength=100
search.resultPageSize=20
push.url=/push
file.suffix.allowed=.properties, .xml, .json
defaultRoom=default
forbidDifferentGroupAccess=true
admins=admin
push.app=qconfig
push.server.max=100
push.server.interval=10
push.server.directPushLimit=5
checkLevel=dev
server.mail.control=true
server.mail.distinct.cache.min=10
server.mail.distinct.cache.size=10000
server.mail.queue.size=10000
admin.properties.conflict.check=true
test=1
freshServerInfoIntervalMs=60000
pbservice.host=
pbservice.app.detail.url=/api/app/detail.json
pbservice.app.list.url=/api/app/list.json
pbservice.app.crewConfigPage.url=/app_detail_info.htm
alarm.send.logOnly=true
alarm.send.url=
wiki.url=
client.check.rate.limit.interval.Second=3
client.check.rate.limit.count=3
table.batchOp.whitelist=b_qconfig_test_cloud
#单位:秒
greyRelease.recover.taskNotOperatedTimeout=60
queryListeningClients.delayMillis=800
template.parseEnvironmentVariables.switch=true
#后续去掉
whiteListApps=
qconfig.server.host=127.0.0.1:8080
admin.multi-datasource.switch=false
new.listening.client.api.switch=true
ignore.public.check.appids=
server.need.doplugin.appids=
server.test.iplist=
server.test.rooms=
log.remarks.max=150
onebutton.publish.new=false
qunar.bastion.api.url='', 1, ''admin'', ''2019-05-31 18:07:47'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (2, ''qconfig'', ''dev:'', ''config.properties'', 0, 2, ''notify.url=notify
notifyPush.url=notifyPush
notifyIpPush.url=admin/notifyIpPush
notifyReference.url=notifyReference
notifyPublic.url=notifyPublic
configLog.showLength=50
permissionLog.showLength=50
clientLog.showLength=50
profileConfigLog.showLength=500
profileRefLog.showLength=100
search.resultPageSize=20
push.url=/push
file.suffix.allowed=.properties, .xml, .json
defaultRoom=default
forbidDifferentGroupAccess=true
admins=admin
push.app=qconfig
push.server.max=100
push.server.interval=10
push.server.directPushLimit=5
checkLevel=dev
server.mail.control=true
server.mail.distinct.cache.min=10
server.mail.distinct.cache.size=10000
server.mail.queue.size=10000
admin.properties.conflict.check=true
test=1
freshServerInfoIntervalMs=60000
pbservice.host=
pbservice.app.detail.url=/api/app/detail.json
pbservice.app.list.url=/api/app/list.json
pbservice.app.crewConfigPage.url=/app_detail_info.htm
alarm.send.logOnly=true
alarm.send.url=
wiki.url=
client.check.rate.limit.interval.Second=3
client.check.rate.limit.count=3
table.batchOp.whitelist=b_qconfig_test_cloud
#单位:秒
greyRelease.recover.taskNotOperatedTimeout=60
queryListeningClients.delayMillis=800
template.parseEnvironmentVariables.switch=true
#后续去掉
whiteListApps=
qconfig.server.host=127.0.0.1:8080
admin.multi-datasource.switch=false
new.listening.client.api.switch=true
ignore.public.check.appids=
server.need.doplugin.appids=
server.test.iplist=
server.test.rooms=
log.remarks.max=150
onebutton.publish.new=false
qunar.bastion.api.url='', 2, ''admin'', ''2019-05-31 18:07:47'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (3, ''qconfig'', ''dev:'', ''config.properties'', 0, 3, ''notify.url=notify
notifyPush.url=notifyPush
notifyIpPush.url=admin/notifyIpPush
notifyReference.url=notifyReference
notifyPublic.url=notifyPublic
configLog.showLength=50
permissionLog.showLength=50
clientLog.showLength=50
profileConfigLog.showLength=500
profileRefLog.showLength=100
search.resultPageSize=20
push.url=/push
file.suffix.allowed=.properties, .xml, .json
defaultRoom=default
forbidDifferentGroupAccess=true
admins=admin
push.app=qconfig
push.server.max=100
push.server.interval=10
push.server.directPushLimit=5
checkLevel=dev
server.mail.control=true
server.mail.distinct.cache.min=10
server.mail.distinct.cache.size=10000
server.mail.queue.size=10000
admin.properties.conflict.check=true
test=1
freshServerInfoIntervalMs=60000
pbservice.host=
pbservice.app.detail.url=/api/app/detail.json
pbservice.app.list.url=/api/app/list.json
pbservice.app.crewConfigPage.url=/app_detail_info.htm
alarm.send.logOnly=true
alarm.send.url=
wiki.url=
client.check.rate.limit.interval.Second=3
client.check.rate.limit.count=3
table.batchOp.whitelist=b_qconfig_test_cloud
#单位:秒
greyRelease.recover.taskNotOperatedTimeout=60
queryListeningClients.delayMillis=800
template.parseEnvironmentVariables.switch=true
#后续去掉
whiteListApps=
qconfig.server.host=127.0.0.1:8080
admin.multi-datasource.switch=false
new.listening.client.api.switch=true
ignore.public.check.appids=
server.need.doplugin.appids=
server.test.iplist=
server.test.rooms=
log.remarks.max=150
onebutton.publish.new=false
qunar.bastion.api.url='', 3, ''admin'', ''2019-05-31 18:07:47'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (4, ''qconfig'', ''dev:'', ''custom_entrypoint_mapping.properties'', 0, 1, '''', 1, ''admin'', ''2019-05-31 18:08:48'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (5, ''qconfig'', ''dev:'', ''custom_entrypoint_mapping.properties'', 0, 2, '''', 2, ''admin'', ''2019-05-31 18:08:48'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (6, ''qconfig'', ''dev:'', ''custom_entrypoint_mapping.properties'', 0, 3, '''', 3, ''admin'', ''2019-05-31 18:08:48'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (7, ''qconfig'', ''dev:'', ''encrypt_key_blacklist.properties'', 0, 1, '''', 1, ''admin'', ''2019-05-31 18:09:10'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (8, ''qconfig'', ''dev:'', ''encrypt_key_blacklist.properties'', 0, 2, '''', 2, ''admin'', ''2019-05-31 18:09:10'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (9, ''qconfig'', ''dev:'', ''encrypt_key_blacklist.properties'', 0, 3, '''', 3, ''admin'', ''2019-05-31 18:09:10'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (10, ''qconfig'', ''dev:'', ''env-mapping.properties'', 0, 1, ''dev=dev
beta=beta
prod=prod
resources=resources
_default='', 1, ''admin'', ''2019-05-31 18:09:47'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (11, ''qconfig'', ''dev:'', ''env-mapping.properties'', 0, 2, ''dev=dev
beta=beta
prod=prod
resources=resources
_default='', 2, ''admin'', ''2019-05-31 18:09:47'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (12, ''qconfig'', ''dev:'', ''env-mapping.properties'', 0, 3, ''dev=dev
beta=beta
prod=prod
resources=resources
_default='', 3, ''admin'', ''2019-05-31 18:09:47'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (13, ''qconfig'', ''dev:'', ''environment.properties'', 0, 1, ''#默认环境列表
defaultEnvs=resources,prod,beta,dev
#环境显示顺序
envOrders={"resources":0,"prod":1,"beta":2,"dev":3}'', 1, ''admin'', ''2019-05-31 18:10:05'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (14, ''qconfig'', ''dev:'', ''environment.properties'', 0, 2, ''#默认环境列表
defaultEnvs=resources,prod,beta,dev
#环境显示顺序
envOrders={"resources":0,"prod":1,"beta":2,"dev":3}'', 2, ''admin'', ''2019-05-31 18:10:05'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (15, ''qconfig'', ''dev:'', ''environment.properties'', 0, 3, ''#默认环境列表
defaultEnvs=resources,prod,beta,dev
#环境显示顺序
envOrders={"resources":0,"prod":1,"beta":2,"dev":3}'', 3, ''admin'', ''2019-05-31 18:10:05'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (16, ''qconfig'', ''dev:'', ''forbidden.app'', 0, 1, '''', 1, ''admin'', ''2019-05-31 18:10:46'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (17, ''qconfig'', ''dev:'', ''forbidden.app'', 0, 2, '''', 2, ''admin'', ''2019-05-31 18:10:46'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (18, ''qconfig'', ''dev:'', ''forbidden.app'', 0, 3, '''', 3, ''admin'', ''2019-05-31 18:10:46'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (19, ''qconfig'', ''dev:'', ''limit-operate-apps'', 0, 1, '''', 1, ''admin'', ''2019-05-31 18:11:16'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (20, ''qconfig'', ''dev:'', ''limit-operate-apps'', 0, 2, '''', 2, ''admin'', ''2019-05-31 18:11:16'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (21, ''qconfig'', ''dev:'', ''limit-operate-apps'', 0, 3, '''', 3, ''admin'', ''2019-05-31 18:11:16'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (22, ''qconfig'', ''dev:'', ''mysql.properties.properties'', 0, 1, ''jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://127.0.0.1:3306/qconfig_opensource_test?useUnicode=true&amp;characterEncoding=utf8
jdbc.username=admin
jdbc.password='', 1, ''admin'', ''2019-05-31 18:16:44'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (23, ''qconfig'', ''dev:'', ''mysql.properties.properties'', 0, 2, ''jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://127.0.0.1:3306/qconfig_opensource_test?useUnicode=true&amp;characterEncoding=utf8
jdbc.username=admin
jdbc.password='', 2, ''admin'', ''2019-05-31 18:16:44'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (24, ''qconfig'', ''dev:'', ''mysql.properties.properties'', 0, 3, ''jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://127.0.0.1:3306/qconfig_opensource_test?useUnicode=true&amp;characterEncoding=utf8
jdbc.username=admin
jdbc.password='', 3, ''admin'', ''2019-05-31 18:16:44'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (25, ''qconfig'', ''dev:'', ''no-token-info'', 0, 1, '''', 1, ''admin'', ''2019-05-31 18:16:52'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (26, ''qconfig'', ''dev:'', ''no-token-info'', 0, 2, '''', 2, ''admin'', ''2019-05-31 18:16:52'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (27, ''qconfig'', ''dev:'', ''no-token-info'', 0, 3, '''', 3, ''admin'', ''2019-05-31 18:16:52'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (28, ''qconfig'', ''dev:'', ''property-conflict-whitelist'', 0, 1, '''', 1, ''admin'', ''2019-05-31 18:17:26'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (29, ''qconfig'', ''dev:'', ''property-conflict-whitelist'', 0, 2, '''', 2, ''admin'', ''2019-05-31 18:17:26'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (30, ''qconfig'', ''dev:'', ''property-conflict-whitelist'', 0, 3, '''', 3, ''admin'', ''2019-05-31 18:17:26'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (31, ''qconfig'', ''dev:'', ''qconfig-servers'', 0, 1, '''', 1, ''admin'', ''2019-05-31 18:17:46'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (32, ''qconfig'', ''dev:'', ''qconfig-servers'', 0, 2, '''', 2, ''admin'', ''2019-05-31 18:17:46'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (33, ''qconfig'', ''dev:'', ''qconfig-servers'', 0, 3, '''', 3, ''admin'', ''2019-05-31 18:17:46'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (34, ''qconfig'', ''dev:'', ''qconfig_file_type.t'', 0, 1, ''[{"columns":{"suffix":"*.properties","description":"properties文件是key/value格式的配置文件，可使用java.util.Properties加载文件","icon":"/css/images/icon/properties.jpg"},"row":"properties文件"},{"columns":{"suffix":"*.json","description":"json是一种轻量级的数据交换格式，一个对象以“{”（左括号）开始，“}”（右括号）结束","icon":"/css/images/icon/json.jpg"},"row":"json文件"},{"columns":{"suffix":"*.yml","description":"YML文件格式是YAML (YAML Aint Markup Language)编写的文件格式，非常简洁和强大","icon":"/css/images/icon/yml.jpg"},"row":"yml文件"},{"columns":{"suffix":"*","description":"qconfig支持自定义文件格式，用户可以自定义解析器解析文件","icon":"/css/images/icon/other.jpg"},"row":"其他格式"},{"columns":{"suffix":"*","description":"qconfig模板是创建模板文件所必需的, 模板提供了模板文件的基本结构<br><a target=\\\\\\"_blank\\\\\\" style=\\\\\\"font-size: 15px\\\\\\" href=\\\\\\" \\\\\\">模版说明</a>","icon":"/css/images/icon/templateModal.jpg"},"row":"qconfig模板"}]'', 1, ''admin'', ''2019-05-31 19:52:34'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (35, ''qconfig'', ''dev:'', ''qconfig_file_type.t'', 0, 2, ''[{"columns":{"suffix":"*.properties","description":"properties文件是key/value格式的配置文件，可使用java.util.Properties加载文件","icon":"/css/images/icon/properties.jpg"},"row":"properties文件"},{"columns":{"suffix":"*.json","description":"json是一种轻量级的数据交换格式，一个对象以“{”（左括号）开始，“}”（右括号）结束","icon":"/css/images/icon/json.jpg"},"row":"json文件"},{"columns":{"suffix":"*.yml","description":"YML文件格式是YAML (YAML Aint Markup Language)编写的文件格式，非常简洁和强大","icon":"/css/images/icon/yml.jpg"},"row":"yml文件"},{"columns":{"suffix":"*","description":"qconfig支持自定义文件格式，用户可以自定义解析器解析文件","icon":"/css/images/icon/other.jpg"},"row":"其他格式"},{"columns":{"suffix":"*","description":"qconfig模板是创建模板文件所必需的, 模板提供了模板文件的基本结构<br><a target=\\\\\\"_blank\\\\\\" style=\\\\\\"font-size: 15px\\\\\\" href=\\\\\\" \\\\\\">模版说明</a>","icon":"/css/images/icon/templateModal.jpg"},"row":"qconfig模板"}]'', 2, ''admin'', ''2019-05-31 19:52:34'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (36, ''qconfig'', ''dev:'', ''qconfig_file_type.t'', 0, 3, ''[{"columns":{"suffix":"*.properties","description":"properties文件是key/value格式的配置文件，可使用java.util.Properties加载文件","icon":"/css/images/icon/properties.jpg"},"row":"properties文件"},{"columns":{"suffix":"*.json","description":"json是一种轻量级的数据交换格式，一个对象以“{”（左括号）开始，“}”（右括号）结束","icon":"/css/images/icon/json.jpg"},"row":"json文件"},{"columns":{"suffix":"*.yml","description":"YML文件格式是YAML (YAML Aint Markup Language)编写的文件格式，非常简洁和强大","icon":"/css/images/icon/yml.jpg"},"row":"yml文件"},{"columns":{"suffix":"*","description":"qconfig支持自定义文件格式，用户可以自定义解析器解析文件","icon":"/css/images/icon/other.jpg"},"row":"其他格式"},{"columns":{"suffix":"*","description":"qconfig模板是创建模板文件所必需的, 模板提供了模板文件的基本结构<br><a target=\\\\\\"_blank\\\\\\" style=\\\\\\"font-size: 15px\\\\\\" href=\\\\\\" \\\\\\">模版说明</a>","icon":"/css/images/icon/templateModal.jpg"},"row":"qconfig模板"}]'', 3, ''admin'', ''2019-05-31 19:52:34'');
INSERT INTO qconfig.config_candidate_snapshot (id, group_id, profile, data_id, based_version, edit_version, content, status, operator, create_time) VALUES (37, ''qconfig'', ''dev:'', ''push_mail_switch.properties'', 0, 1, '''', 3, ''admin'', ''2019-07-08 10:57:09'');
INSERT INTO qconfig.config_log (id, group_id, data_id, profile, based_version, version, ip, port, record_type, remarks, create_time) VALUES (1, ''qconfig'', ''env-mapping.properties'', ''dev:'', 0, 3, 173892217, 8080, 1, '''', ''2019-05-31 19:46:25'');
INSERT INTO qconfig.config_log (id, group_id, data_id, profile, based_version, version, ip, port, record_type, remarks, create_time) VALUES (2, ''qconfig'', ''env-mapping.properties'', ''dev:'', 0, 3, 173892217, 8080, 5, '''', ''2019-05-31 19:46:25'');
INSERT INTO qconfig.config_log (id, group_id, data_id, profile, based_version, version, ip, port, record_type, remarks, create_time) VALUES (3, ''qconfig'', ''push_mail_switch.properties'', ''dev:'', 0, 1, 1682969813, 8080, 1, '''', ''2019-07-08 10:57:38'');
INSERT INTO qconfig.config_log (id, group_id, data_id, profile, based_version, version, ip, port, record_type, remarks, create_time) VALUES (4, ''qconfig'', ''push_mail_switch.properties'', ''dev:'', 0, 1, 1682969813, 8080, 5, '''', ''2019-07-08 10:57:38'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (1, ''qconfig'', ''dev:'', ''config.properties'', 0, 1, ''admin'', 1, '''', 2130706433, ''2019-05-31 18:07:47'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (2, ''qconfig'', ''dev:'', ''config.properties'', 0, 2, ''admin'', 4, '''', 2130706433, ''2019-05-31 18:07:47'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (3, ''qconfig'', ''dev:'', ''config.properties'', 0, 3, ''admin'', 5, '''', 2130706433, ''2019-05-31 18:07:47'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (4, ''qconfig'', ''dev:'', ''custom_entrypoint_mapping.properties'', 0, 2, ''admin'', 4, '''', 2130706433, ''2019-05-31 18:08:48'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (5, ''qconfig'', ''dev:'', ''custom_entrypoint_mapping.properties'', 0, 1, ''admin'', 1, '''', 2130706433, ''2019-05-31 18:08:48'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (6, ''qconfig'', ''dev:'', ''custom_entrypoint_mapping.properties'', 0, 3, ''admin'', 5, '''', 2130706433, ''2019-05-31 18:08:48'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (7, ''qconfig'', ''dev:'', ''encrypt_key_blacklist.properties'', 0, 1, ''admin'', 1, '''', 2130706433, ''2019-05-31 18:09:10'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (8, ''qconfig'', ''dev:'', ''encrypt_key_blacklist.properties'', 0, 2, ''admin'', 4, '''', 2130706433, ''2019-05-31 18:09:10'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (9, ''qconfig'', ''dev:'', ''encrypt_key_blacklist.properties'', 0, 3, ''admin'', 5, '''', 2130706433, ''2019-05-31 18:09:10'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (10, ''qconfig'', ''dev:'', ''env-mapping.properties'', 0, 2, ''admin'', 4, '''', 2130706433, ''2019-05-31 18:09:47'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (11, ''qconfig'', ''dev:'', ''env-mapping.properties'', 0, 1, ''admin'', 1, '''', 2130706433, ''2019-05-31 18:09:47'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (12, ''qconfig'', ''dev:'', ''env-mapping.properties'', 0, 3, ''admin'', 5, '''', 2130706433, ''2019-05-31 18:09:47'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (13, ''qconfig'', ''dev:'', ''environment.properties'', 0, 2, ''admin'', 4, '''', 2130706433, ''2019-05-31 18:10:05'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (14, ''qconfig'', ''dev:'', ''environment.properties'', 0, 1, ''admin'', 1, '''', 2130706433, ''2019-05-31 18:10:05'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (15, ''qconfig'', ''dev:'', ''environment.properties'', 0, 3, ''admin'', 5, '''', 2130706433, ''2019-05-31 18:10:05'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (16, ''qconfig'', ''dev:'', ''forbidden.app'', 0, 2, ''admin'', 4, '''', 2130706433, ''2019-05-31 18:10:46'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (17, ''qconfig'', ''dev:'', ''forbidden.app'', 0, 1, ''admin'', 1, '''', 2130706433, ''2019-05-31 18:10:46'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (18, ''qconfig'', ''dev:'', ''forbidden.app'', 0, 3, ''admin'', 5, '''', 2130706433, ''2019-05-31 18:10:46'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (19, ''qconfig'', ''dev:'', ''limit-operate-apps'', 0, 1, ''admin'', 1, '''', 2130706433, ''2019-05-31 18:11:16'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (20, ''qconfig'', ''dev:'', ''limit-operate-apps'', 0, 2, ''admin'', 4, '''', 2130706433, ''2019-05-31 18:11:16'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (21, ''qconfig'', ''dev:'', ''limit-operate-apps'', 0, 3, ''admin'', 5, '''', 2130706433, ''2019-05-31 18:11:16'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (22, ''qconfig'', ''dev:'', ''mysql.properties.properties'', 0, 2, ''admin'', 4, '''', 2130706433, ''2019-05-31 18:16:44'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (23, ''qconfig'', ''dev:'', ''mysql.properties.properties'', 0, 1, ''admin'', 1, '''', 2130706433, ''2019-05-31 18:16:44'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (24, ''qconfig'', ''dev:'', ''mysql.properties.properties'', 0, 3, ''admin'', 5, '''', 2130706433, ''2019-05-31 18:16:44'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (25, ''qconfig'', ''dev:'', ''no-token-info'', 0, 2, ''admin'', 4, '''', 2130706433, ''2019-05-31 18:16:52'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (26, ''qconfig'', ''dev:'', ''no-token-info'', 0, 1, ''admin'', 1, '''', 2130706433, ''2019-05-31 18:16:52'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (27, ''qconfig'', ''dev:'', ''no-token-info'', 0, 3, ''admin'', 5, '''', 2130706433, ''2019-05-31 18:16:52'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (28, ''qconfig'', ''dev:'', ''property-conflict-whitelist'', 0, 2, ''admin'', 4, '''', 2130706433, ''2019-05-31 18:17:26'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (29, ''qconfig'', ''dev:'', ''property-conflict-whitelist'', 0, 1, ''admin'', 1, '''', 2130706433, ''2019-05-31 18:17:26'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (30, ''qconfig'', ''dev:'', ''property-conflict-whitelist'', 0, 3, ''admin'', 5, '''', 2130706433, ''2019-05-31 18:17:26'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (31, ''qconfig'', ''dev:'', ''qconfig-servers'', 0, 2, ''admin'', 4, '''', 2130706433, ''2019-05-31 18:17:46'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (32, ''qconfig'', ''dev:'', ''qconfig-servers'', 0, 1, ''admin'', 1, '''', 2130706433, ''2019-05-31 18:17:46'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (33, ''qconfig'', ''dev:'', ''qconfig-servers'', 0, 3, ''admin'', 5, '''', 2130706433, ''2019-05-31 18:17:46'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (34, ''qconfig'', ''dev:'', ''qconfig_file_type.t'', 0, 1, ''admin'', 1, '''', 2130706433, ''2019-05-31 19:52:34'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (35, ''qconfig'', ''dev:'', ''qconfig_file_type.t'', 0, 2, ''admin'', 4, '''', 2130706433, ''2019-05-31 19:52:34'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (36, ''qconfig'', ''dev:'', ''qconfig_file_type.t'', 0, 3, ''admin'', 5, '''', 2130706433, ''2019-05-31 19:52:34'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (37, ''qconfig'', ''dev:'', ''push_mail_switch.properties'', 0, 1, ''admin'', 1, '''', 2130706433, ''2019-07-08 10:57:09'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (38, ''qconfig'', ''dev:'', ''push_mail_switch.properties'', 0, 1, ''admin'', 4, '''', 2130706433, ''2019-07-08 10:57:09'');
INSERT INTO qconfig.config_op_log (id, group_id, profile, data_id, based_version, edit_version, operator, operation_type, remarks, ip, operation_time) VALUES (39, ''qconfig'', ''dev:'', ''push_mail_switch.properties'', 0, 1, ''admin'', 5, '''', 2130706433, ''2019-07-08 10:57:09'');
INSERT INTO qconfig.config_snapshot (id, group_id, data_id, profile, based_version, version, checksum, content, create_time) VALUES (1, ''qconfig'', ''config.properties'', ''dev:'', 0, 3, ''7645cc87ca188461ca341172b9e05ac3'', ''notify.url=notify
notifyPush.url=notifyPush
notifyIpPush.url=admin/notifyIpPush
notifyReference.url=notifyReference
notifyPublic.url=notifyPublic
configLog.showLength=50
permissionLog.showLength=50
clientLog.showLength=50
profileConfigLog.showLength=500
profileRefLog.showLength=100
search.resultPageSize=20
push.url=/push
file.suffix.allowed=.properties, .xml, .json
defaultRoom=default
forbidDifferentGroupAccess=true
admins=admin
push.app=qconfig
push.server.max=100
push.server.interval=10
push.server.directPushLimit=5
checkLevel=dev
server.mail.control=true
server.mail.distinct.cache.min=10
server.mail.distinct.cache.size=10000
server.mail.queue.size=10000
admin.properties.conflict.check=true
test=1
freshServerInfoIntervalMs=60000
pbservice.host=
pbservice.app.detail.url=/api/app/detail.json
pbservice.app.list.url=/api/app/list.json
pbservice.app.crewConfigPage.url=/app_detail_info.htm
alarm.send.logOnly=true
alarm.send.url=
wiki.url=
client.check.rate.limit.interval.Second=3
client.check.rate.limit.count=3
table.batchOp.whitelist=b_qconfig_test_cloud
#单位:秒
greyRelease.recover.taskNotOperatedTimeout=60
queryListeningClients.delayMillis=800
template.parseEnvironmentVariables.switch=true
#后续去掉
whiteListApps=
qconfig.server.host=127.0.0.1:8080
admin.multi-datasource.switch=false
new.listening.client.api.switch=true
ignore.public.check.appids=
server.need.doplugin.appids=
server.test.iplist=
server.test.rooms=
log.remarks.max=150
onebutton.publish.new=false
qunar.bastion.api.url='', ''2019-05-31 18:07:47'');
INSERT INTO qconfig.config_snapshot (id, group_id, data_id, profile, based_version, version, checksum, content, create_time) VALUES (2, ''qconfig'', ''custom_entrypoint_mapping.properties'', ''dev:'', 0, 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', '''', ''2019-05-31 18:08:48'');
INSERT INTO qconfig.config_snapshot (id, group_id, data_id, profile, based_version, version, checksum, content, create_time) VALUES (3, ''qconfig'', ''encrypt_key_blacklist.properties'', ''dev:'', 0, 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', '''', ''2019-05-31 18:09:10'');
INSERT INTO qconfig.config_snapshot (id, group_id, data_id, profile, based_version, version, checksum, content, create_time) VALUES (4, ''qconfig'', ''env-mapping.properties'', ''dev:'', 0, 3, ''18cace93a26f08a1b489f14f7b415139'', ''dev=dev
beta=beta
prod=prod
resources=resources
_default='', ''2019-05-31 18:09:47'');
INSERT INTO qconfig.config_snapshot (id, group_id, data_id, profile, based_version, version, checksum, content, create_time) VALUES (5, ''qconfig'', ''environment.properties'', ''dev:'', 0, 3, ''9cb21ac564dcd527773180575a37c92e'', ''#默认环境列表
defaultEnvs=resources,prod,beta,dev
#环境显示顺序
envOrders={"resources":0,"prod":1,"beta":2,"dev":3}'', ''2019-05-31 18:10:05'');
INSERT INTO qconfig.config_snapshot (id, group_id, data_id, profile, based_version, version, checksum, content, create_time) VALUES (6, ''qconfig'', ''forbidden.app'', ''dev:'', 0, 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', '''', ''2019-05-31 18:10:46'');
INSERT INTO qconfig.config_snapshot (id, group_id, data_id, profile, based_version, version, checksum, content, create_time) VALUES (7, ''qconfig'', ''limit-operate-apps'', ''dev:'', 0, 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', '''', ''2019-05-31 18:11:16'');
INSERT INTO qconfig.config_snapshot (id, group_id, data_id, profile, based_version, version, checksum, content, create_time) VALUES (8, ''qconfig'', ''mysql.properties.properties'', ''dev:'', 0, 3, ''327d175863d61a367d42d79f4c996c7e'', ''jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://127.0.0.1:3306/qconfig_opensource_test?useUnicode=true&amp;characterEncoding=utf8
jdbc.username=admin
jdbc.password='', ''2019-05-31 18:16:44'');
INSERT INTO qconfig.config_snapshot (id, group_id, data_id, profile, based_version, version, checksum, content, create_time) VALUES (9, ''qconfig'', ''no-token-info'', ''dev:'', 0, 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', '''', ''2019-05-31 18:16:52'');
INSERT INTO qconfig.config_snapshot (id, group_id, data_id, profile, based_version, version, checksum, content, create_time) VALUES (10, ''qconfig'', ''property-conflict-whitelist'', ''dev:'', 0, 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', '''', ''2019-05-31 18:17:26'');
INSERT INTO qconfig.config_snapshot (id, group_id, data_id, profile, based_version, version, checksum, content, create_time) VALUES (11, ''qconfig'', ''qconfig-servers'', ''dev:'', 0, 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', '''', ''2019-05-31 18:17:46'');
INSERT INTO qconfig.config_snapshot (id, group_id, data_id, profile, based_version, version, checksum, content, create_time) VALUES (12, ''qconfig'', ''qconfig_file_type.t'', ''dev:'', 0, 3, ''0d6d9436f46365a589c9b623c9728e17'', ''[{"columns":{"suffix":"*.properties","description":"properties文件是key/value格式的配置文件，可使用java.util.Properties加载文件","icon":"/css/images/icon/properties.jpg"},"row":"properties文件"},{"columns":{"suffix":"*.json","description":"json是一种轻量级的数据交换格式，一个对象以“{”（左括号）开始，“}”（右括号）结束","icon":"/css/images/icon/json.jpg"},"row":"json文件"},{"columns":{"suffix":"*.yml","description":"YML文件格式是YAML (YAML Aint Markup Language)编写的文件格式，非常简洁和强大","icon":"/css/images/icon/yml.jpg"},"row":"yml文件"},{"columns":{"suffix":"*","description":"qconfig支持自定义文件格式，用户可以自定义解析器解析文件","icon":"/css/images/icon/other.jpg"},"row":"其他格式"},{"columns":{"suffix":"*","description":"qconfig模板是创建模板文件所必需的, 模板提供了模板文件的基本结构<br><a target=\\\\\\"_blank\\\\\\" style=\\\\\\"font-size: 15px\\\\\\" href=\\\\\\" \\\\\\">模版说明</a>","icon":"/css/images/icon/templateModal.jpg"},"row":"qconfig模板"}]'', ''2019-05-31 19:52:34'');
INSERT INTO qconfig.config_snapshot (id, group_id, data_id, profile, based_version, version, checksum, content, create_time) VALUES (13, ''qconfig'', ''push_mail_switch.properties'', ''dev:'', 0, 1, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', '''', ''2019-07-08 10:57:09'');
INSERT INTO qconfig.config_used_log (id, group_id, data_id, profile, ip, port, version, config_type, remarks, status, create_time, update_time, source_group_id, source_data_id, source_profile, consumer_profile) VALUES (1, ''qconfig'', ''env-mapping.properties'', ''dev:'', 173892217, 8080, 3, 1, ''使用远程文件'', 0, ''2019-05-31 19:46:25'', ''2019-05-31 19:46:25'', ''qconfig'', ''env-mapping.properties'', ''dev:'', ''dev:'');
INSERT INTO qconfig.config_used_log (id, group_id, data_id, profile, ip, port, version, config_type, remarks, status, create_time, update_time, source_group_id, source_data_id, source_profile, consumer_profile) VALUES (2, ''qconfig'', ''push_mail_switch.properties'', ''dev:'', 1682969813, 8080, 1, 1, ''使用远程文件'', 0, ''2019-07-08 10:57:38'', ''2019-07-08 10:57:38'', ''qconfig'', ''push_mail_switch.properties'', ''dev:'', ''dev:'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (1, ''qconfig'', ''config.properties'', ''dev:'', 1, ''7645cc87ca188461ca341172b9e05ac3'', ''2019-05-31 18:07:47'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (2, ''qconfig'', ''config.properties'', ''dev:'', 2, ''7645cc87ca188461ca341172b9e05ac3'', ''2019-05-31 18:07:47'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (3, ''qconfig'', ''config.properties'', ''dev:'', 3, ''7645cc87ca188461ca341172b9e05ac3'', ''2019-05-31 18:07:47'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (4, ''qconfig'', ''custom_entrypoint_mapping.properties'', ''dev:'', 1, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:08:48'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (5, ''qconfig'', ''custom_entrypoint_mapping.properties'', ''dev:'', 2, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:08:48'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (6, ''qconfig'', ''custom_entrypoint_mapping.properties'', ''dev:'', 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:08:48'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (7, ''qconfig'', ''encrypt_key_blacklist.properties'', ''dev:'', 1, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:09:10'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (8, ''qconfig'', ''encrypt_key_blacklist.properties'', ''dev:'', 2, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:09:10'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (9, ''qconfig'', ''encrypt_key_blacklist.properties'', ''dev:'', 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:09:10'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (10, ''qconfig'', ''env-mapping.properties'', ''dev:'', 1, ''18cace93a26f08a1b489f14f7b415139'', ''2019-05-31 18:09:47'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (11, ''qconfig'', ''env-mapping.properties'', ''dev:'', 2, ''18cace93a26f08a1b489f14f7b415139'', ''2019-05-31 18:09:47'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (12, ''qconfig'', ''env-mapping.properties'', ''dev:'', 3, ''18cace93a26f08a1b489f14f7b415139'', ''2019-05-31 18:09:47'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (13, ''qconfig'', ''environment.properties'', ''dev:'', 1, ''9cb21ac564dcd527773180575a37c92e'', ''2019-05-31 18:10:05'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (14, ''qconfig'', ''environment.properties'', ''dev:'', 2, ''9cb21ac564dcd527773180575a37c92e'', ''2019-05-31 18:10:05'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (15, ''qconfig'', ''environment.properties'', ''dev:'', 3, ''9cb21ac564dcd527773180575a37c92e'', ''2019-05-31 18:10:05'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (16, ''qconfig'', ''forbidden.app'', ''dev:'', 1, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:10:46'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (17, ''qconfig'', ''forbidden.app'', ''dev:'', 2, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:10:46'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (18, ''qconfig'', ''forbidden.app'', ''dev:'', 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:10:46'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (19, ''qconfig'', ''limit-operate-apps'', ''dev:'', 1, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:11:16'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (20, ''qconfig'', ''limit-operate-apps'', ''dev:'', 2, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:11:16'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (21, ''qconfig'', ''limit-operate-apps'', ''dev:'', 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:11:16'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (22, ''qconfig'', ''mysql.properties.properties'', ''dev:'', 1, ''327d175863d61a367d42d79f4c996c7e'', ''2019-05-31 18:16:44'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (23, ''qconfig'', ''mysql.properties.properties'', ''dev:'', 2, ''327d175863d61a367d42d79f4c996c7e'', ''2019-05-31 18:16:44'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (24, ''qconfig'', ''mysql.properties.properties'', ''dev:'', 3, ''327d175863d61a367d42d79f4c996c7e'', ''2019-05-31 18:16:44'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (25, ''qconfig'', ''no-token-info'', ''dev:'', 1, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:16:52'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (26, ''qconfig'', ''no-token-info'', ''dev:'', 2, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:16:52'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (27, ''qconfig'', ''no-token-info'', ''dev:'', 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:16:52'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (28, ''qconfig'', ''property-conflict-whitelist'', ''dev:'', 1, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:17:26'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (29, ''qconfig'', ''property-conflict-whitelist'', ''dev:'', 2, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:17:26'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (30, ''qconfig'', ''property-conflict-whitelist'', ''dev:'', 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:17:26'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (31, ''qconfig'', ''qconfig-servers'', ''dev:'', 1, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:17:46'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (32, ''qconfig'', ''qconfig-servers'', ''dev:'', 2, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:17:46'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (33, ''qconfig'', ''qconfig-servers'', ''dev:'', 3, ''7e67fcaf3f6b180bae35bc5ed2bd6a10'', ''2019-05-31 18:17:46'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (34, ''qconfig'', ''qconfig_file_type.t'', ''dev:'', 1, ''0d6d9436f46365a589c9b623c9728e17'', ''2019-05-31 19:52:34'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (35, ''qconfig'', ''qconfig_file_type.t'', ''dev:'', 2, ''0d6d9436f46365a589c9b623c9728e17'', ''2019-05-31 19:52:34'');
INSERT INTO qconfig.file_content_md5 (id, group_id, data_id, profile, version, md5, create_time) VALUES (36, ''qconfig'', ''qconfig_file_type.t'', ''dev:'', 3, ''0d6d9436f46365a589c9b623c9728e17'', ''2019-05-31 19:52:34'');
INSERT INTO qconfig.file_template (id, group_id, template, detail, operator, version, create_time, update_time, description, type) VALUES (1, ''qconfig'', ''qconfig_file_type_template'', ''{"rows":{"values":[]},"columns":[{"name":"suffix","description":"","type":"text","default":"","isReadonly":false,"nullable":"true"},{"name":"description","description":"","type":"text","default":"","isReadonly":false,"nullable":"true"},{"name":"icon","description":"","type":"text","default":"","isReadonly":false,"nullable":"true"}],"useRowKey":true,"fileName":""}'', ''admin'', 2, ''2019-05-31 19:48:09'', ''2019-05-31 19:49:50'', '''', 0);
INSERT INTO qconfig.file_template_snapshot (id, group_id, template, detail, operator, version, create_time, update_time, description, type) VALUES (1, ''qconfig'', ''qconfig_file_type_template'', ''{"rows":{"values":[]},"columns":[{"name":"suffix","description":"","type":"text","default":"","isReadonly":false,"nullable":"true"},{"name":"description","description":"","type":"text","default":"","isReadonly":false,"nullable":"true"},{"name":"icon","description":"","type":"text","default":"","isReadonly":false,"nullable":"true"}],"useRowKey":false,"fileName":""}'', ''admin'', 1, ''2019-05-31 19:48:09'', ''2019-05-31 19:48:09'', '''', 0);
INSERT INTO qconfig.file_template_snapshot (id, group_id, template, detail, operator, version, create_time, update_time, description, type) VALUES (2, ''qconfig'', ''qconfig_file_type_template'', ''{"rows":{"values":[]},"columns":[{"name":"suffix","description":"","type":"text","default":"","isReadonly":false,"nullable":"true"},{"name":"description","description":"","type":"text","default":"","isReadonly":false,"nullable":"true"},{"name":"icon","description":"","type":"text","default":"","isReadonly":false,"nullable":"true"}],"useRowKey":true,"fileName":""}'', ''admin'', 2, ''2019-05-31 19:49:50'', ''2019-05-31 19:49:50'', '''', 0);
INSERT INTO qconfig.file_template_version_mapping (id, group_id, data_id, profile, version, template_group, template, template_version, create_time, update_time) VALUES (1, ''qconfig'', ''qconfig_file_type.t'', ''dev:'', 1, ''qconfig'', ''qconfig_file_type_template'', 2, ''2019-05-31 19:52:34'', ''2019-05-31 19:52:34'');
INSERT INTO qconfig.file_template_version_mapping (id, group_id, data_id, profile, version, template_group, template, template_version, create_time, update_time) VALUES (2, ''qconfig'', ''qconfig_file_type.t'', ''dev:'', 2, ''qconfig'', ''qconfig_file_type_template'', 2, ''2019-05-31 19:52:34'', ''2019-05-31 19:52:34'');
INSERT INTO qconfig.file_template_version_mapping (id, group_id, data_id, profile, version, template_group, template, template_version, create_time, update_time) VALUES (3, ''qconfig'', ''qconfig_file_type.t'', ''dev:'', 3, ''qconfig'', ''qconfig_file_type_template'', 2, ''2019-05-31 19:52:34'', ''2019-05-31 19:52:34'');
INSERT INTO qconfig.pb_app (id, code, name, group_code, mail_group, status, creator, create_time) VALUES (1, ''b_qconfig_test'', ''b_qconfig_test'', ''tcdev'', ''[" "]'', 1, '' admin'', ''2017-10-19 17:15:45'');
INSERT INTO qconfig.pb_app (id, code, name, group_code, mail_group, status, creator, create_time) VALUES (14363626, ''qconfig'', ''qconfig'', ''tcdev'', ''["test_2"]'', 2, '' admin'', ''2018-11-27 14:07:09'');
INSERT INTO qconfig.pb_app (id, code, name, group_code, mail_group, status, creator, create_time) VALUES (14363627, ''test_bb'', ''test_bb'', ''admin'', ''["test_2"]'', 2, '' admin'', ''2019-05-15 15:14:56'');
INSERT INTO qconfig.pb_app (id, code, name, group_code, mail_group, status, creator, create_time) VALUES (14363628, ''345'', ''345'', '''', ''null'', 1, '' admin'', ''2019-05-30 16:04:44'');
INSERT INTO qconfig.pb_user_app (id, app_code, role_code, login_id, create_time) VALUES (1, ''b_qconfig_test'', 2, ''admin'', ''2018-11-26 19:28:19'');
INSERT INTO qconfig.pb_user_app (id, app_code, role_code, login_id, create_time) VALUES (24, ''qconfig'', 2, ''admin'', ''2018-11-27 14:06:43'');
INSERT INTO qconfig.pb_user_app (id, app_code, role_code, login_id, create_time) VALUES (40, ''test_bb'', 3, ''admin'', ''2019-05-15 15:14:56'');
INSERT INTO qconfig.pb_user_app (id, app_code, role_code, login_id, create_time) VALUES (41, ''test_bb'', 3, ''test_a'', ''2019-05-15 15:14:56'');
INSERT INTO qconfig.pb_user_app (id, app_code, role_code, login_id, create_time) VALUES (42, ''test_bb'', 2, ''admin'', ''2019-05-15 15:14:56'');
INSERT INTO qconfig.pb_user_app (id, app_code, role_code, login_id, create_time) VALUES (43, ''test_bb'', 2, ''test_b'', ''2019-05-15 15:14:56'');
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (1, ''configLog.showLength'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''50'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (2, ''pbservice.app.detail.url'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''/api/app/detail.json'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (3, ''admin.properties.conflict.check'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''true'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (4, ''push.app'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''qconfig'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (5, ''template.parseEnvironmentVariables.switch'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''true'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (6, ''qunar.bastion.api.url'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', '''', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (7, ''log.remarks.max'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''150'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (8, ''push.server.max'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''100'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (9, ''queryListeningClients.delayMillis'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''800'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (10, ''notifyPublic.url'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''notifyPublic'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (11, ''file.suffix.allowed'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''.properties, .xml, .json'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (12, ''search.resultPageSize'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''20'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (13, ''test'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''1'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (14, ''onebutton.publish.new'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''false'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (15, ''table.batchOp.whitelist'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''b_qconfig_test_cloud'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (16, ''notifyPush.url'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''notifyPush'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (17, ''server.mail.queue.size'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''10000'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (18, ''alarm.send.logOnly'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''true'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (19, ''pbservice.app.list.url'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''/api/app/list.json'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (20, ''server.mail.distinct.cache.min'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''10'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (21, ''notify.url'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''notify'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (22, ''server.mail.control'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''true'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (23, ''wiki.url'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', '''', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (24, ''admins'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', '''', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (25, ''server.test.iplist'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', '''', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (26, ''push.server.interval'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''10'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (27, ''server.test.rooms'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', '''', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (28, ''defaultRoom'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''default'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (29, ''push.url'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''/push'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (30, ''clientLog.showLength'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''50'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (31, ''checkLevel'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''dev'', ''2019-05-31 18:07:47'', ''2019-05-31 18:07:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (32, ''whiteListApps'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', '''', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (33, ''alarm.send.url'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', '''', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (34, ''pbservice.app.crewConfigPage.url'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''/app_detail_info.htm'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (35, ''ignore.public.check.appids'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', '''', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (36, ''push.server.directPushLimit'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''5'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (37, ''pbservice.host'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', '''', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (38, ''admin.multi-datasource.switch'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''false'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (39, ''freshServerInfoIntervalMs'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''60000'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (40, ''profileConfigLog.showLength'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''500'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (41, ''client.check.rate.limit.count'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''3'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (42, ''server.mail.distinct.cache.size'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''10000'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (43, ''permissionLog.showLength'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''50'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (44, ''server.need.doplugin.appids'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', '''', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (45, ''profileRefLog.showLength'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''100'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (46, ''forbidDifferentGroupAccess'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''true'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (47, ''qconfig.server.host'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''127.0.0.1:8080'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (48, ''new.listening.client.api.switch'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''true'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (49, ''client.check.rate.limit.interval.Second'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''3'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (50, ''greyRelease.recover.taskNotOperatedTimeout'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''60'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (51, ''notifyReference.url'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''notifyReference'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (52, ''notifyIpPush.url'', 1, ''b_qconfig_test'', ''dev:'', ''config.properties'', ''admin/notifyIpPush'', ''2019-05-31 18:07:48'', ''2019-05-31 18:07:48'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (53, ''_default'', 1, ''b_qconfig_test'', ''dev:'', ''env-mapping.properties'', '''', ''2019-05-31 18:09:47'', ''2019-05-31 18:09:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (54, ''prod'', 1, ''b_qconfig_test'', ''dev:'', ''env-mapping.properties'', ''prod'', ''2019-05-31 18:09:47'', ''2019-05-31 18:09:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (55, ''dev'', 1, ''b_qconfig_test'', ''dev:'', ''env-mapping.properties'', ''dev'', ''2019-05-31 18:09:47'', ''2019-05-31 18:09:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (56, ''resources'', 1, ''b_qconfig_test'', ''dev:'', ''env-mapping.properties'', ''resources'', ''2019-05-31 18:09:47'', ''2019-05-31 18:09:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (57, ''beta'', 1, ''b_qconfig_test'', ''dev:'', ''env-mapping.properties'', ''beta'', ''2019-05-31 18:09:47'', ''2019-05-31 18:09:47'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (58, ''envOrders'', 1, ''b_qconfig_test'', ''dev:'', ''environment.properties'', ''{"resources":0,"prod":1,"beta":2,"dev":3}'', ''2019-05-31 18:10:05'', ''2019-05-31 18:10:05'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (59, ''defaultEnvs'', 1, ''b_qconfig_test'', ''dev:'', ''environment.properties'', ''resources,prod,beta,dev'', ''2019-05-31 18:10:05'', ''2019-05-31 18:10:05'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (60, ''jdbc.password'', 1, ''b_qconfig_test'', ''dev:'', ''mysql.properties.properties'', ''bj7PLUvvBh09gCEo'', ''2019-05-31 18:16:44'', ''2019-05-31 18:16:44'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (61, ''jdbc.username'', 1, ''b_qconfig_test'', ''dev:'', ''mysql.properties.properties'', ''admin'', ''2019-05-31 18:16:44'', ''2019-05-31 18:16:44'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (62, ''jdbc.driverClassName'', 1, ''b_qconfig_test'', ''dev:'', ''mysql.properties.properties'', ''com.mysql.jdbc.Driver'', ''2019-05-31 18:16:44'', ''2019-05-31 18:16:44'', 3);
INSERT INTO qconfig.properties_entries (id, `key`, searchable, group_id, profile, data_id, value, create_time, update_time, version) VALUES (63, ''jdbc.url'', 1, ''b_qconfig_test'', ''dev:'', ''mysql.properties.properties'', ''jdbc:mysql://127.0.0.1:3306/qconfig_opensource_test?useUnicode=true&amp;characterEncoding=utf8'', ''2019-05-31 18:16:44'', ''2019-05-31 18:16:44'', 3);
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (1, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''configLog.showLength'', 3, 0, ''50'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (2, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''pbservice.app.detail.url'', 3, 0, ''/api/app/detail.json'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (3, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''admin.properties.conflict.check'', 3, 0, ''true'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (4, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''push.app'', 3, 0, ''qconfig'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (5, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''template.parseEnvironmentVariables.switch'', 3, 0, ''true'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (6, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''qunar.bastion.api.url'', 3, 0, '''', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (7, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''log.remarks.max'', 3, 0, ''150'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (8, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''push.server.max'', 3, 0, ''100'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (9, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''queryListeningClients.delayMillis'', 3, 0, ''800'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (10, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''notifyPublic.url'', 3, 0, ''notifyPublic'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (11, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''file.suffix.allowed'', 3, 0, ''.properties, .xml, .json'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (12, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''search.resultPageSize'', 3, 0, ''20'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (13, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''test'', 3, 0, ''1'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (14, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''onebutton.publish.new'', 3, 0, ''false'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (15, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''table.batchOp.whitelist'', 3, 0, ''b_qconfig_test_cloud'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (16, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''notifyPush.url'', 3, 0, ''notifyPush'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (17, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''server.mail.queue.size'', 3, 0, ''10000'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (18, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''alarm.send.logOnly'', 3, 0, ''true'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (19, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''pbservice.app.list.url'', 3, 0, ''/api/app/list.json'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (20, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''server.mail.distinct.cache.min'', 3, 0, ''10'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (21, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''notify.url'', 3, 0, ''notify'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (22, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''server.mail.control'', 3, 0, ''true'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (23, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''wiki.url'', 3, 0, '''', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (24, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''admins'', 3, 0, '' '', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (25, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''server.test.iplist'', 3, 0, '''', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (26, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''push.server.interval'', 3, 0, ''10'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (27, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''server.test.rooms'', 3, 0, '''', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (28, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''defaultRoom'', 3, 0, ''default'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (29, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''push.url'', 3, 0, ''/push'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (30, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''clientLog.showLength'', 3, 0, ''50'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (31, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''checkLevel'', 3, 0, ''dev'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (32, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''whiteListApps'', 3, 0, '''', '''', ''后续去掉'', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (33, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''alarm.send.url'', 3, 0, '''', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (34, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''pbservice.app.crewConfigPage.url'', 3, 0, ''/app_detail_info.htm'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (35, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''ignore.public.check.appids'', 3, 0, '''', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (36, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''push.server.directPushLimit'', 3, 0, ''5'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (37, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''pbservice.host'', 3, 0, '''', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (38, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''admin.multi-datasource.switch'', 3, 0, ''false'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (39, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''freshServerInfoIntervalMs'', 3, 0, ''60000'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (40, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''profileConfigLog.showLength'', 3, 0, ''500'', '''', '''', 1, ''2019-05-31 18:07:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (41, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''client.check.rate.limit.count'', 3, 0, ''3'', '''', '''', 1, ''2019-05-31 18:07:48'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (42, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''server.mail.distinct.cache.size'', 3, 0, ''10000'', '''', '''', 1, ''2019-05-31 18:07:48'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (43, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''permissionLog.showLength'', 3, 0, ''50'', '''', '''', 1, ''2019-05-31 18:07:48'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (44, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''server.need.doplugin.appids'', 3, 0, '''', '''', '''', 1, ''2019-05-31 18:07:48'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (45, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''profileRefLog.showLength'', 3, 0, ''100'', '''', '''', 1, ''2019-05-31 18:07:48'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (46, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''forbidDifferentGroupAccess'', 3, 0, ''true'', '''', '''', 1, ''2019-05-31 18:07:48'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (47, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''qconfig.server.host'', 3, 0, ''127.0.0.1:8080'', '''', '''', 1, ''2019-05-31 18:07:48'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (48, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''new.listening.client.api.switch'', 3, 0, ''true'', '''', '''', 1, ''2019-05-31 18:07:48'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (49, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''client.check.rate.limit.interval.Second'', 3, 0, ''3'', '''', '''', 1, ''2019-05-31 18:07:48'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (50, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''greyRelease.recover.taskNotOperatedTimeout'', 3, 0, ''60'', '''', ''单位:秒'', 1, ''2019-05-31 18:07:48'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (51, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''notifyReference.url'', 3, 0, ''notifyReference'', '''', '''', 1, ''2019-05-31 18:07:48'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (52, ''b_qconfig_test'', ''config.properties'', ''dev:'', ''notifyIpPush.url'', 3, 0, ''admin/notifyIpPush'', '''', '''', 1, ''2019-05-31 18:07:48'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (53, ''b_qconfig_test'', ''env-mapping.properties'', ''dev:'', ''_default'', 3, 0, '''', '''', '''', 1, ''2019-05-31 18:09:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (54, ''b_qconfig_test'', ''env-mapping.properties'', ''dev:'', ''resources'', 3, 0, ''resources'', '''', '''', 1, ''2019-05-31 18:09:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (55, ''b_qconfig_test'', ''env-mapping.properties'', ''dev:'', ''prod'', 3, 0, ''prod'', '''', '''', 1, ''2019-05-31 18:09:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (56, ''b_qconfig_test'', ''env-mapping.properties'', ''dev:'', ''dev'', 3, 0, ''dev'', '''', '''', 1, ''2019-05-31 18:09:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (57, ''b_qconfig_test'', ''env-mapping.properties'', ''dev:'', ''beta'', 3, 0, ''beta'', '''', '''', 1, ''2019-05-31 18:09:47'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (58, ''b_qconfig_test'', ''environment.properties'', ''dev:'', ''envOrders'', 3, 0, ''{"resources":0,"prod":1,"beta":2,"dev":3}'', '''', ''环境显示顺序'', 1, ''2019-05-31 18:10:05'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (59, ''b_qconfig_test'', ''environment.properties'', ''dev:'', ''defaultEnvs'', 3, 0, ''resources,prod,beta,dev'', '''', ''默认环境列表'', 1, ''2019-05-31 18:10:05'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (60, ''b_qconfig_test'', ''mysql.properties.properties'', ''dev:'', ''jdbc.password'', 3, 0, ''bj7PLUvvBh09gCEo'', '''', '''', 1, ''2019-05-31 18:16:44'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (61, ''b_qconfig_test'', ''mysql.properties.properties'', ''dev:'', ''jdbc.driverClassName'', 3, 0, ''com.mysql.jdbc.Driver'', '''', '''', 1, ''2019-05-31 18:16:44'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (62, ''b_qconfig_test'', ''mysql.properties.properties'', ''dev:'', ''jdbc.url'', 3, 0, ''jdbc:mysql://127.0.0.1:3306/qconfig_opensource_test?useUnicode=true&amp;characterEncoding=utf8'', '''', '''', 1, ''2019-05-31 18:16:44'', ''admin'');
INSERT INTO qconfig.properties_entries_log (id, group_id, data_id, profile, entry_key, version, last_version, value, `last_value`, comment, type, create_time, operator) VALUES (63, ''b_qconfig_test'', ''mysql.properties.properties'', ''dev:'', ''jdbc.username'', 3, 0, ''admin'', '''', '''', 1, ''2019-05-31 18:16:44'', ''admin'');
