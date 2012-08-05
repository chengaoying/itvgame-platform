package cn.ohyeah.itvgame.business.service.impl;

import cn.ohyeah.itvgame.platform.model.ProductDetail;

/**
 * @deprecated replaced by {@link WinsideLackRechargeImpl}
 *
 */
@Deprecated public class WinsidefjRechargeImpl extends WinsideRechargeImpl {
	@Override
	public boolean isSupportRecharge(ProductDetail detail) {
		return false;
	}
}
