package benjerrys;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squareup.connect.v2.Payment;
import com.squareup.connect.v2.OrderLineItem;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.Money;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.PaymentRefund;
import com.squareup.connect.v2.Tender;

import util.TimeManager;

public class MonthlyReportBuilder {
    private static String CATEGORY_IC_CONES = "IC CONES";
    private static String CATEGORY_TO_IC_CONES = "TO - IC CONES";
    private static String CATEGORY_TOPPINGS = "TOPPINGS";
    private static String CATEGORY_IC_SUNDAE = "IC SUNDAE";
    private static String CATEGORY_TO_IC_SUNDAE = "TO - IC SUNDAES";
    private static String CATEGORY_IC_DRINKS = "IC DRINKS";
    private static String CATEGORY_TO_IC_DRINKS = "TO - IC DRINKS";
    private static String CATEGORY_IC_HANDPACK = "IC HANDPACK";
    private static String CATEGORY_TO_FRESH_PACKED = "TO - FRESH PACKED";
    private static String CATEGORY_IC_CAKE_NOVELTY = "IC CAKE/NOVELTY";
    private static String CATEGORY_TO_IC_CAKE_NOVELTY = "TO - IC CAKE/NOVELTY";

    private static String CATEGORY_CATERING = "CATERING";
    private static String CATEGORY_SPECIAL_EVENTS = "SPECIAL EVENTS";
    private static String CATEGORY_OFF_PREMISE = "OFF PREMISE";
    private static String CATEGORY_OFF_PREMISE_FEES = "OFF PREMISE - FEES";

    private static String CATEGORY_ON_DEMAND = "ON-DEMAND";

    private static String CATEGORY_SOFT_SERVE = "SOFT SERVE";
    private static String CATEGORY_COLD_BEVERAGES = "COLD BEVERAGES";
    private static String CATEGORY_TO_COLD_BEVERAGES = "TO - COLD BEVERAGES";
    private static String CATEGORY_HOT_BEVERAGES = "HOT BEVERAGES";
    private static String CATEGORY_TO_HOT_BEVERAGES = "TO - HOT BEVERAGES";
    private static String CATEGORY_BAKED_GOODS = "BAKED GOODS";
    private static String CATEGORY_TO_BAKED_GOODS = "TO - BAKED GOODS";
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
    private static String CATEGORY_TO_PREPACKS = "TO - PREPACKS";

    private static String CATEGORY_UNCATEGORIZED = "UNCATEGORIZED SALES";
    private static String CATEGORY_UNASSIGNED_REFUNDS = "UNASSIGNED REFUNDS";

    private static String CATEGORY_GIFT_CARD = "GIFT_CARD";
    private static String CATEGORY_SERVICE_CHARGE = "SERVICE CHARGE";

    private static List<String> IC_TO_MERGED_CONES = new ArrayList<String>();
    private static List<String> IC_TO_MERGED_SUNDAE = new ArrayList<String>();
    private static List<String> IC_TO_MERGED_DRINKS = new ArrayList<String>();
    private static List<String> IC_TO_MERGED_PACKED = new ArrayList<String>();
    private static List<String> IC_TO_MERGED_CAKE_NOVELTY = new ArrayList<String>();
    private static List<String> TO_MERGED_COLD_BEVERAGES = new ArrayList<String>();
    private static List<String> TO_MERGED_HOT_BEVERAGES = new ArrayList<String>();
    private static List<String> TO_MERGED_BAKED_GOODS = new ArrayList<String>();
    private static List<String> TO_MERGED_PREPACKS = new ArrayList<String>();

    private static List<String> GROUP_IC_AND_TOPPINGS = new ArrayList<String>();
    private static List<String> GROUP_OFF_PREMISE = new ArrayList<String>();
    private static List<String> GROUP_TOTAL_ICE_CREAM = new ArrayList<String>();
    private static List<String> GROUP_TOTAL_RETAIL = new ArrayList<String>();
    private static List<String> GROUP_TOTAL_NET_SALES = new ArrayList<String>();
    private static List<String> GROUP_ALL = new ArrayList<String>();

    private static String LABEL_TOTAL_IC_TOPPINGS = "Total IC & Toppings";
    private static String LABEL_OFF_PREMISE = "OFF PREMISE";
    private static String LABEL_TOTAL_RETAIL = "Total Retail";

    static {
        IC_TO_MERGED_CONES.addAll(Arrays.asList(CATEGORY_IC_CONES, CATEGORY_TO_IC_CONES));
        IC_TO_MERGED_SUNDAE.addAll(Arrays.asList(CATEGORY_IC_SUNDAE, CATEGORY_TO_IC_SUNDAE));
        IC_TO_MERGED_DRINKS.addAll(Arrays.asList(CATEGORY_IC_DRINKS, CATEGORY_TO_IC_DRINKS));
        IC_TO_MERGED_PACKED.addAll(Arrays.asList(CATEGORY_IC_HANDPACK, CATEGORY_TO_FRESH_PACKED));
        IC_TO_MERGED_CAKE_NOVELTY.addAll(Arrays.asList(CATEGORY_IC_CAKE_NOVELTY, CATEGORY_TO_IC_CAKE_NOVELTY));
        TO_MERGED_COLD_BEVERAGES.addAll(Arrays.asList(CATEGORY_COLD_BEVERAGES, CATEGORY_TO_COLD_BEVERAGES));
        TO_MERGED_HOT_BEVERAGES.addAll(Arrays.asList(CATEGORY_HOT_BEVERAGES, CATEGORY_TO_HOT_BEVERAGES));
        TO_MERGED_BAKED_GOODS.addAll(Arrays.asList(CATEGORY_BAKED_GOODS, CATEGORY_TO_BAKED_GOODS));
        TO_MERGED_PREPACKS.addAll(Arrays.asList(CATEGORY_PREPACKS, CATEGORY_TO_PREPACKS));

        // GROUP_IC_AND_TOPPINGS
        GROUP_IC_AND_TOPPINGS.addAll(IC_TO_MERGED_CONES);
        GROUP_IC_AND_TOPPINGS.add(CATEGORY_TOPPINGS);

        // GROUP_OFF_PREMISE
        GROUP_OFF_PREMISE.addAll(Arrays.asList(CATEGORY_OFF_PREMISE, CATEGORY_OFF_PREMISE_FEES, CATEGORY_CATERING,
                CATEGORY_SPECIAL_EVENTS));

        // GROUP_TOTAL_ICE_CREAM
        GROUP_TOTAL_ICE_CREAM.addAll(GROUP_IC_AND_TOPPINGS);
        GROUP_TOTAL_ICE_CREAM.addAll(IC_TO_MERGED_SUNDAE);
        GROUP_TOTAL_ICE_CREAM.addAll(IC_TO_MERGED_DRINKS);
        GROUP_TOTAL_ICE_CREAM.addAll(IC_TO_MERGED_PACKED);
        GROUP_TOTAL_ICE_CREAM.addAll(IC_TO_MERGED_CAKE_NOVELTY);
        GROUP_TOTAL_ICE_CREAM.addAll(GROUP_OFF_PREMISE);
        GROUP_TOTAL_ICE_CREAM.add(CATEGORY_ON_DEMAND);

        // GROUP_TOTAL_RETAIL
        GROUP_TOTAL_RETAIL.addAll(Arrays.asList(CATEGORY_HOUSEWARE, CATEGORY_IMPULSE, CATEGORY_RETAIL_GIFTS,
                CATEGORY_STATIONARY, CATEGORY_TSHIRTS, CATEGORY_TOYS_GAMES, CATEGORY_OTHER_APPAREL));

        // GROUP_TOTAL_NET_SALES
        GROUP_TOTAL_NET_SALES.addAll(GROUP_TOTAL_ICE_CREAM);
        GROUP_TOTAL_NET_SALES.addAll(GROUP_TOTAL_RETAIL);
        GROUP_TOTAL_NET_SALES.addAll(TO_MERGED_COLD_BEVERAGES);
        GROUP_TOTAL_NET_SALES.addAll(TO_MERGED_HOT_BEVERAGES);
        GROUP_TOTAL_NET_SALES.addAll(TO_MERGED_BAKED_GOODS);
        GROUP_TOTAL_NET_SALES.addAll(TO_MERGED_PREPACKS);
        GROUP_TOTAL_NET_SALES.addAll(Arrays.asList(CATEGORY_SOFT_SERVE, CATEGORY_PASTRY, CATEGORY_CONFECTIONS,
                CATEGORY_OTHER_FOODS, CATEGORY_UNASSIGNED_REFUNDS));
        GROUP_TOTAL_NET_SALES.add(CATEGORY_UNCATEGORIZED);

        // Track all categories in one place
        GROUP_ALL.addAll(GROUP_TOTAL_NET_SALES);
        GROUP_ALL.add(CATEGORY_GIFT_CARD);
        GROUP_ALL.add(CATEGORY_SERVICE_CHARGE);
    }

    private String location;
    private String dateMonthYear;
    private String beginTime;
    private String endTime;
    private PaymentRefund[] paymentRefunds;
    private Map<String, Payment> payments;
    private Map<String, Order> orders;
    private Map<String, CatalogObject> categories;

    Map<String, CategoryData> categoryTotals;

    public MonthlyReportBuilder(String location, String dateMonthYear, String beginTime, String endTime) {
        this.location = location;
        this.dateMonthYear = dateMonthYear;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }

    public Map<String, Payment> getPayments() {
        return payments;
    }

    public void setPayments(Map<String, Payment> payments) {
        this.payments = payments;
    }

    public PaymentRefund[] getPaymentRefunds() {
        return paymentRefunds;
    }

    public void setPaymentRefunds(PaymentRefund[] paymentRefunds) {
        this.paymentRefunds = paymentRefunds;
    }

    public Map<String, Order> getOrders() {
    	return orders;
    }

    public void setOrders(Map<String, Order> orders) {
    	this.orders = orders;
    }

    public Map<String, CatalogObject> getCategories() {
    	return categories;
    }

    public void setCategories(Map<String, CatalogObject> categories) {
    	this.categories = categories;
    }

    private void initCategoryTotals() {
        categoryTotals = new HashMap<String, CategoryData>();

        for (String category : GROUP_ALL) {
            categoryTotals.put(category, new CategoryData(category));
        }
    }

    public void buildReports() throws ParseException {
        initCategoryTotals();

        // Process gross totals
        for (Order order : orders.values()) {
        	if(order != null && order.getLineItems() != null) {
        		for(OrderLineItem orderLineItem : order.getLineItems()) {
            		if(orderLineItem != null) {
            			CategoryData categoryData = getCalculationCategory(orderLineItem);
                		addSaleCategoryTotals(categoryData, orderLineItem);
            		}
            	}
        	}
        }

        // Process refunds
        Calendar beginTimeCalendar = TimeManager.toCalendar(this.beginTime);
        Calendar endTimeCalendar = TimeManager.toCalendar(this.endTime);

        if (paymentRefunds != null) {
            List<PaymentRefund> refundsInRange = getRefundsInReportDateRange(beginTimeCalendar, endTimeCalendar);
            for(PaymentRefund paymentRefund : refundsInRange) {
            	if(paymentRefund != null) {
            		String orderId = paymentRefund.getOrderId();
                	Order order = orders.get(orderId);
                	if(order != null) {
                		Money moneyFromPaymentRefund = paymentRefund.getAmountMoney();
                		Payment originalPayment = payments.get(paymentRefund.getPaymentId());
                		Order originalOrder = orders.get(originalPayment.getOrderId());
                		Money moneyFromOrder = originalOrder.getTotalMoney();

                    	if (moneyFromPaymentRefund != null && moneyFromOrder != null && moneyFromPaymentRefund.getAmount() == moneyFromOrder.getAmount()) {
                    		// This is a full refund, so process refund as itemized data
                    		if(originalOrder.getLineItems() != null) {
                    			for (OrderLineItem orderLineItem : originalOrder.getLineItems()) {
                            		CategoryData categoryData = getCalculationCategory(orderLineItem);
                            		refundFullCategoryTotals(categoryData, orderLineItem);
                        		}
                    		}
                    	} else {
                    		// These are partial refund(s), so process partial refund(s) as uncategorized refunds
                    		refundPartialCategoryTotals(paymentRefund);
                    	}
                	}
            	}
            }
        }
    }

    private CategoryData getCalculationCategory(OrderLineItem orderLineItem) {
        String itemCategory = CATEGORY_UNCATEGORIZED;
        String catalogObjectId = orderLineItem.getCatalogObjectId();
        if(catalogObjectId != null) {
			CatalogObject category = categories.get(catalogObjectId);
	        if (category != null
            		&& category.getCategoryData() != null
            		&& category.getCategoryData().getName() != null) {
            	itemCategory = category.getCategoryData().getName().toUpperCase();
            }
        }
        else if(orderLineItem.getItemType().equals(CATEGORY_GIFT_CARD)) {
        	itemCategory = CATEGORY_GIFT_CARD;
        }

        return categoryTotals.getOrDefault(itemCategory, categoryTotals.get(CATEGORY_UNCATEGORIZED));
    }

    private List<PaymentRefund> getRefundsInReportDateRange(Calendar beginTime, Calendar endTime)
            throws ParseException {
        ArrayList<PaymentRefund> refundsInDateRange = new ArrayList<PaymentRefund>();

        for (PaymentRefund paymentRefund : paymentRefunds) {
            Calendar refundTime = TimeManager.toCalendar(paymentRefund.getCreatedAt());
            if (beginTime.compareTo(refundTime) <= 0 && endTime.compareTo(refundTime) >= 0) {
                refundsInDateRange.add(paymentRefund);
            }
        }

        return refundsInDateRange;
    }

    private void addSaleCategoryTotals(CategoryData category, OrderLineItem orderLineItem) {
    	if(orderLineItem.getQuantity() != null) {
            category.increaseItemsSoldQty(Integer.parseInt(orderLineItem.getQuantity()));
    	}
        if(orderLineItem.getGrossSalesMoney() != null) {
            category.increaseGrossSalesTotal(orderLineItem.getGrossSalesMoney().getAmount());
        }
        if(orderLineItem.getTotalDiscountMoney() != null) {
            category.increaseDiscountsTotal(orderLineItem.getTotalDiscountMoney().getAmount());
        }
        if(orderLineItem.getTotalTaxMoney() != null) {
        	category.increaseTaxesTotal(orderLineItem.getTotalTaxMoney().getAmount());
        }
    }

    private void refundFullCategoryTotals(CategoryData category, OrderLineItem orderLineItem) {
    	if(orderLineItem.getQuantity() != null) {
            category.increaseItemsRefundedQty(Integer.parseInt(orderLineItem.getQuantity()));
    	}
        if(orderLineItem.getGrossSalesMoney() != null) {
            category.increaseRefundedTotal(orderLineItem.getGrossSalesMoney().getAmount());
        }
        if(orderLineItem.getTotalDiscountMoney() != null) {
            category.decreaseDiscountsTotal(orderLineItem.getTotalDiscountMoney().getAmount());
        }
        if(orderLineItem.getTotalTaxMoney() != null) {
        	category.decreaseTaxesTotal(orderLineItem.getTotalTaxMoney().getAmount());
        }
    }

    private void refundPartialCategoryTotals(PaymentRefund paymentRefund) {
        CategoryData category = categoryTotals.get(CATEGORY_UNASSIGNED_REFUNDS);

        if(paymentRefund.getAmountMoney() != null) {
        	int positiveTotal = paymentRefund.getAmountMoney().getAmount();
            category.increaseRefundedTotal(positiveTotal);
            category.increaseItemsRefundedQty(1);
        }
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

    private CategoryData getMergedSundaeData() {
        return mergeCategoryGroup(CATEGORY_IC_SUNDAE, IC_TO_MERGED_SUNDAE);
    }

    private CategoryData getMergedDrinksData() {
        return mergeCategoryGroup(CATEGORY_IC_DRINKS, IC_TO_MERGED_DRINKS);
    }

    private CategoryData getMergedPackedData() {
        return mergeCategoryGroup(CATEGORY_IC_HANDPACK, IC_TO_MERGED_PACKED);
    }

    private CategoryData getMergedCakeNoveltyData() {
        return mergeCategoryGroup(CATEGORY_IC_CAKE_NOVELTY, IC_TO_MERGED_CAKE_NOVELTY);
    }

    private CategoryData getMergedColdBeveragesData() {
        return mergeCategoryGroup(CATEGORY_COLD_BEVERAGES, TO_MERGED_COLD_BEVERAGES);
    }

    private CategoryData getMergedHotBeveragesData() {
        return mergeCategoryGroup(CATEGORY_HOT_BEVERAGES, TO_MERGED_HOT_BEVERAGES);
    }

    private CategoryData getMergedBakedGoodsData() {
        return mergeCategoryGroup(CATEGORY_BAKED_GOODS, TO_MERGED_BAKED_GOODS);
    }

    private CategoryData getMergedPrepacksData() {
        return mergeCategoryGroup(CATEGORY_PREPACKS, TO_MERGED_PREPACKS);
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

    private int getReportableSalesTotal() {
        // Discounts are a negative value
        return getNetSalesTotal() + getDiscountsTotal();
    }

    public int getTotalTransactions() {
        int total = 0;

        for(Order order : orders.values()) {
        	boolean hasValidPaymentTender = false;
        	if(order != null && order.getTenders() != null) {
        		for(Tender tender : order.getTenders()) {
        			if(tender != null && !tender.getType().equals(Tender.TENDER_TYPE_NO_SALE)) {
        				hasValidPaymentTender = true;
        			}
        		}
        	}
        	if(hasValidPaymentTender) {
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
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_TO_IC_CONES)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_TOPPINGS)));
        rows.add(new CalculationReportRow(getTotalIcToppingsData()));
        rows.add(new CalculationReportRow());
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_IC_SUNDAE)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_TO_IC_SUNDAE)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_IC_DRINKS)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_TO_IC_DRINKS)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_IC_HANDPACK)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_TO_FRESH_PACKED)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_IC_CAKE_NOVELTY)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_TO_IC_CAKE_NOVELTY)));
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
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_TO_COLD_BEVERAGES)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_HOT_BEVERAGES)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_TO_HOT_BEVERAGES)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_BAKED_GOODS)));
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_TO_BAKED_GOODS)));
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
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_TO_PREPACKS)));
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
        rows.add(new CalculationReportRow(categoryTotals.get(CATEGORY_SERVICE_CHARGE)));

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
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, CATEGORY_IC_SUNDAE)
                .setColDollarValue(2, getMergedSundaeData().getReportableSalesTotal()).build());
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, CATEGORY_IC_DRINKS)
                .setColDollarValue(2, getMergedDrinksData().getReportableSalesTotal()).build());
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, CATEGORY_IC_HANDPACK)
                .setColDollarValue(2, getMergedPackedData().getReportableSalesTotal()).build());
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, CATEGORY_IC_CAKE_NOVELTY)
                .setColDollarValue(2, getMergedCakeNoveltyData().getReportableSalesTotal()).build());
        rows.add(new SummaryReportRow(getTotalOffPremiseData()));
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_ON_DEMAND)));
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, "Total Ice Cream Sales")
                .setColDollarValue(2, getTotalIceCreamSalesTotal()).build());
        rows.add(new SummaryReportRow());
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_SOFT_SERVE)));
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, CATEGORY_COLD_BEVERAGES)
                .setColDollarValue(2, getMergedColdBeveragesData().getReportableSalesTotal()).build());
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, CATEGORY_HOT_BEVERAGES)
                .setColDollarValue(2, getMergedHotBeveragesData().getReportableSalesTotal()).build());
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, CATEGORY_BAKED_GOODS)
                .setColDollarValue(2, getMergedBakedGoodsData().getReportableSalesTotal()).build());
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_PASTRY)));
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_CONFECTIONS)));
        rows.add(new SummaryReportRow(categoryTotals.get(CATEGORY_OTHER_FOODS)));
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, "RETAIL / GIFT (All)")
                .setColDollarValue(2, getTotalRetailData().getReportableSalesTotal()).build());
        rows.add(new SummaryReportRow.RowBuilder().setCol(1, CATEGORY_PREPACKS)
                .setColDollarValue(2, getMergedPrepacksData().getReportableSalesTotal()).build());
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
