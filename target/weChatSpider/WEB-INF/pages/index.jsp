<%--
  Created by IntelliJ IDEA.
  User: zhusy
  Date: 2017/6/7 0007
  Time: 9:19
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>图片抓取简单管理</title>
    <script src="./js/jquery-1.7.2.min.js" type="text/javascript"></script>
</head>
<body>
<div>
    <button id="freshStatus">刷新状态</button>
    <button id="startSpider">开始抓取</button>
    <button id="stopSpider">停止抓取</button>
    <button id="startDownload">开始下载</button>
    <button id="stopDownload">停止下载</button>
</div>
<div id="table">
</div>
<script type="text/javascript" src="./js/index.js"></script>
<script type="text/javascript" src="./js/jquery-1.10.2.js"></script>
<script type="text/javascript" src="./js/jquery-1.10.1.min.js"></script>
</body>
</html>
