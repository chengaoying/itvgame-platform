package cn.ohyeah.itvgame.business.service.impl;

import cn.ohyeah.itvgame.platform.model.ProductDetail;

public class WinsideLackRechargeImpl extends WinsideRechargeImpl {
	@Override
	public boolean isSupportRecharge(ProductDetail detail) {
		return false;
	}
}
