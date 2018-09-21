package hudson.plugins.cppncss;

import hudson.AbortException;
import hudson.FilePath;
import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.cppncss.parser.Statistic;
import hudson.plugins.cppncss.parser.StatisticsResult;
import hudson.plugins.helpers.BuildProxy;
import hudson.plugins.helpers.Ghostwriter;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xmlpull.v1.XmlPullParserException;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 08-Jan-2008 23:16:52
 */
public class CppNCSSGhostwriter
        implements Ghostwriter,
        Ghostwriter.MasterGhostwriter,
        Ghostwriter.SlaveGhostwriter {

    private final String reportFilenamePattern;

    private final CppNCSSHealthTarget[] targets;

	private final Integer functionCcnViolationThreshold;

	private final Integer functionNcssViolationThreshold;

    public CppNCSSGhostwriter(String reportFilenamePattern, Integer functionCcnViolationThreshold, Integer functionNcssViolationThreshold, CppNCSSHealthTarget... targets) {
        this.reportFilenamePattern = reportFilenamePattern;
		this.functionCcnViolationThreshold = functionCcnViolationThreshold;
		this.functionNcssViolationThreshold = functionNcssViolationThreshold;
        this.targets = targets;
    }

    @Override
    public boolean performFromMaster(Run<?, ?> build, FilePath executionRoot, TaskListener listener)
            throws InterruptedException, IOException {
    	if (targets != null && targets.length > 0) {
	    	List<Action> actions = build.getActions();
	    	Result buildResult = build.getResult();
	    	if (buildResult == null) {
	    	    // The entire method need to be modified to support Pipeline
	    	    throw new AbortException("Cannot perform publisher for a running job. The plugin needs to be updated to support plugins like Any Build Step. File a JIRA ticket if you need that");
            }

	    	for (Action action : actions) {
				if(action instanceof CppNCSSBuildIndividualReport) {
					CppNCSSBuildIndividualReport cppncssAction = (CppNCSSBuildIndividualReport)action;
					cppncssAction.setBuild(build);
	                for (CppNCSSHealthTarget target : targets) {
	                    Result result = target.evaluateStability(cppncssAction);
	                    if(result.isWorseThan(buildResult)){
	                    	buildResult = result;
	                    }
	                }
				}
			}
	    	build.setResult(buildResult);
    	}
        return true;
    }

    @Override
    public boolean performFromSlave(BuildProxy build, TaskListener listener) throws InterruptedException, IOException {
        FilePath[] paths = build.getExecutionRootDir().list(reportFilenamePattern);
        StatisticsResult results = null;
        Set<String> parsedFiles = new HashSet<String>();
        for (FilePath path : paths) {
            final String pathStr = path.getRemote();
            if (!parsedFiles.contains(pathStr)) {
                parsedFiles.add(pathStr);
                try {
                    StatisticsResult result = Statistic.parse(new File(pathStr));
                    if (results == null) {
                        results = result;
                    } else {
                        results = StatisticsResult.merge(results, result);
                    }
                } catch (XmlPullParserException e) {
                    e.printStackTrace(listener.getLogger());
                }
            }
        }
        if (results != null) {
            CppNCSSBuildIndividualReport action = new CppNCSSBuildIndividualReport(results, functionCcnViolationThreshold, functionNcssViolationThreshold);
            
            if (targets != null && targets.length > 0) {
                HealthReport r = null;
                for (CppNCSSHealthTarget target : targets) {
                    r = HealthReport.min(r, target.evaluateHealth(action, PluginImpl.DISPLAY_NAME + ": "));
                }
                action.setBuildHealth(r);
            }
            build.getActions().add(action);
        }
        return true;
    }
}
