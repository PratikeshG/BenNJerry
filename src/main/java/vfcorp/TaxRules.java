package vfcorp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.squareup.connect.Fee;

public class TaxRules {
    private static final int NY_EXEMPT_THRESHOLD = 11000;
    private static final int MA_EXEMPT_THRESHOLD = 17500;

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

    // PA - TNF Stores #79, 308, 316
    // 0% on clothing
    // Standard rate on all other items (non- clothing)
    private final static String TNF_PA_GROVE_CITY = "vfcorp-tnf-00079";
    private final static String TNF_PA_TANNERSVILLE = "vfcorp-tnf-00308";
    private final static String TNF_PA_PHILADELPHIA_OUTLET = "vfcorp-tnf-00316";

    // MN - TNF Stores #315, 513
    // 0% on clothing/apparel
    // Standard rate on all other items (non- clothing)
    private final static String TNF_MN_ALBERTVILLE = "vfcorp-tnf-00315";
    private final static String TNF_MN_MALL_OF_AMERICA = "vfcorp-tnf-00513";

    private static final Set<String> CLOTHING_DEPT_CLASS = new HashSet<String>(Arrays.asList(new String[] { "10  7076",
            "10  1000", "10  1001", "10  1002", "10  1020", "10  1021", "10  1040", "10  1050", "10  1055", "10  1200",
            "10  1500", "10  1501", "10  1502", "10  1520", "10  1521", "10  1540", "10  1550", "10  1555", "10  1600",
            "10  1601", "10  1602", "10  1603", "10  1604", "10  1605", "10  1606", "10  1607", "10  1610", "10  1611",
            "10  1612", "10  1613", "10  1614", "10  1615", "10  1616", "10  1620", "10  1621", "10  1622", "10  1623",
            "10  1624", "10  1625", "10  1626", "10  1627", "10  1630", "10  1631", "10  1700", "10  1701", "10  1702",
            "10  1703", "10  1704", "10  1705", "10  1706", "10  1707", "10  1710", "10  1711", "10  1712", "10  1713",
            "10  1714", "10  1715", "10  1716", "10  1720", "10  1721", "10  1722", "10  1723", "10  1724", "10  1725",
            "10  1726", "10  1727", "10  1730", "10  1731", "10  1800", "10  2002", "10  2021", "10  2040", "10  2502",
            "10  2520", "10  2521", "10  2540", "10  1801", "10  7074", "10  7075", "11  1011", "11  1012", "11  1013",
            "11  1014", "11  1015", "11  1016", "11  1101", "11  1102", "11  1103", "11  1104", "11  1105", "11  1106",
            "11  1201", "11  1202", "11  1203", "11  1204", "11  1205", "11  1206", "11  1300", "11  1301", "11  1302",
            "11  1303", "11  1304", "11  1305", "11  1306", "11  1307", "11  1207", "11  1308", "11  1208", "11  1309",
            "12  2005", "12  2006", "12  2007", "12  2008", "12  2009", "12  2010", "12  2100", "12  2101", "12  2102",
            "12  2103", "12  2104", "12  2105", "12  2200", "12  2201", "12  2202", "12  2203", "12  2204", "12  2205",
            "12  2300", "12  2301", "12  2302", "12  2303", "12  2304", "12  2305", "12  2400", "12  2401", "12  2402",
            "12  2403", "12  2404", "12  2405", "12  2505", "12  2506", "12  2507", "12  2508", "12  2509", "12  2510",
            "12  2511", "12  2512", "12  2600", "12  2601", "12  2602", "12  2603", "12  2604", "12  2700", "12  2702",
            "12  2703", "12  2704", "12  2701", "13  3050", "13  3051", "13  3052", "13  3053", "13  3054", "13  3100",
            "13  3101", "13  3102", "13  3103", "13  3104", "13  3200", "13  3201", "13  3202", "13  3203", "13  3204",
            "13  3300", "13  3301", "13  3302", "13  3303", "13  3304", "13  3305", "13  3306", "13  3307", "13  3400",
            "13  3401", "13  3402", "13  3403", "13  3404", "13  3500", "13  3501", "13  3502", "13  3503", "13  3504",
            "13  3505", "13  3506", "13  3507", "13  3600", "13  3601", "13  3602", "13  3603", "13  3800", "13  3801",
            "13  3802", "13  3803", "13  3804", "13  3805", "13  3806", "14  4010", "14  4011", "14  4012", "14  4013",
            "14  4100", "14  4101", "14  4102", "14  4103", "15  5636", "15  5060", "15  5061", "15  5062", "15  5063",
            "15  5064", "15  5065", "15  5110", "15  5111", "15  5112", "15  5113", "15  5114", "15  5115", "15  5200",
            "15  5201", "15  5202", "15  5203", "15  5204", "15  5205", "15  5300", "15  5301", "15  5302", "15  5303",
            "15  5304", "15  5305", "15  5306", "15  5307", "15  5400", "15  5401", "15  5402", "15  5403", "15  5520",
            "15  5521", "15  5522", "15  5523", "15  5620", "15  5621", "15  5622", "15  5623", "15  5624", "15  5625",
            "15  5720", "15  5721", "15  5722", "15  5723", "15  5724", "15  5725", "15  5070", "15  5071", "15  5072",
            "15  5073", "15  5074", "15  5075", "15  5130", "15  5131", "15  5132", "15  5133", "15  5134", "15  5135",
            "15  5140", "15  5141", "15  5142", "15  5143", "15  5144", "15  5145", "15  5250", "15  5251", "15  5252",
            "15  5253", "15  5254", "15  5320", "15  5321", "15  5322", "15  5323", "15  5324", "15  5325", "15  5326",
            "15  5327", "15  5330", "15  5331", "15  5332", "15  5333", "15  5334", "15  5335", "15  5336", "15  5337",
            "15  5420", "15  5421", "15  5422", "15  5423", "15  5530", "15  5531", "15  5532", "15  5533", "15  5540",
            "15  5541", "15  5542", "15  5543", "15  5630", "15  5631", "15  5632", "15  5633", "15  5634", "15  5635",
            "15  5730", "15  5731", "15  5732", "15  5733", "15  5734", "15  5735", "15  5740", "15  5741", "15  5742",
            "15  5743", "15  5744", "15  5745", "20  3000", "20  3001", "20  3002", "20  3003", "20  3021", "20  3022",
            "20  3023", "20  3040", "20  3041", "20  3042", "20  3043", "20  4000", "20  4001", "20  4002", "20  4003",
            "20  4021", "20  4022", "20  4023", "20  4040", "20  4041", "20  4042", "20  4043", "21  6403", "21  6005",
            "21  6006", "21  6105", "21  6106", "21  6205", "21  6206", "21  6305", "21  6306", "21  6400", "21  6401",
            "21  6402", "21  6600", "21  6601", "21  6602", "21  6603", "21  6700", "21  6701", "21  6702", "21  6703",
            "21  6800", "21  6801", "21  6802", "21  6803", "21  6900", "21  6704", "21  6804", "24  9010", "24  9011",
            "24  9012", "24  9013", "24  9014", "24  9015", "24  9016", "24  9020", "24  9021", "24  9022", "24  9023",
            "24  9024", "24  9025", "24  9026", "24  9030", "24  9031", "24  9040", "25  9920", "25  9921", "25  9922",
            "25  9923", "25  9930", "25  9931", "25  9932", "25  9933", "25  9940", "25  9941", "25  9942", "26  1201",
            "26  1202", "26  1203", "26  1204", "26  1205", "26  1206", "26  1207", "26  1208", "26  1300", "26  1301",
            "26  1302", "26  1303", "26  1304", "26  1305", "26  1306", "26  1307", "26  1308", "26  1309", "27  2801",
            "27  2802", "27  2803", "27  2811", "27  2812", "27  2813", "27  2822", "27  2831", "27  2832", "30  3011",
            "30  3029", "30  3030", "30  3600", "30  3601", "30  3602", "30  3603", "30  4600", "30  4601", "30  4602",
            "30  4603", "31  9144", "31  9145", "37  3753", "37  3758", "37  3700", "37  3701", "37  3705", "37  3706",
            "37  3710", "37  3711", "37  3712", "37  3713", "37  3714", "37  3715", "37  3720", "37  3721", "37  3722",
            "37  3723", "37  3724", "37  3725", "37  3730", "37  3731", "37  3735", "37  3736", "37  3740", "37  3741",
            "37  3744", "37  3745", "37  3746", "37  3747", "37  3748", "37  3749", "37  3750", "37  3751", "37  3752",
            "37  3755", "37  3756", "37  3760", "37  3765", "37  3757", "40  1004", "40  5000", "40  5001", "40  5002",
            "40  5010", "40  5011", "40  5012", "40  5013", "40  5014", "40  5020", "40  5021", "40  5022", "40  5023",
            "40  5024", "40  5032", "40  5040", "40  5041", "40  5050", "40  5051", "40  5052", "40  5053", "40  5054",
            "40  5100", "40  5101", "40  5102", "40  5104", "40  5150", "40  5151", "40  5170", "40  5171", "60  6020",
            "60  6021", "60  6022", "60  6023", "60  6024", "60  6030", "60  6031", "60  6032", "60  6033", "60  6034",
            "60  6040", "60  6041", "60  6042", "60  6043", "60  6044", "70  7000", "70  7001", "70  7002", "70  7003",
            "70  7004", "70  7005", "70  7021", "70  7050", "70  7051", "70  7053", "70  7055", "70  7070", "70  7071",
            "70  7072", "70  7073", "70  7074", "70  7075", "70  7077", "70  7080", "70  7081", "70  7082", "70  7083",
            "70  7084", "70  7085", "70  7086", "70  7090", "70  7091", "70  7092", "70  7093", "70  7094", "70  7076",
            "70  7111", "75  7500", "75  7540", "75  7550", "78  7811", "78  7820", "78  7821", "78  7830", "78  7840",
            "78  7841", "78  7842", "78  7843", "78  7844", "78  7850", "78  7851", "78  7852", "78  7853", "80  8003",
            "92  9200", "92  9201", "92  9202", "92  9203", "92  9204", "92  9210", "92  9211", "92  9220", "92  9221",
            "92  9250", "92  9251", "92  9252", "92  9253", "92  9254", "92  9260", "92  9261", "92  9270", "92  9271",
            "93  9300", "93  9301", "93  9302", "93  9303", "93  9310", "93  9311", "93  9350", "93  9351", "93  9352",
            "93  9353", "93  9360", "93  9361", "94  9400", "94  9410", "94  9450", "94  9460", "95  9500", "95  9501",
            "95  9502", "95  9503", "95  9550", "95  9551", "95  9552", "95  9553", "96  9600", "96  9610", "96  9620",
            "96  9630" }));

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
        } else if (deployment.equals(TNF_PA_GROVE_CITY) || deployment.equals(TNF_PA_TANNERSVILLE)
                || deployment.equals(TNF_PA_PHILADELPHIA_OUTLET)) {
            if (taxes.length != 1) {
                throw new Exception("Pennsylvania deployment with incorrect number of taxes: " + deployment);
            }

            if (isSpecialTaxCategoryPA(itemDeptClass)) {
                return new Fee[0];
            } else {
                return new Fee[] { taxes[0] };
            }
        } else if (deployment.equals(TNF_MN_ALBERTVILLE) || deployment.equals(TNF_MN_MALL_OF_AMERICA)) {
            if (taxes.length != 1) {
                throw new Exception("Minnesota deployment with incorrect number of taxes: " + deployment);
            }

            if (isSpecialTaxCategoryMN(itemDeptClass)) {
                return new Fee[0];
            } else {
                return new Fee[] { taxes[0] };
            }
        } else if (taxes.length != 1) {
            throw new Exception("Reguar taxed deployment/location with incorrect number of taxes: " + deployment);
        }

        return new Fee[] { taxes[0] };
    }

    private static boolean isSpecialTaxCategoryNYS(int price, String deptClass) {
        if (price < NY_EXEMPT_THRESHOLD && CLOTHING_DEPT_CLASS.contains(deptClass)) {
            return true;
        }
        return false;
    }

    // TODO(bhartard): MA clothing tax is actually rate*(N-$175)
    // Need to treat these differently
    private static boolean isSpecialTaxCategoryBoston(int price, String deptClass) {
        if (price <= MA_EXEMPT_THRESHOLD && CLOTHING_DEPT_CLASS.contains(deptClass)) {
            return true;
        }
        return false;
    }

    private static boolean isSpecialTaxCategoryPA(String deptClass) {
        if (CLOTHING_DEPT_CLASS.contains(deptClass)) {
            return true;
        }
        return false;
    }

    private static boolean isSpecialTaxCategoryMN(String deptClass) {
        if (CLOTHING_DEPT_CLASS.contains(deptClass)) {
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
