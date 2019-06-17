# 开发

[TOC]

> 如何在开发环境启动请查看[如何启动](start.md)

## Client开发

### 应用认证替换

应用认证的主要功能是标示一个应用。对于应用认证方面，目前的QConfig默认使用了读取配置文件的方式来进行应用认证。但是也提供了插件化的方式来实现应用认证。

1. 新建一个maven工程，添加如下依赖。

   ```xml
   <dependency>
     <groupId>qunar.tc.qconfig</groupId>
     <artifactId>qconfig-client</artifactId>
     <version>{Version}</version>
     <optional>true</optional>
   </dependency>
   ```

2. 实现common模块中ServerManagement接口

   ServerMangement接口意义

   - getAppServerConfig

     获取当前运行应用的相关信息

   - healthCheck

     返回应用在线状态

   - appServerConfig中每个字段的含义

     | 名称          | 含义                                                         |
     | :------------ | :----------------------------------------------------------- |
     | name          | 应用名称，即appCode也就是group                               |
     | token         | 用于校验应用的Token，默认是group的加密密钥                   |
     | env           | 应用的环境 如环境为线上机器 则为dev                          |
     | subEnv        | 子环境，有些地方也命名为为buildGroup 如 dev:a 则subEnv为a，env为dev 默认为空字符串 |
     | ip            | 这台机器的IP                                                 |
     | appServerType | 机器类型 分别为 dev,beta,prod                                |
     | profile       | 环境和子环境通过冒号拼接 如 dev:a                            |
     | room          | 机房名称 默认为空字符串                                      |

     

3. 在resources目录下 新建META-INF/services文件，并新建qunar.tc.qconfig.common.application.ServerManagement文件，文件中存放实现类的全限定名。

4. 如果在实现ServerManagement时，修改了鉴权方式，请务必修改Server模块中TokenFilter，以修改鉴权方式。

   如：如果修改了Token内容货加解密算法等

5. 将完成的jar包放入lib目录或者添加到对应工程的依赖中即可。

如果不需要进行插件式的添加，只需要在QConfig Common模块中实现ServerManagement接口，然后按照上方步骤3的方式完成即可。

## Admin模块开发

由于配置中心提供了权限管理功能。但是并没有实现用户管理以及应用管理功能，所以定义了一些Interface用于接入。

### 实现用户登陆功能

当前的QConfig中，已经接入了SpringSecurity用于与现有的权限管理接入。且使用了默认的httpBasic登陆。

要实现自定义，请修改qunar.tc.qconfig.admin.web.security.SecurityConfiguration，即可通过SpringSecurity的自定义登陆，具体实现方式请参照SpringSecurity文档。

### 实现应用管理

QConfig中包含了非常非常简单的应用管理功能。仅支持新建应用以及Token获取功能。

为了便于接入已有的应用管理，我们提供了ApplicationInfoService接口 用于接入自定义应用管理。只需要根据需求替代QConfig Admin中ApplicationInfoService接口的实现即可实现自定义应用管理功能。

### 实现权限管理

QConfig中存在2套权限管理机制，

1. 应用的权限

   应用的角色分为2种，分布给developer和owner，owner拥有应用的完整权限，developer根据环境的不同拥有不同的权限。具体如下

   > 1、resources环境：权限之间属于互斥关系，编辑权限只能编辑，审核权限只能审核，发布权限只能发布。
   >
   > 2、prod环境：与resources环境规则一致。
   >
   > 3、beta环境：编辑权限仅能执行编辑操作，审核权限和发布权限都可执行编辑、审核和发布操作。
   >
   > 4、dev环境：拥有三种权限之一即可在dev环境进行编辑、审核、发布。
   >
   > 5、每个开发人员最多拥有两种权限。
   >
   > 6、无论开发还是owner，线上环境文件都不能由同一个人进行编辑、审核和发布，必须至少有两个人参与。
   >
   > 7、如果不对文件进行单独的权限管理，将继承“所有文件”的权限配置，单独的权限管理对且仅对prod环境生效。

2. QConfig内置的权限

   内置的权限管理主要用于补充应用的权限管理。

要替换权限功能需要替换QConfig中替换如下接口的实现 permissionService, userContextService。

- permissionService主要实现了QConfig内部的应用
- userContextService用于实现整个QConfig中的用户与权限管理。

### 邮件通知功能

在有配置的发布和权限的变更时，QConfig会发送邮件通知相关的负责人。

由于邮件服务的实现存在差异，我们定义了IAlarmService用于实现邮件业务的接入。

只需要替换当前QConfig server-common模块的空实现即可完成接入

## Server开发

- 客户端解析失败通知

  在客户端发生解析错误后，会对相关人员发送邮件进行通知。这里请替代MockMailParseErrorServiceImpl实现MailParseErrorService即可完成通知。



