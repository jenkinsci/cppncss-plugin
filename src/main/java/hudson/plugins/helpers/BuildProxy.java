package hudson.plugins.helpers;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Proxy for the key build information.
 *
 * @author Stephen Connolly
 * @since 12-Jan-2008 12:08:32
 */
public final class BuildProxy implements Serializable {
// ------------------------------ FIELDS ------------------------------

    private final FilePath artifactsDir;
    private final FilePath projectRootDir;
    private final FilePath buildRootDir;
    private final FilePath executionRootDir;
    // It should not be serialized over the channel (JENKINS-49237)
    private final Calendar timestamp;
    // TODO: it should not be serialized over the channel. It should exist only on the master side
    private final List<AbstractBuildAction<Run<?, ?>>> actions =
            new ArrayList<AbstractBuildAction<Run<?, ?>>>();
    //TODO: This class should not be serialized as well?
    private Result result = null;
    private boolean continueBuild = true;

// -------------------------- STATIC METHODS --------------------------


	/**
     * (Call from master) Invokes the ghostwriter on the master and slave nodes for this build.
     *
     * @param ghostwriter The ghostwriter that will be doing the work for the publisher.
     * @param build       The build.
     * @param listener    The build listener.
     * @return {@code true} if the build can continue.
     * @throws IOException          on IOException.
     * @throws InterruptedException on InterruptedException.
     */
    public static boolean doPerform(Ghostwriter ghostwriter,
                                    AbstractBuild<?, ?> build,
                                    BuildListener listener)
            throws IOException, InterruptedException {
    	return doPerform(ghostwriter, build, build.getWorkspace(), listener);
    }

	/**
     * (Call from master) Invokes the ghostwriter on the master and slave nodes for this build.
     *
     * @param ghostwriter The ghostwriter that will be doing the work for the publisher.
     * @param run       The build.
     * @param workspace Path to the build's workspace
     * @param listener    The task listener.
     * @return {@code true} if the build can continue.
     * @throws IOException          on IOException.
     * @throws InterruptedException on InterruptedException.
     */
    public static boolean doPerform(Ghostwriter ghostwriter, Run<?, ?> run, FilePath workspace,
			TaskListener listener) throws IOException, InterruptedException {
    	// first, do we need to do anything on the slave

        if (ghostwriter instanceof Ghostwriter.SlaveGhostwriter) {

            // construct the BuildProxy instance that we will use

            BuildProxy buildProxy = new BuildProxy(
                    //TODO: It is not compatible with custom artifact managers
                    new FilePath(run.getArtifactsDir()),
                    new FilePath(run.getParent().getRootDir()),
                    new FilePath(run.getRootDir()),
                    workspace,
                    run.getTimestamp());

            BuildProxyCallableHelper callableHelper = new BuildProxyCallableHelper(buildProxy, ghostwriter, listener);

            try {
                buildProxy = buildProxy.getExecutionRootDir().act(callableHelper);

                buildProxy.updateBuild(run);

                // terminate the build if necessary
                if (!buildProxy.isContinueBuild()) {
                    return false;
                }
            } catch (Exception e) {
                throw unwrapException(e, listener);
            }
        }

        // finally, on to the master

        final Ghostwriter.MasterGhostwriter masterGhostwriter = Ghostwriter.MasterGhostwriter.class.cast(ghostwriter);

        return masterGhostwriter == null
                || masterGhostwriter.performFromMaster(run, workspace, listener);
		
	}

	//TODO: this logic undermines error propagation in the code
    /**
     * Takes a remote exception that has been wrapped up in the remoting layer, and rethrows it as IOException,
     * InterruptedException or if all else fails, a RuntimeException.
     *
     * @param e        The wrapped exception.
     * @param listener The listener for the build.
     * @return never.
     * @throws IOException          if the wrapped exception is an IOException.
     * @throws InterruptedException if the wrapped exception is an InterruptedException.
     * @throws RuntimeException     if the wrapped exception is neither an IOException nor an InterruptedException.
     */
    private static RuntimeException unwrapException(Exception e,
                                                    TaskListener listener)
            throws IOException, InterruptedException {

        if (e.getCause() instanceof IOException) {
            throw new IOException(e.getCause().getMessage(), e);
        }
        if (e.getCause() instanceof InterruptedException) {
            e.getCause().printStackTrace(listener.getLogger());
            throw new InterruptedException(e.getCause().getMessage());
        }
        if (e.getCause() instanceof RuntimeException) {
            RuntimeException ex = new RuntimeException(e.getCause());
            // It is required to triage JEP-200 security exceptions
            ex.addSuppressed(e);
            throw ex;
        }
        // How on earth do we get this far down the branch
        e.printStackTrace(listener.getLogger());
        throw new RuntimeException("Unexpected exception", e);
    }

    /**
     * (Designed for execution from the master) Updates the build with the results that were reported to this proxy.
     *
     * @param run The run to update.
     */
    public void updateBuild(Run<?, ?> run) {
        // update the actions
        for (AbstractBuildAction<Run<?, ?>> action : actions) {
            if (!run.getActions().contains(action)) {
                action.setBuild(run);
                run.getActions().add(action);
            }
        }

        // update the result
        // TODO: Logic needs to be updated to support Any Build Step and Pipeline jobs
        Result currentResult = run.getResult();
        if (result != null && currentResult != null && result.isWorseThan(currentResult)) {
            run.setResult(result);
        }
    }


// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Constructs a new build proxy that encapsulates all the information that a build step should need from the
     * slave.
     *
     * @param artifactsDir     The artifacts directory on the master.
     * @param projectRootDir   The project directory on the master (i.e. the .../hudson/jobs/ProjectName/). Note for
     *                         multi-module projects it will be .../hudson/jobs/ProjectName/modules/ModuleName/.
     * @param buildRootDir     The build results directory on the master.
     * @param executionRootDir The build base directory on the slave.
     * @param timestamp        The time when the build started executing.
     */
    private BuildProxy(FilePath artifactsDir,
                       FilePath projectRootDir,
                       FilePath buildRootDir,
                       FilePath executionRootDir,
                       Calendar timestamp) {
        this.artifactsDir = artifactsDir;
        this.projectRootDir = projectRootDir;
        this.buildRootDir = buildRootDir;
        this.executionRootDir = executionRootDir;
        this.timestamp = timestamp;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * Getter for property 'actions'.
     *
     * @return Value for property 'actions'.
     */
    public List<AbstractBuildAction<Run<?, ?>>> getActions() {
        return actions;
    }

    /**
     * Gets the directory (on the master) where the artifacts are archived.
     *
     * @return the directory (on the master) where the artifacts are archived.
     */
    public FilePath getArtifactsDir() {
        return artifactsDir;
    }

    /**
     * Root directory of the {@link hudson.model.AbstractBuild} on the master.
     * Files related to the {@link hudson.model.AbstractBuild} should be stored below this directory.
     *
     * @return Root directory of the {@link hudson.model.AbstractBuild} on the master.
     */
    public FilePath getBuildRootDir() {
        return buildRootDir;
    }

    /**
     * Returns the root directory of the checked-out module on the machine where the build executes.
     * This is usually where <tt>pom.xml</tt>, <tt>build.xml</tt>
     * and so on exists.
     *
     * @return Returns the root directory of the checked-out module on the machine where the build executes.
     */
    public FilePath getExecutionRootDir() {
        return executionRootDir;
    }

    /**
     * Root directory of the {@link hudson.model.AbstractProject} on the master.
     * Files related to the {@link hudson.model.AbstractProject} should be stored below this directory.
     *
     * @return Root directory of the {@link hudson.model.AbstractProject} on the master.
     */
    public FilePath getProjectRootDir() {
        return projectRootDir;
    }

    /**
     * Getter for property 'result'.
     *
     * @return Value for property 'result'.
     */
    public Result getResult() {
        return result;
    }

    /**
     * Setter for property 'result'.
     *
     * @param result Value to set for property 'result'.
     */
    public void setResult(Result result) {
        this.result = result;
    }

    /**
     * When the build is scheduled.
     *
     * @return The time when the build started executing.
     */
    public Calendar getTimestamp() {
        return timestamp;
    }
    
    /**
     * Getter for property 'continueBuild'.
     *
     * @return Value for property 'continueBuild'.
     */
    public boolean isContinueBuild() {
        return continueBuild;
    }

    /**
     * Setter for property 'continueBuild'.
     *
     * @param continueBuild Value to set for property 'continueBuild'.
     */
    public void setContinueBuild(boolean continueBuild) {
        this.continueBuild = continueBuild;
    }
}
