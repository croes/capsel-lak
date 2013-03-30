package map;

import marker.EdgeMarker;
import marker.NamedMarker;
import marker.ProxyMarker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import processing.core.PApplet;
import rdf.RDFModel;
import util.StringUtil;
import util.location.CountryLocationCache;
import util.location.LocationCache;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.fhpotsdam.unfolding.geo.Location;

public class CountryMap extends AbstractLAKMap {

	private static final Logger logger = LogManager.getFormatterLogger(CountryMap.class);

	private static final long serialVersionUID = -7231594744155656041L;

	public static void main(String[] args) {
		PApplet.main(new String[] { "map.CountryMap" });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see map.AbstractLAKMap#addAllNodeMarkers()
	 */
	@Override
	protected void addAllNodeMarkers() {
		logger.trace("Adding all country markers");
		ResultSet rs = RDFModel.getAllCountries();
		logger.trace("Gotten all countries, adding them");
		
		while (rs.hasNext()) {
			QuerySolution sol = rs.next();
			String countryName = parseCountryURL(sol.getResource("country").toString());
			Location loc = locationCache.get(countryName);
			if (loc != null) {
				NamedMarker m = new NamedMarker(countryName, loc);
				nodeMarkerManager.addOriginalMarker(m);
			}
		}
	}

	private String parseCountryURL(String countryURL) {
		int lastIndexSlash = countryURL.lastIndexOf("/");
		String country = countryURL.substring(lastIndexSlash + 1);
		return StringUtil.getString(country);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see map.AbstractLAKMap#showNodeMarkersOf(java.lang.String)
	 */
	@Override
	protected void showNodeMarkersOf(String confAcronym) {
		for (String countryURL : RDFModel.getCountriesOfConference(confAcronym)) {
			String countryName = parseCountryURL(countryURL);
			for (ProxyMarker<NamedMarker> m : nodeMarkerManager) {
				if (m.getOriginal().getName().equals(countryName))
					m.setHidden(false);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see map.AbstractLAKMap#addAllEdgeMarkers()
	 */
	@Override
	protected void addAllEdgeMarkers() {
		ResultSet rs = RDFModel.getAllCountryPairsThatWroteAPaperTogether();
		// ResultSetFormatter.out(rs);
		QuerySolution sol;
		while (rs.hasNext()) {
			sol = rs.next();
			if (!isValidSolutionForMarker(sol))
				continue;
			String countryName = parseCountryURL(sol.getResource("country").toString());
			String otherCountryName = parseCountryURL(sol.getResource("otherCountry").toString());
			int coopCount = sol.getLiteral("coopCount").getInt();
			NamedMarker start = getNodeMarkerWithName(countryName);
			NamedMarker end = getNodeMarkerWithName(otherCountryName);
			if (start == null || end == null)
				continue;
			EdgeMarker<NamedMarker> m = new EdgeMarker<>(start, end);
			m.setColor(0xF0505050);
			m.setHighlightColor(0xFFFF0000);
			m.setStrokeWeight(coopCount);
			edgeMarkerManager.addOriginalMarker(m);
			logger.debug("Common papers for %s to %s:%d", countryName, otherCountryName, coopCount);
		}
	}

	/**
	 * Tests whether the given querySolution can be used to create a valid
	 * marker.
	 * 
	 * @param solution
	 * @return
	 */
	private boolean isValidSolutionForMarker(QuerySolution solution) {
		if (solution.getResource("country") == null || solution.getResource("otherCountry") == null)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see map.AbstractLAKMap#addEdgeMarkersOf(java.lang.String)
	 */
	@Override
	protected void addEdgeMarkersOf(String confAcronym) {
		ResultSet rs = RDFModel.getAllCountryPairsThatWroteAPaperTogetherFromGivenConference(confAcronym);
		// ResultSetFormatter.out(rs);
		QuerySolution sol;
		while (rs.hasNext()) {
			sol = rs.next();
			if (!isValidSolutionForMarker(sol))
				continue;

			String countryName = parseCountryURL(sol.getResource("country").toString());
			String otherCountryName = parseCountryURL(sol.getResource("otherCountry").toString());
			int coopCount = sol.getLiteral("coopCount").getInt();
			NamedMarker start = getNodeMarkerWithName(countryName);
			NamedMarker end = getNodeMarkerWithName(otherCountryName);
			if (start == null || end == null)
				continue;
			EdgeMarker<NamedMarker> m = new EdgeMarker<>(start, end);
			m.setColor(0xF0505050);
			m.setHighlightColor(0xFFFF0000);
			m.setStrokeWeight(coopCount);
			edgeMarkerManager.addOriginalMarker(m);
			logger.debug("Common papers for %s to %s:%d for conf:%s", countryName, otherCountryName, coopCount,
					confAcronym);
		}
	}

	@Override
	protected LocationCache createLocationCache() {
		return new CountryLocationCache("data/countries_locs.txt");
	}
}
