package ui.test;

import java.util.LinkedList;
import java.util.List;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import ui.Colors;
import ui.chart.AbstractBarChart;
import ui.chart.BarChartPanel;
import ui.chart.MultipleConferenceSingleYearBarChart;
import ui.chart.SingleConferenceMultipleYearBarChart;


public class BarChartTest {

	public static void main(String[] args) {
		new BarChartTest();
	}

	public BarChartTest() {
		// create new frame
		ApplicationFrame frame = new ApplicationFrame("BarCharTest");

		// create content pane
		BarChartPanel contentPane = new BarChartPanel();
		frame.setContentPane(contentPane);
		
		List<AbstractBarChart> charts = new LinkedList<>();
		
		// chart 1
		{
			SingleConferenceMultipleYearBarChart.Data d1 = new SingleConferenceMultipleYearBarChart.Data(2008, 4);
			SingleConferenceMultipleYearBarChart.Data d2 = new SingleConferenceMultipleYearBarChart.Data(2009, 2);
			SingleConferenceMultipleYearBarChart.Data d4 = new SingleConferenceMultipleYearBarChart.Data(2011, 3);

			charts.add(new SingleConferenceMultipleYearBarChart("SingleConferenceBarChart", "# papers",
					Colors.getColor(0xFF, 0x00, 0x00), d1, d2, d4));
		}
		
		// chart 2
		{
			MultipleConferenceSingleYearBarChart.Data d1 = new MultipleConferenceSingleYearBarChart.Data("Conf1", 4,
					Colors.getColor(0xFF, 0x00, 0x00));
			MultipleConferenceSingleYearBarChart.Data d2 = new MultipleConferenceSingleYearBarChart.Data("Conf2", 5,
					Colors.getColor(0x00, 0x00, 0xFF));
			MultipleConferenceSingleYearBarChart.Data d3 = new MultipleConferenceSingleYearBarChart.Data("Conf3", 2,
					Colors.getColor(0x00, 0xFF, 0x00));

			charts.add(new MultipleConferenceSingleYearBarChart("SingleYearBarChart", "# papers", d1, d2, d3));
		}
		
		// chart 3
		{
			MultipleConferenceSingleYearBarChart.Data d1 = new MultipleConferenceSingleYearBarChart.Data("Conf1", 4,
					Colors.getColor(0xFF, 0x00, 0x00));
			MultipleConferenceSingleYearBarChart.Data d2 = new MultipleConferenceSingleYearBarChart.Data("Conf2", 5,
					Colors.getColor(0x00, 0x00, 0xFF));
			MultipleConferenceSingleYearBarChart.Data d3 = new MultipleConferenceSingleYearBarChart.Data("Conf4", 2,
					Colors.getColor(0x00, 0xFF, 0xFF));

			charts.add(new MultipleConferenceSingleYearBarChart("SingleYearBarChart", "# papers", d1, d2, d3));
		}
		
		contentPane.setCharts(charts);

		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}

}
