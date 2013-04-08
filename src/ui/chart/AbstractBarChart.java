package ui.chart;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.event.EventListenerList;

import log.LogManager;
import log.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public abstract class AbstractBarChart extends ChartPanel implements ChartMouseListener, BarMouseListener {

	private static final long serialVersionUID = 7950681964454985295L;

	public static final int WIDTH = 200, HEIGHT = 200;

	private static final String DUMMY_TITLE_KEY = "_$";
	private static final Integer DUMMY_YEAR_KEY = 0;

	private static final Logger logger = LogManager.getLogger(AbstractBarChart.class);

	private final DefaultCategoryDataset dataset;
	private transient final CategoryPlot plot;
	private transient final BarRenderer renderer;
	private transient final ConferenceBarPainter painter;

	private transient final EventListenerList barEventListeners;

	public AbstractBarChart(String title, String xAxisTitle, String yAxisTitle) {
		super(null); // set to no chart for the time being
		addChartMouseListener(this);

		barEventListeners = new EventListenerList();
		addBarMouseListener(this);

		// set sizes
		{
			Dimension size = new Dimension(WIDTH, HEIGHT);
			setMinimumSize(size);
			setMaximumSize(size);
			setPreferredSize(size);
			setSize(size);
		}

		// create dataset
		dataset = new DefaultCategoryDataset();

		// create chart
		JFreeChart chart = ChartFactory.createBarChart(title, xAxisTitle, yAxisTitle, dataset,
				PlotOrientation.VERTICAL, true, false, false);
		setChart(chart);

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);

		// configure the plot
		plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);

		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		renderer = (BarRenderer) plot.getRenderer();
		painter = new ConferenceBarPainter(true);
		renderer.setBarPainter(painter);
	}

	protected void setDomainAxisVisible(boolean visible) {
		setDomainAxisLineVisible(visible);
		setDomainAxisTickLabelsVibisle(visible);
		setDomainAxisTickMarksVisible(visible);
	}

	protected void setDomainAxisLineVisible(boolean visible) {
		plot.getDomainAxis().setAxisLineVisible(visible);
	}

	protected void setDomainAxisTickLabelsVibisle(boolean visible) {
		plot.getDomainAxis().setTickLabelsVisible(visible);
	}

	protected void setDomainAxisTickMarksVisible(boolean visible) {
		plot.getDomainAxis().setTickMarksVisible(visible);
	}

	protected void setLegendVisible(boolean visible) {
		getChart().getLegend().setVisible(visible);
	}

	protected void addBar(String title, double data) {
		dataset.addValue(data, title, new Integer(0));
	}

	protected void addDatedBar(String title, int year, double data) {
		dataset.addValue(data, title, new Integer(year));
	}

	protected void setTitleColor(String title, Color color) {
		int conferenceIndex = dataset.getRowIndex(title);

		if (conferenceIndex < 0)
			return;

		renderer.setSeriesPaint(conferenceIndex, color);
	}

	private transient CategoryItemEntity highlightedEntity;

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		ChartEntity entity = event.getEntity();
		if (entity == null || !(entity instanceof CategoryItemEntity))
			// ignore the event
			return;

		BarMouseListener[] listeners = barEventListeners.getListeners(BarMouseListener.class);
		if (listeners.length == 0)
			// no one to nofity
			return;

		CategoryItemEntity barEntity = (CategoryItemEntity) entity;

		String title = getHighlightedTitle(barEntity);
		int year = getHighlightedYear(barEntity);

		BarMouseEvent barMouseEvent = new BarMouseEvent(this, title, year);
		for (BarMouseListener listener : listeners) {
			listener.mouseClick(barMouseEvent);
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		ChartEntity entity = event.getEntity();

		if (highlightedEntity == entity)
			// nothing changed
			return;

		if (!(entity instanceof CategoryItemEntity))
			// we're not hovering over a bar, so current bar = null
			entity = null;

		CategoryItemEntity barEntity = (CategoryItemEntity) entity;

		BarMouseListener[] listeners = barEventListeners.getListeners(BarMouseListener.class);
		if (listeners.length == 0) {
			// no one to notify
			highlightedEntity = barEntity;
			return;
		}

		if (highlightedEntity != null) {
			String title = getHighlightedTitle(highlightedEntity);
			int year = getHighlightedYear(highlightedEntity);
			BarMouseEvent barMouseEvent = new BarMouseEvent(this, title, year);

			for (BarMouseListener listener : listeners) {
				listener.mouseLeave(barMouseEvent);
			}
		}

		highlightedEntity = barEntity;

		if (highlightedEntity != null) {
			String title = getHighlightedTitle(highlightedEntity);
			int year = getHighlightedYear(highlightedEntity);
			BarMouseEvent barMouseEvent = new BarMouseEvent(this, title, year);

			for (BarMouseListener listener : listeners) {
				listener.mouseEnter(barMouseEvent);
			}
		}

	}

	public boolean hasTitle(String title) {
		return dataset.getRowIndex(title) >= 0;
	}

	public boolean hasYear(int year) {
		return dataset.getColumnIndex(year) >= 0;
	}

	public void setHighlight(String title) {
		setHighlight(title, DUMMY_YEAR_KEY);
	}

	public void setHighlight(int year) {
		setHighlight(DUMMY_TITLE_KEY, year);
	}

	public void setHighlight(String title, int year) {
		int tIdx = dataset.getRowIndex(title);
		int yIdx = dataset.getColumnIndex(year);
		
		if (((title == DUMMY_TITLE_KEY) && (yIdx < 0))
				|| ((year == DUMMY_YEAR_KEY) && (tIdx < 0))) {
			clearHighlight();
			return;
		}

		logger.trace("Setting highlight to %d-%d", tIdx, yIdx);

		painter.setHighlight(yIdx, tIdx);
		getChart().fireChartChanged();
	}

	public void clearHighlight() {
		painter.clearHighlight();
		getChart().fireChartChanged();
	}

	public String getHighlightedTitle() {
		return getHighlightedTitle(highlightedEntity);
	}

	private String getHighlightedTitle(CategoryItemEntity entity) {
		if (entity == null)
			return null;

		return (String) entity.getRowKey();
	}

	public int getHighlightedYear() {
		return getHighlightedYear(highlightedEntity);
	}

	private int getHighlightedYear(CategoryItemEntity entity) {
		if (entity == null)
			return 0;

		return (Integer) entity.getColumnKey();
	}

	public void addBarMouseListener(BarMouseListener listener) {
		barEventListeners.add(BarMouseListener.class, listener);
	}

	public void removeBarMouseListener(BarMouseListener listener) {
		barEventListeners.remove(BarMouseListener.class, listener);
	}

	@Override
	public void mouseEnter(BarMouseEvent event) {
		setHighlight(event.getTitle(), event.getYear());
	}

	@Override
	public void mouseLeave(BarMouseEvent event) {
		clearHighlight();
	}

	@Override
	public void mouseClick(BarMouseEvent event) {
		// NOP
	}
}
