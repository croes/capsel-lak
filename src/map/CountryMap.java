package map;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import marker.HideableMarker;
import marker.NamedMarker;
import processing.core.PApplet;
import rdf.RDFModel;
import util.CountryLocationCache;
import util.Drawable;
import util.Time;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.ListBox;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.looksgood.ani.Ani;


public class CountryMap extends PApplet{

	private static final long serialVersionUID = -7231594744155656041L;

	public static class HideableLineMarker extends HideableMarker<SimpleLinesMarker> {

		public HideableLineMarker(Location start, Location end) {
			super(new SimpleLinesMarker(start, end));
		}
	}

	private UnfoldingMap map;
	private List<Drawable> drawables = new CopyOnWriteArrayList<Drawable>();
	private ListBox conflist;
	private MarkerManager<NamedMarker> countryMarkMan; //todo: Till vragen over marker manager (vooral map.addMarkerManager, geen correcte generics)
	private MarkerManager<SimpleLinesMarker> edgeMarkMan;

	public static void main(String[] args) {
		PApplet.main(new String[] { "map.CountryMap" });
	}

	public void setup(){
		size(1040, 720); //, GLConstants.GLGRAPHICS); //no bug with markers going outside of map area if this is used
		frameRate(30);

		smooth();
		Ani.init(this);


		map = new UnfoldingMap(this); //, 0, 200, this.width, this.height-200);
		map.setTweening(true); //(doesn't work). it does now, what changed? smooth()?
		map.zoomAndPanTo(new Location(20,0), 3);
		MapUtils.createDefaultEventDispatcher(this, map);

		edgeMarkMan = new MarkerManager<SimpleLinesMarker>();//Generics in markermanager, but not in map.addMarkerManager, cause fuck you.
		addAllEdgeMarkers();
		countryMarkMan = new MarkerManager<NamedMarker>();
		addAllCountryMarkers();

		map.addMarkerManager(edgeMarkMan);
		map.addMarkerManager(countryMarkMan);

		setupGUI();
		populateGUI();
		drawables.add(Time.getInstance());
	}

	/**
	 * Populate the extra GUI elements with the needed data.
	 */
	private void populateGUI() {
		List<RDFNode> confs = RDFModel.getConferences();
		for(int i = 0; i < confs.size(); i++){
			String acronym = confs.get(i).asResource().getProperty(RDFModel.getModel().getProperty("http://data.semanticweb.org/ns/swc/ontology#hasAcronym")).getString();
			conflist.addItem(acronym, i);
		}
	}

	/**
	 * Add all the extra GUI elements (apart from the map) to the PApplet.
	 */
	private void setupGUI(){
		ControlP5 cp5 = new ControlP5(this);
		conflist = cp5.addListBox("Conferences")
				.setPosition(50, 50)
				.setSize(120, 120)
				.setBarHeight(15)
				.setItemHeight(15);
		cp5.addButton("ShowAllButton")
				.setCaptionLabel("Show all")
				.setValue(0)
				.setPosition(200,35)
				.setSize(120,19)
				;
		//Listener to control selection events.
		cp5.addListener(new ControlListener(){
			@Override
			public void controlEvent(ControlEvent e) {
				if (e.isGroup() && e.getGroup().getName().equals("Conferences")) {
					int idx = (int)e.getGroup().getValue();
					String acro = conflist.getItem(idx).getName();
					showOnlyConf(acro);
				}
				if (!e.isGroup() && e.getController().getName().equals("ShowAllButton")) {
					showAll();
				}
			}
		});
	}

	/**
	 * Called when showAll button is clicked
	 */
	private void showAll(){
		showAllOrgMarkers();
		edgeMarkMan.clearMarkers();
		addAllEdgeMarkers();
	}

	/**
	 * Shows only the markers from the given conference name on the map.
	 *  
	 * @param confAcronym
	 */
	private void showOnlyConf(String confAcronym) {
		hideAllCountryMarkers();
		showCountryMarkersOf(confAcronym);
		edgeMarkMan.clearMarkers();
		addEdgeMarkers(confAcronym);
	}

	/**
	 * Adds all the organization markers to the map.
	 */
	private void addAllCountryMarkers() {
		ResultSet rs = RDFModel.getAllCountries();

		while (rs.hasNext()) {
			QuerySolution sol = rs.next();
			String countryName = parseCountryURL(sol.getResource("country").toString());
			Location loc = CountryLocationCache.get(countryName);
			if(loc != null){
				NamedMarker m = new NamedMarker(countryName, loc);
				countryMarkMan.addMarker(m);
			}
		}
	}

	public String parseCountryURL(String countryURL){
		int lastIndexSlash = countryURL.lastIndexOf("/");
		String country = countryURL.substring(lastIndexSlash + 1);
		//System.out.printf("%s parsed from %s\n", country, countryURL);
		return country;
	}

	/**
	 * Hides all the organizations markers from the map.
	 */
	private void hideAllCountryMarkers() {
		for(NamedMarker m : countryMarkMan.getMarkers()){
			m.setHidden(true);
		}
	}

	/**
	 * Shows all the organizations markers from the map.
	 */
	private void showAllOrgMarkers() {
		for(NamedMarker m : countryMarkMan.getMarkers()){
			m.setHidden(false);
		}
	}

	/**
	 * Shows only the organisation markers of the given organisation name on the map.
	 * @param confAcronym
	 */
	private void showCountryMarkersOf(String confAcronym) {
		for(String countryURL : RDFModel.getCountriesOfConference(confAcronym)){
			String countryName = parseCountryURL(countryURL);
			for(NamedMarker m : countryMarkMan.getMarkers()){
				if(m.getName().equals(countryName))
					m.setHidden(false);
			}
		}
	}

	/**
	 * Adds all the line markers between the org markers.
	 */
	private void addAllEdgeMarkers(){
		ResultSet rs = RDFModel.getAllCountryPairsThatWroteAPaperTogether();
		//ResultSetFormatter.out(rs);
		QuerySolution sol;
		while(rs.hasNext()){
			sol = rs.next();
			if(!isValidSolutionForMarker(sol))
				continue;
			String countryName = parseCountryURL(sol.getResource("country").toString());
			String otherCountryName = parseCountryURL(sol.getResource("otherCountry").toString());
			int coopCount = sol.getLiteral("coopCount").getInt();
			Location start = CountryLocationCache.get(countryName);
			Location end = CountryLocationCache.get(otherCountryName);
			if(start == null || end == null)
				continue;
			SimpleLinesMarker m = new SimpleLinesMarker(start, end);
			m.setStrokeColor(0x0100FF00);
			m.setStrokeWeight(coopCount);
			edgeMarkMan.addMarker(m);
			System.out.printf("Common papers for %s to %s:%d\n", countryName, otherCountryName, coopCount);
		}
	}

	/**
	 * Tests whether the given querySolution can be used to create a valid marker.
	 * @param solution
	 * @return
	 */
	private boolean isValidSolutionForMarker(QuerySolution solution){
		if(solution.getResource("country") == null || solution.getResource("otherCountry") == null)
			return false;
		return true;
	}

	/**
	 * Adds all the edge markers of the given conference name to the map.
	 * @param confAcronym
	 */
	private void addEdgeMarkers(String confAcronym){
		ResultSet rs = RDFModel.getAllCountryPairsThatWroteAPaperTogetherFromGivenConference(confAcronym);
		//ResultSetFormatter.out(rs);
		QuerySolution sol;
		while(rs.hasNext()){
			sol = rs.next();
			if(!isValidSolutionForMarker(sol))
				continue;

			String countryName = parseCountryURL(sol.getResource("country").toString());
			String otherCountryName = parseCountryURL(sol.getResource("otherCountry").toString());
			int coopCount = sol.getLiteral("coopCount").getInt();
			Location start = CountryLocationCache.get(countryName);
			Location end = CountryLocationCache.get(otherCountryName);
			if(start == null || end == null)
				continue;
			SimpleLinesMarker m = new SimpleLinesMarker(start, end);
			m.setStrokeColor(0xF000FF00);
			m.setStrokeWeight(coopCount);
			edgeMarkMan.addMarker(m);
			System.out.printf("Common papers for %s to %s:%d for conf:%s\n", countryName, otherCountryName, coopCount, confAcronym);
		}
	}

	@Override
	public void draw(){
		updateDrawables();
		background(245);
		map.draw();
		drawDrawables();
	}

	private void updateDrawables() {
		for(Drawable d : drawables ){
			d.update();
		}
	}

	private void drawDrawables() {
		for(Drawable d : drawables){
			d.draw(this);
		}
	}

	/**
	 * Takes care of the hovering feature. 
	 * When you hover over a marker, the marker is set to selected and the marker handles it change in look itself.
	 */
	public void mouseMoved(){
		List<NamedMarker> hitMarkers = countryMarkMan.getHitMarkers(mouseX, mouseY);
		for (Marker m : countryMarkMan.getMarkers()) {
			m.setSelected(hitMarkers.contains(m));
		}
	}
}