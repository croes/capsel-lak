package ui.selection;

import java.util.EventListener;

public interface YearSelectionListener extends EventListener {

	void onSelected(int year);
	void onUnselected(int year);
}
