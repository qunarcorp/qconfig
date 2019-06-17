# 使用说明

[TOC]

## 依赖以及配置

### 添加依赖

QConfig与其他TC组建相同，需要使用 TCPOM 的方式添加依赖 TCPOM使用方式如下 [TCPOM 使用指南](https://wiki.corp.qunar.com/confluence/pages/viewpage.action?pageId=114438300 "TC POM")

在完成TC POM添加后在需要使用QConfig的模块或者应用的POM中添加如下依赖即可

```xml
<dependency>
  <groupId>qunar.tc.qconfig</groupId>
  <artifactId>qconfig-client</artifactId>
</dependency>
```

### 获取Qunar-app.properties

1. 在应用中心 [管理页面](http://pbservice.corp.qunar.com/app_list.htm "应用中心地址") ，进入对应的应用管理地址，点击下载配置即可获得配置文件。
2. 将文件放入应用的resources根目录即可。文件没有环境限制，所有环境一致

### 与Spring集成

1. xml中添加qconfig namespace

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xmlns:context="http://www.springframework.org/schema/context"
          xmlns:qconfig="http://www.qunar.com/schema/qconfig"
          xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.qunar.com/schema/qconfig http://www.qunar.com/schema/qconfig.xsd
            http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
   ```

2. 在Spring配置文件中添加如下配置即可

   ```xml
   <context:annotation-config />
   <!-- 添加如下配置后即可使用注解热加载  -->
   <qconfig:annotation-driven />
   ```

## 使用指南

主要使用场景有两种 

1. 在XML中使用

   这种使用方式无法热加载，但是很有必要

2. 在代码中使用

### XML中使用

在配置文件中使用示例如下

```xml
<!-- 这里可以写多个文件，用逗号分隔 -->
<qconfig:config files="zookeeper.properties,mysql.properties"/>
 
<!-- 如果想写多个 qconfig:config，不是写在一行，请注意这里的ignore-unresolvable  -->
 <qconfig:config files="config.properties" ignore-unresolvable="true" />
 <qconfig:config files="configt.properties" ignore-unresolvable="true" />
 
<!-- 如果还需要引入spring原生的properties文件呢？ -->
<context:property-placeholder location="classpath:xxxx.properties" ignore-unresolvable="true"/>
 
<!-- 其他地方的使用方式与properties文件的方式一致 -->
<bean id="orderService" class="com.qunar.hotel.OrderService">
   <property name="zkAddress" value="${zkAddress}" />
</bean>
```

*注意：当配置文件不是写在一行，而使用了多个<qconfig:config files="filename" ignore-unresolvable="true" />来配置多个配置文件，或<qconfig:config files="config.properties" ignore-unresolvable="true" />标签与<context:property-placeholder location="classpath:xxxx.properties" ignore-unresolvable="true"/>标签混用时，都需要加上ignore-unresolvable="true"属性，否则会报unresolved异常。*

### 代码中使用

使用代码加载存在两种方式加载。

1. [使用注解加载](userWithApi.md)
2. [使用原生API加载](useWithAnnotation.md)
