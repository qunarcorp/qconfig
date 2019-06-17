# FAQ
常见问题

1. 一个环境有多个buildGroup情况，比如betaa，betab，buildGroup分别为a和b（注意不是betaa和betab），qconfig是如何知道的？
  qconfig通过qconfig.profile这个文件来获取buildGroup的信息。bds和qdr发布系统会自动添加qconfig.profile文件，并将发布的buildGroup写入。

2. 推送功能有什么用？
   当一个配置进行发布后，所有使用了该配置的客户端都会更新配置文件。如果你想要先看看新的配置靠不靠谱，可以先不发布，而是先推送到某一台后者几台机器上面看看情况，而其它机器仍然使用原有的配置。

3. 文件有大小限制么？ 
   单文件最大512K

4. 文件修改后没有更新
   先检查日志，有无use remote file 字样