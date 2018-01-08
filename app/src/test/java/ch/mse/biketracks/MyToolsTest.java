package ch.mse.biketracks;

import org.junit.Test;

import ch.mse.biketracks.utils.MyTools;

import static org.junit.Assert.assertEquals;

/**
 * Test MyTools class
 */
public class MyToolsTest {

    @Test
    public void formatTimeHHmmSS_01h59m59_isCorrect() throws Exception {
        String expected = "01h59m59";
        int elapsedSeconds = 59 + 59*60 + 3600;
        String result = MyTools.FormatTimeHHhmmss(elapsedSeconds);
        assertEquals(expected, result);
    }

    @Test
    public void formatTimeHHmmSS_01h59m59_plus_1_isCorrect() throws Exception {
        String expected = "02h00m00";
        int elapsedSeconds = 59 + 59*60 + 3600 + 1;
        String result = MyTools.FormatTimeHHhmmss(elapsedSeconds);
        assertEquals(expected, result);
    }
}
