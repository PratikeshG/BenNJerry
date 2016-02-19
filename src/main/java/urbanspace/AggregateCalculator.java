package urbanspace;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

import util.SquarePayload;
import util.payment.AggregateReporter;

import com.squareup.connect.Payment;

public class AggregateCalculator implements Callable {

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		@SuppressWarnings("unchecked")
		List<SquarePayload> squarePayloads = (List<SquarePayload>) eventContext.getMessage().getPayload();
		
		ArrayList<LocationResults> lrs = new ArrayList<LocationResults>();
		
		for (SquarePayload payload : squarePayloads) {
			Payment[] payments = (Payment[]) payload.getResults().get("paymentsFromLocation");
			
			LocationResults lr = new LocationResults();
			lr.setMerchantId(payload.getLocationId());

			lr.setGiftCardSales(AggregateReporter.totalMoneyCollectedForGiftCards(payments));

			lr.setTotalTaxMoney(AggregateReporter.totalTaxMoney(payments));
			lr.setTotalTipMoney(AggregateReporter.totalTipMoney(payments));
			lr.setTotalCollectedMoney(AggregateReporter.totalTotalCollectedMoney(payments));
			lr.setTotalDiscountsMoney(AggregateReporter.totalDiscountMoney(payments));

			// gross sales = net sales + discounts
			// total collected = net sales + tax + tips
			// total collected + discounts - tax - tips = gross sales
			lr.setGrossSales(lr.getTotalCollectedMoney() +
					lr.getTotalDiscountsMoney() -
					lr.getTotalTaxMoney() -
					lr.getTotalTipMoney());
			lr.setNetSales(lr.getGrossSales() - lr.getTotalDiscountsMoney());
			
			lr.setTotalCashMoney(AggregateReporter.totalMoneyCollectedForTender(payments, "CASH"));
			lr.setTotalCardMoney(AggregateReporter.totalMoneyCollectedForTender(payments, "CREDIT_CARD"));
			lr.setTotalGiftCardMoney(AggregateReporter.totalMoneyCollectedForTender(payments, "SQUARE_GIFT_CARD") +
					AggregateReporter.totalMoneyCollectedForTender(payments, "THIRD_PARTY_CARD"));
			lr.setTotalOtherMoney(AggregateReporter.totalMoneyCollectedForTender(payments, "NO_SALE") +
					AggregateReporter.totalMoneyCollectedForTender(payments, "SQUARE_WALLET") +
					AggregateReporter.totalMoneyCollectedForTender(payments, "UNKNOWN") +
					AggregateReporter.totalMoneyCollectedForTender(payments, "OTHER"));
			
			lr.setTotalFeesMoney(AggregateReporter.totalProcessingFeeMoney(payments));
			lr.setNetTotalMoney(lr.getTotalCollectedMoney() - lr.getTotalFeesMoney());
			
			lr.setCategorySales(AggregateReporter.totalSalesPerCategory(payments));
			lr.setTotalsPerDiscount(AggregateReporter.totalSalesPerDiscount(payments));
			
			lr.setTotalCardSwipedMoney(AggregateReporter.totalMoneyCollectedForCardEntryMethod(payments, "SWIPED"));
			// No can do on dips or taps
			lr.setTotalCardKeyedMoney(AggregateReporter.totalMoneyCollectedForCardEntryMethod(payments, "MANUAL"));
			lr.setTotalVisaMoney(AggregateReporter.totalMoneyCollectedForCardBrand(payments, "VISA"));
			lr.setTotalMasterCardMoney(AggregateReporter.totalMoneyCollectedForCardBrand(payments, "MASTER_CARD"));
			lr.setTotalDiscoverMoney(AggregateReporter.totalMoneyCollectedForCardBrand(payments, "DISCOVER"));
			lr.setTotalAmexMoney(AggregateReporter.totalMoneyCollectedForCardBrand(payments, "AMERICAN_EXPRESS"));
			lr.setTotalOtherCardMoney(AggregateReporter.totalMoneyCollectedForCardBrand(payments, "DISCOVER_DINERS") +
					AggregateReporter.totalMoneyCollectedForCardBrand(payments, "JCB") +
					AggregateReporter.totalMoneyCollectedForCardBrand(payments, "UNKNOWN"));
			
			lrs.add(lr);
		}
		
		return lrs;
	}
}