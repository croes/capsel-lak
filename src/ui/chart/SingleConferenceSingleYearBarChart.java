package ui.chart;

import java.awt.Color;

public class SingleConferenceSingleYearBarChart extends AbstractBarChart {

	private static final long serialVersionUID = -8220052886507858616L;

	private static final String X_AXIS_TITLE = "organization";
	
	public static class Data {
		private final String organization;
		private final double data;
		private final Color color;
		
		public Data(String organization, double data, Color color) {
			this.organization = organization;
			this.data = data;
			this.color = color;
		}
	}
	
	public SingleConferenceSingleYearBarChart(String title, String yAxisTitle, Data... data) {
		super(title, X_AXIS_TITLE, yAxisTitle);
		
		// hide X-axis
		setDomainAxisVisible(false);
		
		// hide the legend
		setLegendVisible(false);
		
		for (Data d : data) {
			addBar(d.organization, d.data);
			setTitleColor(d.organization, d.color);
		}
	}
	
}
