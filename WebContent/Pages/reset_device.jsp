<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="project.ids.*" %>
<%request.setCharacterEncoding("utf-8");%>
<%
int id = Integer.parseInt(request.getParameter("deviceID"));
ArduinoCommunicationServer.resetDevice(id);

%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>

</body>
</html>