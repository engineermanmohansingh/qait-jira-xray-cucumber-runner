import static org.testng.Assert.*;
import org.testng.annotations.*;
import org.testng.Reporter;

import com.qainfotech.ta.xrayrunner.XRayApiClient;
//import com.qainfotech.ta.testrailrunner.TestDataHandler;
//import com.qainfotech.ta.testrailrunner.GoogleSheetHandler;
//import com.qainfotech.ta.testrailrunner.googlesheetreader.GoogleSheet;

import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author Ramandeep
 */
public class InitiateTestRunnerTest {
    
//    @Test
    public void firstTest() throws Exception{
        XRayApiClient xc = new XRayApiClient();
        xc.createFeatureFiles();
        xc.pushResult();
    }
    
//    @Test
    public void pushResults() throws Exception{
//        TestRailApiClient tr = new TestRailApiClient();
//        tr.pushResult(24, 113);
    }
    
//    @Test
    public void testTheTestDataHandler() throws Exception{
//        Map config = new HashMap();
//        GoogleSheet sheet = new GoogleSheet("1s1UmHmba8D7AzrGrM0TkZ3_58JLGKWV7PVjff6372zA");
//        System.out.println(sheet.exampleTable("Develop TestRail Client for Automation Result Push", "Second Test Case"));
//        sheet.worksheet("TestSuite1");
//        System.out.println(sheet.worksheet("TestSuite1").content);
    }
}

