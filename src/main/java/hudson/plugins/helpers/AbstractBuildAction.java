package hudson.plugins.helpers;

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.HealthReportingAction;
import hudson.model.Run;
import hudson.plugins.cppncss.parser.StatisticSummary;
import hudson.plugins.cppncss.parser.StringStatisticSummary;
import jenkins.model.RunAction2;

import java.io.Serializable;

/**
 * An action that is associated with a build.
 *
 * @author Stephen Connolly
 * @param <BUILD> the build class that the action is associated with.
 * @since 04-Feb-2008 19:41:25
 */
public abstract class AbstractBuildAction<BUILD extends AbstractBuild<?, ?>> 
	implements HealthReportingAction, Serializable, RunAction2
{
    /** Unique identifier for this class. */
    private static final long serialVersionUID = 31415926L;

    /**
     * The owner of this Action.  Ideally I'd like this to be final and set in the constructor, but Maven does not
     * let us do that, so we need a setter.
     */
    private transient BUILD build = null;

    /**
     * Constructs a new AbstractBuildAction.
     */
    protected AbstractBuildAction() {
    }

    /**
     * Getter for property 'build'.
     *
     * @return Value for property 'build'.
     */
    public synchronized BUILD getBuild() {
        return build;
    }

    /**
     * Write once setter for property 'build'.
     *
     * @param build Value to set for property 'build'.
     */
    public synchronized void setBuild(BUILD build) {
        // Ideally I'd prefer to use and AtomicReference... but I'm unsure how it would work with the serialization fun
        if (this.build == null && this.build != build) {
            this.build = build;
        }
    }

    private BUILD runToBuild(Run<?, ?> run) throws IllegalStateException {
        final BUILD b;
        try {
            b = (BUILD)run;
        } catch (ClassCastException ex) {
            throw new IllegalStateException("Action is attached to a wrong job type for run " + run.getFullDisplayName(), ex);
        }
        return b;
    }

    @Override
    public final void onAttached(Run<?, ?> r) {
        setBuild(runToBuild(r));
    }

    @Override
    public final void onLoad(Run<?, ?> r) {
        setBuild(runToBuild(r));
    }

    /**
     * Override to control when the floating box should be displayed.
     *
     * @return <code>true</code> if the floating box should be visible.
     */
    public boolean isFloatingBoxActive() {
        return true;
    }

    /**
     * Override to control when the action displays a trend graph.
     *
     * @return <code>true</code> if the action should show a trend graph.
     */
    public boolean isGraphActive() {
        return false;
    }

    /**
     * Override to define the graph name.
     *
     * @return The graph name.
     */
    public String getGraphName() {
        return getDisplayName();
    }

    /**
     * Override to control the build summary detail.
     *
     * @return the summary string for the main build page.
     * @deprecated Use {@link #getStatisticSummary()}
     */
    @Deprecated
    public String getSummary() {
        if (Util.isOverridden(AbstractBuildAction.class, this.getClass(), "getStatisticSummary")) {
            return getStatisticSummary().getHtmlSummary();
        }
        return "";
    }

    /**
     * @since TODO
     */
    public StatisticSummary getStatisticSummary() {
        // default to the obsolete method
        return new StringStatisticSummary(getSummary());
    }
}
