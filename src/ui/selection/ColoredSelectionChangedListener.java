package ui.selection;

import java.awt.Color;
import java.util.EventListener;

public interface ColoredSelectionChangedListener extends EventListener {

	void onSelected(String item);
	void onUnselected(String item);
	
	void onColorChanged(String item, Color newColor);
}
