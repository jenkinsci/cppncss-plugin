/**
 * 
 */
package hudson.plugins.cppncss;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import hudson.plugins.cppncss.parser.StatisticsResult;
import hudson.plugins.cppncss.parser.StatisticsTotalResult;
import hudson.plugins.helpers.AbstractProjectAction;

/**
 * @author zjianguo
 * 
 */
public class CppNCSSProjectFunctionIndividualReport extends
		CppNCSSProjectIndividualReport implements ProminentProjectAction {
    
    private final StatisticsResult results;

	public CppNCSSProjectFunctionIndividualReport(StatisticsResult results, AbstractProject<?, ?> project,
			Integer functionCcnViolationThreshold,
			Integer functionNcssViolationThreshold) {
		super(project, functionCcnViolationThreshold,
				functionNcssViolationThreshold);
		this.results = results;
	}

	private String fileName;
	private AbstractProjectAction filereport;

	public AbstractProjectAction getFilereport() {
		return filereport;
	}

	public void setFilereport(AbstractProjectAction filereport) {
		this.filereport = filereport;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public AbstractProjectAction getDynamic(String name, StaplerRequest req,
			StaplerResponse rsp) {
		if (name.length() < 1) {
			return this.filereport;
		} else {
			return this;
		}
	}

	@Override
	public String getDisplayName() {
		return fileName;
	}
	
	@Override
    public StatisticsResult getResults() {
        return results;
    }

    @Override
    public StatisticsTotalResult getTotals() {
        return StatisticsResult.total(results);
    }
    
    @Override
    public boolean isGraphActive() {
        //We don't want to show the graph for a single file, since the graph contains only totals 
        return false;
    }
    
    @Override
    public boolean isSummaryActive() {
        return true;
    }
}
