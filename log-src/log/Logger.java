package log;

public interface Logger {

	void catching(Throwable t);
	
	void trace(String s, Object... params);
	void debug(String s, Object... params);
	void  info(String s, Object... params);
	void  warn(String s, Object... params);
	void error(String s, Object... params);
	void fatal(String s, Object... params);
}
