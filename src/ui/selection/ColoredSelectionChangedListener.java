package ui.selection;

import java.awt.Color;

public interface ColoredSelectionChangedListener extends SelectionChangedListener {

	void onColorChanged(String item, Color newColor);
}
