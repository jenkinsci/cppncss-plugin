package hudson.plugins.helpers;

import hudson.model.TaskListener;
import jenkins.security.MasterToSlaveCallable;

import java.io.IOException;

/**
 * A helper class that is used when passing Ghostwriter between the slave and master and invoking the appropriate
 * actions on the agent or the master node.
 *
 * @author Stephen Connolly
 * @since 28-Jan-2008 22:12:29
 */
class BuildProxyCallableHelper extends MasterToSlaveCallable<BuildProxy, Exception> {
    // ------------------------------ FIELDS ------------------------------

    private final BuildProxy buildProxy;
    private final Ghostwriter ghostwriter;
    private final TaskListener listener;

    // --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Creates a new BuildProxyCallableHelper.
     *
     * @param buildProxy  The buildProxy.
     * @param ghostwriter The ghostwriter.
     * @param listener    The listener.
     */
    BuildProxyCallableHelper(BuildProxy buildProxy,
                             Ghostwriter ghostwriter,
                             TaskListener listener) {
        this.buildProxy = buildProxy;
        this.ghostwriter = ghostwriter;
        this.listener = listener;
    }

    // ------------------------ INTERFACE METHODS ------------------------

    // --------------------- Interface Callable ---------------------

    /**
     * {@inheritDoc}
     */
    public BuildProxy call() throws Exception {
        if (ghostwriter instanceof Ghostwriter.SlaveGhostwriter) {
            final Ghostwriter.SlaveGhostwriter slaveBuildStep =
                    (Ghostwriter.SlaveGhostwriter) ghostwriter;
            try {
                buildProxy.setContinueBuild(slaveBuildStep.performFromSlave(buildProxy, listener));
                return buildProxy;
            } catch (IOException e) {
                throw new Exception(e);
            } catch (InterruptedException e) {
                throw new Exception(e);
            }
        }
        return buildProxy;
    }
}
