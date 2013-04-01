package util;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;

public class StringUtil {

	private static final Map<String, String> duplicates;

	static {
		duplicates = new HashMap<>();

		// authors
		duplicates.put("Ryan S.J.d. Baker", "Ryan S.j.d. Baker");

		// universities
		duplicates.put("Carnegie Learning Inc", "Carnegie Learning Inc.");
		duplicates.put("Katholieke Universiteit Leuven", "KU Leuven");
		duplicates.put("K.U.Leuven", "KU Leuven");

		// subjects
		duplicates.put("intelligent tutoring system", "intelligent tutoring systems");
		duplicates.put("bayesian network", "bayesian networks");
		
		// countries
		duplicates.put("New_Zeland", "New_Zealand");
		
		duplicates.put("UK", "United Kingdom");
		duplicates.put("United_Kingdom", "United Kingdom");
	}

	public static String getString(String s) {
		if (s == null || (s = s.trim()).length() == 0)
			return "";

		return duplicates.containsKey(s) ? duplicates.get(s) : s;
	}
	
	public static String getString(Literal l) {
		return getString(l == null ? "" : l.getString());
	}
	
	public static String getString(Statement s) {
		return getString(s == null ? "" : s.getString());
	}

	private StringUtil() {
		// defeats instantiation
	}
}
