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
    		info.setMessage("��ת��ֵ����ʧ�ܣ�ԭ�������ַ����");
    		return info;
			//throw new SubscribeException(e);
		}
	}
	
	public static String getRechargeErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -1: message = "ָ�����û�������"; break;
		case -2: message = "��Ч���û���"; break;
		case -3: message = "����ǰԪ�������㣬�޷�ִ����ز���"; break;
		case -4: message = "У���벻��ȷ������GM��ϵ"; break;
		case -5: message = "δ֪����"; break;
		case -6: message = "����ֵ����Ƶ�������Ժ�����"; break;
		case -7: message = "���Ѵﵽ���³�ֵ���ޣ����¸����ٳ�ֵ"; break;
		case -8: message = "���û���ֹ��ֵ������GM��ϵ"; break;
		case -9: message = "����Ԫ��ʧ�ܣ�����GM��ϵ"; break;
		case -10: message = "����ȱʧ"; break;
		case -11: message = "��Ч�ĳ�ֵ���"; break;
		case -12: message = "����ÿ�ճ�ֵ����"; break;
		case -13: message = "������ֵ����"; break;
		case -14: message = "���Ѵﵽ���¸��˳�ֵ���ޣ����¸����ٳ�ֵ"; break;
		case -15: message = "֧�������������������"; break;
		case -16: message = "��Ʒ���Ʋ���ȷ������GM��ϵ"; break;
		default: message = "δ֪����"; break;
		}
		return message;
	}
	
	public static String getRechargegdErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -5001: message = "ָ�����û�������"; break;
		case -5002: message = "��Ч���û���"; break;
		case -5003: message = "����ǰԪ�������㣬�޷�ִ����ز���"; break;
		case -5004: message = "У���벻��ȷ������GM��ϵ"; break;
		case -5005: message = "δ֪����"; break;
		case -5006: message = "����ֵ����Ƶ�������Ժ�����"; break;
		case -5007: message = "���Ѵﵽ���³�ֵ���ޣ����¸����ٳ�ֵ"; break;
		case -5008: message = "���û���ֹ��ֵ������GM��ϵ"; break;
		case -5009: message = "����Ԫ��ʧ�ܣ�����GM��ϵ"; break;
		case -5010: message = "����ȱʧ"; break;
		case -5011: message = "��Ч�ĳ�ֵ���"; break;
		case -5012: message = "����ÿ�ճ�ֵ����"; break;
		case -5013: message = "������ֵ����"; break;
		case -5014: message = "���Ѵﵽ���¸��˳�ֵ���ޣ����¸����ٳ�ֵ"; break;
		case -5015: message = "֧�������������������"; break;
		case -5016: message = "��Ʒ���Ʋ���ȷ������GM��ϵ"; break;
		case -1: message = "�û�id����ȷ"; break;
		case -2: message = "��ƷID����ȷ"; break;
		case -3: message = "�ѳ����������޶�"; break;
		case -4: message = "sessionID ����ȷ"; break;
		case -5: message = "������ʱ��¼����ʧ�ܻ��������޶����ʧ��"; break;
		case -6: message = "������ʱ��¼����ʧ�ܻ�����ʽ��������ʧ��"; break;
		case -7: message = "ϵͳ��æ�������¹���"; break;
		case -8: message = "���������Ƶ��������1���Ӻ��ٹ���"; break;
		case -9: message = "�û���֤ʧ��"; break;
		case -10: message = "֧�����ʹ���"; break;
		case -11: message = "spid����ȷ"; break;
		case -12: message = "codeΪ��"; break;
		case -13: message = "code����ȷ"; break;
		case -14: message = "timeStmp����ȷ"; break;
		case -15: message = "����ʱ"; break;
		case -16: message = "��ѯ�ʵ����쳣����Ҫ�ٴ�����"; break;
		case -17: message = "��ϷID���������³�ֵ"; break;
		case -18: message = "������������Ϸ�������ܣ����迪ͨ�벦��10000��"; break;
		case -19: message = "���������û����޷����ж���"; break;
		case -23: message = "���Ļ��ֲ��㣬�޷�����"; break;
		case -24: message = "�ܱ�Ǹ�����ֶ�������ʧ�ܣ��뷵������"; break;
		default: message = "δ֪����"; break;
		}
		return message;
	}
	
	public static String getConsumeCoinsErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -1: message = "ָ�����û�������"; break;
		case -2: message = "��Ч���û���"; break;
		case -3: message = "Ԫ��������"; break;
		case -4: message = "У���벻��ȷ������GM��ϵ"; break;
		case -5: message = "δ֪����"; break;
		case -7: message = "��Ʒ���Ʋ���ȷ������GM��ϵ"; break;
		default: message = "δ֪����";
		}
		return message;
	}
	
	public static String getQueryCoinsErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case -1: message = "��Ч���û���ʶ"; break;
		default: message = "δ֪����";
		}
		return message;
	}
}
