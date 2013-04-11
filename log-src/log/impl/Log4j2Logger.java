package log.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4j2Logger implements log.Logger {
	
	static {
		LogManager.getLogger(Log4j2Logger.class).trace("Using the Log4j2 logging framework");
	}
	
	private final Logger logger;
	
	public Log4j2Logger(Class<?> clazz) {
		logger = LogManager.getFormatterLogger(clazz);
	}

	@Override
	public void catching(Throwable t) {
		logger.catching(t);
	}

	@Override
	public void trace(String s, Object... params) {
		logger.trace(s, params);
	}

	@Override
	public void debug(String s, Object... params) {
		logger.debug(s, params);
	}

	@Override
	public void info(String s, Object... params) {
		logger.info(s, params);
	}

	@Override
	public void warn(String s, Object... params) {
		logger.warn(s, params);
	}

	@Override
	public void error(String s, Object... params) {
		logger.error(s, params);
	}

	@Override
	public void fatal(String s, Object... params) {
		logger.fatal(s, params);
	}

}
