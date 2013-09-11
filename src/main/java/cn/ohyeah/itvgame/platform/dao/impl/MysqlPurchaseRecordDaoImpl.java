package cn.ohyeah.itvgame.platform.dao.impl;

import java.util.List;

import cn.halcyon.dao.QueryHelper;
import cn.ohyeah.itvgame.platform.dao.IPurchaseRecordDao;
import cn.ohyeah.itvgame.platform.model.PurchaseRecord;
import cn.ohyeah.itvgame.platform.viewmodel.PurchaseDesc;
import cn.ohyeah.itvgame.platform.viewmodel.PurchaseStatis;

public class MysqlPurchaseRecordDaoImpl implements IPurchaseRecordDao {
	@Override
	public void save(PurchaseRecord pr) {
		QueryHelper.update("insert into PurchaseRecord" 
				+"(accountId, userId, productId, productName, propId, propName, " 
				+"propCount, amount, remark, time, ip) " 
				+"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 
				pr.getAccountId(), pr.getUserId(), pr.getProductId(), pr.getProductName(), 
				pr.getPropId(), pr.getPropName(), pr.getPropCount(), pr.getAmount(), 
				pr.getRemark(), pr.getTime(), pr.getIp());
		long id = QueryHelper.read(long.class, "select LAST_INSERT_ID()");
		pr.setId(id);
	}

	@Override
	public List<PurchaseDesc> queryPurchaseDescList(int accountId, int productId, int offset, int length) {
		return QueryHelper.query(PurchaseDesc.class, 
				"select propId, propName, propCount, amount, remark, time from PurchaseRecord " +
				"where accountId=? and productId=? order by time desc, accountid asc, productId asc limit ?, ?", 
				accountId, productId, offset, length);
	}

	@Override
	public long queryPurchaseRecordCount(int accountId, int productId) {
		return QueryHelper.read(long.class,
						"select count(*) from PurchaseRecord where accountId=? and productId=?", accountId, productId);
	}
	
	@Override
	public List<PurchaseStatis> queryPruchaseStatis(int productId, int offset, int lenght,String sTime, String eTime) {
		return QueryHelper.query(PurchaseStatis.class, 
				"select userId, sum(amount) as sum " +
				"from " +
				"(select userId, amount, time from PurchaseRecord where productId=? and (time > '"+sTime+"') and (time < '"+eTime+"')) as tb1 " +
				"group by userId "+
				"order by sum desc "+
				"limit ?,? ",
				productId, offset, lenght);
	}

}
