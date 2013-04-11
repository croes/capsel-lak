/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2011, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * -----------------------
 * GradientBarPainter.java
 * -----------------------
 * (C) Copyright 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 19-Jun-2008 : Version 1 (DG);
 * 15-Aug-2008 : Use outline paint and shadow paint (DG);
 *
 */

package ui.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.RectangularShape;
import java.io.Serializable;

import log.LogManager;
import log.Logger;

import org.jfree.chart.HashUtilities;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.ui.RectangleEdge;

import ui.Colors;

/**
 * An implementation of the {@link ConferenceBarPainter} interface that uses
 * several gradient fills to enrich the appearance of the bars.
 * 
 * @since 1.0.11
 */
public class ConferenceBarPainter implements BarPainter, Serializable {

	private static final long serialVersionUID = -1887799062900381296L;
	
	private static final Logger logger = LogManager.getLogger(ConferenceBarPainter.class);
	
	private static final Stroke barStroke = new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	private final int fillAlpha;
	private final int strokeAlpha;
	
	private final boolean showHighlights;
	private int highlightColumn, higlightRow;

	/**
	 * Creates a new instance.
	 */
	public ConferenceBarPainter(boolean showHighlights) {
		this(showHighlights, 0x70, 0xA0);
	}

	/**
	 * Creates a new instance.
	 * 
	 */
	public ConferenceBarPainter(boolean showHighlights, int fillAlpha, int strokeAlpha) {
		this.strokeAlpha = strokeAlpha;
		this.fillAlpha = fillAlpha;
		this.showHighlights = showHighlights;
		
		clearHighlight();
	}

	/**
	 * Paints a single bar instance.
	 * 
	 * @param g2
	 *            the graphics target.
	 * @param renderer
	 *            the renderer.
	 * @param row
	 *            the row index.
	 * @param column
	 *            the column index.
	 * @param bar
	 *            the bar
	 * @param base
	 *            indicates which side of the rectangle is the base of the bar.
	 */
	public void paintBar(Graphics2D g2, BarRenderer renderer, int row, int column, RectangularShape bar,
			RectangleEdge base) {
		if (bar.isEmpty())
			// don't draw
			return;
		
		logger.trace("Painting bar %d-%d", row, column);
		
		Paint itemPaint = renderer.getItemPaint(row, column);

		Color baseColor;
		if (itemPaint instanceof Color) {
			baseColor = (Color) itemPaint;
		} else {
			baseColor = Color.blue;
		}

		Color fillColor = Colors
				.getColor(fillAlpha, baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue());
		Color strokeColor = Colors.getColor(strokeAlpha, baseColor.getRed(), baseColor.getGreen(),
				baseColor.getBlue());
		
		if (showHighlight(row, column)) {
			fillColor = strokeColor;
		}

		g2.setPaint(fillColor);
		g2.fill(bar);

		g2.setPaint(strokeColor);
		g2.setStroke(barStroke);
		g2.draw(bar);
	}
	
	public boolean showHighlight(int row, int column) {
		if (!showHighlights)
			return false;
		
		if ((higlightRow < 0) && (highlightColumn < 0))
			return false;
		
		if ((higlightRow >= 0) && (row != higlightRow))
			return false;
		if ((highlightColumn >= 0) && (column != highlightColumn))
			return false;
		
		return true;
	}

	@Override
	public void paintBarShadow(Graphics2D g2, BarRenderer renderer, int row, int column, RectangularShape bar,
			RectangleEdge base, boolean pegShadow) {
		// not so into shadows, we are
	}
	
	public void clearHighlight() {
		setHighlight(-1, -1);
	}
	
	public void setHighlight(int column, int row) {
		higlightRow = row;
		highlightColumn = column;
	}
	
	public int getHighlightColumn() {
		return highlightColumn;
	}
	
	public int getHiglightRow() {
		return higlightRow;
	}

	/**
	 * Tests this instance for equality with an arbitrary object.
	 * 
	 * @param obj
	 *            the obj (<code>null</code> permitted).
	 * 
	 * @return A boolean.
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ConferenceBarPainter)) {
			return false;
		}
		ConferenceBarPainter that = (ConferenceBarPainter) obj;
		if (this.fillAlpha != that.fillAlpha) {
			return false;
		}
		if (this.strokeAlpha != that.strokeAlpha) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a hash code for this instance.
	 * 
	 * @return A hash code.
	 */
	public int hashCode() {
		int hash = 37;
		hash = HashUtilities.hashCode(hash, this.strokeAlpha);
		hash = HashUtilities.hashCode(hash, this.fillAlpha);
		return hash;
	}

}
