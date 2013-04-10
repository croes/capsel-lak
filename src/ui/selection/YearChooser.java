package ui.selection;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import log.LogManager;
import log.Logger;
import ui.Colors;

public class YearChooser extends JPanel {
	
	private static final long serialVersionUID = 161739879163203879L;

	private static final Logger dataLogger = LogManager.getLogger(Data.class);
	private class Data extends JLabel implements MouseListener {
		
		private static final long serialVersionUID = 7852664301535798611L;
		private final int year;
		private boolean selected;
		
		public Data(int year) {
			super(Integer.toString(year, 10));
			addMouseListener(this);
			
			this.year = year;
			selected = false;
			
			setOpaque(false);
			setBackground(Colors.highlightColor);

			setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// only left clicks
			if (e.getButton() != MouseEvent.BUTTON1)
				return;
			
			selected = !selected;
			setOpaque(selected);
			repaint();
			fireSelectionChangedEvent(year, selected);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			dataLogger.trace("mousePressed @ year %d", year);
			// NOP
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			dataLogger.trace("mouseReleased @ year %d", year);
			// NOP
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			dataLogger.trace("mouseEntered @ year %d", year);
			setBorder(BorderFactory.createLineBorder(Colors.highlightColor, 1));
		}

		@Override
		public void mouseExited(MouseEvent e) {
			dataLogger.trace("mouseExited @ year %d", year);
			setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		}
	}
	
	private final Data[] data;
	private final int yearMin;
	private final EventListenerList listeners;

	public YearChooser(int yearMin, int yearMax) {
		if (yearMax < yearMin || yearMin <= 0 || yearMax <= 0)
			throw new IllegalArgumentException(String.format("Illegal year values: min=%d max=%d", yearMin, yearMax));
		
		this.yearMin = yearMin;
		data = new Data[(yearMax - yearMin) + 1];
		listeners = new EventListenerList();
		
		setLayout(new GridLayout(ColoredItemChooser.NB_IN_SAME_COLUMN, 0));
		
		for (int year = yearMin; year <= yearMax; year++) {
			Data d = new Data(year);
			data[year - yearMin] = d;
			add(d);
		}
	}
	
	public void addYearSelectionListener(YearSelectionListener listener) {
		listeners.add(YearSelectionListener.class, listener);
	}
	
	public void removeYearSelectionListener(YearSelectionListener listener) {
		listeners.remove(YearSelectionListener.class, listener);
	}
	
	private void fireSelectionChangedEvent(int year, boolean selected) {
		YearSelectionListener[] listeners = this.listeners.getListeners(YearSelectionListener.class);
		
		if (selected) {
			for (YearSelectionListener listener : listeners)
				listener.onSelected(year);
		} else {
			for (YearSelectionListener listener : listeners)
				listener.onUnselected(year);
		}
	}
	
	public boolean isSelected(int year) {
		if ((year < yearMin) || ((year - yearMin) >= data.length))
			throw new IllegalArgumentException(String.format("Invalid year: %d", year));
		
		return data[year - yearMin].selected;
	}
}
