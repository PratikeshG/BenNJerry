package benjerrys;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squareup.connect.Payment;
import com.squareup.connect.PaymentItemization;
import com.squareup.connect.PaymentTax;
import com.squareup.connect.Refund;
import com.squareup.connect.Tender;

import util.TimeManager;

public class MonthlyReportBuilder {
    private static String CATEGORY_IC_CONES = "IC CONES";
    private static String CATEGORY_TOPPINGS = "TOPPINGS";
    private static String CATEGORY_IC_SUNDAE = "IC SUNDAE";
    private static String CATEGORY_IC_DRINKS = "IC DRINKS";
    private static String CATEGORY_IC_HANDPACK = "IC HANDPACK";
    private static String CATEGORY_IC_CAKE_NOVELTY = "IC CAKE/NOVELTY";

    private static String CATEGORY_CATERING = "CATERING";
    private static String CATEGORY_SPECIAL_EVENTS = "SPECIAL EVENTS";
    private static String CATEGORY_OFF_PREMISE = "OFF PREMISE";
    private static String CATEGORY_OFF_PREMISE_FEES = "OFF PREMISE - FEES";

    private static String CATEGORY_ON_DEMAND = "ON-DEMAND";

    private static String CATEGORY_SOFT_SERVE = "SOFT SERVE";
    private static String CATEGORY_COLD_BEVERAGES = "COLD BEVERAGES";
    private static String CATEGORY_HOT_BEVERAGES = "HOT BEVERAGES";
    private static String CATEGORY_BAKED_GOODS = "BAKED GOODS";
    private static String CATEGORY_PASTRY = "PASTRY";
    private static String CATEGORY_CONFECTIONS = "CONFECTIONS";
    private static String CATEGORY_OTHER_FOODS = "OTHER FOODS";

    private static String CATEGORY_HOUSEWARE = "HOUSEWARE";
    private static String CATEGORY_IMPULSE = "IMPULSE";
    private static String CATEGORY_RETAIL_GIFTS = "RETAIL/GIFTS";
    private static String CATEGORY_STATIONARY = "STATIONARY";
    private static String CATEGORY_TSHIRTS = "T-SHIRTS";
    private static String CATEGORY_TOYS_GAMES = "TOYS & GAMES";
    private static String CATEGORY_OTHER_APPAREL = "OTHER APPAREL";

    private static String CATEGORY_PREPACKS = "PREPACKS";

    private static String CATEGORY_UNCATEGORIZED = "UNCATEGORIZED SALES";
    private static String CATEGORY_UNASSIGNED_REFUNDS = "UNASSIGNED REFUNDS";

    private static String CATEGORY_GIFT_CARD = "GIFT CARD";

    private static List<String> GROUP_IC_AND_TOPPINGS = new ArrayList<String>();
    private static List<String> GROUP_OFF_PREMISE = new ArrayList<String>();
    private static List<String> GROUP_TOTAL_ICE_CREAM = new ArrayList<String>();
    private static List<String> GROUP_TOTAL_RETAIL = new ArrayList<String>();
    private static List<String> GROUP_TOTAL_NET_SALES = new ArrayList<String>();
    private static List<String> GROUP_TOTAL_SUMMARY = new ArrayList<String>();

    private static String LABEL_TOTAL_IC_TOPPINGS = "Total IC & Toppings";
    private static String LABEL_OFF_PREMISE = "OFF PREMISE";
    private static String LABEL_TOTAL_RETAIL = "Total Retail";
    private static String LABEL_BLANK = "";

    static {
        // GROUP_IC_AND_TOPPINGS
        GROUP_IC_AND_TOPPINGS.addAll(Arrays.asList(CATEGORY_IC_CONES, CATEGORY_TOPPINGS));

        // GROUP_OFF_PREMISE
        GROUP_OFF_PREMISE.addAll(Arrays.asList(CATEGORY_OFF_PREMISE, CATEGORY_OFF_PREMISE_FEES, CATEGORY_CATERING,
                CATEGORY_SPECIAL_EVENTS));

        // GROUP_TOTAL_ICE_CREAM
        GROUP_TOTAL_ICE_CREAM.addAll(GROUP_IC_AND_TOPPINGS);
        GROUP_TOTAL_ICE_CREAM.addAll(
                Arrays.asList(CATEGORY_IC_SUNDAE, CATEGORY_IC_DRINKS, CATEGORY_IC_HANDPACK, CATEGORY_IC_CAKE_NOVELTY));
        GROUP_TOTAL_ICE_CREAM.addAll(GROUP_OFF_PREMISE);
        GROUP_TOTAL_ICE_CREAM.add(CATEGORY_ON_DEMAND);

        // GROUP_TOTAL_RETAIL
        GROUP_TOTAL_RETAIL.addAll(Arrays.asList(CATEGORY_HOUSEWARE, CATEGORY_IMPULSE, CATEGORY_RETAIL_GIFTS,
                CATEGORY_STATIONARY, CATEGORY_TSHIRTS, CATEGORY_TOYS_GAMES, CATEGORY_OTHER_APPAREL));

        // GROUP_TOTAL_NET_SALES
        GROUP_TOTAL_NET_SALES.addAll(GROUP_TOTAL_ICE_CREAM);
        GROUP_TOTAL_NET_SALES.addAll(GROUP_TOTAL_RETAIL);
        GROUP_TOTAL_NET_SALES.addAll(Arrays.asList(CATEGORY_SOFT_SERVE, CATEGORY_COLD_BEVERAGES, CATEGORY_HOT_BEVERAGES,
                CATEGORY_BAKED_GOODS, CATEGORY_PASTRY, CATEGORY_CONFECTIONS, CATEGORY_OTHER_FOODS, CATEGORY_PREPACKS,
                CATEGORY_UNASSIGNED_REFUNDS));
        GROUP_TOTAL_NET_SALES.add(CATEGORY_UNCATEGORIZED);

        // GROUP_TOTAL_SUMMARY
        GROUP_TOTAL_SUMMARY.addAll(GROUP_TOTAL_NET_SALES);
        GROUP_TOTAL_SUMMARY.add(CATEGORY_GIFT_CARD);
    }

    private String location;
    private String dateMonthYear;
    private String beginTime;
    private String endTime;
    private Payment[] payments;
    private Payment[] refundedPayments;

    Map<String, CategoryData> categoryTotals;

    public MonthlyReportBuilder(String location, String dateMonthYear, String beginTime, String endTime) {
        this.location = location;
        this.dateMonthYear = dateMonthYear;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }

    public Payment[] getPayments() {
        return payments;
    }

    public void setPayments(Payment[] payments) {
        this.payments = payments;
    }

    public Payment[] getRefundPayments() {
        return refundedPayments;
    }

    public void setRefundedPayments(Payment[] refundedPayments) {
        this.refundedPayments = refundedPayments;
    }

    private void initCategoryTotals() {
        categoryTotals = new HashMap<String, CategoryData>();

        for (String category : GROUP_TOTAL_SUMMARY) {
            categoryTotals.put(category, new CategoryData(category));
        }
    }

    public void buildReports() throws ParseException {
        initCategoryTotals();

        // Process gross totals
        for (Payment payment : payments) {
            for (PaymentItemization paymentItemization : payment.getItemizations()) {
                CategoryData categoryData = getCalculationCategory(paymentItemization);
                addSaleCategoryTotals(categoryData, paymentItemization);
            }
        }

        // Process refunds
        Calendar beginTimeCalendar = TimeManager.toCalendar(this.beginTime);
        Calendar endTimeCalendar = TimeManager.toCalendar(this.endTime);

        if (refundedPayments != null) {
            for (Payment refundedPayment : refundedPayments) {
                List<Refund> refundsInRange = getRefundsInReportDateRange(beginTimeCalendar, endTimeCalendar,
                        refundedPayment);

                if (refundsInRange.size() == 1 && refundsInRange.get(0).getType().equals("FULL")) {
                    // This is a full refund, so process refund as itemized data

                    for (PaymentItemization paymentItemization : refundedPayment.getItemizations()) {
                        CategoryData categoryData = getCalculationCategory(paymentItemization);
                        refundFullCategoryTotals(categoryData, paymentItemization);
                    }
                } else {
                    // These are partial refund(s), so process partial refund(s) as uncategorized refunds
                    for (Refund refund : refundsInRange) {
                        refundPartialCategoryTotals(refund);
                    }
                }
            }
        }
    }

    private CategoryData getCalculationCategory(PaymentItemization paymentItemization) {
        String itemCategory = CATEGORY_UNCATEGORIZED;
        if (paymentItemization.getItemDetail() != null
                && paymentItemization.getItemDetail().getCategoryName() != null) {
            itemCategory = paymentItemization.getItemDetail().getCategoryName().toUpperCase();
        }

        return categoryTotals.getOrDefault(itemCategory, categoryTotals.get(CATEGORY_UNCATEGORIZED));
    }

    private List<Refund> getRefundsInReportDateRange(Calendar beginTime, Calendar endTime, Payment refundedPayment)
            throws ParseException {
        ArrayList<Refund> refundsInDateRange = new ArrayList<Refund>();

        for (Refund refund : refundedPayment.getRefunds()) {
            Calendar refundTime = TimeManager.toCalendar(refund.getCreatedAt());
            if (beginTime.compareTo(refundTime) <= 0 && endTime.compareTo(refundTime) >= 0) {
                refundsInDateRange.add(refund);
            }
        }

        return refundsInDateRange;
    }

    private void addSaleCategoryTotals(CategoryData category, PaymentItemization paymentItemization) {
        category.increaseItemsSoldQty(paymentItemization.getQuantity().intValue());
        category.increaseGrossSalesTotal(paymentItemization.getGrossSalesMoney().getAmount());
        category.increaseDiscountsTotal(paymentItemization.getDiscountMoney().getAmount());

        for (PaymentTax tax : paymentItemization.getTaxes()) {
            category.increaseTaxesTotal(tax.getAppliedMoney().getAmount());
        }
    }

    private void refundFullCategoryTotals(CategoryData category, PaymentItemization paymentItemization) {
        category.increaseItemsRefundedQty(paymentItemization.getQuantity().intValue());
        category.increaseRefundedTotal(paymentItemization.getGrossSalesMoney().getAmount());
        category.decreaseDiscountsTotal(paymentItemization.getDiscountMoney().getAmount());

        for (PaymentTax tax : paymentItemization.getTaxes()) {
            category.decreaseTaxesTotal(tax.getAppliedMoney().getAmount());
        }
    }

    private void refundPartialCategoryTotals(Refund refund) {
        CategoryData category = categoryTotals.get(CATEGORY_UNASSIGNED_REFUNDS);

        int positiveTotal = refund.getRefundedMoney().getAmount();
        positiveTotal = -positiveTotal;
        category.increaseRefundedTotal(positiveTotal);
        category.increaseItemsRefundedQty(1);
    }

    private int sumCategoryGroupReportableSales(List<String> group) {
        int total = 0;

        for (String cat : group) {
            total += categoryTotals.get(cat).getReportableSalesTotal();
        }

        return total;
    }

    private CategoryData mergeCategoryGroup(String name, List<String> group) {
        List<CategoryData> mergeCategories = new ArrayList<CategoryData>();

        for (String category : group) {
            mergeCategories.add(categoryTotals.get(category));
        }

        CategoryData mergedData = new CategoryData(name, mergeCategories);

        return mergedData;
    }

    private int getTotalIceCreamSalesTotal() {
        return sumCategoryGroupReportableSales(GROUP_TOTAL_ICE_CREAM);
    }

    private int getNetSalesTotal() {
        return sumCategoryGroupReportableSales(GROUP_TOTAL_NET_SALES);
    }

    private int getDiscountsTotal() {
        int total = 0;

        for (String cat : GROUP_TOTAL_NET_SALES) {
            total += categoryTotals.get(cat).getDiscountsTotal();
        }

        return total;
    }

    private CategoryData getTotalIcToppingsData() {
        return mergeCategoryGroup(LABEL_TOTAL_IC_TOPPINGS, GROUP_IC_AND_TOPPINGS);
    }

    private CategoryData getTotalOffPremiseData() {
        return mergeCategoryGroup(LABEL_OFF_PREMISE, GROUP_OFF_PREMISE);
    }

    private CategoryData getTotalRetailData() {
        return mergeCategoryGroup(LABEL_TOTAL_RETAIL, GROUP_TOTAL_RETAIL);
    }

    private CategoryData getTotalSummaryData() {
        return mergeCategoryGroup(LABEL_BLANK, GROUP_TOTAL_SUMMARY);
    }

    private int getReportableSalesTotal() {
        // Discounts are a negative value
        return getNetSalesTotal() + getDiscountsTotal();
    }

    public int getTotalTransactions() {
        int total = 0;

        for (Payment payment : payments) {
            boolean hasValidPaymentTender = false;
            for (Tender tender : payment.getTender()) {
                if (!tender.getType().equals(Tender.TENDER_TYPE_NO_SALE)) {
                    hasValidPaymentTender = true;
                }
            }
            if (hasValidPaymentTender) {
                total += 1;
            }
        }

        return total;
    }

    public String generateCalculationReport() {
        StringBuilder reportBuilder = new StringBuilder();

        ArrayList<CalculationReportRow> rows = new ArrayList<CalculationReportRow>();

        rows.add(new CalculationReportRow.RowBuilder().setCol(1, "Category").setCol(2, "Items Sold")
                .setCol(3, "Gross Sales").setCol(4, "Items Refunded").setCol(5, "Refunds")
                .setCol(6, "Reportable Sales (Gross - Refund)").setCol(7, "Discounts & Comps").setCol(8, "Net Sales")
                .setCol(9, "Taxes").build());
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_IC_CONES)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_TOPPINGS)));
        rows.add(new CalculationReportRow(getTotalIcToppingsData()));
        rows.add(new CalculationReportRow());
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_IC_SUNDAE)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_IC_DRINKS)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_IC_HANDPACK)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_IC_CAKE_NOVELTY)));
        rows.add(new CalculationReportRow());
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_CATERING)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_SPECIAL_EVENTS)));
        rows.add(new CalculationReportRow(getTotalOffPremiseData()));
        rows.add(new CalculationReportRow());
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_ON_DEMAND)));
        rows.add(new CalculationReportRow());
        rows.add(new CalculationReportRow.RowBuilder().setCol(1, "Total Ice Cream Sales")
                .setColDollarValue(6, getTotalIceCreamSalesTotal()).build());
        rows.add(new CalculationReportRow());
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_SOFT_SERVE)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_COLD_BEVERAGES)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_HOT_BEVERAGES)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_BAKED_GOODS)));
        rows.add(new CalculationReportRow());
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_HOUSEWARE)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_IMPULSE)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_RETAIL_GIFTS)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_STATIONARY)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_TSHIRTS)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_TOYS_GAMES)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_OTHER_APPAREL)));
        rows.add(new CalculationReportRow(getTotalRetailData()));
        rows.add(new CalculationReportRow());
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_PREPACKS)));
        rows.add(new CalculationReportRow());
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_UNCATEGORIZED)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_UNASSIGNED_REFUNDS)));
        rows.add(new CalculationReportRow());
        rows.add(new CalculationReportRow.RowBuilder().setCol(1, "Pre-Discount Reportable Sales")
                .setColDollarValue(6, getNetSalesTotal()).build());
        rows.add(new CalculationReportRow());
        rows.add(new CalculationReportRow.RowBuilder().setCol(1, "Total Discounts")
                .setColDollarValue(6, getDiscountsTotal()).build());
        rows.add(new CalculationReportRow.RowBuilder().setCol(1, "Total Reportable Sales (Net Sales - Total Discounts)")
                .setColDollarValue(6, getReportableSalesTotal()).build());
        rows.add(new CalculationReportRow());
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_GIFT_CARD)));

        for (CalculationReportRow row : rows) {
            reportBuilder.append(row.toString());
        }

        return reportBuilder.toString();
    }

    public String generateSummaryReport() {
        StringBuilder reportBuilder = new StringBuilder();

        ArrayList<SummaryReportRow> rows = new ArrayList<SummaryReportRow>();

        rows.add(new SummaryReportRow.RowBuilder().setCol(1, dateMonthYear).build());
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, location).build());
        rows.add(new SummaryReportRow());
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, "# of Transactions")
                .setCol(2, Integer.toString(getTotalTransactions())).build());
        rows.add(new SummaryReportRow());
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, "Sales Mix").build());
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, "IC Cones (includes Toppings)")
                .setColDollarValue(2, getTotalIcToppingsData().getReportableSalesTotal()).build());
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_IC_SUNDAE)));
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_IC_DRINKS)));
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_IC_HANDPACK)));
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_IC_CAKE_NOVELTY)));
        rows.add(new SummaryReportRow(getTotalOffPremiseData()));
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_ON_DEMAND)));
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, "Total Ice Cream Sales")
                .setColDollarValue(2, getTotalIceCreamSalesTotal()).build());
        rows.add(new SummaryReportRow());
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_SOFT_SERVE)));
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_COLD_BEVERAGES)));
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_HOT_BEVERAGES)));
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_BAKED_GOODS)));
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_PASTRY)));
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_CONFECTIONS)));
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_OTHER_FOODS)));
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, "RETAIL / GIFT (All)")
                .setColDollarValue(2, getTotalRetailData().getReportableSalesTotal()).build());
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_PREPACKS)));
        rows.add(new SummaryReportRow());
        rows.add(new SummaryReportRow.RowBuilder(categoryTotals.get(CATEGORY_UNCATEGORIZED)).setCol(3, "**").build());
        rows.add(new SummaryReportRow.RowBuilder(categoryTotals.get(CATEGORY_UNASSIGNED_REFUNDS)).setCol(3, "****")
                .build());
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, "Total Sales").setColDollarValue(2, getNetSalesTotal())
                .build());
        rows.add(new SummaryReportRow());
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, "Total Discounts")
                .setColDollarValue(2, getDiscountsTotal()).build());
        rows.add(new SummaryReportRow());
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, "Total Reportable Sales (Net Sales - Total Discounts)")
                .setColDollarValue(2, getReportableSalesTotal()).build());
        rows.add(new SummaryReportRow());
        rows.add(new SummaryReportRow.RowBuilder()
                .setCol(1,
                        "** Uncategorized Sales are any sales entered through the Keypad, rather than the standard ringing process.  You must add these sales to the appropriate Sales Mix category when reporting Monthly Sales on the The Rolling Cone.")
                .build());
        rows.add(new SummaryReportRow());
        rows.add(new SummaryReportRow.RowBuilder()
                .setCol(1,
                        "**** Unassigned refunds are any refunds that were not issued against a specific menu item.  You must deduct these refunds to the appropriate Sales Mix category when reporting Monthly Sales on the The Rolling Cone.")
                .build());
        rows.add(new SummaryReportRow());
        rows.add(new SummaryReportRow.RowBuilder()
                .setCol(1,
                        "Reassigning the above numbers is critical to the integrity of the system sales data and consistent reporting of key performance indicators.")
                .build());

        for (SummaryReportRow row : rows) {
            reportBuilder.append(row.toString());
        }

        return reportBuilder.toString();
    }
}
