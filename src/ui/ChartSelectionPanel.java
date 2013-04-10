package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;
import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import log.LogManager;
import log.Logger;
import ui.chart.AbstractBarChart;
import ui.chart.BarChartPanel;
import ui.chart.BarMouseEvent;
import ui.chart.BarMouseListener;
import ui.chart.MultipleConferenceMultipleYearBarChart;
import ui.chart.MultipleConferenceSingleYearBarChart;
import ui.chart.SingleConferenceMultipleYearBarChart;
import ui.chart.SingleConferenceSingleYearBarChart;
import ui.selection.ColoredItemChooser;
import ui.selection.ColoredSelectionChangedListener;
import ui.selection.YearChooser;
import ui.selection.YearSelectionListener;

public class ChartSelectionPanel extends JPanel implements YearSelectionListener, ColoredSelectionChangedListener,
		BarMouseListener {

	private static final long serialVersionUID = -6697140724684998536L;
	
	private static final Logger logger = LogManager.getLogger(ChartSelectionPanel.class);

	public static final String Y_AXIS_TITLE = "papers";

	public static interface DataProvider {
		double getConferenceData(String conference, int year);

		double getOrganizationData(String conference, String organization, int year);

		int getMinYear();

		int getMaxYear();

		SortedSet<String> getConferences();

		boolean hasConferenceTakenPlace(String conference, int year);

		SortedSet<String> getOrganizationsForConference(String conference, int year);
	}

	public static interface Listener extends EventListener {

		void conferenceSelected(String conference);

		void conferenceUnselected(String conference);

		void yearSelected(int year);

		void yearUnselected(int year);

	}

	private static class SCSYData extends SingleConferenceSingleYearBarChart.Data {
		public SCSYData(String organization, double data, Color color) {
			super(organization, data, color);
		}
	}

	private static class SCMYData extends SingleConferenceMultipleYearBarChart.Data {
		public SCMYData(int year, double data) {
			super(year, data);
		}
	}

	private static class MCSYData extends MultipleConferenceSingleYearBarChart.Data {
		public MCSYData(String conference, double data, Color color) {
			super(conference, data, color);
		}
	}

	private static class MCMYData extends MultipleConferenceMultipleYearBarChart.Data {
		
		static class Year extends MultipleConferenceMultipleYearBarChart.Data.Year {
			public Year(int year, double data) {
				super(year, data);
			}
		}
		
		public MCMYData(String conference, Color color, Year[] years) {
			super(conference, color, years);
		}
	}

	private final DataProvider dataProvider;

	private final YearChooser years;
	private final ColoredItemChooser conferences;
	private final BarChartPanel charts;

	private final EventListenerList listeners;

	private final SortedSet<String> selectedConferences;
	private final SortedSet<Integer> selectedYears;
	
	public ChartSelectionPanel(DataProvider data) {
		this(data, true);
	}

	public ChartSelectionPanel(DataProvider data, boolean horizontal) {
		dataProvider = data;

		// FIXME YearChooser and ColoredItemChooser don't take orientation into
		// account yet
		years = new YearChooser(data.getMinYear(), data.getMaxYear());
		conferences = new ColoredItemChooser(data.getConferences());
		charts = new BarChartPanel(horizontal);

		years.addYearSelectionListener(this);
		conferences.addColoredSelectionChangedEventListener(this);
		charts.addBarMouseListener(this);

		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.X_AXIS));
		Dimension spacerDimension = new Dimension(5, 5);

		subPanel.add(conferences);
		subPanel.add(Box.createRigidArea(spacerDimension));
		subPanel.add(years);
		subPanel.add(Box.createRigidArea(spacerDimension));
		
		setLayout(new BorderLayout());
		{
			JPanel subPanelContainer = new JPanel();
			subPanelContainer.setLayout(new BoxLayout(subPanelContainer, horizontal ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
			
			subPanelContainer.add(horizontal ? Box.createVerticalGlue() : Box.createHorizontalGlue());
			subPanelContainer.add(subPanel);
			subPanelContainer.add(horizontal ? Box.createVerticalGlue() : Box.createHorizontalGlue());
			
			add(subPanelContainer, horizontal ? BorderLayout.WEST : BorderLayout.NORTH);
		}
		add(charts, BorderLayout.CENTER);

		listeners = new EventListenerList();

		selectedConferences = new TreeSet<>();
		selectedYears = new TreeSet<>();
	}
	
	public void addListener(Listener listener) {
		listeners.add(Listener.class, listener);
	}
	
	public void removeListener(Listener listener) {
		listeners.remove(Listener.class, listener);
	}

	@Override
	public void onSelected(String conference) {
		Listener[] listeners = this.listeners.getListeners(Listener.class);
		for (Listener l : listeners) {
			l.conferenceSelected(conference);
		}

		selectedConferences.add(conference);
		drawGraphs();
	}

	@Override
	public void onUnselected(String conference) {
		Listener[] listeners = this.listeners.getListeners(Listener.class);
		for (Listener l : listeners) {
			l.conferenceUnselected(conference);
		}

		selectedConferences.remove(conference);
		drawGraphs();
	}

	@Override
	public void onColorChanged(String conference, Color newColor) {
		drawGraphs();
	}

	@Override
	public void onSelected(int year) {
		Listener[] listeners = this.listeners.getListeners(Listener.class);
		for (Listener l : listeners) {
			l.yearSelected(year);
		}

		selectedYears.add(year);
		drawGraphs();
	}

	@Override
	public void onUnselected(int year) {
		Listener[] listeners = this.listeners.getListeners(Listener.class);
		for (Listener l : listeners) {
			l.yearUnselected(year);
		}

		selectedYears.remove(year);
		drawGraphs();
	}

	@Override
	public void mouseEnter(BarMouseEvent event) {
		// TODO do something with this?
	}

	@Override
	public void mouseLeave(BarMouseEvent event) {
		// TODO do something with this?
	}

	@Override
	public void mouseClick(BarMouseEvent event) {
		// TODO do something with this?
	}

	private void drawGraphs() {
		logger.debug("drawGraphs, nb years: %d, nb conferences: %d", selectedYears.size(), selectedConferences.size());
		
		if (selectedYears.isEmpty() && selectedConferences.isEmpty()) {
			logger.debug("no charts to be shown, clearing panel");
			charts.setCharts(null);
			return;
		}

		List<AbstractBarChart> newCharts = new LinkedList<>();
		
		final SortedSet<String> selectedConferences;
		if (this.selectedConferences.isEmpty())
			selectedConferences = dataProvider.getConferences();
		else
			selectedConferences = this.selectedConferences;
		
		final SortedSet<Integer> selectedYears;
		if (this.selectedYears.isEmpty()) {
			selectedYears = new TreeSet<>();
			for (int year = dataProvider.getMinYear(); year <= dataProvider.getMaxYear(); year++)
				selectedYears.add(year);
		} else
			selectedYears = this.selectedYears;

		if (selectedYears.size() == 1) {
			if (selectedConferences.size() == 1) {
				// one year, one conference

				String conference = selectedConferences.first();
				int year = selectedYears.first();

				Collection<String> orgs = dataProvider.getOrganizationsForConference(conference, year);

				SCSYData[] data = new SCSYData[orgs.size()];

				Iterator<String> orgIter = orgs.iterator();
				for (int i = 0; orgIter.hasNext(); i++) {
					String org = orgIter.next();
					data[i] = new SCSYData(org, dataProvider.getOrganizationData(conference, org, year),
							Color.lightGray);
				}

				newCharts.add(new SingleConferenceSingleYearBarChart(conference, Y_AXIS_TITLE, data));
			} else {
				// one year, multiple conferences

				int year = selectedYears.first();
				MCSYData[] data = new MCSYData[selectedConferences.size()];

				int i = 0;
				for (String conference : selectedConferences) {
					data[i] = new MCSYData(conference, dataProvider.getConferenceData(conference, year),
							conferences.getColor(conference));

					i++;
				}
				
				newCharts.add(new MultipleConferenceSingleYearBarChart(Integer.toString(year, 10), Y_AXIS_TITLE, data));
			}
		} else {
			if (selectedConferences.size() == 1) {
				// multiple years, one conference
				
				String conference = selectedConferences.first();
				SCMYData[] data = new SCMYData[selectedYears.size()];
				
				int i = 0;
				for (int year : selectedYears) {
					data[i] = new SCMYData(year, dataProvider.getConferenceData(conference, year));
					
					i++;
				}
				
				newCharts.add(new SingleConferenceMultipleYearBarChart(conference, Y_AXIS_TITLE, conferences.getColor(conference), data));
			} else {
				// multiple years, multiple conferences
				
				MCMYData[] data = new MCMYData[selectedConferences.size()];
				int nbYears = selectedYears.size();
				
				int i = 0;
				for (String conference : selectedConferences) {
					
					MCMYData.Year[] years = new MCMYData.Year[nbYears];
					
					int j = 0;
					for (int year : selectedYears) {
						years[j] = new MCMYData.Year(year, dataProvider.getConferenceData(conference, year));
						
						j++;
					}
					
					data[i] = new MCMYData(conference, conferences.getColor(conference), years);
					
					i++;
				}
				
				newCharts.add(new MultipleConferenceMultipleYearBarChart("Total", Y_AXIS_TITLE, data));
			}
		}

		logger.debug("setting new charts");
		charts.setCharts(newCharts);
	}
}
