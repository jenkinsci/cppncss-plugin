package hudson.plugins.cppncss.parser;

import java.io.Serializable;

public class StatisticsTotalResult  implements Serializable {
	private Statistic functionTotal;
	private Statistic fileTotal;
	
	public void setFunctionTotal(Statistic functionTotal) {
		this.functionTotal = functionTotal;
	}
	public Statistic getFunctionTotal() {
		return functionTotal;
	}
	public void setFileTotal(Statistic fileTotal) {
		this.fileTotal = fileTotal;
	}
	public Statistic getFileTotal() {
		return fileTotal;
	}

	/**
	 * @deprecated Use {@link #getStatisticSummary(StatisticsTotalResult)}
	 */
	@Deprecated
	public String toSummary(StatisticsTotalResult statisticsTotalResult) {
		return fileTotal.toSummary(statisticsTotalResult.fileTotal);
	}

	/**
	 * @deprecated Use {@link #getStatisticSummary()}
	 */
	@Deprecated
	public String toSummary() {
		return fileTotal.toSummary();
	}

	/**
	 * @since TODO
	 */
	public StatisticSummary getStatisticSummary(StatisticsTotalResult statisticsTotalResult) {
		return fileTotal.getStatisticSummary(statisticsTotalResult.fileTotal);
	}

	/**
	 * @since TODO
	 */
	public StatisticSummary getStatisticSummary() {
		return fileTotal.getStatisticSummary();
	}
	
	public void set(StatisticsTotalResult that) {
		this.functionTotal = that.functionTotal;
		this.fileTotal = that.fileTotal;
		
	}
}
