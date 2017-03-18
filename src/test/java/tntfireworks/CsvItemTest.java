package tntfireworks;

import com.squareup.connect.v2.Money;

import junit.framework.Assert;
import junit.framework.TestCase;

public class CsvItemTest extends TestCase {

    public void testPositiveMoney() {
        int cents = 100;
        String centsCur = "USD";
        String dollarsStr = "1.00";
        String dollarsCur = "USD";

        testMoney(cents, dollarsStr, dollarsCur, centsCur);
    }

    public void testWithDollarSign() {
        int cents = 100;
        String centsCur = "USD";
        String dollarsStr = "$1.00";
        String dollarsCur = "USD";

        try {
            testMoney(cents, dollarsStr, dollarsCur, centsCur);
            fail();
        } catch (NumberFormatException e) {
            Assert.assertTrue(true);
        }
    }

    private void testMoney(int cents, String dollarsStr, String centsCur, String dollarsCur) {
        CsvItem csvItem = new CsvItem();
        csvItem.setSuggestedPrice(dollarsStr);
        csvItem.setCurrency(dollarsCur);

        Money moneyFromStr = new Money(csvItem.getPriceAsSquareMoney());

        Money moneyFromCents = new Money(cents, centsCur);

        Assert.assertTrue(moneyFromStr.equals(moneyFromCents));
    }

    public void testMoreMoney() {
        testMoney(200, "2.00", "USD", "USD");
    }

    public void testSmallMoney() {
        testMoney(2, "0.02", "USD", "USD");
    }

    public void testNoLeadingZero() {
        testMoney(2, ".02", "USD", "USD");
    }

    public void testBigMoney() {
        testMoney(10000, "100.00", "USD", "USD");
    }

    public void testThrowsExceptionWithNegative() {
        try {
            testMoney(2, "-0.02", "USD", "USD");
            fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    public void testOneDecimal() {
        testMoney(900, "9.", "USD", "USD");

    }

    public void testEqualsNoDecimal() {
        int cents = 900;
        String centsCur = "USD";
        String dollarsStr = "9";
        String dollarsCur = "USD";

        CsvItem csvItem = new CsvItem();
        csvItem.setSuggestedPrice(dollarsStr);
        csvItem.setCurrency(dollarsCur);

        Money moneyFromStr = new Money(csvItem.getPriceAsSquareMoney());

        Money moneyFromCents = new Money(cents, centsCur);

        Assert.assertTrue(moneyFromStr.equals(moneyFromCents));

    }

    public void testSmallDecimal() {
        int cents = 900;
        String centsCur = "USD";
        String dollarsStr = "9.0";
        String dollarsCur = "USD";

        CsvItem csvItem = new CsvItem();
        csvItem.setSuggestedPrice(dollarsStr);
        csvItem.setCurrency(dollarsCur);

        Money moneyFromStr = new Money(csvItem.getPriceAsSquareMoney());

        Money moneyFromCents = new Money(cents, centsCur);

        Assert.assertTrue(moneyFromStr.equals(moneyFromCents));

    }

    public void testCurrencyMismatch() {
        int cents = 900;
        String centsCur = "USD";
        String dollarsStr = "9.00";
        String dollarsCur = "EUR";

        CsvItem csvItem = new CsvItem();
        csvItem.setSuggestedPrice(dollarsStr);
        csvItem.setCurrency(dollarsCur);

        Money moneyFromStr = new Money(csvItem.getPriceAsSquareMoney());

        Money moneyFromCents = new Money(cents, centsCur);

        Assert.assertFalse(moneyFromStr.equals(moneyFromCents));

    }

    public void testNonNumeric() {
        int cents = 900;
        String centsCur = "USD";
        String dollarsStr = "abc";
        String dollarsCur = "USD";

        CsvItem csvItem = new CsvItem();
        csvItem.setSuggestedPrice(dollarsStr);
        csvItem.setCurrency(dollarsCur);

        try {
            Money moneyFromStr = new Money(csvItem.getPriceAsSquareMoney());
            fail();
        } catch (IllegalArgumentException e) {

        }

    }

}
