### QConfig启动

> 环境需求 tomcat >= 7.0 JDK >=1.8

1. 启动server。

   首先在数据库中导入admin模块下的sql/main.sql。推荐同时导入qconfig_data，导入qconfig_data后即可将默认的简单配置文件导入数据库中。

   在不存在已启动Server时，首先修改qconfig_test中mysql.properties修改数据库配置。然后直接使用Idea启动Tomcat的方法启动Server. Server将会使用在资源目录下qconfig_test中的配置文件。

   在已有Server在线且Server中存在QConfig配置文件的情况下，推荐使用如下虚拟机启动参数指定Server地址（将IP:PORT 替换为已有Server地址）

   ```
   -Dqconfig.server=IP:PORT -Dqconfig.server.host=IP:PORT
   ```

   在有使用稳定IP或域名的Server以后，可以直接修改common模块下资源目录中default.conf qconfig.default.serverlist的值为Server的域名或者IP:PORT

2. 启动Admin 

   启动方法与Server相似

3. 启动其他应用

   1. 创建应用

      访问 /webapp/page/index.html#/qconfig/appinfo

      输入应用代号和应用名称。

   2. 创建QConfig所需的配置文件

      新建一个名为app-info.properties的文件

      文件模版如下

      ```
      #QConfig中文件环境
      env=dev 
      #文件Token
      token=36b41c5ee5c793a0f01d5aaf8dc3b170
      #应用代号
      appCode=qconfig
      #应用部署端口
      port=8080
      ```

      文件中Token在第一步中的页面appCode下输入应用代号，点击获取Token即可获得

   3. 创建配置所需要的配置文件

   在启动使用QConfig的应用时，存在如下几种情况

   1. 没有已启动的QConfigServer

      在资源目录下新建qconfig_test/应用名 

      在目录下放入使用的配置文件即可启动

   2. 存在启动的Server而没有修改default.conf而编译出的QConfigClient Jar包

      参照启动server时指定的虚拟机参数启动即可。

   3. 存在启动的Server且使用了已经修改了默认Server地址的QConfigClient

      直接启动即可