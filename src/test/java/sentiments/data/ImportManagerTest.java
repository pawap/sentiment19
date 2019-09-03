package sentiments.data;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;


public class ImportManagerTest {

    @Test
    public void FilenameToDate_correctInput_correctOutput()

    {
        //Setup
        ImportManager importManager = new ImportManager();
        String filename = "tweet2019051314:36.json";
        LocalDateTime target = LocalDateTime.of(2019, 5, 13, 14, 36);

        //Test

        LocalDateTime dateTime = importManager.FilenameToDateTime(filename);

        //Assert
        assertEquals("date is not equal to the target date", target,  dateTime);
    }
}
