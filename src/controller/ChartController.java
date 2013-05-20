package controller;

import ui.ChartSelectionPanel;
import ui.map.SwitchingLAKMap;
import util.task.Task;
import util.task.TaskManager;

public class ChartController implements SwitchingLAKMap.Listener {
	
	private final ChartSelectionPanel chartPanel;
	
	private final TaskManager taskManager;
	
	public ChartController(ChartSelectionPanel chartPanel) {
		this.chartPanel = chartPanel;
		
		taskManager = new TaskManager("ChartController", 1);
	}

	@Override
	public void organizationClicked(final String organization) {
		taskManager.schedule(new Task("OrganizationClicked") {
			
			@Override
			public void execute() throws Throwable {
				chartPanel.scrollToChartOfOrganization(organization);
			}
		});
	}

}
