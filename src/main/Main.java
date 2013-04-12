package main;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import org.jfree.ui.RefineryUtilities;

import log.LogManager;
import log.Logger;
import processing.core.PApplet;
import ui.ChartSelectionPanel;
import ui.map.AbstractLAKMap;
import ui.map.SwitchingLAKMap;
import controller.MapController;
import controller.MapDataProvider;

public class Main {

	private static final Logger logger = LogManager.getLogger(Main.class);

	private static final String organizationLocationFile = "data/organisations.csv";
	private static final String countryLocationFile = "data/countries.csv";
	private static final String organizationCountryFile = "data/organisation_country_map.csv";

	private static final boolean drawFPS = true;
	private static final boolean horizontal = true;

	public static void main(String[] args) {
		logger.info("Starting...");

		logger.debug("Creating dataprovider...");
		MapDataProvider dataProvider;
		try {
			dataProvider = new MapDataProvider(organizationLocationFile, countryLocationFile, organizationCountryFile);
		} catch (IOException e) {
			logger.fatal("Encountered IO Exception");
			logger.catching(e);

			System.exit(1);
			return;
		}

		AbstractLAKMap<?, ?> map = new SwitchingLAKMap(dataProvider, drawFPS);
		MapController mapController = new MapController(dataProvider, map);
		ChartSelectionPanel chartSelectionPanel = new ChartSelectionPanel(dataProvider, horizontal);
		chartSelectionPanel.addListener(mapController);

		// init graphics
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice displayDevice = environment.getDefaultScreenDevice();

		Frame frame = new Frame("LAK Dataset", displayDevice.getDefaultConfiguration());
		frame.setLayout(new BorderLayout());

		frame.add(map, BorderLayout.CENTER);
		frame.add(chartSelectionPanel, horizontal ? BorderLayout.SOUTH : BorderLayout.EAST);

		Image image = Toolkit.getDefaultToolkit().createImage(PApplet.ICON_IMAGE);
		frame.setIconImage(image);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		//displayDevice.setFullScreenWindow(frame);
		frame.setSize(1200, 800);
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
		
		frame.setResizable(false);

		// init processing
		map.init();
	}
}
