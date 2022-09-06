<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.io.*, java.net.*"%>

<%
%>

		
	<a id="link" href="<%=request.getParameter("link")%>"><%=request.getParameter("linktext")%></a>
<script>
	document.getElementById('link').click();
</script>
