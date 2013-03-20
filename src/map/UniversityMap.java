package map;

import java.util.List;

import marker.EdgeMarker;
import marker.HideableMarker;
import marker.NamedMarker;
import processing.core.PApplet;
import rdf.RDFModel;
import util.location.LocationCache;
import util.location.OrganizationLocationCache;
import util.StringUtil;

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
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import de.looksgood.ani.Ani;


public class UniversityMap extends PApplet{
	
	private static final long serialVersionUID = -7231594744155656041L;
	
	public static class HideableLineMarker extends HideableMarker<SimpleLinesMarker> {
		
		public HideableLineMarker(Location start, Location end) {
			super(new SimpleLinesMarker(start, end));
		}
	}
	
	private UnfoldingMap map;
	private ListBox conflist;
	
	private MarkerManager<NamedMarker> orgMarkMan; //todo: Till vragen over marker manager (vooral map.addMarkerManager, geen correcte generics)
	private MarkerManager<EdgeMarker> edgeMarkMan;
	
	private LocationCache locationCache;
	
	public static void main(String[] args) {
		PApplet.main(new String[] { "map.UniversityMap" });
	}
	
	public void setup(){
		size(1040, 720); //, GLConstants.GLGRAPHICS); //no bug with markers going outside of map area if this is used
		frameRate(30);
		
		smooth();
		Ani.init(this);
		
		// create location cache
		locationCache = new OrganizationLocationCache("data/org_locs.txt");
		
	    map = new UnfoldingMap(this); //, 0, 200, this.width, this.height-200);
	    map.setTweening(true); //(doesn't work). it does now, what changed? smooth()?
	    map.zoomAndPanTo(new Location(20,0), 3);
	    MapUtils.createDefaultEventDispatcher(this, map);
	    
	    orgMarkMan = new MarkerManager<NamedMarker>();
		addAllOrgMarkers();
		edgeMarkMan = new MarkerManager<EdgeMarker>();//Generics in markermanager, but not in map.addMarkerManager, cause fuck you.
	    addAllEdgeMarkers();
		
		map.addMarkerManager(orgMarkMan);
		map.addMarkerManager(edgeMarkMan);
		
		setupGUI();
		populateGUI();
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
			Location loc = locationCache.get(orgname);
			if(loc != null){
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

		QuerySolution sol;
		while(rs.hasNext()){
			sol = rs.next();
			if(!isValidSolutionForMarker(sol))
				continue;
			String orgName = StringUtil.getString(sol.getLiteral("orgName"));
			String otherOrgName = StringUtil.getString(sol.getLiteral("otherOrgName"));
			int coopCount = sol.getLiteral("coopCount").getInt();
			NamedMarker start = getMarkerWithName(orgName);
			NamedMarker end = getMarkerWithName(otherOrgName);
			if(start == null || end == null)
				continue;
			EdgeMarker m = new EdgeMarker(start, end);
			m.setColor(0xF0505050);
			m.setHighlightColor(0xFFFF0000);
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
			NamedMarker start = getMarkerWithName(orgName);
			NamedMarker end = getMarkerWithName(otherOrgName);
			if(start == null || end == null)
				continue;
			EdgeMarker m = new EdgeMarker(start, end);
			m.setColor(0xF0505050);
			m.setHighlightColor(0xFFFF0000);
			m.setStrokeWeight(coopCount);
			edgeMarkMan.addMarker(m);
			System.out.printf("Common papers for %s to %s:%d\n", orgName, otherOrgName, coopCount);
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
		List<NamedMarker> hitMarkers = orgMarkMan.getHitMarkers(mouseX, mouseY);
		for (Marker m : orgMarkMan.getMarkers()) {
			m.setSelected(hitMarkers.contains(m));
		}
		
	}
	
	/**
	 * Find the marker with a given name
	 */
	public NamedMarker getMarkerWithName(String name){
		for(NamedMarker nm : orgMarkMan.getMarkers()){
			if(nm.getName().equals(name))
				return nm;
		}
		//System.out.println("Could not find marker with name:" + name);
		return null;
	}
	
	
	/**
	 * Moved to EdgeMarker
	 */
	@Deprecated
	public boolean isInside(int mouseX, int mouseY, SimpleLinesMarker marker){
		Location l1 = marker.getLocations().get(0);
		Location l2 = marker.getLocations().get(1);
		ScreenPosition  sposa = map.getScreenPosition(l1),
						sposb = map.getScreenPosition(l2);
		float 	xa = sposa.x,
				xb = sposb.x,
				ya = sposa.y,
				yb = sposb.y;
		if(mouseX > Math.max(xa, xb)
				|| mouseX < Math.min(xa, xb)
				|| mouseY > Math.max(ya, yb)
				|| mouseY < Math.min(ya, yb)){
			return false;
		}
		float	m = (ya - yb) / (xa - xb),
				b = ya - m * xa,
				d = (float) (Math.abs(mouseY - m * mouseX - b) / Math.sqrt(m*m + 1));
		return d < 3;
	}
}
