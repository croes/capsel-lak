package util.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Task implements Runnable {

	private static final Logger logger = LogManager.getFormatterLogger(Task.class);

	private final String name;

	public Task(String name) {
		this.name = name;
	}

	public abstract void execute() throws Throwable;

	@Override
	public final void run() {
		logger.trace("Starting task %s, thread %s", name, Thread.currentThread().getName());
		try {
			execute();
		} catch (Throwable t) {
			logger.error("Unexpected exception when executing task %s", name);
			logger.catching(t);
			return;
		}
		logger.trace("Task %s ended", name);
	}

	public String getName() {
		return name;
	}

}
