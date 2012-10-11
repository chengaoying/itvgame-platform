package cn.ohyeah.itvgame.business.service.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkage.lcsmp.interfaces.server.ws.vas.spsys.SPSysInterface;
import com.linkage.lcsmp.interfaces.server.ws.vas.spsys.SPSysInterfaceServiceLocator;
import com.linkage.lcsmp.interfaces.server.ws.vas.spsys.req.ServiceOrderReq;

import cn.halcyon.utils.ThreadSafeClientConnManagerUtil;
import cn.ohyeah.itvgame.business.ErrorCode;
import cn.ohyeah.itvgame.business.ResultInfo;
import cn.ohyeah.itvgame.business.model.DijoyResponseEntry;
import cn.ohyeah.itvgame.business.service.BusinessException;
import cn.ohyeah.itvgame.business.service.SubscribeException;

public class ShengyiSubscribeUtil {
	private static final Log log = LogFactory.getLog(WinsideSubscribeUtil.class);
	private static final DefaultHttpClient httpClient;
	
	static {
		httpClient = ThreadSafeClientConnManagerUtil.buildDefaultHttpClient();
	}
	
	public static ResultInfo recharge(String userId, String appId, int number, String feeCode, 
			  String returnUrl, String notifyUrl, String platformExt, String appExt){
		
		throw new BusinessException("not supported");
	}
	
	public static ResultInfo consumeCoins(String userId, String userToken, int amount,
									  String shengyiCPID, String shengyiCPPassWord, String shengyiUserIdType,
									  String shengyiProductId, String timeStamp, String transactionID){
		
		ServiceOrderReq orderReq = new ServiceOrderReq();
		orderReq.setUserID(userId);
		orderReq.setUserToken(userToken);
		orderReq.setPrice(amount);
		orderReq.setSPID(shengyiCPID);
		orderReq.setUserIDType(Integer.parseInt(shengyiUserIdType));
		orderReq.setProductID(shengyiProductId);
		orderReq.setTimeStamp(timeStamp);
		orderReq.setTransactionID(transactionID);
		
		try {
			SPSysInterfaceServiceLocator locator = new SPSysInterfaceServiceLocator();
			SPSysInterface sys = locator.getSPSysInterface();
			sys.serviceOrder(orderReq);
		} catch (ServiceException e1) {
			e1.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		try {
			
	    	ResultInfo info = new ResultInfo();
	    	//info.setInfo(11);
	    	//if(entry.getPayResult()==0){
	    		//info.setInfo(entry.getSum());
	    	//}else{
	    		//info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
	    	//	//info.setMessage(getRechargegdErrorMessage(entry.getPayResult())+"("+String.valueOf(entry.getPayResult())+")");
	    	//}
	    	return info;
		} catch (Exception e) {
			throw new SubscribeException(e);
		}
	}
	
	public static String getRechargegdErrorMessage(int errorCode) {
		String message = null;
		switch (errorCode) {
		case 2: message = "业务协议不能解析"; break;
		case 3: message = "业务协议不符合规范"; break;
		case 4: message = "业务协议不符合规范"; break;
		case 5: message = "业务没有定义"; break;
		case 6: message = "请求系统服务超时"; break;
		case 7: message = "系统服务返回的结果不符合规范"; break;
		case 8: message = "系统服务返回的数据不符合规范"; break;
		case 9: message = "请求业务的数据类型不符合规范"; break;
		case 13: message = "数据库错误"; break;
		case 70005: message = "无可用的网关服务器"; break;
		case 70002: message = "上次登录验证请求正在处理中"; break;
		case 70001: message = "账号状态不可用"; break;
		case 70000: message = "账号或密码错误"; break;
		case 60006: message = "兑换失败！单号不存在"; break;
		case 60005: message = "兑换失败！单号格式错误。"; break;
		case 60004: message = "兑换失败！单号重复。"; break;
		case 60003: message = "支付项目不存在"; break;
		case 60002: message = "支付密钥不存在"; break;
		case 60001: message = "此应用不存在或已下架"; break;
		case 60000: message = "兑换失败！数据库错误。"; break;
		case 41001: message = "数据库错误"; break;
		case 41000: message = "请求的内容不存在"; break;
		case 40009: message = "数据库格式错误"; break;
		case 40001: message = "数据库错误"; break;
		case 30000: message = "没有查找到数据"; break;
		case 30011: message = "存款订单号重复"; break;
		case 30012: message = "存款账户不存在。"; break;
		case 30013: message = "充值超过上限"; break;
		case 30021: message = "取款订单号重复"; break;
		case 30022: message = "取款账户不存在。"; break;
		case 30023: message = "取款金额不足。"; break;
		case 30024: message = "超过单次支付上限"; break;
		case 30025: message = "余额不足，请到广电营业厅充值，详情咨询96296。"; break;
		case 20005: message = "没有找到应用"; break;
		case 20004: message = "没有找到目录"; break;
		case 20003: message = "购买数量超过限制"; break;
		case 20000: message = "没有查找到数据"; break;
		case 10000: message = "没有查找到数据"; break;
		case 2001: message = "页面参数传递错误"; break;
		case 1005: message = "读取缓存错误"; break;
		case 1004: message = "认证信息错误"; break;
		case 1003: message = "请求业务的数据类型不符合规范"; break;
		case 1002: message = "用户数据缺失"; break;
		case 1001: message = "认证信息缺失"; break;
		case 1000: message = "后台请求失败"; break;
		default: message = "余额不足，请到广电营业厅充值，详情咨询96296"; break;
		}
		return message;
	}

}
