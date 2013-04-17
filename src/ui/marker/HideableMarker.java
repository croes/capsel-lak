package ui.marker;

import ui.marker.proxy.SingleProxyMarker;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;

public class HideableMarker<E extends Marker> extends SingleProxyMarker<E> {

	public HideableMarker(E original) {
		super(original);
	}
	
	@Override public void draw(UnfoldingMap map) {
		if (original.isHidden())
			return;
		original.draw(map);
	}
}
