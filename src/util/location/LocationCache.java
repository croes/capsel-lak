package util.location;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import util.CSVFileCache;
import de.fhpotsdam.unfolding.geo.Location;

public abstract class LocationCache extends CSVFileCache implements Iterable<Location> {

	private Map<String, Location> locations;

	public LocationCache(String fileLocation) throws IOException {
		super(fileLocation, Format.FLOAT, Format.FLOAT, Format.STRING);
	}

	protected final void preParseInit() {
		locations = new HashMap<>();
	}

	private void writeEntryToFile(String name, Location loc) {
		CSVLine line = new CSVLine();

		line.setFloat(0, loc.x);
		line.setFloat(1, loc.y);
		line.setString(2, name);

		addLine(line);
	}

	@Override
	public Iterator<Location> iterator() {
		return locations.values().iterator();
	}

	public Location get(String name) {
		if (!locations.containsKey(name)) {
			Location loc = lookup(name);
			if (loc != null) {
				writeEntryToFile(name, loc);
				locations.put(name, loc);
			}
		}

		return locations.get(name);
	}

	protected abstract Location lookup(String name);

	@Override
	protected void processLine(CSVLine line) {
		locations.put(line.getString(2), new Location(line.getFloat(0), line.getFloat(1)));
	}

}
