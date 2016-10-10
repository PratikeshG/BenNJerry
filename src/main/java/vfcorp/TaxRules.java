package vfcorp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.squareup.connect.Fee;

public class TaxRules {
    // New York, NY - TNF Stores #12, 18, 516
    // 0% on clothing & footwear below $110 per item
    // 8.875% on clothing & footwear $110 and above per item
    // 8.875% on all other items (non- clothing/footwear)
    private final static String TNF_NYC_BROADWAY = "vfcorp-tnf-00012";
    private final static String TNF_NYC_WOOSTER = "vfcorp-tnf-00018";
    private final static String TNF_NYC_FIFTH = "vfcorp-tnf-00516";

    // White Plains, NY - Westchester Co. - TNF Store #28
    // 4.375% on clothing & footwear below $110 per item
    // 8.375% on clothing & footwear $110 and above per item
    // 8.375% on all other items (non-clothing/footwear)
    private final static String TNF_NY_WHITEPLAINS = "vfcorp-tnf-00028";

    // Victor, NY - Ontario Co. - TNF Store #58
    // 3.5% on clothing & footwear below $110 per item
    // 7.5% on clothing & footwear $110 and above per item
    // 7.5% on all other items (non-clothing/footwear)
    private final static String TNF_NY_ONTARIO = "vfcorp-tnf-00058";

    // Central Valley, NY - Orange, Co. - TNF Store #64
    // 4.125% on clothing & footwear below $110 per item
    // 8.125% on clothing & footwear $110 and above per item
    // 8.125% on all other items (non-clothing/footwear)
    private final static String TNF_NY_WOODBURY = "vfcorp-tnf-00064";

    // Riverhead, NY  - Suffolk Co. - TNF Store #319
    // 4.625% on clothing & footwear below $110 per item
    // 8.625% on clothing & footwear $110 and above per item
    // 8.625% on all other items (non-clothing/footwear)
    private final static String TNF_NY_RIVERHEAD = "vfcorp-tnf-00319";

    // Boston
    // No sales tax on clothing (and shoes) that costs less than $175.
    // It it costs more than $175, you pay 6.25% on the amount over 175
    private final static String TNF_BOSTON = "vfcorp-tnf-00014";

    private static final Set<String> NYS_EXCLUSION_DEPT_CLASS = new HashSet<String>(Arrays.asList(new String[] { "1000",
            "1001", "1002", "1020", "1021", "1040", "1050", "1055", "1200", "1500", "1501", "1502", "1520", "1521",
            "1540", "1550", "1555", "1600", "1601", "1602", "1603", "1604", "1605", "1606", "1607", "1610", "1611",
            "1612", "1613", "1614", "1615", "1616", "1620", "1621", "1622", "1623", "1624", "1625", "1626", "1627",
            "1630", "1631", "1700", "1701", "1702", "1703", "1704", "1705", "1706", "1707", "1710", "1711", "1712",
            "1713", "1714", "1715", "1716", "1720", "1721", "1722", "1723", "1724", "1725", "1726", "1727", "1730",
            "1731", "1800", "1801", "2002", "2021", "2040", "2502", "2520", "2521", "2540", "1011", "1012", "1013",
            "1014", "1015", "1016", "1101", "1102", "1103", "1104", "1105", "1106", "1201", "1202", "1203", "1204",
            "1205", "1206", "1300", "1301", "1302", "1303", "1304", "1305", "1306", "1307", "1309", "2005", "2006",
            "2007", "2008", "2009", "2010", "2100", "2101", "2102", "2103", "2104", "2105", "2200", "2201", "2202",
            "2203", "2204", "2205", "2300", "2301", "2302", "2303", "2304", "2305", "2400", "2401", "2402", "2403",
            "2404", "2405", "2505", "2506", "2507", "2508", "2509", "2510", "2511", "2512", "2600", "2601", "2602",
            "2603", "2604", "2700", "2701", "2702", "2703", "2704", "3050", "3051", "3052", "3053", "3054", "3100",
            "3101", "3102", "3103", "3104", "3200", "3201", "3202", "3203", "3204", "3300", "3301", "3302", "3303",
            "3304", "3305", "3306", "3307", "3400", "3401", "3402", "3403", "3404", "3500", "3501", "3502", "3503",
            "3504", "3505", "3506", "3507", "4010", "4011", "4012", "4013", "4100", "4101", "4102", "4103", "5060",
            "5061", "5062", "5063", "5064", "5065", "5070", "5071", "5072", "5073", "5074", "5075", "5110", "5111",
            "5112", "5113", "5114", "5115", "5130", "5131", "5132", "5133", "5134", "5135", "5140", "5141", "5142",
            "5143", "5144", "5145", "5200", "5201", "5202", "5203", "5204", "5205", "5250", "5251", "5252", "5253",
            "5254", "5300", "5301", "5302", "5303", "5304", "5305", "5306", "5307", "5320", "5321", "5322", "5323",
            "5324", "5325", "5326", "5327", "5330", "5331", "5332", "5333", "5334", "5335", "5336", "5337", "5400",
            "5401", "5402", "5403", "5420", "5421", "5422", "5423", "5520", "5521", "5522", "5523", "5530", "5531",
            "5532", "5533", "5540", "5541", "5542", "5543", "5620", "5621", "5622", "5623", "5624", "5625", "5630",
            "5631", "5632", "5633", "5634", "5635", "5636", "5720", "5721", "5722", "5723", "5724", "5725", "5730",
            "5731", "5732", "5733", "5734", "5735", "5740", "5741", "5742", "5743", "5744", "5745", "3000", "3001",
            "3002", "3003", "3021", "3022", "3023", "3040", "3041", "3042", "3043", "4000", "4001", "4002", "4003",
            "4021", "4022", "4023", "4040", "4041", "4042", "4043", "6005", "6006", "6105", "6106", "6205", "6206",
            "6305", "6306", "6400", "6401", "6402", "6403", "6600", "6601", "6602", "6603", "6700", "6701", "6702",
            "6703", "6800", "6801", "6802", "6803", "6900", "7010", "7011", "7012", "7013", "7014", "7015", "7016",
            "7120", "7121", "7122", "7123", "7200", "7300", "7301", "7302", "7303", "7304", "7400", "7401", "7402",
            "7501", "7502", "7503", "7504", "7505", "8015", "8016", "8017", "8018", "8019", "8020", "8021", "8022",
            "8023", "8024", "8025", "8026", "8027", "8028", "8029", "8030", "8031", "8033", "8101", "8102", "8103",
            "8104", "8105", "8106", "8200", "8201", "8202", "8203", "8300", "8301", "8302", "8303", "9010", "9011",
            "9012", "9013", "9014", "9015", "9016", "9020", "9021", "9022", "9023", "9024", "9025", "9026", "9030",
            "9031", "9040", "9110", "1301", "3011", "3029", "3030", "3600", "3601", "3602", "3603", "4600", "4601",
            "4602", "4603", "9140", "9141", "9142", "9143", "9144", "9145", "9146", "9147", "3700", "3701", "3705",
            "3706", "3710", "3711", "3712", "3713", "3714", "3715", "3720", "3721", "3722", "3723", "3724", "3725",
            "3730", "3731", "3735", "3736", "3740", "3741", "3744", "3745", "3746", "3747", "3748", "3749", "3750",
            "3751", "3752", "3753", "3755", "3756", "3757", "3758", "3760", "3765", "3770", "3771", "3772", "3773",
            "3775", "3776", "3778", "3779", "3780", "1004", "5000", "5001", "5002", "5010", "5011", "5012", "5013",
            "5014", "5020", "5021", "5022", "5023", "5024", "5032", "5040", "5041", "5050", "5051", "5052", "5053",
            "5054", "5100", "5101", "5102", "5104", "5150", "5151", "5170", "5171", "5103", "5750", "6020", "6021",
            "6022", "6023", "6024", "6030", "6031", "6032", "6033", "6034", "6040", "6041", "6042", "6043", "6044",
            "7000", "7001", "7002", "7003", "7004", "7005", "7021", "7050", "7051", "7053", "7055", "7070", "7071",
            "7072", "7073", "7074", "7075", "7076", "7077", "7080", "7081", "7082", "7083", "7084", "7085", "7086",
            "7090", "7091", "7092", "7093", "7094", "7111", "7500", "7510", "7520", "7530", "7540", "7550", "7810",
            "7811", "7820", "7821", "7830", "7840", "7841", "7842", "7843", "7844", "7850", "7851", "7852", "7853",
            "8003", "8009", "8011", "9200", "9201", "9202", "9203", "9204", "9210", "9211", "9220", "9221", "9250",
            "9251", "9252", "9253", "9254", "9260", "9261", "9270", "9271", "9300", "9301", "9302", "9303", "9310",
            "9311", "9350", "9351", "9352", "9353", "9360", "9361", "9400", "9410", "9450", "9460", "9500", "9501",
            "9502", "9503", "9550", "9551", "9552", "9553", "9600", "9610", "9620", "9630", "9700", "9701", "9710",
            "9711", "9720", "9721", "9722", "9723", "9730", "9731", "9732", "9800", "9801", "9802", "9900", "9910",
            "9900", "9910" }));

    public static Fee[] getItemTaxesForLocation(String deployment, Fee[] taxes, int itemPrice, String itemDeptClass)
            throws Exception {

        if (deployment.equals(TNF_NYC_BROADWAY) || deployment.equals(TNF_NYC_WOOSTER)
                || deployment.equals(TNF_NYC_FIFTH)) {
            if (taxes.length != 1) {
                throw new Exception("NYC deployment with incorrect number of taxes: " + deployment);
            }

            if (isSpecialTaxCategoryNYS(itemPrice, itemDeptClass)) {
                return new Fee[0];
            } else {
                return new Fee[] { taxes[0] };
            }
        } else if (deployment.equals(TNF_BOSTON)) {
            if (taxes.length != 1) {
                throw new Exception("Boston deployment with incorrect number of taxes: " + deployment);
            }

            // We can't actually handle Boston taxes right now
            if (isSpecialTaxCategoryBoston(itemPrice, itemDeptClass)) {
                return new Fee[0];
            } else {
                return new Fee[] { taxes[0] };
            }
        } else if (deployment.equals(TNF_NY_WHITEPLAINS) || deployment.equals(TNF_NY_ONTARIO)
                || deployment.equals(TNF_NY_WOODBURY) || deployment.equals(TNF_NY_RIVERHEAD)) {
            if (taxes.length != 2) {
                throw new Exception("NYS deployment with incorrect number of taxes: " + deployment);
            }

            Fee lowTax = getLowerTax(taxes[0], taxes[1]);
            Fee highTax = getHigherTax(taxes[0], taxes[1]);

            if (isSpecialTaxCategoryNYS(itemPrice, itemDeptClass)) {
                return new Fee[] { lowTax };
            } else {
                return new Fee[] { highTax };
            }
        } else if (taxes.length != 1) {
            throw new Exception("Reguar taxed deployment/location with incorrect number of taxes: " + deployment);
        }

        return new Fee[] { taxes[0] };
    }

    private static boolean isSpecialTaxCategoryNYS(int price, String deptClass) {
        String c = deptClass.substring(deptClass.length() - 4);
        /*
         * The dept/classes they provided do not seem valid. Ignoring for now.
         *
         * if (price < 11000 && NYS_EXCLUSION_DEPT_CLASS.contains(c)) { return
         * true; }
         */
        if (price < 11000) {
            return true;
        }
        return false;
    }

    private static boolean isSpecialTaxCategoryBoston(int price, String deptClass) {
        String c = deptClass.substring(deptClass.length() - 4);
        /*
         * The dept/classes they provided do not seem valid. Ignoring for now.
         *
         * if (price < 17500 && NYS_EXCLUSION_DEPT_CLASS.contains(c)) { return
         * true; }
         */
        if (price < 17500) {
            return true;
        }
        return false;
    }

    private static Fee getLowerTax(Fee fee1, Fee fee2) {
        if (Float.parseFloat(fee1.getRate()) < Float.parseFloat(fee2.getRate())) {
            return fee1;
        }
        return fee2;
    }

    private static Fee getHigherTax(Fee fee1, Fee fee2) {
        if (Float.parseFloat(fee1.getRate()) >= Float.parseFloat(fee2.getRate())) {
            return fee1;
        }
        return fee2;
    }
}
