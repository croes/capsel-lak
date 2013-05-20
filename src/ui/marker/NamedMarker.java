package ui.marker;

import javax.vecmath.Color4f;
import javax.vecmath.Point2f;

import processing.core.PApplet;
import processing.core.PGraphics;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.looksgood.ani.Ani;
import de.looksgood.ani.easing.Easing;

public class NamedMarker extends SimplePointMarker implements LineSelectableMarker {

	protected static enum State {
		POINT, SHOWING, SHOWN, HIDING;
	}

	protected static final float RADIUS = 44;
	protected static final float ANI_DURATION = .5f;

	protected static final Color4f BLACK = new Color4f(0, 0, 0, 255);
	protected static final Color4f CIRCLE_COLOR = new Color4f(0x50, 0x50, 0x80, 200);

	protected static final Color4f OVERLAY_COLOR = new Color4f(255, 255, 255, 127.5f);
	protected static final float OVERLAY_MARGIN = 2;

	protected static final float TEXT_HEIGHT = 8;

	protected final String str;

	protected final boolean animated;

	protected float current;
	protected float opacity;

	protected State state;

	protected int countSelectedLines;
	
	private Color4f color;

	public NamedMarker(String title, Location location) {
		this(title, location, true);
	}

	public NamedMarker(String title, Location location, boolean animated) {
		super(location);
		this.str = title;
		this.animated = animated;
		// make the marker black
		color = new Color4f(BLACK);

		opacity = 1;

		state = State.POINT;
		current = 0;
		countSelectedLines = 0;
	}
	
	@Override
	public void setColor(int color) {
		this.color.w = (color >> 24) & 0xFF;
		this.color.x = (color >> 16) & 0xFF;
		this.color.y = (color >>  8) & 0xFF;
		this.color.z = (color >>  0) & 0xFF;
	}

	public void setHidden(boolean hidden) {
		if (hidden == isHidden()) {
			super.setHidden(hidden);
			return;
		}

		super.setHidden(hidden);
		if (!animated)
			return;

		if (hidden)
			// delay
			Ani.to(this, ANI_DURATION, ANI_DURATION, "opacity", 0, Easing.QUAD_OUT).start();
		else
			Ani.to(this, ANI_DURATION, "opacity", 1, Easing.QUAD_IN).start();
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

	public String getText() {
		return getName();
	}

	protected void onSelectedUpdated() {
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
		if (opacity == 0)
			return;
		if (state == State.POINT) {
			pg.pushStyle();

			pg.strokeWeight(strokeWeight);
			pg.fill(color.x, color.y, color.z, color.w * opacity);
			pg.stroke(0, 0, 0, 255 * opacity);

			pg.ellipse((int) x, (int) y, radius, radius);

			pg.popStyle();

			return;
		}

		pg.pushStyle();

		pg.strokeWeight(getStrokeWeight());

		float radius = getRadius();
		float phiStart = getArcStart(), phiEnd = getArcEnd();
		String str = getText();
		float textWidth = pg.textWidth(str);
		float textAlpha = opacity * getTextAlpha();
		float overlayAlpha = opacity * getOverlayAlpha();

		// draw a semi-transparent circle

		pg.stroke(OVERLAY_COLOR.x, OVERLAY_COLOR.y, OVERLAY_COLOR.z, overlayAlpha * OVERLAY_COLOR.w);
		pg.fill(OVERLAY_COLOR.x, OVERLAY_COLOR.y, OVERLAY_COLOR.z, overlayAlpha * OVERLAY_COLOR.w);
		pg.ellipse(x, y, radius, radius);

		// draw the purple
		Color4f circleColor = getCircleColor();
		pg.stroke(circleColor.x, circleColor.y, circleColor.z, opacity * circleColor.w);
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
		// pg.textSize(TEXT_HEIGHT);
		pg.text(str, x - textWidth / 2, y + 4);

		pg.popStyle();
	}

	public String getName() {
		return str;
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

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;

		if (str.equals(o))
			return true;
		if (!(o instanceof NamedMarker))
			return false;
		return str.equals(((NamedMarker) o).str);
	}

	@Override
	public int hashCode() {
		return str.hashCode();
	}
}