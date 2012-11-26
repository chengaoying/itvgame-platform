package cn.ohyeah.itvgame.business.service.impl;

import java.util.Map;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.ohyeah.itvgame.business.ErrorCode;
import cn.ohyeah.itvgame.business.ResultInfo;
import cn.ohyeah.itvgame.business.service.SubscribeException;
import cn.ohyeah.itvgame.global.Configuration;

public class ShixianSubscribeUtil {
	private static final Log log = LogFactory.getLog(ShixianSubscribeUtil.class);
	//private static final DefaultHttpClient httpClient;
	private static final String rechargeUrlShixian;
	
	static {
		//httpClient = ThreadSafeClientConnManagerUtil.buildDefaultHttpClient();
		rechargeUrlShixian = Configuration.getProperty("shixian", "rechargeUrl");
	}
	
	public static ResultInfo consumeCoins(Map<String, Object> props){
		String buyUrl = (String) props.get("buyURL");
		String feeaccount = (String) props.get("feeaccount");
		String returnurl = (String) props.get("returnurl");
		String dwjvl = (String) props.get("dwjvl");
		String opcomkey = (String) props.get("opcomkey");
		String paysubway = (String) props.get("paysubway");
		String gameid = (String) props.get("gameid");
		String user_group_id = (String) props.get("user_group_id");
		String userToken = (String) props.get("userToken");
		int ammount = (Integer) props.get("amount");
		String userId = (String) props.get("userId");
		if(buyUrl==""||buyUrl==null){
			buyUrl = rechargeUrlShixian;
		}
		String params = "vplat#feeaccount="+feeaccount+";tvplat#returnurl="+returnurl+"; tvplat#numbercode="+userId
				+"; tvplat#dwjvl="+dwjvl+"; tvplat#opcomkey="+opcomkey+"; tvplat#paysubway="+paysubway+"; tvplat#gameid="+gameid
				+"; USER_TOKEN="+userToken+"; USER_GROUP_ID="+user_group_id;
		
		String rechargeUrl = String.format(buyUrl, ammount);
		log.info("rechargeUrl:"+rechargeUrl);
		ResultInfo info = new ResultInfo();
		/*info.setInfo(100);
		return info;*/
		
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(rechargeUrl);
		try {
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,	new DefaultHttpMethodRetryHandler());
			method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
			method.setRequestHeader("cookie", params);
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				throw new SubscribeException("Method failed: " + method.getStatusLine());
			}
			// ��ȡ����
			String body = method.getResponseBodyAsString();
			log.info("body==>"+body);
			
			String ss = body.substring(body.indexOf("*"),body.lastIndexOf("*"));
			
			if(!isErrorMessage(ss)){
				if(ss.equals("��ϲ��")){
					info.setInfo(ammount);
				}else if(ss.equalsIgnoreCase("password")){
					info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
					info.setMessage(ss);
				}else{
					int code = Integer.parseInt(ss);
					if(0==code){
						info.setInfo(ammount);
					}else{
						info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
						info.setMessage(getErrorMessage(code));
					}
				}
			} else {
				info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
				info.setMessage(ss);
			}
			
			return info;
		} catch (Exception e) {
			throw new SubscribeException(e);
		} finally{
			method.releaseConnection();
		}
	}

	private static String getErrorMessage(int errorCode){
		String message = null;
		switch (errorCode) {
		case 2012: message = "����ͯ��"; break;
		case 2: message = "����ID�����ڻ����û��ѱ�����"; break;
		case -1001: message = "��ֵ���˺�����ID�����쳣"; break;
		case -1002: message = "�û��������ڻ����û��ѱ�����"; break;
		case -1004: message = "����ID�����ڻ����û��ѱ�����"; break;
		case -1218: message = "�û���ֵ�����޶�"; break;
		case -1999: message = "ϵͳ�쳣"; break;
		case -9305: message = "�û���ֵ�����޶�"; break;
		default: message = "δ֪����"; break;
		}
		return message;
	}
	
	private static boolean isErrorMessage(String message){
		if(message.equals("���Žӿڵ���ʧ��")){
			return true;
		}else if(message.equals("��ֵʧ��")){
			return true;
		}else if(message.equals("�û���ֵ�����޶�")){
			return true;
		}else if(message.equals("����ID�����ڻ���")){
			return true;
		}else if(message.equals("���ĸôε�½")){
			return true;
		}else{
			return false;
		}
	}
}
