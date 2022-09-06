<%@ page language="java" pageEncoding="UTF-8" import="java.io.*, java.net.*"%>
<%    
	com.ser.evITAWeb.api.IDoxisServer doxisServer=com.ser.evITAWeb.api.ClientUtil.getServerObject(request);
	
	byte[] data = (byte[]) doxisServer.getSessionObject("content");
	response.setCharacterEncoding("utf-8");
	response.setContentType("APPLICATION/OCTET-STREAM; charset=UTF-8");   
	response.setHeader("Content-length", Integer.toString(data.length));
	
	String filename = (String) doxisServer.getSessionObject("filename");  
	
	String filenameUTF8 = URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
	String filenameISO = URLDecoder.decode(filename, "ISO8859_1");

	response.setHeader("Content-Disposition","attachment; filename=\"" + filenameISO + "\"; filename*=UTF-8''" + filenameUTF8);  
	
	response.getOutputStream().write(data, 0, data.length);
	response.getOutputStream().flush();
  
%>   