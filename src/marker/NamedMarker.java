package marker;

import javax.vecmath.Color4f;

import processing.core.PApplet;
import processing.core.PGraphics;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.looksgood.ani.Ani;
import de.looksgood.ani.easing.Easing;

public class NamedMarker extends SimplePointMarker {
	
	private static enum State {
		POINT, SHOWING, SHOWN, HIDING;
	}
	
	private static final float RADIUS = 44;
	private static final float ANI_DURATION = .5f;

	private static final Color4f BLACK = new Color4f(0, 0, 0, 255);
	private static final Color4f CIRCLE_COLOR = new Color4f(200, 125, 200, 200);
	
	private final String str;
	
	private final boolean animated;
	
	private float current;
	
	private State state;
	
	private int countSelectedLines;
	private boolean visible;

	public NamedMarker(String title, Location location) {
		this(title, location, true);
	}
	
	public NamedMarker(String title, Location location, boolean animated) {
		super(location);
		this.str = title;
		this.animated = animated;
		this.visible = true;
		// make the marker black
		color = highlightColor = highlightStrokeColor = strokeColor
				= 0;
		
		state = State.POINT;
		current = 0;
		countSelectedLines = 0;
	}
	
	public void setSelected(boolean selected) {
		//If the selection stays the same, only call the super method.
		if (selected == isSelected()) {
			super.setSelected(selected);
			return;
		}
		
		super.setSelected(selected);
		
		if (!animated) {
			state = (selected) ? State.SHOWN : State.POINT;
			return;
		}
		
		if (selected && (state == State.POINT || state == State.HIDING)) {
			state = State.SHOWING;
			Ani.to(this, ANI_DURATION, "current", 100, Easing.QUAD_IN, "onEnd:callBack").start();
		} else if (!selected && (state == State.SHOWING || state == State.SHOWN)) {
			state = State.HIDING;
			Ani.to(this, ANI_DURATION, "current", 0, Easing.QUAD_OUT, "onEnd:callBack").start();
		}
	}
	
	public void callBack() {
		//System.out.println("Callback!");
		if (state == State.SHOWING) {
			state = State.SHOWN;
		} else {
			state = State.POINT;
		}
	}

	public void draw(PGraphics pg, float x, float y) {
		if(!visible)
			return;
		if (state == State.POINT) {
			super.draw(pg, x, y);
			return;
		}
		
		pg.pushStyle();
		
		pg.strokeWeight(getStrokeWeight());
		Color4f circleColor = getCircleColor();
		pg.stroke(circleColor.x, circleColor.y, circleColor.z, circleColor.w);
		pg.strokeCap(PGraphics.SQUARE);
		pg.noFill();
		
		float r = getRadius();
		float phiStart = getArcStart(), phiEnd = getArcEnd();
		pg.arc(x, y, r, r, -phiEnd, -phiStart);
		pg.arc(x, y, r, r, phiStart, phiEnd);
		
		pg.fill(0, 0, 0, getTextAlpha() * 255);
		pg.text(str, x - pg.textWidth(str) / 2, y + 4);
		
		pg.popStyle();
	}
	
	public String getName(){
		return str;
	}
	
	private float getArcStart() {
		return PGraphics.PI * .3f * _getStep1();
	}
	
	private float getArcEnd() {
		return PGraphics.PI * (1f - .3f * _getStep1());
	}
	
	private float getStrokeWeight() {
		return radius + (radius - 12f) * _getStep1();
	}
	
	private float getRadius() {
		return RADIUS * _getStep1(); 
	}
	
	private float getTextAlpha() {
		return _getStep2();
	}
	
	private Color4f getCircleColor() {
		Color4f res = new Color4f();
		
		res.interpolate(BLACK, CIRCLE_COLOR, _getStep1());
		
		return res;
	}
	
	private float _getStep1() {
		return PApplet.min(80f, current) / 80f;
	}
	
	private float _getStep2() {
		return PApplet.max(0f, current - 80f) / 20f;
	}

	//@Override
	public synchronized void addSelectedLine() {
		countSelectedLines++;
		if (countSelectedLines == 1) setSelected(true);
	}

	//@Override
	public synchronized void removeSelectedLine() {
		countSelectedLines--;
		if (countSelectedLines < 0) {
			countSelectedLines = 0;
		}
		if (countSelectedLines == 0) setSelected(false);
		
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}