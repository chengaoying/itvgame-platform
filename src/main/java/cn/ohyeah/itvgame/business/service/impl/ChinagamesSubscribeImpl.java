package cn.ohyeah.itvgame.business.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.halcyon.utils.RequestContext;
import cn.ohyeah.itvgame.business.ResultInfo;
import cn.ohyeah.itvgame.global.BeanManager;
import cn.ohyeah.itvgame.global.Configuration;
import cn.ohyeah.itvgame.platform.dao.IPurchaseRelationDao;
import cn.ohyeah.itvgame.platform.model.Account;
import cn.ohyeah.itvgame.platform.model.Authorization;
import cn.ohyeah.itvgame.platform.model.ProductDetail;
import cn.ohyeah.itvgame.platform.model.PurchaseRelation;
import cn.ohyeah.itvgame.utils.ToolUtil;

public class ChinagamesSubscribeImpl extends AbstractSubscribeImpl {
	private static final Log log = LogFactory.getLog(ChinagamesSubscribeImpl.class);
	private static final IPurchaseRelationDao prDao;
	static {
		prDao = (IPurchaseRelationDao)BeanManager.getDao("purchaseRelationDao");
	}
	
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
		String user_id = account.getUserId();
		String sp_id = Configuration.getProperty("telcomsh", "sp_id");
		String sp_key = Configuration.getProperty("telcomsh", "sp_key");
		String user_token = (String)props.get("userToken");
		String game_id = (String)props.get("gameid");
		int amount = pr.getAmount();
		//String des = (String)props.get("remark");
		if (account.isPrivilegeSuperUser()) {
			log.debug("≤‚ ‘’À∫≈≥‰÷µ[userId="+account.getUserId()+", amount="+ pr.getAmount()+", subImpl="+getImplementorName()+"]");
			ResultInfo info = new ResultInfo();
			info.setInfo(0);
			return info;
		}
		else {
			//String timeStamp = DateUtil.createTimeId(DateUtil.PATTERN_DEFAULT);
			long timestamp = System.currentTimeMillis();
			//String order_id =  user_id.substring(0, 8) + timestamp /*+ ToolUtil.getAutoincrementValue()*/;
			return ChinagamesSubscribeUtil.recharge(user_id, sp_id, game_id, timestamp, amount, sp_key, user_token);
		}
	}
	

	@Override
	public PurchaseRelation queryPurchaseRelation(ProductDetail detail,
			String subscribeType, int period, int amount) {
		PurchaseRelation pr =  prDao.read(detail.getProductId(), "telcomsh", subscribeType, period, 0/*amount*/);
		pr.setAmount(amount);
		pr.setSubscribeImplementor(getImplementorName());
		return pr;
	}

	@Override
	public ResultInfo expend(Map<String, Object> props, Account account,
			ProductDetail detail, Authorization auth, PurchaseRelation pr,
			String remark, Date time) {
		log.debug("[Subscribe Amount] ==> " + pr.getAmount());
		String userId = account.getUserId();
		String sp_id = Configuration.getProperty("telcomsh", "sp_id");
		String sp_key = Configuration.getProperty("telcomsh", "sp_key");
		String userToken = (String)props.get("userToken");
		String game_id = (String)props.get("gameid");
		int amount = pr.getAmount();
		String des = (String)props.get("remark");
		if (account.isPrivilegeSuperUser()) {
			log.debug("≤‚ ‘’À∫≈π∫¬Úµ¿æﬂ[userId="+account.getUserId()+", amount="+ pr.getAmount()+", subImpl="+getImplementorName()+"]");
			ResultInfo info = new ResultInfo();
			info.setInfo(0);
			return info;
		}
		else {
			//String timeStamp = DateUtil.createTimeId(DateUtil.PATTERN_DEFAULT);
			long timeStamp = System.currentTimeMillis();
			String order_id =  userId.substring(0, 8) + timeStamp + ToolUtil.getAutoincrementValue();
			return ChinagamesSubscribeUtil.consume(userId, sp_id, game_id, order_id, des, timeStamp, amount, sp_key,userToken);
		}
	}

	@Override
	public ResultInfo subscribeReqAction(RequestContext rc,
			Map<String, Object> props, String returnUrl, Account account,
			ProductDetail detail, PurchaseRelation pr, Date time)
			throws IOException {
		return null;
	}

	@Override
	public ResultInfo subscribeRspAction(RequestContext rc,
			Map<String, Object> props, Account account, ProductDetail detail,
			PurchaseRelation pr, String remark, Date time) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
