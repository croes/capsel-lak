package ui.marker.proxy;

import java.util.HashMap;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;

/**
 * An empty ProxyMarker implementation that returns non-null values, with
 * references to itself.
 * 
 * @param <E>
 */
public class EmptyProxyMarker<E extends Marker> implements ProxyMarker<E> {

	@Override
	public String getId() {
		return "";
	}

	@Override
	public void setId(String id) {
	}

	@Override
	public Location getLocation() {
		return new Location(0, 0);
	}

	@Override
	public void setLocation(float lat, float lng) {
	}

	@Override
	public void setLocation(Location location) {
	}

	@Override
	public double getDistanceTo(Location location) {
		return 0;
	}

	@Override
	public void setProperties(HashMap<String, Object> properties) {
	}

	@Override
	public HashMap<String, Object> getProperties() {
		return new HashMap<>();
	}

	@Override
	public boolean isInside(UnfoldingMap map, float checkX, float checkY) {
		return false;
	}

	@Override
	public void draw(UnfoldingMap map) {
	}

	@Override
	public void setSelected(boolean selected) {
	}

	@Override
	public boolean isSelected() {
		return false;
	}

	@Override
	public void setHidden(boolean hidden) {
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	@Override
	public void setColor(int color) {
	}

	@Override
	public void setStrokeColor(int color) {
	}

	@Override
	public void setStrokeWeight(int weight) {
	}

	@Override
	public Marker getMarker() {
		return getMarker(0);
	}

	@Override
	public E getOriginal() {
		return getOriginal(0);
	}

	@Override
	public int getMarkerCount() {
		return 0;
	}

	@Override
	public Marker getMarker(int i) {
		throw new IllegalArgumentException();
	}

	@Override
	public E getOriginal(int i) {
		throw new IllegalArgumentException();
	}

}