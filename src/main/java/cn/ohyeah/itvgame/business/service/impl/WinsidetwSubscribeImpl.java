package cn.ohyeah.itvgame.business.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import cn.ohyeah.itvgame.global.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.halcyon.utils.RequestContext;
import cn.ohyeah.itvgame.business.ResultInfo;
import cn.ohyeah.itvgame.business.service.BusinessException;
import cn.ohyeah.itvgame.global.BeanManager;
import cn.ohyeah.itvgame.platform.dao.IPurchaseRelationDao;
import cn.ohyeah.itvgame.platform.model.Account;
import cn.ohyeah.itvgame.platform.model.Product;
import cn.ohyeah.itvgame.platform.model.ProductDetail;
import cn.ohyeah.itvgame.platform.model.PurchaseRelation;

public class WinsidetwSubscribeImpl extends AbstractSubscribeImpl {
	private static final Log log = LogFactory.getLog(WinsidetwSubscribeImpl.class);
	private static final IPurchaseRelationDao prDao;
	
	static {
		prDao = (IPurchaseRelationDao)BeanManager.getDao("purchaseRelationDao");
	}
	
	public WinsidetwSubscribeImpl() {
		super("winsidetw");
	}
	
	protected WinsidetwSubscribeImpl(String implName) {
		super(implName);
	}
	
	@Override
	public ResultInfo subscribeAction(Map<String, Object> props,
			Account account, ProductDetail detail, PurchaseRelation pr,
			String remark, Date time) {
		if ("recharge".equalsIgnoreCase(pr.getSubscribeType())) {
			return recharge(props, account, detail, pr, remark);
		}
		else {
			return expend(props, account, detail, pr, remark);
		}
	}
	
	protected ResultInfo recharge(Map<String, Object> props, Account account,
			ProductDetail detail, PurchaseRelation pr, String remark) {
		String buyURL = (String)props.get("buyURL");
		String userid = account.getUserId();
		String spid = (String)props.get("spid");
		String userToken = (String)props.get("userToken");
		String username = (String)props.get("accountName");
		String checkKey = (String)props.get("checkKey");
		Product product = (Product)props.get("product");
		String password = (String)props.get("password");
		return WinsideSubscribeUtil.recharge(buyURL, spid, userid, username, pr.getAmount(), product.getAppName(), userToken, checkKey, password);
	}
	
	protected ResultInfo expend(Map<String, Object> props, Account account,
			ProductDetail detail, PurchaseRelation pr, String remark) {
		String buyURL = (String)props.get("buyURL");
		String userid = account.getUserId();
		String username = (String)props.get("accountName");
		String checkKey = (String)props.get("checkKey");
		Product product = (Product)props.get("product");
		int costAmount = pr.getAmount()*getCashToAmountRatio(detail);
		log.debug("[Subscribe Amount] ==> "+costAmount);
		return WinsideSubscribeUtil.consumeCoins(buyURL, userid, 
				username, checkKey, product.getAppName(), remark, 1, costAmount);
	}

	@Override
	public ResultInfo subscribeReqAction(RequestContext rc,
			Map<String, Object> props, String returnUrl, Account account,
			ProductDetail detail, PurchaseRelation pr, Date time)
			throws IOException {
		throw new BusinessException("not supported");
	}

	@Override
	public ResultInfo subscribeRspAction(RequestContext rc,
			Map<String, Object> props, Account account, ProductDetail detail,
			PurchaseRelation pr, String remark, Date time) {
		throw new BusinessException("not supported");
	}

	@Override
	public PurchaseRelation queryPurchaseRelation(ProductDetail detail,
			String subscribeType, int period, int amount) {
		String subType = subscribeType;
		if ("recharge".equals(subscribeType)
                && (Configuration.isRechargeManagerGame(detail.getAppName())
                || Configuration.isRechargeManagerPlatform(detail.getAppName()))) {
			subType = "expend";
		}
		PurchaseRelation pr = prDao.read(detail.getProductId(), "winside", subType, 0, 0);
		if (pr != null) {
			pr.setValue(period);
			pr.setAmount(amount);
		}
		return pr;
	}
}
