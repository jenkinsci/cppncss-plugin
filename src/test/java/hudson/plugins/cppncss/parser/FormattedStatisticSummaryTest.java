package hudson.plugins.cppncss.parser;

import static org.junit.Assert.*;

import org.junit.Test;

public class FormattedStatisticSummaryTest {

    private final String expectedSummary = "<ul><li>CCN: 7 (-3)</li><li>Functions: 6 (-5)</li><li>NCSS: 14 (+2)</li>"
            + "<li>CCN Violations: 1 (-1)</li><li>NCSS Violations: 3 (+1)</li><li>Max CCN: 4 (+2)</li></ul>";
    @Test
    public void getHtmlSummaryReportsCorrectAbsoluteAndDifferences() {
        FormattedStatisticSummary fss = new FormattedStatisticSummary(10, 11, 12, 2, 2, 2, 7, 6, 14, 1, 3, 4);
        assertEquals(expectedSummary, fss.getHtmlSummary());
    }

    @Test
    public void getHtmlSummaryShowsNoChangeText() {
        FormattedStatisticSummary fss = new FormattedStatisticSummary(10, 11, 12, 0, 0, 4, 10, 11, 12, 0, 0, 4);
        assertEquals(
                "<ul><li>CCN: 10 (No change)</li><li>Functions: 11 (No change)</li><li>NCSS: 12 (No change)</li>"
                + "<li>CCN Violations: 0 (No change)</li><li>NCSS Violations: 0 (No change)</li><li>Max CCN: 4 (No change)</li></ul>",
                fss.getHtmlSummary());
    }
    
    @Test
    public void testConstructorForTwoStatistics() {
        Statistic was = new Statistic("");
        was.setCcn(10);
        was.setFunctions(11);
        was.setNcss(12);
        was.setCcnViolations(2);
        was.setNcssViolations(2);
        was.setMaxCcn(2);
        
        Statistic now = new Statistic("");
        now.setCcn(7);
        now.setFunctions(6);
        now.setNcss(14);
        now.setCcnViolations(1);
        now.setNcssViolations(3);
        now.setMaxCcn(4);
        
        FormattedStatisticSummary fss = new FormattedStatisticSummary(was, now);
                
        assertEquals(expectedSummary, fss.getHtmlSummary());
    }
    
}
