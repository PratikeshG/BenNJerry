package vfcorp.smartwool;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;

import com.squareup.connect.v2.Location;

public class ReportBuilderTestUtils<T> {
    protected void verifyReport(HashMap<String, List<T>> report, T[][] connectModel, Location[] locations) {
    	for (int index = 0; index < locations.length; index++) {
    		Assert.isTrue(report.containsKey(locations[index].getId()));
    		List<T> connectObjs = report.get(locations[index].getId());
			Assert.isTrue(connectModel[index].length == connectObjs.size());
    	}
    }

	protected String readFileToString(String path) throws IOException {
		return IOUtils.toString(this.getClass().getResourceAsStream(path));
	}
}
