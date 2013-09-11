package cn.ohyeah.itvgame.platform.dao;

import java.util.List;

import cn.ohyeah.itvgame.platform.model.PurchaseRecord;
import cn.ohyeah.itvgame.platform.viewmodel.PurchaseDesc;
import cn.ohyeah.itvgame.platform.viewmodel.PurchaseStatis;

public interface IPurchaseRecordDao {
	public void save(PurchaseRecord pr);
	public List<PurchaseDesc> queryPurchaseDescList(int accountId, int productId, int offset, int length);
	public long queryPurchaseRecordCount(int accountId, int productId);
	public List<PurchaseStatis> queryPruchaseStatis(int productId, int offset, int lenght, String sTime, String eTime);
}
