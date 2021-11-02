package vfcorp.smartwool;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.Assert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.connect.BusinessLocationsAdapter;
import com.squareup.connect.Merchant;
import com.squareup.connect.Payment;
import com.squareup.connect.PaymentsAdapter;
import com.squareup.connect.Refund;
import com.squareup.connect.RefundsAdapter;
import com.squareup.connect.SquareClient;
import com.squareup.connect.v2.Location;
import com.squareup.connect.v2.LocationsAdapter;
import com.squareup.connect.v2.SquareClientV2;

import util.reports.PaymentsReportBuilder;
import util.reports.RefundsReportBuilder;

public class ReportBuilderTests {
    private static final String merchantJsonPath = "/smartwool/merchant.json";
    private static final String locationsJsonPath = "/smartwool/locations.json";

    @Test
    public void testPaymentsReportBuilder() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        ReportBuilderTestUtils<Payment> reportTestUtils = new ReportBuilderTestUtils<Payment>();

        final String paymentComplete1 = "/smartwool/payment-1.json";
        final String paymentComplete2 = "/smartwool/payment-2.json";

        try {
            Merchant merchantResult = gson.fromJson(reportTestUtils.readFileToString(merchantJsonPath), Merchant.class);
            Payment[] payments1 = gson.fromJson(reportTestUtils.readFileToString(paymentComplete1), Payment[].class);
            Payment[] payments2 = gson.fromJson(reportTestUtils.readFileToString(paymentComplete2), Payment[].class);

            SquareClient client = Mockito.mock(SquareClient.class);
            BusinessLocationsAdapter businessLocationsAdapter = Mockito.mock(BusinessLocationsAdapter.class);
            PaymentsAdapter paymentsAdapter = Mockito.mock(PaymentsAdapter.class);

            Mockito.when(client.payments()).thenReturn(paymentsAdapter);
            Mockito.when(client.payments().list()).thenReturn(payments1, payments2);

            Location[] locations = gson.fromJson(reportTestUtils.readFileToString(locationsJsonPath), Location[].class);
            Assert.isTrue(locations.length == 2);

            SquareClientV2 clientV2 = Mockito.mock(SquareClientV2.class);
            LocationsAdapter locationAdapter = Mockito.mock(LocationsAdapter.class);

            Mockito.when(clientV2.locations()).thenReturn(locationAdapter);
            Mockito.when(clientV2.locations().list()).thenReturn(locations);

            HashMap<String, List<Payment>> report = new PaymentsReportBuilder("connect.squareupstaging.com", "test",
                    "test", client).forLocations(Arrays.asList(locations)).build();

            reportTestUtils.verifyReport(report, new Payment[][] { payments1, payments2 }, locations);

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testRefundsReportBuilder() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        ReportBuilderTestUtils<Refund> reportTestUtils = new ReportBuilderTestUtils<Refund>();

        final String refundComplete1 = "/smartwool/refund-1.json";
        final String refundComplete2 = "/smartwool/refund-2.json";

        try {
            Merchant merchantResult = gson.fromJson(reportTestUtils.readFileToString(merchantJsonPath), Merchant.class);
            Refund[] refund1 = gson.fromJson(reportTestUtils.readFileToString(refundComplete1), Refund[].class);
            Refund[] refund2 = gson.fromJson(reportTestUtils.readFileToString(refundComplete2), Refund[].class);

            SquareClient client = Mockito.mock(SquareClient.class);
            BusinessLocationsAdapter businessLocationsAdapter = Mockito.mock(BusinessLocationsAdapter.class);
            RefundsAdapter refundsAdapter = Mockito.mock(RefundsAdapter.class);

            Mockito.when(client.refunds()).thenReturn(refundsAdapter);
            Mockito.when(client.refunds().list()).thenReturn(refund1, refund2);

            Location[] locations = gson.fromJson(reportTestUtils.readFileToString(locationsJsonPath), Location[].class);
            Assert.isTrue(locations.length == 2);

            SquareClientV2 clientV2 = Mockito.mock(SquareClientV2.class);
            LocationsAdapter locationAdapter = Mockito.mock(LocationsAdapter.class);

            Mockito.when(clientV2.locations()).thenReturn(locationAdapter);
            Mockito.when(clientV2.locations().list()).thenReturn(locations);

            HashMap<String, List<Refund>> report = new RefundsReportBuilder("connect.squareupstaging.com", "test",
                    "test", client).forLocations(Arrays.asList(locations)).build();

            reportTestUtils.verifyReport(report, new Refund[][] { refund1, refund2 }, locations);

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
