package main;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import log.LogManager;
import log.Logger;

public class UIConstants {

	private static final Map<Integer, Color> colors;

	private static final Logger logger = LogManager.getLogger(UIConstants.class);

	static {
		colors = new HashMap<>();
	}

	public static Color getColor(int argb) {
		return getColor((argb >> 24), (argb >> 16), (argb >> 8), (argb >> 0));
	}

	public static Color getColor(int r, int g, int b) {
		return getColor(0xFF, r, g, b);
	}

	public static Color getColor(int a, int r, int g, int b) {
		a &= 0xFF;
		r &= 0xFF;
		g &= 0xFF;
		b &= 0xFF;

		logger.trace("Color [a=%d,r=%d,g=%d,b=%d] requested", a, r, g, b);

		int argb = (a << 24) | (r << 16) | (g << 8) | (b << 0);
		if (colors.containsKey(argb)) {
			Color color = colors.get(argb);
			logger.trace("Color found, is [a=%d,r=%d,g=%d,b=%d]", color.getAlpha(), color.getRed(), color.getGreen(),
					color.getBlue());
			return colors.get(argb);
		}

		Color color = new Color(r, g, b, a);
		colors.put(argb, color);
		logger.trace("Color created, is [a=%d,r=%d,g=%d,b=%d]", color.getAlpha(), color.getRed(), color.getGreen(),
				color.getBlue());
		return color;
	}

}
