package util.reports;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CSVGenerator {
	StringBuilder output = null;
	List<String> headers = null;
	CSVPrinter printer = null;
	public CSVGenerator(String[] headers) throws IOException {
		this(Arrays.asList(headers));
	}
	public CSVGenerator(List<String> headers) throws IOException {
		this.output = new StringBuilder();
		this.printer = new CSVPrinter(output, CSVFormat.DEFAULT);
		printer.printRecord(headers);
	}
	public void addRecord(List<String> row) throws IOException {
		printer.printRecord(row);
	}
	public String toString() {
		try {
			printer.close();
			return output.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
