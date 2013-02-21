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


public class WinsideSubscribeUtil {
	private static final Log log = LogFactory.getLog(WinsideSubscribeUtil.class);
	private static final DefaultHttpClient httpClient;
	private static final String consumeCoinsUrlPattern;
	private static final String queryCoinsUrlPattern;
	private static final String rechargeUrlPatternWinsidegd;
	private static final String rechargeUrlPatternWinside;
	private static final String rechargePageUrlPatternWinside;
	
	static {
		httpClient = ThreadSafeClientConnManagerUtil.buildDefaultHttpClient();
		consumeCoinsUrlPattern = Configuration.getProperty("winside", "consumeCoinsUrl");
		queryCoinsUrlPattern = Configuration.getProperty("winside", "queryCoinsUrl");
		rechargeUrlPatternWinsidegd = Configuration.getProperty("winsidegd", "rechargeUrl");
		rechargeUrlPatternWinside = Configuration.getProperty("winside", "rechargeUrl");
		rechargePageUrlPatternWinside = Configuration.getProperty("winside", "rechargePageUrl");
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
			log.info("[Winside recharge Url] ==> "+rechargeUrl);
	    	HttpGet httpget = new HttpGet(rechargeUrl);
	    	String body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
	    	log.info("[Winside recharge Result] ==> "+body);
	    	ResultInfo info = new ResultInfo();
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
	
	public static ResultInfo rechargeGd(String buyURL, String spid, String userid, String username, int money, String product, String gameid,
			String enterURL, String zyUserToken, String stbType, String payType, String checkKey, String password) {
		try {
			String checkcode = userid 
							+ "|" + spid
							+ "|" + product
							+ "|" + checkKey
							+ "|" + money;
			checkcode = DigestUtils.md5Hex(checkcode);
			
			String winsideUrlPattern = null;
			if (!buyURL.endsWith("/")&&!rechargeUrlPatternWinsidegd.startsWith("/")) {
				winsideUrlPattern = buyURL + "/" +rechargeUrlPatternWinsidegd;
			}
			else {
				winsideUrlPattern = buyURL + rechargeUrlPatternWinsidegd;
			}
			
			String rechargeUrl = String.format(winsideUrlPattern, userid, username, spid, stbType, product, money, gameid, enterURL, zyUserToken, checkcode, payType);
			if (StringUtils.isNotEmpty(password)) {
				rechargeUrl += "&passwd="+password;
			}
			log.info("[Winside rechargegd Url] ==> "+rechargeUrl);
	    	HttpGet httpget = new HttpGet(rechargeUrl);
	    	String body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
	    	log.info("[Winside rechargegd Result] ==> "+body);
	    	ResultInfo info = new ResultInfo();
	    	String[] result = body.split("#");
	    	if (!"success".equalsIgnoreCase(result[0].trim())) {
	    		info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
	    		//info.setMessage(getRechargegdErrorMessage(Integer.parseInt(result[1].trim())));
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
			String product, String con, int amount, int coins) {
		try {
			String contents = "consumeCoins";
			String checkcode = userid 
							+ "|" + username
							+ "|" + product
							+ "|" + contents
							+ "|" + amount
							+ "|" + coins
							+ "|" +checkKey;
			log.info("checkcode:"+checkcode);
			checkcode = DigestUtils.md5Hex(checkcode);
			log.info("checkcode:"+checkcode);
			String winsideUrlPattern = null;
			if (!buyURL.endsWith("/")&&!consumeCoinsUrlPattern.startsWith("/")) {
				winsideUrlPattern = buyURL + "/" +consumeCoinsUrlPattern;
			}
			else {
				winsideUrlPattern = buyURL + consumeCoinsUrlPattern;
			}
			String consumeCoinsUrl = String.format(winsideUrlPattern, userid, username, product, contents, amount, coins, checkcode);
			log.debug("[Winside consumeCoins Url] ==> "+consumeCoinsUrl);
	    	HttpGet httpget = new HttpGet(consumeCoinsUrl);
	    	String body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
	    	log.debug("[Winside consumeCoins Result] ==> "+body);
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
			if (!buyURL.endsWith("/")&&!consumeCoinsUrlPattern.startsWith("/")) {
				winsideUrlPattern = buyURL + "/" +queryCoinsUrlPattern;
			}
			else {
				winsideUrlPattern = buyURL + queryCoinsUrlPattern;
			}
			String queryCoinsUrl = String.format(winsideUrlPattern, userid);
			log.debug("[Winside queryCoins Url] ==> "+queryCoinsUrl);
	    	HttpGet httpget = new HttpGet(queryCoinsUrl);
	    	String body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
	    	log.debug("[Winside queryCoins Result] ==> "+body);
	    	String[] result = body.split("#");
	    	if ("success".equalsIgnoreCase(result[0].trim())) {
	    		return Integer.parseInt(result[2].trim());
	    	}
	    	else {
	    		if ("-1".equals(result[1].trim())) {
	    			return 0;
	    		}
	    		else {
	    			throw new SubscribeException(getQueryCoinsErrorMessage(Integer.parseInt(result[1].trim())));
	    			//throw new SubscribeException(result[2].trim());
	    		}
	    	}
		}
		catch (Exception e) {
			throw new SubscribeException(e);
		}
	}
	
	public static ResultInfo gotoRechargePage(String buyUrl, String userId){
		ResultInfo info = new ResultInfo();
		try {
			String winsideUrlPattern = null;
			if (!buyUrl.endsWith("/")&&!consumeCoinsUrlPattern.startsWith("/")) {
				winsideUrlPattern = buyUrl + "/" +rechargePageUrlPatternWinside;
			}
			else {
				winsideUrlPattern = buyUrl + rechargePageUrlPatternWinside;
			}
			String url = String.format(winsideUrlPattern, userId);
			log.debug("[Winside consumeCoins Url] ==> "+url);
	    	HttpGet httpget = new HttpGet(url);
	    	String body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
	    	log.debug("[Winside consumeCoins Result] ==> "+body);
	    	String[] result = body.split("#");
	    	//info.setInfo(11);
	    	if (!"success".equalsIgnoreCase(result[0].trim())) {
	    		info.setErrorCode(ErrorCode.EC_GOTO_RECHARGE_PAGE);
	    		//info.setMessage(getConsumeCoinsErrorMessage(Integer.parseInt(result[1].trim())));
	    		info.setMessage(result[2].trim());
	    	}
	    	else {
	    		info.setInfo(result[0].trim());
	    	}
	    	return info;
		}
		catch (Exception e) {
			info.setErrorCode(ErrorCode.EC_GOTO_RECHARGE_PAGE);
    		info.setMessage("跳转充值界面失败，原因：请求地址有误");
    		return info;
			//throw new SubscribeException(e);
		}
	}
	
	public static String getRechargeErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -1: message = "指定的用户不存在"; break;
		case -2: message = "无效的用户名"; break;
		case -3: message = "您当前元宝数不足，无法执行相关操作"; break;
		case -4: message = "校验码不正确，请与GM联系"; break;
		case -5: message = "未知错误"; break;
		case -6: message = "您冲值过于频繁，请稍候再试"; break;
		case -7: message = "您已达到本月充值上限，请下个月再冲值"; break;
		case -8: message = "该用户禁止充值，请与GM联系"; break;
		case -9: message = "更新元宝失败，请与GM联系"; break;
		case -10: message = "参数缺失"; break;
		case -11: message = "无效的冲值金额"; break;
		case -12: message = "超出每日充值上限"; break;
		case -13: message = "超出充值上限"; break;
		case -14: message = "您已达到本月个人充值上限，请下个月再冲值"; break;
		case -15: message = "支付密码错误，请重新输入"; break;
		case -16: message = "产品名称不正确，请与GM联系"; break;
		default: message = "未知错误"; break;
		}
		return message;
	}
	
	public static String getRechargegdErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -5001: message = "指定的用户不存在"; break;
		case -5002: message = "无效的用户名"; break;
		case -5003: message = "您当前元宝数不足，无法执行相关操作"; break;
		case -5004: message = "校验码不正确，请与GM联系"; break;
		case -5005: message = "未知错误"; break;
		case -5006: message = "您冲值过于频繁，请稍候再试"; break;
		case -5007: message = "您已达到本月充值上限，请下个月再冲值"; break;
		case -5008: message = "该用户禁止充值，请与GM联系"; break;
		case -5009: message = "更新元宝失败，请与GM联系"; break;
		case -5010: message = "参数缺失"; break;
		case -5011: message = "无效的冲值金额"; break;
		case -5012: message = "超出每日充值上限"; break;
		case -5013: message = "超出充值上限"; break;
		case -5014: message = "您已达到本月个人充值上限，请下个月再冲值"; break;
		case -5015: message = "支付密码错误，请重新输入"; break;
		case -5016: message = "产品名称不正确，请与GM联系"; break;
		case -1: message = "用户id不正确"; break;
		case -2: message = "产品ID不正确"; break;
		case -3: message = "已超过月消费限额"; break;
		case -4: message = "sessionID 不正确"; break;
		case -5: message = "订单临时记录插入失败或者消费限额更新失败"; break;
		case -6: message = "订单临时记录更新失败或者正式订单插入失败"; break;
		case -7: message = "系统繁忙，请重新购买"; break;
		case -8: message = "您购买过于频繁，请在1分钟后再购买"; break;
		case -9: message = "用户验证失败"; break;
		case -10: message = "支付类型错误"; break;
		case -11: message = "spid不正确"; break;
		case -12: message = "code为空"; break;
		case -13: message = "code不正确"; break;
		case -14: message = "timeStmp不正确"; break;
		case -15: message = "请求超时"; break;
		case -16: message = "查询帐单出异常，需要再次请求"; break;
		case -17: message = "游戏ID出错，请重新充值"; break;
		case -18: message = "您已屏蔽了游戏订购功能，如需开通请拨打10000号"; break;
		case -19: message = "您是政企用户，无法进行订购"; break;
		case -23: message = "您的积分不足，无法订购"; break;
		case -24: message = "很抱歉，积分订购处理失败，请返回重试"; break;
		default: message = "未知错误"; break;
		}
		return message;
	}
	
	public static String getConsumeCoinsErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -1: message = "指定的用户不存在"; break;
		case -2: message = "无效的用户名"; break;
		case -3: message = "元宝数不足"; break;
		case -4: message = "校验码不正确，请与GM联系"; break;
		case -5: message = "未知错误"; break;
		case -7: message = "产品名称不正确，请与GM联系"; break;
		default: message = "未知错误";
		}
		return message;
	}
	
	public static String getQueryCoinsErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -1: message = "无效的用户标识"; break;
		default: message = "未知错误";
		}
		return message;
	}
}
