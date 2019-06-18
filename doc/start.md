### QConfig启动

> 环境需求 
>
> tomcat >= 7.0 
>
> JDK >=1.8
>
> Mysql >= 5.6.5

#### 部署

QConfig的部署中，主要分为如下几步。

1. 导入数据库。导入克隆代码中admin模块下的sql/main.sql。推荐同时导入qconfig_data，导入qconfig_data后即可将QConfig配置文件以及示例文件和应用导入。
2. 进行Server和admin模块的部署工作，部署具体步骤见部署细节。首先部署Server，再部署Admin
3. 启动Client，获取配置。

#### 部署细节

1. 首先根据部署配置修改对应模块资源目录下app-info.properties配置文件中端口，然后修改资源目录下qconfig_test目录下qconfig/mysql.properties的参数用于配置数据库，如果已经有在线Server，请修改此目录下的qconfig.server.host。

2. 在项目根目录执行了mvn install 后，在对应模块目录下执行mvn package命令打包，在target目录下即可查看到对应的war包。

3. 如果存在启动完成的server，请在tomcat启动的虚拟机参数中添加如下参数用于指定QConfig_server.

   ```
   -Dqconfig.server=IP:PORT -Dqconfig.server.host=IP:PORT
   ```

4. 启动tomcat即可完成启动.

5. 在启动Server，且有稳定Server且导入了数据Sql后，可以通过Admin修改QConfig系统中QConfig的mysql.properties等环境等配置文件。同时启动时，通过虚拟机参数指定使用的QConfigServer。

6. 如果启动后的Server配置了稳定的域名或者IP，可以修改common模块中default.conf文件相关配置，然后再次编译client包，并使用这个包，就可以不用通过虚拟机参数指定Server而直接使用jar包中默认参数。