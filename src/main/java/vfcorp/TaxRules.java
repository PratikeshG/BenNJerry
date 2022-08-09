package vfcorp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.squareup.connect.v2.CatalogItem;
import com.squareup.connect.v2.CatalogItemVariation;
import com.squareup.connect.v2.CatalogObject;
import com.squareup.connect.v2.ItemVariationLocationOverride;

public class TaxRules {
    private static boolean ENABLE_AL_TAX_HOLIDAY = false;
    private static boolean ENABLE_AR_TAX_HOLIDAY = false;
    private static boolean ENABLE_CT_TAX_HOLIDAY = false;
    private static boolean ENABLE_IL_TAX_HOLIDAY = false;
    private static boolean ENABLE_FL_TAX_HOLIDAY = false;
    private static boolean ENABLE_IA_TAX_HOLIDAY = false;
    private static boolean ENABLE_MA_TAX_HOLIDAY = false;
    private static boolean ENABLE_MD_TAX_HOLIDAY = false;
    private static boolean ENABLE_MO_TAX_HOLIDAY = false;
    private static boolean ENABLE_NM_TAX_HOLIDAY = false;
    private static boolean ENABLE_OH_TAX_HOLIDAY = false;
    private static boolean ENABLE_OK_TAX_HOLIDAY = false;
    private static boolean ENABLE_SC_TAX_HOLIDAY = false;
    private static boolean ENABLE_TN_TAX_HOLIDAY = false;
    private static boolean ENABLE_TX_TAX_HOLIDAY = false;
    private static boolean ENABLE_VA_TAX_HOLIDAY = false;

    private static String BRAND_TNF = "TNF";
    private static String BRAND_VANS = "VANS";

    // VANS bag fees
    private static final Set<String> VANS_SKUS_TAX_FREE = new HashSet<String>(
            Arrays.asList(new String[] { "195436643935", "887040993765", "757969465981", "191476107444", "400007022584",
                    "400007022331", "400007022416" }));

    // Canada deployments
    public static final String TNF_CANADA_DEPLOYMENT = "vfcorp-tnfca-";
    public static final String TNF_CANADA_YOUTH_SKUS_PATH = "/vfc-plu-filters/vfcorp-tnfca-youth-skus.txt";
    private static final Set<String> TNF_CANADA_YOUTH_TAX_CATALOG_IDS = new HashSet<String>(Arrays.asList(
            new String[] { "QBVIRG2LPEFDLPI2MKV7KLDX", "XMR5QCVNS6JANJTNBA72H2RD", "6FSZJMFIIODRXUAUC7JM4FZO" }));

    // Canada - Montreal, Quebec
    public static final String TNF_CANADA_1213 = "vfcorp-tnfca-01213";
    public static final String TNF_CANADA_1263 = "vfcorp-tnfca-01263";

    // Canada - British Columbia
    public static final String TNF_CANADA_1214 = "vfcorp-tnfca-01214";
    public static final String TNF_CANADA_1262 = "vfcorp-tnfca-01262";

    public static final int NY_EXEMPT_THRESHOLD = 11000;
    public static final int MA_EXEMPT_THRESHOLD = 17500;
    public static final int RI_EXEMPT_THRESHOLD = 25000;

    // Stores with bag fees
    public final static String TNF_POST_ST = "vfcorp-tnf-00001";
    public final static String TNF_CHICAGO = "vfcorp-tnf-00010";
    public final static String TNF_VALLEY_FAIR = "vfcorp-tnf-00021";
    public final static String TNF_BETHESDA = "vfcorp-tnf-00048";
    public final static String TNF_STANFORD = "vfcorp-tnf-00517";

    // Tax free bag SKUs
    public final static String BAG_4508 = "040005164508";
    public final static String BAG_4909 = "040004834909";
    public final static String BAG_3039 = "040008783039";
    public final static String BAG_4632 = "040007174632";
    public final static String BAG_8465 = "040009948465";

    // TAX HOLIDAY STORES
    // AL
    private static final String TNF_AL_BIRMINGHAM = "vfcorp-tnf-00056";
    private static final int AL_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING = 10000;
    private static final int AL_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES = 5000;

    // CT
    private static final String TNF_CT_509 = "vfcorp-tnf-00509";
    private static final int CT_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING = 10000;

    // IL
    private static final int IL_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING = 10000;
    private static final int IL_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES = 0; //TODO: rramankutty@ to set up Percentage tax rule for school supplies

    // TN
    private static final String TNF_TN_306 = "vfcorp-tnf-00306";
    private static final String TNF_TN_505 = "vfcorp-tnf-00505";
    private static final int TN_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING = 20000;
    private static final int TN_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES = 20000;

    // FL
    private static final String TNF_FL_69 = "vfcorp-tnf-00069";
    private static final String TNF_FL_85 = "vfcorp-tnf-00085";
    private static final String TNF_FL_87 = "vfcorp-tnf-00087";
    private static final String TNF_FL_301 = "vfcorp-tnf-00301";
    private static final String TNF_FL_309 = "vfcorp-tnf-00309";
    private static final String TNF_FL_521 = "vfcorp-tnf-00521";
    private static final int FL_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING = 6000;
    private static final int FL_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES = 1500;

    // OH
    private static final String TNF_OH_37 = "vfcorp-tnf-00037";
    // OH store #43 defined separately below
    private static final String TNF_OH_75 = "vfcorp-tnf-00075";
    private static final String TNF_OH_305 = "vfcorp-tnf-00305";
    // OH store #320 defined separately below
    private static final String TNF_OH_504 = "vfcorp-tnf-00504";
    private static final int OH_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING = 7500;
    private static final int OH_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES = 2000;

    // OK
    private static final String TNF_OK_312 = "vfcorp-tnf-00312";
    private static final int OK_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING = 10000;

    // SC
    private static final String TNF_SC_82 = "vfcorp-tnf-00082";
    private static final String TNF_SC_324 = "vfcorp-tnf-00324";
    private static final String TNF_SC_325 = "vfcorp-tnf-00325";

    // VA
    private static final String TNF_VA_17 = "vfcorp-tnf-00017";
    private static final String TNF_VA_77 = "vfcorp-tnf-00077";
    private static final String TNF_VA_310 = "vfcorp-tnf-00310";
    private static final String TNF_VA_328 = "vfcorp-tnf-00328";
    private static final int VA_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING = 10000;
    private static final int VA_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES = 2000;

    // TX
    private static final String TNF_TX_59 = "vfcorp-tnf-00059";
    private static final String TNF_TX_83 = "vfcorp-tnf-00083";
    private static final String TNF_TX_86 = "vfcorp-tnf-00086";
    private static final String TNF_TX_323 = "vfcorp-tnf-00323";
    private static final String TNF_TX_327 = "vfcorp-tnf-00327";
    private static final String TNF_TX_515 = "vfcorp-tnf-00515";
    private static final String TNF_TX_520 = "vfcorp-tnf-00520";
    private static final int TX_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING = 10000;
    private static final int TX_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES = 10000;

    // MD
    private static final String TNF_MD_48 = "vfcorp-tnf-00048";
    private static final String TNF_MD_84 = "vfcorp-tnf-00084";
    private static final String TNF_MD_514 = "vfcorp-tnf-00514";
    private static final String TNF_MD_534 = "vfcorp-tnf-00534";
    private static final int MD_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING = 10000;
    private static final int MD_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES = 4000;

    // MO
    private static final String TNF_MO_25 = "vfcorp-tnf-00025";
    private static final String TNF_MO_529 = "vfcorp-tnf-00529";
    private static final int MO_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING = 10000;
    private static final int MO_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES = 5000;

    // NM
    private static final String TNF_NM_54 = "vfcorp-tnf-00054";
    private static final int NM_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING = 10000;
    private static final int NM_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES = 3000;
    private static final int NM_TAX_HOLIDAY_EXEMPT_THRESHOLD_BACKPACKS = 10000;

    // New York, NY - TNF Stores #12, 18, 516, 527
    // 0% on clothing & footwear below $110 per item
    // 8.875% on clothing & footwear $110 and above per item
    // 8.875% on all other items (non-clothing/footwear)
    public final static String TNF_NYC_BROADWAY = "vfcorp-tnf-00012";
    public final static String TNF_NYC_WOOSTER = "vfcorp-tnf-00018";
    public final static String TNF_NYC_FIFTH = "vfcorp-tnf-00516";
    public final static String TNF_NYC_SOHO = "vfcorp-tnf-00527";

    // White Plains, NY - Westchester Co. - TNF Store #28
    // 4.375% on clothing & footwear below $110 per item
    // 8.375% on clothing & footwear $110 and above per item
    // 8.375% on all other items (non-clothing/footwear)
    public final static String TNF_NY_WHITEPLAINS = "vfcorp-tnf-00028";

    // Victor, NY - Ontario Co. - TNF Store #58
    // 3.5% on clothing & footwear below $110 per item
    // 7.5% on clothing & footwear $110 and above per item
    // 7.5% on all other items (non-clothing/footwear)
    public final static String TNF_NY_ONTARIO = "vfcorp-tnf-00058";

    // Central Valley, NY - Orange, Co. - TNF Store #64
    // 4.125% on clothing & footwear below $110 per item
    // 8.125% on clothing & footwear $110 and above per item
    // 8.125% on all other items (non-clothing/footwear)
    public final static String TNF_NY_WOODBURY = "vfcorp-tnf-00064";

    // Riverhead, NY  - Suffolk Co. - TNF Store #319
    // 4.625% on clothing & footwear below $110 per item
    // 8.625% on clothing & footwear $110 and above per item
    // 8.625% on all other items (non-clothing/footwear)
    public final static String TNF_NY_RIVERHEAD = "vfcorp-tnf-00319";

    // MA
    private static final int MA_TAX_HOLIDAY_EXEMPT_THRESHOLD = 250000;

    // Iowa
    private static final int IA_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING = 10000;

    // No sales tax on clothing (and shoes) that costs less than (or equal to) $175.
    // It it costs more than $175, you pay 6.25% on the amount over 175
    public final static String TNF_BOSTON = "vfcorp-tnf-00014";
    public final static String TNF_PEABODY = "vfcorp-tnf-00039";
    public final static String TNF_BRAINTREE = "vfcorp-tnf-00053";

    // Rhode Island
    // No sales tax on clothing (and shoes) that costs less than (or equal to) $250.
    // It it costs more than $250, you pay 7% on the amount over 250
    public final static String TNF_RHODE_ISLAND = "vfcorp-tnf-00508";

    // PA - TNF Stores #79, 308, 316
    // 0% on clothing
    // Standard rate on all other items (non- clothing)
    public final static String TNF_PA_KING_OF_PRUSSIA = "vfcorp-tnf-00029";
    public final static String TNF_PA_GROVE_CITY = "vfcorp-tnf-00079";
    public final static String TNF_PA_TANNERSVILLE = "vfcorp-tnf-00308";
    public final static String TNF_PA_PHILADELPHIA_OUTLET = "vfcorp-tnf-00316";
    public final static String TNF_PA_LANCASTER = "vfcorp-tnf-00329";

    // NJ
    // 0% on clothing
    // Standard rate on all other items (non- clothing)
    public final static String TNF_NJ_CHERRY_HILL = "vfcorp-tnf-00055";
    public final static String TNF_NJ_CHERRY_HILL_533 = "vfcorp-tnf-00533";
    public final static String TNF_NJ_510 = "vfcorp-tnf-00510";

    // MN - TNF Stores #22, 315, 513
    // 0% on clothing/apparel
    // Standard rate on all other items (non- clothing)
    public final static String TNF_MN_MINNEAPOLIS = "vfcorp-tnf-00022";
    public final static String TNF_MN_ALBERTVILLE = "vfcorp-tnf-00315";
    public final static String TNF_MN_MALL_OF_AMERICA = "vfcorp-tnf-00513";
    public final static String TNF_MN_EDINA = "vfcorp-tnf-00523";

    // Nebraska Crossing TNF Stores #317
    // 7.0 Sales Tax
    // 1.95 Occupancy Tax
    public final static String TNF_NE_NEBRASKA_CROSSING = "vfcorp-tnf-00317";

    // Columbus, OH TNF Stores #43 and #320
    // 7.0 Sales Tax
    // 0.50 Mall Tax
    public final static String TNF_OH_COLUMBUS = "vfcorp-tnf-00043";
    public final static String TNF_OH_COLUMBUS_OUTLET = "vfcorp-tnf-00320";

    // TODO(bhartard): Remove from source, load from DB or CSV
    private static final Set<String> TNF_CLOTHING_DEPT_CLASS = new HashSet<String>(Arrays.asList(new String[] {
            "10  7076", "10  1000", "10  1001", "10  1002", "10  1020", "10  1021", "10  1040", "10  1050", "10  1055",
            "10  1200", "10  1500", "10  1501", "10  1502", "10  1520", "10  1521", "10  1540", "10  1550", "10  1555",
            "10  1600", "10  1601", "10  1602", "10  1603", "10  1604", "10  1605", "10  1606", "10  1607", "10  1610",
            "10  1611", "10  1612", "10  1613", "10  1614", "10  1615", "10  1616", "10  1620", "10  1621", "10  1622",
            "10  1623", "10  1624", "10  1625", "10  1626", "10  1627", "10  1630", "10  1631", "10  1700", "10  1701",
            "10  1702", "10  1703", "10  1704", "10  1705", "10  1706", "10  1707", "10  1710", "10  1711", "10  1712",
            "10  1713", "10  1714", "10  1715", "10  1716", "10  1720", "10  1721", "10  1722", "10  1723", "10  1724",
            "10  1725", "10  1726", "10  1727", "10  1730", "10  1731", "10  1800", "10  2002", "10  2021", "10  2040",
            "10  2502", "10  2520", "10  2521", "10  2540", "10  1801", "10  7074", "10  7075", "11  1011", "11  1012",
            "11  1013", "11  1014", "11  1015", "11  1016", "11  1101", "11  1102", "11  1103", "11  1104", "11  1105",
            "11  1106", "11  1201", "11  1202", "11  1203", "11  1204", "11  1205", "11  1206", "11  1300", "11  1301",
            "11  1302", "11  1303", "11  1304", "11  1305", "11  1306", "11  1307", "11  1207", "11  1308", "11  1208",
            "11  1309", "12  2005", "12  2006", "12  2007", "12  2008", "12  2009", "12  2010", "12  2100", "12  2101",
            "12  2102", "12  2103", "12  2104", "12  2105", "12  2200", "12  2201", "12  2202", "12  2203", "12  2204",
            "12  2205", "12  2300", "12  2301", "12  2302", "12  2303", "12  2304", "12  2305", "12  2400", "12  2401",
            "12  2402", "12  2403", "12  2404", "12  2405", "12  2505", "12  2506", "12  2507", "12  2508", "12  2509",
            "12  2510", "12  2511", "12  2512", "12  2600", "12  2601", "12  2602", "12  2603", "12  2604", "12  2700",
            "12  2702", "12  2703", "12  2704", "12  2701", "13  3050", "13  3051", "13  3052", "13  3053", "13  3054",
            "13  3100", "13  3101", "13  3102", "13  3103", "13  3104", "13  3200", "13  3201", "13  3202", "13  3203",
            "13  3204", "13  3300", "13  3301", "13  3302", "13  3303", "13  3304", "13  3305", "13  3306", "13  3307",
            "13  3400", "13  3401", "13  3402", "13  3403", "13  3404", "13  3500", "13  3501", "13  3502", "13  3503",
            "13  3504", "13  3505", "13  3506", "13  3507", "13  3600", "13  3601", "13  3602", "13  3603", "13  3800",
            "13  3801", "13  3802", "13  3803", "13  3804", "13  3805", "13  3806", "14  4010", "14  4011", "14  4012",
            "14  4013", "14  4100", "14  4101", "14  4102", "14  4103", "15  5636", "15  5060", "15  5061", "15  5062",
            "15  5063", "15  5064", "15  5065", "15  5110", "15  5111", "15  5112", "15  5113", "15  5114", "15  5115",
            "15  5200", "15  5201", "15  5202", "15  5203", "15  5204", "15  5205", "15  5300", "15  5301", "15  5302",
            "15  5303", "15  5304", "15  5305", "15  5306", "15  5307", "15  5400", "15  5401", "15  5402", "15  5403",
            "15  5520", "15  5521", "15  5522", "15  5523", "15  5620", "15  5621", "15  5622", "15  5623", "15  5624",
            "15  5625", "15  5720", "15  5721", "15  5722", "15  5723", "15  5724", "15  5725", "15  5070", "15  5071",
            "15  5072", "15  5073", "15  5074", "15  5075", "15  5130", "15  5131", "15  5132", "15  5133", "15  5134",
            "15  5135", "15  5140", "15  5141", "15  5142", "15  5143", "15  5144", "15  5145", "15  5250", "15  5251",
            "15  5252", "15  5253", "15  5254", "15  5320", "15  5321", "15  5322", "15  5323", "15  5324", "15  5325",
            "15  5326", "15  5327", "15  5330", "15  5331", "15  5332", "15  5333", "15  5334", "15  5335", "15  5336",
            "15  5337", "15  5420", "15  5421", "15  5422", "15  5423", "15  5530", "15  5531", "15  5532", "15  5533",
            "15  5540", "15  5541", "15  5542", "15  5543", "15  5630", "15  5631", "15  5632", "15  5633", "15  5634",
            "15  5635", "15  5730", "15  5731", "15  5732", "15  5733", "15  5734", "15  5735", "15  5740", "15  5741",
            "15  5742", "15  5743", "15  5744", "15  5745", "20  3000", "20  3001", "20  3002", "20  3003", "20  3021",
            "20  3022", "20  3023", "20  3040", "20  3041", "20  3042", "20  3043", "20  4000", "20  4001", "20  4002",
            "20  4003", "20  4021", "20  4022", "20  4023", "20  4040", "20  4041", "20  4042", "20  4043", "21  6403",
            "21  6005", "21  6006", "21  6105", "21  6106", "21  6205", "21  6206", "21  6305", "21  6306", "21  6400",
            "21  6401", "21  6402", "21  6600", "21  6601", "21  6602", "21  6603", "21  6700", "21  6701", "21  6702",
            "21  6703", "21  6800", "21  6801", "21  6802", "21  6803", "21  6900", "21  6704", "21  6804", "24  9010",
            "24  9011", "24  9012", "24  9013", "24  9014", "24  9015", "24  9016", "24  9020", "24  9021", "24  9022",
            "24  9023", "24  9024", "24  9025", "24  9026", "24  9030", "24  9031", "24  9040", "25  9920", "25  9921",
            "25  9922", "25  9923", "25  9930", "25  9931", "25  9932", "25  9933", "25  9940", "25  9941", "25  9942",
            "26  1201", "26  1202", "26  1203", "26  1204", "26  1205", "26  1206", "26  1207", "26  1208", "26  1300",
            "26  1301", "26  1302", "26  1303", "26  1304", "26  1305", "26  1306", "26  1307", "26  1308", "26  1309",
            "27  2801", "27  2802", "27  2803", "27  2811", "27  2812", "27  2813", "27  2822", "27  2831", "27  2832",
            "30  3011", "30  3029", "30  3030", "30  3600", "30  3601", "30  3602", "30  3603", "30  4600", "30  4601",
            "30  4602", "30  4603", "31  9144", "31  9145", "37  3753", "37  3758", "37  3700", "37  3701", "37  3705",
            "37  3706", "37  3710", "37  3711", "37  3712", "37  3713", "37  3714", "37  3715", "37  3720", "37  3721",
            "37  3722", "37  3723", "37  3724", "37  3725", "37  3730", "37  3731", "37  3735", "37  3736", "37  3740",
            "37  3741", "37  3744", "37  3745", "37  3746", "37  3747", "37  3748", "37  3749", "37  3750", "37  3751",
            "37  3752", "37  3755", "37  3756", "37  3760", "37  3765", "37  3757", "40  1004", "40  5000", "40  5001",
            "40  5002", "40  5010", "40  5011", "40  5012", "40  5013", "40  5014", "40  5020", "40  5021", "40  5022",
            "40  5023", "40  5024", "40  5032", "40  5040", "40  5041", "40  5050", "40  5051", "40  5052", "40  5053",
            "40  5054", "40  5100", "40  5101", "40  5102", "40  5104", "40  5150", "40  5151", "40  5170", "40  5171",
            "60  6020", "60  6021", "60  6022", "60  6023", "60  6024", "60  6030", "60  6031", "60  6032", "60  6033",
            "60  6034", "60  6040", "60  6041", "60  6042", "60  6043", "60  6044", "70  7000", "70  7001", "70  7002",
            "70  7003", "70  7004", "70  7005", "70  7021", "70  7050", "70  7051", "70  7053", "70  7055", "70  7070",
            "70  7071", "70  7072", "70  7073", "70  7074", "70  7075", "70  7077", "70  7080", "70  7081", "70  7082",
            "70  7083", "70  7084", "70  7085", "70  7086", "70  7090", "70  7091", "70  7092", "70  7093", "70  7094",
            "70  7076", "70  7111", "75  7500", "75  7540", "75  7550", "78  7811", "78  7820", "78  7821", "78  7830",
            "78  7840", "78  7841", "78  7842", "78  7843", "78  7844", "78  7850", "78  7851", "78  7852", "78  7853",
            "80  8003", "92  9200", "92  9201", "92  9202", "92  9203", "92  9204", "92  9210", "92  9211", "92  9220",
            "92  9221", "92  9250", "92  9251", "92  9252", "92  9253", "92  9254", "92  9260", "92  9261", "92  9270",
            "92  9271", "93  9300", "93  9301", "93  9302", "93  9303", "93  9310", "93  9311", "93  9350", "93  9351",
            "93  9352", "93  9353", "93  9360", "93  9361", "94  9400", "94  9410", "94  9450", "94  9460", "95  9500",
            "95  9501", "95  9502", "95  9503", "95  9550", "95  9551", "95  9552", "95  9553", "96  9600", "96  9610",
            "96  9620", "96  9630", "27  2801", "27  2802", "27  2803", "27  2811", "27  2812", "27  2813", "27  2822",
            "27  2823", "27  2831", "27  2832", "29  2900", "29  2901", "29  2902", "29  2903", "29  2910", "29  2911",
            "29  2912", "29  2913", "29  2920", "29  2921", "29  2922", "29  2923", "29  2924", "29  2925", "29  2926",
            "29  2930", "29  2931", "29  2932", "29  2933", "29  2934", "29  2935", "29  2936", "97  9700", "97  9701",
            "97  9702", "27  2801", "27  2802", "27  2803", "27  2811", "27  2812", "27  2813", "27  2822", "27  2823",
            "27  2831", "27  2832", "29  2900", "29  2901", "29  2902", "29  2903", "29  2910", "29  2911", "29  2912",
            "29  2913", "29  2920", "29  2921", "29  2922", "29  2923", "29  2924", "29  2925", "29  2926", "29  2930",
            "29  2931", "29  2932", "29  2933", "29  2934", "29  2935", "29  2936", "97  9700", "97  9701", "97  9702",
            "97  9710", "97  9711", "97  9712", "97  9720", "97  9721", "97  9722", "97  9730", "97  9732", "110 1011",
            "110 1012", "110 1013", "110 1014", "110 1015", "110 1016", "111 1201", "111 1202", "111 1203", "111 1204",
            "111 1205", "111 1206", "111 1207", "111 1208", "112 1101", "112 1102", "112 1103", "112 1104", "112 1105",
            "112 1106", "113 1300", "113 1301", "113 1302", "113 1303", "113 1304", "113 1305", "113 1306", "113 1307",
            "113 1308", "113 1309", "120 2005", "120 2006", "120 2007", "120 2008", "120 2009", "120 2010", "120 2200",
            "120 2201", "120 2202", "120 2203", "120 2204", "120 2205", "120 2400", "120 2401", "120 2402", "120 2403",
            "120 2404", "120 2405", "120 2600", "120 2601", "120 2602", "120 2603", "121 2100", "121 2101", "121 2102",
            "121 2103", "121 2104", "121 2105", "121 2300", "121 2301", "121 2302", "121 2303", "121 2304", "121 2305",
            "121 2505", "121 2506", "121 2507", "121 2508", "121 2509", "121 2510", "121 2511", "121 2512", "121 2700",
            "121 2701", "123 2604", "123 2702", "123 2703", "123 2704", "131 3050", "131 3051", "131 3052", "131 3053",
            "131 3054", "131 3200", "131 3201", "131 3202", "131 3203", "131 3204", "131 3400", "131 3401", "131 3402",
            "131 3403", "131 3404", "132 3100", "132 3101", "132 3102", "132 3103", "132 3104", "132 3300", "132 3301",
            "132 3302", "132 3303", "132 3304", "132 3305", "132 3306", "132 3307", "132 3500", "132 3501", "132 3502",
            "132 3503", "132 3504", "132 3505", "132 3506", "132 3507", "132 3800", "132 3801", "132 3803", "132 3804",
            "132 3806", "133 3600", "133 3601", "133 3602", "133 3603", "133 3802", "133 3805", "140 4010", "140 4011",
            "140 4012", "140 4013", "140 4014", "141 4100", "141 4101", "141 4102", "141 4103", "141 4104", "150 5060",
            "150 5061", "150 5062", "150 5063", "150 5064", "150 5065", "150 5073", "150 5200", "150 5201", "150 5202",
            "150 5203", "150 5205", "150 5400", "150 5401", "150 5403", "150 5404", "150 5620", "150 5621", "150 5624",
            "151 5110", "151 5111", "151 5112", "151 5113", "151 5114", "151 5115", "151 5204", "151 5253", "151 5300",
            "151 5301", "151 5302", "151 5303", "151 5304", "151 5305", "151 5306", "151 5307", "151 5323", "151 5324",
            "151 5326", "151 5333", "151 5334", "151 5336", "151 5520", "151 5521", "151 5523", "151 5524", "151 5721",
            "151 5724", "152 5070", "152 5071", "152 5072", "152 5074", "152 5075", "152 5130", "152 5131", "152 5132",
            "152 5133", "152 5134", "152 5135", "152 5143", "152 5250", "152 5251", "152 5252", "152 5254", "152 5320",
            "152 5321", "152 5322", "152 5325", "152 5327", "152 5402", "152 5420", "152 5421", "152 5422", "152 5423",
            "152 5424", "152 5522", "152 5530", "152 5531", "152 5532", "152 5533", "152 5534", "152 5541", "152 5542",
            "152 5630", "152 5731", "152 5734", "153 5140", "153 5141", "153 5142", "153 5144", "153 5145", "153 5330",
            "153 5331", "153 5332", "153 5335", "153 5337", "153 5540", "153 5543", "153 5544", "153 5622", "153 5623",
            "153 5625", "153 5631", "153 5632", "153 5633", "153 5634", "153 5635", "153 5636", "153 5720", "153 5722",
            "153 5723", "153 5725", "153 5730", "153 5732", "153 5733", "153 5735", "153 5740", "153 5741", "153 5742",
            "153 5743", "153 5744", "153 5745", "210 6005", "210 6006", "210 6105", "210 6106", "211 6205", "211 6206",
            "211 6305", "211 6306", "212 6400", "212 6401", "212 6402", "212 6403", "212 6900", "213 6600", "213 6601",
            "214 6602", "214 6603", "214 6700", "214 6701", "214 6702", "214 6703", "214 6704", "214 6800", "214 6801",
            "214 6802", "214 6803", "214 6804", "220 7010", "220 7011", "220 7012", "220 7013", "220 7014", "220 7015",
            "220 7016", "221 7120", "221 7121", "221 7122", "221 7123", "223 7200", "223 7300", "223 7301", "223 7302",
            "223 7303", "223 7304", "224 7400", "224 7401", "224 7402", "225 7501", "225 7502", "225 7503", "225 7504",
            "225 7505", "230 8015", "230 8016", "230 8017", "230 8018", "230 8019", "230 8020", "230 8021", "230 8022",
            "230 8023", "230 8024", "230 8025", "230 8026", "230 8027", "230 8028", "230 8029", "230 8030", "230 8031",
            "230 8033", "230 8034", "231 8101", "231 8102", "231 8103", "231 8104", "231 8105", "231 8106", "232 8200",
            "232 8201", "232 8202", "232 8203", "233 8300", "233 8301", "233 8302", "233 8303", "233 8304", "233 8305",
            "240 9010", "240 9011", "240 9012", "240 9013", "240 9014", "240 9015", "240 9016", "240 9040", "241 9020",
            "241 9021", "241 9022", "241 9023", "241 9024", "241 9025", "241 9026", "242 9030", "242 9031", "250 9110",
            "251 9920", "251 9921", "251 9922", "251 9923", "251 9930", "251 9931", "251 9932", "251 9933", "251 9940",
            "251 9941", "251 9942", "280 2801", "280 2802", "280 2803", "281 2811", "281 2812", "281 2813", "282 2821",
            "282 2822", "282 2823", "283 2831", "283 2832", "290 2900", "290 2901", "290 2902", "290 2903", "290 2920",
            "290 2921", "290 2922", "290 2923", "290 2924", "290 2925", "290 2926", "291 2910", "291 2911", "291 2912",
            "291 2913", "291 2930", "291 2931", "291 2932", "291 2933", "291 2934", "291 2935", "291 2936", "295 2950",
            "296 2960", "310 9140", "310 9141", "310 9142", "310 9143", "310 9144", "310 9145", "310 9146", "310 9147",
            "370 3700", "370 3701", "370 3705", "370 3706", "371 3710", "371 3711", "371 3712", "371 3713", "371 3714",
            "371 3715", "371 3720", "371 3721", "371 3722", "371 3723", "371 3724", "371 3725", "373 3730", "373 3731",
            "373 3735", "373 3736", "374 3740", "374 3741", "374 3744", "374 3745", "374 3746", "374 3747", "374 3748",
            "374 3749", "375 3750", "375 3751", "375 3752", "375 3753", "375 3755", "375 3756", "375 3757", "375 3758",
            "376 3760", "376 3765", "377 3770", "377 3771", "377 3772", "377 3773", "377 3775", "377 3776", "377 3778",
            "377 3779", "378 3780", "900 9001", "900 9004", "900 9005", "900 9006", "900 9007", "920 9200", "920 9201",
            "920 9202", "920 9203", "920 9204", "920 9210", "920 9211", "920 9220", "920 9221", "921 9250", "921 9251",
            "921 9252", "921 9253", "921 9254", "921 9260", "921 9261", "921 9270", "921 9271", "930 9300", "930 9301",
            "930 9302", "930 9303", "930 9310", "930 9311", "931 9350", "931 9351", "931 9352", "931 9353", "931 9360",
            "931 9361", "940 9400", "940 9410", "941 9450", "941 9460", "950 9500", "950 9501", "950 9502", "950 9503",
            "951 9550", "951 9551", "951 9552", "951 9553", "960 9600", "960 9610", "961 9620", "961 9630", "970 9700",
            "970 9701", "970 9702", "971 9710", "971 9711", "971 9712", "972 9720", "972 9721", "972 9722", "972 9723",
            "973 9730", "973 9731", "973 9732", "980 9800", "980 9801", "980 9802", "990 9900", "990 9910" }));

    private static final Set<String> TNFCA_TAX_EXEMPT_DEPT_CLASS = new HashSet<String>(
            Arrays.asList(new String[] { "90  9007" }));

    // Includes book bags -- day packs
    private static final Set<String> TNF_SCHOOL_SUPPLIES_DEPT_CLASS = new HashSet<String>(
            Arrays.asList(new String[] { "22  7010", "22  7011", "22  7012", "22  7013", "22  7014", "22  7015",
                    "27  2821", "31  9146", "31  9147", "220 7010", "220 7011", "220 7012", "220 7013", "220 7014",
                    "220 7015", "270 2821", "282 2821", "310 9146", "310 9147" }));

    // NUATICA #88 - NJ no nothing tax
    public final static String NAUTICA_NJ_ELIZABETH = "vfcorp-nautica-00088";

    private static final Set<String> NAUTICA_NON_CLOTHING_DEPT_CLASS = new HashSet<String>(Arrays.asList(new String[] {
            "208 1093", "770 7770", "777 7779", "777 7777", "818 8183", "202 1098", "202 1094", "200 1092" }));

    // VANS - Minnesota, Vermont, New Jersey, Pennsylvania - sales tax free clothing
    private static final List<String> VANS_MINNESOTA_STORES = new ArrayList<String>(
            Arrays.asList(new String[] { "vfcorp-vans-00082", "vfcorp-vans-00507" }));
    private static final List<String> VANS_VERMONT_STORES = new ArrayList<String>(Arrays.asList(new String[] {}));
    private static final List<String> VANS_NEW_JERSEY_STORES = new ArrayList<String>(Arrays.asList(new String[] {
            "vfcorp-vans-00119", "vfcorp-vans-00247", "vfcorp-vans-00331", "vfcorp-vans-00332", "vfcorp-vans-00334",
            "vfcorp-vans-00381", "vfcorp-vans-00382", "vfcorp-vans-00383", "vfcorp-vans-00411", "vfcorp-vans-00418",
            "vfcorp-vans-00485", "vfcorp-vans-00527", "vfcorp-vans-00535", "vfcorp-vans-00542", "vfcorp-vans-00549" }));
    private static final List<String> VANS_PENNSYLVANIA_STORES = new ArrayList<String>(Arrays.asList(new String[] {
            "vfcorp-vans-00120", "vfcorp-vans-00384", "vfcorp-vans-00390", "vfcorp-vans-00395", "vfcorp-vans-00402",
            "vfcorp-vans-00421", "vfcorp-vans-00442", "vfcorp-vans-00480", "vfcorp-vans-00481" }));

    // VANS - New York - exemption is limited to clothing costing less than $110 per item or pair.
    private static final List<String> VANS_NEW_YORK_STORES = new ArrayList<String>(Arrays.asList(new String[] {
            "vfcorp-vans-00321", "vfcorp-vans-00322", "vfcorp-vans-00330", "vfcorp-vans-00358", "vfcorp-vans-00365",
            "vfcorp-vans-00425", "vfcorp-vans-00458", "vfcorp-vans-00459", "vfcorp-vans-00460", "vfcorp-vans-00464",
            "vfcorp-vans-00479", "vfcorp-vans-00493", "vfcorp-vans-00550", "vfcorp-vans-00572", "vfcorp-vans-00429",
            "vfcorp-vans-00325", "vfcorp-vans-00329", "vfcorp-vans-00456", "vfcorp-vans-00566", "vfcorp-vans-00498",
            "vfcorp-vans-00512", "vfcorp-vans-00528", "vfcorp-vans-00533", "vfcorp-vans-00539", "vfcorp-vans-00540",
            "vfcorp-vans-00574", "vfcorp-test-00528", "vfcorp-test-00990" }));

    // VANS - New York - no county exemption on clothing
    private static final List<String> VANS_NEW_YORK_STORES_NON_EXEMPT = new ArrayList<String>(
            Arrays.asList(new String[] { "vfcorp-vans-00321", "vfcorp-vans-00322", "vfcorp-vans-00330",
                    "vfcorp-vans-00358", "vfcorp-vans-00365", "vfcorp-vans-00425", "vfcorp-vans-00458",
                    "vfcorp-vans-00459", "vfcorp-vans-00460", "vfcorp-vans-00464", "vfcorp-vans-00479",
                    "vfcorp-vans-00493", "vfcorp-vans-00550", "vfcorp-vans-00572", "vfcorp-vans-00574" }));

    // VANS - Massachusetts - clothing exemption is limited to the first $175 of an article of clothing. Anything over $175 per item is taxable.
    private static final List<String> VANS_MASSACHUSETTS_STORES = new ArrayList<String>(Arrays
            .asList(new String[] { "vfcorp-vans-00170", "vfcorp-vans-00377", "vfcorp-vans-00378", "vfcorp-vans-00389",
                    "vfcorp-vans-00451", "vfcorp-vans-00517", "vfcorp-vans-00552", "vfcorp-vans-00575" }));

    // VANS - Rhode Island – exemption applies to first $250 of sales price per item of clothing. Anything over $250 is taxable per item.
    private static final List<String> VANS_RHODE_ISLAND_STORES = new ArrayList<String>(
            Arrays.asList(new String[] { "vfcorp-vans-00380" }));

    private static final List<String> VANS_FLORIDA_STORES = new ArrayList<String>(Arrays.asList(new String[] {
            "vfcorp-vans-00003", "vfcorp-vans-00016", "vfcorp-vans-00114", "vfcorp-vans-00183", "vfcorp-vans-00185",
            "vfcorp-vans-00187", "vfcorp-vans-00188", "vfcorp-vans-00199", "vfcorp-vans-00222", "vfcorp-vans-00296",
            "vfcorp-vans-00320", "vfcorp-vans-00350", "vfcorp-vans-00354", "vfcorp-vans-00355", "vfcorp-vans-00366",
            "vfcorp-vans-00367", "vfcorp-vans-00372", "vfcorp-vans-00413", "vfcorp-vans-00419", "vfcorp-vans-00422",
            "vfcorp-vans-00423", "vfcorp-vans-00432", "vfcorp-vans-00433", "vfcorp-vans-00436", "vfcorp-vans-00437",
            "vfcorp-vans-00452", "vfcorp-vans-00462", "vfcorp-vans-00467", "vfcorp-vans-00475", "vfcorp-vans-00514",
            "vfcorp-vans-00522", "vfcorp-vans-00523", "vfcorp-vans-00546", "vfcorp-vans-00551", "vfcorp-vans-00557",
            "vfcorp-vans-00562" }));

    private static final String VANS_MO_495 = "vfcorp-vans-00495";

    private static final List<String> VANS_MISSOURI_STORES = new ArrayList<String>(
            Arrays.asList(new String[] { VANS_MO_495, "vfcorp-vans-00486", "vfcorp-vans-00518", "vfcorp-vans-00555" }));

    private static final List<String> VANS_NEW_MEXICO_STORES = new ArrayList<String>(
            Arrays.asList(new String[] { "vfcorp-vans-00193", "vfcorp-vans-00194" }));

    private static final List<String> VANS_OHIO_STORES = new ArrayList<String>(Arrays
            .asList(new String[] { "vfcorp-vans-00155", "vfcorp-vans-00496", "vfcorp-vans-00497", "vfcorp-vans-00504",
                    "vfcorp-vans-00525", "vfcorp-vans-00553", "vfcorp-vans-00547", "vfcorp-vans-00577" }));

    private static final List<String> VANS_OKLAHOMA_STORES = new ArrayList<String>(Arrays.asList(
            new String[] { "vfcorp-vans-00297", "vfcorp-vans-00328", "vfcorp-vans-00345", "vfcorp-vans-00374" }));

    private static final List<String> VANS_TEXAS_STORES = new ArrayList<String>(Arrays.asList(new String[] {
            "vfcorp-vans-00123", "vfcorp-vans-00160", "vfcorp-vans-00196", "vfcorp-vans-00197", "vfcorp-vans-00200",
            "vfcorp-vans-00212", "vfcorp-vans-00213", "vfcorp-vans-00214", "vfcorp-vans-00215", "vfcorp-vans-00218",
            "vfcorp-vans-00228", "vfcorp-vans-00229", "vfcorp-vans-00236", "vfcorp-vans-00244", "vfcorp-vans-00246",
            "vfcorp-vans-00248", "vfcorp-vans-00250", "vfcorp-vans-00251", "vfcorp-vans-00258", "vfcorp-vans-00259",
            "vfcorp-vans-00262", "vfcorp-vans-00272", "vfcorp-vans-00277", "vfcorp-vans-00281", "vfcorp-vans-00282",
            "vfcorp-vans-00285", "vfcorp-vans-00286", "vfcorp-vans-00292", "vfcorp-vans-00299", "vfcorp-vans-00317",
            "vfcorp-vans-00327", "vfcorp-vans-00338", "vfcorp-vans-00339", "vfcorp-vans-00340", "vfcorp-vans-00352",
            "vfcorp-vans-00353", "vfcorp-vans-00356", "vfcorp-vans-00357", "vfcorp-vans-00373", "vfcorp-vans-00453",
            "vfcorp-vans-00509", "vfcorp-vans-00521", "vfcorp-vans-00713" }));

    private static final List<String> VANS_VIRGINIA_STORES = new ArrayList<String>(Arrays.asList(new String[] {
            "vfcorp-vans-00080", "vfcorp-vans-00115", "vfcorp-vans-00235", "vfcorp-vans-00245", "vfcorp-vans-00283",
            "vfcorp-vans-00289", "vfcorp-vans-00291", "vfcorp-vans-00403", "vfcorp-vans-00406", "vfcorp-vans-00424",
            "vfcorp-vans-00465", "vfcorp-vans-00500", "vfcorp-vans-00554" }));

    private static final List<String> VANS_SOUTH_CAROLINA_STORES = new ArrayList<String>(
            Arrays.asList(new String[] { "vfcorp-vans-00083", "vfcorp-vans-00398", "vfcorp-vans-00409",
                    "vfcorp-vans-00463", "vfcorp-vans-00476", "vfcorp-vans-00543" }));

    private static final List<String> VANS_IOWA_STORES = new ArrayList<String>(
            Arrays.asList(new String[] { "vfcorp-vans-00537" }));

    private static final List<String> VANS_ARKANSAS_STORES = new ArrayList<String>(
            Arrays.asList(new String[] { "vfcorp-vans-00469" }));

    private static final List<String> VANS_MARYLAND_STORES = new ArrayList<String>(
            Arrays.asList(new String[] { "vfcorp-vans-00140", "vfcorp-vans-00165", "vfcorp-vans-00207",
                    "vfcorp-vans-00238", "vfcorp-vans-00271", "vfcorp-vans-00274", "vfcorp-vans-00400" }));

    private static final List<String> VANS_CONNECTICUT_STORES = new ArrayList<String>(Arrays.asList(new String[] {
            "vfcorp-vans-00397", "vfcorp-vans-00434", "vfcorp-vans-00443", "vfcorp-vans-00482", "vfcorp-vans-00505" }));

    private static final List<String> VANS_ILLINOIS_STORES = new ArrayList<String>(
            Arrays.asList(new String[] { "vfcorp-vans-00112", "vfcorp-vans-00341", "vfcorp-vans-00416",
                    "vfcorp-vans-00447", "vfcorp-vans-00534", "vfcorp-vans-00578" }));

    private static final Set<String> VANS_CLOTHING_DEPT_CLASS = new HashSet<String>(Arrays.asList(new String[] {
            "390 3970", "390 3965", "390 3960", "390 3955", "380 3820", "370 3775", "390 3935", "390 3930", "390 3945",
            "390 3940", "390 3905", "390 3910", "390 2110", "390 3900", "381 3867", "381 3866", "381 3854", "381 3846",
            "380 3875", "380 3880", "381 3801", "381 3830", "370 3799", "380 3800", "380 2102", "370 9920", "381 3836",
            "390 3915", "390 3920", "390 3925", "380 3805", "380 3815", "380 3810", "370 2106", "370 2107", "370 2108",
            "370 2301", "370 2302", "370 2304", "370 3720", "370 3725", "370 3755", "370 3760", "370 3765", "370 3770",
            "800 8201", "750 7520", "750 7510", "750 7500", "720 7200", "710 7110", "720 7210", "710 7100", "700 7000",
            "370 2105", "370 2104", "360 3635", "360 3625", "360 3630", "360 3620", "360 3615", "360 3610", "360 3605",
            "360 3600", "350 3530", "350 3500", "350 3510", "350 3515", "350 3520", "295 2959", "270 3788", "270 2410",
            "295 2954", "300 3009", "370 2100", "360 3640", "360 3645", "370 3750", "370 3745", "370 3740", "380 3870",
            "380 3868", "380 3865", "380 3860", "380 3850", "380 3855", "380 3845", "380 3840", "380 3835", "380 3825",
            "380 3830", "370 2306", "370 2350", "370 2901", "370 3715", "370 2902", "370 3705", "370 3710", "370 3700",
            "370 3725", "370 3730", "370 3735", "270 2006", "295 2009", "295 2950", "270 3788", "270 2514", "103 1240",
            "103 1040", "270 2508", "270 2509", "270 2510", "295 2955", "270 0225", "270 2512", "270 2505", "270 2506",
            "270 2507", "270 2511", "270 2513", "100 1005", "100 1006", "100 1007", "100 1008", "100 1009", "100 1010",
            "100 1011", "100 1012", "100 1013", "100 1014", "100 1015", "100 1020", "103 1030", "103 1130", "103 1230",
            "110 1105", "110 1105", "110 1106", "110 1107", "110 1108", "110 1109", "110 1110", "110 1111", "110 1112",
            "110 1113", "110 1114", "110 1115", "110 1115", "110 1120", "120 1205", "120 1206", "120 1207", "120 1208",
            "120 1209", "120 1210", "120 1211", "120 1212", "120 1213", "120 1214", "120 1215", "130 1305", "130 1306",
            "130 1307", "130 1308", "130 1309", "130 1310", "130 1311", "130 1312", "130 1315", "130 1320", "140 1405",
            "140 1406", "140 1408", "140 1409", "140 1410", "140 1411", "140 1415", "150 1505", "150 1506", "150 1507",
            "150 1508", "150 1509", "150 1510", "150 1511", "150 1512", "150 1515", "150 1520", "160 1606", "160 1608",
            "160 1610", "160 9900", "300 3099", "300 3199", "300 3299", "300 3399", "300 3499", "300 3599", "300 3699",
            // NEW HIERARCHY CODES AFTER PROJECT POLARIS - SAP TRANSITION
            "405 7700", "407 7702", "403 7093", "403 7617", "403 7089", "403 7100", "403 7616", "403 7096", "403 7107",
            "403 7111", "403 7103", "400 7019", "400 7009", "400 7645", "400 7240", "400 7000", "401 7138", "400 7143",
            "403 7083", "403 7084", "403 7082", "401 7031", "401 7029", "400 7140", "400 7141", "400 7270", "400 7016",
            "401 7021", "400 7013", "401 7020", "400 7003", "403 7063", "403 7067", "403 7059", "403 7072", "403 7079",
            "403 7614", "403 7075", "403 7122", "403 7068", "403 7123", "403 7615", "403 7119", "401 7207", "401 7028",
            "401 7137", "401 7030", "401 7023", "400 7007", "401 7034", "401 7035", "401 7622", "400 7006", "401 7134",
            "401 7045", "401 7136", "401 7026", "401 7049", "401 7027", "401 7036", "401 7008", "401 7033", "401 7022",
            "407 7215", "407 7104", "407 7211", "407 7222", "407 7203", "407 7218", "407 7229", "407 7233", "407 7225",
            "404 7145", "404 7135", "404 7623", "404 7629", "404 7126", "405 7281", "404 7630", "407 7086", "407 7087",
            "407 7085", "405 7156", "405 7619", "404 7626", "404 7627", "404 7625", "404 7142", "405 7147", "404 7139",
            "405 7146", "404 7129", "407 7185", "407 7189", "407 7181", "407 7194", "407 7201", "407 7101", "407 7197",
            "407 7040", "407 7190", "407 7245", "407 7102", "407 7241", "405 7124", "405 7154", "405 7208", "405 7155",
            "405 7149", "420 7193", "420 7192", "404 7133", "405 7159", "405 7620", "405 7618", "404 7132", "405 7205",
            "405 7169", "405 7125", "405 7152", "405 7157", "405 7153", "405 7160", "405 7010", "405 7158", "405 7148",
            "417 7469", "417 7121", "417 7465", "417 7476", "417 7120", "417 7472", "417 7117", "417 7042", "417 7118",
            "414 7635", "414 7395", "414 7634", "414 7640", "414 7376", "415 7280", "414 7641", "417 7463", "417 7464",
            "417 7088", "415 7407", "415 7284", "414 7637", "414 7638", "414 7636", "414 7392", "415 7397", "414 7380",
            "414 7389", "415 7396", "414 7379", "417 7108", "417 7116", "417 7112", "417 7105", "417 7106", "417 7114",
            "417 7110", "417 7113", "417 7109", "417 7204", "417 7115", "417 7041", "415 7239", "415 7404", "415 7244",
            "415 7406", "415 7399", "414 7383", "415 7410", "415 7411", "415 7621", "414 7382", "415 7540", "415 7131",
            "415 7272", "415 7402", "415 7408", "415 7403", "415 7412", "415 7011", "415 7409", "415 7398" }));

    private static final Set<String> VANS_SCHOOL_SUPPLIES_DEPT_CLASS = new HashSet<String>(
            Arrays.asList(new String[] { "402 7166", "406 7150", "406 7151", "416 7289", "416 7161" }));

    private static int getLocationPrice(CatalogItemVariation itemVariation, String locationId) {
        if (itemVariation.getLocationOverrides() != null) {
            for (ItemVariationLocationOverride locationPrice : itemVariation.getLocationOverrides()) {
                if (locationPrice.getLocationId().equals(locationId)) {
                    return locationPrice.getPriceMoney().getAmount();
                }
            }
        }

        // ignore variable priced items
        if (itemVariation.getPriceMoney() == null) {
            return 0;
        }
        return itemVariation.getPriceMoney().getAmount();
    }

    public static String[] getItemTaxesForLocation(CatalogItem item, CatalogObject[] taxes, String deployment,
            String locationId) throws Exception {
        CatalogItemVariation itemVariation = item.getVariations()[0].getItemVariationData();

        int itemPrice = getLocationPrice(itemVariation, locationId);
        String itemDeptClass = Util.getValueInParenthesis(itemVariation.getName());

        if (itemVariation.getSku() != null && (itemVariation.getSku().equals(BAG_4508)
                || itemVariation.getSku().equals(BAG_4909) || itemVariation.getSku().equals(BAG_3039)
                || itemVariation.getSku().equals(BAG_4632) || itemVariation.getSku().equals(BAG_8465)
                || VANS_SKUS_TAX_FREE.contains(itemVariation.getSku()))) {
            return new String[0];
        } else if (deployment.equals(TNF_NYC_BROADWAY) || deployment.equals(TNF_NYC_WOOSTER)
                || deployment.equals(TNF_NYC_FIFTH) || deployment.equals(TNF_NYC_SOHO)) {
            if (taxes.length != 1) {
                throw new Exception("NYC deployment with incorrect number of taxes: " + deployment);
            }

            if (isSpecialTaxCategoryNYS(itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_BOSTON) || deployment.equals(TNF_PEABODY)
                || deployment.equals(TNF_BRAINTREE)) {
            if (taxes.length != 1) {
                throw new Exception("MA deployment with incorrect number of taxes: " + deployment);
            }

            // We can't actually handle MA taxes right now
            if (isSpecialTaxCategoryMA(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_RHODE_ISLAND)) {
            if (taxes.length != 1) {
                throw new Exception("Rhode Island deployment with incorrect number of taxes: " + deployment);
            }

            // We can't actually handle Rhode Island taxes right now
            if (isSpecialTaxCategoryRhodeIsland(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_NY_WHITEPLAINS) || deployment.equals(TNF_NY_ONTARIO)
                || deployment.equals(TNF_NY_WOODBURY) || deployment.equals(TNF_NY_RIVERHEAD)) {
            if (taxes.length != 2) {
                throw new Exception("NYS deployment with incorrect number of taxes: " + deployment);
            }

            CatalogObject lowTax = getLowerTax(taxes[0], taxes[1]);
            CatalogObject highTax = getHigherTax(taxes[0], taxes[1]);

            if (isSpecialTaxCategoryNYS(itemPrice, itemDeptClass)) {
                return new String[] { lowTax.getId() };
            } else {
                return new String[] { highTax.getId() };
            }
        } else if (deployment.equals(TNF_PA_KING_OF_PRUSSIA) || deployment.equals(TNF_PA_GROVE_CITY)
                || deployment.equals(TNF_PA_TANNERSVILLE) || deployment.equals(TNF_PA_PHILADELPHIA_OUTLET)
                || deployment.equals(TNF_PA_LANCASTER)) {
            if (taxes.length != 1) {
                throw new Exception("Pennsylvania deployment with incorrect number of taxes: " + deployment);
            }

            if (isSpecialTaxCategoryPA(itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_NJ_CHERRY_HILL) || deployment.equals(TNF_NJ_CHERRY_HILL_533)
                || deployment.equals(TNF_NJ_510)) {
            if (taxes.length != 1) {
                throw new Exception("New Jersey deployment with incorrect number of taxes: " + deployment);
            }

            if (isSpecialTaxCategoryNJ(itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_MN_MINNEAPOLIS) || deployment.equals(TNF_MN_ALBERTVILLE)
                || deployment.equals(TNF_MN_MALL_OF_AMERICA) || deployment.equals(TNF_MN_EDINA)) {
            if (taxes.length != 1) {
                throw new Exception("Minnesota deployment with incorrect number of taxes: " + deployment);
            }

            if (isSpecialTaxCategoryMN(itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_NE_NEBRASKA_CROSSING)) {
            if (taxes.length != 2) {
                throw new Exception("Nebraska Crossing deployment with incorrect number of taxes: " + deployment);
            }
            return new String[] { taxes[0].getId(), taxes[1].getId() };
        } else if (deployment.equals(TNF_OH_COLUMBUS) || deployment.equals(TNF_OH_COLUMBUS_OUTLET)) {
            if (taxes.length != 2) {
                throw new Exception("Columbus deployment with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemOH(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId(), taxes[1].getId() };
            }
        } else if (deployment.equals(TNF_AL_BIRMINGHAM)) {
            if (taxes.length != 1) {
                throw new Exception("Alabama with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemAL(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_CT_509)) {
            if (taxes.length != 1) {
                throw new Exception("CT with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemCT(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_TN_306) || deployment.equals(TNF_TN_505)) {
            if (taxes.length != 1) {
                throw new Exception("TN with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemTN(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_FL_69) || deployment.equals(TNF_FL_85) || deployment.equals(TNF_FL_87)
                || deployment.equals(TNF_FL_301) || deployment.equals(TNF_FL_309) || deployment.equals(TNF_FL_521)) {
            if (taxes.length != 1) {
                throw new Exception("FL with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemFL(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_OH_37) || deployment.equals(TNF_OH_75) || deployment.equals(TNF_OH_305)
                || deployment.equals(TNF_OH_504)) {
            if (taxes.length != 1) {
                throw new Exception("OH with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemOH(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_OK_312)) {
            if (taxes.length != 1) {
                throw new Exception("OK with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemOK(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_SC_82) || deployment.equals(TNF_SC_324) || deployment.equals(TNF_SC_325)) {
            if (taxes.length != 1) {
                throw new Exception("SC with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemSC(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_VA_17) || deployment.equals(TNF_VA_77) || deployment.equals(TNF_VA_310)
                || deployment.equals(TNF_VA_328)) {
            if (taxes.length != 1) {
                throw new Exception("VA with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemVA(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_TX_59) || deployment.equals(TNF_TX_83) || deployment.equals(TNF_TX_86)
                || deployment.equals(TNF_TX_323) || deployment.equals(TNF_TX_327) || deployment.equals(TNF_TX_515)
                || deployment.equals(TNF_TX_520)) {
            if (taxes.length != 1) {
                throw new Exception("TX with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemTX(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_MD_48) || deployment.equals(TNF_MD_84) || deployment.equals(TNF_MD_514)
                || deployment.equals(TNF_MD_534)) {
            if (taxes.length != 1) {
                throw new Exception("MD with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemMD(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_MO_25) || deployment.equals(TNF_MO_529)) {
            if (taxes.length != 1) {
                throw new Exception("MO with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemMO(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(TNF_NM_54)) {
            if (taxes.length != 1) {
                throw new Exception("NM with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemNM(BRAND_TNF, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(NAUTICA_NJ_ELIZABETH)) {
            if (taxes.length != 1) {
                throw new Exception("NJ location with incorrect number of taxes: " + deployment);
            }

            if (isTaxedCategoryNauticaNJ(itemDeptClass)) {
                return new String[] { taxes[0].getId() };
            } else {
                return new String[0];
            }
        } else if (VANS_VERMONT_STORES.contains(deployment) || VANS_MINNESOTA_STORES.contains(deployment)
                || VANS_PENNSYLVANIA_STORES.contains(deployment) || VANS_NEW_JERSEY_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS clothing tax free location with incorrect number of taxes: " + deployment);
            }

            if (!VANS_CLOTHING_DEPT_CLASS.contains(itemDeptClass)) {
                return new String[] { taxes[0].getId() };
            } else {
                return new String[0];
            }
        } else if (VANS_NEW_YORK_STORES.contains(deployment)) {
            if (VANS_NEW_YORK_STORES_NON_EXEMPT.contains(deployment)) {
                if (taxes.length != 2) {
                    //throw new Exception("NYS deployment with incorrect number of taxes: " + deployment);
                }

                CatalogObject lowTax = getLowerTax(taxes[0], taxes[1]);
                CatalogObject highTax = getHigherTax(taxes[0], taxes[1]);

                if (vansIsSpecialTaxCategoryNY(itemPrice, itemDeptClass)) {
                    return new String[] { lowTax.getId() };
                } else {
                    return new String[] { highTax.getId() };
                }
            } else {
                if (taxes.length != 1) {
                    throw new Exception("VANS NY location with incorrect number of taxes: " + deployment);
                }

                if (vansIsSpecialTaxCategoryNY(itemPrice, itemDeptClass)) {
                    return new String[0];
                } else {
                    return new String[] { taxes[0].getId() };
                }
            }
        } else if (VANS_ILLINOIS_STORES.contains(deployment)) {
            if (taxes.length != 2) {
                throw new Exception("VANS IL with incorrect number of taxes: " + deployment);
            }

            CatalogObject lowTax = getLowerTax(taxes[0], taxes[1]);
            CatalogObject highTax = getHigherTax(taxes[0], taxes[1]);

            if (isTaxHolidayItemIL(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[] { lowTax.getId() };
            } else {
                return new String[] { highTax.getId() };
            }

        } else if (VANS_MASSACHUSETTS_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS MA location with incorrect number of taxes: " + deployment);
            }

            if (isSpecialTaxCategoryMA(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (VANS_FLORIDA_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS FL with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemFL(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.equals(VANS_MO_495)) {
            if (taxes.length != 2) {
                throw new Exception("VANS MO with incorrect number of taxes: " + deployment);
            }

            CatalogObject lowTax = getLowerTax(taxes[0], taxes[1]);
            CatalogObject highTax = getHigherTax(taxes[0], taxes[1]);

            if (isTaxHolidayItemMO(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[] { lowTax.getId() };
            } else {
                return new String[] { highTax.getId() };
            }
        } else if (VANS_MISSOURI_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS MO with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemMO(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (VANS_NEW_MEXICO_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS NM with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemNM(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (VANS_OHIO_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS OH with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemOH(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (VANS_OKLAHOMA_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS OK with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemOK(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (VANS_TEXAS_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS TX with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemTX(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (VANS_VIRGINIA_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS VA with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemVA(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (VANS_SOUTH_CAROLINA_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS SC with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemSC(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (VANS_IOWA_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS IA with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemIA(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (VANS_MARYLAND_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS MD with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemMD(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (VANS_CONNECTICUT_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS CT with incorrect number of taxes: " + deployment);
            }

            if (isTaxHolidayItemCT(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }

        } else if (VANS_RHODE_ISLAND_STORES.contains(deployment)) {
            if (taxes.length != 1) {
                throw new Exception("VANS RI location with incorrect number of taxes: " + deployment);
            }

            if (isSpecialTaxCategoryRhodeIsland(BRAND_VANS, itemPrice, itemDeptClass)) {
                return new String[0];
            } else {
                return new String[] { taxes[0].getId() };
            }
        } else if (deployment.startsWith(TNF_CANADA_DEPLOYMENT)) {
            if (taxes.length < 1) {
                throw new Exception("Canadian deployment/location with incorrect number of taxes: " + deployment);
            }

            // Tax exempt categories in all but BC region
            if (!deployment.equals(TNF_CANADA_1214) && !deployment.equals(TNF_CANADA_1262)) {
                if (deptClassIsCanadaTaxExemptCategory(itemDeptClass)) {
                    return new String[0];
                }
            }

            ArrayList<String> caTaxes = new ArrayList<String>();
            for (int i = 0; i < taxes.length; i++) {
                caTaxes.add(taxes[i].getId());
            }

            // No Youth Tax in Quebec
            if (deployment.equals(TNF_CANADA_1213) || deployment.equals(TNF_CANADA_1263)) {
                return caTaxes.toArray(new String[0]);
            } else {
                String validYouthTaxString = "";
                for (String t : TNF_CANADA_YOUTH_TAX_CATALOG_IDS) {
                    if (caTaxes.contains(t)) {
                        validYouthTaxString = t;
                    }
                }
                if (validYouthTaxString.length() < 1) {
                    throw new Exception("Canadian deployment/location not assigned youth tax: " + deployment);
                }

                if (isSpecialTaxCategoryCanadaYouth(itemVariation.getSku())) {
                    return new String[] { validYouthTaxString };
                } else {
                    caTaxes.removeAll(TNF_CANADA_YOUTH_TAX_CATALOG_IDS);
                    return caTaxes.toArray(new String[0]);
                }
            }
        } else if (taxes.length != 1) {
            //throw new Exception("Regular taxed deployment/location with incorrect number of taxes: " + deployment);
        }

        return new String[] { taxes[0].getId() };
    }

    public static boolean isSpecialTaxCategoryCanadaYouth(String sku) throws IOException {
        String itemNumber = ("00000000000000" + sku).substring(sku.length());

        HashSet<String> canadaYouthSkus = (HashSet<String>) getCanadaYouthSkus();
        if (canadaYouthSkus.contains(itemNumber)) {
            return true;
        } else {
            return false;
        }
    }

    public static Set<String> getCanadaYouthSkus() throws IOException {
        HashSet<String> skus = new HashSet<String>();

        InputStream iSKU = TaxRules.class.getResourceAsStream(TNF_CANADA_YOUTH_SKUS_PATH);
        BufferedReader brSKU = new BufferedReader(new InputStreamReader(iSKU, "UTF-8"));
        try {
            String line;
            while ((line = brSKU.readLine()) != null) {
                skus.add(line.trim());
            }
        } finally {
            brSKU.close();
        }

        return skus;
    }

    public static boolean deptClassIsCanadaTaxExemptCategory(String deptClass) {
        return TNFCA_TAX_EXEMPT_DEPT_CLASS.contains(deptClass);
    }

    public static boolean deptClassIsClothingTaxCategoryTnf(String deptClass) {
        return TNF_CLOTHING_DEPT_CLASS.contains(deptClass);
    }

    public static boolean deptClassIsClothingTaxCategoryVans(String deptClass) {
        return VANS_CLOTHING_DEPT_CLASS.contains(deptClass);
    }

    public static boolean deptClassIsSchoolSuppliesTaxCategoryTnf(String deptClass) {
        return TNF_SCHOOL_SUPPLIES_DEPT_CLASS.contains(deptClass);
    }

    public static boolean deptClassIsSchoolSuppliesTaxCategoryVans(String deptClass) {
        return VANS_SCHOOL_SUPPLIES_DEPT_CLASS.contains(deptClass);
    }

    private static boolean isTaxHolidayItemAL(String brand, int price, String deptClass) {
        if (!ENABLE_AL_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals("TNF")) {
            // Clothing $100 and less
            if (price <= AL_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }

            // School supplies $50 and less
            if (price <= AL_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price <= AL_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
            if (price <= AL_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemTN(String brand, int price, String deptClass) {
        if (!ENABLE_TN_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals("TNF")) {
            // Clothing $100 and less
            if (price <= TN_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }

            // School supplies $100 and less
            if (price <= TN_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price <= TN_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
            if (price <= TN_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemFL(String brand, int price, String deptClass) {
        if (!ENABLE_FL_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals("TNF")) {
            if (price < FL_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }
            if (price <= FL_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price < FL_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }

            if (price <= FL_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemOH(String brand, int price, String deptClass) {
        if (!ENABLE_OH_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals(BRAND_TNF)) {
            // Clothing less than or equal to $75
            if (price <= OH_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }

            // School supplies $20 or less
            if (price <= OH_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price <= OH_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
            if (price <= OH_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemOK(String brand, int price, String deptClass) {
        if (!ENABLE_OK_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals(BRAND_TNF)) {
            // Clothing less than $100
            if (price < OK_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price < OK_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemSC(String brand, int price, String deptClass) {
        if (!ENABLE_SC_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals(BRAND_TNF)) {
            // Clothing all
            if (deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }
            // School supplies all
            if (deptClassIsSchoolSuppliesTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
            if (deptClassIsSchoolSuppliesTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemIA(String brand, int price, String deptClass) {
        if (!ENABLE_IA_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals(BRAND_TNF)) {
            // Clothing all
            if (price <= IA_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price <= IA_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemVA(String brand, int price, String deptClass) {
        if (!ENABLE_VA_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals(BRAND_TNF)) {
            // Clothing <= $100
            if (price <= VA_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }

            // School supplies <= $20
            if (price <= VA_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price <= VA_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
            if (price <= VA_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemTX(String brand, int price, String deptClass) {
        if (!ENABLE_TX_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals(BRAND_TNF)) {
            // Clothing < $100
            if (price < TX_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }

            // School supplies < $100
            if (price < TX_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price < TX_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
            if (price < TX_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemMD(String brand, int price, String deptClass) {
        if (!ENABLE_MD_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals(BRAND_TNF)) {
            // Clothing <= $100
            if (price <= MD_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }

            // First $40 of backpack/bookbag -- not valid with Square
            if (price <= MD_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price <= MD_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
            if (price <= MD_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemCT(String brand, int price, String deptClass) {
        if (!ENABLE_CT_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals(BRAND_TNF)) {
            // Clothing and footwear <= $100
            if (price < CT_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price < CT_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemIL(String brand, int price, String deptClass) {
        if (!ENABLE_IL_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals(BRAND_TNF)) {
            // Clothing and footwear <= $100
            if (price < IL_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price < IL_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemMO(String brand, int price, String deptClass) {
        if (!ENABLE_MO_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals(BRAND_TNF)) {
            // Clothing $100 or less
            if (price <= MO_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }
            // School supplies $50 or less
            if (price <= MO_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price <= MO_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
            if (price <= MO_TAX_HOLIDAY_EXEMPT_THRESHOLD_SUPPLIES
                    && deptClassIsSchoolSuppliesTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemNM(String brand, int price, String deptClass) {
        if (!ENABLE_NM_TAX_HOLIDAY) {
            return false;
        }

        if (brand.equals(BRAND_TNF)) {
            // Clothing under $100
            if (price <= NM_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }
            // Backpacks $100 or less (backpacks are only values in supplies list)
            if (price <= NM_TAX_HOLIDAY_EXEMPT_THRESHOLD_BACKPACKS
                    && deptClassIsSchoolSuppliesTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price <= NM_TAX_HOLIDAY_EXEMPT_THRESHOLD_CLOTHING && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
            if (price <= NM_TAX_HOLIDAY_EXEMPT_THRESHOLD_BACKPACKS
                    && deptClassIsSchoolSuppliesTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTaxHolidayItemMA(int price) {
        if (!ENABLE_MA_TAX_HOLIDAY) {
            return false;
        }

        // All categories $2,500 or less
        if (price <= MA_TAX_HOLIDAY_EXEMPT_THRESHOLD) {
            return true;
        }

        return false;
    }

    private static boolean isSpecialTaxCategoryNYS(int price, String deptClass) {
        if (price < NY_EXEMPT_THRESHOLD && deptClassIsClothingTaxCategoryTnf(deptClass)) {
            return true;
        }
        return false;
    }

    private static boolean vansIsSpecialTaxCategoryNY(int price, String deptClass) {
        if (price < NY_EXEMPT_THRESHOLD && VANS_CLOTHING_DEPT_CLASS.contains(deptClass)) {
            return true;
        }
        return false;
    }

    // TODO(bhartard): MA clothing tax is actually rate*(N-$175)
    // Need to treat these differently
    private static boolean isSpecialTaxCategoryMA(String brand, int price, String deptClass) {

        if (brand.equals(BRAND_TNF)) {
            if (price <= MA_EXEMPT_THRESHOLD && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price <= MA_EXEMPT_THRESHOLD && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
        }

        if (isTaxHolidayItemMA(price)) {
            return true;
        }

        return false;
    }

    // TODO(bhartard): RI clothing tax is actually rate*(N-$250)
    // Need to treat these differently
    private static boolean isSpecialTaxCategoryRhodeIsland(String brand, int price, String deptClass) {
        if (brand.equals(BRAND_TNF)) {
            if (price <= RI_EXEMPT_THRESHOLD && deptClassIsClothingTaxCategoryTnf(deptClass)) {
                return true;
            }
        } else {
            if (price <= RI_EXEMPT_THRESHOLD && deptClassIsClothingTaxCategoryVans(deptClass)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSpecialTaxCategoryPA(String deptClass) {
        return deptClassIsClothingTaxCategoryTnf(deptClass);
    }

    private static boolean isSpecialTaxCategoryNJ(String deptClass) {
        return deptClassIsClothingTaxCategoryTnf(deptClass);
    }

    private static boolean isSpecialTaxCategoryMN(String deptClass) {
        return deptClassIsClothingTaxCategoryTnf(deptClass);
    }

    private static boolean isTaxedCategoryNauticaNJ(String deptClass) {
        return NAUTICA_NON_CLOTHING_DEPT_CLASS.contains(deptClass);
    }

    private static CatalogObject getLowerTax(CatalogObject tax1, CatalogObject tax2) {
        if (Float.parseFloat(tax1.getTaxData().getPercentage()) < Float.parseFloat(tax2.getTaxData().getPercentage())) {
            return tax1;
        }
        return tax2;
    }

    private static CatalogObject getHigherTax(CatalogObject tax1, CatalogObject tax2) {
        if (Float.parseFloat(tax1.getTaxData().getPercentage()) >= Float
                .parseFloat(tax2.getTaxData().getPercentage())) {
            return tax1;
        }
        return tax2;
    }
}
