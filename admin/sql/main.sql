drop table api_groupid_permission_rel;
create table api_groupid_permission_rel
(
    id                  bigint auto_increment comment '自增主键'
        primary key,
    groupid_rel_id      bigint       default 0                    not null comment 'appid映射关系表id',
    permission_id       bigint       default 0                    not null comment '权限表id',
    datachange_lasttime timestamp(3) default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3) comment '更新时间'
)comment 'appid和permission对应关系';
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
)
    comment 'appid是否有操作其他appid的权限关系表';

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
)
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
)
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
)
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
)
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
)
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
)
    comment '配置信息' charset = utf8;

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
)
    comment '提交的配置信息表' charset = utf8;

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
)
    comment '提交的配置信息快照' charset = utf8;

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
)
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
)
    comment '访问日志' charset = utf8;

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
)
    comment '配置操作记录表' charset = utf8;

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
)
    comment '配置profile表' charset = utf8;

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
)
    comment '引用文件表' charset = utf8;

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
)
    comment '引用文件日志表' charset = utf8;

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
)
    comment '配置信息快照' charset = utf8;

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
)
    comment '当前使用的config表' charset = utf8;

create index idx_group_dataid_profile
    on config_used_log (group_id, data_id, profile);

drop table if exists default_template_config;
create table default_template_config
(
    id          bigint(11) unsigned auto_increment comment '主键'
        primary key,
    config      varchar(2000) default ''                not null comment '文件的默认配置',
    create_time timestamp     default CURRENT_TIMESTAMP not null comment '创建时间'
)
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
)
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
)
    comment '需要加密的key表' charset = utf8;

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
)
    comment '文件提交备注表' charset = utf8;

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
)
    comment '文件MD5映射表' charset = utf8;

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
)
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
)
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
)
    comment '文件权限表' charset = utf8;

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
)
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
)
    comment '配置推送历史' charset = utf8;

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
)
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
)
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
)
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
)
    comment '文件模版映射表' charset = utf8;

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
)
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
)
    comment '当前使用的config表' charset = utf8;

drop table if exists group_op_log;
create table group_op_log
(
    id             bigint unsigned auto_increment comment '主键'
        primary key,
    group_id       varchar(50)                         not null comment '组',
    operator       varchar(30)                         not null comment '操作人',
    remarks        varchar(500)                        not null comment '备注',
    operation_time timestamp default CURRENT_TIMESTAMP not null comment '操作时间'
)
    comment 'group相关操作日志表' charset = utf8;

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
)
    comment '应用表' charset = utf8;

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
)
    comment '用户应用表' charset = utf8;

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
)
    comment '权限表' charset = utf8;

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
)
    comment '所有properties文件相关的信息，用于搜索' charset = utf8;

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
)
    comment 'properties entry变更日志表' charset = utf8;

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
)
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
)
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
)
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
)
    comment '引用文件最后一次快照表' charset = utf8;

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
)
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
)
    comment '用户收藏表';

