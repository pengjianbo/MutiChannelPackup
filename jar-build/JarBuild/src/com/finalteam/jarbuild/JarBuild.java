package com.finalteam.jarbuild;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class JarBuild {

	public static void main(String[] args) {
//		String path = System.getProperty("java.class.path");
		try {
			FileInputStream fis = new FileInputStream("build-config.properties");
			Properties pro = new Properties();
			pro.load(fis);
			String channelPrefix = pro.getProperty("channel.prefix");
			String apkPath = pro.getProperty("apk.path");
			String channelList = pro.getProperty("channel.list");
			String[] channelArray = channelList.split(",");
			List<String> channels = Arrays.asList(channelArray);
			if ( channels != null && channels.size() > 0 ) {
				ApkChannelUtil.buildChannels(new File(apkPath), channels, channelPrefix);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
