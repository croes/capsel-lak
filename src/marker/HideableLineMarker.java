package marker;

import processing.core.PGraphics;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;

public class HideableLineMarker extends SimpleLinesMarker{
	
	private boolean visible;
	
	public HideableLineMarker(Location start, Location end){
		super(start, end);
		this.visible = true;
	}
	
	public void draw(PGraphics pg, float x, float y){
		if(visible)
			super.draw(pg, x, y);
	}
	
	public void setVisible(boolean visible){
		this.visible = visible;
	}

}
