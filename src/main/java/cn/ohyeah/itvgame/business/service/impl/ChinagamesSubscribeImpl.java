package cn.ohyeah.itvgame.business.service.impl;

import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.ohyeah.itvgame.business.ResultInfo;
import cn.ohyeah.itvgame.global.Configuration;
import cn.ohyeah.itvgame.platform.model.Account;
import cn.ohyeah.itvgame.platform.model.ProductDetail;
import cn.ohyeah.itvgame.platform.model.PurchaseRelation;

public class ChinagamesSubscribeImpl extends TelcomshSubscribeImpl {
	private static final Log log = LogFactory.getLog(ChinagamesSubscribeImpl.class);
	
	public ChinagamesSubscribeImpl() {
		super("chinagames");
	}
	
	protected ChinagamesSubscribeImpl(String implName) {
		super(implName);
	}
	
	@Override
	public ResultInfo subscribeAction(Map<String, Object> props,
			Account account, ProductDetail detail, PurchaseRelation pr,
			String remark, Date time) {
		log.debug("[Subscribe Amount] ==> " + pr.getAmount());
		String userToken = (String)props.get("userToken");
		if (account.isPrivilegeSuperUser()) {
			log.debug("≤‚ ‘’À∫≈∂©π∫[userId="+account.getUserId()+", amount="+ pr.getAmount()+", subImpl="+getImplementorName()+"]");
			return new ResultInfo();
		}
		else {
			return ChinagamesSubscribeUtil.subscribe(account.getUserId(), pr.getSubscribeId(), userToken, 
					Configuration.getSpid(), Integer.toString((Integer)props.get("payType")));
		}
	}

	@Override
	public PurchaseRelation queryPurchaseRelation(ProductDetail detail,
			String subscribeType, int period, int amount) {
		PurchaseRelation pr =  super.queryPurchaseRelation(detail, subscribeType, period, amount);
		pr.setSubscribeImplementor(getImplementorName());
		return pr;
	}
	
	
}
