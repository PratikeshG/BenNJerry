package benjerrys;

/**
 * A SummaryReportRow object stores the data required for a single row
 * of the three-column output CSV for the Summary report.
 *
 * Summary Report Columns: Category, Totals
 *
 * @author bhartard
 */
public class SummaryReportRow {
    private String cols[];

    public SummaryReportRow() {
        cols = new String[3];
    }

    public SummaryReportRow(RowBuilder builder) {
        cols = builder.cols;
    }

    public SummaryReportRow(CategoryData categoryData) {
        cols = new String[] { categoryData.getCategory(),
                Util.centsToDollarsString(categoryData.getReportableSalesTotal()), "" };
    }

    public static class RowBuilder {
        private String cols[];

        public RowBuilder() {
            cols = new String[3];
        }

        public RowBuilder(CategoryData categoryData) {
            this();
            setCol(1, categoryData.getCategory());
            setCol(2, Util.centsToDollarsString(categoryData.getReportableSalesTotal()));
        }

        public RowBuilder setCol(int col, String data) {
            if (col < 1 || col > 3) {
                return this;
            }

            this.cols[col - 1] = data;
            return this;
        }

        public RowBuilder setColDollarValue(int col, int data) {
            return setCol(col, Util.centsToDollarsString(data));
        }

        public SummaryReportRow build() {
            return new SummaryReportRow(this);
        }
    }

    @Override
    public String toString() {
        return String.format("\"%s\",\"%s\",\"%s\"\n", Util.getOrDefaultEmpty(cols[0]), Util.getOrDefaultEmpty(cols[1]),
                Util.getOrDefaultEmpty(cols[2]));
    }
}
