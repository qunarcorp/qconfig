# 使用注解加载

[TOC]

*前提：所有注解的发现依赖Spring，所有只有在Spring的托管对象中才能生效*

*除了QConfig注解，其他注解需要版本 >= 1.6.0*

## QConfig注解

使用示例如下

```java
@QConfig("config2.properties")
private Properties config;

//支持Map<String,String>
@QConfig("config1.properties")
private Map<String, String> config;


//跨应用获取公开文件，这里指定应用名为otherapp
@QConfig("otherapp#config1.properties")
private Map<String, String> publicConfig;

//还支持String，config里的内容是config.properties里完整内容，业务可以自行解析
@QConfig("config3.properties")
private String config;

//对于Json文件存在以下特殊使用方式
// 每次配置变更，person也会重新加载
@QConfig("person.json")
private Person person;

//对于table文件即.t结尾的文件存在以下方式
@QConfig("testtable.t")
private QTable config;
```

*以上的所有方式支持将变量作为参数使用方法进行注入，每次配置更新时，方法会被调用*

```java
@QConfig("config2.properties")
private void test(String word) {
    //每次配置有更新时方法都会被调用
}
```

*在1.6.0以后注解还增加了日志级别配置，可以自动打印日志， 日志可选 off, low, mid, high四个级别，默认为low*

*off级别表示不打印日志；low级别表示每次有变更时都会打印一条日志；mid级别在变更时会打印获取到的新的内容；high级别不仅打印新的内容，还会把老的内容也打印出来*

使用方式如下

```java
// 日志级别设置为high，当有变更发生时，map原有的值和最新map的值都会打印在日志中，这里以map举例，实际使用qconfig注解的对象都会以toString方法打印出来
@QConfig(value = "test.properties", logLevel = QConfigLogLevel.high)
private Map<String, String> map1;
 
// 日志级别设置为mid，当有变更发生时，最新map的值都会打印在日志中
@QConfig(value = "test.properties", logLevel = QConfigLogLevel.mid)
private Map<String, String> map2;

// 日志级别设置为low，当有变更发生时，会打印一条发生变更的日志，默认为此级别
@QConfig(value = "test.properties", logLevel = QConfigLogLevel.low)
private Map<String, String> map3;

// 日志级别设置为off，当有变更发生时，不会打印日志
@QConfig(value = "test.properties", logLevel = QConfigLogLevel.off)
private Map<String, String> map4;
```

## QMapConfig注解

在字段和方法上使用时除了具有之前QConfig注解获取Map和Properties的功能，还增加了获取自定义对象的功能。

示例如下

```java
@Service
public class Test {
    @QMapConfig("test.properties")
    private Map<String, String> map;
     
    @QMapConfig("test.properties")
    private Properties p;
 
    //直接转换为对象
    @QMapConfig("test.properties")
    private Person person;
     
    //通过translator转化为对象
    @QMapConfig(value = "test.properties", translator = PersonTranslator.class)
    private Person person;
 
    @QMapConfig("test.properties")
    public void onChange(Map<String, String> map) { ... }
 
    @QMapConfig("test.properties")
    public void onChange(Properties p) { ... }
 
    @QMapConfig("test.properties")
    public void onChange(Person person) { ... }
    
    @QMapConfig(value = "test.properties", translator = PersonTranslator.class)
    public void onTranslatorChange(Person person) { ... }
}
 
public class Person {
    //使用map中key为"person.name"的value
    @QConfigField(key = "person.name")
    private String name;
 
    //使用map中key为age的value
    private int age;
 
    //使用AddressTranslator对map中key为"address的value进行转换
    @QConfigField(AddressTranslator.class)
    private Address address;
}
 
public class AddressTranslator extends QConfigTranslator<Address> {
    @Override
    public Address translate(String value) {
        int i = value.indexOf(":");
        return new Address(value.substring(0, i), value.substring(i + 1));
    }
}
 
public class PersonTranslator extends QConfigMapTranslator<Person> {
    @Override
    public Person translate(Map<String, String> map) {
        return new Person(String.valueOf(map.get("name")), Integer.parseInt(map.get("age")));
    }
}
 
public static void main(Stirng[] args) {
    TypedConfig<Person> config = TypedConfig.get("test.properties", Person.class);
    config.addListener(new Configuration.ConfigListener<Person>() {
        @Override
        public void onLoad(Person person) {
            // do
        }
    });
}
```

如上面代码所示，Test类中person字段是Person类型，对于自定义类型，qconfig会自动进行解析；这种方式的注解也在api中提供了类似功能如main函数中所示。

同时自定义类型也可以定义一个继承自QConfigMapTranslator<T>的解析类来进行解析，注意所有的QConfig的Translator都需要提供一个无参构造函数，并且应当是无状态的。

对于自定义类型中的字段，属于qconfig内置类型的（包括String, Enum，boolean，byte，char，short，int，long，float，double以及它们的包装类型），如name和age，qconfig会自动进行处理，并且提供了QConfigField注解进行辅助处理。

QConfigField字段有两个属性，一个是key，key存在时按照key从map中获取value，key不存在时按照字段名从map中获取相应value；还有一个是value属性，用于处理自定义类型或者做特殊处理，value属性只能填写继承自QConfigTranslator<T>的class。

- 只需要某个Key的内容

  当我们只需要文件中某个key的内容的时候，也可以使用QMapConfig注解，只需要填入key参数,并且可以填写defaultValue参数，作为key对应value为空时的默认值。*注意，key，defaultValue参数和translator参数冲突.*

  ```java
  @Service
  public class Test {
      @QMapConfig(value = "test.properties", key = "count")
      private int count;
   
      @QMapConfig(value = "test.properties", key = "test.name", defaultValue = "xiaoming"）
      private String name;
  }
  ```

- 在类上使用

  在类上面使用QMapConfig注解时，需要配合容器（比如spring）的注入机制.

  ```
  @Component
  @QMapConfig("person.properties")
  public class Person {
      private String name;
      private int age;
  }
   
  @Service
  public void TestService {
      @Resource
      private Person person;
  }
  ```

  解析方式和在字段和方法上使用时类似，同样支持QConfigField注解。

  注意，类注解方式使用反射实现，如果对象比如这里的Person对象有一定的约束条件，需要谨慎使用，详情见正确性问题。

- 日志

  与QConfig一致

## QTableConfig注解

QTableConfig只提供在字段和方法上使用的功能。除了QConfig注解提供的自动转换为QTable的功能外，还提供了转化为自定义对象以及list和map的功能。示例如下

```java
@Service
public class Test {
    @QTableConfig("table.t")
    private QTable qTable;
 
    @QTableConfig(value = "table.t", translator = MyTableTranslator.class)
    private MyTable myTable;
 
    //不考虑rowkey，rowkey对应的map直接转换为Person
    @QTableConfig("persons.t")
    private List<Person> personList;
 
    //rowkey做key，rowkey对应的map转换为Person
    @QTableConfig("persons.t")
    private Map<String, Person> personMap;
}
 
public class MyTableConfig extends QConfigTableTranslator<MyTable> {
    @Override
    public MyTable translate(QTable qTable) {
        return new MyTable(qTable);
    }
}
 
public static void main(String[] args) {
        TypedConfig.getList("persons.t", Person.class);
        TypedConfig.getMap("persons.t", Person.class);
}
```

QTableConfig注解也支持一个translator参数，可以将QTable转化为自定义的bean。同时还自动支持转化为List<Bean>和Map<String, Bean>形式，并且提供api形式如main函数中所示。

转化为list形式的时，table的rowkey不起作用，转化为map形式时，rowkey将作为map的key存在。Bean的解析和QMapConfig类似，同样也支持@QConfigField注解。

- 日志

  与QConfig一致

## DisableQConfig

对于不想要进行处理的字段，可以用DisableQConfig注解。比如如下面的代码注解后，address字段就不会进行处理。

```java
public class Person {
    String name;
 
    int age;
 
    @DisableQConfig
    String address;
}
```

## 其他注意

- string类型到list，set，map和基本类型数组的自动转换

  在QMapConfig和QTableConfig作用的部分，当满足qconfig的约定时，string类型可以自动转换为list，set或者map。

  - 约定

    字段为list或set或基本类型数组时，用英文逗号','分隔;

    字段为map时，用英文冒号":"分隔key value，用英文分号';'分隔key value对.

  - 示例

    qconfig会自动分析需要translator进行转换的地方，比如从String转换到Person的translator，可以写在Person的注解上，也可以写在List<Person>, Set<Person>和Map<String, List<Person>>的注解上。

    ```java
    @Service
    public class Test {
        //key为"admins"的value自动转换为List
        @QMapConfig(value = "test.properties", key = "admins")
        private List<String> adminList;
     
        //key为"admins"的value自动转换为Set
        @QMapConfig(value = "test.properties", key = "admins")
        private Set<String> adminSet;
     
        //key为"map"的值自动转换为Map，同时Map中Value参数自动转换为Integer
        @QMapConfig(value = "test.properties", key = "map", defaultValue = "a:1;b:2;c:3")
        private Map<String, Integer> map;
     
        //key为"maplist"的值自动转换为Map，同时Map中Value参数自动转换为List<Integer>
        @QMapConfig(value = "test.properties", key = "maplist", defaultValue = "a:1,2;b:2,4;c:3,6")
        private Map<String, List<Integer>> mapList;
     
        @QMapConfig("person.properties")
        private Person person;
    }
     
    public class Person {
        private String name;
     
        private List<Integer> counts;
     
        @QConfigField(key = "apps")
        private List<String> apps;
     
        //PersonListTranslator的返回值是List<Person>，对"persons1"的值按照使用PersonListTranslator解析
        @QConfigField(translator = PersonListTranslator.class)
        private List<Person> persons1;
     
        //PersonTranslator的返回值是Person，对"persons2"的值按照List解析，list中每个item使用PersonTranslator解析
        @QConfigField(translator = PersonTranslator.class)
        private List<Person> persons2;
         
        //PersonTranslator的返回值是Person，对"personMap"的值按照map解析，map中每个value按照List解析，list中每个item使用PersonTranslator解析
        @QConfigField(translator = PersonTranslator.class)
        private Map<String, List<Person>> personMap;
     
        private static class PersonListTranslator extends QConfigTranslator<List<Person>> {
            public List<Person> translate(String value) {
                List<Person> persons = parsePersons(value);
                return persons;
            }
        }
     
     
        private static class PersonTranslator extends QConfigTranslator<Person> {
            public Person translate(String value) {
                return new Person(value);
            }
        }
    }
    ```

- QConfig获取对象的更新策略

  qconfig获取对象的更新策略是每次获取一个全新的对象，而不是在原有的对象上更新（map和table使用了proxy的机制，会在原有的对象上更新。

  为什么要新建对象呢，有以下因素。要做同一对象的动态更新，并且对用户透明，可以使用proxy，继承，反射三种方式。

  - proxy和继承

    proxy和继承无法解决结构体的问题，对于像Person这样的结构体对象，使用person.name这样的方式时，proxy和继承方式都会失效。

  - 反射正确性问题