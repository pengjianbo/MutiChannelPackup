<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<link rel="stylesheet" type="text/css" href="css/style.css" />
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Android多渠道打包</title>
</head>

<body>
	<form action="fileupload" method="post" enctype="multipart/form-data">
		在此填写渠道，每个渠道占一行：<br/><textarea rows="20" cols="50" id="channels" name="channels"></textarea>
		<p class="help-block">请选择apk文件</p><input type="file" name="file">
		<p/>
		渠道识别前缀（默认channel-）：<input type="text" id="channelPrefix" name="channelPrefix" value="channel-"/><p/>
		<input type="submit" title="执行" value="执行">
	</form>
</body>
</html>