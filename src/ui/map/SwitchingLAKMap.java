package ui.map;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import log.LogManager;
import log.Logger;
import ui.marker.EdgeMarker;
import ui.marker.SwitchableNamedMarker;
import ui.marker.proxy.EmptyProxyMarker;
import ui.marker.proxy.GroupedProxyMarker;
import ui.marker.proxy.ProxyMarker;
import util.StringCouple;
import util.task.Task;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.events.MapEvent;
import de.fhpotsdam.unfolding.events.MapEventListener;
import de.fhpotsdam.unfolding.events.ZoomMapEvent;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.MapUtils;

public class SwitchingLAKMap extends AbstractLAKMap<SwitchingLAKMap.Node, SwitchingLAKMap.Edge> {

	public static class Node extends SwitchableNamedMarker {

		private ProxyMarker<Node> proxy;

		private boolean fakeIsSelectedOnce;

		public Node(String s1, Location l1, String s2, Location l2, boolean showTwo) {
			super(s1, l1, s2, l2, showTwo);
			fakeIsSelectedOnce = false;
		}

		public void setProxy(ProxyMarker<Node> proxy) {
			this.proxy = proxy;
		}

		@Override
		public boolean isSelected() {
			if (fakeIsSelectedOnce) {
				fakeIsSelectedOnce = false;
				logger.trace("Faking isSelected() once...");
				return !super.isSelected();
			}
			return super.isSelected();
		}

		@Override
		protected void onSelectedUpdated() {
			super.onSelectedUpdated();

			if (proxy == null)
				return;

			if (this.isSelected())
				return;

			boolean selected = countSelectedLines > 0;

			fakeIsSelectedOnce = true;
			if (selected) {
				this.selected = true;
				proxy.setSelected(true);
				this.selected = false;
			} else {
				proxy.setSelected(false);
			}
		}

	}

	public static class Edge extends EdgeMarker<Node> {
		
		private final SwitchingLAKMap map;

		public Edge(SwitchingLAKMap map, ProxyMarker<Node> m1, ProxyMarker<Node> m2) {
			super(m1.getOriginal(), m2.getOriginal());

			this.map = map;
			
			m1.getOriginal().setProxy(m1);
			m2.getOriginal().setProxy(m2);
		}
		
		@Override
		public void setSelected(boolean selected) {
			if (selected == isSelected()) {
				super.setSelected(selected);
				return;
			}
			
			// this may take some calculation
			ProxyMarker<Node> m1s = map.getNodeMarkerWithName(getM1().getName());
			ProxyMarker<Node> m2s = map.getNodeMarkerWithName(getM2().getName());
			
			super.setSelected(selected);
			
			for (int i = 0; i < m1s.getMarkerCount(); i++) {
				if (selected) {
					m1s.getOriginal(i).addSelectedLine();
				} else {
					m1s.getOriginal(i).removeSelectedLine();
				}
			}
			
			for (int i = 0; i < m2s.getMarkerCount(); i++) {
				if (selected) {
					m2s.getOriginal(i).addSelectedLine();
				} else {
					m2s.getOriginal(i).removeSelectedLine();
				}
			}
		}

	}
	
	private static final int TRESHOLD = 4;

	private static final long serialVersionUID = -4973608268494925502L;

	private static final Logger logger = LogManager.getLogger(SwitchingLAKMap.class);

	private transient boolean showCountry;
	private transient Map<StringCouple, Integer> cooperationData;

	public SwitchingLAKMap(DataProvider data, boolean drawFPS) {
		super(data, drawFPS);
		showCountry = false; // set to true in setup!!
		cooperationData = null;
	}

	@Override
	public void setup() {
		super.setup();

		logger.debug("Attaching MapEventListener to listen for zoom events");

		EventDispatcher dispatcher = MapUtils.createDefaultEventDispatcher(this, map);
		dispatcher.register(new MapEventListener() {

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

				scheduleSetShowCountry(zoomLevel <= TRESHOLD);
			}

			@Override
			public String getId() {
				return map.getId();
			}
		}, "zoom", map.getId());
		
		scheduleSetShowCountry(map.getZoomLevel() <= TRESHOLD);
	}
	
	private void scheduleSetShowCountry(final boolean showCountry) {
		schedule(new Task("SetShowCountry") {
			@Override
			public void execute() {
				setShowCountry(showCountry);
			}
		});
	}

	private void setShowCountry(boolean showCountry) {
		if (showCountry == this.showCountry)
			return;

		this.showCountry = showCountry;

		// do the hard work
		
		final Map<StringCouple, Integer> edgeData = getCooperationData();

		for (ProxyMarker<Node> m : nodeMarkerManager) {
			m.getOriginal().switchMarker(showCountry);
		}

		if (edgeData.isEmpty())
			return;

		for (ProxyMarker<Edge> m : edgeMarkerManager) {
			Integer width = edgeData.get(m.getOriginal().getIds());
			if (width == null) {
				logger.trace("not showing %s", m.getOriginal().getIds());
				m.getOriginal().setHiddenUnanimated(true);
			} else {
				logger.trace("showing %s", m.getOriginal().getIds());
				if (m.isHidden()) {
					m.getOriginal().setWidth(width);
					m.getOriginal().setHiddenUnanimated(false);
				} else
					m.getOriginal().setWidthAnimated(width);
			}
		}
	}

	@Override
	protected void createAllNodeMarkers() {
		Collection<String> organizations = dataProvider.getAllOrganizations();

		for (String organization : organizations) {
			String country = dataProvider.getCountry(organization);

			if (country == null) {
				logger.error("No country found for university %s", organization);
				continue;
			}

			if (super.getNodeMarkerWithName(organization).getMarkerCount() != 0)
				continue;

			Location organizationLocation = dataProvider.getOrganizationLocation(organization);
			Location countryLocation = dataProvider.getCountryLocation(country);

			if (organizationLocation == null) {
				logger.error("No location for university %s found", organization);
				continue;
			}
			if (countryLocation == null) {
				logger.error("No location for country %s found", country);
				continue;
			}

			Node node = new Node(organization, organizationLocation, country, countryLocation, showCountry);
			storeNodeMarker(node);
		}
	}

	@Override
	protected void createAllEdgeMarkers() {
		setCooperationData(dataProvider.getAllOrganizationCooperationData());
		Map<StringCouple, Integer> cooperationData = getCooperationData();

		for (StringCouple orgs : this.cooperationData.keySet()) {
			ProxyMarker<Node> m1 = getNodeMarkerWithName(orgs.getString1());
			ProxyMarker<Node> m2 = getNodeMarkerWithName(orgs.getString2());

			if (m1.getMarkerCount() == 0 || m2.getMarkerCount() == 0)
				continue;

			Edge edge = new Edge(this, m1, m2);

			Integer width = cooperationData.get(orgs);
			if (width == null) {
				edge.setHidden(true);
			} else {
				edge.setWidth(width);
			}

			logger.trace("Created edge marker for %s", orgs);
			storeEdgeMarker(orgs, edge);
		}
	}

	@Override
	public void showAllEdgeMarkers() {
		setCooperationData(dataProvider.getAllOrganizationCooperationData());
		Map<StringCouple, Integer> cooperationData = getCooperationData();

		for (StringCouple orgs : this.cooperationData.keySet()) {
			ProxyMarker<Edge> edge = getEdgeMarkerForNames(orgs);

			Integer width = cooperationData.get(orgs);
			if (width == null) {
				edge.setHidden(true);
			} else {
				edge.setHidden(false);
				for (int i = 0; i < edge.getMarkerCount(); i++)
					edge.getOriginal(i).setWidthAnimated(width);
			}
		}
	}

	@Override
	public void showConferences(Collection<String> selectedConferenceAcronyms) {
		// first, get the data

		Set<String> organizations = new HashSet<>();
		Map<StringCouple, Integer> edgeData = new HashMap<>();

		for (String conferenceAcronym : selectedConferenceAcronyms) {
			dataProvider.getOrganizationsForConference(conferenceAcronym, organizations);
			dataProvider.getOrganizationCooperationDataForConference(conferenceAcronym, edgeData);
		}

		setCooperationData(edgeData);
		edgeData = getCooperationData();

		// hide all markers

		hideAllNodeMarkers();
		hideAllEdgeMarkers();

		// show the right markers

		for (String organization : organizations) {
			Marker marker = getNodeMarkerWithName(organization);
			marker.setHidden(false);
		}

		for (Map.Entry<StringCouple, Integer> edge : edgeData.entrySet()) {
			ProxyMarker<Edge> edgeMarker = getEdgeMarkerForNames(edge.getKey());

			edgeMarker.setHidden(false);

			for (int i = 0; i < edgeMarker.getMarkerCount(); i++)
				edgeMarker.getOriginal(i).setWidthAnimated(edge.getValue());
		}
	}

	private void setCooperationData(Map<StringCouple, Integer> data) {
		logger.trace("Setting cooperation data, size = %d", data == null ? 0 : data.size());
		cooperationData = data;
	}
	
	private Map<StringCouple, Integer> getCooperationData() {
		if (cooperationData == null)
			return Collections.emptyMap();
		
		if (!showCountry)
			return cooperationData;

		Map<StringCouple, Integer> countryData = new HashMap<>();
		Map<StringCouple, Integer> result = new HashMap<>();

		for (Map.Entry<StringCouple, Integer> dataEntry : cooperationData.entrySet()) {
			StringCouple orgs = dataEntry.getKey();
			StringCouple countries = new StringCouple(dataProvider.getCountry(orgs.getString1()),
					dataProvider.getCountry(orgs.getString2()));

			if (countryData.containsKey(countries)) {
				countryData.put(countries, countryData.get(countries) + dataEntry.getValue());
			} else {
				countryData.put(countries, dataEntry.getValue());
			}
		}

		for (Map.Entry<StringCouple, Integer> dataEntry : cooperationData.entrySet()) {
			StringCouple orgs = dataEntry.getKey();
			StringCouple countries = new StringCouple(dataProvider.getCountry(orgs.getString1()),
					dataProvider.getCountry(orgs.getString2()));

			if (!countries.sameStrings())
				result.put(orgs, countryData.get(countries));
		}

		return result;
	}

	@Override
	public void selectOrg(String selectedUniversity) {
		getNodeMarkerWithName(selectedUniversity).setSelected(true);
	}

	@Override
	public void unselectOrg(String unselectedUniversity) {
		getNodeMarkerWithName(unselectedUniversity).setSelected(false);
	}

	@Override
	protected ProxyMarker<Edge> getEdgeMarkerForNames(StringCouple names) {
		if (!showCountry)
			return super.getEdgeMarkerForNames(names);

		String country1 = dataProvider.getCountry(names.getString1());
		String country2 = dataProvider.getCountry(names.getString2());

		if ((country1 == null) || (country2 == null) || country1.equals(country2)) {
			// not useful or even impossible to show
			return new EmptyProxyMarker<>();
		}

		Set<StringCouple> organizationCooperations = dataProvider
				.getOrganizationCooperationForCountries(new StringCouple(country1, country2));

		List<ProxyMarker<Edge>> edges = new LinkedList<>();

		for (StringCouple coop : organizationCooperations) {
			ProxyMarker<Edge> edge = super.getEdgeMarkerForNames(coop);
			if (edge.getMarkerCount() == 0)
				continue;
			edges.add(edge);
		}
		
		if (edges.isEmpty()) {
			logger.error(
					"There are apparently no coperations between countries %s and %s, but we know %s and %s cooperated...",
					country1, country2, names.getString1(), names.getString2());
			return new EmptyProxyMarker<>();
		}

		return new GroupedProxyMarker<>(edges);
	}

	/**
	 * Find the marker with a given name
	 */
	protected ProxyMarker<Node> getNodeMarkerWithName(String name) {
		if (!showCountry)
			return super.getNodeMarkerWithName(name);

		String country = dataProvider.getCountry(name);

		if (country == null) {
			logger.error("Cannot found country for organization %s", name);
			return new EmptyProxyMarker<>();
		}

		Set<String> organizations = dataProvider.getOrganizations(country);

		List<ProxyMarker<Node>> nodes = new LinkedList<>();

		for (String organization : organizations) {
			ProxyMarker<Node> node = super.getNodeMarkerWithName(organization);
			if (node.getMarkerCount() == 0) {
				logger.error("Unable to find marker for organization %s", organization);
				logger.error("Got %s in return", node);
				continue;
			}

			nodes.add(node);
		}

		if (nodes.isEmpty()) {
			logger.error("Organizations for country %s is empty, but we know %s is in that country...", country, name);
			return new EmptyProxyMarker<>();
		}

		return new GroupedProxyMarker<>(nodes);
	}

}
