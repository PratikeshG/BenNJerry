package util.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CSVGenerator {
	private List<String> headers = null;
	private ArrayList<List<String>> rows = new ArrayList<List<String>>();
	public CSVGenerator(String[] headers) throws IOException {
		this(Arrays.asList(headers));
	}
	public CSVGenerator(List<String> headers) throws IOException {
		this.headers = headers;
	}
	public void addRecord(List<String> row) throws IOException {
		this.rows.add(row);
	}
	public String build() throws IOException {
		StringBuilder output = new StringBuilder();
		CSVPrinter printer = new CSVPrinter(output, CSVFormat.DEFAULT);
		printer.printRecord(this.headers);
		for (List<String> row : this.rows) {
			printer.printRecord(row);
		}
		printer.close();
		return output.toString();
	}
}
