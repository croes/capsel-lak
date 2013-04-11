package ui.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import log.LogManager;
import log.Logger;
import processing.core.PApplet;
import ui.marker.EdgeMarker;
import ui.marker.NamedMarker;
import ui.marker.SelectableMarkerManager;
import util.StringCouple;
import util.task.Task;
import util.task.TaskManager;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.looksgood.ani.Ani;

public abstract class AbstractLAKMap<Node extends NamedMarker, Edge extends EdgeMarker<Node>> extends PApplet {

	private static final long serialVersionUID = 7338377844735009681L;

	private static final Logger logger = LogManager.getLogger(AbstractLAKMap.class);

	public static interface DataProvider {

		Location getCountryLocation(String country);

		Location getOrganizationLocation(String organization);

		String getCountry(String organization);

		SortedSet<String> getAllOrganizations();

		/**
		 * Just a call to
		 * <code>getOrganizationsForConference(conferenceAcronym, null);</code>
		 * 
		 * @see #getOrganizationsForConference(String, Set)
		 */
		Set<String> getOrganizationsForConference(String conferenceAcronym);

		/**
		 * Returns a set containing the organizations for the given conference.
		 * If the set given is not <code>null</code>, the data is added to that
		 * set and the set is returned. Otherwise, a new set object is returned.
		 */
		Set<String> getOrganizationsForConference(String conferenceAcronym, Set<String> organizations);

		Set<StringCouple> getOrganizationCooperationsForConference(String conferenceAcronym);

		Set<StringCouple> getOrganizationCooperationsForConference(String conferenceAcronym,
				Set<StringCouple> organizationCooperation);

		Map<StringCouple, Integer> getOrganizationCooperationDataForConference(String conferenceAcronym);

		Map<StringCouple, Integer> getOrganizationCooperationDataForConference(String conferenceAcronym,
				Map<StringCouple, Integer> data);
		
		Map<StringCouple, Integer> getAllOrganizationCooperationData();
	}

	protected UnfoldingMap map;

	protected final SelectableMarkerManager<Node> nodeMarkerManager;
	protected final SelectableMarkerManager<Edge> edgeMarkerManager;
	
	private final Map<String, Node> nodes;
	private final Map<StringCouple, Edge> edges;

	private final TaskManager mouseMovedTaskManager;

	protected final DataProvider dataProvider;

	private final boolean drawFPS;

	public AbstractLAKMap(DataProvider data, boolean drawFPS) {
		dataProvider = data;
		this.drawFPS = drawFPS;

		mouseMovedTaskManager = new TaskManager("MouseMoved", 1);

		nodeMarkerManager = new SelectableMarkerManager<>();
		edgeMarkerManager = new SelectableMarkerManager<>();
		
		nodes = new HashMap<>();
		edges = new HashMap<>();
	}

	@Override
	public void setup() {
		logger.debug("Setting up AbstractLAKMap");

		frameRate(30);
		smooth();

		// init LibAni
		Ani.init(this);

		logger.debug("Creating map");
		map = new UnfoldingMap(this);
		map.setTweening(true);

		map.zoomAndPanTo(new Location(20, 0), 3);
		MapUtils.createDefaultEventDispatcher(this, map);

		map.addMarkerManager(edgeMarkerManager);
		map.addMarkerManager(nodeMarkerManager);

		logger.debug("Populating the map");
		createAllNodeMarkers();
		createAllEdgeMarkers();

		logger.debug("AbstractLAKMap set up");
	}

	@Override
	public void draw() {
		background(245);

		// draw the map
		map.draw();

		// draw the FPS
		if (drawFPS)
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
	
	protected final void storeNodeMarker(Node marker) {
		nodeMarkerManager.addOriginalMarker(marker);
		nodes.put(marker.getName(), marker);
	}

	protected abstract void createAllNodeMarkers();

	/**
	 * Shows all the organizations markers from the map.
	 */
	protected void showAllNodeMarkers() {
		for (Marker m : nodeMarkerManager) {
			m.setHidden(false);
		}
	}

	/**
	 * Hides all the organizations markers from the map.
	 */
	protected void hideAllNodeMarkers() {
		for (Marker m : nodeMarkerManager) {
			m.setHidden(true);
		}
	}

	/**
	 * Find the marker with a given name
	 */
	protected Node getNodeMarkerWithName(String name) {
		return nodes.get(name);
	}
	
	protected final void storeEdgeMarker(String from, String to, Edge marker) {
		edgeMarkerManager.addOriginalMarker(marker);
		edges.put(new StringCouple(from, to), marker);
	}

	protected abstract void createAllEdgeMarkers();
	
	protected void showAllEdgeMarkers() {
		// TODO revert back to the data of all conferences if needed
		
		for (Marker m : edgeMarkerManager) {
			m.setHidden(false);
		}
	}
	
	protected void hideAllEdgeMarkers() {
		for (Marker m : edgeMarkerManager) {
			m.setHidden(true);
		}
	}

	public void showAllConferences() {
		showAllNodeMarkers();
		showAllEdgeMarkers();
	}

	public abstract void showConferences(Collection<String> selectedConferenceAcronyms);

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