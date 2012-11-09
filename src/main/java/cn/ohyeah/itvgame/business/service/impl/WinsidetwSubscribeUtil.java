package cn.ohyeah.itvgame.business.service.impl;

import java.net.URLEncoder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import cn.halcyon.utils.ThreadSafeClientConnManagerUtil;
import cn.ohyeah.itvgame.business.ErrorCode;
import cn.ohyeah.itvgame.business.ResultInfo;
import cn.ohyeah.itvgame.business.service.SubscribeException;
import cn.ohyeah.itvgame.global.Configuration;


public class WinsidetwSubscribeUtil {
	private static final Log log = LogFactory.getLog(WinsidetwSubscribeUtil.class);
	private static final DefaultHttpClient httpClient;
	private static final String consumeCoinsUrlPattern;
	private static final String queryCoinsUrlPattern;
	private static final String rechargeUrlPatternWinside;
	private static final String onlineUrlPatternWinside;
	
	static {
		httpClient = ThreadSafeClientConnManagerUtil.buildDefaultHttpClient();
		consumeCoinsUrlPattern = Configuration.getProperty("winsidetw", "consumeCoinsUrl");
		queryCoinsUrlPattern = Configuration.getProperty("winsidetw", "queryCoinsUrl");
		rechargeUrlPatternWinside = Configuration.getProperty("winsidetw", "rechargeUrl");
		onlineUrlPatternWinside = Configuration.getProperty("winsidetw", "onlineUrl");
	}
	
	public static ResultInfo recharge(String buyURL, String spid, String userid, String username, int money, 
			String product, String userToken, String checkKey, String password) {
		try {
			String checkcode = userid 
							+ "|" + spid
							+ "|" + product
							+ "|" + checkKey
							+ "|" + money;
			checkcode = DigestUtils.md5Hex(checkcode);
			
			String winsideUrlPattern = null;
			if (!buyURL.endsWith("/")&&!rechargeUrlPatternWinside.startsWith("/")) {
				winsideUrlPattern = buyURL + "/" +rechargeUrlPatternWinside;
			}
			else {
				winsideUrlPattern = buyURL + rechargeUrlPatternWinside;
			}
			
			String rechargeUrl = String.format(winsideUrlPattern, userid, username, money, spid, product, userToken, checkcode);
			if (StringUtils.isNotEmpty(password)) {
				rechargeUrl += "&passwd="+password;
			}
			log.debug("[Winsidetw recharge Url] ==> "+rechargeUrl);
	    	HttpGet httpget = new HttpGet(rechargeUrl);
	    	String body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
	    	log.debug("[Winsidetw recharge Result] ==> "+body);
	    	ResultInfo info = new ResultInfo();
	    	//info.setInfo(1000);
	    	String[] result = body.split("#");
	    	if (!"success".equalsIgnoreCase(result[0].trim())) {
	    		info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
	    		//info.setMessage(getRechargeErrorMessage(Integer.parseInt(result[1].trim())));
	    		info.setMessage(result[2].trim());
	    	}
	    	else {
	    		info.setInfo(Integer.parseInt(result[2].trim()));
	    	}
	    	return info;
		}
		catch (Exception e) {
			throw new SubscribeException(e);
		}

	}
	
	public static ResultInfo consumeCoins(String buyURL, String userid, String username, String checkKey,
			String product, String contents, int amount, int coins) {
		try {
			String checkcode = userid 
							+ "|" + username
							+ "|" + product
							+ "|" + contents
							+ "|" + amount
							+ "|" + coins
							+ "|" +checkKey;
			checkcode = DigestUtils.md5Hex(checkcode);
			String winsideUrlPattern = null;
			if (!buyURL.endsWith("/")&&!consumeCoinsUrlPattern.startsWith("/")) {
				winsideUrlPattern = buyURL + "/" +consumeCoinsUrlPattern;
			}
			else {
				winsideUrlPattern = buyURL + consumeCoinsUrlPattern;
			}
			String consumeCoinsUrl = String.format(winsideUrlPattern, userid, username, product, URLEncoder.encode(contents, "UTF-8"), amount, coins, checkcode, "memo");
			log.debug("[Winsidetw consumeCoins Url] ==> "+consumeCoinsUrl);
	    	HttpGet httpget = new HttpGet(consumeCoinsUrl);
	    	String body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
	    	log.debug("[Winsidetw consumeCoins Result] ==> "+body);
	    	ResultInfo info = new ResultInfo();
	    	String[] result = body.split("#");
	    	if (!"success".equalsIgnoreCase(result[0].trim())) {
	    		info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
	    		//info.setMessage(getConsumeCoinsErrorMessage(Integer.parseInt(result[1].trim())));
	    		info.setMessage(result[2].trim());
	    	}
	    	else {
	    		info.setInfo(coins);
	    	}
	    	return info;
		}
		catch (Exception e) {
			throw new SubscribeException(e);
		}
	}
	
	public static int queryCoins(String buyURL, String userid) {
		try {
			String winsideUrlPattern = null;
			if (!buyURL.endsWith("/")&&!queryCoinsUrlPattern.startsWith("/")) {
				winsideUrlPattern = buyURL + "/" +queryCoinsUrlPattern;
			}
			else {
				winsideUrlPattern = buyURL + queryCoinsUrlPattern;
			}
			String queryCoinsUrl = String.format(winsideUrlPattern, userid);
			log.debug("[Winsidetw queryCoins Url] ==> "+queryCoinsUrl);
	    	HttpGet httpget = new HttpGet(queryCoinsUrl);
	    	String body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
	    	log.debug("[Winsidetw queryCoins Result] ==> "+body);
	    	String[] result = body.split("#");
	    	if ("success".equalsIgnoreCase(result[0].trim())) {
	    		return Integer.parseInt(result[2].trim());
	    	}
	    	else {
	    		if ("-1".equals(result[1].trim())) {
	    			return 0;
	    		}
	    		else {
	    			//throw new SubscribeException(getQueryCoinsErrorMessage(Integer.parseInt(result[1].trim())));
	    			throw new SubscribeException(result[2].trim());
	    		}
	    	}
		}
		catch (Exception e) {
			throw new SubscribeException(e);
		}
	}
	
	public static ResultInfo sendHeartbeatPacket(String buyURL, String userid, String product) {
		ResultInfo info = new ResultInfo();
		try {
			String winsideUrlPattern = null;
			if (!buyURL.endsWith("/")&&!onlineUrlPatternWinside.startsWith("/")) {
				winsideUrlPattern = buyURL + "/" +onlineUrlPatternWinside;
			}
			else {
				winsideUrlPattern = buyURL + onlineUrlPatternWinside;
			}
			String onlineUrl = String.format(winsideUrlPattern, userid, product);
			log.debug("[Winsidetw queryCoins Url] ==> "+onlineUrl);
	    	HttpGet httpget = new HttpGet(onlineUrl);
	    	String body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
	    	log.debug("[Winsidetw queryCoins Result] ==> "+body);
	    	String[] result = body.split("#");
	    	if (!"success".equalsIgnoreCase(result[0].trim())) {
	    		info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
	    		//info.setMessage(getConsumeCoinsErrorMessage(Integer.parseInt(result[1].trim())));
	    		info.setMessage(result[2].trim());
	    	}
	    	else {
	    		info.setInfo(result[2].trim());
	    	}
	    	return info;
		}
		catch (Exception e) {
			info.setErrorCode(ErrorCode.EC_SEND_HEARTBEAT);
    		info.setMessage("发送心跳包失败，原因：地址有误");
    		return info;
			//throw new SubscribeException(e);
		}
	}
	
	public static String getRechargeErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -1: message = "指定的用户不存在"; break;
		case -2: message = "无效的用户名"; break;
		case -3: message = "您当前v币数不足，无法执行相关操作"; break;
		case -4: message = "校验码不正确，请与GM联系"; break;
		case -5: message = "未知错误"; break;
		case -6: message = "您冲值过于频繁，请稍候再试"; break;
		case -7: message = "您已达到本月充值上限，请下个月再冲值"; break;
		case -8: message = "该用户禁止充值，请与GM联系"; break;
		case -9: message = "更新v币失败，请与GM联系"; break;
		case -10: message = "参数缺失"; break;
		case -11: message = "无效的冲值金额"; break;
		case -12: message = "超出每日充值上限"; break;
		case -13: message = "超出充值上限"; break;
		case -14: message = "您已达到本月个人充值上限，请下个月再冲值"; break;
		case -15: message = "支付密码错误，请重新输入"; break;
		case -16: message = "产品名称不正确，请与GM联系"; break;
		case -17: message = "无效订单"; break;
		case -18: message = "鉴权失败，请重新开启机顶盒后再试"; break;
		case -19: message = "返回值分析失败"; break;
		case -20: message = "您是政企客户，系统暂不提供此类用户在线充值"; break;
		case 9103: message = "支付密码错误，请重新输入"; break;
		default: message = "未知错误"; break;
		}
		return message;
	}
	
	public static String getConsumeCoinsErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -1: message = "指定的用户不存在"; break;
		case -2: message = "无效的用户名"; break;
		case -3: message = "v币数不足"; break;
		case -4: message = "校验码不正确，请与GM联系"; break;
		case -5: message = "未知错误"; break;
		default: message = "未知错误";
		}
		return message;
	}
	
	public static String getQueryCoinsErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -1: message = "该用户未充过值"; break;
		default: message = "未知错误";
		}
		return message;
	}
}
