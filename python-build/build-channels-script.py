#!/usr/bin/python
# coding=utf-8
import zipfile
import shutil
import os
import ConfigParser

#读取配置文件
config = ConfigParser.ConfigParser()
config.read("channels-config.ini")
#apk路径
apk_path = config.get("Build-Config", "apk.path")
print "src apk path=" + apk_path
#渠道识别前缀
channel_prefix = config.get("Build-Config", "channel.prefix")
print "channel prefix=" + channel_prefix
#渠道列表
channel_list = config.get("Build-Config", "channel.list")
print "channel list=" + channel_list
#解析渠道，生成渠道数组
channel_array = channel_list.split(',')

# 空文件 便于写入此空文件到apk包中作为channel文件
src_temp_file = 'temp_.txt'
# 创建一个空文件（不存在则创建）
f = open(src_temp_file, 'w')
f.close()


src_apk = apk_path
# file name (with extension)
src_apk_file_name = os.path.basename(src_apk)
# 分割文件名与后缀
temp_list = os.path.splitext(src_apk_file_name)
# name without extension
src_apk_name = temp_list[0]
# 后缀名，包含.   例如: ".apk "
src_apk_extension = temp_list[1]

# 创建生成目录,与文件名相关
output_dir = 'apks_' + src_apk_name + '/'
# 目录不存在则创建
if not os.path.exists(output_dir):
    os.mkdir(output_dir)

# 遍历渠道号并创建对应渠道号的apk文件
for line in channel_array:
    # 获取当前渠道号，因为从渠道文件中获得带有\n,所有strip一下
    target_channel = line.strip()
    # 拼接对应渠道号的apk
    target_apk = output_dir + src_apk_name + "-" + target_channel + src_apk_extension
    # 拷贝建立新apk
    shutil.copy(src_apk,  target_apk)
    # zip获取新建立的apk文件
    zipped = zipfile.ZipFile(target_apk, 'a', zipfile.ZIP_DEFLATED)

    # 初始化渠道信息
    target_channel_file = "META-INF/" + channel_prefix + "{channel}".format(channel = target_channel)
    # 写入渠道信息
    zipped.write(src_temp_file, target_channel_file)
    # 关闭zip流
    zipped.close()

#删除临时文件
os.remove(src_temp_file)