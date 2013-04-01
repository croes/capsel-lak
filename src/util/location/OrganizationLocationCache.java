package util.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import log.LogManager;
import log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import de.fhpotsdam.unfolding.geo.Location;

public class OrganizationLocationCache extends LocationCache {

	private static final Logger logger = LogManager.getLogger(OrganizationLocationCache.class);

	private static final String GOOGLE_PLACES_API_KEY = "AIzaSyCkUxgtcX32ogBa2Odtf2L4IWsYAH81mfw";

	public OrganizationLocationCache(String fileName) throws IOException {
		super(fileName);
	}

	@Override
	protected Location lookup(String name) {
		name = name.trim();
		if (name.length() == 0)
			return null;

		try {
			JSONObject json = readJsonFromUrl("https://maps.googleapis.com/maps/api/place/textsearch/json?sensor=false&key="
					+ GOOGLE_PLACES_API_KEY + "&query=" + URLEncoder.encode(name, "UTF-8"));
			if (json.getString("status").equals("OK")) {
				double latitude = json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry")
						.getJSONObject("location").getDouble("lat");
				double longitude = json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry")
						.getJSONObject("location").getDouble("lng");
				Location loc = new Location(latitude, longitude);

				return loc;
			} else {
				logger.debug(String.format("No coordinates found for %s.\n", name));
			}
		} catch (IOException | JSONException e) {
			logger.warn(String.format("Error encountered when looking up the location of %s\n", name), e);
		}
		return null;
	}

	private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	private static String readAll(BufferedReader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

}
