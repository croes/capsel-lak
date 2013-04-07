package log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class LogManager {

	private static final Constructor<? extends Logger> loggerCreator;

	private static final Map<Class<?>, Logger> loggers;

	@SuppressWarnings("unchecked")
	private static Constructor<? extends Logger> getLoggerCreator() {
		Class<? extends Logger> loggerClass;

		try {
			loggerClass = (Class<? extends Logger>) Class.forName("log.impl.Log4j2Logger");
		} catch (ExceptionInInitializerError e) {
			try {
				loggerClass = (Class<? extends Logger>) Class.forName("log.impl.Log4jLogger");
			} catch (ClassNotFoundException e1) {
				throw new ExceptionInInitializerError(e1);
			} catch (ExceptionInInitializerError e1) {
				throw new ExceptionInInitializerError(
						"Failed to load Log4j2 and Log4j, please make sure that at least of of both is added to the classpath");
			}
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError(e);
		}

		try {
			return loggerClass.getDeclaredConstructor(Class.class);
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	static {
		loggerCreator = getLoggerCreator();

		loggers = new HashMap<>();
	}

	public static final Logger getLogger(Class<?> clazz) {
		if (loggers.containsKey(clazz))
			return loggers.get(clazz);

		Logger logger;
		try {
			logger = loggerCreator.newInstance(clazz);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Excption when creating logger instance", e);
		}

		loggers.put(clazz, logger);
		return logger;
	}

}
