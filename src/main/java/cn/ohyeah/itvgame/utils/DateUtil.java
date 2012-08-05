package cn.ohyeah.itvgame.utils;

import java.util.Calendar;

public class DateUtil {
	public static java.sql.Timestamp convertToSqlDate(java.util.Date date) {
		return date==null?null:new java.sql.Timestamp(date.getTime());
	}
	
	public static java.sql.Timestamp convertToSqlDate(java.sql.Timestamp date) {
		return date;
	}
	
	public static java.util.Date convertToUtilDate(java.sql.Timestamp date) {
		return date==null?null:new java.util.Date(date.getTime());
	}
	
	public static java.util.Date convertToUtilDate(java.util.Date date) {
		return date;
	}
	
	public static java.util.Date getMonthStartTime(java.util.Date t) {
		Calendar c = Calendar.getInstance();
		c.setTime(t);
		c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
		c.set(Calendar.HOUR_OF_DAY, c.getActualMinimum(Calendar.HOUR_OF_DAY));
		c.set(Calendar.MINUTE, c.getActualMinimum(Calendar.MINUTE));
		c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
		c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
		return c.getTime();
	}
	
	public static java.util.Date getMonthEndTime(java.util.Date t) {
		Calendar c = Calendar.getInstance();
		c.setTime(t);
		c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
		c.set(Calendar.HOUR_OF_DAY, c.getActualMaximum(Calendar.HOUR_OF_DAY));
		c.set(Calendar.MINUTE, c.getActualMaximum(Calendar.MINUTE));
		c.set(Calendar.SECOND, c.getActualMaximum(Calendar.SECOND));
		c.set(Calendar.MILLISECOND, c.getActualMaximum(Calendar.MILLISECOND));
		return c.getTime();
	}
	
}
