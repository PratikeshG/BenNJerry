package urbanspace;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import util.SquarePayload;
import util.TimeManager;

import com.squareup.connect.Payment;

public class IndividualCalculator {

	private String timeMethod;
	private String timeZone;
	private int offset;
	private int range;
	private ReportCalculator reportCalculator;
	
	public IndividualCalculator(String timeMethod, String timeZone, int offset, int range) {
		this.timeZone = timeZone;
		this.offset = offset;
		this.range = range;
	}

	public void setTimeMethod(String timeMethod) {
		this.timeMethod = timeMethod;
	}
	
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public void setRange(int range) {
		this.range = range;
	}
	
	public String generate(SquarePayload squarePayload) throws ParseException {
		Payment[] payments = (Payment[]) squarePayload.getResults().get("util.square.PaymentsLister");
		Payment[] refundPayments = (Payment[]) squarePayload.getResults().get("util.square.RefundPaymentsRetriever");
		
		reportCalculator = new ReportCalculator(range, offset, timeZone);
		
		IndividualLocationResult locationResult = new IndividualLocationResult();
		
		locationResult.setMerchantId(squarePayload.getMerchantId());
        locationResult.setMerchantName(squarePayload.getMerchantAlias());
		
        locationResult.setGiftCardSales(totalMoneyCollectedForGiftCardsByTime(payments));
        locationResult.setGiftCardSalesRefunds(totalMoneyCollectedForGiftCardsRefundsByTime(refundPayments));
        locationResult.setGiftCardSalesNet(addTimeMoneyMaps(locationResult.getGiftCardSales(), locationResult.getGiftCardSalesRefunds()));
		
        locationResult.setTotalTaxMoney(totalTaxMoneyByTime(payments));
        locationResult.setTotalTaxMoneyRefunds(totalTaxMoneyRefundsByTime(refundPayments));
        locationResult.setTotalTaxMoneyNet(addTimeMoneyMaps(locationResult.getTotalTaxMoney(), locationResult.getTotalTaxMoneyRefunds()));
        
        locationResult.setTotalTipMoney(totalTipMoneyByTime(payments));
        locationResult.setTotalTipMoneyRefunds(totalTipMoneyRefundsByTime(refundPayments));
        locationResult.setTotalTipMoneyNet(addTimeMoneyMaps(locationResult.getTotalTipMoney(), locationResult.getTotalTipMoneyRefunds()));

        // total collected + discounts - tax - tips = gross sales
        locationResult.setTotalCollectedMoney(totalTotalCollectedMoneyByTime(payments));
        locationResult.setTotalCollectedMoneyRefunds(totalTotalCollectedMoneyRefundsByTime(refundPayments));
        locationResult.setTotalCollectedMoneyNet(addTimeMoneyMaps(locationResult.getTotalCollectedMoney(), locationResult.getTotalCollectedMoneyRefunds()));

        locationResult.setTotalDiscountsMoney(totalDiscountMoneyByTime(payments));
        locationResult.setTotalDiscountsMoneyRefunds(totalDiscountMoneyRefundsByTime(refundPayments));
        locationResult.setTotalDiscountsMoneyNet(addTimeMoneyMaps(locationResult.getTotalDiscountsMoney(), locationResult.getTotalDiscountsMoneyRefunds()));
        
        // gross sales = net sales + discounts
        locationResult.setGrossSales(totalGrossSalesByTime(payments));
        locationResult.setGrossSalesRefunds(totalGrossSalesRefundsByTime(refundPayments));
        locationResult.setGrossSalesNet(addTimeMoneyMaps(locationResult.getGrossSales(), locationResult.getGrossSalesRefunds()));

        locationResult.setNetSales(addTimeMoneyMaps(locationResult.getGrossSales(), locationResult.getTotalDiscountsMoney()));
        locationResult.setNetSalesRefunds(addTimeMoneyMaps(locationResult.getGrossSalesRefunds(), locationResult.getTotalDiscountsMoneyRefunds()));
        locationResult.setNetSalesNet(addTimeMoneyMaps(locationResult.getNetSales(), locationResult.getNetSalesRefunds()));

        locationResult.setTotalCashMoney(totalMoneyCollectedForTenderByTime(payments, "CASH"));
        locationResult.setTotalCashMoneyRefunds(totalMoneyCollectedForTenderRefundsByTime(refundPayments, "CASH"));
        locationResult.setTotalCashMoneyNet(addTimeMoneyMaps(locationResult.getTotalCashMoney(), locationResult.getTotalCashMoneyRefunds()));

        locationResult.setTotalCardMoney(totalMoneyCollectedForTenderByTime(payments, "CREDIT_CARD"));
        locationResult.setTotalCardMoneyRefunds(totalMoneyCollectedForTenderRefundsByTime(refundPayments, "CREDIT_CARD"));
        locationResult.setTotalCardMoneyNet(addTimeMoneyMaps(locationResult.getTotalCardMoney(), locationResult.getTotalCardMoneyRefunds()));

        locationResult.setTotalGiftCardMoney(totalMoneyCollectedForTenderByTime(payments, "SQUARE_GIFT_CARD"));
        locationResult.setTotalGiftCardMoneyRefunds(totalMoneyCollectedForTenderRefundsByTime(refundPayments, "SQUARE_GIFT_CARD"));
        locationResult.setTotalGiftCardMoneyNet(addTimeMoneyMaps(locationResult.getTotalGiftCardMoney(), locationResult.getTotalGiftCardMoneyRefunds()));

        /*
        locationResult.setTotalOtherMoney(totalMoneyCollectedForTender(payments, "NO_SALE") + totalMoneyCollectedForTender(payments, "SQUARE_WALLET") + totalMoneyCollectedForTender(payments, "UNKNOWN") + totalMoneyCollectedForTender(payments, "OTHER") + totalMoneyCollectedForTender(payments, "THIRD_PARTY_CARD"));
        locationResult.setTotalOtherMoneyRefunds(totalMoneyCollectedForTenderRefunds(refundPayments, "NO_SALE") + totalMoneyCollectedForTenderRefunds(refundPayments, "SQUARE_WALLET") + totalMoneyCollectedForTenderRefunds(refundPayments, "UNKNOWN") + totalMoneyCollectedForTenderRefunds(refundPayments, "OTHER") + totalMoneyCollectedForTenderRefunds(payments, "THIRD_PARTY_CARD"));
        locationResult.setTotalOtherMoneyNet(locationResult.getTotalOtherMoney() + locationResult.getTotalOtherMoneyRefunds());
		*/
        locationResult.setTotalPartialRefundsMoney(totalPartialRefundsByTime());
        locationResult.setTotalPartialRefundsMoneyRefunds(totalPartialRefundsRefundsByTime(refundPayments));
        locationResult.setTotalPartialRefundsMoneyNet(addTimeMoneyMaps(locationResult.getTotalPartialRefundsMoney(), locationResult.getTotalPartialRefundsMoneyRefunds()));

        locationResult.setTotalFeesMoney(totalProcessingFeeMoneyByTime(payments));
        locationResult.setTotalFeesMoneyRefunds(totalProcessingFeeMoneyRefundsByTime(refundPayments));
        locationResult.setTotalFeesMoneyNet(addTimeMoneyMaps(locationResult.getTotalFeesMoney(), locationResult.getTotalFeesMoneyRefunds()));

        locationResult.setNetTotalMoney(addTimeMoneyMaps(locationResult.getTotalCollectedMoney(), locationResult.getTotalFeesMoney()));
        locationResult.setNetTotalMoneyRefunds(addTimeMoneyMaps(locationResult.getTotalCollectedMoneyRefunds(), locationResult.getTotalFeesMoneyRefunds()));
        locationResult.setNetTotalMoneyNet(addTimeMoneyMaps(locationResult.getNetTotalMoney(), locationResult.getNetTotalMoneyRefunds()));
        
        /* Needs to be patched up
        locationResult.setCategorySales(totalSalesPerCategory(payments));
        locationResult.setTotalsPerDiscount(totalSalesPerDiscount(payments));
        
        locationResult.setTotalCardSwipedMoney(totalMoneyCollectedForCardEntryMethod(payments, "SWIPED"));
        // No can do on dips or taps
        locationResult.setTotalCardKeyedMoney(totalMoneyCollectedForCardEntryMethod(payments, "MANUAL"));
        locationResult.setTotalVisaMoney(totalMoneyCollectedForCardBrand(payments, "VISA"));
        locationResult.setTotalMasterCardMoney(totalMoneyCollectedForCardBrand(payments, "MASTER_CARD"));
        locationResult.setTotalDiscoverMoney(totalMoneyCollectedForCardBrand(payments, "DISCOVER"));
        locationResult.setTotalAmexMoney(totalMoneyCollectedForCardBrand(payments, "AMERICAN_EXPRESS"));
        locationResult.setTotalOtherCardMoney(totalOtherCardMoney(totalMoneyCollectedForCardBrand(payments, "DISCOVER_DINERS"), totalMoneyCollectedForCardBrand(payments, "JCB"), totalMoneyCollectedForCardBrand(payments, "UNKNOWN")));
        end patching up */
        
		StringBuilder sb = new StringBuilder();
		sb.append("\"" + locationResult.getMerchantName() + "\",Daily Total,6am-7am,7am-8am,8am-9am,9am-10am,10am-11am,11am-12pm,12pm-1pm,1pm-2pm,2pm-3pm,3pm-4pm,4pm-5pm,5pm-6pm,6pm-7pm,7pm-8pm,8pm-9pm\n");
		sb.append("Gross Sales," + breakoutTimeTotalString(locationResult.getGrossSales()) + "\n");
		sb.append("Gross Sales (Refunds)," + breakoutTimeTotalString(locationResult.getGrossSalesRefunds()) + "\n");
		sb.append("Gross Sales (Net)," + breakoutTimeTotalString(locationResult.getGrossSalesNet()) + "\n");
		sb.append("Total Discounts Money," + breakoutTimeTotalString(locationResult.getTotalDiscountsMoney()) + "\n");
		sb.append("Total Discounts Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalDiscountsMoneyRefunds()) + "\n");
		sb.append("Total Discounts Money (Net)," + breakoutTimeTotalString(locationResult.getTotalDiscountsMoneyNet()) + "\n");
		sb.append("Net Sales," + breakoutTimeTotalString(locationResult.getNetSales()) + "\n");
		sb.append("Net Sales (Refunds)," + breakoutTimeTotalString(locationResult.getNetSalesRefunds()) + "\n");
		sb.append("Net Sales (Net)," + breakoutTimeTotalString(locationResult.getNetSalesNet()) + "\n");
		sb.append("Gift Card Sales," + breakoutTimeTotalString(locationResult.getGiftCardSales()) + "\n");
		sb.append("Gift Card Sales (Refunds)," + breakoutTimeTotalString(locationResult.getGiftCardSalesRefunds()) + "\n");
		sb.append("Gift Card Sales (Net)," + breakoutTimeTotalString(locationResult.getGiftCardSalesNet()) + "\n");
		sb.append("Total Tax Money," + breakoutTimeTotalString(locationResult.getTotalTaxMoney()) + "\n");
		sb.append("Total Tax Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalTaxMoneyRefunds()) + "\n");
		sb.append("Total Tax Money (Net)," + breakoutTimeTotalString(locationResult.getTotalTaxMoneyNet()) + "\n");
		sb.append("Total Tip Money," + breakoutTimeTotalString(locationResult.getTotalTipMoney()) + "\n");
		sb.append("Total Tip Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalTipMoneyRefunds()) + "\n");
		sb.append("Total Tip Money (Net)," + breakoutTimeTotalString(locationResult.getTotalTipMoneyNet()) + "\n");
		sb.append("Total Partial Refunds Money," + breakoutTimeTotalString(locationResult.getTotalPartialRefundsMoney()) + "\n");
		sb.append("Total Partial Refunds Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalPartialRefundsMoneyRefunds()) + "\n");
		sb.append("Total Partial Refunds Money (Net)," + breakoutTimeTotalString(locationResult.getTotalPartialRefundsMoneyNet()) + "\n");
		sb.append("Total Collected Money," + breakoutTimeTotalString(locationResult.getTotalCollectedMoney()) + "\n");
		sb.append("Total Collected Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalCollectedMoneyRefunds()) + "\n");
		sb.append("Total Collected Money (Net)," + breakoutTimeTotalString(locationResult.getTotalCollectedMoneyNet()) + "\n");
		sb.append("Total Cash Money," + breakoutTimeTotalString(locationResult.getTotalCashMoney()) + "\n");
		sb.append("Total Cash Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalCashMoneyRefunds()) + "\n");
		sb.append("Total Cash Money (Net)," + breakoutTimeTotalString(locationResult.getTotalCashMoneyNet()) + "\n");
		sb.append("Total Card Money," + breakoutTimeTotalString(locationResult.getTotalCardMoney()) + "\n");
		sb.append("Total Card Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalCardMoneyRefunds()) + "\n");
		sb.append("Total Card Money (Net)," + breakoutTimeTotalString(locationResult.getTotalCardMoneyNet()) + "\n");
		sb.append("Total Gift Card Money," + breakoutTimeTotalString(locationResult.getTotalGiftCardMoney()) + "\n");
		sb.append("Total Gift Card Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalGiftCardMoneyRefunds()) + "\n");
		sb.append("Total Gift Card Money (Net)," + breakoutTimeTotalString(locationResult.getTotalGiftCardMoneyNet()) + "\n");
		sb.append("Total Other Money," + breakoutTimeTotalString(locationResult.getTotalOtherMoney()) + "\n");
		sb.append("Total Other Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalOtherMoneyRefunds()) + "\n");
		sb.append("Total Other Money (Net)," + breakoutTimeTotalString(locationResult.getTotalOtherMoneyNet()) + "\n");
		sb.append("Total Fees Money," + breakoutTimeTotalString(locationResult.getTotalFeesMoney()) + "\n");
		sb.append("Total Fees Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalFeesMoneyRefunds()) + "\n");
		sb.append("Total Fees Money (Net)," + breakoutTimeTotalString(locationResult.getTotalFeesMoneyNet()) + "\n");
		sb.append("Net Total Money," + breakoutTimeTotalString(locationResult.getNetTotalMoney()) + "\n");
		sb.append("Net Total Money (Refunds)," + breakoutTimeTotalString(locationResult.getNetTotalMoneyRefunds()) + "\n");
		sb.append("Net Total Money (Net)," + breakoutTimeTotalString(locationResult.getNetTotalMoneyNet()) + "\n");

		sb.append("\nCategory Sales\n");
		if (locationResult.getCategorySales() != null) {
			for (String category : locationResult.getCategorySales().keySet()) {
				LinkedHashMap<String,Integer> categorySales = locationResult.getCategorySales().get(category);
				sb.append("\"" + category + "\"," + breakoutTimeTotalString(categorySales) + "\n");
				
				LinkedHashMap<String,Integer> categorySalesRefunds = locationResult.getCategorySalesRefunds().get(category);
				sb.append("\"" + category + " (Refunds)\"," + breakoutTimeTotalString(categorySalesRefunds) + "\n");
				
				LinkedHashMap<String,Integer> categorySalesNet = locationResult.getCategorySalesNet().get(category);
				sb.append("\"" + category + " (Net)\"," + breakoutTimeTotalString(categorySalesNet) + "\n");
			}
		}
		
		sb.append("\nDiscounts\n");
		if (locationResult.getTotalsPerDiscount() != null) {
			for (String discount : locationResult.getTotalsPerDiscount().keySet()) {
				LinkedHashMap<String,Integer> discountSales = locationResult.getTotalsPerDiscount().get(discount);
				sb.append("\"" + discount + "\"," + breakoutTimeTotalString(discountSales) + "\n");
				
				LinkedHashMap<String,Integer> discountSalesRefunds = locationResult.getTotalsPerDiscountRefunds().get(discount);
				sb.append("\"" + discount + " (Refunds)\"," + breakoutTimeTotalString(discountSalesRefunds) + "\n");
				
				LinkedHashMap<String,Integer> discountSalesNet = locationResult.getTotalsPerDiscountNet().get(discount);
				sb.append("\"" + discount + " (Net)\"," + breakoutTimeTotalString(discountSalesNet) + "\n");
			}
		}
		
		sb.append("Total Card Swiped Money," + breakoutTimeTotalString(locationResult.getTotalCardSwipedMoney()) + "\n");
		sb.append("Total Card Swiped Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalCardSwipedMoneyRefunds()) + "\n");
		sb.append("Total Card Swiped Money (Net)," + breakoutTimeTotalString(locationResult.getTotalCardSwipedMoneyNet()) + "\n");
		sb.append("Total Card Tapped Money," + breakoutTimeTotalString(locationResult.getTotalCardTappedMoney()) + "\n");
		sb.append("Total Card Tapped Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalCardTappedMoneyRefunds()) + "\n");
		sb.append("Total Card Tapped Money (Net)," + breakoutTimeTotalString(locationResult.getTotalCardTappedMoneyNet()) + "\n");
		sb.append("Total Card Dipped Money," + breakoutTimeTotalString(locationResult.getTotalCardDippedMoney()) + "\n");
		sb.append("Total Card Dipped Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalCardDippedMoneyRefunds()) + "\n");
		sb.append("Total Card Dipped Money (Net)," + breakoutTimeTotalString(locationResult.getTotalCardDippedMoneyNet()) + "\n");
		sb.append("Total Card Keyed Money," + breakoutTimeTotalString(locationResult.getTotalCardKeyedMoney()) + "\n");
		sb.append("Total Card Keyed Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalCardKeyedMoneyRefunds()) + "\n");
		sb.append("Total Card Keyed Money (Net)," + breakoutTimeTotalString(locationResult.getTotalCardKeyedMoneyNet()) + "\n");
		sb.append("Total Visa Money," + breakoutTimeTotalString(locationResult.getTotalVisaMoney()) + "\n");
		sb.append("Total Visa Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalVisaMoneyRefunds()) + "\n");
		sb.append("Total Visa Money (Net)," + breakoutTimeTotalString(locationResult.getTotalVisaMoneyNet()) + "\n");
		sb.append("Total MasterCard Money," + breakoutTimeTotalString(locationResult.getTotalMasterCardMoney()) + "\n");
		sb.append("Total MasterCard Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalMasterCardMoneyRefunds()) + "\n");
		sb.append("Total MasterCard Money (Net)," + breakoutTimeTotalString(locationResult.getTotalMasterCardMoneyNet()) + "\n");
		sb.append("Total Discover Money," + breakoutTimeTotalString(locationResult.getTotalDiscoverMoney()) + "\n");
		sb.append("Total Discover Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalDiscoverMoneyRefunds()) + "\n");
		sb.append("Total Discover Money (Net)," + breakoutTimeTotalString(locationResult.getTotalDiscoverMoneyNet()) + "\n");
		sb.append("Total Amex Money," + breakoutTimeTotalString(locationResult.getTotalAmexMoney()) + "\n");
		sb.append("Total Amex Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalAmexMoneyRefunds()) + "\n");
		sb.append("Total Amex Money (Net)," + breakoutTimeTotalString(locationResult.getTotalAmexMoneyNet()) + "\n");
		sb.append("Total Other Card Money," + breakoutTimeTotalString(locationResult.getTotalOtherCardMoney()) + "\n");
		sb.append("Total Other Card Money (Refunds)," + breakoutTimeTotalString(locationResult.getTotalOtherCardMoneyRefunds()) + "\n");
		sb.append("Total Other Card Money (Net)," + breakoutTimeTotalString(locationResult.getTotalOtherCardMoneyNet()) + "\n");
		
		return sb.toString();
	}

	private LinkedHashMap<String,Integer> addTimeMoneyMaps(LinkedHashMap<String,Integer> moneyMap1, LinkedHashMap<String,Integer> moneyMap2) {
		if (moneyMap1 == null) {
			return moneyMap2;
		} else if (moneyMap2 == null) {
			return moneyMap1;
		}
		
		LinkedHashMap<String,Integer> addedMap = new LinkedHashMap<String,Integer>();
		
		for (String time : moneyMap1.keySet()) {
			addedMap.put(time, moneyMap1.get(time) + moneyMap2.get(time));
		}
		
		return addedMap;
	}

	// This function ONLY MAKES SENSE FOR ONE DAY OF PAYMENTS.
	private LinkedHashMap<String,List<Payment>> splitPaymentsByTime(Payment[] payments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = new LinkedHashMap<String,List<Payment>>();
		
		paymentsSplitByTime.put("sixAm", new LinkedList<Payment>());
		paymentsSplitByTime.put("sevenAm", new LinkedList<Payment>());
		paymentsSplitByTime.put("eightAm", new LinkedList<Payment>());
		paymentsSplitByTime.put("nineAm", new LinkedList<Payment>());
		paymentsSplitByTime.put("tenAm", new LinkedList<Payment>());
		paymentsSplitByTime.put("elevenAm", new LinkedList<Payment>());
		paymentsSplitByTime.put("twelvePm", new LinkedList<Payment>());
		paymentsSplitByTime.put("onePm", new LinkedList<Payment>());
		paymentsSplitByTime.put("twoPm", new LinkedList<Payment>());
		paymentsSplitByTime.put("threePm", new LinkedList<Payment>());
		paymentsSplitByTime.put("fourPm", new LinkedList<Payment>());
		paymentsSplitByTime.put("fivePm", new LinkedList<Payment>());
		paymentsSplitByTime.put("sixPm", new LinkedList<Payment>());
		paymentsSplitByTime.put("sevenPm", new LinkedList<Payment>());
		paymentsSplitByTime.put("eightPm", new LinkedList<Payment>());
		
		// Starts at 6am-7am; goes to 8pm-9pm
		for (Payment payment : payments) {
			Calendar c = TimeManager.toCalendar(payment.getCreatedAt());
			c.setTimeZone(TimeZone.getTimeZone(timeZone));
			
			if (c.get(Calendar.HOUR_OF_DAY) == 6) {
			    paymentsSplitByTime.get("sixAm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 7) {
			    paymentsSplitByTime.get("sevenAm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 8) {
			    paymentsSplitByTime.get("eightAm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 9) {
			    paymentsSplitByTime.get("nineAm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 10) {
			    paymentsSplitByTime.get("tenAm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 11) {
			    paymentsSplitByTime.get("elevenAm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 12) {
			    paymentsSplitByTime.get("twelvePm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 13) {
			    paymentsSplitByTime.get("onePm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 14) {
			    paymentsSplitByTime.get("twoPm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 15) {
			    paymentsSplitByTime.get("threePm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 16) {
			    paymentsSplitByTime.get("fourPm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 17) {
			    paymentsSplitByTime.get("fivePm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 18) {
			    paymentsSplitByTime.get("sixPm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 19) {
			    paymentsSplitByTime.get("sevenPm").add(payment);
			} else if (c.get(Calendar.HOUR_OF_DAY) == 20) {
			    paymentsSplitByTime.get("eightPm").add(payment);
			}
		}
		
		return paymentsSplitByTime;
	}
	
	private String breakoutTimeTotalString(LinkedHashMap<String,Integer> totalsByTime) {
		StringBuilder sb = new StringBuilder();
		
		if (totalsByTime != null) {
			int total = 0;
			for (Integer integer: totalsByTime.values()) {
				total += integer;
			}
			
			sb.append(centsToDollars(total) + ",");
			
			for (Integer integer : totalsByTime.values()) {
				sb.append(centsToDollars(integer) + ",");
			}
			
			sb.setLength(sb.length() - 1);
		}
		
		return sb.toString();
	}
	
	private String centsToDollars(int cents) {
		// Why Canada? Because it displays negative currencies as "-$XX.YY".
		// US displays negative currencies as "($XX.YY)".
		NumberFormat n = NumberFormat.getCurrencyInstance(Locale.CANADA);
		return "\"" + n.format(cents / 100.0) + "\"";
	}
    
	private LinkedHashMap<String,Integer> totalGrossSalesByTime(Payment[] payments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);
		
		LinkedHashMap<String,Integer> totalGrossSalesByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalGrossSalesByTime.put(key, reportCalculator.totalGrossSales(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
		}
		
		return totalGrossSalesByTime;
	}

	private LinkedHashMap<String, Integer> totalGrossSalesRefundsByTime(Payment[] refundPayments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);
		
		LinkedHashMap<String,Integer> totalGrossSalesRefundsByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalGrossSalesRefundsByTime.put(key, reportCalculator.totalGrossSalesRefunds(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
		}
		
		return totalGrossSalesRefundsByTime;
	}

	private LinkedHashMap<String, Integer> totalMoneyCollectedForGiftCardsByTime(
			Payment[] payments) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private LinkedHashMap<String, Integer> totalMoneyCollectedForGiftCardsRefundsByTime(
			Payment[] refundPayments) {
		// TODO Auto-generated method stub
		return null;
	}

	private LinkedHashMap<String, Integer> totalTaxMoneyByTime(Payment[] payments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);
		
		LinkedHashMap<String,Integer> totalTaxMoneyByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalTaxMoneyByTime.put(key, reportCalculator.totalTaxMoney(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
		}
		
		return totalTaxMoneyByTime;
	}

	private LinkedHashMap<String, Integer> totalTaxMoneyRefundsByTime(Payment[] refundPayments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);
		
		LinkedHashMap<String,Integer> totalTaxMoneyRefundsByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalTaxMoneyRefundsByTime.put(key, reportCalculator.totalTaxMoneyRefunds(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
		}
		
		return totalTaxMoneyRefundsByTime;
	}

	private LinkedHashMap<String, Integer> totalTipMoneyByTime(Payment[] payments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);
		
		LinkedHashMap<String,Integer> totalTipMoneyByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalTipMoneyByTime.put(key, reportCalculator.totalTipMoney(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
		}
		
		return totalTipMoneyByTime;
	}

	private LinkedHashMap<String, Integer> totalTipMoneyRefundsByTime(Payment[] refundPayments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);
		
		LinkedHashMap<String,Integer> totalTipMoneyRefundsByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalTipMoneyRefundsByTime.put(key, reportCalculator.totalTipMoneyRefunds(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
		}
		
		return totalTipMoneyRefundsByTime;
	}

	private LinkedHashMap<String, Integer> totalTotalCollectedMoneyByTime(Payment[] payments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);
		
		LinkedHashMap<String,Integer> totalTotalCollectedMoneyByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalTotalCollectedMoneyByTime.put(key, reportCalculator.totalTotalCollectedMoney(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
		}
		
		return totalTotalCollectedMoneyByTime;
	}

	private LinkedHashMap<String, Integer> totalTotalCollectedMoneyRefundsByTime(Payment[] refundPayments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);
		
		LinkedHashMap<String,Integer> totalTotalCollectedMoneyRefundsByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalTotalCollectedMoneyRefundsByTime.put(key, reportCalculator.totalTotalCollectedMoneyRefunds(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
		}
		
		return totalTotalCollectedMoneyRefundsByTime;
	}

	private LinkedHashMap<String, Integer> totalDiscountMoneyByTime(Payment[] payments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);
		
		LinkedHashMap<String,Integer> totalDiscountedMoneyByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalDiscountedMoneyByTime.put(key, reportCalculator.totalDiscountMoney(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
		}
		
		return totalDiscountedMoneyByTime;
	}

	private LinkedHashMap<String, Integer> totalDiscountMoneyRefundsByTime(Payment[] refundPayments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);
		
		LinkedHashMap<String,Integer> totalDiscountMoneyRefundsByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalDiscountMoneyRefundsByTime.put(key, reportCalculator.totalDiscountMoneyRefunds(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
		}
		
		return totalDiscountMoneyRefundsByTime;
	}

	private LinkedHashMap<String, Integer> totalMoneyCollectedForTenderByTime(Payment[] payments, String tender) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);
		
		LinkedHashMap<String,Integer> totalMoneyCollectedForTenderByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalMoneyCollectedForTenderByTime.put(key, reportCalculator.totalMoneyCollectedForTender(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()]), tender));
		}
		
		return totalMoneyCollectedForTenderByTime;
	}

	private LinkedHashMap<String, Integer> totalMoneyCollectedForTenderRefundsByTime(Payment[] refundPayments, String tender) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);
		
		LinkedHashMap<String,Integer> totalMoneyCollectedForTenderRefundsByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalMoneyCollectedForTenderRefundsByTime.put(key, reportCalculator.totalMoneyCollectedForTenderRefunds(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()]), tender));
		}
		
		return totalMoneyCollectedForTenderRefundsByTime;
	}

	private LinkedHashMap<String, Integer> totalPartialRefundsByTime() throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(new Payment[]{});
		
		LinkedHashMap<String,Integer> totalPartialRefundsByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			totalPartialRefundsByTime.put(key, 0);
		}
		
		return totalPartialRefundsByTime;
	}

	private LinkedHashMap<String, Integer> totalPartialRefundsRefundsByTime(Payment[] refundPayments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);
		
		LinkedHashMap<String,Integer> totalPartialRefundsRefundsByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalPartialRefundsRefundsByTime.put(key, reportCalculator.totalPartialRefundsRefunds(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
		}
		
		return totalPartialRefundsRefundsByTime;
	}

	private LinkedHashMap<String, Integer> totalProcessingFeeMoneyByTime(Payment[] payments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(payments);
		
		LinkedHashMap<String,Integer> totalProcessingFeeMoneyByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalProcessingFeeMoneyByTime.put(key, reportCalculator.totalProcessingFeeMoney(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
		}
		
		return totalProcessingFeeMoneyByTime;
	}

	private LinkedHashMap<String, Integer> totalProcessingFeeMoneyRefundsByTime(Payment[] refundPayments) throws ParseException {
		LinkedHashMap<String,List<Payment>> paymentsSplitByTime = splitPaymentsByTime(refundPayments);
		
		LinkedHashMap<String,Integer> totalProcessingFeeMoneyByTime = new LinkedHashMap<String,Integer>();
		
		for (String key : paymentsSplitByTime.keySet()) {
			List<Payment> paymentsInTimeInterval = paymentsSplitByTime.get(key);
			totalProcessingFeeMoneyByTime.put(key, reportCalculator.totalProcessingFeeMoneyRefunds(paymentsInTimeInterval.toArray(new Payment[paymentsInTimeInterval.size()])));
		}
		
		return totalProcessingFeeMoneyByTime;
	}
}