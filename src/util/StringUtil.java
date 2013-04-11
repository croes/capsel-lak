package util;

import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class StringUtil {

	private static final StringStringCSVFileCache duplicates;

	static {
		try {
			duplicates = new StringStringCSVFileCache("data/strings.csv");
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static String getString(String s) {
		if (s == null || (s = s.trim()).length() == 0)
			return "";

		return duplicates.hasKey(s) ? duplicates.get(s) : s;
	}
	
	public static String getString(Literal l) {
		return getString(l == null ? "" : l.getString());
	}
	
	public static String getString(Statement s) {
		return getString(s == null ? "" : s.getString());
	}
	
	public static String parseCountryURL(String countryURL) {
		int lastIndexSlash = countryURL.lastIndexOf("/");
		String country = countryURL.substring(lastIndexSlash + 1);
		return getString(country);
	}
	
	public static String parseCountryURL(Resource s) {
		return parseCountryURL(String.valueOf(s));
	}
	
	public static String getInitials(String longString){
		StringBuilder builder = new StringBuilder();
		String[] words = longString.split(" ");
		for (int i = 0; i < words.length; i++) {
			if(words[i].length() > 0)
				builder.append(words[i].charAt(0));
		}
		return builder.toString();
	}

	private StringUtil() {
		// defeats instantiation
	}
}
