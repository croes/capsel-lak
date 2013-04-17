package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import util.task.Task;
import util.task.TaskManager;

public class ChartSelectionPanel extends JPanel implements YearSelectionListener, ColoredSelectionChangedListener,
		BarMouseListener, MouseListener {

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

		SortedSet<String> getOrganizationsForConferences(Collection<String> conferences, Collection<Integer> years);

		SortedSet<String> getOrganizationsForConferences(Collection<String> conferences, int year);

		SortedSet<String> getOrganizationsForConferences(String conference, Collection<Integer> years);
	}

	public static interface Listener extends EventListener {

		void conferenceSelected(String conference);

		void conferenceUnselected(String conference);

		void yearSelected(int year);

		void yearUnselected(int year);
		
		void orgSelected(String university);
		
		void orgUnselected(String university);

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

	private final TaskManager updateTaskManager;
	private volatile transient boolean updatePending;
	private final Object lock;

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
			subPanelContainer.setLayout(new BoxLayout(subPanelContainer, horizontal ? BoxLayout.Y_AXIS
					: BoxLayout.X_AXIS));

			subPanelContainer.add(horizontal ? Box.createVerticalGlue() : Box.createHorizontalGlue());
			subPanelContainer.add(subPanel);
			subPanelContainer.add(horizontal ? Box.createVerticalGlue() : Box.createHorizontalGlue());

			add(subPanelContainer, horizontal ? BorderLayout.WEST : BorderLayout.NORTH);
		}
		add(charts, BorderLayout.CENTER);

		listeners = new EventListenerList();

		selectedConferences = new TreeSet<>();
		selectedYears = new TreeSet<>();

		updateTaskManager = new TaskManager("ChartUpdater", 1);
		updatePending = false;
		lock = new Object();
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
		scheduleDrawGraphs();
	}

	@Override
	public void onUnselected(String conference) {
		Listener[] listeners = this.listeners.getListeners(Listener.class);
		for (Listener l : listeners) {
			l.conferenceUnselected(conference);
		}

		selectedConferences.remove(conference);
		scheduleDrawGraphs();
	}

	@Override
	public void onColorChanged(String conference, Color newColor) {
		scheduleDrawGraphs();
	}

	@Override
	public void onSelected(int year) {
		Listener[] listeners = this.listeners.getListeners(Listener.class);
		for (Listener l : listeners) {
			l.yearSelected(year);
		}

		selectedYears.add(year);
		scheduleDrawGraphs();
	}

	@Override
	public void onUnselected(int year) {
		Listener[] listeners = this.listeners.getListeners(Listener.class);
		for (Listener l : listeners) {
			l.yearUnselected(year);
		}

		selectedYears.remove(year);
		scheduleDrawGraphs();
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

	private AbstractBarChart createSCMYGraph(String organization, String conference, Collection<Integer> years) {
		SCMYData[] data = new SCMYData[years.size()];

		int i = 0;
		for (int year : years) {
			data[i] = new SCMYData(year, dataProvider.getOrganizationData(conference, organization, year));

			i++;
		}

		return new SingleConferenceMultipleYearBarChart(organization, Y_AXIS_TITLE, conferences.getColor(conference),
				data);

	}

	private AbstractBarChart createMCSYGraph(String organization, Collection<String> conferences, int year) {
		MCSYData[] data = new MCSYData[conferences.size()];

		int i = 0;
		for (String conference : conferences) {
			data[i] = new MCSYData(conference, dataProvider.getOrganizationData(conference, organization, year),
					this.conferences.getColor(conference));

			i++;
		}

		return new MultipleConferenceSingleYearBarChart(organization, Y_AXIS_TITLE, data);
	}

	private AbstractBarChart createMCMYGraph(String organization, Collection<String> conferences,
			Collection<Integer> years) {
		MCMYData[] data = new MCMYData[conferences.size()];
		int nbYears = years.size();

		int i = 0;
		for (String conference : conferences) {

			MCMYData.Year[] yearDatas = new MCMYData.Year[nbYears];

			int j = 0;
			for (int year : years) {
				yearDatas[j] = new MCMYData.Year(year, dataProvider.getOrganizationData(conference, organization, year));

				j++;
			}

			data[i] = new MCMYData(conference, this.conferences.getColor(conference), yearDatas);

			i++;
		}

		return new MultipleConferenceMultipleYearBarChart(organization, Y_AXIS_TITLE, data);
	}

	private void scheduleDrawGraphs() {
		synchronized (lock) {
			if (updatePending)
				return;
			updatePending = true;
		}

		updateTaskManager.schedule(new Task("drawGraphs") {
			@Override
			public void execute() throws Throwable {
				synchronized(lock) {
					updatePending = false;
				}
				drawGraphs();
			}
		});
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
				AbstractBarChart chart = new SingleConferenceSingleYearBarChart(conference, Y_AXIS_TITLE, data);
				chart.addMouseListener(this);
				newCharts.add(chart);
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
				AbstractBarChart chart = new MultipleConferenceSingleYearBarChart("Total", Y_AXIS_TITLE, data);
				chart.addMouseListener(this);
				newCharts.add(chart);

				for (String org : dataProvider.getOrganizationsForConferences(selectedConferences, year)){
					AbstractBarChart subChart = createMCSYGraph(org, selectedConferences, year); 
					subChart.addMouseListener(this);
					newCharts.add(subChart);
				}
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

				AbstractBarChart chart = new SingleConferenceMultipleYearBarChart("Total", Y_AXIS_TITLE, conferences
						.getColor(conference), data); 
				chart.addMouseListener(this);
				newCharts.add(chart);

				for (String org : dataProvider.getOrganizationsForConferences(conference, selectedYears)){
					AbstractBarChart subChart = createSCMYGraph(org, conference, selectedYears);
					subChart.addMouseListener(this);
					newCharts.add(subChart);
				}
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

				AbstractBarChart chart = new MultipleConferenceMultipleYearBarChart("Total", Y_AXIS_TITLE, data);
				chart.addMouseListener(this);
				newCharts.add(chart);

				for (String org : dataProvider.getOrganizationsForConferences(selectedConferences, selectedYears)){
					AbstractBarChart subChart = createMCMYGraph(org, selectedConferences, selectedYears);
					subChart.addMouseListener(this);
					newCharts.add(subChart);
				}
			}
		}

		logger.debug("setting new charts");
		charts.setCharts(newCharts);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//NOP
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		AbstractBarChart enteredChart = (AbstractBarChart) e.getSource();
		logger.debug("mouse entered: %s", enteredChart.getChart().getTitle().getText());
		Listener[] listeners = this.listeners.getListeners(Listener.class);
		for (Listener l : listeners) {
			l.orgSelected(enteredChart.getFullTitle());
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		AbstractBarChart exitedChart = (AbstractBarChart) e.getSource();
		logger.debug("mouse exited: %s",exitedChart.getChart().getTitle().getText());
		Listener[] listeners = this.listeners.getListeners(Listener.class);
		for (Listener l : listeners) {
			l.orgUnselected(exitedChart.getFullTitle());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//NOP
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		//NOP
	}
}
