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
import de.fhpotsdam.unfolding.utils.MapUtils;

public class SwitchingLAKMap extends AbstractLAKMap<SwitchableNamedMarker, EdgeMarker<SwitchableNamedMarker>> {

	private static final long serialVersionUID = -4973608268494925502L;

	private static final Logger logger = LogManager.getLogger(SwitchingLAKMap.class);
	
	private transient boolean showCountry;
	
	public SwitchingLAKMap(AbstractLAKMap.DataProvider data, boolean drawFPS) {
		super(data, drawFPS);
		showCountry = false;
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
		
		setShowCountry(true);
	}
	
	private void setShowCountry(boolean showCountry) {
		if (showCountry == this.showCountry)
			return;
		
		this.showCountry = showCountry;
		
		for (ProxyMarker<SwitchableNamedMarker> m : nodeMarkerManager) {
			m.getOriginal().switchMarker(showCountry);
		}
		
		// todo set the size of the edge markers
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

			storeNodeMarker(new SwitchableNamedMarker(organization, organizationLocation,
					country, countryLocation));
		}
	}

	@Override
	protected void createAllEdgeMarkers() {
		// TODO
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
		
		// TODO calculate edge data for the countries
		
		hideAllNodeMarkers();
		hideAllEdgeMarkers();
		
		for (String organization : organizations) {
			SwitchableNamedMarker marker = getNodeMarkerWithName(organization);
			if (marker != null)
				marker.setHidden(false);
			else
				logger.error("Marker for organization %s not found", organization);
		}
		
		// TODO show the right edges
	}

}
