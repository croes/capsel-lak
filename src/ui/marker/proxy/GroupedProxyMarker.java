package ui.marker.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;

public class GroupedProxyMarker<E extends Marker> implements ProxyMarker<E> {

	private final List<ProxyMarker<E>> markers;
	private final ProxyMarker<E> firstMarker;

	public GroupedProxyMarker(Collection<ProxyMarker<E>> markers) {
		if (markers == null)
			throw new NullPointerException();
		if (markers.size() == 0)
			throw new IllegalArgumentException(
					"Cannot create empty GroupedProxyMarker, use EmptyProxyMarker instead");
		this.markers = new ArrayList<>(markers);
		firstMarker = this.markers.get(0);
	}

	@Override
	public String getId() {
		return firstMarker.getId();
	}

	@Override
	public void setId(String id) {
		// NOP
	}

	@Override
	public Location getLocation() {
		return new Location(0, 0);
	}

	@Override
	public void setLocation(float lat, float lng) {
		for (Marker m : markers)
			m.setLocation(lat, lng);
	}

	@Override
	public void setLocation(Location location) {
		for (Marker m : markers) 
			m.setLocation(location);
	}

	@Override
	public double getDistanceTo(Location location) {
		return firstMarker.getDistanceTo(location);
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
		return firstMarker.isInside(map, checkX, checkY);
	}

	@Override
	public void draw(UnfoldingMap map) {
		for (Marker m : markers) {
			m.draw(map);
		}
	}

	@Override
	public void setSelected(boolean selected) {
		for (Marker m : markers) {
			m.setSelected(selected);
		}
	}

	@Override
	public boolean isSelected() {
		return firstMarker.isSelected();
	}

	@Override
	public void setHidden(boolean hidden) {
		for (Marker m : markers) {
			m.setHidden(hidden);
		}
	}

	@Override
	public boolean isHidden() {
		return firstMarker.isHidden();
	}

	@Override
	public void setColor(int color) {
		for (Marker m : markers) {
			m.setColor(color);
		}
	}

	@Override
	public void setStrokeColor(int color) {
		for (Marker m : markers)
			m.setStrokeColor(color);
	}

	@Override
	public void setStrokeWeight(int weight) {
		for (Marker m : markers)
			m.setStrokeWeight(weight);
	}

	@Override
	public Marker getMarker() {
		return firstMarker;
	}

	@Override
	public Marker getMarker(int i) {
		return markers.get(i);
	}

	@Override
	public E getOriginal() {
		return firstMarker.getOriginal();
	}

	@Override
	public E getOriginal(int i) {
		return markers.get(i).getOriginal();
	}

	@Override
	public int getMarkerCount() {
		return markers.size();
	}

}