package ui.marker.proxy;

import java.util.HashMap;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;

public class SingleProxyMarker<E extends Marker> implements ProxyMarker<E> {

	protected final E original;

	protected final Marker marker;

	public SingleProxyMarker(E original) {
		if (original == null)
			throw new NullPointerException();
		this.original = original;
		marker = original;
	}

	public SingleProxyMarker(SingleProxyMarker<E> originalProxy) {
		if (originalProxy == null)
			throw new NullPointerException();
		original = originalProxy.getOriginal();
		marker = originalProxy;
	}

	/* (non-Javadoc)
	 * @see ui.marker.ProxyMarker#getMarker()
	 */
	@Override
	public Marker getMarker() {
		return marker;
	}

	/* (non-Javadoc)
	 * @see ui.marker.ProxyMarker#getOriginal()
	 */
	@Override
	public E getOriginal() {
		return original;
	}

	@Override
	public String getId() {
		return marker.getId();
	}

	@Override
	public void setId(String id) {
		marker.setId(id);
	}

	@Override
	public Location getLocation() {
		return marker.getLocation();
	}

	@Override
	public void setLocation(float lat, float lng) {
		marker.setLocation(lat, lng);
	}

	@Override
	public void setLocation(Location location) {
		marker.setLocation(location);
	}

	@Override
	public double getDistanceTo(Location location) {
		return marker.getDistanceTo(location);
	}

	@Override
	public void setProperties(HashMap<String, Object> properties) {
		marker.setProperties(properties);
	}

	@Override
	public HashMap<String, Object> getProperties() {
		return marker.getProperties();
	}

	@Override
	public boolean isInside(UnfoldingMap map, float checkX, float checkY) {
		return marker.isInside(map, checkX, checkY);
	}

	@Override
	public void draw(UnfoldingMap map) {
		marker.draw(map);
	}

	@Override
	public void setSelected(boolean selected) {
		marker.setSelected(selected);
	}

	@Override
	public boolean isSelected() {
		return marker.isSelected();
	}

	@Override
	public void setHidden(boolean hidden) {
		marker.setHidden(hidden);
	}

	@Override
	public boolean isHidden() {
		return marker.isHidden();
	}

	@Override
	public void setColor(int color) {
		marker.setColor(color);
	}

	@Override
	public void setStrokeColor(int color) {
		marker.setStrokeColor(color);
	}

	@Override
	public void setStrokeWeight(int weight) {
		marker.setStrokeWeight(weight);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;

		if (getOriginal().equals(o))
			return true;
		if (getMarker().equals(o))
			return true;

		if (!(o instanceof SingleProxyMarker))
			return false;
		
		ProxyMarker<?> other = (ProxyMarker<?>)o;
		return getOriginal().equals(other.getOriginal());
	}

	@Override
	public int hashCode() {
		return getOriginal().hashCode();
	}

	@Override
	public Marker getMarker(int i) {
		if (i == 0)
			return getMarker();
		throw new IllegalArgumentException();
	}

	@Override
	public E getOriginal(int i) {
		if (i == 0)
			return getOriginal();
		throw new IllegalArgumentException();
	}

	@Override
	public int getMarkerCount() {
		return 1;
	}

}
