package marker;

import javax.vecmath.Color4f;
import javax.vecmath.Point2f;

import processing.core.PApplet;
import processing.core.PGraphics;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.looksgood.ani.Ani;
import de.looksgood.ani.easing.Easing;

public class SwitchableNamedMarker extends SimplePointMarker implements LineSelectableMarker {

	private static enum State {
		POINT, SHOWING, SHOWN, HIDING;
	}
	
	private static enum SwitchState {
		ONE, TO_TWO, TWO, TO_ONE;
	}

	private static final float RADIUS = 44;
	private static final float ANI_DURATION = .5f;

	private static final Color4f BLACK = new Color4f(0, 0, 0, 255);
	private static final Color4f CIRCLE_COLOR = new Color4f(200, 125, 200, 200);

	private static final Color4f OVERLAY_COLOR = new Color4f(255, 255, 255, 127.5f);
	private static final float OVERLAY_MARGIN = 2;

	private static final float TEXT_HEIGHT = 8;

	private final String str_one;
	private final String str_two;
	
	private final Location loc_one;
	private final Location loc_two;

	private final boolean animated;

	private float current;
	private float switchCurrent;

	private State state;
	private SwitchState switchState;

	private int countSelectedLines;

	public SwitchableNamedMarker(String s1, Location l1, String s2, Location l2) {
		this(s1, l1, s2, l2, true);
	}

	public SwitchableNamedMarker(String s1, Location l1, String s2, Location l2, boolean animated) {
		super(new Location(l1));
		
		str_one = s1;
		str_two = s2;
		
		loc_one = l1;
		loc_two = l2;
		
		this.animated = animated;
		// make the marker black
		color = highlightColor = highlightStrokeColor = strokeColor = 0;

		state = State.POINT;
		current = 0;
		
		switchState = SwitchState.ONE;
		switchCurrent = 0;
		
		countSelectedLines = 0;
	}

	public void setSelected(boolean selected) {
		// If the selection stays the same, only call the super method.
		if (selected == isSelected()) {
			super.setSelected(selected);
			return;
		}

		super.setSelected(selected);
		onSelectedUpdated();
	}

	private void onSelectedUpdated() {
		boolean selected = this.selected || (countSelectedLines > 0);

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

	@SuppressWarnings("unused")
	private void callBack() {
		if (state == State.SHOWING) {
			state = State.SHOWN;
		} else {
			state = State.POINT;
		}
	}

	public void draw(PGraphics pg, float x, float y) {
		if (switchState != SwitchState.ONE && switchState != SwitchState.TWO) {
			location.toString();
		}
		if (isHidden())
			return;
		if (state == State.POINT) {
			super.draw(pg, x, y);
			return;
		}

		pg.pushStyle();

		pg.strokeWeight(getStrokeWeight());

		float radius = getRadius();
		float phiStart = getArcStart(), phiEnd = getArcEnd();
		String str = str();
		float textWidth = pg.textWidth(str);
		float textAlpha = getTextAlpha();
		float overlayAlpha = getOverlayAlpha();

		// draw a semi-transparent circle

		pg.stroke(OVERLAY_COLOR.x, OVERLAY_COLOR.y, OVERLAY_COLOR.z, overlayAlpha * OVERLAY_COLOR.w);
		pg.fill(OVERLAY_COLOR.x, OVERLAY_COLOR.y, OVERLAY_COLOR.z, overlayAlpha * OVERLAY_COLOR.w);
		pg.ellipse(x, y, radius, radius);

		// draw the purple
		Color4f circleColor = getCircleColor();
		pg.stroke(circleColor.x, circleColor.y, circleColor.z, circleColor.w);
		pg.strokeCap(PGraphics.SQUARE);
		pg.noFill();

		pg.arc(x, y, radius, radius, -phiEnd, -phiStart);
		pg.arc(x, y, radius, radius, phiStart, phiEnd);

		// draw overlay for the text
		pg.fill(OVERLAY_COLOR.x, OVERLAY_COLOR.y, OVERLAY_COLOR.z, textAlpha * OVERLAY_COLOR.w);
		pg.noStroke();
		pg.rect(x - textWidth / 2 - OVERLAY_MARGIN, y - TEXT_HEIGHT / 2 - OVERLAY_MARGIN, textWidth + 2
				* OVERLAY_MARGIN, TEXT_HEIGHT + 2 * OVERLAY_MARGIN);

		// draw the text
		pg.fill(0, 0, 0, textAlpha * 255);
		//pg.textSize(TEXT_HEIGHT);
		pg.text(str, x - textWidth / 2, y + 4);

		pg.popStyle();
	}

	public String getName() {
		return str_one;
	}

	private float getOverlayAlpha() {
		return _getStep1();
	}

	private float getArcStart() {
		return PGraphics.PI * .3f * _getStep1();
	}

	private float getArcEnd() {
		return PGraphics.PI * (1f - .3f * _getStep1());
	}

	private float getStrokeWeight() {
		return radius + (12f - radius) * _getStep1();
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

	// @Override
	public synchronized void addSelectedLine() {
		countSelectedLines++;
		if (countSelectedLines == 1)
			onSelectedUpdated();
	}

	// @Override
	public synchronized void removeSelectedLine() {
		countSelectedLines--;
		if (countSelectedLines < 0) {
			countSelectedLines = 0;
		}
		if (countSelectedLines == 0)
			onSelectedUpdated();
	}

	@Override
	public boolean isInside(float checkX, float checkY, float x, float y) {
		if (state == State.POINT)
			return super.isInside(checkX, checkY, x, y);

		Point2f pos = new Point2f(x, y);
		return pos.distance(new Point2f(checkX, checkY)) < PApplet.abs(getRadius() - getStrokeWeight());
	}
	
	private String str() {
		if (switchState == SwitchState.TWO)
			return str_two;
		return str_one;
	}
	
	@SuppressWarnings("unused")
	private void switchCallback() {
		if (switchState == SwitchState.TO_TWO)
			switchState = SwitchState.TWO;
		else
			switchState = SwitchState.ONE;
	}
	
	public void switchMarker(boolean two) {
		if (two && (switchState == SwitchState.TWO || switchState == SwitchState.TO_TWO))
			return;
		if (!two && (switchState == SwitchState.ONE || switchState == SwitchState.TO_ONE))
			return;
		
		if (!animated) {
			switchState = two ? SwitchState.TWO : SwitchState.ONE;
			switchCurrent = two ? 100 : 0;
			return;
		}
		
		if (two) {
			switchState = SwitchState.TO_TWO;
			Ani.to(this, ANI_DURATION, "switchCurrent", 100, Easing.LINEAR, "onUpdate:updateLocation,onEnd:switchCallback").start();
		} else {
			switchState = SwitchState.TO_ONE;
			Ani.to(this, ANI_DURATION, "switchCurrent", 0, Easing.LINEAR, "onUpdate:updateLocation,onEnd:switchCallback").start();
		}
	}
	
	@SuppressWarnings("unused")
	private void updateLocation() {
		switch(switchState) {
		case ONE:
			location.set(loc_one);
			break;
		case TWO:
			location.set(loc_two);
			break;
		default:
			location.set(
					(loc_two.x * switchCurrent + loc_one.x * (100 - switchCurrent)) / 100f,
					(loc_two.y * switchCurrent + loc_one.y * (100 - switchCurrent)) / 100f,
					0
					);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((str_one == null) ? 0 : str_one.hashCode());
		result = prime * result + ((str_two == null) ? 0 : str_two.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SwitchableNamedMarker other = (SwitchableNamedMarker) obj;
		if (str_one == null) {
			if (other.str_one != null)
				return false;
		} else if (!str_one.equals(other.str_one))
			return false;
		if (str_two == null) {
			if (other.str_two != null)
				return false;
		} else if (!str_two.equals(other.str_two))
			return false;
		return true;
	}
	
	
}