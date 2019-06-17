# API

### SDK

SDK api只能操作本应用

- 上传方法

  通过ConfigUploader.getInstance获取一个uploader对象

  通过uploader的方法上传。

  1. **Optional<Snapshot<String>> getCurrent(String dataId)**

     该函数用来获取名为dataId的文件的当前快照信息，获取的规则与应用使用qconfig拉取的规则相同，如果不存在则返回Optional.absent()；

     Snapshot<String>.getVersion()可以获取版本信息；Snapshot<String>.getContent()可以获取版本内容。

     getCurrent接口与系统本身正在使用qconfig-client自动获取的配置不一定相同（系统使用的文件版本更新可能存在延迟，getCurrent保证为最新的）。

     getCurrent接口应该与下面的接下来描述的uploadAtVersion接口配合使用。

  2. **UploadResult uploadAtVersion(VersionProfile version, String dataId, String data)**

     该函数用于将dataId为文件名，版本为version的数据data上传到qconfig，qconfig会判断根据qconfig文件拉取规则判断当前机器可以获取到的dataId文件的最新版本，如果和version相同，那么上传成功。

     如果要新建文件，传入version应为VersionProfile.ABSENT；新文件将创建在本机器所在环境（dev，beta，prod）对应的buildGroup中。

     UploadResult.getCode()返回上传结果的code，0为成功，具体code见UploadReturnCode类（新版本参考ApiResonseCode类）;

     UploadResult.getMessage()返回上传结果的描述。

     uploadAtVersion接口应该与前面描述的getCurrent接口配合使用。

  3. **UploadResult upload(String dataId, String data) throws Exception**

     根据qconfig在机器上缓存的版本信息作为version调用uploadAtVersion，version为本机的当前应用最近一次使用（指通过spring配置或者Mapconfig.get以及TypeConfig.get）dataId文件的远程版本，如果不存在，则相当于新建文件。

     如果远程版本与当前使用的版本不一致，那么上传会失败。

### HTTP

* 获取文件

  - url  

    /restapi/configs

  - 请求类型

    GET

  - 参数

    | Key           |                          |
    | :------------ | ------------------------ |
    | Subenv        | 子环境，如果传入空字符串 |
    | Groupie       | 本应用的appCode          |
    | env           | 环境 /prod/beta/dev      |
    | token         | 联系管理员申请           |
    | targetgroupid | 配置所在的应用appcode    |
    | dataid        | 配置文件名               |

  - 返回

    ```
    {
        "status": 0,
        "message": "正常",
        "data": { 
       			"group": "qconfig-test-2",
            "dataId": "test.properties",
            "profile": "beta:a",
            "basedVersion": 24,
            "editVersion": 27,
            "data": "key3=value\n",
            "operator": "client",
            "status": "PUBLISH",
            "application": null,
            "updateTime": null
       }
    }
    ```

    status:0请求正常, 9配置不存在

* 上传/更新文件

  - 说明

    common-core包提供了获取服务器环境和子环境的方法：

    ```
    ServerManager.getInstance()
    .getAppConfig().getServer()
    .getEnv()/getSubEnv()
    ```

    version字段传入配置当前的版本号，用来避免并发修改冲突。版本号可通过前述获取配置接口获得，取返回值中的editVersion传入即可(不需要手动+1)

  - url

    /restapi/configs

  - 请求类型

    POST

  - 参数

    - header

       Content-Type: application/json

    - Query param

      token 请联系管理员申请

    - request body

      ```
      {
          "requesttime": "2018-01-25 10:52:00", //请求时间
          "operator": "zhangsan", //操作人
          "config": {
              "groupid": "test_app", //本应用appcode
              "targetgroupid": "qconfig-test-2", //配置所在的appcode
              "env": "beta", //配置的环境，详见备注
              "subenv": "", //配置的子环境
              "serverenv": "beta", //服务器环境
              "serversubenv": "noah", //服务器子环境
              "dataid": "config.properties", //配置名称
              "version": 6, //当前配置版本号，新建配置则传0，请求完成后配置版本会加1
              "content": "key=value4445", //配置内容
              "public": "false", //是否设为公开配置，注意一旦设为公共无法再取消
              "description": "测试api上传配置" //配置的描述信息
          }
      }
      ```

  - 返回

    ```
    {"status":0,"message":"正常","data":{}}
    ```

    