package hudson.plugins.cppncss;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import org.jenkinsci.Symbol;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.helpers.BuildProxy;
import hudson.plugins.helpers.Ghostwriter;
import hudson.plugins.helpers.health.HealthMetric;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import java.io.IOException;
import org.apache.commons.beanutils.ConvertUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 08-Jan-2008 21:24:06
 */
public class CppNCSSPublisher extends Recorder implements SimpleBuildStep {

    private String reportFilenamePattern;
    private Integer functionCcnViolationThreshold = 10;
    private Integer functionNcssViolationThreshold = 100;
    private CppNCSSHealthTarget[] targets;

    @DataBoundConstructor
    public CppNCSSPublisher(String reportFilenamePattern, Integer functionCcnViolationThreshold, Integer functionNcssViolationThreshold, CppNCSSHealthTarget[] targets) {
		reportFilenamePattern.getClass();
        this.reportFilenamePattern = reportFilenamePattern;
        this.functionCcnViolationThreshold = functionCcnViolationThreshold;
        this.functionNcssViolationThreshold = functionNcssViolationThreshold;
        
        this.targets = targets == null ? new CppNCSSHealthTarget[0] : targets;
    }

    public String getReportFilenamePattern() {
        return reportFilenamePattern;
    }

	public Integer getFunctionCcnViolationThreshold() {
		return functionCcnViolationThreshold;
	}

	public Integer getFunctionNcssViolationThreshold() {
		return functionNcssViolationThreshold;
	}

	// TODO: replace by lists
	@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Legacy code, suppressed due to the performance reasons")
	public CppNCSSHealthTarget[] getTargets() {
        return targets;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    private Ghostwriter newGhostwriter() {
        return new CppNCSSGhostwriter(reportFilenamePattern, functionCcnViolationThreshold, functionNcssViolationThreshold, targets);
    }

    @Override
    public void perform(Run<?,?> run, FilePath workspace, Launcher launcher, TaskListener listener) {
        try {
            BuildProxy.doPerform(newGhostwriter(), run, workspace, listener);
        } catch (IOException | InterruptedException e) {
            run.setResult(Result.FAILURE);
            e.printStackTrace(listener.getLogger());
        }
    }

    @Extension @Symbol("cppncss")
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return "Publish " + PluginImpl.DISPLAY_NAME;
        }

        static {
            ConvertUtils.register(CppNCSSHealthMetrics.CONVERTER, CppNCSSHealthMetrics.class);
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public HealthMetric[] getMetrics() {
            return CppNCSSHealthMetrics.values();
        }
    }

}
