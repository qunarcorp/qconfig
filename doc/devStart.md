#### 本地启动

1. 启动server。

   首先在数据库中导入admin模块下的sql/main.sql。推荐同时导入qconfig_data，导入qconfig_data后即可将默认的简单配置文件导入数据库中。

   在不存在已启动Server时，首先修改qconfig_test中mysql.properties修改数据库配置。

   然后按照如下步骤在Idea中添加Tomcat进行启动。

   1. 添加启动项，点击 add Configuration，添加TomcatServer。

      ![doc/addConfig.png](/Users/deepdown/Dev_Env/QConfigOpenSource/qconfig/doc/image/addConfig.png)

   2. 在TomcatServer中Deployment 中添加Server。

      ![doc/addConfig.png](/Users/deepdown/Dev_Env/QConfigOpenSource/qconfig/doc/image/deploy.png)

   3. 在tomcat虚拟机参数中添加如下内容

      ![doc/addConfig.png](/Users/deepdown/Dev_Env/QConfigOpenSource/qconfig/doc/image/vmOptions.png)

      (请将ip和端口更换为以后的Server地址，以用于指定server，如果没有则可以忽视这部。)

   4. 点击启动，进行Server启动即可。

      如果没有进行上一步，启动过程中可能出现异常，但是不会影响正常启动。

   5. 访问对应启动url，如果出现erueka界面，即表示已正常启动。

    Server将会使用在资源目录下qconfig_test中的配置文件。

   在已有Server在线且Server中存在QConfig配置文件的情况下，推荐使用如下虚拟机启动参数指定Server地址（将IP:PORT 替换为已有Server地址）

   ```
   -Dqconfig.server=IP:PORT -Dqconfig.server.host=IP:PORT
   ```

   在有使用稳定IP或域名的Server以后，可以直接修改common模块下资源目录中default.conf qconfig.default.serverlist的值为Server的域名或者IP:PORT

2. 启动Admin 

> 启动后，默认登陆账户为 admin 密码为 123456

   启动方法与Server一致

3. 启动其他应用

   1. 创建应用。

      如是编写helloworld。可以跳过这步。

      访问 /webapp/page/index.html#/qconfig/appinfo(前端重构中)

      输入应用Code和应用名称。

   2. 创建QConfig所需的配置文件

      如是编写helloworld，推荐使用如下配置，然后直接使用b_qconfig_test进行测试即可。配置文件内容如下

      ```
      env=dev
      token=BZKuDgvHtwWPu56Ti4UQxL4Vb0GyYV4V+X4uHvw4HuVNFo/j1Cx+c8FrsW7en6b4zpnBKgvMHvJT/TzApZDRdGzsTN9zq1DBGOeYQjf2y628zAOqoWFZ767oF/2LH9ewJK/ij7Hxm2BtZTI9PhjDu+CQxRhJROcqtgZGS9da62k=
      appCode=b_qconfig_test
      port=8080
      ```

      

      新建一个名为app-info.properties的文件，格式参照如上。

      文件中Token在第一步中的页面appCode下输入应用代号，点击获取Token即可获得

   3. 创建配置所需要的配置文件

   在启动使用QConfig的应用时，存在如下几种情况

   1. 没有已启动的QConfigServer

      在资源目录下新建qconfig_test/应用名 

      在目录下放入使用的配置文件即可启动

   2. 存在启动的Server而没有使用已修改default.conf而QConfigClient Jar包

      参照启动server时指定的虚拟机参数启动即可。

   3. 存在启动的Server且使用了已经修改了默认Server地址的QConfigClient

      直接启动即可