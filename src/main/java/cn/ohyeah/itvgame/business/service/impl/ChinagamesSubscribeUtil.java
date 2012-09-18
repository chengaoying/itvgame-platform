package cn.ohyeah.itvgame.business.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import cn.halcyon.utils.ThreadSafeClientConnManagerUtil;
import cn.ohyeah.itvgame.business.ErrorCode;
import cn.ohyeah.itvgame.business.ResultInfo;
import cn.ohyeah.itvgame.business.service.SubscribeException;
import cn.ohyeah.itvgame.global.Configuration;

public class ChinagamesSubscribeUtil {
	private static final Log log = LogFactory.getLog(ChinagamesSubscribeUtil.class);
	private static final String subscribeReturnUrl = "http://127.0.0.1/notexist/subresult";
	private static final String subscribeUrl;
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
    			 if (StringUtils.startsWithIgnoreCase(location, subscribeReturnUrl)) {
	    			 int pos = StringUtils.indexOfIgnoreCase(location, "Result=", subscribeReturnUrl.length());
    				 if (pos > 0) {
    					 int npos = location.indexOf('&', pos);
    					 if (npos > 0) {
    						 body = location.substring(pos, npos);
    					 }
    					 else {
    						 body = location.substring(pos);
    					 }
    				 }
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
    	subscribeUrl = Configuration.getSubscribeUrl("chinagames");
    	try {
			encSubscribeReturnUrl = java.net.URLEncoder.encode(subscribeReturnUrl, "gbk");
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
	
    public static String getErrorMessage(int result) {
    	String message = null;
    	switch(result) {
    	case 9001:	message = "超过产品的单月消费限额"; break;
    	case 9002:	message = "超过用户的单月消费限额"; break;
    	default:	message = TelcomshSubscribeUtil.getErrorMessage(result);
    	}
    	return message;
    }
    
    private static String getUrlPrefix(String url) {
    	return url.substring(0, url.indexOf('/', "http://".length()));
    }
    
    private static String filterSubRedirect(String userId, String payType, String subUrlPre, String body, int deep) throws Exception{
    	return TelcomshSubscribeUtil.filterSubRedirect(userId, payType, subUrlPre, body, deep);
    }

    private static String execSubRequest(String subUrl) throws Exception {
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
}
