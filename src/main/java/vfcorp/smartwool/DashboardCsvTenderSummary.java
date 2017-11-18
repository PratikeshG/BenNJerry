package vfcorp.smartwool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.TenderCardDetails;
import com.squareup.connect.v2.Transaction;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

public class DashboardCsvTenderSummary {
	private static final String TENDER_TYPE_CASH = "CASH";
	private static final String TENDER_TYPE_SQUARE_GIFT_CARD = "SQUARE_GIFT_CARD";
	private static final String TENDER_TYPE_CARD = "CARD";

    private static final String DIPPED_LABEL = "Dipped";
    private static final String TAPPED_LABEL = "Tapped";
    private static final String SWIPED_LABEL = "Swiped";
    private static final String KEYED_LABEL  = "Keyed";
    private static final String ONFILE_LABEL = "On File";
    private static final String NA_LABEL 	  = "N/A";

    private static Map<String, String> entryMethodLabelMap = ImmutableMap.<String, String>builder()
    		.put(TenderCardDetails.ENTRY_METHOD_SWIPED, SWIPED_LABEL)
    		.put(TenderCardDetails.ENTRY_METHOD_KEYED, KEYED_LABEL)
    		.put(TenderCardDetails.ENTRY_METHOD_EMV, DIPPED_LABEL)
    		.put(TenderCardDetails.ENTRY_METHOD_ON_FILE, ONFILE_LABEL)
    		.put(TenderCardDetails.ENTRY_METHOD_CONTACTLESS, TAPPED_LABEL)
    		.build();

    private static Map<String, String> cardBrandLabelMap = ImmutableMap.<String, String>builder()
    		.put("AMERICAN_EXPRESS", "American Express")
    		.put("VISA", "Visa")
    		.put("MASTERCARD", "MasterCard")
    		.put("DISCOVER", "Discover")
    		.put("DISCOVER_DINERS", "Discover Diners")
    		.put("SQUARE_GIFT_CARD", "Square Gift Card")
    		.put("OTHER_BRAND", "Other")
    		.build();

	private int cash = 0;
	private int giftCard = 0;
	private int card = 0;
	private int other = 0;
	private int swiped = 0;
	private int manual = 0;

	private HashSet<String> cardEntryMethods = new HashSet<>();
	private ArrayList<String> cardBrands = new ArrayList<>();
	private HashSet<String> panSuffi = new HashSet<String>();

	public int getGiftCard() {
		return giftCard;
	}
	public int getCard() {
		return card;
	}
	public int getCash() {
		return cash;
	}
	public int getOther() {
		return other;
	}
	public int getSwiped() {
		return swiped;
	}
	public int getManual() {
		return manual;
	}

	public String getCardBrands() {
		return StringUtils.join(this.cardBrands, ", ");
	}
	public String getCardEntryMethods() {
		List<String> entryMethods = new ArrayList<String>();
		for (String entryMethod : this.cardEntryMethods) {
			entryMethods.add(entryMethod);
		}
		return StringUtils.join(entryMethods, ", ");
	}
	public String getPanSuffi() {
		List<String> panSuffi = new ArrayList<String>();
		for (String panSuffix : this.panSuffi) {
			panSuffi.add(panSuffix);
		}
		return StringUtils.join(panSuffi, ", ");
	}
	public static DashboardCsvTenderSummary generateTenderSummary(Transaction transaction) throws Exception {
		DashboardCsvTenderSummary summary = new DashboardCsvTenderSummary();
		for (Tender tender : transaction.getTenders()) {
			int totalMoney = tender.getAmountMoney().getAmount();
			if (tender.getType().equals(TENDER_TYPE_CARD) || tender.getType().equals(TENDER_TYPE_SQUARE_GIFT_CARD)) {
				String entryMethod = tender.getCardDetails().getEntryMethod();
				String entryMethodLabel = entryMethodLabelMap.get(entryMethod);
				String cardBrandLabel = cardBrandLabelMap.get(tender.getCardDetails().getCard().getCardBrand());

				summary.cardEntryMethods.add(entryMethodLabel);
				summary.cardBrands.add(cardBrandLabel);
				summary.panSuffi.add(tender.getCardDetails().getCard().getLast4());
			}
			switch (tender.getType()) {
			case TENDER_TYPE_CASH:
				summary.cash += totalMoney;
				break;
			case TENDER_TYPE_SQUARE_GIFT_CARD:
				summary.giftCard += totalMoney;
				break;
			case TENDER_TYPE_CARD:
				summary.card += totalMoney;
				break;
			default:
				throw new Exception("Unknown tender type: " + tender.getType());
			}
		}
		if (summary.cardEntryMethods.size() == 0) {
			summary.cardEntryMethods.add(NA_LABEL);
		}
		return summary;
	}
}
