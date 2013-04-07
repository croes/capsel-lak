package util.location;

import java.io.IOException;

import log.LogManager;
import log.Logger;

import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;

import de.fhpotsdam.unfolding.geo.Location;

public class CountryLocationCache extends LocationCache {

	private static final Logger logger = LogManager.getLogger(CountryLocationCache.class);

	public CountryLocationCache(String fileName) throws IOException {
		super(fileName);
	}

	@Override
	protected Location lookup(String name) {
		if (name.trim().length() == 0)
			return null;
		logger.debug(String.format("Searching location for: %s", name));

		ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
		WebService.setUserName("capsel");
		String sparqlQueryString1 = "PREFIX o: <http://dbpedia.org/ontology/>"
				+ "PREFIX p: <http://dbpedia.org/property/>" + "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>"
				+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +

				"SELECT ?long ?lat ?country " + "WHERE {" + "?country a o:Country." + "?country foaf:name \"" + name
				+ "\"@en." + "?country geo:long ?long." + "?country geo:lat ?lat." + "}";

		Query query = QueryFactory.create(sparqlQueryString1);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
		Location res = null;
		try {
			ResultSet results = qexec.execSelect();

			// TODO there are multiple results, we only use the first one atm.
			if (results.hasNext()) {
				QuerySolution result = results.next();
				
				Literal latLiteral = result.getLiteral("lat");
				Literal longLiteral = result.getLiteral("long");
				
				float latCountry = latLiteral.getFloat();
				float longCountry = longLiteral.getFloat();
				res = new Location(latCountry, longCountry);
				logger.debug(String.format("location for %s found: (%.7f, %.7f)\n", name, latCountry, longCountry));
			} else {
				logger.debug(String.format("No location found for %s, searching on using Geonames\n", name));

				searchCriteria.setQ(name);
				searchCriteria.setMaxRows(1);

				try {
					// a toponym search result as returned by the geonames
					// webservice.
					ToponymSearchResult searchResult = WebService.search(searchCriteria);

					for (Toponym toponym : searchResult.getToponyms()) {
						// prints the search results. We have access on certain
						// get-Functions. In our Case the Name, Country,
						// Longitude and Latitude
						/*
						 * System.out.println(toponym.getName() + " " +
						 * toponym.getCountryName() + " " +
						 * toponym.getLongitude() + " " +
						 * toponym.getLatitude());
						 */

						res = new Location(toponym.getLatitude(), toponym.getLongitude());
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		} finally {
			qexec.close();
		}
		return res;
	}

}
