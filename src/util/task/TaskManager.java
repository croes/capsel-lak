package util.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import log.LogManager;
import log.Logger;

public class TaskManager {

	private static final Logger logger = LogManager.getLogger(TaskManager.class);

	private static class ThreadFactory implements java.util.concurrent.ThreadFactory {

		private int count = 0;
		private final String name;

		private final ThreadGroup group;

		public ThreadFactory(String name) {
			this.name = name;
			group = new ThreadGroup(name);
		}

		@Override
		public Thread newThread(Runnable r) {
			count++;

			Thread t = new Thread(group, r, String.format("TaskManager_%s-%d", name, count));
			t.setDaemon(true);
			return t;
		}

	}

	private final ExecutorService executor;

	public TaskManager(String name, int nbConcurrent) {
		if (nbConcurrent == 1)
			executor = Executors.newSingleThreadExecutor(new ThreadFactory(name));
		else
			executor = Executors.newFixedThreadPool(nbConcurrent, new ThreadFactory(name));
	}

	public Future<?> schedule(Task task) {
		logger.trace("Scheduling task %s with name %s (current thread: %s)", task, task.getName(), Thread
				.currentThread().getName());
		return executor.submit(task);
	}

	public void shutdown() {
		executor.shutdown();
	}
}
