package hudson.plugins.cppncss;

import hudson.model.FreeStyleProject;
import hudson.model.FreeStyleBuild;
import hudson.model.Label;
import hudson.scm.NullSCM;
import hudson.slaves.DumbSlave;
import hudson.tasks.Shell;
import hudson.plugins.cppncss.CppNCSSPublisher;
import hudson.plugins.cppncss.parser.Statistic;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.SingleFileSCM;

public class CppNCSSPublisherTest extends HudsonTestCase {
   
    /**
     * Verify that it works on a master.
     */
    public void testOnMaster() throws Exception {
        FreeStyleProject project = createFreeStyleProject();

        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(2);
	
        files.add(new SingleFileSCM("cppncss-reports/first-report/first-report-cppncss.xml",
                                    getClass().getResource("cppncss-reports/first-report/first-report-cppncss.xml")));
        files.add(new SingleFileSCM("cppncss-reports/second-report/second-report-cppncss.xml",
                                    getClass().getResource("cppncss-reports/second-report/second-report-cppncss.xml")));
        
        project.setScm(new MultiFileSCM(files));
	
        project.getPublishersList().add(new CppNCSSPublisher("**/*-cppncss.xml", 3, 100,  null));
        FreeStyleBuild build1 = project.scheduleBuild2(0).get();
	
        FreeStyleBuild build2 = project.scheduleBuild2(0).get();
        assertLogContains("Parsing CppNCSS report file \"first-report-cppncss.xml\"", build2);
        assertLogContains("Parsing CppNCSS report file \"second-report-cppncss.xml\"", build2);
        assertBuildStatusSuccess(build2);
    }
    
    public void testOnMasterPreviousBuildFailedWithoutCPPNCSSReportButCurrentBuildShouldWork() throws Exception {
        FreeStyleProject project = createFreeStyleProject();

        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(2);
	
        files.add(new SingleFileSCM("cppncss-reports/first-report/first-report-cppncss.xml",
                                    getClass().getResource("cppncss-reports/first-report/first-report-cppncss.xml")));
        files.add(new SingleFileSCM("cppncss-reports/second-report/second-report-cppncss.xml",
                                    getClass().getResource("cppncss-reports/second-report/second-report-cppncss.xml")));
        
        project.setScm(new MultiFileSCM(files));
	
        CppNCSSHealthTarget targets[] = new CppNCSSHealthTarget[1];
        targets[0] = new CppNCSSHealthTarget(CppNCSSHealthMetrics.NUMBER_OF_CCN_VIOLATED_FUNCTION, "3.0", "3.0", "4.0", "6.0", "1.0");
        CppNCSSPublisher cppNCSSPublisher = new CppNCSSPublisher("**/*-cppncss.xml", 3, 100,  targets);
        project.getPublishersList().add(cppNCSSPublisher);
        
        //success build
        FreeStyleBuild build1 = project.scheduleBuild2(0).get();
        CppNCSSBuildIndividualReport action =(CppNCSSBuildIndividualReport) build1.getAction(AbstractBuildReport.class);
        
        Collection<Statistic> functionResults = action.getResults().getFunctionResults();
    	int ccnViolatedFunctions = 0;
    	
    	for (Statistic statistic : functionResults) {
			if(statistic.getCcn() > 3)
				ccnViolatedFunctions ++;
		}
    	
        assertEquals(5, ccnViolatedFunctions);
        
        //failed build and no result file
        project.setScm(new NullSCM());
        project.getSomeWorkspace().deleteRecursive();
        project.getBuildersList().add(new Shell("exit 1"));
        FreeStyleBuild build2 = project.scheduleBuild2(0).get();
        action =(CppNCSSBuildIndividualReport) build2.getAction(AbstractBuildReport.class);
        assertNull( action);
        
        //success again. should work
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().removeAll(Shell.class);
        FreeStyleBuild build3 = project.scheduleBuild2(0).get();
        
        action =(CppNCSSBuildIndividualReport) build3.getAction(AbstractBuildReport.class);
        functionResults = action.getResults().getFunctionResults();
    	ccnViolatedFunctions = 0;
    	
    	for (Statistic statistic : functionResults) {
			if(statistic.getCcn() > 3)
				ccnViolatedFunctions ++;
		}
    	
        assertEquals(5, ccnViolatedFunctions);

        assertLogContains("Parsing CppNCSS report file \"first-report-cppncss.xml\"", build3);
        assertLogContains("Parsing CppNCSS report file \"second-report-cppncss.xml\"", build3);
        assertBuildStatusSuccess(build3);

    }
    

    /**
     * Verify that it works on a slave.
     */
    public void testOnSlave() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        DumbSlave slave = createSlave(Label.get("cppncss-test-slave"));
        
        project.setAssignedLabel(slave.getSelfLabel());
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(2);
	
        files.add(new SingleFileSCM("cppncss-reports/first-report/first-report-cppncss.xml",
                                    getClass().getResource("cppncss-reports/first-report/first-report-cppncss.xml")));
        files.add(new SingleFileSCM("cppncss-reports/second-report/second-report-cppncss.xml",
                                    getClass().getResource("cppncss-reports/second-report/second-report-cppncss.xml")));
        
        project.setScm(new MultiFileSCM(files));
	
        project.getPublishersList().add(new CppNCSSPublisher("**/*-cppncss.xml", 3, 100, null));
        FreeStyleBuild build1 = project.scheduleBuild2(0).get();
	
        FreeStyleBuild build2 = project.scheduleBuild2(0).get();
        System.out.println(getLog(build2));
        assertBuildStatusSuccess(build2);
    }

}
