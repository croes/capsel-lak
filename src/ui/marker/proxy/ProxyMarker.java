package ui.marker.proxy;

import de.fhpotsdam.unfolding.marker.Marker;

public interface ProxyMarker<E extends Marker> extends Marker {

	Marker getMarker();
	
	Marker getMarker(int i);

	E getOriginal();
	
	E getOriginal(int i);
	
	int getMarkerCount();

}