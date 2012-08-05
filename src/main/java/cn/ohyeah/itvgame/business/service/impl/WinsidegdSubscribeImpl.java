package cn.ohyeah.itvgame.business.service.impl;

import java.util.Map;

import cn.ohyeah.itvgame.business.ResultInfo;
import cn.ohyeah.itvgame.global.Configuration;
import cn.ohyeah.itvgame.platform.model.Account;
import cn.ohyeah.itvgame.platform.model.Product;
import cn.ohyeah.itvgame.platform.model.ProductDetail;
import cn.ohyeah.itvgame.platform.model.PurchaseRelation;
import cn.ohyeah.itvgame.platform.model.SubscribePayType;

public class WinsidegdSubscribeImpl extends WinsideSubscribeImpl {
	//private static final Log log = LogFactory.getLog(WinsidegdSubscribeImpl.class);
	
	public WinsidegdSubscribeImpl() {
		super("winsidegd");
	}
	
	protected WinsidegdSubscribeImpl(String implName) {
		super(implName);
	}
	
	@Override
	protected ResultInfo recharge(Map<String, Object> props, Account account,
			ProductDetail detail, PurchaseRelation pr, String remark) {
		String buyURL = (String)props.get("buyURL");
		String userid = account.getUserId();
		String spid = (String)props.get("spid");
		String gameid = (String)props.get("gameid");
		String enterURL = (String)props.get("enterURL");
		String zyUserToken = (String)props.get("userToken");
		String stbType = (String)props.get("stbType");
		Integer payType = (Integer)props.get("payType");
		String username = (String)props.get("accountName");
		String checkKey = (String)props.get("checkKey");
		Product product = (Product)props.get("product");
		int amount = pr.getAmount();
		if (payType == SubscribePayType.PAY_TYPE_POINTS) {
			amount *= Configuration.getTelcomOperatorCashToPointsRatio();
		}
		String password = (String)props.get("password");
		return WinsideSubscribeUtil.rechargeGd(buyURL, spid, userid, username, amount, 
				product.getAppName(), gameid, enterURL, zyUserToken, stbType, payType.toString(), checkKey, password);
	}

}
