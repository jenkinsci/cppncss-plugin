package hudson.plugins.cppncss;

import java.util.Scanner;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import hudson.model.Label;

public class CppNCSSPublisherPipelineTest{

    @Rule public JenkinsRule jenkins = new JenkinsRule();
    
    private String groovyString(String s) {
    	return "'" + s.replaceAll("\\\\", "\\\\\\\\").replaceAll("\r\n", "\\\\n").replaceAll("'", "\\\\'") + "'";
    }
    
    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        
        Scanner s = new Scanner(getClass().getResourceAsStream("cppncss-reports/first-report/first-report-cppncss.xml"), "UTF-8");
        String file1Text = s.useDelimiter("\\A").next();
        s.close();
        s = new Scanner(getClass().getResourceAsStream("cppncss-reports/second-report/second-report-cppncss.xml"), "UTF-8");
        String file2Text = s.useDelimiter("\\A").next();
        s.close();
                
        String pipelineScript
                = "node {\n"
                + "writeFile text: " + groovyString(file1Text) + ", file: 'folder/first-report-cppncss.xml'\n"
                + "writeFile text: " + groovyString(file2Text) + ", file: 'folder/second-report-cppncss.xml'\n"
                + "cppncss functionCcnViolationThreshold: 15, functionNcssViolationThreshold: 16, reportFilenamePattern: '**/*-cppncss.xml', targets: []\n"
                + "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkins.assertLogContains("Parsing CppNCSS report file \"first-report-cppncss.xml\"", completedBuild);
        jenkins.assertLogContains("Parsing CppNCSS report file \"second-report-cppncss.xml\"", completedBuild); 
    }

}
