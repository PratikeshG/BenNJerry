package benjerrys;

import java.text.NumberFormat;
import java.util.Locale;

public class Util {
    public static String centsToDollarsString(int cents) {
        // Why Canada? Because it displays negative currencies as "-$XX.YY".
        // US displays negative currencies as "($XX.YY)".
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.CANADA);
        return n.format(cents / 100.0);
    }

    public static String getOrDefaultEmpty(String input) {
        return (input != null) ? input : "";
    }
}
