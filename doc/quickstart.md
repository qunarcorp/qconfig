# 快速开始

1. 引入依赖

todo

   

2. 读取配置

   1. 配置Spring

      ```xml
       <context:annotation-config />
       
       <qconfig:annotation-driven />
      ```

   2. 使用注解读取配置

      ```java
      //这个类需要配置成Spring的bean
      public class YourClass{
       
        @QConfig("config2.properties")
        private Properties config;
       
        //以下config都是动态变化的，config.properties发生变更后，每次都去config获取值都是最新值
       
        //支持Map<String,String>
        @QConfig("config1.properties")
        private Map<String, String> config;
       
       
        //跨应用获取公开文件，这里指定应用名为otherapp
        @QConfig("otherapp#config1.properties")
        private Map<String, String> publicConfig;
       
        //还支持String，config里的内容是config.properties里完整内容，业务可以自行解析
        @QConfig("config3.properties")
        private String config;
      }
      ```