package ui.test;

import java.util.Arrays;

import javax.swing.JPanel;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import ui.selection.ColoredItemChooser;

public class ColoredItemChoserTest {

	public static void main(String[] args) {
		JPanel choser = new ColoredItemChooser(Arrays.asList("one", "two", "three", "four", "five", "six", "seven"));
		
		ApplicationFrame frame = new ApplicationFrame("ColoredItemChoserTest");
		frame.setContentPane(choser);
		
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}
}
