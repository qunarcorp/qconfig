# 监控

对于QConfig，我们在Server和admin中进行了关键部分的监控埋点。同时，还提供了插件式的监控接入，默认提供Prometheus客户端的接入，除此以外也可以很方便的定制。

## 使用Prometheus

QConfig中默认提供了Prometheus接入，接入方式如下。

将如下的jar包以及QConfig中qcofig-metrics-promethues放入admin和Server的lib目录下：

[prometheus-client](http://central.maven.org/maven2/io/prometheus/simpleclient/0.6.0/simpleclient-0.6.0.jar)

[prometheus-common](http://central.maven.org/maven2/io/prometheus/simpleclient_common/0.6.0/simpleclient_common-0.6.0.jar)

[prometheus-httpserver](http://central.maven.org/maven2/io/prometheus/simpleclient_httpserver/0.6.0/simpleclient_httpserver-0.6.0.jar)

[prometheus-graphite](http://central.maven.org/maven2/io/prometheus/simpleclient_graphite_bridge/0.6.0/simpleclient_graphite_bridge-0.6.0.jar)

默认情况下会在3333端口暴露监控指标。也可以添加qconfig.monitor.properties配置修改端口号。

```
monitor.port=3333
```

也提供了graphite方式接入，按照如下配置即可

```
monitor.type=graphite
graphite.host=<host>
graphite.port=<port>
```

##定制插件

QConfig使用SPI的机制提供接入，如果要接入第三方系统，可以按照如下步骤

1. 创建MAVEN，添加如下依赖

   ```
   <dependency>
     <groupId>qunar.tc.qconfig</groupId>
     <artifactId>qconfig-client</artifactId>
     <version>{version}</version>
     <optional>true</optional>
   </dependency>
   ```

2. 实现QConfigCounter, QConfigMeter, QConfigTimer, QConfigMetricsRegistry几个接口

   - QConfigCounter 计数，比如每分钟请求次数
   - QConfigMeter qps/tps 监控，比如发送的qps。
   - QConfigTimer 时长监控
   - QConfigMetricRegistry SPI入口类， 参照PrometheusQConfigMetricRegistry实现，这个类只会初始化一次，所以需要在构造函数中完成初始化工作。

3. 在resources下创建META-INF/services文件夹，在里面创建名为qunar.tc.qconfig.common.metrics.QConfigMetricRegistry的文本文件，文件内容即QConfigMetricRegistry实现类的全名(包括包名)。比如：qunar.tc.qconfig.metrics.prometheus.PrometheusQConfigMetricRegistry

### 指标意义

| 名词                     | 描述                 | tag   |
| ------------------------ | -------------------- | ----- |
| user_statistics          | 用户操作统计         | type  |
| applyTimer               | 保存计时             |       |
| publishTimer             | 发布计时             |       |
| configFile.notFound      | 找不到配置           | appId |
| configFile.notFound.disk | 文件缓存中找不到配置 | appId |
| configFile.notFound.db   | 数据库中找不到配置   | appId |



