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
    		info.setMessage("����������ʧ�ܣ�ԭ�򣺵�ַ����");
    		return info;
			//throw new SubscribeException(e);
		}
	}
	
	public static String getRechargeErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -1: message = "ָ�����û�������"; break;
		case -2: message = "��Ч���û���"; break;
		case -3: message = "����ǰv�������㣬�޷�ִ����ز���"; break;
		case -4: message = "У���벻��ȷ������GM��ϵ"; break;
		case -5: message = "δ֪����"; break;
		case -6: message = "����ֵ����Ƶ�������Ժ�����"; break;
		case -7: message = "���Ѵﵽ���³�ֵ���ޣ����¸����ٳ�ֵ"; break;
		case -8: message = "���û���ֹ��ֵ������GM��ϵ"; break;
		case -9: message = "����v��ʧ�ܣ�����GM��ϵ"; break;
		case -10: message = "����ȱʧ"; break;
		case -11: message = "��Ч�ĳ�ֵ���"; break;
		case -12: message = "����ÿ�ճ�ֵ����"; break;
		case -13: message = "������ֵ����"; break;
		case -14: message = "���Ѵﵽ���¸��˳�ֵ���ޣ����¸����ٳ�ֵ"; break;
		case -15: message = "֧�������������������"; break;
		case -16: message = "��Ʒ���Ʋ���ȷ������GM��ϵ"; break;
		case -17: message = "��Ч����"; break;
		case -18: message = "��Ȩʧ�ܣ������¿��������к�����"; break;
		case -19: message = "����ֵ����ʧ��"; break;
		case -20: message = "��������ͻ���ϵͳ�ݲ��ṩ�����û����߳�ֵ"; break;
		case 9103: message = "֧�������������������"; break;
		default: message = "δ֪����"; break;
		}
		return message;
	}
	
	public static String getConsumeCoinsErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -1: message = "ָ�����û�������"; break;
		case -2: message = "��Ч���û���"; break;
		case -3: message = "v��������"; break;
		case -4: message = "У���벻��ȷ������GM��ϵ"; break;
		case -5: message = "δ֪����"; break;
		default: message = "δ֪����";
		}
		return message;
	}
	
	public static String getQueryCoinsErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -1: message = "���û�δ���ֵ"; break;
		default: message = "δ֪����";
		}
		return message;
	}
}
