package ui.test;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import controller.ChartSelectionController;

import ui.ChartSelectionPanel;

public class ChartSelectionPanelTest {

	public static void main(String[] args) {
		ApplicationFrame frame = new ApplicationFrame("ChartSelectionPanelTest");
		
		frame.add(new ChartSelectionPanel(new ChartSelectionController(), true));
		
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}
}
