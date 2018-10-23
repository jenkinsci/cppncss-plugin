package hudson.plugins.cppncss;

import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.Run;
import hudson.plugins.cppncss.parser.StatisticsResult;

import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.util.Collection;
import java.util.Collections;

/**
 * TODO javadoc.
 * 
 * @author Stephen Connolly
 * @since 08-Jan-2008 21:15:05
 */
public class CppNCSSBuildIndividualReport extends
		AbstractBuildReport<Run<?, ?>> implements Action, SimpleBuildStep.LastBuildAction {

	private HealthReport healthReport;

	private CppNcssBuildFunctionIndividualReport cppFunction;

	public CppNCSSBuildIndividualReport(StatisticsResult results,
			Integer functionCcnViolationThreshold,
			Integer functionNcssViolationThreshold) {
		super(results, functionCcnViolationThreshold,
				functionNcssViolationThreshold);
	}

	/**
	 * Write-once setter for property 'build'.
	 * 
	 * @param build
	 *            The value to set the build to.
	 */
	@Override
	public synchronized void setBuild(Run<?, ?> build) {
		super.setBuild(build);
		if (this.getBuild() != null) {
			getResults().setOwner(this.getBuild());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public HealthReport getBuildHealth() {
		return healthReport;
	}

	public void setBuildHealth(HealthReport healthReport) {
		this.healthReport = healthReport;
	}

	public AbstractBuildReport getDynamic(String name, StaplerRequest req,
			StaplerResponse rsp) {
		if (cppFunction == null) {
			cppFunction = new CppNcssBuildFunctionIndividualReport(
					getResults(), getFunctionCcnViolationThreshold(),
					getFunctionNcssViolationThreshold());
		}
		if (name.length() >= 1) {
			cppFunction.setFileName(name);
			cppFunction.setBuild(this.getBuild());
			cppFunction.setFilereport(this);
			return cppFunction;
		} else {
			return this;
		}
	}

	@Override
	public Collection<? extends Action> getProjectActions() {
		return Collections.singleton(
				new CppNCSSProjectIndividualReport(getBuild().getParent(),
						getFunctionCcnViolationThreshold(),
						getFunctionNcssViolationThreshold()));
	}
}
