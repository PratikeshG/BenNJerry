package urbanspace;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.squareup.connect.Payment;

public class AggregateReportGenerator {

	private String timeZone;
	private int offset;
	private int range;
	
	public AggregateReportGenerator(String timeZone, int offset, int range) {
		this.timeZone = timeZone;
		this.offset = offset;
		this.range = range;
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
	
	public String generate(List<ReportGeneratorPayload> reportGeneratorPayloads) throws ParseException {
		ArrayList<AggregateLocationResult> locationResults = new ArrayList<AggregateLocationResult>();
		ReportCalculator reportCalculator = new ReportCalculator(range, offset, timeZone);
		
		for (ReportGeneratorPayload reportGeneratorPayload: reportGeneratorPayloads) {
			Payment[] payments = reportGeneratorPayload.getPayments();
			Payment[] refundPayments = reportGeneratorPayload.getRefundPayments();
			
			// Dedupe refunds objects
			Map<String,Payment> deduped = new HashMap<String,Payment>();
			for (Payment refund : refundPayments) {
				deduped.put(refund.getId(), refund);
			}
			refundPayments = deduped.values().toArray(new Payment[deduped.size()]);
			
			AggregateLocationResult locationResult = new AggregateLocationResult();
			
            locationResult.setMerchantId(reportGeneratorPayload.getMerchantId());
            locationResult.setMerchantName(reportGeneratorPayload.getMerchantAlias());

            locationResult.setGiftCardSales(reportCalculator.totalMoneyCollectedForGiftCards(payments));
            locationResult.setGiftCardSalesRefunds(reportCalculator.totalMoneyCollectedForGiftCardsRefunds(refundPayments));
            locationResult.setGiftCardSalesNet(locationResult.getGiftCardSales() + locationResult.getGiftCardSalesRefunds());

            locationResult.setTotalTaxMoney(reportCalculator.totalTaxMoney(payments));
            locationResult.setTotalTaxMoneyRefunds(reportCalculator.totalTaxMoneyRefunds(refundPayments));
            locationResult.setTotalTaxMoneyNet(locationResult.getTotalTaxMoney() + locationResult.getTotalTaxMoneyRefunds());
            
            locationResult.setTotalTipMoney(reportCalculator.totalTipMoney(payments));
            locationResult.setTotalTipMoneyRefunds(reportCalculator.totalTipMoneyRefunds(refundPayments));
            locationResult.setTotalTipMoneyNet(locationResult.getTotalTipMoney() + locationResult.getTotalTipMoneyRefunds());

            // total collected + discounts - tax - tips = gross sales
            locationResult.setTotalCollectedMoney(reportCalculator.totalTotalCollectedMoney(payments));
            locationResult.setTotalCollectedMoneyRefunds(reportCalculator.totalTotalCollectedMoneyRefunds(refundPayments));
            locationResult.setTotalCollectedMoneyNet(locationResult.getTotalCollectedMoney() + locationResult.getTotalCollectedMoneyRefunds());

            locationResult.setTotalDiscountsMoney(reportCalculator.totalDiscountMoney(payments));
            locationResult.setTotalDiscountsMoneyRefunds(reportCalculator.totalDiscountMoneyRefunds(refundPayments));
            locationResult.setTotalDiscountsMoneyNet(locationResult.getTotalDiscountsMoney() + locationResult.getTotalDiscountsMoneyRefunds());

            // gross sales = net sales + discounts
            locationResult.setGrossSales(reportCalculator.totalGrossSales(payments));
            locationResult.setGrossSalesRefunds(reportCalculator.totalGrossSalesRefunds(refundPayments));
            locationResult.setGrossSalesNet(locationResult.getGrossSales() + locationResult.getGrossSalesRefunds());

            locationResult.setNetSales(locationResult.getGrossSales() + locationResult.getTotalDiscountsMoney());
            locationResult.setNetSalesRefunds(locationResult.getGrossSalesRefunds() + locationResult.getTotalDiscountsMoneyRefunds());
            locationResult.setNetSalesNet(locationResult.getNetSales() + locationResult.getNetSalesRefunds());

            locationResult.setTotalCashMoney(reportCalculator.totalMoneyCollectedForTender(payments, "CASH"));
            locationResult.setTotalCashMoneyRefunds(reportCalculator.totalMoneyCollectedForTenderRefunds(refundPayments, "CASH"));
            locationResult.setTotalCashMoneyNet(locationResult.getTotalCashMoney() + locationResult.getTotalCashMoneyRefunds());

            locationResult.setTotalCardMoney(reportCalculator.totalMoneyCollectedForTender(payments, "CREDIT_CARD"));
            locationResult.setTotalCardMoneyRefunds(reportCalculator.totalMoneyCollectedForTenderRefunds(refundPayments, "CREDIT_CARD"));
            locationResult.setTotalCardMoneyNet(locationResult.getTotalCardMoney() + locationResult.getTotalCardMoneyRefunds());

            locationResult.setTotalGiftCardMoney(reportCalculator.totalMoneyCollectedForTender(payments, "SQUARE_GIFT_CARD"));
            locationResult.setTotalGiftCardMoneyRefunds(reportCalculator.totalMoneyCollectedForTenderRefunds(refundPayments, "SQUARE_GIFT_CARD"));
            locationResult.setTotalGiftCardMoneyNet(locationResult.getTotalGiftCardMoney() + locationResult.getTotalGiftCardMoneyRefunds());

            locationResult.setTotalOtherMoney(reportCalculator.totalMoneyCollectedForTender(payments, "NO_SALE") + reportCalculator.totalMoneyCollectedForTender(payments, "SQUARE_WALLET") + reportCalculator.totalMoneyCollectedForTender(payments, "UNKNOWN") + reportCalculator.totalMoneyCollectedForTender(payments, "OTHER") + reportCalculator.totalMoneyCollectedForTender(payments, "THIRD_PARTY_CARD"));
            locationResult.setTotalOtherMoneyRefunds(reportCalculator.totalMoneyCollectedForTenderRefunds(refundPayments, "NO_SALE") + reportCalculator.totalMoneyCollectedForTenderRefunds(refundPayments, "SQUARE_WALLET") + reportCalculator.totalMoneyCollectedForTenderRefunds(refundPayments, "UNKNOWN") + reportCalculator.totalMoneyCollectedForTenderRefunds(refundPayments, "OTHER") + reportCalculator.totalMoneyCollectedForTenderRefunds(payments, "THIRD_PARTY_CARD"));
            locationResult.setTotalOtherMoneyNet(locationResult.getTotalOtherMoney() + locationResult.getTotalOtherMoneyRefunds());

            locationResult.setTotalPartialRefundsMoney(0);
            locationResult.setTotalPartialRefundsMoneyRefunds(reportCalculator.totalPartialRefundsRefunds(refundPayments));
            locationResult.setTotalPartialRefundsMoneyNet(locationResult.getTotalPartialRefundsMoney() + locationResult.getTotalPartialRefundsMoneyRefunds());

            locationResult.setTotalFeesMoney(reportCalculator.totalProcessingFeeMoney(payments));
            locationResult.setTotalFeesMoneyRefunds(reportCalculator.totalProcessingFeeMoneyRefunds(refundPayments));
            locationResult.setTotalFeesMoneyNet(locationResult.getTotalFeesMoney() + locationResult.getTotalFeesMoneyRefunds());

            locationResult.setNetTotalMoney(locationResult.getTotalCollectedMoney() + locationResult.getTotalFeesMoney());
            locationResult.setNetTotalMoneyRefunds(locationResult.getTotalCollectedMoneyRefunds() + locationResult.getTotalFeesMoneyRefunds());
            locationResult.setNetTotalMoneyNet(locationResult.getNetTotalMoney() + locationResult.getNetTotalMoneyRefunds());
            
            locationResults.add(locationResult);
        }
        
		// Sort results alphabetically, by merchant name
		locationResults.sort(new Comparator<AggregateLocationResult>() {
			@Override
			public int compare (AggregateLocationResult a1, AggregateLocationResult a2) {
				return a1.getMerchantName().compareTo(a2.getMerchantName());
			}
		});
		
		// TODO(colinlam): create a new class that makes this more clear.
		// i.e. addRow(new CSVRow({
		//   merchantId: locationResult.getMerchantId(),
		//   grossSales: locationResult.getGrossSales(),
		//   ...
		// }))
		StringBuilder sb = new StringBuilder();
		sb.append("Merchant Name,SUID,Gross Sales,Gross Sales (Refunds),Gross Sales (Net),Discounts,Discounts (Refunds),Discounts (Net),Net Sales,Net Sales (Refunds),Net Sales (Net),Gift Card Sales,Gift Card Sales (Refunds),Gift Card Sales (Net),Tax,Tax (Refunds),Tax (Net),Tips,Tips (Refunds),Tips (Net),Partial Refunds,Partial Refunds (Refunds),Partial Refunds (Net),Total Collected,Total Collected (Refunds),Total Collected (Net),Cash,Cash (Refunds),Cash (Net),Card,Card (Refunds),Card (Net),Gift Card,Gift Card (Refunds),Gift Card (Net),Other,Other (Refunds),Other (Net),Fees,Fees (Refunds),Fees (Net),Net Total,Net Total (Refunds),Net Total (Net)\n");
		for (AggregateLocationResult locationResult : locationResults) {
			sb.append(locationResult.getMerchantName() + ",");
			sb.append(locationResult.getMerchantId() + ",");
			sb.append(centsToDollars(locationResult.getGrossSales()) + ",");
			sb.append(centsToDollars(locationResult.getGrossSalesRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getGrossSalesNet()) + ",");
			sb.append(centsToDollars(locationResult.getTotalDiscountsMoney()) + ",");
			sb.append(centsToDollars(locationResult.getTotalDiscountsMoneyRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getTotalDiscountsMoneyNet()) + ",");
			sb.append(centsToDollars(locationResult.getNetSales()) + ",");
			sb.append(centsToDollars(locationResult.getNetSalesRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getNetSalesNet()) + ",");
			sb.append(centsToDollars(locationResult.getGiftCardSales()) + ",");
			sb.append(centsToDollars(locationResult.getGiftCardSalesRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getGiftCardSalesNet()) + ",");
			sb.append(centsToDollars(locationResult.getTotalTaxMoney()) + ",");
			sb.append(centsToDollars(locationResult.getTotalTaxMoneyRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getTotalTaxMoneyNet()) + ",");
			sb.append(centsToDollars(locationResult.getTotalTipMoney()) + ",");
			sb.append(centsToDollars(locationResult.getTotalTipMoneyRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getTotalTipMoneyNet()) + ",");
			sb.append(centsToDollars(locationResult.getTotalPartialRefundsMoney()) + ",");
			sb.append(centsToDollars(locationResult.getTotalPartialRefundsMoneyRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getTotalPartialRefundsMoneyNet()) + ",");
			sb.append(centsToDollars(locationResult.getTotalCollectedMoney()) + ",");
			sb.append(centsToDollars(locationResult.getTotalCollectedMoneyRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getTotalCollectedMoneyNet()) + ",");
			sb.append(centsToDollars(locationResult.getTotalCashMoney()) + ",");
			sb.append(centsToDollars(locationResult.getTotalCashMoneyRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getTotalCashMoneyNet()) + ",");
			sb.append(centsToDollars(locationResult.getTotalCardMoney()) + ",");
			sb.append(centsToDollars(locationResult.getTotalCardMoneyRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getTotalCardMoneyNet()) + ",");
			sb.append(centsToDollars(locationResult.getTotalGiftCardMoney()) + ",");
			sb.append(centsToDollars(locationResult.getTotalGiftCardMoneyRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getTotalGiftCardMoneyNet()) + ",");
			sb.append(centsToDollars(locationResult.getTotalOtherMoney()) + ",");
			sb.append(centsToDollars(locationResult.getTotalOtherMoneyRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getTotalOtherMoneyNet()) + ",");
			sb.append(centsToDollars(locationResult.getTotalFeesMoney()) + ",");
			sb.append(centsToDollars(locationResult.getTotalFeesMoneyRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getTotalFeesMoneyNet()) + ",");
			sb.append(centsToDollars(locationResult.getNetTotalMoney()) + ",");
			sb.append(centsToDollars(locationResult.getNetTotalMoneyRefunds()) + ",");
			sb.append(centsToDollars(locationResult.getNetTotalMoneyNet()) + "\n");
		}
		
		return sb.toString();
	}

	private String centsToDollars(int cents) {
		// Why Canada? Because it displays negative currencies as "-$XX.YY".
		// US displays negative currencies as "($XX.YY)".
		NumberFormat n = NumberFormat.getCurrencyInstance(Locale.CANADA);
		return "\"" + n.format(cents / 100.0) + "\"";
	}
}