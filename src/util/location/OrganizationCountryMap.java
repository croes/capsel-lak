package util.location;

import java.util.HashMap;
import java.util.Map;

import log.LogManager;
import log.Logger;

import rdf.RDFModel;
import util.StringUtil;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class OrganizationCountryMap {

	private static final Logger logger = LogManager.getLogger(OrganizationCountryMap.class);

	private static OrganizationCountryMap instance;

	public static OrganizationCountryMap getInstance() {
		if (instance == null) {
			synchronized (OrganizationCountryMap.class) {
				if (instance == null) {
					ResultSet rs = RDFModel.getOrganisationCountryMap();
					OrganizationCountryMap map = new OrganizationCountryMap(rs);
					instance = map;
				}
			}
		}

		return instance;
	}

	private final Map<String, String> map;

	private OrganizationCountryMap(ResultSet rs) {
		map = new HashMap<>();

		while (rs.hasNext()) {
			QuerySolution sol = rs.next();
			String countryName = StringUtil.parseCountryURL(sol.getResource("country"));
			String orgName = StringUtil.getString(sol.getLiteral("orgName"));
			
			if ("".equals(countryName) || "".equals(orgName)) {
				continue;
			}

			if (map.containsKey(orgName)) {
				logger.warn("map says org \"%s\" is in %s, but query says %s", orgName, map.get(orgName), countryName);
			}
			map.put(orgName, countryName);
		}
	}
	
	public String get(String org) {
		return map.get(org);
	}
}
