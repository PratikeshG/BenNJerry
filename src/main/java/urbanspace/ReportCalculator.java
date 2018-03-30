package urbanspace;

import java.text.ParseException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentDiscount;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.Refund;
import com.squareup.connect.Tender;

import util.TimeManager;

public class ReportCalculator {

    private String timeZone;
    private int offset;
    private int range;

    public ReportCalculator(int range, int offset, String timeZone) {
        this.range = range;
        this.offset = offset;
        this.timeZone = timeZone;
    }

    public int totalTaxMoney(Payment[] payments) {
        int total = 0;

        if (payments != null) {
            for (Payment payment : payments) {
                if (payment.getTaxMoney() != null) {
                    total += payment.getTaxMoney().getAmount();
                }
            }
        }

        return total;
    }

    public int totalTaxMoneyRefunds(Payment[] refundPayments) throws ParseException {
        int total = 0;

        Map<String, String> beginEndTime = TimeManager.getPastDayInterval(range, offset, timeZone);
        Calendar beginTime = TimeManager.toCalendar(beginEndTime.get("begin_time"));
        Calendar endTime = TimeManager.toCalendar(beginEndTime.get("end_time"));

        if (refundPayments != null) {
            for (Payment refundPayment : refundPayments) {
                List<Refund> refundsInRange = getRefundsInRange(beginTime, endTime, refundPayment);

                if (refundsInRange.size() == 1 && refundsInRange.get(0).getType().equals("FULL")) {
                    // additive / inclusive tax money not included if original transaction not taxed
                    if (refundsInRange.get(0).getRefundedAdditiveTaxMoney() != null) {
                        total += refundsInRange.get(0).getRefundedAdditiveTaxMoney().getAmount();
                    }

                    if (refundsInRange.get(0).getRefundedInclusiveTaxMoney() != null) {
                        total += refundsInRange.get(0).getRefundedInclusiveTaxMoney().getAmount();
                    }
                }
            }
        }

        return total;
    }

    public int totalTipMoney(Payment[] payments) {
        int total = 0;

        if (payments != null) {
            for (Payment payment : payments) {
                if (payment.getTipMoney() != null) {
                    total += payment.getTipMoney().getAmount();
                }
            }
        }

        return total;
    }

    public int totalTipMoneyRefunds(Payment[] refundPayments) throws ParseException {
        int total = 0;

        Map<String, String> beginEndTime = TimeManager.getPastDayInterval(range, offset, timeZone);
        Calendar beginTime = TimeManager.toCalendar(beginEndTime.get("begin_time"));
        Calendar endTime = TimeManager.toCalendar(beginEndTime.get("end_time"));

        if (refundPayments != null) {
            for (Payment refundPayment : refundPayments) {
                List<Refund> refundsInRange = getRefundsInRange(beginTime, endTime, refundPayment);

                if (refundsInRange.size() == 1 && refundsInRange.get(0).getType().equals("FULL")) {
                    if (refundsInRange.get(0).getRefundedTipMoney() != null) {
                        total += refundsInRange.get(0).getRefundedTipMoney().getAmount();
                    }
                }
            }
        }

        return total;
    }

    public int totalPartialRefundsRefunds(Payment[] refundPayments) throws ParseException {
        int total = 0;

        Map<String, String> beginEndTime = TimeManager.getPastDayInterval(range, offset, timeZone);
        Calendar beginTime = TimeManager.toCalendar(beginEndTime.get("begin_time"));
        Calendar endTime = TimeManager.toCalendar(beginEndTime.get("end_time"));

        if (refundPayments != null) {
            for (Payment refundPayment : refundPayments) {
                List<Refund> refundsInRange = getRefundsInRange(beginTime, endTime, refundPayment);

                for (Refund refund : refundsInRange) {
                    if (refund.getType().equals("PARTIAL") && refund.getRefundedMoney() != null) {
                        total += refund.getRefundedMoney().getAmount();
                    }
                }
            }
        }

        return total;
    }

    public int totalDiscountMoney(Payment[] payments) {
        int total = 0;

        if (payments != null) {
            for (Payment payment : payments) {
                if (payment.getDiscountMoney() != null) {
                    total += payment.getDiscountMoney().getAmount();
                }
            }
        }

        return total;
    }

    public int totalDiscountMoneyRefunds(Payment[] refundPayments) throws ParseException {
        int total = 0;

        Map<String, String> beginEndTime = TimeManager.getPastDayInterval(range, offset, timeZone);
        Calendar beginTime = TimeManager.toCalendar(beginEndTime.get("begin_time"));
        Calendar endTime = TimeManager.toCalendar(beginEndTime.get("end_time"));

        if (refundPayments != null) {
            for (Payment refundPayment : refundPayments) {
                List<Refund> refundsInRange = getRefundsInRange(beginTime, endTime, refundPayment);

                if (refundsInRange.size() == 1 && refundsInRange.get(0).getType().equals("FULL")) {
                    if (refundsInRange.get(0).getRefundedDiscountMoney() != null) {
                        total += refundsInRange.get(0).getRefundedDiscountMoney().getAmount();
                    }
                }
            }
        }

        return total;
    }

    public int totalTotalCollectedMoney(Payment[] payments) {
        int total = 0;

        if (payments != null) {
            for (Payment payment : payments) {
                if (payment.getTotalCollectedMoney() != null) {
                    total += payment.getTotalCollectedMoney().getAmount();
                }
            }
        }

        return total;
    }

    public int totalTotalCollectedMoneyRefunds(Payment[] refundPayments) throws ParseException {
        int total = 0;

        Map<String, String> beginEndTime = TimeManager.getPastDayInterval(range, offset, timeZone);
        Calendar beginTime = TimeManager.toCalendar(beginEndTime.get("begin_time"));
        Calendar endTime = TimeManager.toCalendar(beginEndTime.get("end_time"));

        if (refundPayments != null) {
            for (Payment refundPayment : refundPayments) {
                List<Refund> refundsInRange = getRefundsInRange(beginTime, endTime, refundPayment);

                for (Refund refundInRange : refundsInRange) {
                    if (refundInRange.getRefundedMoney() != null) {
                        total += refundInRange.getRefundedMoney().getAmount();
                    }
                }
            }
        }

        return total;
    }

    public int totalProcessingFeeMoney(Payment[] payments) {
        int total = 0;

        if (payments != null) {
            for (Payment payment : payments) {
                if (payment.getProcessingFeeMoney() != null) {
                    total += payment.getProcessingFeeMoney().getAmount();
                }
            }
        }

        return total;
    }

    public int totalProcessingFeeMoneyRefunds(Payment[] refundPayments) throws ParseException {
        int total = 0;

        Map<String, String> beginEndTime = TimeManager.getPastDayInterval(range, offset, timeZone);
        Calendar beginTime = TimeManager.toCalendar(beginEndTime.get("begin_time"));
        Calendar endTime = TimeManager.toCalendar(beginEndTime.get("end_time"));

        if (refundPayments != null) {
            for (Payment refundPayment : refundPayments) {
                List<Refund> refundsInRange = getRefundsInRange(beginTime, endTime, refundPayment);

                for (Refund refundInRange : refundsInRange) {
                    if (refundInRange.getRefundedProcessingFeeMoney() != null) {
                        total += refundInRange.getRefundedProcessingFeeMoney().getAmount();
                    }
                }
            }
        }

        return total;
    }

    public int totalMoneyCollectedForTender(Payment[] payments, String tenderType) {
        int total = 0;

        if (payments != null) {
            for (Payment payment : payments) {
                for (Tender tender : payment.getTender()) {
                    if (tenderType.equals(tender.getType())) {
                        if (tender.getTotalMoney() != null) {
                            total += tender.getTotalMoney().getAmount();
                        }
                    }
                }
            }
        }

        return total;
    }

    public int totalMoneyCollectedForTenderRefunds(Payment[] refundPayments, String tenderType) throws ParseException {
        int total = 0;

        Map<String, String> beginEndTime = TimeManager.getPastDayInterval(range, offset, timeZone);
        Calendar beginTime = TimeManager.toCalendar(beginEndTime.get("begin_time"));
        Calendar endTime = TimeManager.toCalendar(beginEndTime.get("end_time"));

        if (refundPayments != null) {
            for (Payment refundPayment : refundPayments) {
                List<Refund> refundsInRange = getRefundsInRange(beginTime, endTime, refundPayment);

                for (Refund refundInRange : refundsInRange) {
                    for (Tender tender : refundPayment.getTender()) {
                        if (refundInRange.getPaymentId().equals(tender.getId()) && tenderType.equals(tender.getType())
                                && refundInRange.getRefundedMoney() != null) {
                            total += refundInRange.getRefundedMoney().getAmount();
                            break;
                        }
                    }
                }
            }
        }

        return total;
    }

    public int totalMoneyCollectedForGiftCards(Payment[] payments) {
        int total = 0;

        if (payments != null) {
            for (Payment payment : payments) {
                for (PaymentItemization paymentItemization : payment.getItemizations()) {
                    if (paymentItemization.getItemizationType().contains("GIFT_CARD")
                            && paymentItemization.getGrossSalesMoney() != null) {
                        total += paymentItemization.getGrossSalesMoney().getAmount();
                    }
                }
            }
        }

        return total;
    }

    public int totalMoneyCollectedForGiftCardsRefunds(Payment[] refundPayments) throws ParseException {
        int total = 0;

        Map<String, String> beginEndTime = TimeManager.getPastDayInterval(range, offset, timeZone);
        Calendar beginTime = TimeManager.toCalendar(beginEndTime.get("begin_time"));
        Calendar endTime = TimeManager.toCalendar(beginEndTime.get("end_time"));

        if (refundPayments != null) {
            for (Payment refundPayment : refundPayments) {
                List<Refund> refundsInRange = getRefundsInRange(beginTime, endTime, refundPayment);

                if (refundsInRange.size() == 1 && refundsInRange.get(0).getType().equals("FULL")) {
                    for (PaymentItemization paymentItemization : refundPayment.getItemizations()) {
                        if (paymentItemization.getItemizationType().contains("GIFT_CARD")
                                && paymentItemization.getGrossSalesMoney() != null) {
                            total -= paymentItemization.getGrossSalesMoney().getAmount();
                        }
                    }
                }
            }
        }

        return total;
    }

    public int totalGrossSales(Payment[] payments) {
        int total = 0;

        if (payments != null) {
            for (Payment payment : payments) {
                if (payment.getTotalCollectedMoney() != null) {
                    total += payment.getTotalCollectedMoney().getAmount();
                }

                if (payment.getDiscountMoney() != null) {
                    total -= payment.getDiscountMoney().getAmount();
                }

                if (payment.getTaxMoney() != null) {
                    total -= payment.getTaxMoney().getAmount();
                }

                if (payment.getTipMoney() != null) {
                    total -= payment.getTipMoney().getAmount();
                }
            }
        }

        return total;
    }

    public int totalGrossSalesRefunds(Payment[] refundPayments) throws ParseException {
        int total = 0;

        Map<String, String> beginEndTime = TimeManager.getPastDayInterval(range, offset, timeZone);
        Calendar beginTime = TimeManager.toCalendar(beginEndTime.get("begin_time"));
        Calendar endTime = TimeManager.toCalendar(beginEndTime.get("end_time"));

        if (refundPayments != null) {
            for (Payment refundPayment : refundPayments) {
                List<Refund> refundsInRange = getRefundsInRange(beginTime, endTime, refundPayment);

                if (refundsInRange.size() == 1 && refundsInRange.get(0).getType().equals("FULL")) {
                    Refund refundInRange = refundsInRange.get(0);

                    if (refundInRange.getRefundedMoney() != null) {
                        total += refundInRange.getRefundedMoney().getAmount();
                    }

                    if (refundInRange.getRefundedDiscountMoney() != null) {
                        total -= refundInRange.getRefundedDiscountMoney().getAmount();
                    }

                    if (refundInRange.getRefundedAdditiveTaxMoney() != null) {
                        total -= refundInRange.getRefundedAdditiveTaxMoney().getAmount();
                    }

                    if (refundInRange.getRefundedInclusiveTaxMoney() != null) {
                        total -= refundInRange.getRefundedInclusiveTaxMoney().getAmount();
                    }

                    if (refundInRange.getRefundedTipMoney() != null) {
                        total -= refundInRange.getRefundedTipMoney().getAmount();
                    }
                }
            }
        }

        return total;
    }

    public int totalMoneyCollectedForCardEntryMethod(Payment[] payments, String method) {
        int total = 0;

        if (payments != null) {
            for (Payment payment : payments) {
                for (Tender tender : payment.getTender()) {
                    if (method.equals(tender.getEntryMethod())) {
                        if (tender.getTotalMoney() != null) {
                            total += tender.getTotalMoney().getAmount();
                        }
                    }
                }
            }
        }

        return total;
    }

    public int totalMoneyCollectedForCardEntryMethodRefunds(Payment[] refundPayments, String method)
            throws ParseException {
        int total = 0;

        Map<String, String> beginEndTime = TimeManager.getPastDayInterval(range, offset, timeZone);
        Calendar beginTime = TimeManager.toCalendar(beginEndTime.get("begin_time"));
        Calendar endTime = TimeManager.toCalendar(beginEndTime.get("end_time"));

        if (refundPayments != null) {
            for (Payment refundPayment : refundPayments) {
                List<Refund> refundsInRange = getRefundsInRange(beginTime, endTime, refundPayment);

                for (Refund refundInRange : refundsInRange) {
                    for (Tender tender : refundPayment.getTender()) {
                        if (refundInRange.getPaymentId().equals(tender.getId())
                                && method.equals(tender.getEntryMethod()) && refundInRange.getRefundedMoney() != null) {
                            total += refundInRange.getRefundedMoney().getAmount();
                            break;
                        }
                    }
                }
            }
        }

        return total;
    }

    public int totalMoneyCollectedForCardBrand(Payment[] payments, String brand) {
        int total = 0;

        if (payments != null) {
            for (Payment payment : payments) {
                for (Tender tender : payment.getTender()) {
                    if (brand.equals(tender.getCardBrand())) {
                        if (tender.getTotalMoney() != null) {
                            total += tender.getTotalMoney().getAmount();
                        }
                    }
                }
            }
        }

        return total;
    }

    public int totalMoneyCollectedForCardBrandRefunds(Payment[] refundPayments, String brand) throws ParseException {
        int total = 0;

        Map<String, String> beginEndTime = TimeManager.getPastDayInterval(range, offset, timeZone);
        Calendar beginTime = TimeManager.toCalendar(beginEndTime.get("begin_time"));
        Calendar endTime = TimeManager.toCalendar(beginEndTime.get("end_time"));

        if (refundPayments != null) {
            for (Payment refundPayment : refundPayments) {
                List<Refund> refundsInRange = getRefundsInRange(beginTime, endTime, refundPayment);

                for (Refund refundInRange : refundsInRange) {
                    for (Tender tender : refundPayment.getTender()) {
                        if (refundInRange.getPaymentId().equals(tender.getId()) && brand.equals(tender.getCardBrand())
                                && refundInRange.getRefundedMoney() != null) {
                            total += refundInRange.getRefundedMoney().getAmount();
                            break;
                        }
                    }
                }
            }
        }

        return total;
    }

    public int totalMoneyCollectedForCategory(Payment[] payments, String category) {
        int total = 0;

        if (payments != null) {
            for (Payment payment : payments) {
                for (PaymentItemization paymentItemization : payment.getItemizations()) {
                    if (paymentItemization.getItemDetail() != null && paymentItemization.getGrossSalesMoney() != null
                            && category.equals(paymentItemization.getItemDetail().getCategoryName())) {
                        total += paymentItemization.getGrossSalesMoney().getAmount();
                    }
                }
            }
        }

        return total;
    }

    public int totalMoneyCollectedForCategoryRefunds(Payment[] refundPayments, String category) throws ParseException {
        int total = 0;

        Map<String, String> beginEndTime = TimeManager.getPastDayInterval(range, offset, timeZone);
        Calendar beginTime = TimeManager.toCalendar(beginEndTime.get("begin_time"));
        Calendar endTime = TimeManager.toCalendar(beginEndTime.get("end_time"));

        if (refundPayments != null) {
            for (Payment refundPayment : refundPayments) {
                List<Refund> refundsInRange = getRefundsInRange(beginTime, endTime, refundPayment);

                if (refundsInRange.size() == 1 && refundsInRange.get(0).getType().equals("FULL")) {
                    for (PaymentItemization paymentItemization : refundPayment.getItemizations()) {
                        if (paymentItemization.getItemDetail() != null
                                && paymentItemization.getGrossSalesMoney() != null
                                && category.equals(paymentItemization.getItemDetail().getCategoryName())) {
                            total -= paymentItemization.getGrossSalesMoney().getAmount();
                        }
                    }
                }
            }
        }

        return total;
    }

    public int totalMoneyCollectedForDiscount(Payment[] payments, String discount) {
        int total = 0;

        if (payments != null) {
            for (Payment payment : payments) {
                for (PaymentItemization paymentItemization : payment.getItemizations()) {
                    for (PaymentDiscount paymentDiscount : paymentItemization.getDiscounts()) {
                        if (discount.equals(paymentDiscount.getName()) && paymentDiscount.getAppliedMoney() != null) {
                            total += paymentDiscount.getAppliedMoney().getAmount();
                        }
                    }
                }
            }
        }

        return total;
    }

    public int totalMoneyCollectedForDiscountRefunds(Payment[] refundPayments, String discount) throws ParseException {
        int total = 0;

        Map<String, String> beginEndTime = TimeManager.getPastDayInterval(range, offset, timeZone);
        Calendar beginTime = TimeManager.toCalendar(beginEndTime.get("begin_time"));
        Calendar endTime = TimeManager.toCalendar(beginEndTime.get("end_time"));

        if (refundPayments != null) {
            for (Payment refundPayment : refundPayments) {
                List<Refund> refundsInRange = getRefundsInRange(beginTime, endTime, refundPayment);

                if (refundsInRange.size() == 1 && refundsInRange.get(0).getType().equals("FULL")) {
                    for (PaymentItemization paymentItemization : refundPayment.getItemizations()) {
                        for (PaymentDiscount paymentDiscount : paymentItemization.getDiscounts()) {
                            if (discount.equals(paymentDiscount.getName())
                                    && paymentDiscount.getAppliedMoney() != null) {
                                total -= paymentDiscount.getAppliedMoney().getAmount();
                            }
                        }
                    }
                }
            }
        }

        return total;
    }

    private List<Refund> getRefundsInRange(Calendar beginTime, Calendar endTime, Payment refundPayment)
            throws ParseException {
        List<Refund> refundsInRange = new LinkedList<Refund>();

        for (Refund refund : refundPayment.getRefunds()) {
            Calendar refundTime = TimeManager.toCalendar(refund.getCreatedAt());
            if (beginTime.compareTo(refundTime) <= 0 && endTime.compareTo(refundTime) >= 0) {
                refundsInRange.add(refund);
            }
        }

        return refundsInRange;
    }
}
