package ui.chart;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;

public class BarChartPanel extends JPanel implements BarMouseListener {

	private static final long serialVersionUID = 1251298766687293859L;

	public static final int SPACER = 5;

	private static final Component createSpacer() {
		return Box.createRigidArea(new Dimension(SPACER, SPACER));
	}

	private final List<AbstractBarChart> charts;

	private final EventListenerList eventListeners;

	private final JPanel contentPane;

	public BarChartPanel() {
		this(true);
	}

	public BarChartPanel(boolean horizontal) {
		charts = new LinkedList<>();
		eventListeners = new EventListenerList();

		contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, horizontal ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));

		setLayout(new GridLayout(1, 1));
		if (horizontal)
			add(new JScrollPane(contentPane, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		else
			add(new JScrollPane(contentPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		
		// set the minimum, preferred and maximum size
		
		int minHeight, prefHeight, maxHeight;
		minHeight = prefHeight = maxHeight = AbstractBarChart.HEIGHT;
		int minWidth, prefWidth, maxWidth;
		minWidth = prefWidth = maxWidth = AbstractBarChart.WIDTH;
		if (horizontal) {
			JScrollBar scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
			minHeight += scrollBar.getPreferredSize().height;
			maxHeight = prefHeight = minHeight;
			
			prefWidth *= 2;
			maxWidth = Integer.MAX_VALUE;
		} else {
			JScrollBar scrollBar = new JScrollBar(JScrollBar.VERTICAL);
			minWidth += scrollBar.getPreferredSize().width;
			maxWidth = prefWidth = minWidth;
			
			prefHeight *= 2;
			maxHeight = Integer.MAX_VALUE;
		}
		
		setMinimumSize(new Dimension(minWidth, minHeight));
		setPreferredSize(new Dimension(prefWidth, prefHeight));
		setMaximumSize(new Dimension(maxWidth, maxHeight));
	}
	
	public void addChart(AbstractBarChart chart) {
		addChart2(chart);
		
		revalidate();
		repaint();
	}

	private void addChart2(AbstractBarChart chart) {
		if (!charts.isEmpty()) {
			contentPane.add(createSpacer());
		}
		contentPane.add(chart);
		charts.add(chart);

		addBarMouseListener(chart);
		chart.addBarMouseListener(this);
	}

	private void removeAllCharts() {
		contentPane.removeAll();

		for (AbstractBarChart chart : charts) {
			removeBarMouseListener(chart);
			chart.removeBarMouseListener(this);
		}
		charts.clear();
	}

	public void setCharts(List<AbstractBarChart> charts) {
		removeAllCharts();

		if (charts != null) {
			for (AbstractBarChart c : charts) {
				addChart2(c);
			}
		}

		revalidate();
		repaint();
	}

	public void addBarMouseListener(BarMouseListener listener) {
		eventListeners.add(BarMouseListener.class, listener);
	}

	public void removeBarMouseListener(BarMouseListener listener) {
		eventListeners.remove(BarMouseListener.class, listener);
	}

	@Override
	public void mouseEnter(BarMouseEvent event) {
		for (BarMouseListener listener : eventListeners.getListeners(BarMouseListener.class)) {
			if (listener != event.getSource())
				listener.mouseEnter(event);
		}
	}

	@Override
	public void mouseLeave(BarMouseEvent event) {
		for (BarMouseListener listener : eventListeners.getListeners(BarMouseListener.class)) {
			if (listener != event.getSource())
				listener.mouseLeave(event);
		}
	}

	@Override
	public void mouseClick(BarMouseEvent event) {
		for (BarMouseListener listener : eventListeners.getListeners(BarMouseListener.class)) {
			if (listener != event.getSource())
				listener.mouseClick(event);
		}
	}

	public void highlightAll(String title, int year) {
		for (AbstractBarChart chart : charts)
			chart.setHighlight(title, year);
	}

	public void highlightAll(String title) {
		for (AbstractBarChart chart : charts)
			chart.setHighlight(title);
	}

	public void highlightAll(int year) {
		for (AbstractBarChart chart : charts)
			chart.setHighlight(year);
	}

	public void clearAllHighlight() {
		for (AbstractBarChart chart : charts)
			chart.clearHighlight();
	}
}
