package benjerrys;

/**
 * A CalculationReportRow object stores the data required for a single row
 * of the nine-column output CSV for the Calculation report.
 *
 * Calculation Report columns: Category, Items Sold, Gross Sales, Items
 * Refunded, Refunds, Reportable Sales (Gross - Refund), Discounts & Comps, Net
 * Sales, Taxes
 *
 * @author bhartard
 */
public class CalculationReportRow {
    private String cols[];

    public CalculationReportRow() {
        cols = new String[9];
    }

    public CalculationReportRow(RowBuilder builder) {
        cols = builder.cols;
    }

    public CalculationReportRow(CategoryData categoryData) {
        cols = new String[] { categoryData.getCategory(), Integer.toString(categoryData.getItemsSoldQty()),
                Util.centsToDollarsString(categoryData.getGrossSalesTotal()),
                Integer.toString(categoryData.getItemsRefundedQty()),
                Util.centsToDollarsString(categoryData.getRefundedTotal()),
                Util.centsToDollarsString(categoryData.getReportableSalesTotal()),
                Util.centsToDollarsString(categoryData.getDiscountsTotal()),
                Util.centsToDollarsString(categoryData.getNetSalesTotal()),
                Util.centsToDollarsString(categoryData.getTaxesTotal()) };
    }

    public static class RowBuilder {
        private String cols[];

        public RowBuilder() {
            cols = new String[9];
        }

        public RowBuilder setCol(int col, String colData) {
            if (col < 1 || col > 9) {
                return this;
            }

            // Input is 1-indexed
            this.cols[col - 1] = colData;
            return this;
        }

        public RowBuilder setColDollarValue(int col, int colData) {
            return setCol(col, Util.centsToDollarsString(colData));
        }

        public CalculationReportRow build() {
            return new CalculationReportRow(this);
        }
    }

    @Override
    public String toString() {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                Util.getOrDefaultEmpty(cols[0]), Util.getOrDefaultEmpty(cols[1]), Util.getOrDefaultEmpty(cols[2]),
                Util.getOrDefaultEmpty(cols[3]), Util.getOrDefaultEmpty(cols[4]), Util.getOrDefaultEmpty(cols[5]),
                Util.getOrDefaultEmpty(cols[6]), Util.getOrDefaultEmpty(cols[7]), Util.getOrDefaultEmpty(cols[8]));
    }
}
