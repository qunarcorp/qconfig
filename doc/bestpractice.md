# 最佳实践
参照Demo模块中使用，对应的配置文件存放在Demo模块资源目录 qconfig_test中。

下面推荐一些常规用法。

- 开关

  对于开关而言，推荐在properties文件中配置。参照如下方式添加到初始化函数中使用。

  ```java
  @QMapConfig(value = "config.properties", key = "switch", defaultValue = "false")
  boolean someSwitch;
  ```
  
- 黑白名单

  参照如下方式添加到初始化函数中使用

  ```java
  @QMapConfig(value = "config.properties", key = "black", defaultValue = "")
  List<String> blackList;
  ```
  
  