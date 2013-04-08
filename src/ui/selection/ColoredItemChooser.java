package ui.selection;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import log.LogManager;
import log.Logger;
import ui.Colors;

public class ColoredItemChooser extends JPanel {

	private static final long serialVersionUID = -2747857830234890664L;

	public static final int NB_IN_SAME_COLUMN = 5;

	private static final Color[] colors;

	static {
		colors = new Color[] { Colors.getColor(0xFF, 0x00, 0x00), Colors.getColor(0x00, 0xFF, 0x00),
				Colors.getColor(0x00, 0x00, 0xFF), Colors.getColor(0xFF, 0xFF, 0x00),
				Colors.getColor(0xFF, 0x00, 0xFF), Colors.getColor(0x00, 0xFF, 0xFF), };
	}

	private static class ColorIcon implements Icon {

		private final Color color;

		public ColorIcon(Color color) {
			this.color = color;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(color);
			g.fillRect(x, y, getIconWidth(), getIconHeight());
		}

		@Override
		public int getIconWidth() {
			return 20;
		}

		@Override
		public int getIconHeight() {
			return 20;
		}
	}

	private static final Logger dataLogger = LogManager.getLogger(Data.class);

	private class Data extends JLabel implements MouseListener {
		private static final long serialVersionUID = -7600717323962308236L;

		private Color color;
		private boolean selected;

		Data(String title, Color color) {
			super(title);

			setColor(color, false);
			selected = false;

			addMouseListener(this);
		}

		private void setColor(Color color, boolean drawBorder) {
			this.color = color;

			setIcon(new ColorIcon(color));
			setBackground(color);

			if (drawBorder)
				setBorder(BorderFactory.createLineBorder(color));
		}

		private boolean foregroundShouldBeWhite() {
			int offsetFromGray = 0;
			offsetFromGray = color.getRed() - 127;
			offsetFromGray += color.getGreen() - 127;
			offsetFromGray += color.getBlue() - 127;

			return offsetFromGray < 0;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			dataLogger.trace("Data item %s clicked with button %d", getText(), e.getButton());

			if (e.getButton() == MouseEvent.BUTTON3) {
				dataLogger.debug("Choosing new color for item %s", getText());
				Color newColor = JColorChooser.showDialog(getRootPane(), "Pick a new color", color);
				dataLogger.debug("New color is: %s", newColor);

				if (newColor != null) {
					setColor(newColor, true);

					fireColorChangedEvent(getText(), color);
				}

				return;
			}

			selected = !selected;
			dataLogger.debug("Item %s changed selection", getText());

			setOpaque(selected);
			if (selected) {
				if (foregroundShouldBeWhite())
					setForeground(Color.white);
				else
					setForeground(Color.black);
			} else {
				setForeground(Color.black);
			}
			repaint();

			fireSelectionChangedEvent(getText(), selected);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			dataLogger.trace("Data item %s pressed", getText());
			// NOP
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			dataLogger.trace("Data item %s released", getText());
			// NOP
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			dataLogger.trace("Data item %s entered", getText());
			setBorder(BorderFactory.createLineBorder(color));
		}

		@Override
		public void mouseExited(MouseEvent e) {
			dataLogger.trace("Data item %s left", getText());
			setBorder(BorderFactory.createEmptyBorder());
		}
	}

	private final SortedMap<String, Data> data;

	private final EventListenerList listeners;

	public ColoredItemChooser(Collection<String> items) {
		data = new TreeMap<>();

		// set the layout to grid
		int nbCols = ((items.size() - 1) / NB_IN_SAME_COLUMN) + 1;
		setLayout(new GridLayout(0, nbCols));

		int i = 0;
		for (String item : items) {
			data.put(item, new Data(item, colors[i]));
			i = (i + 1) % colors.length;

			add(data.get(item));
		}

		listeners = new EventListenerList();
	}

	public boolean hasItem(String item) {
		return data.containsKey(item);
	}

	private Data get(String item) {
		return data.get(item);
	}

	public boolean isSelected(String item) {
		return hasItem(item) && get(item).selected;
	}

	public Color getColor(String item) {
		return hasItem(item) ? get(item).color : null;
	}

	public void addItemSelectionChangedEventListener(SelectionChangedListener listener) {
		listeners.add(SelectionChangedListener.class, listener);
	}

	public void removeItemSelectionChangedEventListener(SelectionChangedListener listener) {
		listeners.remove(SelectionChangedListener.class, listener);
	}

	public void addColoredItemSelectionChangedEventListener(ColoredSelectionChangedListener listener) {
		listeners.add(SelectionChangedListener.class, listener);
		listeners.add(ColoredSelectionChangedListener.class, listener);
	}

	public void removeColoredItemSelectionChangedEventListener(ColoredSelectionChangedListener listener) {
		listeners.remove(SelectionChangedListener.class, listener);
		listeners.remove(ColoredSelectionChangedListener.class, listener);
	}

	private void fireSelectionChangedEvent(String item, boolean selected) {
		SelectionChangedListener[] listeners = this.listeners.getListeners(SelectionChangedListener.class);

		if (selected) {
			for (SelectionChangedListener listener : listeners) {
				listener.onSelected(item);
			}
		} else {
			for (SelectionChangedListener listener : listeners) {
				listener.onUnselected(item);
			}
		}
	}

	private void fireColorChangedEvent(String item, Color color) {
		ColoredSelectionChangedListener[] listeners = this.listeners
				.getListeners(ColoredSelectionChangedListener.class);

		for (ColoredSelectionChangedListener listener : listeners) {
			listener.onColorChanged(item, color);
		}
	}
}
