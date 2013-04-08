package ui.chart;

import java.awt.Color;

public class MultipleConferenceSingleYearBarChart extends AbstractBarChart {
	
	private static final long serialVersionUID = 5799167199894518303L;

	private static final String X_AXIS_TITLE = "conference";
	
	public static class Data {
		private final String conference;
		private final double data;
		private final Color color;

		public Data(String conference, double data, Color color) {
			this.conference = conference;
			this.data = data;
			this.color = color;
		}
	}
	
	public MultipleConferenceSingleYearBarChart(String title, String yAxisTitle, Data... data) {
		super(title, X_AXIS_TITLE, yAxisTitle);
		
		// hide entire X-axis
		setDomainAxisVisible(false);
		
		// hide the legend
		setLegendVisible(false);
		
		for (Data d : data) {
			addBar(d.conference, d.data);
			setTitleColor(d.conference, d.color);
		}
	}
}
