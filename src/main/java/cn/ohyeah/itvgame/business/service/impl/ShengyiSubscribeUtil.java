package cn.ohyeah.itvgame.business.service.impl;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sy.service.cpservice.action.TelecomServeStub;
import com.sy.service.cpservice.action.TelecomServeStub.ServiceOrder;
import com.sy.service.cpservice.action.TelecomServeStub.ServiceOrderForm;
import com.sy.service.cpservice.action.TelecomServeStub.ServiceOrderResponse;
import com.sy.service.cpservice.action.TelecomServeStub.ServiceOrderRsp;

import cn.ohyeah.itvgame.business.ErrorCode;
import cn.ohyeah.itvgame.business.ResultInfo;
import cn.ohyeah.itvgame.business.service.BusinessException;
import cn.ohyeah.itvgame.business.service.SubscribeException;

public class ShengyiSubscribeUtil {
	private static final Log log = LogFactory.getLog(ShengyiSubscribeUtil.class);
	
	public static ResultInfo recharge(String userId, String appId, int number, String feeCode, 
			  String returnUrl, String notifyUrl, String platformExt, String appExt){
		
		throw new BusinessException("not supported");
	}
	
	public static ResultInfo consumeCoins(String userId, String userToken, int amount,
									  String shengyiCPID, String shengyiCPPassWord, String shengyiUserIdType,
									  String shengyiProductId, String timeStamp, String transactionID){
		
		ServiceOrderForm form = new ServiceOrderForm();
		form.setUserID(userId);
		form.setUserToken(userToken);
		form.setPrice(amount);
		form.setCPID(shengyiCPID);
		form.setCPPassWord(shengyiCPPassWord);
		form.setUserIDType(Integer.parseInt(shengyiUserIdType));
		form.setProductID(shengyiProductId);
		form.setTimeStamp(timeStamp);
		form.setTransactionID(transactionID);
		ServiceOrder order = new ServiceOrder();
		order.setServiceOrderForm(form);
		try {
			TelecomServeStub  stub = new TelecomServeStub();
			// stub._getServiceClient().getOptions().setProperty(HTTPConstants.CHUNKED,"false"); 
			ServiceOrderResponse res = stub.serviceOrder(order);
			ServiceOrderRsp orderRsp = res.get_return();
			log.info("¶©¹º½á¹û£º"+orderRsp.getResult()+orderRsp.getDescription());
			ResultInfo info = new ResultInfo();
	    	//info.setInfo(11);
	    	if(orderRsp.getResult().equals("0")){
	    		info.setInfo(orderRsp.getServiceID());
	    	}else{
	    		info.setErrorCode(ErrorCode.EC_SUBSCRIBE_FAILED);
	    		info.setMessage(orderRsp.getResult()+orderRsp.getDescription());
	    	}
	    	return info;
			
		} catch (AxisFault e1) {
			throw new SubscribeException(e1);
		} catch (RemoteException e) {
			throw new SubscribeException(e);
		}
	}

}
