package util.payment;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentDiscount;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.Tender;

public class AggregateReporter {
	
	public static int totalInclusiveTaxMoney(Payment[] payments) {
		int total = 0;
		for (Payment payment : payments) {
			if (payment.getInclusiveTaxMoney() != null) {
				total += payment.getInclusiveTaxMoney().getAmount();				
			}
		}
		return total;
	}
	
	public static int totalAdditiveTaxMoney(Payment[] payments) {
		int total = 0;
		for (Payment payment : payments) {
			if (payment.getAdditiveTaxMoney() != null) {
				total += payment.getAdditiveTaxMoney().getAmount();
			}
		}
		return total;
	}
	
	public static int totalTaxMoney(Payment[] payments) {
		int total = 0;
		for (Payment payment : payments) {
			if (payment.getTaxMoney() != null) {
				total += payment.getTaxMoney().getAmount();
			}
		}
		return total;
	}
	
	public static int totalTipMoney(Payment[] payments) {
		int total = 0;
		for (Payment payment : payments) {
			if (payment.getTipMoney() != null) {
				total += payment.getTipMoney().getAmount();
			}
		}
		return total;
	}
	
	public static int totalDiscountMoney(Payment[] payments) {
		int total = 0;
		for (Payment payment : payments) {
			if (payment.getDiscountMoney() != null) {
				total -= payment.getDiscountMoney().getAmount();
			}
		}
		return total;
	}

	public static int totalTotalCollectedMoney(Payment[] payments) {
		int total = 0;
		for (Payment payment : payments) {
			if (payment.getTotalCollectedMoney() != null) {
				total += payment.getTotalCollectedMoney().getAmount();
			}
		}
		return total;
	}
	
	public static int totalProcessingFeeMoney(Payment[] payments) {
		int total = 0;
		for (Payment payment : payments) {
			if (payment.getProcessingFeeMoney() != null) {
				total -= payment.getProcessingFeeMoney().getAmount();
			}
		}
		return total;
	}
	
	public static int totalNetTotalMoney(Payment[] payments) {
		int total = 0;
		for (Payment payment : payments) {
			if (payment.getNetTotalMoney() != null) {
				total += payment.getNetTotalMoney().getAmount();
			}
		}
		return total;
	}
	
	public static int totalRefundedMoney(Payment[] payments) {
		int total = 0;
		for (Payment payment : payments) {
			total += payment.getRefundedMoney().getAmount();
		}
		return total;
	}
	
	// Below methods are not reflective of the Payment object
	
	public static int totalMoneyCollectedForTender(Payment[] payments, String tenderType) {
		// Possible values for tender: CREDIT_CARD, CASH, THIRD_PARTY_CARD,
		// NO_SALE, SQUARE_WALLET, SQUARE_GIFT_CARD, UNKNOWN, OTHER.
		int total = 0;
		for (Payment payment : payments) {
			for (Tender tender : payment.getTender()) {
				if (tenderType.equals(tender.getType())) {
					if (tender.getTenderedMoney() != null) {
						total += tender.getTenderedMoney().getAmount();
					} else {
						total += tender.getTotalMoney().getAmount();
					}
				}
			}
		}
		return total;
	}
	
	public static int totalNumberOfPaymentsCollectedForTender(Payment[] payments, String tenderType) {
		// Possible values for tender: CREDIT_CARD, CASH, THIRD_PARTY_CARD,
		// NO_SALE, SQUARE_WALLET, SQUARE_GIFT_CARD, UNKNOWN, OTHER.
		int total = 0;
		for (Payment payment : payments) {
			for (Tender tender : payment.getTender()) {
				if (tenderType.equals(tender.getType())) {
					total += 1;
				}
			}
		}
		return total;
	}
	
	public static int totalMoneyCollectedForCategory(Payment[] payments, String category) {
		int total = 0;
		for (Payment payment : payments) {
			for (PaymentItemization paymentItemization : payment.getItemizations()) {
				if (category.equals(paymentItemization.getItemDetail().getCategoryName()) && paymentItemization.getTotalMoney() != null) {
					total += paymentItemization.getTotalMoney().getAmount();
				}
			}
		}
		return total;
	}
	
	public static double totalNumberOfSalesForCategory(Payment[] payments, String category) {
		double total = 0;
		for (Payment payment : payments) {
			for (PaymentItemization paymentItemization : payment.getItemizations()) {
				if (category.equals(paymentItemization.getItemDetail().getCategoryName())) {
					total += paymentItemization.getQuantity();
				}
			}
		}
		return total;
	}
	
	public static int totalMoneyCollectedForDiscount(Payment[] payments, String discount) {
		int total = 0;
		for (Payment payment : payments) {
			for (PaymentItemization paymentItemization : payment.getItemizations()) {
				for (PaymentDiscount paymentDiscount : paymentItemization.getDiscounts()) {
					if (discount.equals(paymentDiscount.getName()) && paymentDiscount.getAppliedMoney() != null) {
						total -= paymentDiscount.getAppliedMoney().getAmount();
					}
				}
			}
		}
		return total;
	}
	
	public static int totalNumberOfDiscountsCollectedForDiscount(Payment[] payments, String discount) {
		int total = 0;
		for (Payment payment : payments) {
			for (PaymentItemization paymentItemization : payment.getItemizations()) {
				for (PaymentDiscount paymentDiscount : paymentItemization.getDiscounts()) {
					if (discount.equals(paymentDiscount.getName())) {
						total += 1;
					}
				}
			}
		}
		return total;
	}
	
	public static int totalMoneyCollectedForCardEntryMethod(Payment[] payments, String method) {
		// Possible values for method: MANUAL, SCANNED, SQUARE_CASH,
		// SQUARE_WALLET, SWIPED, WEB_FORM, OTHER.
		int total = 0;
		for (Payment payment : payments) {
			for (Tender tender : payment.getTender()) {
				if (method.equals(tender.getEntryMethod())) {
					if (tender.getTenderedMoney() != null) {
						total += tender.getTenderedMoney().getAmount();
					} else {
						total += tender.getTotalMoney().getAmount();
					}
				}
			}
		}
		return total;
	}
	
	public static int totalNumberOfPaymentsCollectedForEntryMethod(Payment[] payments, String method) {
		// Possible values for method: MANUAL, SCANNED, SQUARE_CASH,
		// SQUARE_WALLET, SWIPED, WEB_FORM, OTHER.
		int total = 0;
		for (Payment payment : payments) {
			for (Tender tender : payment.getTender()) {
				if (method.equals(tender.getEntryMethod())) {
					total += 1;
				}
			}
		}
		return total;
	}
	
	public static int totalMoneyCollectedForCardBrand(Payment[] payments, String brand) {
		// Possible values for brand: UNKNOWN, VISA, MASTER_CARD,
		// AMERICAN_EXPRESS, DISCOVER, DISCOVER_DINERS, JCB.
		int total = 0;
		for (Payment payment : payments) {
			for (Tender tender : payment.getTender()) {
				if (brand.equals(tender.getCardBrand())) {
					if (tender.getTenderedMoney() != null) {
						total += tender.getTenderedMoney().getAmount();
					} else {
						total += tender.getTotalMoney().getAmount();
					}
				}
			}
		}
		return total;
	}
	
	public static int totalNumberOfPaymentsCollectedForCardBrand(Payment[] payments, String brand) {
		// Possible values for brand: UNKNOWN, VISA, MASTER_CARD,
		// AMERICAN_EXPRESS, DISCOVER, DISCOVER_DINERS, JCB.
		int total = 0;
		for (Payment payment : payments) {
			for (Tender tender : payment.getTender()) {
				if (brand.equals(tender.getCardBrand())) {
					total += 1;
				}
			}
		}
		return total;
	}
	
	public static int totalMoneyCollectedForGiftCards(Payment[] payments) {
		int total = 0;
		for (Payment payment : payments) {
			for (PaymentItemization paymentItemization : payment.getItemizations()) {
				if (paymentItemization.getItemizationType().contains("GIFT_CARD") && paymentItemization.getTotalMoney() != null) {
					total += paymentItemization.getTotalMoney().getAmount();
				}
			}
		}
		return total;
	}
}
