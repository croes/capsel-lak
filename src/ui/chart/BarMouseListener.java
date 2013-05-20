package ui.chart;

import java.util.EventListener;

import org.jfree.chart.ChartMouseEvent;

public interface BarMouseListener extends EventListener {

	void mouseEnter(BarMouseEvent event);
	
	void mouseLeave(BarMouseEvent event);
	
	void mouseClick(BarMouseEvent event);
	
	void mouseChartClick(ChartMouseEvent event, String chartTitle);
}
