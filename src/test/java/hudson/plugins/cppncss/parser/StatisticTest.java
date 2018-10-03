package hudson.plugins.cppncss.parser;

import hudson.plugins.cppncss.parser.Statistic;
import junit.framework.TestCase;

import java.net.URL;
import java.io.File;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 25-Feb-2008 22:37:25
 */
public class StatisticTest extends TestCase {

    public StatisticTest(String name) {
        super(name);
    }

    public void testAntSmoke() throws Exception {
        File inputFile = new File(getClass().getResource("ant-cppncss-report.xml").getFile()).getAbsoluteFile();

        StatisticsResult r = Statistic.parse(inputFile, 0, 0);

        Statistic expected = new Statistic("");
        expected.setCcn(24);
        expected.setFunctions(9);
        expected.setNcss(129);
        expected.setNcssViolations(0);
        expected.setCcnViolations(0);
        expected.setMaxCcn(5);

        assertEquals(expected, Statistic.total(r.getFileResults()));
        

        expected.setNcss(59);
        
        assertEquals(expected, Statistic.total(r.getFunctionResults()));
    }

    public void testMaven2Smoke() throws Exception {
        File inputFile = new File(getClass().getResource("m2-cppncss-report.xml").getFile()).getAbsoluteFile();

        StatisticsResult r = Statistic.parse(inputFile, -1, -1);

        Statistic expected = new Statistic("");
        expected.setCcn(20);
        expected.setFunctions(9);
        expected.setNcss(129);
        expected.setNcssViolations(0);
        expected.setCcnViolations(0);
        expected.setMaxCcn(4);

        assertEquals(expected, Statistic.total(r.getFileResults()));
    }

    public void testMerge() throws Exception {
        File inputFile = new File(getClass().getResource("ant-cppncss-report.xml").getFile()).getAbsoluteFile();

        StatisticsResult r1 = Statistic.parse(inputFile, 0, 0);

        inputFile = new File(getClass().getResource("m2-cppncss-report.xml").getFile()).getAbsoluteFile();

        StatisticsResult r2 = Statistic.parse(inputFile, 0, 0);

        Statistic expected = new Statistic("");
        expected.setCcn(44);
        expected.setFunctions(18);
        expected.setNcss(258);
        expected.setNcssViolations(0);
        expected.setCcnViolations(0);
        expected.setMaxCcn(5);
        
        assertEquals(expected, Statistic.total(Statistic.merge(r1.getFileResults(), r2.getFileResults())));
    }
    
    private Statistic findMatching(Collection<Statistic> stats, Predicate<Statistic> p)
    {
        return stats.stream().filter(p).findFirst().get();        
    }
    
    public void testViolationCount() throws Exception {
        File inputFile = new File(getClass().getResource("ant-cppncss-report.xml").getFile()).getAbsoluteFile();

        StatisticsResult r = Statistic.parse(inputFile, 5, 3);
        
        
        assertEquals(3, Statistic.total(r.getFileResults()).getCcnViolations());
        assertEquals(6, Statistic.total(r.getFileResults()).getNcssViolations());
        assertEquals(3, Statistic.total(r.getFunctionResults()).getCcnViolations());
        assertEquals(6, Statistic.total(r.getFunctionResults()).getNcssViolations());
                
        Statistic allocFuncStats = findMatching(r.getFunctionResults(), s -> s.getName().matches( "statistics_alloc__r.*"));        
        assertEquals(1, allocFuncStats.getCcnViolations());
        
        Statistic fileStats = findMatching(r.getFileResults(), s -> s.getName().matches(".*statisticsobserver.c"));
        assertEquals(2, fileStats.getCcnViolations());
        assertEquals(5, fileStats.getNcssViolations());       
    }
}
