package hudson.plugins.cppncss.parser;

import static org.junit.Assert.*;

import org.junit.Test;

public class FormattedStatisticSummaryTest {

	@Test
	public void getHtmlSummaryReportsCorrectDifferences() {
		FormattedStatisticSummary fss = new FormattedStatisticSummary(10, 11, 12, 7, 6, 14);
		assertEquals(fss.getHtmlSummary(), "<ul><li>ccn (-3)</li><li>functions (-5)</li><li>ncss (+2)</li></ul>");
	}

}
