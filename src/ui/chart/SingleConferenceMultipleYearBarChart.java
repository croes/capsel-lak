package ui.chart;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class SingleConferenceMultipleYearBarChart extends AbstractBarChart {

	private static final long serialVersionUID = 927095314165226189L;

	public static final int MAX_MIN_YEAR = 2008;
	public static final int MIN_MAX_YEAR = 2012;

	private static final String X_AXIS_TITLE = "year";
	private static final String BARSERIES_ID = "0xdeadbeef";

	public static class Data {
		private final int year;
		private final double data;

		public Data(int year, double data) {
			this.year = year;
			this.data = data;
		}
	}

	public SingleConferenceMultipleYearBarChart(String title, String yAxisTitle, Color color, Data... data) {
		super(title, X_AXIS_TITLE, yAxisTitle);

		// remove the legend
		setLegendVisible(false);

		// remove the axis and ticks of the x-axis
		setDomainAxisLineVisible(false);
		setDomainAxisTickMarksVisible(false);

		// add the data
		addData(data);

		// set the color
		setTitleColor(BARSERIES_ID, color);
	}

	private void addData(Data[] data) {
		if (data == null || data.length == 0)
			return;

		Map<Integer, Data> dataMap = new HashMap<>();
		int minYear = MAX_MIN_YEAR, maxYear = MIN_MAX_YEAR;

		for (Data d : data) {
			minYear = Math.min(minYear, d.year);
			maxYear = Math.max(maxYear, d.year);

			dataMap.put(d.year, d);
		}

		for (int year = minYear; year <= maxYear; year++) {
			if (dataMap.containsKey(year))
				addDatedBar(BARSERIES_ID, year, dataMap.get(year).data);
			else
				addDatedBar(BARSERIES_ID, year, 0);
		}
	}

}
