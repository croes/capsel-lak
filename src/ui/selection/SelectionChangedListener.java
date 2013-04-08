package ui.selection;

import java.util.EventListener;

public interface SelectionChangedListener extends EventListener {

	void onSelected(String item);
	void onUnselected(String item);
	
}
