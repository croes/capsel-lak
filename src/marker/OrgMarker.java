package marker;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.RDFNode;

import rdf.RDFModel;
import processing.core.PConstants;
import processing.core.PGraphics;
import util.LocationCache;
import util.Time;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;


public class OrgMarker extends AbstractMarker {
	
	private String text;
	private int rmin, rmax, speed; //speed in pixels/sec
	private float r;
	private RDFNode node;
	private Animator animator;
	private UnfoldingMap umap;
	
	public OrgMarker(String text, Location loc, RDFNode node, UnfoldingMap umap){
		this(text, loc, 5, 10, node, umap);
	}
	
	public OrgMarker(String text, Location loc, int rmin, int rmax, RDFNode node, UnfoldingMap umap){
		super(loc);
		this.text = text;
		this.rmin = rmin;
		this.rmax = rmax;
		this.r = rmin;
		this.speed = 10;
		this.color = 122;
		this.highlightColor = 0xFFFF0000;
		this.node = node;
		this.animator = new PaperCountAnimator();
		this.umap = umap;
	}
	
	public RDFNode getRDFNode(){
		return node;
	}
	
	public void update(){
		animator.update();
	}

	@Override
	public void draw(PGraphics p, float x, float y) {
		update();
		animator.draw(p, x, y);
		p.pushMatrix();
		p.fill(0xFFFF0000);
		p.ellipse(x, y, r, r);
		if(isSelected()){
			p.textAlign(PConstants.CENTER, PConstants.BOTTOM);
			p.text(text, x, y-r-5);
		}
		p.popMatrix();
	}

	@Override
	protected boolean isInside(float checkX, float checkY, float x, float y) {
		double distance =  Math.sqrt(Math.pow(checkX-x,2)+Math.pow(checkY-y, 2));
		//System.out.printf("mouse: (%.3f,%.3f), marker: (%.3f,%.3f) distance: %.3f\n", checkX, checkY, x, y, distance);
		return distance < r + 3;
	}
	
	public abstract class Animator{
		public Animator(){
			setup();
		}
		public abstract void setup();
		public abstract void update();
		public abstract void draw(PGraphics p, float x, float y);
	}
	
	public class DefaultAnimator extends Animator{
		
		@Override
		public void setup(){}
		
		@Override
		public void update() {
			if(isSelected()){
				if(r > rmax)
					speed = -10;
				else if(r < rmin)
					speed = 10;
				r += speed * Time.deltaTime();
			}
			else{
				r = rmin;
			}
		}

		@Override
		public void draw(PGraphics p, float x, float y) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public class PaperCountAnimator extends Animator{
		
		private int paperCount;
		
		@Override
		public void setup() {
			//System.out.printf("Paper count for: %s\n", text);
			//System.out.println("===================");
		
			//System.out.println("====================");
			paperCount = RDFModel.getPaperCount(text);
			rmin = 5;
			r = (float) (rmin + 5f*Math.sqrt(paperCount));
			text += ": " + paperCount;
			
		}
		@Override
		public void update() {
		}
		@Override
		public void draw(PGraphics p, float x, float y) {
		}
		
	}
	
	public class CommonPaperAnimator extends Animator{
		
		private List<Marker> edges;
		private boolean showingEdges = false;

		@Override
		public void setup() {
			edges = new ArrayList<Marker>();
			ResultSet rs = RDFModel.getCommonPaperCount(text);
			ResultSetFormatter.out(rs);
			QuerySolution sol;
			while(rs.hasNext()){
				sol = rs.next();
				if(sol.getLiteral("otherOrgName") == null)
					continue;
				String otherOrgName = sol.getLiteral("otherOrgName").getString();
				int coopCount = sol.getLiteral("coopCount").getInt();
				Location start = getLocation();
				Location end = LocationCache.get(otherOrgName);
				SimpleLinesMarker m = new SimpleLinesMarker();
				m.setStrokeColor(0xFF00FF00);
				m.setStrokeWeight(coopCount);
				edges.add(new SimpleLinesMarker(start, end));
				//System.out.printf("Common papers for %s:%d\n", text, sol.get("count").asLiteral().getInt());
			}
			
		}

		@Override
		public void update() {
			if(isSelected() && !showingEdges){
				umap.addMarkers(edges);
				showingEdges = true;
			}
			else if(!isSelected() && showingEdges){
				//umap.getMarkers().removeAll(edges);
			}
		}

		@Override
		public void draw(PGraphics p, float x, float y) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
