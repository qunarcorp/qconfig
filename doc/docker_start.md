# Docker 快速部署

基于Docker的便利性，我们提供了Docker部署作为快速启动方便。（仅适用了解，入门）

### 1. Docker准备
 docker的安装请参照[官方文档](https://docs.docker.com/install/). 安装文档完成安装即可。
 
### 2. 下载文件
 查看仓库中scripts/docker目录 。
### 3. 启动
 在scripts/docker目录中执行 docker-compose up，期间会出现部分异常，属于正常现象。
 完成启动后，分布访问localhost:8080出现eureka界面即代表正常，
 然后访问localhost:8083，即可访问admin， 账户为admin, 密码123456
 同时数据库账户为root,密码为空，端口是宿主机3306端口。
### 4. 使用
 在应该需要使用的工程的资源目录下新建app-info.properties文件
 并在其中加入如下内容
```
env=dev
token=BZKuDgvHtwWPu56Ti4UQxL4Vb0GyYV4V+X4uHvw4HuVNFo/j1Cx+c8FrsW7en6b4zpnBKgvMHvJT/TzApZDRdGzsTN9zq1DBGOeYQjf2y628zAOqoWFZ767oF/2LH9ewJK/ij7Hxm2BtZTI9PhjDu+CQxRhJROcqtgZGS9da62k=
appCode=b_qconfig_test
port=8084
```
 然后按照[使用说明](howto.md),配置Spring所需的配置。
 最后在虚拟机参数中加入如下参数, 即可开始使用。
```
-Dqconfig.server=localhost:8080 -Dqconfig.server.host=localhost:8080 -Dqserver.http.urls=localhost:8080 -Dqserver.https.urls=localhost:8443
```