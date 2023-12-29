package scripts;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//pratikesh
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.connect.Payment;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Order;
import com.squareup.connect.v2.PaymentRefund;
import com.squareup.connect.v2.SquareClientV2;
import com.squareup.connect.v2.Tender;

import util.ConnectV2MigrationHelper;
import util.SquarePayload;

public class PullAllOrders {
    private final static String ENCRYPTED_ACCESS_TOKEN = System.getenv("SCRIPT_ENCRYPTED_ACCESS_TOKEN");
    private final static String ENCRYPTION_KEY = System.getenv("SCRIPT_ENCRYPTION_KEY");
    private final static String LOCATION_ID = "REPLACE_ME";

    private final static String API_URL = "https://connect.squareup.com";

    public static void main(String[] args) throws Exception {
        SquarePayload account = new SquarePayload();
        account.setEncryptedAccessToken(ENCRYPTED_ACCESS_TOKEN);

        SquareClientV2 client = new SquareClientV2(API_URL, account.getAccessToken(ENCRYPTION_KEY), "2023-03-15");
        //Writer out = new OutputStreamWriter(new FileOutputStream(OUTPUT_PATH), StandardCharsets.ISO_8859_1);

        Map<String, String> params = new HashMap<>();
        params.put("end_time", "2023-07-04T00:00:00");
        params.put("begin_time", "2022-07-04T00:00:00");

        Order[] orders = ConnectV2MigrationHelper.getOrders(client, LOCATION_ID, params);
        Set<String> v2TenderIds = new HashSet<>();
        for (Order order : orders) {
            if (order.getTenders() != null) {
                for (Tender tender : order.getTenders()) {
                    v2TenderIds.add(tender.getId());
                }
            }
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(orders);

        try (FileOutputStream fileOutputStream = new FileOutputStream("validr5ytdoutput.txt");
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(json);

            System.out.println("Objects written to file successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        SquareClient v1Client = new SquareClient(account.getAccessToken(ENCRYPTION_KEY), API_URL);
        v1Client.setLocation(LOCATION_ID);
        Map<String, String> customMap = new HashMap<>();
        customMap.put("begin_time", "2022-07-04T00:00:00Z");
        customMap.put("end_time", "2023-07-04T00:00:00Z");
        customMap.put("order", "DESC");
        customMap.put("limit", "200");
        Payment[] payments = v1Client.payments().list(customMap);

        String paymentJson = gson.toJson(payments);
        params.put("location_id", LOCATION_ID);
        com.squareup.connect.v2.Payment[] v2payments = client.payments().list(params);
        Map<String, List<PaymentRefund>> orderRefunds = ConnectV2MigrationHelper.getRefundsForOrders(client, orders,
                v2payments);
        for (Order order : orders) {
            List<PaymentRefund> refunds = orderRefunds.getOrDefault(order.getId(), Collections.EMPTY_LIST);
            Map<String, List<PaymentRefund>> tenderToPaymentRefund = getPaymentIdToRefunds(refunds);
            Map<String, Integer> tenderToAmount = getTenderToRefundAmount(order, tenderToPaymentRefund);
            if (!tenderToPaymentRefund.isEmpty())
                System.out.println(order.getId());
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream("v1paymentytdoutput.txt");
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(paymentJson);

            System.out.println("Objects written to file successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("DONE");
    }

    private static Map<String, List<PaymentRefund>> getPaymentIdToRefunds(List<PaymentRefund> refunds) {
        Map<String, List<PaymentRefund>> paymentIdToRefunds = new HashMap<>();
        if (refunds != null) {
            for (PaymentRefund refund : refunds) {
                if (!paymentIdToRefunds.containsKey(refund.getPaymentId())) {
                    paymentIdToRefunds.put(refund.getPaymentId(), new ArrayList<>());
                }
                paymentIdToRefunds.get(refund.getPaymentId()).add(refund);
            }
        }
        return paymentIdToRefunds;
    }

    private static Map<String, Integer> getTenderToRefundAmount(Order order,
            Map<String, List<PaymentRefund>> refundsMap) {
        Map<String, Integer> tenderToRefundAmount = new HashMap<String, Integer>();
        if (!refundsMap.isEmpty()) {
            for (Tender tender : order.getTenders()) {
                List<PaymentRefund> refunds = refundsMap.get(tender.getId());
                if (areValidRefunds(refunds)) {
                    int total = 0;
                    for (PaymentRefund refund : refunds) {
                        total += refund.getAmountMoney().getAmount();
                    }
                    tenderToRefundAmount.put(tender.getId(), total);
                }
            }
        }

        return tenderToRefundAmount;
    }

    private static boolean areValidRefunds(List<PaymentRefund> refunds) {
        for (PaymentRefund refund : refunds) {
            if (refund == null || refund.getAmountMoney() == null || refund.getAmountMoney().getAmount() < 0) {
                return false;
            }
        }
        return true;
    }

}
