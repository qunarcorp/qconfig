## 配置文件信息

### 配置文件

| 文件名                               | 文件作用                                  |
| ------------------------------------ | ----------------------------------------- |
| Config.properties                    | qconfig主配置文件                         |
| Custom_entrypoint_mapping.properties | 不同机房的Server入口的映射                |
| env-mapping.properties               | 不同名称的环境名称与dev，beta，prod的映射 |
| environment.properties               | 环境显示顺序与默认列表                    |
| forbidden.app                        | app黑名单                                 |
| mysql.properties                     | 数据库配置                                |
| property-conflict-whitelist          | properties文件检查白名单                  |
| Config_file_type.t                   | 文件类型信息与介绍                        |

### 部分配置内容

- config.properties

  | 配置项                                     | 意义                                |
  | ------------------------------------------ | ----------------------------------- |
  | notify**                                   | 通知Server配置变更或者推送的连接    |
  | **Log.showLength                           | admin中页面上限制的config数量       |
  | forbidDifferentGroupAccess                 | server是否允许跨应用访问            |
  | admins                                     | 管理员，用,分割                     |
  | push.server.max                            | server推送是单次最大数量            |
  | push.server.interval                       | server推送间隔                      |
  | push.server.directPushLimit                | 直接推送最大值                      |
  | admin.properties.conflict.check            | Admin是否开启properties key冲突校验 |
  | client.check.rate.limit**                  | admin限流参数                       |
  | table.batchOp.whitelist                    | admin table csv导入白名单           |
  | greyRelease.recover.taskNotOperatedTimeout | 灰度发布恢复时间                    |
  | client.check.rate.limit.interval.Second    | 获取consumer二次确认时间            |
  | qconfig.server.host                        | server地址，用,分隔                 |
  | server.test.rooms                          | dev/beta机房 名称                    |
  | server.test.iplist                         | dev/beta qconfigServer地址          |

  **这里的dev/beta并非指配置的dev/beta，当beta/dev环境的应用请求到QConfig时，会优先将导向到对应的dev/beta qconfig集群**