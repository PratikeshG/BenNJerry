package util.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.Tender;
import com.squareup.connect.v2.TenderCardDetails;

public class DashboardCsvTenderSummary {
	private static final String TENDER_TYPE_CASH = "CASH";
	private static final String TENDER_TYPE_SQUARE_GIFT_CARD = "SQUARE_GIFT_CARD";
	private static final String TENDER_TYPE_CARD = "CARD";
	private static final String TENDER_TYPE_NO_SALE = "NO_SALE";
	private static final String TENDER_TYPE_OTHER = "OTHER";

    private static final String DIPPED_LABEL = "Dipped";
    private static final String TAPPED_LABEL = "Tapped";
    private static final String SWIPED_LABEL = "Swiped";
    private static final String KEYED_LABEL  = "Keyed";
    private static final String ONFILE_LABEL = "On File";
    private static final String NA_LABEL 	  = "N/A";

    private static Map<String, String> entryMethodLabelMap;
    static {
        entryMethodLabelMap = new HashMap<>();
        entryMethodLabelMap.put(TenderCardDetails.ENTRY_METHOD_SWIPED, SWIPED_LABEL);
        entryMethodLabelMap.put(TenderCardDetails.ENTRY_METHOD_KEYED, KEYED_LABEL);
        entryMethodLabelMap.put(TenderCardDetails.ENTRY_METHOD_EMV, DIPPED_LABEL);
        entryMethodLabelMap.put(TenderCardDetails.ENTRY_METHOD_ON_FILE, ONFILE_LABEL);
        entryMethodLabelMap.put(TenderCardDetails.ENTRY_METHOD_CONTACTLESS, TAPPED_LABEL);
    }

    private static Map<String, String> cardBrandLabelMap;
    static {
        cardBrandLabelMap = new HashMap<>();
        cardBrandLabelMap.put("AMERICAN_EXPRESS", "American Express");
        cardBrandLabelMap.put("VISA", "Visa");
        cardBrandLabelMap.put("MASTERCARD", "MasterCard");
        cardBrandLabelMap.put("DISCOVER", "Discover");
        cardBrandLabelMap.put("DISCOVER_DINERS", "Discover Diners");
        cardBrandLabelMap.put("SQUARE_GIFT_CARD", "Square Gift Card");
        cardBrandLabelMap.put("OTHER_BRAND", "Other");
    }

	private int cash = 0;
	private int giftCard = 0;
	private int card = 0;
	private int other = 0;
	private int swiped = 0;
	private int manual = 0;

	private HashSet<String> cardEntryMethods = new HashSet<>();
	private ArrayList<String> cardBrands = new ArrayList<>();
	private HashSet<String> panSuffixes = new HashSet<String>();

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
		return String.join(", ", this.cardBrands);
	}
	public String getCardEntryMethods() {
		List<String> entryMethods = new ArrayList<String>();
		for (String entryMethod : this.cardEntryMethods) {
			entryMethods.add(entryMethod);
		}
		return String.join(", ", entryMethods);
	}
	public String getPanSuffixes() {
		List<String> panSuffixes = new ArrayList<String>();
		for (String panSuffix : this.panSuffixes) {
			panSuffixes.add(panSuffix);
		}
		return String.join(", ", panSuffixes);
	}

	public static DashboardCsvTenderSummary generateTenderSummary(Order order) throws Exception {
		DashboardCsvTenderSummary summary = new DashboardCsvTenderSummary();
		if(order.getTenders() != null) {
			for (Tender tender : order.getTenders()) {
				int totalMoney = tender.getAmountMoney().getAmount();
				if (tender.getType().equals(TENDER_TYPE_CARD) || tender.getType().equals(TENDER_TYPE_SQUARE_GIFT_CARD)) {
					String entryMethod = tender.getCardDetails().getEntryMethod();
					String entryMethodLabel = entryMethodLabelMap.get(entryMethod);
					String cardBrandLabel = cardBrandLabelMap.get(tender.getCardDetails().getCard().getCardBrand());

					summary.cardEntryMethods.add(entryMethodLabel);
					summary.cardBrands.add(cardBrandLabel);
					summary.panSuffixes.add(tender.getCardDetails().getCard().getLast4());
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
				case TENDER_TYPE_OTHER:
					summary.other += totalMoney;
				case TENDER_TYPE_NO_SALE:
					break;
				default:
					throw new Exception("Unknown tender type: " + tender.getType());
				}
			}
		}

		if (summary.cardEntryMethods.size() == 0) {
			summary.cardEntryMethods.add(NA_LABEL);
		}
		return summary;
	}
}
