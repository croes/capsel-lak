package util;

import java.util.HashMap;
import java.util.Map;

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
	}

	public static String getString(String s) {
		if (s == null || (s = s.trim()).length() == 0)
			return "";

		return duplicates.containsKey(s) ? duplicates.get(s) : s;
	}

	private StringUtil() {
		// defeats instantiation
	}
}
