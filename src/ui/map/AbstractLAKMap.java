package ui.map;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import log.LogManager;
import log.Logger;
import processing.core.PApplet;
import ui.marker.EdgeMarker;
import ui.marker.NamedMarker;
import ui.marker.SelectableMarkerManager;
import ui.marker.proxy.EmptyProxyMarker;
import ui.marker.proxy.ProxyMarker;
import ui.marker.proxy.SingleProxyMarker;
import util.StringCouple;
import util.task.Task;
import util.task.TaskManager;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.mapdisplay.MapDisplayFactory;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.providers.AbstractMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.looksgood.ani.Ani;

public abstract class AbstractLAKMap<Node extends NamedMarker, Edge extends EdgeMarker<Node>> extends PApplet implements
		ComponentListener {
	
	public static interface Listener extends EventListener {
		void organizationClicked(String organization);
	}

	private static final long serialVersionUID = 7338377844735009681L;

	private static final Logger logger = LogManager.getLogger(AbstractLAKMap.class);

	protected UnfoldingMap map;

	protected final SelectableMarkerManager<Node> nodeMarkerManager;
	protected final SelectableMarkerManager<Edge> edgeMarkerManager;

	private final Map<String, ProxyMarker<Node>> nodes;
	private final Map<StringCouple, ProxyMarker<Edge>> edges;

	private final TaskManager mapTaskManager;

	protected final DataProvider dataProvider;

	private final boolean drawFPS;
	
	private final AbstractMapProvider mapProvider;
	
	protected final EventListenerList listeners;
	
	public AbstractLAKMap(DataProvider data, boolean drawFPS) {
		this(data, drawFPS, MapDisplayFactory.getDefaultProvider());
	}

	public AbstractLAKMap(DataProvider data, boolean drawFPS, AbstractMapProvider mapProvider) {
		listeners = new EventListenerList();
		
		dataProvider = data;
		this.drawFPS = drawFPS;
		this.mapProvider = mapProvider;

		mapTaskManager = new TaskManager("MapTaskManager", 1);

		nodeMarkerManager = new SelectableMarkerManager<>();
		edgeMarkerManager = new SelectableMarkerManager<>();

		nodes = new HashMap<>();
		edges = new HashMap<>();
	}

	@Override
	public void setup() {
		logger.debug("Setting up AbstractLAKMap");

		addComponentListener(this);

		frameRate(60);
		smooth();

		// init LibAni
		Ani.init(this);

		logger.debug("Creating map");
		map = new UnfoldingMap(this, mapProvider);
		map.setTweening(true);

		map.zoomAndPanTo(new Location(20, 0), 3);
		MapUtils.createDefaultEventDispatcher(this, map);

		map.addMarkerManager(edgeMarkerManager);
		map.addMarkerManager(nodeMarkerManager);

		logger.debug("Scheduling task to populate the map");
		schedule(new Task("PopulateMap") {
			@Override
			public void execute() throws Throwable {
				logger.debug("Populating the map");
				createAllNodeMarkers();
				createAllEdgeMarkers();
			}
		});

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
	
	public final void addListener(Listener listener) {
		listeners.add(Listener.class, listener);
	}
	
	public final void removeListener(Listener listener) {
		listeners.remove(Listener.class, listener);
	}

	protected final void storeNodeMarker(Node marker) {
		ProxyMarker<Node> proxy = nodeMarkerManager.addOriginalMarker(marker);
		nodes.put(marker.getName(), proxy);
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
	protected ProxyMarker<Node> getNodeMarkerWithName(String name) {
		ProxyMarker<Node> node = nodes.get(name);
		if (node == null)
			return new EmptyProxyMarker<>();
		return node;
	}

	protected final void storeEdgeMarker(String from, String to, Edge marker) {
		storeEdgeMarker(new StringCouple(from, to), marker);
	}

	protected final void storeEdgeMarker(StringCouple locs, Edge marker) {
		ProxyMarker<Edge> proxy = edgeMarkerManager.addOriginalMarker(marker);
		edges.put(locs, proxy);
	}

	protected abstract void createAllEdgeMarkers();

	protected abstract void showAllEdgeMarkers();

	protected void hideAllEdgeMarkers() {
		for (Marker m : edgeMarkerManager) {
			m.setHidden(true);
		}
	}

	protected ProxyMarker<Edge> getEdgeMarkerForNames(String name1, String name2) {
		return getEdgeMarkerForNames(new StringCouple(name1, name2));
	}

	protected ProxyMarker<Edge> getEdgeMarkerForNames(StringCouple names) {
		ProxyMarker<Edge> edge = edges.get(names);
		if (edge == null)
			return new EmptyProxyMarker<>();
		return edge;
	}

	public void showAllConferences() {
		showAllNodeMarkers();
		showAllEdgeMarkers();
	}

	public abstract void showConferences(Collection<String> selectedConferenceAcronyms);

	public abstract void selectOrg(String selectedUniversity);

	public abstract void unselectOrg(String unselectedUniversity);
	
	public abstract void panToOrganization(String organization);
	
	protected final void schedule(Task task) {
		mapTaskManager.schedule(task);
	}

	/**
	 * Takes care of the hovering feature. When you hover over a marker, the
	 * marker is set to selected and the marker handles it change in look
	 * itself.
	 */
	public void mouseMoved() {
		final List<? extends Marker> hitEdgeMarkers = edgeMarkerManager.getHitMarkers(mouseX, mouseY);
		final List<? extends Marker> hitNodeMarkers = nodeMarkerManager.getHitMarkers(mouseX, mouseY);
		schedule(new Task("mouseMoved") {
			@Override
			public void execute() {

				for (Marker m : edgeMarkerManager) {
					m.setSelected(hitEdgeMarkers.contains(m));
				}

				for (Marker m : nodeMarkerManager) {
					m.setSelected(hitNodeMarkers.contains(m));
				}
			}

		});
	}
	
	@Override
	public void mouseClicked() {
		List<SingleProxyMarker<Node>> hitNodeMarkers = nodeMarkerManager.getHitMarkers(mouseX, mouseY);
		logger.debug("mouse clicked @ (%d,%d), nb. markers: %d", mouseX, mouseY, hitNodeMarkers.size());
		
		if (hitNodeMarkers.size() != 1)
			return;
		
		final String name = hitNodeMarkers.get(0).getOriginal().getText();
		
		schedule(new Task("mouseClicked") {

			@Override
			public void execute() throws Throwable {
				Listener[] listeners = AbstractLAKMap.this.listeners.getListeners(Listener.class);
				
				for (Listener listener : listeners) {
					listener.organizationClicked(name);
				}
			}});
	}

	@Override
	public void componentResized(ComponentEvent e) {
		map.mapDisplay.resize(getWidth(), getHeight());
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// NOP
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// NOP
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// NOP
	}

}
