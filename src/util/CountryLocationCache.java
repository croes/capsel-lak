package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

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

public class CountryLocationCache {
	
	private Map<String, Location> locs;
	private static final String cacheFile = "data/countries_locs.txt";

	private static CountryLocationCache instance = null;

	public static CountryLocationCache getInstance() {
		if (instance == null) {
			instance = new CountryLocationCache();
		}
		return instance;
	}

	private CountryLocationCache() {
		locs = new HashMap<String, Location>();
		initCache();
	}

	private void initCache() {
		Scanner scan = null;
		;
		try {
			scan = new Scanner(new File(cacheFile));
			try {
				scan.nextLine(); // skip header
				while (scan.hasNextLine()) {
					String line = scan.nextLine();
					String[] cells = line.split(";");
					float lat = Float.parseFloat(cells[0]);
					float lng = Float.parseFloat(cells[1]);
					String name = cells[2];
					name = name.trim();
					if (!name.equals(""))
						locs.put(name, new Location(lat, lng));
				}
			} finally {
				scan.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void addLocation(String name, Location loc) {
		name = name.trim();
		if (!name.equals("")) {
			locs.put(name, loc);
			writeEntryToFile(name, loc);
		}
	}

	public boolean inCache(String name) {
		return locs.containsKey(name);
	}

	private void writeEntryToFile(String name, Location loc) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(cacheFile, true));
			out.printf("%.7f;%.7f;%s;-;\n", loc.x, loc.y, name);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}
	}

	public static Location get(String name) {
		CountryLocationCache cache = getInstance();
		if (cache.locs.containsKey(name)) {
			return cache.locs.get(name);
		} else {
			Location loc = cache.lookup(name);
			if (loc != null)
				cache.addLocation(name, loc);
			return loc;
		}
	}

	public Set<Entry<String, Location>> getEntries() {
		return locs.entrySet();
	}

	public Set<String> getNames() {
		return locs.keySet();
	}

	public Collection<Location> getLocations() {
		return locs.values();
	}

	public Location lookup(String country) {
		if (country.trim().length() == 0) 
			return null;
		System.out.printf("Searching location for: %s\n", country);
		//System.out.println("searching location for: " + country);
		ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
		WebService.setUserName("capsel");
		String sparqlQueryString1 = 				
					"PREFIX o: <http://dbpedia.org/ontology/>" +
					"PREFIX p: <http://dbpedia.org/property/>" +
					"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>" +
					"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +

					"SELECT ?long ?lat ?country " +
					"WHERE {" +
					  "?country a o:Country." +
					  "?country foaf:name \"" + country + "\"@en." +
					  "?country geo:long ?long." +
					  "?country geo:lat ?lat." +
					"}";

		Query query = QueryFactory.create(sparqlQueryString1);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);

		ResultSet results = qexec.execSelect();
		//ResultSetFormatter.out(System.out, results, query);
		
		//TODO there are multiple results, we only use the first one atm.
		Location res = null;
		if(results.hasNext()){
			QuerySolution result = results.next();
			//System.out.println(result);
			Literal latLiteral = result.getLiteral("lat");
			Literal longLiteral = result.getLiteral("long");
			//System.out.println(latLiteral);
			//System.out.println(longLiteral);
			float latCountry = latLiteral.getFloat();
			float longCountry = longLiteral.getFloat();
			res = new Location(latCountry, longCountry);
			System.out.printf("location for %s found: (%.7f, %.7f)\n");
			//System.out.println("(" + latCountry + ", " + longCountry + ")");
		} else{
			System.out.printf("No location found for %s, searching on using Geonames\n", country);
			
			searchCriteria.setQ(country);
			searchCriteria.setMaxRows(1);
			
			try {
				ToponymSearchResult searchResult = WebService.search(searchCriteria); // a toponym search result as returned by the geonames webservice.

				for (Toponym toponym : searchResult.getToponyms()) {
					/*System.out.println(toponym.getName() + " " + toponym.getCountryName()
							+ " " + toponym.getLongitude() + " "
							+ toponym.getLatitude());*/ // prints the search results. We have access on certain get-Functions. In our Case the Name, Country, Longitude and Latitude

					res = new Location(toponym.getLatitude(), toponym.getLongitude());
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		qexec.close() ;
		return res;
	}

	public static void main(String[] args) {
		CountryLocationCache cache = new CountryLocationCache();
		for (Entry<String, Location> e : cache.getEntries()) {
			System.out.println(String.format("%s: <%.7f, %.7f>", e.getKey(), e.getValue().x, e.getValue().y));
		}
		// cache.writeEntryToFile("Candy Mountain", new Location(50,50));
	}
}
