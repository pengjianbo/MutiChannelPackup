package cn.finalteam.androidchannelbuild.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import cn.finalteam.androidchannelbuild.utils.ApkChannelUtil;
import cn.finalteam.androidchannelbuild.utils.FileUtils;
import cn.finalteam.androidchannelbuild.utils.MultiZipDownloadUtil;
/**
 * 
 * @author pengjianbo
 *
 */
@WebServlet(name = "fileupload", urlPatterns = "/fileupload")
@MultipartConfig
public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		String action = request.getParameter("action");
		if ( action != null && action.equals("download") ) {
			List<File> channelFiles = (List<File>) request.getSession().getAttribute("channelFile");
			String zipStorePath = (String) request.getSession().getAttribute("zipStorePath");
			if ( channelFiles == null || channelFiles.size() == 0 || zipStorePath == null || zipStorePath.trim().length() == 0) {
				out.println("没找到下载资源");
			} else {
				printChannelList(out, channelFiles);
				try {
					MultiZipDownloadUtil.downLoadFiles(channelFiles, new File(zipStorePath), request, response);
				} catch (Exception e) {
					e.printStackTrace();
					out.println("下载失败");
				}
			}
		} else {
			String channelString = request.getParameter("channels");
			String channelPrefix = request.getParameter("channelPrefix");
			if (channelString == null || channelString.trim().length() == 0
					|| channelString.trim().split("\\n").length == 0) {
				out.println("请输入渠道名");
			} else {
				// 存储路径
				Part part = request.getPart("file");
				// Servlet3没有提供直接获取文件名的方法,需要从请求头中解析出来
				// 获取请求头
				String header = part.getHeader("content-disposition");
				// 获取文件名
				String fileName = parseFileName(header);
				String baseFileName = FileUtils
						.getFileNameWithoutExtension(fileName);
				String storePath = request.getServletContext().getRealPath(
						"/uploadFile/") + baseFileName + "/";
				new File(storePath).delete();
				if (!new File(storePath).exists()) {
					FileUtils.makeDirs(storePath);
				}
				System.out.println("storePath=" + storePath);
				if ("apk".equalsIgnoreCase(FileUtils.getFileExtension(fileName))) {
					// 把文件写到指定路径
					String fstr = storePath + File.separator + fileName;
					File saveFile = new File(fstr);
					saveFile.createNewFile();
					part.write(fstr);
					out.println("上传成功");
					String[] channelArray = channelString.trim().split("\\n");
					List<String> channels = Arrays.asList(channelArray);
					InputStream in = this.getServletContext().getResourceAsStream(
							"/WEB-INF/classes/build-config.properties");
					if (channelPrefix == null || channelPrefix.trim().length() == 0) {
						Properties pro = new Properties();
						pro.load(in);
						channelPrefix = pro.getProperty("channel_prefix");
					}
					
					if (channelPrefix == null || channelPrefix.trim().length() == 0) {
						out.println("channelPrefix不能为空，请检查build-config.properties文件");
					} else {
						List<File> channelFiles = ApkChannelUtil.buildChannels(
								saveFile, channels, channelPrefix);
						if (channelFiles.size() > 0) {
							request.getSession().setAttribute("channelFile", channelFiles);
							request.getSession().setAttribute("zipStorePath", new File(storePath, baseFileName + ".zip").getAbsolutePath());
							out.println("<p/>恭喜你打包成功，点击<a href='fileupload?action=download'>下载</a>渠道包");
							printChannelList(out, channelFiles);
						} else {
							out.println("没有生产任何渠道包~请联系管理员");
						}
					}
	
				} else {
					out.println("文件错误！请上传apk文件~");
				}
			}
		}
		out.flush();
		out.close();
	}

	/**
	 * 根据请求头解析出文件名 请求头的格式：form-data; name="file"; filename="a.txt"
	 * 
	 * @param header
	 * @return
	 */
	public String parseFileName(String header) {
		return header.substring(header.lastIndexOf("=") + 2,
				header.length() - 1);
	}
	
	private void printChannelList(PrintWriter out, List<File> fileList) {
		out.println("<h4>已完成的渠道包列表：</h4>");
		for (File file : fileList) {
			out.println("<h6>" + file.getName() + "</h6>");
		}
	}
}