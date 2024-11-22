package minerva.base;

import org.pmw.tinylog.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @see Timer
 */
public abstract class AbstractTimer implements org.quartz.Job {

	@Override
	public final void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            timerEvent();
        } catch (Exception e) {
            Logger.error(e);
        }
	}
	
    /**
     * Timer event occured. Implement what to do.
     */
	protected abstract void timerEvent();
}
