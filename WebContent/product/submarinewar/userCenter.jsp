<%@ page language="java" contentType="text/html; charset=GB18030"
    pageEncoding="GB18030"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="cn.ohyeah.itvgame.platform.service.RechargeService"%>
<%@page import="cn.ohyeah.itvgame.business.ResultInfo"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="cn.ohyeah.itvgame.global.BeanManager"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>�û�����</title>
	<meta name="page-view-size" content="640*530"/>
	<meta http-equiv="Content-Type" content="text/html; charset=GB18030">
  	<meta http-equiv="Pragma" content="no-cache" />
  	<meta http-equiv="Cache-Control" content="no-cache" />
  	<meta http-equiv="Expires" content="0" />
</head>
<%!
	private static RechargeService rs = (RechargeService)BeanManager.getBean("rechargeService");
%>
<%
	int productId = Integer.parseInt(request.getParameter("productId"));
	int accountId = Integer.parseInt(request.getParameter("accountId"));
	String userId = request.getParameter("userId");
	String accountName = request.getParameter("accountName");
	String dspUserId = userId;
	String returnUrl = request.getParameter("returnUrl");
	
	int goldCoin = 0;
	if (request.getParameter("goldCoin") == null) {
		Map<String, Object> props = new HashMap<String, Object>();	
		props.put("accountName", accountName);
		ResultInfo rst = rs.queryBanlance(props, accountId, productId);
		if (rst.getErrorCode() == 0) {
			goldCoin = (Integer)rst.getInfo();
		}
	}
	else {
		goldCoin = Integer.parseInt(request.getParameter("goldCoin"));
	}
	
	String backUrl = URLEncoder.encode("userCenter.jsp?returnUrl="+URLEncoder.encode(returnUrl, "gbk")
			+"&goldCoin="+goldCoin
			+"&productId="+productId
			+"&accountId="+accountId
			+"&accountName="+accountName
			+"&userId="+userId, "gbk");
%>
<script type="text/javascript">
function back(){
	window.location.href='<%=returnUrl%>';
}

function PageScroll(evt){
  evt = evt ? evt : window.event;
  var keyCode = evt.which ? evt.which : evt.keyCode;
  if(keyCode==8 || keyCode==126 || keyCode==48){
	  back();
	  return;
  }
}

function init() {
	var f = document.getElementById("focusMe");
	if (f) f.focus();
	document.onkeypress = PageScroll;
}
</script>
<body bgcolor="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="init();">
<div style="position:absolute;left:0px;top:0px;width:634px;height:520px;background-image:url('../../content/product/submarinewar/userCenter.jpg');background-repeat:no-repeat;">
<table>
	<tbody>
	<tr>
		<td>
			<span style="left:480px;top:126px;position:absolute;width:143px;height:29px;font-size:18px;color:#01dfe4;text-align:center"><%=dspUserId %></span>
			<span style="left:480px;top:183px;position:absolute;width:143px;height:29px;font-size:18px;color:#01dfe4;text-align:center"><%=goldCoin%></span>
		</td>
	</tr>
	<tr>
		<td>
			<div style="left:133px;top:197px;position:absolute">
			<a href="purchase.jsp?productId=<%=productId %>&accountId=<%=accountId %>&userId=<%=userId %>&goldCoin=<%=goldCoin %>&returnUrl=<%=backUrl%>">
			<img width="227" height="66" src="../../content/common/empty.gif" border="0"/></a>
			</div>
			<div style="left:133px;top:314px;position:absolute">
			<a href="recharge.jsp?productId=<%=productId %>&accountId=<%=accountId %>&userId=<%=userId %>&goldCoin=<%=goldCoin %>&returnUrl=<%=backUrl%>">
			<img width="227" height="66" src="../../content/common/empty.gif" border="0"/></a>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<div style="left:343px;top:414px;position:absolute">
			<a href="<%=returnUrl %>"><img width="84" height="34" src="../../content/common/empty.gif" border="0"/></a>
			</div>
		</td>
	</tr>
	</tbody>
</table>
</div>
</body>
</html>