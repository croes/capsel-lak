package map;

import java.util.List;

import marker.EdgeMarker;
import marker.HideableMarker;
import marker.NamedMarker;
import processing.core.PApplet;
import rdf.RDFModel;
import util.StringUtil;
import util.location.CountryLocationCache;
import util.location.LocationCache;

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
	private ListBox conflist;
	private MarkerManager<NamedMarker> countryMarkMan; //todo: Till vragen over marker manager (vooral map.addMarkerManager, geen correcte generics)
	private MarkerManager<EdgeMarker> edgeMarkMan;
	
	private LocationCache locationCache;

	public static void main(String[] args) {
		PApplet.main(new String[] { "map.CountryMap" });
	}

	public void setup(){
		size(1040, 720); //, GLConstants.GLGRAPHICS); //no bug with markers going outside of map area if this is used
		frameRate(30);

		smooth();
		Ani.init(this);

		// create location cache
		locationCache = new CountryLocationCache("data/countries_locs.txt");

		map = new UnfoldingMap(this); //, 0, 200, this.width, this.height-200);
		map.setTweening(true); //(doesn't work). it does now, what changed? smooth()?
		map.zoomAndPanTo(new Location(20,0), 3);
		MapUtils.createDefaultEventDispatcher(this, map);

		countryMarkMan = new MarkerManager<NamedMarker>();
		addAllCountryMarkers();
		edgeMarkMan = new MarkerManager<EdgeMarker>();//Generics in markermanager, but not in map.addMarkerManager, cause fuck you.
		addAllEdgeMarkers();

		map.addMarkerManager(countryMarkMan);
		map.addMarkerManager(edgeMarkMan);

		setupGUI();
		populateGUI();
	}

	/**
	 * Populate the extra GUI elements with the needed data.
	 */
	private void populateGUI() {
		List<RDFNode> confs = RDFModel.getConferences();
		for (int i = 0; i < confs.size(); i++) {
			String acronym = StringUtil.getString(confs
					.get(i)
					.asResource()
					.getProperty(
							RDFModel.getModel().getProperty("http://data.semanticweb.org/ns/swc/ontology#hasAcronym")));
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
			Location loc = locationCache.get(countryName);
			if(loc != null){
				NamedMarker m = new NamedMarker(countryName, loc);
				countryMarkMan.addMarker(m);
			}
		}
	}

	public String parseCountryURL(String countryURL){
		int lastIndexSlash = countryURL.lastIndexOf("/");
		String country = countryURL.substring(lastIndexSlash + 1);
		return StringUtil.getString(country);
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
			NamedMarker start = getMarkerWithName(countryName);
			NamedMarker end = getMarkerWithName(otherCountryName);
			if(start == null || end == null)
				continue;
			EdgeMarker m = new EdgeMarker(start, end);
			m.setColor(0xF0505050);
			m.setHighlightColor(0xFFFF0000);
			m.setStrokeWeight(coopCount);
			edgeMarkMan.addMarker(m);
			System.out.printf("Common papers for %s to %s:%d\n", countryName, otherCountryName, coopCount);
		}
	}
	
	/**
	 * Find the marker with a given name
	 */
	public NamedMarker getMarkerWithName(String name){
		for(NamedMarker nm : countryMarkMan.getMarkers()){
			if(nm.getName().equals(name))
				return nm;
		}
		//System.out.println("Could not find marker with name:" + name);
		return null;
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
			NamedMarker start = getMarkerWithName(countryName);
			NamedMarker end = getMarkerWithName(otherCountryName);
			if(start == null || end == null)
				continue;
			EdgeMarker m = new EdgeMarker(start, end);
			m.setColor(0xF0505050);
			m.setHighlightColor(0xFFFF0000);
			m.setStrokeWeight(coopCount);
			edgeMarkMan.addMarker(m);
			System.out.printf("Common papers for %s to %s:%d for conf:%s\n", countryName, otherCountryName, coopCount, confAcronym);
		}
	}

	@Override
	public void draw(){
		background(245);
		map.draw();
	}

	/**
	 * Takes care of the hovering feature. 
	 * When you hover over a marker, the marker is set to selected and the marker handles it change in look itself.
	 */
	public void mouseMoved(){
		List<EdgeMarker> edgeHitMarkers = edgeMarkMan.getHitMarkers(mouseX, mouseY);
		boolean edgeMarked = false;
		for (EdgeMarker m : edgeMarkMan.getMarkers()){
			if(edgeHitMarkers.contains(m)){
				m.setSelected(true);
				m.getM1().setSelected(true);
				m.getM2().setSelected(true);
				//System.out.printf("Marked edge from: %s to %s\n", ((NamedMarker)m.getM1()).getName(), ((NamedMarker)m.getM2()).getName());
				edgeMarked = true;
			}else{
				m.setSelected(false);
			}
		}
		if(edgeMarked)
			return; //don't deselect orgMarker if an edge is marked
		List<NamedMarker> hitMarkers = countryMarkMan.getHitMarkers(mouseX, mouseY);
		for (Marker m : countryMarkMan.getMarkers()) {
			m.setSelected(hitMarkers.contains(m));
		}
	}
}
