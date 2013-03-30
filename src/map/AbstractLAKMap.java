package map;

import java.util.List;

import marker.EdgeMarker;
import marker.NamedMarker;
import marker.ProxyMarker;
import marker.SelectableMarkerManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import processing.core.PApplet;
import rdf.RDFModel;
import util.StringUtil;
import util.location.LocationCache;
import util.task.Task;
import util.task.TaskManager;

import com.hp.hpl.jena.rdf.model.RDFNode;

import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.ListBox;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.looksgood.ani.Ani;

public abstract class AbstractLAKMap extends PApplet {

	private static final long serialVersionUID = -4973608268494925502L;

	private static final Logger logger = LogManager.getFormatterLogger(AbstractLAKMap.class);

	protected UnfoldingMap map;

	protected SelectableMarkerManager<NamedMarker> nodeMarkerManager;
	protected SelectableMarkerManager<EdgeMarker<NamedMarker>> edgeMarkerManager;

	protected LocationCache locationCache;

	protected abstract LocationCache createLocationCache();

	protected ListBox conferenceList;
	
	private TaskManager mouseMovedTaskManager = new TaskManager("MouseMoved", 1);

	@Override
	public void setup() {
		logger.debug("Setting up AbstractLAKMap");

		size(1040, 720);
		frameRate(30);
		smooth();

		// init LibAni
		Ani.init(this);

		// create location cache
		logger.debug("Creating LocationCache");
		locationCache = createLocationCache();

		logger.debug("Creating map");
		map = new UnfoldingMap(this);
		map.setTweening(true);

		map.zoomAndPanTo(new Location(20, 0), 3);
		MapUtils.createDefaultEventDispatcher(this, map);

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

	protected abstract void addAllNodeMarkers();

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

	protected abstract void showNodeMarkersOf(String conference);

	/**
	 * Find the marker with a given name
	 */
	protected NamedMarker getNodeMarkerWithName(String name) {
		for (ProxyMarker<NamedMarker> nm : nodeMarkerManager) {
			if (nm.getOriginal().getName().equals(name))
				return nm.getOriginal();
		}
		return null;
	}

	protected abstract void addAllEdgeMarkers();

	protected abstract void addEdgeMarkersOf(String conference);

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
