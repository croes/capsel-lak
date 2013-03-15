package map;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import marker.NamedMarker;
import marker.OrgMarker;
import processing.core.PApplet;
import rdf.RDFModel;
import util.Drawable;
import util.LocationCache;
import util.Time;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.RDFNode;

import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.ListBox;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.looksgood.ani.Ani;


public class UniversityMap extends PApplet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7231594744155656041L;
	
	private UnfoldingMap map;
	private List<Drawable> drawables = new CopyOnWriteArrayList<Drawable>();
	private ListBox conflist;
	
	public static void main(String[] args) {
		PApplet.main(new String[] { "map.UniversityMap" });
	}
	
	public void setup(){
		size(1040, 720);//, GLConstants.GLGRAPHICS);
		frameRate(30);
		
		smooth();
		Ani.init(this);
		
		setupGUI();
	    map = new UnfoldingMap(this, 0, 200, this.width, this.height-200);
	    map.setTweening(true); //doesn't work
	    map.zoomAndPanTo(new Location(20,0), 3);
	    MapUtils.createDefaultEventDispatcher(this, map);
	    
	    addEdgeMarkers();
	    
		loadOrgMarkers();
		//addEdgeMarkers();
		populateGUI();
		drawables.add(Time.getInstance());
	}

	private void makeOrgsOfConfSelected(String confAcronym) {
		for(RDFNode node : getOrganisationsOfConference(confAcronym)){
			for(Marker m : map.getMarkers()){
				if(((OrgMarker)m).getRDFNode().equals(node)){
					m.setSelected(true);
				}
			}
		}
	}

	private void populateGUI() {
		List<RDFNode> confs = getConferences();
		for(int i = 0; i < confs.size(); i++){
			String acronym = confs.get(i).asResource().getProperty(RDFModel.getModel().getProperty("http://data.semanticweb.org/ns/swc/ontology#hasAcronym")).getString();
			conflist.addItem(acronym, i);
		}
	}

	private void loadOrgMarkers() {
		ResultSet rs = RDFModel.query( 
				"SELECT DISTINCT ?org ?orgname \n" +
				"WHERE \n" +
				"{ \n" +
				"?org rdf:type foaf:Organization . \n" +
				"?org foaf:name ?orgname . \n" +
				"} \n");

		while (rs.hasNext()) {
			QuerySolution sol = rs.next();
			String orgname = sol.getLiteral("orgname").toString();
			Location loc = LocationCache.get(orgname);
			if(loc != null){
				//OrgMarker m = new OrgMarker(orgname, loc, sol.get("org"), map);
				Marker m = new NamedMarker(orgname, loc);
				map.addMarkers(m);
			}
		}
	}
	
	private void setupGUI(){
		ControlP5 cp5 = new ControlP5(this);
		conflist = cp5.addListBox("Conferences")
						.setPosition(50, 50)
						.setSize(120, 120)
						.setBarHeight(15)
						.setItemHeight(15);
		cp5.addListener(new ControlListener(){
			@Override
			public void controlEvent(ControlEvent e) {
				if (e.isGroup() && e.getGroup().getName().equals("Conferences")) {
					int idx = (int)e.getGroup().getValue();
					String acro = conflist.getItem(idx).getName();
					makeOrgsOfConfSelected(acro);
				}
			}
		});
	}
	public List<RDFNode> getOrganisationsOfConference(String confAcronym){
		ResultSet rs = RDFModel.query( 
				"SELECT * \n" +
				"WHERE \n" +
				"{ \n" +
				"?conf rdf:type swc:ConferenceEvent .\n" +
				"?conf swc:hasAcronym \"" + confAcronym + "\" .\n" +
				"?conf swc:hasRelatedDocument ?proc . \n" +
				"?proc swc:hasPart ?paper. \n" +
				"?paper dc:creator ?author . \n" +
				"?author swrc:affiliation ?org . \n" +
				"} \n");
		//ResultSetFormatter.out(rs);
		QuerySolution sol = null;
		List<RDFNode> answers = new ArrayList<RDFNode>();
		while(rs.hasNext()){
			sol = rs.next();
			answers.add(sol.get("org"));
		}
		return answers;
	}
	
	public void addEdgeMarkers(){
		ResultSet rs = RDFModel.query(
				"SELECT ?orgName ?otherOrgName (COUNT(?otherMember) as ?coopCount) \n" +
				"WHERE \n" +
				"{\n" +
				"?org rdf:type foaf:Organization .\n " +
				"?org foaf:name ?orgName . \n" +
				"?org foaf:member ?member . \n" +
				"?paper dc:creator ?member .\n" +
				"?paper dc:creator ?otherMember . \n" +
				"?otherMember swrc:affiliation ?otherOrg . \n" +
				"?otherOrg foaf:name ?otherOrgName . \n" +
				"FILTER (?otherMember != ?member && ?otherOrg != ?org)" +
				"} GROUP BY ?orgName ?otherOrgName\n"
				);
		ResultSetFormatter.out(rs);
		QuerySolution sol;
		while(rs.hasNext()){
			sol = rs.next();
			if(sol.getLiteral("orgName") == null)
				continue;
			if(sol.getLiteral("otherOrgName") == null)
				continue;
			String orgName = sol.getLiteral("orgName").getString();
			String otherOrgName = sol.getLiteral("otherOrgName").getString();
			int coopCount = sol.getLiteral("coopCount").getInt();
			Location start = LocationCache.get(orgName);
			Location end = LocationCache.get(otherOrgName);
			if(start == null || end == null)
				continue;
			SimpleLinesMarker m = new SimpleLinesMarker();
			m.setStrokeColor(0xF000FF00);
			m.setStrokeWeight(coopCount);
			map.addMarkers(new SimpleLinesMarker(start, end));
			System.out.printf("Common papers for %s to %s:%d\n", orgName, otherOrgName, coopCount);
		}
	}
	
	public List<RDFNode> getConferences(){
		ResultSet rs = RDFModel.query( 
				"SELECT DISTINCT ?conf \n" +
				"WHERE \n" +
				"{ \n" +
				"?conf rdf:type swc:ConferenceEvent \n" +
				"} \n");
		//ResultSetFormatter.out(rs);
		QuerySolution sol = null;
		List<RDFNode> answers = new ArrayList<RDFNode>();
		while(rs.hasNext()){
			sol = rs.next();
			answers.add(sol.get("conf"));
		}
		return answers;
	}
	
	public void draw(){
		updateAndDrawDrawables();
		background(245);
		map.draw();
	}

	private void updateAndDrawDrawables() {
		for(Drawable d : drawables ){
			d.update();
		}
		
		for(Drawable d : drawables){
			d.draw(this);
		}
	}
	
	public void mouseMoved(){
		List<Marker> hitMarkers = map.getHitMarker(mouseX, mouseY);
		for (Marker m : map.getMarkers()) {
			m.setSelected(hitMarkers.contains(m));
		}
	}
}
