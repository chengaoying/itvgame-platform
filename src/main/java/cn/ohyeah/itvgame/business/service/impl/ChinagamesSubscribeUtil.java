package cn.ohyeah.itvgame.business.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.halcyon.utils.ThreadSafeClientConnManagerUtil;
import cn.ohyeah.itvgame.business.ErrorCode;
import cn.ohyeah.itvgame.business.ResultInfo;
import cn.ohyeah.itvgame.business.model.TelcomshResponseEntry;
import cn.ohyeah.itvgame.business.service.SubscribeException;
import cn.ohyeah.itvgame.global.Configuration;
import cn.ohyeah.itvgame.utils.ToolUtil;

public class ChinagamesSubscribeUtil {
	private static final Log log = LogFactory.getLog(ChinagamesSubscribeUtil.class);
	private static final String subscribeReturnUrl; /*= "http://127.0.0.1/notexist/subresult"*/
	private static final String subscribeUrl;
	private static final String rechargeUrl;
	private static String encSubscribeReturnUrl;
	private static DefaultHttpClient httpClient;
    
    public static ResponseHandler<String> bodyHandler = new ResponseHandler<String>() {
        public String handleResponse(HttpResponse response) 
        		throws ClientProtocolException, IOException {
        	String body = null;
        	int responseCode = response.getStatusLine().getStatusCode();
    		if (responseCode == 301 || responseCode == 302) {
    			 Header locationHeader = response.getFirstHeader("location");
    			 String location = locationHeader.getValue();
    			 log.debug("subscribe returnUrl===>"+location);
    			 if (StringUtils.startsWithIgnoreCase(location, subscribeReturnUrl)) {
    				 body = location.substring(subscribeReturnUrl.length()+1, location.length());
    				 log.debug("subscribe return info===>"+body);
	    			 /*int pos = StringUtils.indexOfIgnoreCase(location, "Result=", subscribeReturnUrl.length());
    				 if (pos > 0) {
    					 int npos = location.indexOf('&', pos);
    					 if (npos > 0) {
    						 body = location.substring(pos, npos);
    					 }
    					 else {
    						 body = location.substring(pos);
    					 }
    				 }*/
    			 }
            }
    		else {
    			HttpEntity entity = response.getEntity();
    			if (entity != null) {
                    body = EntityUtils.toString(entity);
                }
    		}
    		return body;
        }
    };
    
    static {
    	//subscribeUrl = Configuration.getSubscribeUrl("chinagames");
    	subscribeUrl = Configuration.getProperty("telcomsh", "expenseUrl");
    	rechargeUrl = Configuration.getProperty("telcomsh", "rechargeUrl");
    	subscribeReturnUrl = Configuration.getProperty("telcomsh", "serverUrl");
    	try {
			encSubscribeReturnUrl = java.net.URLEncoder.encode(subscribeReturnUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("URL编码错误", e);
		}
    	init();
    }

	private static void init() {
		httpClient = ThreadSafeClientConnManagerUtil.buildDefaultHttpClient();
		httpClient.setRedirectStrategy(new DefaultRedirectStrategy() {
			@Override
	        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException  {
	            try {
	            	boolean isRedirect = super.isRedirected(request, response, context);
	                if (isRedirect) {
	                	Header locationHeader = response.getFirstHeader("location");
	                    if (locationHeader != null) {
	                    	String location = locationHeader.getValue();
	                    	if (StringUtils.startsWithIgnoreCase(location, subscribeReturnUrl)) {
	                    		isRedirect = false;
	                    	}
	                    }
		            }
		            return isRedirect;
	            } catch (ProtocolException e) {
	                log.error("Error occured when redirect determine", e);
	                throw e;
	            }
	        }
	    });
	}
	
	/**
	 * @param result
	 * @return
	 */
    public static String getErrorMessage(int result) {
    	String message = null;
    	switch(result) {
    	case 9001:	message = "超过产品的单月消费限额"; break;
    	case 9002:	message = "超过用户的单月消费限额"; break;
    	default:	message = TelcomshSubscribeUtil.getErrorMessage(result);
    	}
    	return message;
    }
    
    private static String getErrorInfo(int result){
    	String message = null;
    	switch(result) {
    	case -1:	message = "余额不足"; break;
    	case -2:	message = "用户为黑名单不允许消费"; break;
    	case -3:	message = "MD5校验错误"; break;
    	case -4:	message = "SP不存在"; break;
    	case -5:	message = "游戏不存在"; break;
    	case -6:	message = "接口版本不可用"; break;
    	case -7:	message = "参数错误"; break;
    	case -8:	message = "订单号已存在"; break;
    	case -99:	message = "系统内部错误"; break;
    	default:	message = "未知错误："+result;
    	}
    	return message;
    }
    
    private static String getReturnMessage(int result){
    	String message = null;
    	switch(result) {
    	case -1:	message = "充值余额不可用"; break;
    	case -2:	message = "UserID和UserToken验证错误"; break;
    	case -3:	message = "MD5校验错误"; break;
    	case -4:	message = "SP不存在"; break;
    	case -5:	message = "游戏不存在"; break;
    	case -6:	message = "接口版本不可用"; break;
    	case -7:	message = "参数错误"; break;
    	case -8:	message = "用户充值已达充值限额"; break;
    	case -99:	message = "系统内部错误"; break;
    	default:	message = "未知错误："+result;
    	}
    	return message;
    }
    
    private static String getUrlPrefix(String url) {
    	return url.substring(0, url.indexOf('/', "http://".length()));
    }
    
    private static String filterSubRedirect(String userId, String payType, String subUrlPre, String body, int deep) throws Exception{
    	return TelcomshSubscribeUtil.filterSubRedirect(userId, payType, subUrlPre, body, deep);
    }

    public static String execSubRequest(String subUrl) throws Exception {
    	HttpGet httpget = new HttpGet(subUrl);
    	return ThreadSafeClientConnManagerUtil.execute(httpClient, httpget, bodyHandler);
    }
    
    private static String extractSubResult(String url) throws Exception {
    	return TelcomshSubscribeUtil.extractSubResult(url);
    }
    
    private static String extractBackUrl(String body) throws Exception {
    	return TelcomshSubscribeUtil.extractBackUrl(body);
    }
    
    /*额外发送一次请求，使中游大厅记录订购结果*/
    private static void execStatRequest(String url) {
		try {
			HttpGet httpget = new HttpGet(url);
			ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, httpget);
		}
		catch (Exception e) {
			//由于这个请求只跟对账有关，失败后不作任何处理
		}
    }

	public static ResultInfo subscribe(String userId, String subscribeId, String userToken, String spid, String payType) {
		ResultInfo info = new ResultInfo();
        try {
        	String subUrlPre = getUrlPrefix(subscribeUrl);
            String subUrl = String.format(subscribeUrl, userId, subscribeId, userToken, spid, encSubscribeReturnUrl);
            log.debug("[subscribe url] ==> "+subUrl);
            String body = execSubRequest(subUrl);
            if (body == null) {
            	info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
				info.setMessage("无法获取电信订购重定向页面");
            }
            else {
            	if (StringUtils.startsWithIgnoreCase(body, "Result=")) {
            		String result = body.substring("Result=".length());
            		log.debug("[subscribe result] ==> "+result);
            		int rst = Integer.parseInt(result);
            		info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
					info.setMessage(getErrorMessage(rst));
            	}
            	else {
		            body = filterSubRedirect(userId, payType, subUrlPre, body, 3);
		            log.debug("return body ===>"+body);
		            if (body == null) {
		            	info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
						info.setMessage("无法解析电信订购确认页面");
		            }
		            else {
		            	String backUrl = extractBackUrl(body);
		            	log.debug("[backUrl ===>]"+backUrl);
			            String result = extractSubResult(backUrl);
			            execStatRequest(backUrl);   
			            log.debug("[subscribe result] ==> "+result);
			            if (result != null) {
			            	try {
			            		int rst = Integer.parseInt(result);
			            		if (rst != 0) {
			            			info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
									info.setMessage(getErrorMessage(rst));
			            		}
			            	}
			            	catch (NumberFormatException e) {
			            		info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
								info.setMessage("无法解析电信订购结果链接，订购结果不为整数");
			            	}
			            }
			            else {
			            	info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
							info.setMessage("无法解析电信订购结果链接");
			            }
		            }
            	}
            }
		} catch (Exception e) {
			throw new SubscribeException(e);
		}
		return info;
	}
	
	public static ResultInfo recharge(String user_id, String sp_id, String game_id, 
			long timestamp, int amount, String sp_key, String user_token){
		//sp_id=%s&game_id=%s&amount=%s&timestamp=%s&user_id=%s&user_token=%s&version=%s&digest=%s
		String version = Configuration.getProperty("telcomsh", "expenseVersion");
		//amount game_id sp_id timestamp user_id user_token version sp_key
		String digest = DigestUtils.md5Hex(amount 
				   + game_id
				   + sp_id 
				   + timestamp 
				   + user_id
				   + user_token
				   + version 
				   + sp_key);
		String url = String.format(rechargeUrl, sp_id, game_id, amount,timestamp,user_id,user_token,version,digest);
		log.debug("rechargeUrl ==>>" + url);
		ResultInfo info = new ResultInfo();
		try{
			HttpGet get = new HttpGet(url);
			String body = ThreadSafeClientConnManagerUtil.executeForBodyString(httpClient, get);
			log.debug("return message ==>>" + body);
			ObjectMapper op = new ObjectMapper();
	    	JsonNode node = op.readValue(body, JsonNode.class);
	    	TelcomshResponseEntry entry = new TelcomshResponseEntry();
	    	entry.setResult(Integer.parseInt(formatString(String.valueOf(node.get("result")))));
	    	entry.setMessage(formatString(String.valueOf(node.get("message"))));
	    	entry.setTimestamp(Long.parseLong(formatString(String.valueOf(node.get("timestamp")))));
	    	entry.setDigest(formatString(String.valueOf(node.get("digest"))));
	    	if(entry.getResult() == 0){
	    		info.setInfo(0);
	    	}else{
	    		info.setErrorCode(entry.getResult());
	    		info.setMessage(getReturnMessage(entry.getResult()));
	    	}
	    	return info;
		}catch(Exception e){
			throw new SubscribeException(e);
		}
	}
	
	private static String formatString(String str){
		if(str.contains("\"")){
			str = str.substring(1, str.length()-1);
		}
		return str;
	}
	
	public static ResultInfo consume(String user_id, String sp_id, String game_id, String order_id, String description, 
			long timestamp, int amount, String sp_key, String userToken){
		ResultInfo info = new ResultInfo();
        try {
        	String version = Configuration.getProperty("telcomsh", "expenseVersion");
        	//String notify_url = null;
        	String digest = DigestUtils.md5Hex(amount 
        									   + description 
        									   + game_id
        									   //+ notify_url
        									   + order_id 
        									   + subscribeReturnUrl 
        									   + sp_id 
        									   + timestamp 
        									   + version 
        									   + sp_key);
            String subUrl = String.format(subscribeUrl,sp_id, game_id, order_id, java.net.URLEncoder.encode(description, "UTF-8"), timestamp,encSubscribeReturnUrl,/*notify_url,*/amount,version,digest);
            //subUrl += "&ran="+ToolUtil.getAutoincrementValue();
            log.debug("[subscribe url] ==> "+subUrl);
            
            Element e = Jsoup.connect(subUrl).get();
    		e = submitFirstForm1(e,userToken);
    	
    		e = submitFirstForm2(e);
    		e = submitFirstForm2(e);
    		
    		log.debug("[body] ==> "+e.html());
    		Element div = e.getElementsByTag("div").get(0);
    		int result = Integer.parseInt(String.valueOf(div.attr("name")));
    		if(result == 0){
    			info.setInfo(0);
    		}else{
    			info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
    			info.setMessage(getErrorInfo(result));
    		}
            
		} catch (Exception e) {
			throw new SubscribeException(e);
		}
		return info;
	}
	
	private static Element submitFirstForm1(Element e,String userToken){
		Element form = e.getElementsByTag("form").get(0);
//		String url = form.attr("action");
		String url = "http://124.75.29.164:7001/iptv3a/VASGetUserinfoMoreAction.do";
		Elements paramsE = form.select("input[name]");
		Map<String, String> params = new HashMap<String, String>();
		for (Element element : paramsE)
		{
			String key = element.attr("name");
			String value = element.attr("value");
			if("usertoken".equals(key)){
				params.put(key, userToken);
			}else{
				params.put(key, value);
			}
			
		}
		Element result =null;
		try {
			result = Jsoup.connect(url).data(params).post();
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		return result;
	}
	private static Element submitFirstForm2(Element e){
		Element form = e.getElementsByTag("form").get(0);
		String url = form.attr("action");
		Elements paramsE = form.select("input[name]");
		Map<String, String> params = new HashMap<String, String>();
		for (Element element : paramsE)
		{
			String key = element.attr("name");
			String value = element.attr("value");
			params.put(key, value);
		}
		Element result =null;
		try {
			result = Jsoup.connect(url).data(params).post();
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		return result;
	}
	
	/*private static Element submitFirstForm3(Element e) throws UnsupportedEncodingException{
		Element form = e.getElementsByTag("form").get(0);
		String url = form.attr("action");
		Elements paramsE = form.select("input[name]");
		Map<String, String> params = new HashMap<String, String>();
		for (Element element : paramsE)
		{
			String key = element.attr("name");
			String value = URLEncoder.encode(element.attr("value"), "UTF-8");
			params.put(key, value);
		}
		Element result =null;
		try {
			result = Jsoup.connect(url).data(params).get();
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		return result;
	}*/
	
}
