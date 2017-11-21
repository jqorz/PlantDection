##小叶识图

作者:jqorz

###说明
植物识别App for Android

###功能
从图库中选择图片进行植物识别

###使用框架
- [EventBus (事件总线框架)](https://www.baidu.com/s?tn=mswin_oem_dg&ie=utf-16&word=eventbus)
- [ButterKnife (注解框架)](http://jakewharton.github.io/butterknife/)
- [OkUtil (网络通信框架)](http://blog.csdn.net/lmj623565791/article/details/47911083)
- [Gson (Java对象与Json数据转换的库)](http://blog.csdn.net/wanghao200906/article/details/45889955)
- [AndPermission (动态权限申请框架)](https://github.com/yanzhenjie/AndPermission/blob/master/README-CN.md)

###结构介绍
算法类为EdgeDetector,Vein,ColorUtil
调用算法的主要逻辑为LeafService
其他的类大都为日常项目使用的工具类

###使用介绍
1. 将assest文件夹下的内容拷贝至sd卡下，分别为/sdcard/PlantDetection/datafile.txt（此为测试集的数据，训练中科院植物研究所的植物数据集得到的，用来掩人耳目的）
和/sdcard/PlantDetection/leaves description（此文件夹下的内容为植物的说明，用来检测成功后显示植物的百科）
2. 授予软件存储权限，联网权限
3. 选择相册中的图片即可进行识别（百度api返回的结果和那些支持的植物可以在Log日志中看到，逻辑是百度api返回的植物的名称，如果与本地的植物百科的名字相符，便会提示检测成功）
###辅助说明

此项目原为一同学的本科毕设论文（包含完整的毕设文档），为C/S模式并含有其他一些功能。
但经测试，虽然用了看上去很牛逼的算法，但并不能真正检测出正确的结果。
因此，本人将原项目中的服务器中的检测算法改到了客户端，保留了原算法代码（大家都懂的），然后接入了百度的识图API，能够识别出部分图片
（觉得有用的欢迎star）

 [此为原作者的原文地址](https://github.com/MinTate/Mleaf)