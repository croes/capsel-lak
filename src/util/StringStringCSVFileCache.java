package util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class StringStringCSVFileCache extends CSVFileCache {
	
	private Map<String, String> strings;
	
	public StringStringCSVFileCache(String fileLocation) throws IOException {
		super(fileLocation, Format.STRING, Format.STRING);
	}

	@Override
	protected void preParseInit() {
		strings = new HashMap<>();
	}

	@Override
	protected void processLine(CSVLine line) {
		strings.put(line.getString(0), line.getString(1));
	}
	
	public boolean hasKey(String key) {
		return strings.containsKey(key);
	}
	
	public String get(String key) {
		return strings.get(key);
	}
	
	public void put(String key, String val) {
		strings.put(key, val);
		
		CSVLine line = new CSVLine();
		line.setString(0, key);
		line.setString(1, val);
		
		addLine(line);
	}

}
