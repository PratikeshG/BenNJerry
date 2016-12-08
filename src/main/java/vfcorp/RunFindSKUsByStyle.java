package vfcorp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import vfcorp.rpc.ItemAdditionalDataRecord;

public class RunFindSKUsByStyle {
    private static final String ITEM_ADDITIONAL_DATA_RECORD = "36";
    private static final String PATH = "/Users/bhartard/desktop/styles/PLU00001.DTA";
    private static final String[] styleList = { "0A2VE20C5", "0A2VE2870", "0A2VE2JK3", "0A2VE2NXS", "0A2TCNHDC",
            "0A2TCNJK3", "0A2TCNSTJ", "00C761STJ", "0A2VDJCGW", "0A2VDJKER", "0A2VDJKFB", "0A2VDRCGW", "0A2VDRKER",
            "0A2VDRKLU", "0A2VDLKX7", "0A2VDLLKM", "0A2VDLRDX", "0A33M3JK3", "0A33M3FN4", "0A33M3QBV", "0A33M3QCX",
            "0A33M3DYY", "0A37O8JK3", "0A37O8HCA", "0A37O8QCX", "0A37O8NXG", "0A3709RPZ", "0A3709RPY", "0A3709RQB",
            "00CUS2JK3", "00CUS2SZJ", "00CUS2TKE", "0A2VCVFN4", "0A2VCVJK3", "0A2VCVNXD", "00CTL4ECT", "00CTL4NXD",
            "0A2VG1JK3", "0A2VG1SZF", "0A2VCGJK3", "0A2VCGNXD", "0A2VCOCGW", "0A2VCOQLB", "0A2VCCCGW", "0A2VCCQLD",
            "0A2VDYDYZ", "0A2VDYHKW", "0A2VDYNY0", "0A2VDYQBK", "00CH2TKY4", "00CH2TRUD", "00CH2TSFS", "0A2VBWCTE",
            "0A2VBWKX7", "0A2VBZFNU", "0A2VBZLMW", "00CG1BJK3", "00CG1BNXY", "00CXH8JK3", "00CXH8NYD", "00CG1EJK3",
            "00CG1ESJK", "0A2V3XHBT", "0A2V3XJK3", "0A2V3ZHBT", "0A2V3ZV3T", "0A2V41JK3", "0A2V3YJK3", "0A2V4SGFG",
            "0A2V4SRDF", "0A2V4STMN", "0A35SZJK3", "0A35SZNXG", "0A35T1JK3", "0A2VAATQV", "0A2VAATQZ", "0A2VABTQW",
            "0A2VABTQZ" };

    public static void main(String[] args) throws Exception {

        // -----------------------------------------------------------------
        // ------------- DO NOT EDIT BELOW THIS LINE -----------------------
        // -----------------------------------------------------------------

        System.out.println("Processing PLU...");

        HashMap<String, Integer> counts = new HashMap<String, Integer>();
        for (String s : styleList) {
            counts.put(s, 0);
        }

        File file = new File(PATH);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);

        // Parse file
        Scanner scanner = new Scanner(bis, "UTF-8");
        while (scanner.hasNextLine()) {
            String rpcLine = scanner.nextLine();

            if (rpcLine.length() < 2) {
                continue;
            } else {
                String recordType = rpcLine.substring(0, 2);

                if (recordType.equals(ITEM_ADDITIONAL_DATA_RECORD)) {
                    ItemAdditionalDataRecord dataRecord = new ItemAdditionalDataRecord(rpcLine);

                    String style = dataRecord.getValue("Additional Data").trim();
                    String foundStyle = styleInList(style);
                    if (foundStyle.length() > 0) {
                        updateCounts(counts, foundStyle);
                        System.out.println(String.format("%s", dataRecord.getValue("Item Number").trim()));
                    }
                }
            }
        }

        scanner.close();

        // Scanner suppresses exceptions
        if (scanner.ioException() != null) {
            throw scanner.ioException();
        }

        for (String key : counts.keySet()) {
            System.out.println(String.format("%s: %d", key, counts.get(key)));
        }

        bis.close();
        System.out.println("Done.");
    }

    private static void updateCounts(Map<String, Integer> counts, String key) {
        Integer c = counts.get(key);
        if (c != null) {
            counts.put(key, c + 1);
        } else {
            counts.put(key, 1);
        }
    }

    private static String styleInList(String style) {
        for (String s : styleList) {
            if (style.startsWith(s)) {
                return s;
            }
        }
        return "";
    }
}
