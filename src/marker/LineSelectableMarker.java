package marker;

import de.fhpotsdam.unfolding.marker.Marker;

public interface LineSelectableMarker extends Marker {

	void addSelectedLine();
	
	void removeSelectedLine();
}
