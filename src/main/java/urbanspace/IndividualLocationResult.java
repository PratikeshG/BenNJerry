package urbanspace;

import java.util.LinkedHashMap;

public class IndividualLocationResult {
	private String merchantId;
	private String merchantName;
	private LinkedHashMap<String,Integer> grossSales;
	private LinkedHashMap<String,Integer> grossSalesRefunds;
	private LinkedHashMap<String,Integer> grossSalesNet;
	private LinkedHashMap<String,Integer> totalDiscountsMoney;
	private LinkedHashMap<String,Integer> totalDiscountsMoneyRefunds;
	private LinkedHashMap<String,Integer> totalDiscountsMoneyNet;
	private LinkedHashMap<String,Integer> netSales;
	private LinkedHashMap<String,Integer> netSalesRefunds;
	private LinkedHashMap<String,Integer> netSalesNet;
	private LinkedHashMap<String,Integer> giftCardSales;
	private LinkedHashMap<String,Integer> giftCardSalesRefunds;
	private LinkedHashMap<String,Integer> giftCardSalesNet;
	private LinkedHashMap<String,Integer> totalTaxMoney;
	private LinkedHashMap<String,Integer> totalTaxMoneyRefunds;
	private LinkedHashMap<String,Integer> totalTaxMoneyNet;
	private LinkedHashMap<String,Integer> totalTipMoney;
	private LinkedHashMap<String,Integer> totalTipMoneyRefunds;
	private LinkedHashMap<String,Integer> totalTipMoneyNet;
	private LinkedHashMap<String,Integer> totalPartialRefundsMoney;
	private LinkedHashMap<String,Integer> totalPartialRefundsMoneyRefunds;
	private LinkedHashMap<String,Integer> totalPartialRefundsMoneyNet;
	private LinkedHashMap<String,Integer> totalCollectedMoney;
	private LinkedHashMap<String,Integer> totalCollectedMoneyRefunds;
	private LinkedHashMap<String,Integer> totalCollectedMoneyNet;
	private LinkedHashMap<String,Integer> totalCashMoney;
	private LinkedHashMap<String,Integer> totalCashMoneyRefunds;
	private LinkedHashMap<String,Integer> totalCashMoneyNet;
	private LinkedHashMap<String,Integer> totalCardMoney;
	private LinkedHashMap<String,Integer> totalCardMoneyRefunds;
	private LinkedHashMap<String,Integer> totalCardMoneyNet;
	private LinkedHashMap<String,Integer> totalGiftCardMoney;
	private LinkedHashMap<String,Integer> totalGiftCardMoneyRefunds;
	private LinkedHashMap<String,Integer> totalGiftCardMoneyNet;
	private LinkedHashMap<String,Integer> totalOtherMoney;
	private LinkedHashMap<String,Integer> totalOtherMoneyRefunds;
	private LinkedHashMap<String,Integer> totalOtherMoneyNet;
	private LinkedHashMap<String,Integer> totalFeesMoney;
	private LinkedHashMap<String,Integer> totalFeesMoneyRefunds;
	private LinkedHashMap<String,Integer> totalFeesMoneyNet;
	private LinkedHashMap<String,Integer> netTotalMoney;
	private LinkedHashMap<String,Integer> netTotalMoneyRefunds;
	private LinkedHashMap<String,Integer> netTotalMoneyNet;
	private LinkedHashMap<String,LinkedHashMap<String,Integer>> categorySales;
	private LinkedHashMap<String,LinkedHashMap<String,Integer>> categorySalesRefunds;
	private LinkedHashMap<String,LinkedHashMap<String,Integer>> categorySalesNet;
	private LinkedHashMap<String,LinkedHashMap<String,Integer>> totalsPerDiscount;
	private LinkedHashMap<String,LinkedHashMap<String,Integer>> totalsPerDiscountRefunds;
	private LinkedHashMap<String,LinkedHashMap<String,Integer>> totalsPerDiscountNet;
	private LinkedHashMap<String,Integer> totalCardSwipedMoney;
	private LinkedHashMap<String,Integer> totalCardSwipedMoneyRefunds;
	private LinkedHashMap<String,Integer> totalCardSwipedMoneyNet;
	private LinkedHashMap<String,Integer> totalCardTappedMoney;
	private LinkedHashMap<String,Integer> totalCardTappedMoneyRefunds;
	private LinkedHashMap<String,Integer> totalCardTappedMoneyNet;
	private LinkedHashMap<String,Integer> totalCardDippedMoney;
	private LinkedHashMap<String,Integer> totalCardDippedMoneyRefunds;
	private LinkedHashMap<String,Integer> totalCardDippedMoneyNet;
	private LinkedHashMap<String,Integer> totalCardKeyedMoney;
	private LinkedHashMap<String,Integer> totalCardKeyedMoneyRefunds;
	private LinkedHashMap<String,Integer> totalCardKeyedMoneyNet;
	private LinkedHashMap<String,Integer> totalVisaMoney;
	private LinkedHashMap<String,Integer> totalVisaMoneyRefunds;
	private LinkedHashMap<String,Integer> totalVisaMoneyNet;
	private LinkedHashMap<String,Integer> totalMasterCardMoney;
	private LinkedHashMap<String,Integer> totalMasterCardMoneyRefunds;
	private LinkedHashMap<String,Integer> totalMasterCardMoneyNet;
	private LinkedHashMap<String,Integer> totalDiscoverMoney;
	private LinkedHashMap<String,Integer> totalDiscoverMoneyRefunds;
	private LinkedHashMap<String,Integer> totalDiscoverMoneyNet;
	private LinkedHashMap<String,Integer> totalAmexMoney;
	private LinkedHashMap<String,Integer> totalAmexMoneyRefunds;
	private LinkedHashMap<String,Integer> totalAmexMoneyNet;
	private LinkedHashMap<String,Integer> totalOtherCardMoney;
	private LinkedHashMap<String,Integer> totalOtherCardMoneyRefunds;
	private LinkedHashMap<String,Integer> totalOtherCardMoneyNet;
	
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public LinkedHashMap<String, Integer> getGrossSales() {
		return grossSales;
	}
	public void setGrossSales(LinkedHashMap<String, Integer> grossSales) {
		this.grossSales = grossSales;
	}
	public LinkedHashMap<String, Integer> getGrossSalesRefunds() {
		return grossSalesRefunds;
	}
	public void setGrossSalesRefunds(LinkedHashMap<String, Integer> grossSalesRefunds) {
		this.grossSalesRefunds = grossSalesRefunds;
	}
	public LinkedHashMap<String, Integer> getGrossSalesNet() {
		return grossSalesNet;
	}
	public void setGrossSalesNet(LinkedHashMap<String, Integer> grossSalesNet) {
		this.grossSalesNet = grossSalesNet;
	}
	public LinkedHashMap<String, Integer> getTotalDiscountsMoney() {
		return totalDiscountsMoney;
	}
	public void setTotalDiscountsMoney(LinkedHashMap<String, Integer> totalDiscountsMoney) {
		this.totalDiscountsMoney = totalDiscountsMoney;
	}
	public LinkedHashMap<String, Integer> getTotalDiscountsMoneyRefunds() {
		return totalDiscountsMoneyRefunds;
	}
	public void setTotalDiscountsMoneyRefunds(
			LinkedHashMap<String, Integer> totalDiscountsMoneyRefunds) {
		this.totalDiscountsMoneyRefunds = totalDiscountsMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalDiscountsMoneyNet() {
		return totalDiscountsMoneyNet;
	}
	public void setTotalDiscountsMoneyNet(
			LinkedHashMap<String, Integer> totalDiscountsMoneyNet) {
		this.totalDiscountsMoneyNet = totalDiscountsMoneyNet;
	}
	public LinkedHashMap<String, Integer> getNetSales() {
		return netSales;
	}
	public void setNetSales(LinkedHashMap<String, Integer> netSales) {
		this.netSales = netSales;
	}
	public LinkedHashMap<String, Integer> getNetSalesRefunds() {
		return netSalesRefunds;
	}
	public void setNetSalesRefunds(LinkedHashMap<String, Integer> netSalesRefunds) {
		this.netSalesRefunds = netSalesRefunds;
	}
	public LinkedHashMap<String, Integer> getNetSalesNet() {
		return netSalesNet;
	}
	public void setNetSalesNet(LinkedHashMap<String, Integer> netSalesNet) {
		this.netSalesNet = netSalesNet;
	}
	public LinkedHashMap<String, Integer> getGiftCardSales() {
		return giftCardSales;
	}
	public void setGiftCardSales(LinkedHashMap<String, Integer> giftCardSales) {
		this.giftCardSales = giftCardSales;
	}
	public LinkedHashMap<String, Integer> getGiftCardSalesRefunds() {
		return giftCardSalesRefunds;
	}
	public void setGiftCardSalesRefunds(LinkedHashMap<String, Integer> giftCardSalesRefunds) {
		this.giftCardSalesRefunds = giftCardSalesRefunds;
	}
	public LinkedHashMap<String, Integer> getGiftCardSalesNet() {
		return giftCardSalesNet;
	}
	public void setGiftCardSalesNet(LinkedHashMap<String, Integer> giftCardSalesNet) {
		this.giftCardSalesNet = giftCardSalesNet;
	}
	public LinkedHashMap<String, Integer> getTotalTaxMoney() {
		return totalTaxMoney;
	}
	public void setTotalTaxMoney(LinkedHashMap<String, Integer> totalTaxMoney) {
		this.totalTaxMoney = totalTaxMoney;
	}
	public LinkedHashMap<String, Integer> getTotalTaxMoneyRefunds() {
		return totalTaxMoneyRefunds;
	}
	public void setTotalTaxMoneyRefunds(LinkedHashMap<String, Integer> totalTaxMoneyRefunds) {
		this.totalTaxMoneyRefunds = totalTaxMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalTaxMoneyNet() {
		return totalTaxMoneyNet;
	}
	public void setTotalTaxMoneyNet(LinkedHashMap<String, Integer> totalTaxMoneyNet) {
		this.totalTaxMoneyNet = totalTaxMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalTipMoney() {
		return totalTipMoney;
	}
	public void setTotalTipMoney(LinkedHashMap<String, Integer> totalTipMoney) {
		this.totalTipMoney = totalTipMoney;
	}
	public LinkedHashMap<String, Integer> getTotalTipMoneyRefunds() {
		return totalTipMoneyRefunds;
	}
	public void setTotalTipMoneyRefunds(LinkedHashMap<String, Integer> totalTipMoneyRefunds) {
		this.totalTipMoneyRefunds = totalTipMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalTipMoneyNet() {
		return totalTipMoneyNet;
	}
	public void setTotalTipMoneyNet(LinkedHashMap<String, Integer> totalTipMoneyNet) {
		this.totalTipMoneyNet = totalTipMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalPartialRefundsMoney() {
		return totalPartialRefundsMoney;
	}
	public void setTotalPartialRefundsMoney(
			LinkedHashMap<String, Integer> totalPartialRefundsMoney) {
		this.totalPartialRefundsMoney = totalPartialRefundsMoney;
	}
	public LinkedHashMap<String, Integer> getTotalPartialRefundsMoneyRefunds() {
		return totalPartialRefundsMoneyRefunds;
	}
	public void setTotalPartialRefundsMoneyRefunds(
			LinkedHashMap<String, Integer> totalPartialRefundsMoneyRefunds) {
		this.totalPartialRefundsMoneyRefunds = totalPartialRefundsMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalPartialRefundsMoneyNet() {
		return totalPartialRefundsMoneyNet;
	}
	public void setTotalPartialRefundsMoneyNet(
			LinkedHashMap<String, Integer> totalPartialRefundsMoneyNet) {
		this.totalPartialRefundsMoneyNet = totalPartialRefundsMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalCollectedMoney() {
		return totalCollectedMoney;
	}
	public void setTotalCollectedMoney(LinkedHashMap<String, Integer> totalCollectedMoney) {
		this.totalCollectedMoney = totalCollectedMoney;
	}
	public LinkedHashMap<String, Integer> getTotalCollectedMoneyRefunds() {
		return totalCollectedMoneyRefunds;
	}
	public void setTotalCollectedMoneyRefunds(
			LinkedHashMap<String, Integer> totalCollectedMoneyRefunds) {
		this.totalCollectedMoneyRefunds = totalCollectedMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalCollectedMoneyNet() {
		return totalCollectedMoneyNet;
	}
	public void setTotalCollectedMoneyNet(
			LinkedHashMap<String, Integer> totalCollectedMoneyNet) {
		this.totalCollectedMoneyNet = totalCollectedMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalCashMoney() {
		return totalCashMoney;
	}
	public void setTotalCashMoney(LinkedHashMap<String, Integer> totalCashMoney) {
		this.totalCashMoney = totalCashMoney;
	}
	public LinkedHashMap<String, Integer> getTotalCashMoneyRefunds() {
		return totalCashMoneyRefunds;
	}
	public void setTotalCashMoneyRefunds(LinkedHashMap<String, Integer> totalCashMoneyRefunds) {
		this.totalCashMoneyRefunds = totalCashMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalCashMoneyNet() {
		return totalCashMoneyNet;
	}
	public void setTotalCashMoneyNet(LinkedHashMap<String, Integer> totalCashMoneyNet) {
		this.totalCashMoneyNet = totalCashMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalCardMoney() {
		return totalCardMoney;
	}
	public void setTotalCardMoney(LinkedHashMap<String, Integer> totalCardMoney) {
		this.totalCardMoney = totalCardMoney;
	}
	public LinkedHashMap<String, Integer> getTotalCardMoneyRefunds() {
		return totalCardMoneyRefunds;
	}
	public void setTotalCardMoneyRefunds(LinkedHashMap<String, Integer> totalCardMoneyRefunds) {
		this.totalCardMoneyRefunds = totalCardMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalCardMoneyNet() {
		return totalCardMoneyNet;
	}
	public void setTotalCardMoneyNet(LinkedHashMap<String, Integer> totalCardMoneyNet) {
		this.totalCardMoneyNet = totalCardMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalGiftCardMoney() {
		return totalGiftCardMoney;
	}
	public void setTotalGiftCardMoney(LinkedHashMap<String, Integer> totalGiftCardMoney) {
		this.totalGiftCardMoney = totalGiftCardMoney;
	}
	public LinkedHashMap<String, Integer> getTotalGiftCardMoneyRefunds() {
		return totalGiftCardMoneyRefunds;
	}
	public void setTotalGiftCardMoneyRefunds(
			LinkedHashMap<String, Integer> totalGiftCardMoneyRefunds) {
		this.totalGiftCardMoneyRefunds = totalGiftCardMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalGiftCardMoneyNet() {
		return totalGiftCardMoneyNet;
	}
	public void setTotalGiftCardMoneyNet(LinkedHashMap<String, Integer> totalGiftCardMoneyNet) {
		this.totalGiftCardMoneyNet = totalGiftCardMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalOtherMoney() {
		return totalOtherMoney;
	}
	public void setTotalOtherMoney(LinkedHashMap<String, Integer> totalOtherMoney) {
		this.totalOtherMoney = totalOtherMoney;
	}
	public LinkedHashMap<String, Integer> getTotalOtherMoneyRefunds() {
		return totalOtherMoneyRefunds;
	}
	public void setTotalOtherMoneyRefunds(
			LinkedHashMap<String, Integer> totalOtherMoneyRefunds) {
		this.totalOtherMoneyRefunds = totalOtherMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalOtherMoneyNet() {
		return totalOtherMoneyNet;
	}
	public void setTotalOtherMoneyNet(LinkedHashMap<String, Integer> totalOtherMoneyNet) {
		this.totalOtherMoneyNet = totalOtherMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalFeesMoney() {
		return totalFeesMoney;
	}
	public void setTotalFeesMoney(LinkedHashMap<String, Integer> totalFeesMoney) {
		this.totalFeesMoney = totalFeesMoney;
	}
	public LinkedHashMap<String, Integer> getTotalFeesMoneyRefunds() {
		return totalFeesMoneyRefunds;
	}
	public void setTotalFeesMoneyRefunds(LinkedHashMap<String, Integer> totalFeesMoneyRefunds) {
		this.totalFeesMoneyRefunds = totalFeesMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalFeesMoneyNet() {
		return totalFeesMoneyNet;
	}
	public void setTotalFeesMoneyNet(LinkedHashMap<String, Integer> totalFeesMoneyNet) {
		this.totalFeesMoneyNet = totalFeesMoneyNet;
	}
	public LinkedHashMap<String, Integer> getNetTotalMoney() {
		return netTotalMoney;
	}
	public void setNetTotalMoney(LinkedHashMap<String, Integer> netTotalMoney) {
		this.netTotalMoney = netTotalMoney;
	}
	public LinkedHashMap<String, Integer> getNetTotalMoneyRefunds() {
		return netTotalMoneyRefunds;
	}
	public void setNetTotalMoneyRefunds(LinkedHashMap<String, Integer> netTotalMoneyRefunds) {
		this.netTotalMoneyRefunds = netTotalMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getNetTotalMoneyNet() {
		return netTotalMoneyNet;
	}
	public void setNetTotalMoneyNet(LinkedHashMap<String, Integer> netTotalMoneyNet) {
		this.netTotalMoneyNet = netTotalMoneyNet;
	}
	public LinkedHashMap<String, LinkedHashMap<String, Integer>> getCategorySales() {
		return categorySales;
	}
	public void setCategorySales(LinkedHashMap<String, LinkedHashMap<String, Integer>> categorySales) {
		this.categorySales = categorySales;
	}
	public LinkedHashMap<String, LinkedHashMap<String, Integer>> getCategorySalesRefunds() {
		return categorySalesRefunds;
	}
	public void setCategorySalesRefunds(
			LinkedHashMap<String, LinkedHashMap<String, Integer>> categorySalesRefunds) {
		this.categorySalesRefunds = categorySalesRefunds;
	}
	public LinkedHashMap<String, LinkedHashMap<String, Integer>> getCategorySalesNet() {
		return categorySalesNet;
	}
	public void setCategorySalesNet(
			LinkedHashMap<String, LinkedHashMap<String, Integer>> categorySalesNet) {
		this.categorySalesNet = categorySalesNet;
	}
	public LinkedHashMap<String, LinkedHashMap<String, Integer>> getTotalsPerDiscount() {
		return totalsPerDiscount;
	}
	public void setTotalsPerDiscount(
			LinkedHashMap<String, LinkedHashMap<String, Integer>> totalsPerDiscount) {
		this.totalsPerDiscount = totalsPerDiscount;
	}
	public LinkedHashMap<String, LinkedHashMap<String, Integer>> getTotalsPerDiscountRefunds() {
		return totalsPerDiscountRefunds;
	}
	public void setTotalsPerDiscountRefunds(
			LinkedHashMap<String, LinkedHashMap<String, Integer>> totalsPerDiscountRefunds) {
		this.totalsPerDiscountRefunds = totalsPerDiscountRefunds;
	}
	public LinkedHashMap<String, LinkedHashMap<String, Integer>> getTotalsPerDiscountNet() {
		return totalsPerDiscountNet;
	}
	public void setTotalsPerDiscountNet(
			LinkedHashMap<String, LinkedHashMap<String, Integer>> totalsPerDiscountNet) {
		this.totalsPerDiscountNet = totalsPerDiscountNet;
	}
	public LinkedHashMap<String, Integer> getTotalCardSwipedMoney() {
		return totalCardSwipedMoney;
	}
	public void setTotalCardSwipedMoney(LinkedHashMap<String, Integer> totalCardSwipedMoney) {
		this.totalCardSwipedMoney = totalCardSwipedMoney;
	}
	public LinkedHashMap<String, Integer> getTotalCardSwipedMoneyRefunds() {
		return totalCardSwipedMoneyRefunds;
	}
	public void setTotalCardSwipedMoneyRefunds(
			LinkedHashMap<String, Integer> totalCardSwipedMoneyRefunds) {
		this.totalCardSwipedMoneyRefunds = totalCardSwipedMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalCardSwipedMoneyNet() {
		return totalCardSwipedMoneyNet;
	}
	public void setTotalCardSwipedMoneyNet(
			LinkedHashMap<String, Integer> totalCardSwipedMoneyNet) {
		this.totalCardSwipedMoneyNet = totalCardSwipedMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalCardTappedMoney() {
		return totalCardTappedMoney;
	}
	public void setTotalCardTappedMoney(LinkedHashMap<String, Integer> totalCardTappedMoney) {
		this.totalCardTappedMoney = totalCardTappedMoney;
	}
	public LinkedHashMap<String, Integer> getTotalCardTappedMoneyRefunds() {
		return totalCardTappedMoneyRefunds;
	}
	public void setTotalCardTappedMoneyRefunds(
			LinkedHashMap<String, Integer> totalCardTappedMoneyRefunds) {
		this.totalCardTappedMoneyRefunds = totalCardTappedMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalCardTappedMoneyNet() {
		return totalCardTappedMoneyNet;
	}
	public void setTotalCardTappedMoneyNet(
			LinkedHashMap<String, Integer> totalCardTappedMoneyNet) {
		this.totalCardTappedMoneyNet = totalCardTappedMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalCardDippedMoney() {
		return totalCardDippedMoney;
	}
	public void setTotalCardDippedMoney(LinkedHashMap<String, Integer> totalCardDippedMoney) {
		this.totalCardDippedMoney = totalCardDippedMoney;
	}
	public LinkedHashMap<String, Integer> getTotalCardDippedMoneyRefunds() {
		return totalCardDippedMoneyRefunds;
	}
	public void setTotalCardDippedMoneyRefunds(
			LinkedHashMap<String, Integer> totalCardDippedMoneyRefunds) {
		this.totalCardDippedMoneyRefunds = totalCardDippedMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalCardDippedMoneyNet() {
		return totalCardDippedMoneyNet;
	}
	public void setTotalCardDippedMoneyNet(
			LinkedHashMap<String, Integer> totalCardDippedMoneyNet) {
		this.totalCardDippedMoneyNet = totalCardDippedMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalCardKeyedMoney() {
		return totalCardKeyedMoney;
	}
	public void setTotalCardKeyedMoney(LinkedHashMap<String, Integer> totalCardKeyedMoney) {
		this.totalCardKeyedMoney = totalCardKeyedMoney;
	}
	public LinkedHashMap<String, Integer> getTotalCardKeyedMoneyRefunds() {
		return totalCardKeyedMoneyRefunds;
	}
	public void setTotalCardKeyedMoneyRefunds(
			LinkedHashMap<String, Integer> totalCardKeyedMoneyRefunds) {
		this.totalCardKeyedMoneyRefunds = totalCardKeyedMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalCardKeyedMoneyNet() {
		return totalCardKeyedMoneyNet;
	}
	public void setTotalCardKeyedMoneyNet(
			LinkedHashMap<String, Integer> totalCardKeyedMoneyNet) {
		this.totalCardKeyedMoneyNet = totalCardKeyedMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalVisaMoney() {
		return totalVisaMoney;
	}
	public void setTotalVisaMoney(LinkedHashMap<String, Integer> totalVisaMoney) {
		this.totalVisaMoney = totalVisaMoney;
	}
	public LinkedHashMap<String, Integer> getTotalVisaMoneyRefunds() {
		return totalVisaMoneyRefunds;
	}
	public void setTotalVisaMoneyRefunds(LinkedHashMap<String, Integer> totalVisaMoneyRefunds) {
		this.totalVisaMoneyRefunds = totalVisaMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalVisaMoneyNet() {
		return totalVisaMoneyNet;
	}
	public void setTotalVisaMoneyNet(LinkedHashMap<String, Integer> totalVisaMoneyNet) {
		this.totalVisaMoneyNet = totalVisaMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalMasterCardMoney() {
		return totalMasterCardMoney;
	}
	public void setTotalMasterCardMoney(LinkedHashMap<String, Integer> totalMasterCardMoney) {
		this.totalMasterCardMoney = totalMasterCardMoney;
	}
	public LinkedHashMap<String, Integer> getTotalMasterCardMoneyRefunds() {
		return totalMasterCardMoneyRefunds;
	}
	public void setTotalMasterCardMoneyRefunds(
			LinkedHashMap<String, Integer> totalMasterCardMoneyRefunds) {
		this.totalMasterCardMoneyRefunds = totalMasterCardMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalMasterCardMoneyNet() {
		return totalMasterCardMoneyNet;
	}
	public void setTotalMasterCardMoneyNet(
			LinkedHashMap<String, Integer> totalMasterCardMoneyNet) {
		this.totalMasterCardMoneyNet = totalMasterCardMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalDiscoverMoney() {
		return totalDiscoverMoney;
	}
	public void setTotalDiscoverMoney(LinkedHashMap<String, Integer> totalDiscoverMoney) {
		this.totalDiscoverMoney = totalDiscoverMoney;
	}
	public LinkedHashMap<String, Integer> getTotalDiscoverMoneyRefunds() {
		return totalDiscoverMoneyRefunds;
	}
	public void setTotalDiscoverMoneyRefunds(
			LinkedHashMap<String, Integer> totalDiscoverMoneyRefunds) {
		this.totalDiscoverMoneyRefunds = totalDiscoverMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalDiscoverMoneyNet() {
		return totalDiscoverMoneyNet;
	}
	public void setTotalDiscoverMoneyNet(LinkedHashMap<String, Integer> totalDiscoverMoneyNet) {
		this.totalDiscoverMoneyNet = totalDiscoverMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalAmexMoney() {
		return totalAmexMoney;
	}
	public void setTotalAmexMoney(LinkedHashMap<String, Integer> totalAmexMoney) {
		this.totalAmexMoney = totalAmexMoney;
	}
	public LinkedHashMap<String, Integer> getTotalAmexMoneyRefunds() {
		return totalAmexMoneyRefunds;
	}
	public void setTotalAmexMoneyRefunds(LinkedHashMap<String, Integer> totalAmexMoneyRefunds) {
		this.totalAmexMoneyRefunds = totalAmexMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalAmexMoneyNet() {
		return totalAmexMoneyNet;
	}
	public void setTotalAmexMoneyNet(LinkedHashMap<String, Integer> totalAmexMoneyNet) {
		this.totalAmexMoneyNet = totalAmexMoneyNet;
	}
	public LinkedHashMap<String, Integer> getTotalOtherCardMoney() {
		return totalOtherCardMoney;
	}
	public void setTotalOtherCardMoney(LinkedHashMap<String, Integer> totalOtherCardMoney) {
		this.totalOtherCardMoney = totalOtherCardMoney;
	}
	public LinkedHashMap<String, Integer> getTotalOtherCardMoneyRefunds() {
		return totalOtherCardMoneyRefunds;
	}
	public void setTotalOtherCardMoneyRefunds(
			LinkedHashMap<String, Integer> totalOtherCardMoneyRefunds) {
		this.totalOtherCardMoneyRefunds = totalOtherCardMoneyRefunds;
	}
	public LinkedHashMap<String, Integer> getTotalOtherCardMoneyNet() {
		return totalOtherCardMoneyNet;
	}
	public void setTotalOtherCardMoneyNet(
			LinkedHashMap<String, Integer> totalOtherCardMoneyNet) {
		this.totalOtherCardMoneyNet = totalOtherCardMoneyNet;
	}
	
}
