# FAQ
常见问题

1. 一个环境有多个buildGroup情况，比如betaa，betab，buildGroup分别为a和b（注意不是betaa和betab），qconfig是如何知道的？
  qconfig通过qconfig.profile这个文件来获取buildGroup的信息。bds和qdr发布系统会自动添加qconfig.profile文件，并将发布的buildGroup写入。

2. 推送功能有什么用？
   当一个配置进行发布后，所有使用了该配置的客户端都会更新配置文件。如果你想要先看看新的配置靠不靠谱，可以先不发布，而是先推送到某一台后者几台机器上面看看情况，而其它机器仍然使用原有的配置。
   
3. 环境、文件名等都配对了，但总是获取不到配置，日志中显示"java.util.concurrent.TimeoutException: No response received after 2000"
	请先确认一下异常日志中有没有"JDKAsyncHttpProvider"的信息，如果存在，那就是因为在pom文件中把qconfig的netty给exclude掉了，系统退化到使用jdk提供的AsyncHttpClient（如果使用的是debug级别的日志输出，那么日志中应该还会有"Response 200"这样的信息）。在版本冲突时（netty3和netty4不会出现冲突），只要在pom文件的dependencymanagement里面写上
	
4. 文件有大小限制么？
	单文件最大512K
	
5. 文件修改后没有更新

  先检查日志，有无use remote file 字样