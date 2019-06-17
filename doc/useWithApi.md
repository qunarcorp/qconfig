# 使用API获取

原生API的方式获取，根据不同的文件类型有不同的使用方式。

## Properties

示例如下	

MapConfig

```java
MapConfig config = MapConfig.get("config.properties");
//这个map是动态变化的
Map<String, String> map = config.asMap();
//过一段时间后，来check是否有变更
String zkAddress = map.get("zkAddress");

//也可以通过listener的方式来监听更新
MapConfig config = MapConfig.get("config.properties");
config.asMap();
config.addListener(new Configuration.ConfigListener<Map<String, String>>() {
  @Override //文件加载成功或者有新版本触发
  public void onLoad(Map<String, String> conf) {
     for (Map.Entry<String, String> entry : conf.entrySet()) {
         logger.info(entry.getKey() + "=>" + entry.getValue());
     }
  }
});
```

同时可以使用PropertiesChangeListener来对Key进行监听, PropertiesChangeListener只有在properties文件内容有变化时才触发。如果文件里面的property为空，那么添加listener的操作并不会触发调用。其定义与使用如下

```java
//定义
public interface PropertiesChangeListener {
    void onChange(PropertiesChange change);
}
 
public class PropertiesChange {
    public boolean isChange(String key);//判断与上一次相比，key对应的value是否有变化
    public Map<String, PropertyItem> getItems();//获取所有Property的信息，包括新增、删除、修改以及不变的
}
 
public class PropertyItem {
 
    public enum Type {
        Add, Delete, Modify, NoChange
    }
 
    public Type getType();
 
    public String getOldValue();
 
    public String getNewValue();
 
    //一些辅助方法，这里只列出了int，有boolean，int，long，float，double
    public Optional<Integer> getOldInt();
    public Optional<Integer> getNewInt();
 
    //解析成功返回结果，否则返回def
    public int getOldInt(int def);
    public int getNewInt(int def);
}
//使用
MapConfig config = MapConfig.get("config.properties");
config.asMap();
config.addPropertiesListener(new MapConfig.PropertiesChangeListener() {
  @Override //配置发生变更的时候会触发，没有变更不触发
  public void onChange(PropertiesChange change) {
     // ...
  }
});
```

## Json

> 需要版本大于1.3.6

使用示例如下 

```
JsonConfig<Person> config = JsonConfig.get("person.json", person.class);

Person person = person.current();

config.addListener(new Configuration.ConfigListener<Person>() {
  @Override
  public void onLoad(Person newPerson) {
    logger.info("new person: {}", newPerson);
  }
});
```

如果需要使用泛型，get函数支持传入ParameterizedClass对象用于描述带泛型的类（当然也支持不带的）

ParameterizedClass类有两个概念，一个是clazz，一个是parameter，clazz就是带泛型参数的具体类对象;parameter则是对泛型参数的描述，因为泛型参数也可能是泛型，所以parameter也是ParameterizedClass对象。

构造方法如下

```java
public static ParameterizedClass of(Class clazz);
 
public static ParameterizedClass of(Class clazz, Class... parameters);
 
public static ParameterizedClass of(Class clazz, ParameterizedClass... parameters);
 
public static ParameterizedClass of(Class clazz, Collection<ParameterizedClass> parameters);
 
public ParameterizedClass addParameter(Class parameter);
 
public ParameterizedClass addParameter(ParameterizedClass parameter);
```

比如要生成List<Integer>的描述，可以写

```java
JsonConfig.ParameterizedClass parameter = JsonConfig.ParameterizedClass.of(List.class, Integer.class);
 
JsonConfig.ParameterizedClass parameter = JsonConfig.ParameterizedClass.of(List.class).addParameter(Integer.class);
 
JsonConfig<List<Integer>> config = JsonConfig.get("list.json", parameter);

```

Map<String, Integer>的描述，可以写

```java
JsonConfig.ParameterizedClass parameter = JsonConfig.ParameterizedClass.of(Map.class, String.class, Integer.class);
 
JsonConfig.ParameterizedClass parameter = JsonConfig.ParameterizedClass.of(Map.class).addParameter(String.class).addParameter(String.class);
 
JsonConfig<Map<String, Integer>> config = JsonConfig.get("map.json", parameter);

```

更复杂一些的描述，比如我们想生成Map<String, Foo<Integer>>

```java
class Foo<T> { }
 
JsonConfig.ParameterizedClass stringDesc = JsonConfig.ParameterizedClass.of(String.class);
JsonConfig.ParameterizedClass fooDesc = JsonConfig.ParameterizedClass.of(Foo.class, Integer.class);
JsonConfig.ParameterizedClass parameter = JsonConfig.ParameterizedClass.of(Map.class, stringDesc, fooDesc);
 
JsonConfig.ParameterizedClass parameter = JsonConfig.ParameterizedClass.of(Map.class)
          .addParameter(String.class)
          .addParameter(JsonConfig.ParameterizedClass.of(Foo.class, Integer.class));
 
JsonConfig<Map<String, Foo<Integer>>> config = JsonConfig.get("complex.json", parameter);

```

## table文件

- 使用MapConfig

  使用MapConfig的流程和普通文件一致，也可以和spring进行集成。

  要通过MapConfig获取模版文件的值，MapConfig的key为模版的“行名” + “/” + “列名”，当行名为空的时候key为“列名”（没有斜杠），列名为空的时候key为“行名”（没有斜杠）。

  比如行名为“row”，列名为“column”，那么key就是“row/column”；行名为“row”，列名为空，key就是“row”；行名为空，列名为“column”，key就是“column”。

  **当以MapConfig形式注入，比如下图，testtable.t将以MapConfig方式被读取**

  **由于一个qconfig文件只能被一种方式读取，当以MapConfig方式读取后，就不能按照接下来所说的按TableConfig方式使用了**

- 使用TableConfig

  TableConfig在代码中使用时跟MapConfig类似，区别在与MapConfig使用asMap返回一个Map，TableConfig使用asTable返回一个QTable。

## 自定义文件

自定义文件格式需要自行解析，使用方法如下

```java
TypedConfig<MyConfig> config = TypedConfig.get("config.cus", new TypedConfig.Parser<MyConfig>(){
   @Override
   public MyConfig parse(String data) throws IOException{
       //自行解析配置
   }
});
 
//MyConfig是你自己定义的一个类
MyConfig config = config.current();
 
//或订阅变更
config.addListener(new ConfigListener<MyConfig>(){
  @Override //配置发生变更的时候会触发
  public void onLoad(MyConfig conf) {
     //
  }
});
```

