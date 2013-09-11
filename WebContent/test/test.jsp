<%@page import="cn.ohyeah.itvgame.platform.service.PurchaseRecordService"%>
<%@page import="cn.ohyeah.itvgame.platform.viewmodel.PurchaseStatis"%>
<%@page import="java.util.*" %>
<%@page import="cn.ohyeah.itvgame.global.BeanManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%!
 	private static final PurchaseRecordService prs = (PurchaseRecordService)BeanManager.getBean("purchaseRecordService");
%>
<%
 	List<PurchaseStatis> list= prs.queryPurchaseStatis(12,0,10,"2011-01-01 00:00:00", "2013-09-09 00:00:00");
	out.println("用户id--消费总金额");
	out.println("<br/>");
	for(PurchaseStatis ps:list){
		out.println(ps.getUserId()+"--"+ps.getSum());
		out.println("<br/>");
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>

</body>
</html>