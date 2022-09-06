<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.io.*, java.net.*"%>

<%
com.ser.evITAWeb.api.IDoxisServer doxisServer=com.ser.evITAWeb.api.ClientUtil.getServerObject(request);
%>

		
<%=doxisServer.getSessionObject("text")%><br />
	<a id="link" href="<%=com.ser.evITAWeb.framework.EvitaWebHelper.getEvitaWebContext()+"/customJsps/ru/office/fileDownloader2.jsp?subsession=" + request.getParameter("subsession")%>"><%=doxisServer.getSessionObject("linktext")%></a>
<script>
	document.getElementById('link').click();
</script>
