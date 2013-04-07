package chart;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class MultipleConferenceMultipleYearBarChart extends AbstractBarChart {

	private static final long serialVersionUID = -8165076169993281954L;

	private static final String X_AXIS_TITLE = "year";
	
	public static final int MAX_MIN_YEAR = 2008;
	public static final int MIN_MAX_YEAR = 2012;
	
	public static class Data {
		
		public static class Year {
			private final int year;
			private final double data;
			
			public Year(int year, double data) {
				this.year = year;
				this.data = data;
			}
		}
		
		private final String conference;
		private final Year[] years;
		private final Color color;
		
		public Data(String conference, Color color, Year... years) {
			this.conference = conference;
			this.color = color;
			this.years = years;
		}
	}
	
	public MultipleConferenceMultipleYearBarChart(String title, String yAxisTitle, Data... data) {
		super(title, X_AXIS_TITLE, yAxisTitle);
		
		// hide the legend
		setLegendVisible(false);
		
		// hide the X-axis ticks and line
		setDomainAxisTickMarksVisible(false);
		setDomainAxisLineVisible(false);
		
		addData(data);
	}
	
	private void addData(Data[] data) {
		int minYear = MAX_MIN_YEAR, maxYear = MIN_MAX_YEAR;
		Map<Data, Map<Integer, Data.Year>> dataMap = new HashMap<>();
		
		for (Data d : data) {
			Map<Integer, Data.Year> yearMap = new HashMap<>();
			dataMap.put(d, yearMap);
			
			for (Data.Year year : d.years) {
				minYear = Math.min(minYear, year.year);
				maxYear = Math.max(maxYear, year.year);
				
				yearMap.put(year.year, year);
			}
		}
		
		for (Data d : data) {
			Map<Integer, Data.Year> yearMap = dataMap.get(d);
			
			for (int year = minYear; year <= maxYear; year++) {
				if (yearMap.containsKey(year))
					addDatedBar(d.conference, year, yearMap.get(year).data);
				else
					addDatedBar(d.conference, year, 0);
			}
			
			setTitleColor(d.conference, d.color);
		}
	}
}
