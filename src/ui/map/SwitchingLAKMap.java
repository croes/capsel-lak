package ui.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import log.LogManager;
import log.Logger;
import ui.marker.EdgeMarker;
import ui.marker.ProxyMarker;
import ui.marker.SwitchableNamedMarker;
import util.StringCouple;
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

		public Edge(ProxyMarker<Node> m1, ProxyMarker<Node> m2) {
			super(m1.getOriginal(), m2.getOriginal());

			m1.getOriginal().setProxy(m1);
			m2.getOriginal().setProxy(m2);
		}

	}

	private static final long serialVersionUID = -4973608268494925502L;

	private static final Logger logger = LogManager.getLogger(SwitchingLAKMap.class);

	private transient boolean showCountry;
	private transient Map<StringCouple, Integer> lastShownEdgeData;

	public SwitchingLAKMap(AbstractLAKMap.DataProvider data, boolean drawFPS) {
		super(data, drawFPS);
		showCountry = true;
		lastShownEdgeData = null;
	}

	@Override
	public void setup() {
		super.setup();

		logger.debug("Attaching MapEventListener to listen for zoom events");

		EventDispatcher dispatcher = MapUtils.createDefaultEventDispatcher(this, map);
		dispatcher.register(new MapEventListener() {

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

				setShowCountry(zoomLevel <= TRESHOLD);
			}

			@Override
			public String getId() {
				return map.getId();
			}
		}, "zoom", map.getId());
	}

	private void setShowCountry(boolean showCountry) {
		if (showCountry == this.showCountry)
			return;

		this.showCountry = showCountry;

		for (ProxyMarker<Node> m : nodeMarkerManager) {
			m.getOriginal().switchMarker(showCountry);
		}

		if (lastShownEdgeData == null)
			return;

		Map<StringCouple, Integer> edgeData = getCooperationData(lastShownEdgeData);

		for (ProxyMarker<Edge> m : edgeMarkerManager) {
			Integer width = edgeData.get(m.getOriginal().getIds());
			if (width == null)
				m.setHidden(true);
			else {
				if (m.isHidden()) {
					m.getOriginal().setWidth(width);
					m.setHidden(false);
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

			if (getNodeMarkerWithName(organization) != null)
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
		lastShownEdgeData = dataProvider.getAllOrganizationCooperationData();
		Map<StringCouple, Integer> cooperationData = getCooperationData(lastShownEdgeData);

		for (StringCouple orgs : lastShownEdgeData.keySet()) {
			ProxyMarker<Node> m1 = getNodeMarkerWithName(orgs.getString1());
			ProxyMarker<Node> m2 = getNodeMarkerWithName(orgs.getString2());

			if (m1 == null || m2 == null)
				continue;

			Edge edge = new Edge(m1, m2);

			Integer width = cooperationData.get(orgs);
			if (width == null) {
				edge.setHidden(true);
			} else {
				edge.setWidth(width);
			}

			storeEdgeMarker(orgs, edge);
		}
	}

	@Override
	public void showAllEdgeMarkers() {
		lastShownEdgeData = dataProvider.getAllOrganizationCooperationData();
		Map<StringCouple, Integer> cooperationData = getCooperationData(lastShownEdgeData);

		for (StringCouple orgs : lastShownEdgeData.keySet()) {
			ProxyMarker<Edge> edge = getEdgeMarkerForNames(orgs);

			if (edge == null) {
				logger.error("Cannot find edge marker for organization couple %s", orgs);
				continue;
			}

			Integer width = cooperationData.get(orgs);
			if (width == null) {
				edge.setHidden(true);
			} else {
				edge.setHidden(false);
				edge.getOriginal().setWidthAnimated(width);
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

		lastShownEdgeData = edgeData;
		edgeData = getCooperationData(edgeData);

		// hide all markers

		hideAllNodeMarkers();
		hideAllEdgeMarkers();

		// show the right markers

		for (String organization : organizations) {
			Marker marker = getNodeMarkerWithName(organization);
			if (marker != null)
				marker.setHidden(false);
			else
				logger.error("Marker for organization %s not found", organization);
		}

		for (Map.Entry<StringCouple, Integer> edge : edgeData.entrySet()) {
			ProxyMarker<Edge> edgeMarker = getEdgeMarkerForNames(edge.getKey());

			if (edgeMarker == null) {
				logger.error("Edge marker for organization couple %s not found", edge.getKey());
				continue;
			}

			edgeMarker.setHidden(false);
			edgeMarker.getOriginal().setWidthAnimated(edge.getValue());
		}
	}

	private Map<StringCouple, Integer> getCooperationData(Map<StringCouple, Integer> data) {
		if (!showCountry)
			return data;

		Map<StringCouple, Integer> countryData = new HashMap<>();
		Map<StringCouple, Integer> result = new HashMap<>();

		for (Map.Entry<StringCouple, Integer> dataEntry : data.entrySet()) {
			StringCouple orgs = dataEntry.getKey();
			StringCouple countries = new StringCouple(dataProvider.getCountry(orgs.getString1()),
					dataProvider.getCountry(orgs.getString2()));

			if (countryData.containsKey(countries)) {
				countryData.put(countries, countryData.get(countries) + dataEntry.getValue());
			} else {
				countryData.put(countries, dataEntry.getValue());
			}
		}

		for (Map.Entry<StringCouple, Integer> dataEntry : data.entrySet()) {
			StringCouple orgs = dataEntry.getKey();
			StringCouple countries = new StringCouple(dataProvider.getCountry(orgs.getString1()),
					dataProvider.getCountry(orgs.getString2()));

			if (!countries.sameStrings())
				result.put(orgs, countryData.get(countries));
		}

		return result;
	}

}
