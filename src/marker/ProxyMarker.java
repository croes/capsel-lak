package marker;

import java.util.HashMap;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;

public class ProxyMarker<E extends Marker> implements Marker {
	
	protected final E original;
	
	public ProxyMarker(E original) {
		this.original = original;
	}
	
	public E getOriginal() {
		return original;
	}

	@Override
	public String getId() {
		return original.getId();
	}

	@Override
	public void setId(String id) {
		original.setId(id);
	}

	@Override
	public Location getLocation() {
		return original.getLocation();
	}

	@Override
	public void setLocation(float lat, float lng) {
		original.setLocation(lat, lng);
	}

	@Override
	public void setLocation(Location location) {
		original.setLocation(location);
	}

	@Override
	public double getDistanceTo(Location location) {
		return original.getDistanceTo(location);
	}

	@Override
	public void setProperties(HashMap<String, Object> properties) {
		original.setProperties(properties);
	}

	@Override
	public HashMap<String, Object> getProperties() {
		return original.getProperties();
	}

	@Override
	public boolean isInside(UnfoldingMap map, float checkX, float checkY) {
		return original.isInside(map, checkX, checkY);
	}

	@Override
	public void draw(UnfoldingMap map) {
		original.draw(map);
	}

	@Override
	public void setSelected(boolean selected) {
		original.setSelected(selected);
	}

	@Override
	public boolean isSelected() {
		return original.isSelected();
	}

	@Override
	public void setHidden(boolean hidden) {
		original.setHidden(hidden);
	}

	@Override
	public boolean isHidden() {
		return original.isHidden();
	}

	@Override
	public void setColor(int color) {
		original.setColor(color);
	}

	@Override
	public void setStrokeColor(int color) {
		original.setStrokeColor(color);
	}

	@Override
	public void setStrokeWeight(int weight) {
		original.setStrokeWeight(weight);
	}

}
