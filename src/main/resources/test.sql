create table message_list
(
    id         int auto_increment comment '主键'
        primary key,
    user_id  int                             null comment '用户id',

    list_id   varchar(256)                            null comment '用户信息列表id,可能是好友或者群组',
    type      varchar(2)                            null comment '类型说明（01：好友，02：群组）',
    time      datetime               default CURRENT_TIMESTAMP not null comment '最新信息的维护时间',
    message   text                    null comment '最新信息',

    cst_create datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    create_by  int                                    null comment '创建人用户ID',
    cst_modify datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    update_by  int                                    null comment '更新人用户ID',
    remarks    varchar(256)                           null comment '备注',
    del_flag   char(2)      default '01'              null comment '删除标记位'
)
    comment '消息列表';

