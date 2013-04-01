package map;

import java.io.IOException;
import java.util.List;

import marker.EdgeMarker;
import marker.ProxyMarker;
import marker.SelectableMarkerManager;
import marker.SwitchableNamedMarker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import processing.core.PApplet;
import rdf.RDFModel;
import util.StringUtil;
import util.location.CountryLocationCache;
import util.location.LocationCache;
import util.location.OrganizationCountryMap;
import util.location.OrganizationLocationCache;
import util.task.Task;
import util.task.TaskManager;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.ListBox;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.events.MapEvent;
import de.fhpotsdam.unfolding.events.MapEventListener;
import de.fhpotsdam.unfolding.events.ZoomMapEvent;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.looksgood.ani.Ani;

public class SwitchingLAKMap extends PApplet {

	private static final long serialVersionUID = -4973608268494925502L;

	private static final Logger logger = LogManager.getFormatterLogger(SwitchingLAKMap.class);
	
	public static void main(String[] args) {
		PApplet.main(new String[] { "map.SwitchingLAKMap"});
	}

	protected UnfoldingMap map;

	protected SelectableMarkerManager<SwitchableNamedMarker> nodeMarkerManager;
	protected SelectableMarkerManager<EdgeMarker<SwitchableNamedMarker>> edgeMarkerManager;

	protected LocationCache countryLocationCache, universityLocationCache;

	protected ListBox conferenceList;

	private TaskManager mouseMovedTaskManager = new TaskManager("MouseMoved", 1);

	private OrganizationCountryMap orgCountryMap;

	@Override
	public void setup() {
		logger.debug("Setting up SwitchingLAKMap");

		size(1040, 720);
		frameRate(30);
		smooth();

		logger.debug("Initializing libAni");
		// init LibAni
		Ani.init(this);

		// create location cache
		logger.debug("Creating LocationCaches");
		try {
			countryLocationCache = new CountryLocationCache("data/countries_locs.txt");
			universityLocationCache = new OrganizationLocationCache("data/org_locs.txt");
		} catch (IOException e) {
			logger.fatal("Location cache file produced an IO Error");
			logger.catching(e);
			
			exit();
		}

		logger.debug("Creating OrganizationCountryMap");
		orgCountryMap = OrganizationCountryMap.getInstance();

		logger.debug("Creating map");
		map = new UnfoldingMap(this);
		map.setTweening(true);

		map.zoomAndPanTo(new Location(20, 0), 8);
		EventDispatcher dispatcher = MapUtils.createDefaultEventDispatcher(this, map);
		dispatcher.register(new MapEventListener() {

			int prevZoomLevel = 8;

			final int TRESHOLD = 5;

			@Override
			public void onManipulation(MapEvent event) {
				if (!(event instanceof ZoomMapEvent))
					return;

				ZoomMapEvent zevent = (ZoomMapEvent) event;
				logger.trace("%s\t- zoom:%.2f - zoomDelta:%.2f - zoomLevel:%d - zoomLevelDelta:%d",
						zevent.getSubType(), zevent.getZoom(), zevent.getZoomDelta(), zevent.getZoomLevel(),
						zevent.getZoomLevelDelta());
				logger.trace("ZoomLevel map: %d", map.getZoomLevel());

				final int zoomLevel;
				switch (zevent.getSubType()) {
				case ZoomMapEvent.ZOOM_TO_LEVEL:
					zoomLevel = zevent.getZoomLevel();
					break;
				case ZoomMapEvent.ZOOM_BY_LEVEL:
					zoomLevel = map.getZoomLevel() + zevent.getZoomLevelDelta();
					break;
				default:
					return;
				}

				int prevZoomLevel = this.prevZoomLevel;
				this.prevZoomLevel = zoomLevel;

				if ((zoomLevel > TRESHOLD) == (prevZoomLevel > TRESHOLD))
					return;

				for (ProxyMarker<SwitchableNamedMarker> m : nodeMarkerManager) {
					m.getOriginal().switchMarker(zoomLevel <= TRESHOLD);
				}
			}

			@Override
			public String getId() {
				return map.getId();
			}
		}, "zoom", map.getId());

		nodeMarkerManager = new SelectableMarkerManager<>();
		edgeMarkerManager = new SelectableMarkerManager<>();

		map.addMarkerManager(edgeMarkerManager);
		map.addMarkerManager(nodeMarkerManager);

		logger.debug("Creating ControlP5 UI");
		setupGUI();
		populateGUI();

		logger.debug("Populating the map");
		addAllNodeMarkers();
		addAllEdgeMarkers();

		logger.debug("AbstractLAKMap set up");
	}

	@Override
	public void draw() {
		background(245);

		// draw the map
		map.draw();

		// draw the FPS
		drawFPS();
	}

	private void drawFPS() {
		String s = String.format("FPS: %.7f", frameRate);
		float width = textWidth(s);

		stroke(0);
		fill(0);
		rect(10, 5, width + 10, 20);

		stroke(0xFFEEEEEE);
		fill(0xFFEEEEEE);
		text(s, 15, 20);
	}

	/**
	 * Add all the extra GUI elements (apart from the map) to the PApplet.
	 */
	protected void setupGUI() {
		ControlP5 cp5 = new ControlP5(this);
		conferenceList = cp5.addListBox("Conferences").setPosition(50, 50).setSize(120, 120).setBarHeight(15)
				.setItemHeight(15);
		cp5.addButton("ShowAllButton").setCaptionLabel("Show all").setValue(0).setPosition(200, 35).setSize(120, 19);
		// Listener to control selection events.
		cp5.addListener(new ControlListener() {
			@Override
			public void controlEvent(ControlEvent e) {
				if (e.isGroup() && e.getGroup().getName().equals("Conferences")) {
					int idx = (int) e.getGroup().getValue();
					String acro = conferenceList.getItem(idx).getName();
					showOnlyConference(acro);
				}
				if (!e.isGroup() && e.getController().getName().equals("ShowAllButton")) {
					showAll();
				}
			}
		});
	}

	/**
	 * Populate the extra GUI elements with the needed data.
	 */
	protected void populateGUI() {
		List<RDFNode> confs = RDFModel.getConferences();
		for (int i = 0; i < confs.size(); i++) {
			String acronym = StringUtil.getString(confs
					.get(i)
					.asResource()
					.getProperty(
							RDFModel.getModel().getProperty("http://data.semanticweb.org/ns/swc/ontology#hasAcronym")));
			conferenceList.addItem(acronym, i);
		}
	}

	protected void addAllNodeMarkers() {
		ResultSet rs = RDFModel.getAllOrganisations();

		while (rs.hasNext()) {
			QuerySolution sol = rs.next();
			String universityName = StringUtil.getString(sol.getLiteral("orgname"));
			String countryName = orgCountryMap.get(universityName);

			if (countryName == null) {
				logger.error("No country found for university %s", universityName);
				continue;
			}

			if (getNodeMarkerWithName(universityName) != null)
				continue;

			Location universityLocation = universityLocationCache.get(universityName);
			Location countryLocation = countryLocationCache.get(countryName);

			if (universityLocation == null) {
				logger.error("No location for university %s found", universityName);
				continue;
			}
			if (countryLocation == null) {
				logger.error("No location for country %s found", countryName);
				continue;
			}

			nodeMarkerManager.addOriginalMarker(new SwitchableNamedMarker(universityName, universityLocation,
					countryName, countryLocation));
		}
	}

	/**
	 * Shows all the organizations markers from the map.
	 */
	protected void showAllNodeMarkers() {
		for (Marker m : nodeMarkerManager.getMarkers()) {
			m.setHidden(false);
		}
	}

	/**
	 * Hides all the organizations markers from the map.
	 */
	protected void hideAllNodeMarkers() {
		for (Marker m : nodeMarkerManager.getMarkers()) {
			m.setHidden(true);
		}
	}

	protected void showNodeMarkersOf(String conference) {
		// TODO
	}

	/**
	 * Find the marker with a given name
	 */
	protected SwitchableNamedMarker getNodeMarkerWithName(String name) {
		for (ProxyMarker<SwitchableNamedMarker> nm : nodeMarkerManager) {
			if (nm.getOriginal().getName().equals(name))
				return nm.getOriginal();
		}
		return null;
	}

	protected void addAllEdgeMarkers() {
		// TODO
	}

	protected void addEdgeMarkersOf(String conference) {
		// TODO
	}

	/**
	 * Called when showAll button is clicked
	 */
	protected void showAll() {
		showAllNodeMarkers();
		edgeMarkerManager.clearMarkers();
		addAllEdgeMarkers();
	}

	/**
	 * Shows only the markers from the given conference name on the map.
	 * 
	 * @param confAcronym
	 */
	protected void showOnlyConference(String confAcronym) {
		hideAllNodeMarkers();
		showNodeMarkersOf(confAcronym);
		edgeMarkerManager.clearMarkers();
		addEdgeMarkersOf(confAcronym);
	}

	/**
	 * Takes care of the hovering feature. When you hover over a marker, the
	 * marker is set to selected and the marker handles it change in look
	 * itself.
	 */
	public void mouseMoved() {
		final List<? extends Marker> hitEdgeMarkers = edgeMarkerManager.getHitMarkers(mouseX, mouseY);
		final List<? extends Marker> hitNodeMarkers = nodeMarkerManager.getHitMarkers(mouseX, mouseY);

		mouseMovedTaskManager.schedule(new Task("mouseMoved") {
			@Override
			public void execute() throws Throwable {
				for (Marker m : edgeMarkerManager) {
					m.setSelected(hitEdgeMarkers.contains(m));
				}

				for (Marker m : nodeMarkerManager) {
					m.setSelected(hitNodeMarkers.contains(m));
				}
			}

		});
	}
}
