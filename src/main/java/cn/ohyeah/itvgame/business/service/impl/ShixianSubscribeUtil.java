package cn.ohyeah.itvgame.business.service.impl;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static final String rechargeUrlPWShixian;
	private static final String expendUrlShixian;
	private static final String tokenUrl;
	
	static {
		httpClient = ThreadSafeClientConnManagerUtil.buildDefaultHttpClient();
		rechargeUrlShixian = Configuration.getProperty("shixian", "baseUrl") + Configuration.getProperty("shixian", "rechargeUrl");
		rechargeUrlPWShixian = Configuration.getProperty("shixian", "baseUrl") + Configuration.getProperty("shixian", "rechargeUrl_pw");
		expendUrlShixian = Configuration.getProperty("shixian", "baseUrl") + Configuration.getProperty("shixian", "expendUrl");
		tokenUrl = Configuration.getProperty("shixian", "baseUrl") + Configuration.getProperty("shixian", "tokenUrl");
	}
	
	public static ResultInfo recharge(Map<String, Object> props){
		String feeaccount = (String) props.get("feeaccount");
		String dwjtvkey = (String) props.get("dwjtvkey");
		String opcomkey = (String) props.get("vl_zonekey");
		String paysubway = (String) props.get("paysubway");
		int ammount = (Integer) props.get("amount");
		String userId = (String) props.get("userId");
		String appId = (String) props.get("appId");
		String opcomtoken = (String) props.get("opcomtoken");
		String opcompara = (String) props.get("opcompara");
		String returnurl = (String) props.get("returnurl");
		String gameid = (String) props.get("gameid");
		String params = "tvplat#feeaccount="+feeaccount
						+";tvplat#returnurl="+returnurl
						+";tvplat#numbercode="+userId
						+";tvplat#dwjvl="+dwjtvkey
						+";tvplat#opcomkey="+opcomkey
						+";tvplat#paysubway="+paysubway
						+";tvplat#pay#fromid="+gameid
						+";tvplat#pay#from="+1
						+";USER_TOKEN="+opcomtoken
						+";USER_GROUP_ID="+opcompara;
		log.info("cookie:"+params);
		
		/*String userToken = getToken();
		getUserInfo(userToken);*/
		
		String password = (String) props.get("password");
		String rechargeUrl = null;
		if(password.equals("")){
			rechargeUrl = String.format(rechargeUrlShixian, ammount, appId);
		}else{
			rechargeUrl = String.format(rechargeUrlPWShixian, password, appId, ammount);
		}
		log.info("rechargeUrl:"+rechargeUrl);
		ResultInfo info = new ResultInfo();
		HttpGet httpget = new HttpGet(rechargeUrl);
		
		//往header中写入cookie，cookie中的是接口所需参数
		httpget.addHeader("cookie", params);
    	String body;
		try {
			body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
			log.info("body==>"+body);
			String ss = getReturnInfo(body);
			log.info("returnMessage:"+ss);
			if(!isErrorMessage(ss)){
				if(ss.indexOf("恭喜您") >= 0){
					info.setInfo(ammount);
				}else if(isError(ss)){
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
	
	private static boolean isError(String str){
		if(str.indexOf("密码") >= 0){
			if(str.indexOf("有误")>=0 
					|| str.indexOf("错误")>=0
					|| str.indexOf("不正确")>=0){
				return true;
			}
		}
		return false;
	}
	
	private static String getReturnInfo(String body){
		String str = Configuration.getPattern("shixian", "span");
		Pattern p = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(body);
		String info = "";
		if(m.find()){
			info = m.group(1);    
			while(info.indexOf("&nbsp;") >= 0){
				info = info.substring("&nbsp;".length(), info.length());       
			}
		}
		return info;
	}
	
	public static ResultInfo expend(Map<String, Object> props){
		String feeaccount = (String) props.get("feeaccount");
		String dwjtvkey = (String) props.get("dwjtvkey");
		String opcomkey = (String) props.get("opcomkey");
		String paysubway = (String) props.get("paysubway");
		String userId = (String) props.get("userId");
		//String userToken = (String)props.get("userToken");
		String userToken = getToken();
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
	
	private static void getUserInfo(String token){
		String url = Configuration.getProperty("shixian", "baseUrl") + Configuration.getProperty("shixian", "userInfoUrl");
		url = String.format(url, token);
		HttpGet httpget = new HttpGet(url);
		String body;
		try {
			body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
			log.info("body==>"+body);
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
		case 2012: message = "江苏童锁"; break;
		case 2: message = "数字ID不存在或者用户已被冻结"; break;
		case -1001: message = "充值的账号数字ID类型异常"; break;
		case -1002: message = "用户名不存在或者用户已被冻结"; break;
		case -1004: message = "数字ID不存在或者用户已被冻结"; break;
		case -1218: message = "用户充值超出限额"; break;
		case -1999: message = "系统异常"; break;
		case -9305: message = "用户充值超出限额"; break;
		default: message = "未知错误"; break;
		}
		return message;
	}
	
	private static boolean isErrorMessage(String message){
		if(message.equals("电信接口调用失败")){
			return true;
		}else if(message.equals("充值失败")){
			return true;
		}else if(message.equals("用户充值超出限额")){
			return true;
		}else if(message.equals("数字ID不存在或者")){
			return true;
		}else if(message.equals("您的该次登陆")){
			return true;
		}else{
			return false;
		}
	}
}
