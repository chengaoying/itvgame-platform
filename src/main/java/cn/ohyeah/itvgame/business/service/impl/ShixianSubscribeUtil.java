package cn.ohyeah.itvgame.business.service.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.halcyon.utils.ThreadSafeClientConnManagerUtil;
import cn.ohyeah.itvgame.business.ErrorCode;
import cn.ohyeah.itvgame.business.ResultInfo;
import cn.ohyeah.itvgame.business.service.SubscribeException;
import cn.ohyeah.itvgame.global.Configuration;

public class ShixianSubscribeUtil {
	private static final Log log = LogFactory.getLog(ShixianSubscribeUtil.class);
	private static final DefaultHttpClient httpClient;
	private static final String rechargeUrlShixian;
	private static final String expendUrlShixian;
	private static final String tokenUrl;
	
	static {
		httpClient = ThreadSafeClientConnManagerUtil.buildDefaultHttpClient();
		rechargeUrlShixian = Configuration.getProperty("shixian", "baseUrl") + Configuration.getProperty("shixian", "rechargeUrl");
		expendUrlShixian = Configuration.getProperty("shixian", "baseUrl") + Configuration.getProperty("shixian", "expendUrl");
		tokenUrl = Configuration.getProperty("shixian", "baseUrl") + Configuration.getProperty("shixian", "tokenUrl");
	}
	
	public static ResultInfo recharge(Map<String, Object> props){
		String feeaccount = (String) props.get("feeaccount");
		String dwjtvkey = (String) props.get("dwjtvkey");
		String opcomkey = (String) props.get("opcomkey");
		String paysubway = (String) props.get("paysubway");
		int ammount = (Integer) props.get("amount");
		String userId = (String) props.get("userId");
		String appId = (String) props.get("appId");
		String params = "tvplat#feeaccount="+feeaccount+";tvplat#returnurl="+/*returnurl*/""+"; tvplat#numbercode="+userId
				+"; tvplat#dwjvl="+dwjtvkey+"; tvplat#opcomkey="+opcomkey+"; tvplat#paysubway="+paysubway;
		log.info("cookie:"+params);
		String rechargeUrl = String.format(rechargeUrlShixian, ammount, appId);
		log.info("rechargeUrl:"+rechargeUrl);
		ResultInfo info = new ResultInfo();
		HttpGet httpget = new HttpGet(rechargeUrl);
		
		//��header��д��cookie��cookie�е��ǽӿ��������
		httpget.addHeader("cookie", params);
    	String body;
		try {
			body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
			log.info("body==>"+body);
			String ss = body.substring(body.indexOf("*")+1,body.lastIndexOf("*"));
			log.info("returnMessage:"+ss);
			if(!isErrorMessage(ss)){
				if(ss.startsWith("��ϲ��")){
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
		}
	}
	
	public static ResultInfo expend(Map<String, Object> props){
		String feeaccount = (String) props.get("feeaccount");
		String dwjtvkey = (String) props.get("dwjtvkey");
		String opcomkey = (String) props.get("opcomkey");
		String paysubway = (String) props.get("paysubway");
		String userId = (String) props.get("userId");
		String userToken = (String)props.get("userToken");
		//String userToken = getToken();
		String gameCode = (String)props.get("gameCode");
		int amount = (Integer)props.get("amount");
		String params = "tvplat#feeaccount="+feeaccount+";tvplat#returnurl="+/*returnurl*/""+"; tvplat#numbercode="+userId
					+"; tvplat#dwjvl="+dwjtvkey+"; tvplat#opcomkey="+opcomkey+"; tvplat#paysubway="+paysubway;
		log.info("cookie:"+params);
		
		String expendUrl = String.format(expendUrlShixian, gameCode,amount,userToken);
		log.debug("expendUrl==>"+expendUrl);
		ResultInfo info = new ResultInfo();
		HttpGet httpget = new HttpGet(expendUrl);
		//httpget.addHeader("cookie", params);
		String body;
		try {
			body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
			log.info("body==>"+body);
			ObjectMapper op = new ObjectMapper();
	    	JsonNode node = op.readValue(body, JsonNode.class);
	    	String code = String.valueOf(node.get("code"));
	    	String message = String.valueOf(node.get("message"));
	    	if(formatString(code).equals("1")){
	    		info.setInfo(amount);
	    	}else{
	    		info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
	    		info.setMessage(message);
	    	}
			return info;
		} catch (Exception e) {
			throw new SubscribeException(e);
		}
	}
	
	private static String getToken(){
		String vlcode =  Configuration.getProperty("shixian", "vlcode");
		String url = String.format(tokenUrl, vlcode);
		log.debug("tokenUrl==>"+url);
		HttpGet get = new HttpGet(url);
		String body;
		try {
			body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, get);
			log.info("body==>"+body);
			ObjectMapper op = new ObjectMapper();
	    	JsonNode node = op.readValue(body, JsonNode.class);
	    	String code = String.valueOf(node.get("code"));
	    	if(formatString(code).equals("1")){
	    		String token = String.valueOf(node.get("token"));
		    	return formatString(token);
	    	}else{
	    		return null;
	    	}
		} catch (Exception e) {
			throw new SubscribeException(e);
		}
	}

	private static String formatString(String str){
		if(str.contains("\"")){
			str = str.substring(1, str.length()-1);
		}
		return str;
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
