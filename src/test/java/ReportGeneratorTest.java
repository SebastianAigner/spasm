import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.File;
import org.junit.Assert;
/**
 * Created by Sebastian Aigner.
 */
public class ReportGeneratorTest {

    private ReportGenerator reportGenerator = new ReportGenerator("https://www.twitch.tv/esl_csgo/v/59482126");
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void completeReportGenerationTest() throws Exception {
        String file = "";
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File("").getAbsolutePath().concat("/src/test/data/test_report.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Scanner s = new Scanner(fis);
        while (s.hasNextLine()) {
            file += s.nextLine();
        }
        reportGenerator.doInBackground();
        String result = reportGenerator.getResult();
        Assert.assertEquals("Actual data format generated from a real life example does not match demo report", file, result);
    }

    @After
    public void tearDown() throws Exception {

    }
}