package vfcorp;

import static org.junit.Assert.fail;

import org.junit.Test;

import vfcorp.tlog.AuthorizationCode;

public class RecordTest {

	@Test
	public void put_PutNullValue_ExceptionIsntThrown() throws Exception {
		try {
			AuthorizationCode authorizationCode = new AuthorizationCode().parse(null);
			authorizationCode.toString();
		} catch (NullPointerException e) {
			fail("not expecting exception");
		}
	}

}
