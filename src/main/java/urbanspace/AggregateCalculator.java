package urbanspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

import util.SquarePayload;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentDiscount;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.Tender;

public class AggregateCalculator implements Callable {

    @Override
    public Object onCall(MuleEventContext eventContext) throws Exception {
        @SuppressWarnings("unchecked")
        List<SquarePayload> squarePayloads = (List<SquarePayload>) eventContext.getMessage().getPayload();
        
        ArrayList<LocationResult> lrs = new ArrayList<LocationResult>();
        
        for (SquarePayload payload : squarePayloads) {
            Payment[] payments = (Payment[]) payload.getResults().get("paymentsFromLocation");
            
            LocationResult lr = new LocationResult();
            lr.setMerchantId(payload.getLocationId());

            lr.setGiftCardSales(totalMoneyCollectedForGiftCards(payments));

            lr.setTotalTaxMoney(totalTaxMoney(payments));
            lr.setTotalTipMoney(totalTipMoney(payments));
            lr.setTotalCollectedMoney(totalTotalCollectedMoney(payments));
            lr.setTotalDiscountsMoney(totalDiscountMoney(payments));

            // gross sales = net sales + discounts
            // total collected = net sales + tax + tips
            // total collected + discounts - tax - tips = gross sales
            lr.setGrossSales(grossSales(lr.getTotalCollectedMoney(),
                    lr.getTotalDiscountsMoney(),
                    lr.getTotalTaxMoney(),
                    lr.getTotalTipMoney()));
            lr.setNetSales(netSales(lr.getGrossSales(), lr.getTotalDiscountsMoney()));
            
            lr.setTotalCashMoney(totalMoneyCollectedForTender(payments, "CASH"));
            lr.setTotalCardMoney(totalMoneyCollectedForTender(payments, "CREDIT_CARD"));
            lr.setTotalGiftCardMoney(gitCardSales(totalMoneyCollectedForTender(payments, "SQUARE_GIFT_CARD"),
                    totalMoneyCollectedForTender(payments, "THIRD_PARTY_CARD")));
            lr.setTotalOtherMoney(totalOtherMoney(totalMoneyCollectedForTender(payments, "NO_SALE"),
                    totalMoneyCollectedForTender(payments, "SQUARE_WALLET"),
                    totalMoneyCollectedForTender(payments, "UNKNOWN"),
                    totalMoneyCollectedForTender(payments, "OTHER")));
            
            lr.setTotalFeesMoney(totalProcessingFeeMoney(payments));
            lr.setNetTotalMoney(netTotalMoney(lr.getTotalCollectedMoney(), lr.getTotalFeesMoney()));
            
            lr.setCategorySales(totalSalesPerCategory(payments));
            lr.setTotalsPerDiscount(totalSalesPerDiscount(payments));
            
            lr.setTotalCardSwipedMoney(totalMoneyCollectedForCardEntryMethod(payments, "SWIPED"));
            // No can do on dips or taps
            lr.setTotalCardKeyedMoney(totalMoneyCollectedForCardEntryMethod(payments, "MANUAL"));
            lr.setTotalVisaMoney(totalMoneyCollectedForCardBrand(payments, "VISA"));
            lr.setTotalMasterCardMoney(totalMoneyCollectedForCardBrand(payments, "MASTER_CARD"));
            lr.setTotalDiscoverMoney(totalMoneyCollectedForCardBrand(payments, "DISCOVER"));
            lr.setTotalAmexMoney(totalMoneyCollectedForCardBrand(payments, "AMERICAN_EXPRESS"));
            lr.setTotalOtherCardMoney(totalOtherCardMoney(totalMoneyCollectedForCardBrand(payments, "DISCOVER_DINERS"),
                    totalMoneyCollectedForCardBrand(payments, "JCB"),
                    totalMoneyCollectedForCardBrand(payments, "UNKNOWN")));
            
            lrs.add(lr);
        }
        
        return lrs;
    }
    
    private Map<String,Integer> totalTaxMoney(Payment[] payments) {
        int total = 0;
        Map<String,Integer> results = newLocationResultsMap();
        for (Payment payment : payments) {
            if (payment.getTaxMoney() != null) {
                total += payment.getTaxMoney().getAmount();
                insertIntoLocationResultsMap(results, payment.getCreatedAt(), payment.getTaxMoney().getAmount());
            }
        }
        
        results.put("Total", total);
        
        return results;
    }
    
    private Map<String,Integer> totalTipMoney(Payment[] payments) {
        int total = 0;
        Map<String,Integer> results = newLocationResultsMap();
        for (Payment payment : payments) {
            if (payment.getTipMoney() != null) {
                total += payment.getTipMoney().getAmount();
                insertIntoLocationResultsMap(results, payment.getCreatedAt(), payment.getTipMoney().getAmount());
            }
        }
        
        results.put("Total", total);
        
        return results;
    }
    
    private Map<String,Integer> totalDiscountMoney(Payment[] payments) {
        int total = 0;
        Map<String,Integer> results = newLocationResultsMap();
        for (Payment payment : payments) {
            if (payment.getDiscountMoney() != null) {
                total -= payment.getDiscountMoney().getAmount();
                insertIntoLocationResultsMap(results, payment.getCreatedAt(), -payment.getDiscountMoney().getAmount());
            }
        }
        
        results.put("Total", total);
        
        return results;
    }

    private Map<String,Integer> totalTotalCollectedMoney(Payment[] payments) {
        int total = 0;
        Map<String,Integer> results = newLocationResultsMap();
        for (Payment payment : payments) {
            if (payment.getTotalCollectedMoney() != null) {
                total += payment.getTotalCollectedMoney().getAmount();
                insertIntoLocationResultsMap(results, payment.getCreatedAt(), payment.getTotalCollectedMoney().getAmount());
            }
        }
        
        results.put("Total", total);
        
        return results;
    }
    
    private Map<String,Integer> totalProcessingFeeMoney(Payment[] payments) {
        int total = 0;
        Map<String,Integer> results = newLocationResultsMap();
        for (Payment payment : payments) {
            if (payment.getProcessingFeeMoney() != null) {
                total -= payment.getProcessingFeeMoney().getAmount();
                insertIntoLocationResultsMap(results, payment.getCreatedAt(), -payment.getProcessingFeeMoney().getAmount());
            }
        }
        
        results.put("Total", total);
        
        return results;
    }
    
    private Map<String,Integer> totalMoneyCollectedForTender(Payment[] payments, String tenderType) {
        int total = 0;
        Map<String,Integer> results = newLocationResultsMap();
        for (Payment payment : payments) {
            for (Tender tender : payment.getTender()) {
                if (tenderType.equals(tender.getType())) {
                    if (tender.getTenderedMoney() != null) {
                    	total += tender.getTenderedMoney().getAmount();
                        insertIntoLocationResultsMap(results, payment.getCreatedAt(), tender.getTenderedMoney().getAmount());
                    } else {
                    	total += tender.getTotalMoney().getAmount();
                        insertIntoLocationResultsMap(results, payment.getCreatedAt(), tender.getTotalMoney().getAmount());
                    }
                }
            }
        }
        
        results.put("Total", total);
        
        return results;
    }
    
    private Map<String,Map<String,Integer>> totalSalesPerCategory(Payment[] payments) {
        HashMap<String, Map<String, Integer>> categorySales = new HashMap<String,Map<String,Integer>>();
        
        for (Payment payment : payments) {
            for (PaymentItemization paymentItemization : payment.getItemizations()) {
                if (!categorySales.containsKey(paymentItemization.getItemDetail().getCategoryName())) {
                    categorySales.put(paymentItemization.getItemDetail().getCategoryName(),
                		newLocationResultsMap());
                    categorySales.get(paymentItemization.getItemDetail().getCategoryName()).put("Total", 0);
                }
                
                Map<String,Integer> categoryResultMap = categorySales.get(paymentItemization.getItemDetail().getCategoryName());
                insertIntoLocationResultsMap(categoryResultMap, payment.getCreatedAt(), paymentItemization.getTotalMoney().getAmount());
                categoryResultMap.put("Total", categoryResultMap.get("Total") + paymentItemization.getTotalMoney().getAmount());
            }
        }
        
        if (categorySales.containsKey("")) {
            categorySales.put("No category", categorySales.get(""));
            categorySales.remove("");
        }
        
        return categorySales;
    }
    
    private Map<String,Map<String,Integer>> totalSalesPerDiscount(Payment[] payments) {
        HashMap<String, Map<String, Integer>> discountSales = new HashMap<String,Map<String,Integer>>();
        
        for (Payment payment : payments) {
            for (PaymentItemization paymentItemization : payment.getItemizations()) {
                for (PaymentDiscount paymentDiscount : paymentItemization.getDiscounts()) {
                    if (!discountSales.containsKey(paymentDiscount.getName())) {
                        discountSales.put(paymentDiscount.getName(), newLocationResultsMap());
                        discountSales.get(paymentDiscount.getName()).put("Total", 0);
                    }
                    
                    Map<String,Integer> discountResultMap = discountSales.get(paymentDiscount.getName());
                    insertIntoLocationResultsMap(discountResultMap, payment.getCreatedAt(), -paymentDiscount.getAppliedMoney().getAmount());
                    discountResultMap.put("Total", discountResultMap.get("Total") - paymentDiscount.getAppliedMoney().getAmount());
                }
            }
        }
        
        return discountSales;
    }
    
    private Map<String,Integer> totalMoneyCollectedForCardEntryMethod(Payment[] payments, String method) {
    	int total = 0;
        Map<String,Integer> results = newLocationResultsMap();
        for (Payment payment : payments) {
            for (Tender tender : payment.getTender()) {
                if (method.equals(tender.getEntryMethod())) {
                    if (tender.getTenderedMoney() != null) {
                    	total += tender.getTenderedMoney().getAmount();
                        insertIntoLocationResultsMap(results, payment.getCreatedAt(), tender.getTenderedMoney().getAmount());
                    } else {
                    	total += tender.getTotalMoney().getAmount();
                        insertIntoLocationResultsMap(results, payment.getCreatedAt(), tender.getTotalMoney().getAmount());
                    }
                }
            }
        }
        
        results.put("Total", total);
        
        return results;
    }
    
    private Map<String,Integer> totalMoneyCollectedForCardBrand(Payment[] payments, String brand) {
    	int total = 0;
        Map<String,Integer> results = newLocationResultsMap();
        for (Payment payment : payments) {
            for (Tender tender : payment.getTender()) {
                if (brand.equals(tender.getCardBrand())) {
                    if (tender.getTenderedMoney() != null) {
                    	total += tender.getTenderedMoney().getAmount();
                        insertIntoLocationResultsMap(results, payment.getCreatedAt(), tender.getTenderedMoney().getAmount());
                    } else {
                    	total += tender.getTotalMoney().getAmount();
                        insertIntoLocationResultsMap(results, payment.getCreatedAt(), tender.getTotalMoney().getAmount());
                    }
                }
            }
        }
        
        results.put("Total", total);
        
        return results;
    }
    
    private Map<String,Integer> totalMoneyCollectedForGiftCards(Payment[] payments) {
    	int total = 0;
        Map<String,Integer> results = newLocationResultsMap();
        for (Payment payment : payments) {
            for (PaymentItemization paymentItemization : payment.getItemizations()) {
                if (paymentItemization.getItemizationType().contains("GIFT_CARD") && paymentItemization.getTotalMoney() != null) {
                	total += paymentItemization.getTotalMoney().getAmount();
                    insertIntoLocationResultsMap(results, payment.getCreatedAt(), paymentItemization.getTotalMoney().getAmount());
                }
            }
        }
        
        results.put("Total", total);
        
        return results;
    }
	
	private Map<String,Integer> grossSales(Map<String,Integer> collected,
			Map<String,Integer> discounts,
			Map<String,Integer> tax,
			Map<String,Integer> tips) {
		
		Map<String,Integer> results = new HashMap<String,Integer>();
		
		for (String key : collected.keySet()) {
			results.put(key, collected.get(key) + discounts.get(key) - tax.get(key) - tips.get(key));
		}
		
		return results;
	}
	
	private Map<String,Integer> netSales(Map<String,Integer> grossSales, Map<String,Integer> discounts) {
		Map<String,Integer> results = new HashMap<String,Integer>();
		
		for (String key : grossSales.keySet()) {
			results.put(key, grossSales.get(key) - discounts.get(key));
		}
		
		return results;
	}
	
	private Map<String,Integer> gitCardSales(Map<String,Integer> squareGiftCards, Map<String,Integer> otherGiftCards) {
		Map<String,Integer> results = new HashMap<String,Integer>();
		
		for (String key : squareGiftCards.keySet()) {
			results.put(key, squareGiftCards.get(key) + otherGiftCards.get(key));
		}
		
		return results;
	}
	
	private Map<String,Integer> totalOtherMoney(Map<String,Integer> noSale,
			Map<String,Integer> squareWallet,
			Map<String,Integer> unknown,
			Map<String,Integer> other) {
		
		Map<String,Integer> results = new HashMap<String,Integer>();
		
		for (String key : noSale.keySet()) {
			results.put(key, noSale.get(key) + squareWallet.get(key) + unknown.get(key) + other.get(key));
		}
		
		return results;
	}
	
	private Map<String,Integer> netTotalMoney(Map<String,Integer> collected, Map<String,Integer> fees) {
		Map<String,Integer> results = new HashMap<String,Integer>();
		
		for (String key : collected.keySet()) {
			results.put(key, collected.get(key) - fees.get(key));
		}
		
		return results;
	}
	
	private Map<String,Integer> totalOtherCardMoney(Map<String,Integer> discoverDiners,
			Map<String,Integer> jcb,
			Map<String,Integer> unknown) {
		
		Map<String,Integer> results = new HashMap<String,Integer>();
		
		for (String key : discoverDiners.keySet()) {
			results.put(key, discoverDiners.get(key) + jcb.get(key) + unknown.get(key));
		}
		
		return results;
	}
    
    private Map<String,Integer> newLocationResultsMap() {
        HashMap<String,Integer> m = new HashMap<String,Integer>();
        m.put("6am-7am", 0);
        m.put("7am-8am", 0);
        m.put("8am-9am", 0);
        m.put("9am-10am", 0);
        m.put("10am-11am", 0);
        m.put("11am-12pm", 0);
        m.put("12pm-1pm", 0);
        m.put("1pm-2pm", 0);
        m.put("2pm-3pm", 0);
        m.put("3pm-4pm", 0);
        m.put("4pm-5pm", 0);
        m.put("5pm-6pm", 0);
        m.put("6pm-7pm", 0);
        m.put("7pm-8pm", 0);
        m.put("8pm-9pm", 0);
        return m;
    }
    
    private void insertIntoLocationResultsMap(Map<String,Integer> m, String isoDate, Integer i) {
        int h = Integer.parseInt(isoDate.substring(11, 13));
        switch(h) {
            case 6: m.put("6am-7am", m.get("6am-7am") + i);
            break;
            case 7: m.put("7am-8am", m.get("7am-8am") + i);
            break;
            case 8: m.put("8am-9am", m.get("8am-9am") + i);
            break;
            case 9: m.put("9am-10am", m.get("9am-10am") + i);
            break;
            case 10: m.put("10am-11am", m.get("10am-11am") + i);
            break;
            case 11: m.put("11am-12pm", m.get("11am-12pm") + i);
            break;
            case 12: m.put("12pm-1pm", m.get("12pm-1pm") + i);
            break;
            case 13: m.put("1pm-2pm", m.get("1pm-2pm") + i);
            break;
            case 14: m.put("2pm-3pm", m.get("2pm-3pm") + i);
            break;
            case 15: m.put("3pm-4pm", m.get("3pm-4pm") + i);
            break;
            case 16: m.put("4pm-5pm", m.get("4pm-5pm") + i);
            break;
            case 17: m.put("5pm-6pm", m.get("5pm-6pm") + i);
            break;
            case 18: m.put("6pm-7pm", m.get("6pm-7pm") + i);
            break;
            case 19: m.put("7pm-8pm", m.get("7pm-8pm") + i);
            break;
            case 20: m.put("8pm-9pm", m.get("8pm-9pm") + i);
            break;
        }
    }
}
