package util.location;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import de.fhpotsdam.unfolding.geo.Location;

public abstract class LocationCache implements Iterable<Location> {

	private final String fileLocation;

	private final Map<String, Location> locations;

	public LocationCache(String fileLocation) {
		this.fileLocation = fileLocation;
		locations = new HashMap<>();

		loadCache();
	}

	protected void loadCache() {
		Scanner scan = null;

		try {
			scan = new Scanner(new File(fileLocation));
			try {
				scan.nextLine(); // skip header
				while (scan.hasNextLine()) {
					String line = scan.nextLine();
					String[] cells = line.split(";");

					if (cells.length < 3)
						continue;

					float lat = Float.parseFloat(cells[0]);
					float lng = Float.parseFloat(cells[1]);
					String name = cells[2].trim();
					if (!name.equals(""))
						locations.put(name, new Location(lat, lng));
				}
			} finally {
				scan.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void writeEntryToFile(String name, Location loc) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(fileLocation, true));
			out.printf("%.7f;%.7f;%s;-;\n", loc.x, loc.y, name);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}
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

}
