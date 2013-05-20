package ui.tile;

import java.io.File;

import log.LogManager;
import log.Logger;
import processing.core.PApplet;
import processing.core.PImage;
import de.fhpotsdam.unfolding.core.Coordinate;
import de.fhpotsdam.unfolding.providers.AbstractMapProvider;

public class CachedMapProvider extends AbstractMapProvider {

	private static final Logger logger = LogManager.getLogger(CachedMapProvider.class);

	private final AbstractMapProvider provider;
	private PApplet p;

	private final String fileFormat;
	
	public CachedMapProvider(String dataDirectory, AbstractMapProvider provider) {
		this(dataDirectory, null, provider);
	}

	public CachedMapProvider(String dataDirectory, PApplet p, AbstractMapProvider provider) {
		super(provider.projection);
		this.p = p;
		this.provider = provider;

		dataDirectory = dataDirectory.endsWith(File.separator) ? dataDirectory : (dataDirectory + File.separator);
		
		fileFormat = String.format("%s%%d%s%%dx%%d.png", dataDirectory, File.separator);
	}
	
	public void setPApplet(PApplet p) {
		this.p = p;
	}

	private String getFileName(Coordinate coordinate) {
		return String.format(fileFormat, (int) coordinate.zoom, (int) coordinate.row,
				(int) coordinate.column);
	}

	@Override
	public PImage getTile(Coordinate coordinate) {
		logger.debug("Getting tile for Coordinate[row=%f,column=%f,zoom=%f", coordinate.row, coordinate.column,
				coordinate.zoom);
		
		File imageFile = p.dataFile(getFileName(coordinate));
		if (imageFile.exists()) {
			logger.debug("Found tile image @ %s", imageFile.getPath());
			return p.loadImage(imageFile.getAbsolutePath());
		}
		
		logger.debug("Image not found yet, loading from URL");
		PImage image = getTileFromUrl(provider.getTileUrls(coordinate));
		
		if (image == null) {
			logger.error("Loading the image from URL failed");
			return null;
		}
		
		logger.debug("Storing image @ %s", imageFile.getPath());
		PApplet.createPath(imageFile);
		image.save(imageFile.getAbsolutePath());
		
		return image;
	}

	@Override
	public String[] getTileUrls(Coordinate coordinate) {
		return null;
	}

	@Override
	public int tileWidth() {
		return provider.tileWidth();
	}

	@Override
	public int tileHeight() {
		return provider.tileHeight();
	}

	/**
	 * Loads tile from URL(s) by using Processing's loadImage function. If
	 * multiple URLs are provided, all tile images are blended into each other.
	 * 
	 * @param urls
	 *            The URLs (local or remote) to load the tiles from.
	 * @return The tile image.
	 */
	protected PImage getTileFromUrl(String[] urls) {
		// Load image from URL (local file included)
		// NB: Use 'unknown' as content-type to let loadImage decide
		PImage img = p.loadImage(urls[0], "unknown");

		if (img != null) {
			// If array contains multiple URLs, load all images and blend them
			// together
			for (int i = 1; i < urls.length; i++) {
				PImage img2 = p.loadImage(urls[i], "unknown");
				if (img2 != null) {
					img.blend(img2, 0, 0, img.width, img.height, 0, 0, img.width, img.height, PApplet.BLEND);
				}
			}
		}

		return img;
	}

}
