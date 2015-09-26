# MutiChannelPackup
Android多渠道打包，有网页版，Gradle版，Python版、BAT脚本、Jar版…看大家的喜好选择

* META-INF渠道识别型：网页版、Python版、BAT脚本、Jar版、
* Manifest渠道识别型：Gradle版

META-INF渠道识别详情请查看美团team博客 [点击查看](http://tech.meituan.com/mt-apk-packaging.html)

##功能描述
* 支持修改manifest渠道配置
* 支持在apk的META-INFO目录下生成渠道文件
* APK不需要重新签名
* 现只对umeng渠道统计做了支持
* 不需要第二次签名
* 对包无损害

##功能更新中
* 支持更多的第三方渠道统计工具
* 编写windowns bat脚本
* 添加网页版用户体系、权限和数据库配置
* ……


##网页版
* META-INF渠道识别
* 使用简单，非技术小白也可以用
* 功能相对强大(设计中…)

1、在android中获取渠道名：

```java
	/**
     * 友盟配置
     */
    private void umengConfig() {
        String channel = getChannelFromApk(this, "channel-");
        if (StringUtils.isEmpty(channel)) {
            channel = "paojiao";
        }
        if ( Constants.DEBUG ) {
            Toast.makeText(IApplication.this, "当前渠道:" + channel, Toast.LENGTH_SHORT).show();
        }
        AnalyticsConfig.setChannel(channel);
    }
    
	/**
     * 从apk中获取版本信息
     * @param context
     * @param channelPrefix 渠道前缀
     * @return
     */
    public static String getChannelFromApk(Context context, String channelPrefix) {
        //从apk包中获取
        ApplicationInfo appinfo = context.getApplicationInfo();
        String sourceDir = appinfo.sourceDir;
        //默认放在meta-inf/里， 所以需要再拼接一下
        String key = "META-INF/" + channelPrefix;
        String ret = "";
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(sourceDir);
            Enumeration<?> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.startsWith(key)) {
                    ret = entryName;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipfile != null) {
                try {
                    zipfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String[] split = ret.split(channelPrefix);
        String channel = "";
        if (split != null && split.length >= 2) {
            channel = ret.substring(channelPrefix.length());
        }
        return channel;
    }
```
2、在build-config.properties中配置渠道识别前缀（可选）

3、把AndroidChannelBuild.war上传到Java服务CHANNEL_VALUE器上

4、输入http://www.xxx.yyy/AndroidChannelBuild/

5、上传文件、填写渠道前缀（可选）、输入渠道名称

6、下载打包完成的渠道包

###**部分截图**
![上传文件](images/web_channel_build_upload_file.tiff)![构建成功](images/web_channel_build_success.tiff)


##Gradle版
* 支持manifest渠道配置
* 使用简单不易出错

###manifest渠道配置
网络上有好多人都是通过添加

```xml
android {  
    productFlavors {
        xiaomi {}
        360 {}
        baidu {}
        wandoujia {}
    }  

    productFlavors.all { 
        flavor -> flavor.manifestPlaceholders = [CHANNEL_VALUE: name] 
    }
}
```
代码实现的多渠道打包，这样有个问题，我们项目渠道号经常变，每个版本都有不同的渠道号，不停的在增加导致gradle文件就非常长。我今天特别为这个事情纠结了一上午发现还可以这么写：

```xml
productFlavors {
        for (int i = 0 ; i < channelArray.size(); i++) {
            def channel = channelArray[i]
            "${channel}"{
                manifestPlaceholders = [CHANNEL_VALUE: channel]
            }
        }
    }
```
让我格外的惊喜，这里分享给大家

###使用方法
1、添加channels.properties渠道信息配置文件

```properties
#默认渠道
channel.default=paojiao
#全部渠道列表
channel.list=baidu,360,hiapk....
```
2、在module build.gradle引入channels.gradle配置文件

```xml
apply from: "https://raw.githubusercontent.com/MutiChannelPackup/gradle-build/master/channels.gradle"
```
或者将channels.gradle下载到本地引入

```xml
apply from: "../channels.gradle"
```

3、在manifest中添加

```xml
<meta-data android:name="UMENG_CHANNEL" android:value="${CHANNEL_VALUE}" />
<meta-data android:name="JPUSH_CHANNEL" android:value="${CHANNEL_VALUE}" />
...
```
4、执行打包./gradlew assembleRelease或通过android studio->generate signed apk打包

5、在module目录下可以看到所有的渠道包 

##Python版
* 支持META-INF渠道识别
* 极速生成渠道包
* 使用简单不易出错

###使用方法
1、在android中获取渠道名：

```java
	/**
     * 友盟配置
     */
    private void umengConfig() {
        String channel = getChannelFromApk(this, "channel-");
        if (StringUtils.isEmpty(channel)) {
            channel = "paojiao";
        }
        if ( Constants.DEBUG ) {
            Toast.makeText(IApplication.this, "当前渠道:" + channel, Toast.LENGTH_SHORT).show();
        }
        AnalyticsConfig.setChannel(channel);
    }
    
	/**
     * 从apk中获取版本信息
     * @param context
     * @param channelPrefix 渠道前缀
     * @return
     */
    public static String getChannelFromApk(Context context, String channelPrefix) {
        //从apk包中获取
        ApplicationInfo appinfo = context.getApplicationInfo();
        String sourceDir = appinfo.sourceDir;
        //默认放在meta-inf/里， 所以需要再拼接一下
        String key = "META-INF/" + channelPrefix;
        String ret = "";
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(sourceDir);
            Enumeration<?> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.startsWith(key)) {
                    ret = entryName;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipfile != null) {
                try {
                    zipfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String[] split = ret.split(channelPrefix);
        String channel = "";
        if (split != null && split.length >= 2) {
            channel = ret.substring(channelPrefix.length());
        }
        return channel;
    }
```

2、配置channels-config.ini文件

```ini
[Build-Config]
#配置apk路径
apk.path = /Users/pengjianbo/Documents/dev/android_dev/workspace/GradleBuildChannels/app/demo.apk
#配置渠道识别前缀
channel.prefix = channel-
#配置渠道列表
channel.list = baidu,qq,360,paojiao
```
3、执行build-channels-script.py脚本

```shell
python build-channels-script.py 
```
4、可以看到生成了一个apks_{apk file name}的文件夹

##Jar版
* 支持META-INF渠道识别
* 极速生成渠道包
* 使用简单不易出错

###使用方法
1、在android中获取渠道名：

```java
	/**
     * 友盟配置
     */
    private void umengConfig() {
        String channel = getChannelFromApk(this, "channel-");
        if (StringUtils.isEmpty(channel)) {
            channel = "paojiao";
        }
        if ( Constants.DEBUG ) {
            Toast.makeText(IApplication.this, "当前渠道:" + channel, Toast.LENGTH_SHORT).show();
        }
        AnalyticsConfig.setChannel(channel);
    }
    
	/**
     * 从apk中获取版本信息
     * @param context
     * @param channelPrefix 渠道前缀
     * @return
     */
    public static String getChannelFromApk(Context context, String channelPrefix) {
        //从apk包中获取
        ApplicationInfo appinfo = context.getApplicationInfo();
        String sourceDir = appinfo.sourceDir;
        //默认放在meta-inf/里， 所以需要再拼接一下
        String key = "META-INF/" + channelPrefix;
        String ret = "";
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(sourceDir);
            Enumeration<?> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.startsWith(key)) {
                    ret = entryName;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipfile != null) {
                try {
                    zipfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String[] split = ret.split(channelPrefix);
        String channel = "";
        if (split != null && split.length >= 2) {
            channel = ret.substring(channelPrefix.length());
        }
        return channel;
    }
```

2、配置build-config.properties文件

```properties
#配置apk路径
apk.path = /Users/pengjianbo/Documents/dev/android_dev/workspace/GradleBuildChannels/app/demo.apk
#配置渠道识别前缀
channel.prefix = channel-
#配置渠道列表
channel.list = baidu,qq,360,paojiao
```
3、执行JarBuild.jar

```shell
java -jar JarBuild.jar
```
4、可以看到在apk.path文件夹下生成了所有渠道包

#BAT版
**Upcoming...**
