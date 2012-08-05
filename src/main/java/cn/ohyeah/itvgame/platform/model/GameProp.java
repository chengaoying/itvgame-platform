package cn.ohyeah.itvgame.platform.model;

public class GameProp implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -704244188899056764L;
	private int propId;
	private String propName;
	private int price;
	private int validPeriod;
	private int productId;		/* Ù”⁄ƒƒ∏ˆ”Œœ∑*/
	private String description;	/*√Ë ˆ*/
	
	public int getPropId() {
		return propId;
	}
	public void setPropId(int propId) {
		this.propId = propId;
	}
	public String getPropName() {
		return propName;
	}
	public void setPropName(String propName) {
		this.propName = propName;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public int getProductId() {
		return productId;
	}
	public void setValidPeriod(int validPeriod) {
		this.validPeriod = validPeriod;
	}
	public int getValidPeriod() {
		return validPeriod;
	}
	
}
