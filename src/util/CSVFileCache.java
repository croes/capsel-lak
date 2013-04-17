package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import log.LogManager;
import log.Logger;

public abstract class CSVFileCache {

	private static final Logger logger = LogManager.getLogger(CSVFileCache.class);
	
	private static final Pattern csvPattern = Pattern.compile(
			"\"([^\"]*)\"|(?<=;|^)([^;]*)(?=;|$)"
//			"\"([^\"]*)\"|(?<=^|;)([^;]*)(?=;|$)"
			);
	
	/*
	 * "([^"]*)"
	 * |
	 * (?<^|;)([^;]*)(?=;|$)
	 */

	protected static enum Format {
		FLOAT("%7f", Float.class, "float") {
			@Override
			public Float fromString(String s) {
				return Float.valueOf(s);
			}
		},
		STRING("%s", String.class, "String") {
			@Override
			public String fromString(String s) {
				return String.valueOf(s);
			}
		},
		INTEGER("%i", Integer.class, "int") {
			@Override
			public Integer fromString(String s) {
				return Integer.valueOf(s);
			}
		};

		private String fmt;
		private Class<?> clazz;
		private String name;

		private Format(String f, Class<?> c, String n) {
			fmt = f;
			clazz = c;
			name = n;
		}

		public String toString(Object o) {
			return String.format(fmt, o);
		}

		public abstract Object fromString(String s);

		public Class<?> getType() {
			return clazz;
		}

		public String toString() {
			return name;
		}
	}

	protected class CSVLine {
		private Object[] values;

		private CSVLine(Object[] values) {
			this.values = values;
		}

		public CSVLine() {
			values = new Object[formats.length];
		}

		private void checkIdx(int idx, Format requestedFormat) {
			if (idx >= formats.length)
				throw new IllegalArgumentException("Invalid index " + idx);
			if (formats[idx] != requestedFormat)
				throw new IllegalArgumentException("Value index " + idx + " is not of type " + requestedFormat
						+ " but " + formats[idx]);
		}

		public String getString(int idx) {
			checkIdx(idx, Format.STRING);
			return (String) values[idx];
		}

		public void setString(int idx, String s) {
			checkIdx(idx, Format.STRING);
			values[idx] = s;
		}

		public Float getFloat(int idx) {
			checkIdx(idx, Format.FLOAT);
			return (Float) values[idx];
		}

		public void setFloat(int idx, Float f) {
			checkIdx(idx, Format.FLOAT);
			values[idx] = Float.valueOf(f);
		}

		public Integer getInt(int idx) {
			checkIdx(idx, Format.INTEGER);
			return (Integer) values[idx];
		}

		public void setInt(int idx, Integer i) {
			checkIdx(idx, Format.INTEGER);
			values[idx] = Integer.valueOf(i);
		}
	}

	private final String fileLocation;
	private final Format[] formats;

	public CSVFileCache(String fileLocation, Format... formats) throws IOException {
		this.formats = formats;
		this.fileLocation = fileLocation;

		preParseInit();
		parseFile();
	}

	protected abstract void preParseInit();

	private void parseFile() throws IOException {
		Scanner scan = null;

		try {
			scan = new Scanner(new File(fileLocation));
			try {
				// skip header
				scan.nextLine();
				int lineNumber = 1;
				
				Matcher lineMatcher;
				int matchCount;
				String[] matches = new String[formats.length];
				
				while (scan.hasNextLine()) {
					String line = scan.nextLine();
					lineNumber++;
					
					lineMatcher = csvPattern.matcher(line);
					matchCount = 0;
					
					while(lineMatcher.find() && matchCount < formats.length) {
						String match = lineMatcher.group(1);
						if (match == null) match = lineMatcher.group();
						
						matches[matchCount] = match;
						matchCount++;
					}
					
					if (matchCount < formats.length) {
						logger.error("Line %d has too few fields (%d) in file %s: \"%s\"", lineNumber, matchCount, fileLocation, line);
						continue;
					}

					Object[] cellValues = new Object[formats.length];
					for (int idx = 0; idx < formats.length; idx++) {
						if (matches[idx].equals("-")) {
							cellValues[idx] = null;
						} else {
							cellValues[idx] = formats[idx].fromString(matches[idx]);
						}
					}

					processLine(new CSVLine(cellValues));
				}
			} finally {
				scan.close();
			}
		} catch (FileNotFoundException e) {
			logger.error("File %s not found", fileLocation);
			logger.info("Creating CSVFileCache file %s", fileLocation);

			(new File(fileLocation)).createNewFile();

			StringBuilder sb = new StringBuilder();
			for (Format f : formats) {
				sb.append(f).append(';');
			}
			addLine(sb.toString());
		}
	}

	protected void addLine(CSVLine line) {
		StringBuilder sb = new StringBuilder();

		Object[] values = line.values;
		for (int idx = 0; idx < formats.length; idx++) {
			if (values[idx] == null) {
				sb.append("-;");
				continue;
			}

			sb.append(formats[idx].toString(values[idx])).append(";");
		}

		addLine(sb.toString());
	}

	private void addLine(String s) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(fileLocation, true));
			out.printf("%s\n", s);
		} catch (IOException e) {
			logger.error("IOException when adding line \"%s\" to file %s", s, fileLocation);
			logger.catching(e);
		} finally {
			if (out != null)
				out.close();
		}
	}

	protected abstract void processLine(CSVLine line);
}
