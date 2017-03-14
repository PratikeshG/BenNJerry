package tntfireworks;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import util.DbConnection;

public class DbConnectionTest {

    @Mock
    private DbConnection dbConnection;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMockDBConnection() throws Exception {
        String catsQuery = "SELECT * FROM CATS;";
        Mockito.when(dbConnection.executeQuery(catsQuery)).thenReturn(10);
        Mockito.verify(dbConnection, Mockito.times(0)).executeQuery(catsQuery);
        assertEquals(dbConnection.executeQuery(catsQuery), 10);
        Mockito.verify(dbConnection).executeQuery(catsQuery);
        Mockito.verify(dbConnection, Mockito.times(1)).executeQuery(catsQuery);
    }

}