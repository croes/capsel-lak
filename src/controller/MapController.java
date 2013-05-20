package controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import log.LogManager;
import log.Logger;
import ui.ChartSelectionPanel;
import ui.map.AbstractLAKMap;
import util.task.Task;
import util.task.TaskManager;

public class MapController implements ChartSelectionPanel.Listener {

	private static final Logger logger = LogManager.getLogger(MapController.class);

	public static interface DataProvider {

		boolean hasConferenceTakenPlace(String conference, int year);

		Collection<String> getConferences();

		int getMinYear();

		int getMaxYear();

	}

	private final Set<String> selectedConferences;
	private final Set<Integer> selectedYears;

	private final Collection<String> selectedConferenceAcronyms;

	private final DataProvider dataProvider;
	private final AbstractLAKMap<?, ?> map;

	private final TaskManager mapTaskManager;
	private transient volatile boolean updatePending;
	private final Object lock;

	public MapController(DataProvider data, AbstractLAKMap<?, ?> map) {
		dataProvider = data;
		this.map = map;

		selectedConferences = new TreeSet<>();
		selectedYears = new TreeSet<>();
		selectedConferenceAcronyms = new LinkedList<>();

		mapTaskManager = new TaskManager("MapTaskManager", 1);
		updatePending = false;
		lock = new Object();
	}

	@Override
	public void conferenceSelected(String conference) {
		selectedConferences.add(conference);

		for (int year : selectedYears) {
			if (dataProvider.hasConferenceTakenPlace(conference, year))
				selectedConferenceAcronyms.add(conference + Integer.toString(year, 10));
		}

		scheduleUpdateMap();
	}

	@Override
	public void conferenceUnselected(String conference) {
		selectedConferences.remove(conference);

		for (int year : selectedYears) {
			selectedConferenceAcronyms.remove(conference + Integer.toString(year, 10));
		}

		scheduleUpdateMap();
	}

	@Override
	public void yearSelected(int year) {
		selectedYears.add(year);

		String yearString = Integer.toString(year, 10);
		for (String conference : selectedConferences) {
			if (dataProvider.hasConferenceTakenPlace(conference, year))
				selectedConferenceAcronyms.add(conference + yearString);
		}

		scheduleUpdateMap();
	}

	@Override
	public void yearUnselected(int year) {
		selectedYears.remove(year);

		String yearString = Integer.toString(year, 10);
		for (String conference : selectedConferences) {
			selectedConferenceAcronyms.remove(conference + yearString);
		}

		scheduleUpdateMap();
	}

	private void scheduleUpdateMap() {
		synchronized (lock) {
			if (updatePending)
				return;
			updatePending = true;
		}

		mapTaskManager.schedule(new Task("MapUpdate") {
			@Override
			public void execute() throws Throwable {
				synchronized (lock) {
					updatePending = false;
				}
				updateMap();
			}
		});
	}

	private void updateMap() {
		if (selectedYears.isEmpty() && selectedConferences.isEmpty()) {
			logger.debug("No conferences or years selected, showing all data");
			map.showAllConferences();
			return;
		}

		if (selectedYears.isEmpty()) {
			logger.debug("No years selected, showing selected conference(s) (count: %d) for all years",
					selectedConferences.size());
			final Collection<String> shownConferenceAcronyms = new HashSet<>();

			for (int year = dataProvider.getMinYear(); year <= dataProvider.getMaxYear(); year++) {
				String yearString = Integer.toString(year, 10);
				for (String conference : selectedConferences) {
					if (dataProvider.hasConferenceTakenPlace(conference, year))
						shownConferenceAcronyms.add(conference + yearString);
				}
			}

			map.showConferences(shownConferenceAcronyms);
		} else if (selectedConferences.isEmpty()) {
			logger.debug("No conferences selected, showing selected year(s) (count: %d) for all conferences",
					selectedYears.size());
			final Collection<String> shownConferenceAcronyms = new HashSet<>();

			for (int year : selectedYears) {
				String yearString = Integer.toString(year, 10);
				for (String conference : dataProvider.getConferences()) {
					if (dataProvider.hasConferenceTakenPlace(conference, year))
						shownConferenceAcronyms.add(conference + yearString);
				}
			}

			map.showConferences(shownConferenceAcronyms);
		} else {
			logger.debug("Showing %d conference-year combinations", selectedConferenceAcronyms.size());
			map.showConferences(selectedConferenceAcronyms);
		}
	}
	
	private void scheduleUpdateSelectedOrg(final String organization, final boolean selected) {
		mapTaskManager.schedule(new Task("SelectedOrgUpdate"){
			@Override
			public void execute() {
				if (selected)
					map.selectOrg(organization);
				else
					map.unselectOrg(organization);
			}});
	}

	@Override
	public void orgSelected(String university) {
		scheduleUpdateSelectedOrg(university, true);
	}

	@Override
	public void orgUnselected(String university) {
		scheduleUpdateSelectedOrg(university, false);
	}

	@Override
	public void orgClicked(final String organization) {
		mapTaskManager.schedule(new Task("OrgClicked") {

			@Override
			public void execute() throws Throwable {
				map.panToOrganization(organization);
			}});
	}

}
