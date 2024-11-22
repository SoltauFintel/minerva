package minerva.base;

import java.util.TimeZone;

import org.pmw.tinylog.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import github.soltaufintel.amalia.web.config.AppConfig;
import gitper.base.StringService;
import minerva.config.MinervaOptions;

/**
 * Timer builder
 */
public class Timer {
	public static Timer INSTANCE;
	private final AppConfig config;
	private final org.quartz.Scheduler scheduler;
	
	public static Timer create(AppConfig config) {
		if (INSTANCE != null) {
			throw new RuntimeException("Timer has already been created!");
		}
		INSTANCE = new Timer(config);
		return INSTANCE;
	}
	
	private Timer(AppConfig config) {
		this.config = config;
		try {
			scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void createTimer(Class<? extends AbstractTimer> timerClass, String defaultCron) {
		createTimer(timerClass, defaultCron, false);
	}
	
	public void createTimer(Class<? extends AbstractTimer> timerClass, String defaultCron, boolean forceTimerAndDefaultCron) {
		if (forceTimerAndDefaultCron) {
			installTimer(timerClass, defaultCron);
		} else if (checkIfTimersAreActive(timerClass)) {
			String cron = config.get(timerClass.getSimpleName() + ".cron", defaultCron);
			installTimer(timerClass, cron);
		}
	}
	
	private void installTimer(Class<? extends AbstractTimer> timerClass, String cron) {
		if (StringService.isNullOrEmpty(cron) || "-".equals(cron.trim())) {
			Logger.debug("Timer " + timerClass.getSimpleName() + " has not been started because cron expression is empty or '-'.");
		} else {
			try {
		        JobDetail job = JobBuilder.newJob(timerClass).build();
		        Trigger trigger = TriggerBuilder.newTrigger()
		                .withSchedule(CronScheduleBuilder.cronSchedule(cron).inTimeZone(TimeZone.getTimeZone("CET"))).build();
				scheduler.scheduleJob(job, trigger);
	            Logger.info(timerClass.getSimpleName() + " started. cron: " + cron);
			} catch (Exception e) {
				throw new RuntimeException("Error scheduling timer " + timerClass.getSimpleName() + " with cron \"" + cron + "\"", e);
			}
		}
	}
	
	public static boolean checkIfTimersAreActive(Class<? extends AbstractTimer> timerClass) {
		boolean active = "1".equals(MinervaOptions.TIMER_ACTIVE.get());
		if (!active) {
			Logger.debug("Timer " + timerClass.getSimpleName() + " has not been started because '"
					+ MinervaOptions.TIMER_ACTIVE.getLabel() + "' is not '1'.");
		}
		return active;
	}
}
