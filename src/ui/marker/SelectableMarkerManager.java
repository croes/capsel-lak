package ui.marker;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ui.marker.proxy.SingleProxyMarker;
import util.ConcurrentHashSet;
import de.fhpotsdam.unfolding.marker.AbstractMarkerManager;
import de.fhpotsdam.unfolding.marker.Marker;

/**
 * An {@link AbstractMarkerManager} implementation that draws the {@link Marker}
 * s with <tt>{@link Marker#isSelected()} == true</tt> last.
 */
public class SelectableMarkerManager<E extends Marker> extends AbstractMarkerManager<SingleProxyMarker<E>> {

	private class SetIterator implements Iterator<SingleProxyMarker<E>> {

		private boolean unselectedDone;
		private Iterator<MarkerWrapper> it;

		public SetIterator() {
			it = unselected.iterator();
			unselectedDone = false;
		}

		@Override
		public boolean hasNext() {
			if (it.hasNext())
				return true;

			if (!unselectedDone) {
				unselectedDone = true;
				it = selected.iterator();
				return it.hasNext();
			}

			return false;
		}

		@Override
		public MarkerWrapper next() {
			return it.next();
		}

		@Override
		public void remove() {
			it.remove();
		}

	}

	private class OriginalSetIterator implements Iterator<E> {

		private Iterator<SingleProxyMarker<E>> it = new SetIterator();

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public E next() {
			return it.next().getOriginal();
		}

		@Override
		public void remove() {
			it.remove();
		}

	}

	public class MarkerWrapper extends SingleProxyMarker<E> {

		public MarkerWrapper(E original) {
			super(original);
		}

		public MarkerWrapper(SingleProxyMarker<E> origProxy) {
			super(origProxy);
		}

		public void setSelected(boolean s) {
			if (s == isSelected())
				return;

			super.setSelected(s);
			if (s) {
				unselected.remove(this);
				selected.add(this);
			} else {
				selected.remove(this);
				unselected.add(this);
			}
		}
	}

	private final Set<MarkerWrapper> selected, unselected;

	public SelectableMarkerManager() {
		selected = new ConcurrentHashSet<>();
		unselected = new ConcurrentHashSet<>();
	}

	@Override
	public Collection<SingleProxyMarker<E>> getMarkers() {
		Set<SingleProxyMarker<E>> all = new HashSet<>();
		all.addAll(selected);
		all.addAll(unselected);
		return all;
	}

	@Override
	public void addMarkers(Collection<SingleProxyMarker<E>> markers) {
		for (SingleProxyMarker<E> o : markers) {
			MarkerWrapper m = new MarkerWrapper(o);
			if (m.isSelected())
				selected.add(m);
			else
				unselected.add(m);
		}
	}

	public void addOriginalMarkers(Collection<E> markers) {
		for (E o : markers) {
			MarkerWrapper m = new MarkerWrapper(o);
			if (m.isSelected())
				selected.add(m);
			else
				unselected.add(m);
		}
	}

	@Override
	public boolean addMarker(SingleProxyMarker<E> marker) {
		MarkerWrapper m = new MarkerWrapper(marker);
		if (m.isSelected())
			return selected.add(m);
		else
			return unselected.add(m);
	}
	
	public SingleProxyMarker<E> addOriginalMarker(E marker) {
		MarkerWrapper m = new MarkerWrapper(marker);
		if (m.isSelected())
			selected.add(m);
		else
			unselected.add(m);
		return m;
	}

	@Override
	public void clearMarkers() {
		selected.clear();
		unselected.clear();
	}

	@Override
	public boolean removeMarker(SingleProxyMarker<E> marker) {
		return unselected.remove(marker) || selected.remove(marker);
	}

	@Override
	public void setMarkers(Collection<SingleProxyMarker<E>> markers) {
		clearMarkers();
		if (markers != null)
			addMarkers(markers);
	}

	@Override
	public Iterator<SingleProxyMarker<E>> iterator() {
		return new SetIterator();
	}

	public Iterator<E> originalIterator() {
		return new OriginalSetIterator();
	}

}
