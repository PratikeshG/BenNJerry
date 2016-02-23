package urbanspace;

import java.util.Map;

public class LocationResults {
	private String merchantId;
	private Map<String,Integer> grossSales;
	private Map<String,Integer> totalDiscountsMoney;
	private Map<String,Integer> netSales;
	private Map<String,Integer> giftCardSales;
	private Map<String,Integer> totalTaxMoney;
	private Map<String,Integer> totalTipMoney;
	private Map<String,Integer> totalCollectedMoney;
	private Map<String,Integer> totalCashMoney;
	private Map<String,Integer> totalCardMoney;
	private Map<String,Integer> totalGiftCardMoney;
	private Map<String,Integer> totalOtherMoney;
	private Map<String,Integer> totalFeesMoney;
	private Map<String,Integer> netTotalMoney;
	private Map<String,Map<String,Integer>> categorySales;
	private Map<String,Map<String,Integer>> totalsPerDiscount;
    private Map<String,Integer> totalCardSwipedMoney;
    private Map<String,Integer> totalCardTappedMoney;
    private Map<String,Integer> totalCardDippedMoney;
    private Map<String,Integer> totalCardKeyedMoney;
    private Map<String,Integer> totalVisaMoney;
    private Map<String,Integer> totalMasterCardMoney;
    private Map<String,Integer> totalDiscoverMoney;
    private Map<String,Integer> totalAmexMoney;
    private Map<String,Integer> totalOtherCardMoney;

	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public Map<String, Integer> getGrossSales() {
		return grossSales;
	}
	public void setGrossSales(Map<String, Integer> grossSales) {
		this.grossSales = grossSales;
	}
	public Map<String, Integer> getTotalDiscountsMoney() {
		return totalDiscountsMoney;
	}
	public void setTotalDiscountsMoney(Map<String, Integer> totalDiscountsMoney) {
		this.totalDiscountsMoney = totalDiscountsMoney;
	}
	public Map<String, Integer> getNetSales() {
		return netSales;
	}
	public void setNetSales(Map<String, Integer> netSales) {
		this.netSales = netSales;
	}
	public Map<String, Integer> getGiftCardSales() {
		return giftCardSales;
	}
	public void setGiftCardSales(Map<String, Integer> giftCardSales) {
		this.giftCardSales = giftCardSales;
	}
	public Map<String, Integer> getTotalTaxMoney() {
		return totalTaxMoney;
	}
	public void setTotalTaxMoney(Map<String, Integer> totalTaxMoney) {
		this.totalTaxMoney = totalTaxMoney;
	}
	public Map<String, Integer> getTotalTipMoney() {
		return totalTipMoney;
	}
	public void setTotalTipMoney(Map<String, Integer> totalTipMoney) {
		this.totalTipMoney = totalTipMoney;
	}
	public Map<String, Integer> getTotalCollectedMoney() {
		return totalCollectedMoney;
	}
	public void setTotalCollectedMoney(Map<String, Integer> totalCollectedMoney) {
		this.totalCollectedMoney = totalCollectedMoney;
	}
	public Map<String, Integer> getTotalCashMoney() {
		return totalCashMoney;
	}
	public void setTotalCashMoney(Map<String, Integer> totalCashMoney) {
		this.totalCashMoney = totalCashMoney;
	}
	public Map<String, Integer> getTotalCardMoney() {
		return totalCardMoney;
	}
	public void setTotalCardMoney(Map<String, Integer> totalCardMoney) {
		this.totalCardMoney = totalCardMoney;
	}
	public Map<String, Integer> getTotalGiftCardMoney() {
		return totalGiftCardMoney;
	}
	public void setTotalGiftCardMoney(Map<String, Integer> totalGiftCardMoney) {
		this.totalGiftCardMoney = totalGiftCardMoney;
	}
	public Map<String, Integer> getTotalOtherMoney() {
		return totalOtherMoney;
	}
	public void setTotalOtherMoney(Map<String, Integer> totalOtherMoney) {
		this.totalOtherMoney = totalOtherMoney;
	}
	public Map<String, Integer> getTotalFeesMoney() {
		return totalFeesMoney;
	}
	public void setTotalFeesMoney(Map<String, Integer> totalFeesMoney) {
		this.totalFeesMoney = totalFeesMoney;
	}
	public Map<String, Integer> getNetTotalMoney() {
		return netTotalMoney;
	}
	public void setNetTotalMoney(Map<String, Integer> netTotalMoney) {
		this.netTotalMoney = netTotalMoney;
	}
	public Map<String, Map<String, Integer>> getCategorySales() {
		return categorySales;
	}
	public void setCategorySales(Map<String, Map<String, Integer>> categorySales) {
		this.categorySales = categorySales;
	}
	public Map<String, Map<String, Integer>> getTotalsPerDiscount() {
		return totalsPerDiscount;
	}
	public void setTotalsPerDiscount(
			Map<String, Map<String, Integer>> totalsPerDiscount) {
		this.totalsPerDiscount = totalsPerDiscount;
	}
	public Map<String, Integer> getTotalCardSwipedMoney() {
		return totalCardSwipedMoney;
	}
	public void setTotalCardSwipedMoney(Map<String, Integer> totalCardSwipedMoney) {
		this.totalCardSwipedMoney = totalCardSwipedMoney;
	}
	public Map<String, Integer> getTotalCardTappedMoney() {
		return totalCardTappedMoney;
	}
	public void setTotalCardTappedMoney(Map<String, Integer> totalCardTappedMoney) {
		this.totalCardTappedMoney = totalCardTappedMoney;
	}
	public Map<String, Integer> getTotalCardDippedMoney() {
		return totalCardDippedMoney;
	}
	public void setTotalCardDippedMoney(Map<String, Integer> totalCardDippedMoney) {
		this.totalCardDippedMoney = totalCardDippedMoney;
	}
	public Map<String, Integer> getTotalCardKeyedMoney() {
		return totalCardKeyedMoney;
	}
	public void setTotalCardKeyedMoney(Map<String, Integer> totalCardKeyedMoney) {
		this.totalCardKeyedMoney = totalCardKeyedMoney;
	}
	public Map<String, Integer> getTotalVisaMoney() {
		return totalVisaMoney;
	}
	public void setTotalVisaMoney(Map<String, Integer> totalVisaMoney) {
		this.totalVisaMoney = totalVisaMoney;
	}
	public Map<String, Integer> getTotalMasterCardMoney() {
		return totalMasterCardMoney;
	}
	public void setTotalMasterCardMoney(Map<String, Integer> totalMasterCardMoney) {
		this.totalMasterCardMoney = totalMasterCardMoney;
	}
	public Map<String, Integer> getTotalDiscoverMoney() {
		return totalDiscoverMoney;
	}
	public void setTotalDiscoverMoney(Map<String, Integer> totalDiscoverMoney) {
		this.totalDiscoverMoney = totalDiscoverMoney;
	}
	public Map<String, Integer> getTotalAmexMoney() {
		return totalAmexMoney;
	}
	public void setTotalAmexMoney(Map<String, Integer> totalAmexMoney) {
		this.totalAmexMoney = totalAmexMoney;
	}
	public Map<String, Integer> getTotalOtherCardMoney() {
		return totalOtherCardMoney;
	}
	public void setTotalOtherCardMoney(Map<String, Integer> totalOtherCardMoney) {
		this.totalOtherCardMoney = totalOtherCardMoney;
	}
}
