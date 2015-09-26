package cn.finalteam.androidchannelbuild.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 
 * @author pengjianbo
 *
 */
public class ApkChannelUtil {
	
	/**
	 * 生产渠道包
	 * 
	 * @param srcApkFile apk源文件
	 * @param channels 渠道列表
	 * @param channelPrefix渠道前缀
	 * @return 生成的渠道包
	 */
	public static List<File> buildChannels(File srcApkFile, List<String> channels, String channelPrefix) {
		List<File> resultFileList = new ArrayList<File>();
		for (String channel : channels) {
			channel = channel.trim().replace("\n", "");
			String appName = srcApkFile.getName().substring(0, srcApkFile.getName().lastIndexOf("."));
			String apkFileName = appName + "-" + channel + ".apk";
			File parentFile = srcApkFile.getParentFile();
			File apkTargetFile = new File(parentFile, apkFileName);
			//拷贝apk
			FileUtils.copy(srcApkFile, apkTargetFile);
			//修改渠道
			boolean b = buildChannel(apkTargetFile.getAbsolutePath(), channelPrefix, channel);
			if ( b ) {
				resultFileList.add(apkTargetFile);
			}
		}
		return resultFileList;
	}

	 /**
     * 修改渠道号
     * <p>
     * demo: <code>changeChannel("xxx../../my.apk", "hiapk");</code>
     * </p>
     *
     * @param zipFilename apk文件
     * @param channel     新渠道号
     * @return true or false
     */
    public static boolean buildChannel(String zipFilename, String channelPrefix, String channel) {
		try (FileSystem zipfs = createZipFileSystem(zipFilename, false)) {
			String channelBegin = "/META-INF/" + channelPrefix;
			String channelFlagName = channelBegin + channel;
			
			final Path root = zipfs.getPath("/META-INF/");
			ChannelFileVisitor visitor = new ChannelFileVisitor(channelBegin);
			Files.walkFileTree(root, visitor);
			
			Path existChannel = visitor.getChannelFile();
			Path newChannel = zipfs.getPath(channelFlagName);
			if (existChannel!=null) {
				Files.move(existChannel, newChannel, StandardCopyOption.ATOMIC_MOVE);
			} else {
				Files.createFile(newChannel);
			}
			
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static FileSystem createZipFileSystem(String zipFilename, boolean create) throws IOException {
		final Path path = Paths.get(zipFilename);
		final URI uri = URI.create("jar:file:" + path.toUri().getPath());

		final Map<String, String> env = new HashMap<>();
		if (create) {
			env.put("create", "true");
		}
		return FileSystems.newFileSystem(uri, env);
	}
	
	private static class ChannelFileVisitor extends SimpleFileVisitor<Path> {
		private Path channelFile;
		private PathMatcher matcher;
		
		public ChannelFileVisitor(String channelBegin) {
			String channelFlagNameMatcher = "regex:" + channelBegin + "[0-9a-zA-Z]{1,5}";
			matcher = FileSystems.getDefault().getPathMatcher(channelFlagNameMatcher);
		}

		public Path getChannelFile() {
			return channelFile;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (matcher.matches(file)) {
				channelFile = file;
				return FileVisitResult.TERMINATE;
			} else {
				return FileVisitResult.CONTINUE;
			}
		}
	}
}
