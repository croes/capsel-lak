package marker;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

public class EdgeMarker extends SimpleLinesMarker {
	
	private Marker m1, m2;
	
	public EdgeMarker(Marker m1, Marker m2){
		super(m1.getLocation(), m2.getLocation());
		this.m1 = m1;
		this.m2 = m2;
	}
	
	public Marker getM1(){
		return m1;
	}
	
	public Marker getM2(){
		return m2;
	}
	
	@Override
	public void setSelected(boolean selected){
		this.selected = selected;
	}
	
	@Override
	public boolean isInside(UnfoldingMap map, float checkX, float checkY) {
		Location l1 = m1.getLocation();
		Location l2 = m2.getLocation();
		ScreenPosition  sposa = map.getScreenPosition(l1),
						sposb = map.getScreenPosition(l2);
		float 	xa = sposa.x,
				xb = sposb.x,
				ya = sposa.y,
				yb = sposb.y;
		if(checkX > Math.max(xa, xb)
				|| checkX < Math.min(xa, xb)
				|| checkY > Math.max(ya, yb)
				|| checkY < Math.min(ya, yb)){
			return false;
		}
		float	m = (ya - yb) / (xa - xb),
				b = ya - m * xa,
				d = (float) (Math.abs(checkY - m * checkX - b) / Math.sqrt(m*m + 1));
		return d < this.strokeWeight;
	}

}
