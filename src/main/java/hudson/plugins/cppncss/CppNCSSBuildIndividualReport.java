package hudson.plugins.cppncss;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.plugins.cppncss.parser.Statistic;
import hudson.plugins.cppncss.parser.StatisticsResult;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * TODO javadoc.
 * 
 * @author Stephen Connolly
 * @since 08-Jan-2008 21:15:05
 */
public class CppNCSSBuildIndividualReport extends
		AbstractBuildReport<AbstractBuild<?, ?>> implements Action {

	private HealthReport healthReport;

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
	public synchronized void setBuild(AbstractBuild<?, ?> build) {
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
	    CppNcssBuildFunctionIndividualReport cppFunction = new CppNcssBuildFunctionIndividualReport(
					getResults().singleFileResult(name), getFunctionCcnViolationThreshold(),
					getFunctionNcssViolationThreshold());
		if (name.length() >= 1) {
			cppFunction.setFileName(name);
			cppFunction.setBuild(this.getBuild());
			cppFunction.setFilereport(this);
			return cppFunction;
		} else {
			return this;
		}
	}
}
