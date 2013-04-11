package ui.marker;

import java.util.HashMap;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;

public class ProxyMarker<E extends Marker> implements Marker {

	protected final E original;
	protected final ProxyMarker<E> originalProxy;

	public ProxyMarker(E original) {
		if (original == null)
			throw new NullPointerException();
		this.original = original;
		originalProxy = null;
	}

	public ProxyMarker(ProxyMarker<E> originalProxy) {
		if (originalProxy == null)
			throw new NullPointerException();
		original = null;
		this.originalProxy = originalProxy;
	}

	public E getOriginal() {
		return original == null ? originalProxy.getOriginal() : original;
	}

	@Override
	public String getId() {
		return original == null ? originalProxy.getId() : original.getId();
	}

	@Override
	public void setId(String id) {
		if (original == null)
			originalProxy.setId(id);
		else
			original.setId(id);
	}

	@Override
	public Location getLocation() {
		return original == null ? originalProxy.getLocation() : original.getLocation();
	}

	@Override
	public void setLocation(float lat, float lng) {
		if (original == null)
			originalProxy.setLocation(lat, lng);
		else
			original.setLocation(lat, lng);
	}

	@Override
	public void setLocation(Location location) {
		if (original == null)
			originalProxy.setLocation(location);
		else
			original.setLocation(location);
	}

	@Override
	public double getDistanceTo(Location location) {
		return original == null ? originalProxy.getDistanceTo(location) : original.getDistanceTo(location);
	}

	@Override
	public void setProperties(HashMap<String, Object> properties) {
		if (original == null)
			originalProxy.setProperties(properties);
		else
			original.setProperties(properties);
	}

	@Override
	public HashMap<String, Object> getProperties() {
		return original == null ? originalProxy.getProperties() : original.getProperties();
	}

	@Override
	public boolean isInside(UnfoldingMap map, float checkX, float checkY) {
		return original == null ? originalProxy.isInside(map, checkX, checkY) : original.isInside(map, checkX, checkY);
	}

	@Override
	public void draw(UnfoldingMap map) {
		if (original == null)
			originalProxy.draw(map);
		else
			original.draw(map);
	}

	@Override
	public void setSelected(boolean selected) {
		if (original == null)
			originalProxy.setSelected(selected);
		else
			original.setSelected(selected);
	}

	@Override
	public boolean isSelected() {
		return original == null ? originalProxy.isSelected() : original.isSelected();
	}

	@Override
	public void setHidden(boolean hidden) {
		if (original == null)
			originalProxy.setHidden(hidden);
		else
			original.setHidden(hidden);
	}

	@Override
	public boolean isHidden() {
		return original == null ? originalProxy.isHidden() : original.isHidden();
	}

	@Override
	public void setColor(int color) {
		if (original == null)
			originalProxy.setColor(color);
		else
		original.setColor(color);
	}

	@Override
	public void setStrokeColor(int color) {
		if (original == null)
			originalProxy.setStrokeColor(color);
		else
		original.setStrokeColor(color);
	}

	@Override
	public void setStrokeWeight(int weight) {
		if (original == null)
			originalProxy.setStrokeWeight(weight);
		else
		original.setStrokeWeight(weight);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		
		if (getOriginal().equals(o))
			return true;

		if (original == null && originalProxy.equals(o))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		return getOriginal().hashCode();
	}

}
