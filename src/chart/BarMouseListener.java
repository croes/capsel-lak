package chart;

import java.util.EventListener;

public interface BarMouseListener extends EventListener {

	void mouseEnter(BarMouseEvent event);
	
	void mouseLeave(BarMouseEvent event);
	
	void mouseClick(BarMouseEvent event);
}
