* qconfig会载入哪个环境的配置？
	qconfig通过应用中心的接口获取目标机器的环境，应用中心通过机器名来判断机器所在为prod，beta或dev环境，本地应用会被当作dev环境。试图通过maven编辑选项等设置环境是没有用的。
* 在本地运行程序调试，如何才能获取beta:noah123环境的配置？
  resources下面添加qconfig.profile和qunar-env.properties文件
	qunar-env.properties    name=beta
	qconfig.profile			noah123
	注意，出于安全考虑，qunar-env.properties仅可以指定dev/beta，试图指定为prod环境无效
* noah子环境的配置是如何生成的？
  noah创建环境时，可选择生成qconfig配置，生成配置在n3_envId子环境目录下，与其他环境隔离，方便测试。环境销毁时，文件随之删除。
  配置按照beta:noah(根据profileId指定子环境)->beta:->prod->resources优先级顺序，从其他环境copy到noah新环境，并做相应环境变量替换
  涉及到引用文件时，源文件也copy到同样到子环境，不会跨环境生成引用文件，避免冲突

