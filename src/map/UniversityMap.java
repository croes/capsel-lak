package map;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import marker.HideableMarker;
import marker.NamedMarker;
import processing.core.PApplet;
import rdf.RDFModel;
import util.Drawable;
import util.LocationCache;
import util.StringUtil;
import util.Time;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

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


public class UniversityMap extends PApplet{
	
	private static final long serialVersionUID = -7231594744155656041L;
	
	public static class HideableLineMarker extends HideableMarker<SimpleLinesMarker> {
		
		public HideableLineMarker(Location start, Location end) {
			super(new SimpleLinesMarker(start, end));
		}
	}
	
	private UnfoldingMap map;
	private List<Drawable> drawables = new CopyOnWriteArrayList<Drawable>();
	private ListBox conflist;
	private MarkerManager<NamedMarker> orgMarkMan; //todo: Till vragen over marker manager (vooral map.addMarkerManager, geen correcte generics)
	private MarkerManager<SimpleLinesMarker> edgeMarkMan;
	public static void main(String[] args) {
		PApplet.main(new String[] { "map.UniversityMap" });
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
	    orgMarkMan = new MarkerManager<NamedMarker>();
		addAllOrgMarkers();
		
		map.addMarkerManager(edgeMarkMan);
		map.addMarkerManager(orgMarkMan);
		
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
			String acronym = StringUtil.getString(confs.get(i).asResource()
					.getProperty(
							RDFModel.getModel()
								.getProperty("http://data.semanticweb.org/ns/swc/ontology#hasAcronym")
					));
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
			     .setSize(120,19);
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
		hideAllOrgMarkers();
		showOrgMarkersOf(confAcronym);
		edgeMarkMan.clearMarkers();
		addEdgeMarkers(confAcronym);
	}

	/**
	 * Adds all the organization markers to the map.
	 */
	private void addAllOrgMarkers() {
		ResultSet rs = RDFModel.getAllOrganisations();

		while (rs.hasNext()) {
			QuerySolution sol = rs.next();
			String orgname = StringUtil.getString(sol.getLiteral("orgname"));
			Location loc = LocationCache.get(orgname);
			if(loc != null){
				//OrgMarker m = new OrgMarker(orgname, loc, sol.get("org"), map);
				NamedMarker m = new NamedMarker(orgname, loc);
				orgMarkMan.addMarker(m);
			}
		}
	}
	
	/**
	 * Hides all the organizations markers from the map.
	 */
	private void hideAllOrgMarkers() {
		for(NamedMarker m : orgMarkMan.getMarkers()){
			m.setHidden(true);
		}
	}
	
	/**
	 * Shows all the organizations markers from the map.
	 */
	private void showAllOrgMarkers() {
		for(NamedMarker m : orgMarkMan.getMarkers()){
			m.setHidden(false);
		}
	}

	/**
	 * Shows only the organisation markers of the given organisation name on the map.
	 * @param confAcronym
	 */
	private void showOrgMarkersOf(String confAcronym) {
		for(RDFNode node : RDFModel.getOrganisationsOfConference(confAcronym)){
			String orgName = StringUtil.getString(node.asResource().getProperty(FOAF.name));
			//System.out.println(orgName);
			for(NamedMarker m : orgMarkMan.getMarkers()){
				if(m.getName().equals(orgName))
					m.setHidden(false);
			}
		}
	}

	/**
	 * Adds all the line markers between the org markers.
	 */
	private void addAllEdgeMarkers(){
		ResultSet rs = RDFModel.getAllOrganisationPairsThatWroteAPaperTogether();
		//ResultSetFormatter.out(rs);
		QuerySolution sol;
		while(rs.hasNext()){
			sol = rs.next();
			if(!isValidSolutionForMarker(sol))
				continue;
			String orgName = StringUtil.getString(sol.getLiteral("orgName"));
			String otherOrgName = StringUtil.getString(sol.getLiteral("otherOrgName"));
			int coopCount = sol.getLiteral("coopCount").getInt();
			Location start = LocationCache.get(orgName);
			Location end = LocationCache.get(otherOrgName);
			if(start == null || end == null)
				continue;
			SimpleLinesMarker m = new SimpleLinesMarker(start, end);
			m.setStrokeColor(0x0100FF00);
			m.setStrokeWeight(coopCount);
			edgeMarkMan.addMarker(m);
			//System.out.printf("Common papers for %s to %s:%d\n", orgName, otherOrgName, coopCount);
		}
	}
	
	/**
	 * Tests whether the given querySolution can be used to create a valid marker.
	 * @param solution
	 * @return
	 */
	private boolean isValidSolutionForMarker(QuerySolution solution){
		if(solution.getLiteral("orgName") == null || solution.getLiteral("otherOrgName") == null)
			return false;
		return true;
	}
	
	/**
	 * Adds all the edge markers of the given conference name to the map.
	 * @param confAcronym
	 */
	private void addEdgeMarkers(String confAcronym){
		ResultSet rs = RDFModel.getAllOrganisationPairsThatWroteAPaperTogetherFromGivenConference(confAcronym);
		//ResultSetFormatter.out(rs);
		QuerySolution sol;
		while(rs.hasNext()){
			sol = rs.next();
			if(!isValidSolutionForMarker(sol))
				continue;
			String orgName = StringUtil.getString(sol.getLiteral("orgName"));
			String otherOrgName = StringUtil.getString(sol.getLiteral("otherOrgName"));
			int coopCount = sol.getLiteral("coopCount").getInt();
			Location start = LocationCache.get(orgName);
			Location end = LocationCache.get(otherOrgName);
			if(start == null || end == null)
				continue;
			SimpleLinesMarker m = new SimpleLinesMarker(start, end);
			m.setStrokeColor(0xF000FF00);
			m.setStrokeWeight(coopCount);
			edgeMarkMan.addMarker(m);
			System.out.printf("Common papers for %s to %s:%d\n", orgName, otherOrgName, coopCount);
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
		List<NamedMarker> hitMarkers = orgMarkMan.getHitMarkers(mouseX, mouseY);
		for (Marker m : orgMarkMan.getMarkers()) {
			m.setSelected(hitMarkers.contains(m));
		}
	}
}
