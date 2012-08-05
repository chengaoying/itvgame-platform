package cn.ohyeah.itvgame.business.service.impl;

import cn.ohyeah.itvgame.business.service.IPointsService;
import cn.ohyeah.itvgame.global.Configuration;

public class WinsidegdPointsServiceImpl implements IPointsService {
	private static final boolean supportPoints;
	private static final String pointsUnit;
	private static final int cashToPointsRatio;
	
	static {
		supportPoints = Configuration.isSupportPointsService("winsidegd");
		pointsUnit = Configuration.getPointsUnit("telcomgd");
		cashToPointsRatio = Configuration.getCashToPointsRatio("telcomgd");
	}
	
	@Override
	public boolean isSupportPointsService() {
		return supportPoints;
	}

	@Override
	public int queryAvailablePoints(String userId) {
		return 0;
	}

	@Override
	public String getPointsUnit() {
		return pointsUnit;
	}

	@Override
	public int getCashToPointsRatio() {
		return cashToPointsRatio;
	}

}
