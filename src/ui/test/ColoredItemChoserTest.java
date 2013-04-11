package ui.test;

import java.awt.Container;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.JPanel;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import ui.selection.ColoredItemChooser;
import ui.selection.YearChooser;

public class ColoredItemChoserTest {

	public static void main(String[] args) {
		JPanel choser = new ColoredItemChooser(Arrays.asList("one", "two", "three", "four", "five", "six", "seven"));
		
		ApplicationFrame frame = new ApplicationFrame("ColoredItemChoserTest");
		
		Container container = Box.createHorizontalBox();
		frame.setContentPane(container);
		
		container.add(choser);
		container.add(new YearChooser(2008, 2012));
		
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}
}
