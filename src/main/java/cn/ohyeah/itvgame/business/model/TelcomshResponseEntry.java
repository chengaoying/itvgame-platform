package cn.ohyeah.itvgame.business.model;

public class TelcomshResponseEntry {

	//{"result":"-7","message":"sp_id²ÎÊıÎª¿Õ£¡","timestamp":"1373594270833","digest":""}
	private int result;
	private String message;
	private long timestamp;
	private String digest;
	
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getDigest() {
		return digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}
	
}
