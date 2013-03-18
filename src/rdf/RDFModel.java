package rdf;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.FileManager;

public class RDFModel {
	
	private static Model model;
	
	private static final String[] modelFileNames =
		{
		 "data/2011_fulltext_.rdf"
		,"data/2012_fulltext_.rdf"
		,"data/edm2008.rdf"
		,"data/edm2009.rdf"
		,"data/edm2010.rdf"
		,"data/edm2011.rdf"
		,"data/edm2012.rdf"
		,"data/jets12_fulltext_.rdf"
		};
	
	public static Model getModel(){
		if(model == null){
			parseModels();
		}
		return model;
	}
	
	private static void parseModels(){
		System.out.println("Loading RDF models...");
		model = ModelFactory.createDefaultModel();
		for(String modelFileName : modelFileNames){
			model = model.union(RDFModel.parseModel(modelFileName));
		}
	}
	
	private static Model parseModel(String fileName){
		// create an empty model
		Model model = ModelFactory.createDefaultModel();

		// use the FileManager to find the input file
		InputStream in = FileManager.get().open( fileName );
		if (in == null) {
		    throw new IllegalArgumentException(
		    		"File: " + fileName + " not found");
		}

		// read the RDF/XML file
		model.read(in, "", null);
		return model;
	}
	
	public static ResultSet query(String queryString){
		//for(Entry<String, String> e : model.getNsPrefixMap().entrySet()){
			//queryString = String.format("PREFIX %s: <%s> \n" , e.getKey(), e.getValue()) + queryString;
		//}
		String prefix =
				"PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
				"PREFIX swc: <http://data.semanticweb.org/ns/swc/ontology#>\n" + 
				"PREFIX ical: <http://www.w3.org/2002/12/cal/ical#>\n" +
				"PREFIX swrc: <http://swrc.ontoware.org/ontology#>\n" +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX bibo: <http://purl.org/ontology/bibo/>\n" +
				"PREFIX led: <http://data.linkededucation.org/ns/linked-education.rdf#>\n" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
				"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" ;
		//String encodedString = "http://data.linkededucation.org/openrdf-sesame/repositories/lak-conference?query=" + URLEncoder.encode(queryString);
		//System.out.println(encodedString);
		queryString = prefix + queryString;
		//System.out.println(queryString);
		Query query = QueryFactory.make();
        QueryFactory.parse(query, queryString, null, Syntax.syntaxSPARQL);

		//query.setPrefixMapping(model);
		QueryExecution qexec = QueryExecutionFactory.create(query, getModel()) ;
		ResultSet results;
		try {
			results = ResultSetFactory.copyResults(qexec.execSelect());
		} finally { qexec.close() ; }
		//resultsrw.reset();
		return results;
	}
	
	public static ResultSet getAllOrganisations(){
		return RDFModel.query( 
				"SELECT DISTINCT ?org ?orgname \n" +
				"WHERE \n" +
				"{ \n" +
				"?org rdf:type foaf:Organization . \n" +
				"?org foaf:name ?orgname . \n" +
				"} \n");
	}
	
	public static ResultSet getAllOrganisationPairsThatWroteAPaperTogether(){
		return RDFModel.query(
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
	}
	
	public static ResultSet getAllOrganisationPairsThatWroteAPaperTogetherFromGivenConference(String confAcronym){
		return RDFModel.query(
				"SELECT ?orgName ?otherOrgName (COUNT(?otherMember) as ?coopCount) \n" +
				"WHERE \n" +
				"{\n" +
				"?conf swc:hasAcronym \"" + confAcronym + "\" .\n" +
				"?conf swc:hasRelatedDocument ?proc . \n" +
				"?org rdf:type foaf:Organization .\n " +
				"?org foaf:name ?orgName . \n" +
				"?org foaf:member ?member . \n" +
				"?paper dc:creator ?member .\n" +
				"?paper dc:creator ?otherMember . \n" +
				"?paper swc:isPartOf ?proc . \n" +
				"?otherMember swrc:affiliation ?otherOrg . \n" +
				"?otherOrg foaf:name ?otherOrgName . \n" +
				"FILTER (?otherMember != ?member && ?otherOrg != ?org)" +
				"} GROUP BY ?orgName ?otherOrgName\n"
				);
	}
	
	/**
	 * Returns all the organizations that took part in the given conference.
	 * @param confAcronym
	 * @return
	 */
	public static List<RDFNode> getOrganisationsOfConference(String confAcronym){
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
	
	/**
	 * Returns all the available conferences.
	 * @return
	 */
	public static List<RDFNode> getConferences(){
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
	
	public static int getPaperCount(String text){
		int paperCount = 0;
		ResultSet rs = RDFModel.query(
				"SELECT ?org (COUNT(?paper) AS ?paperCount)\n" +
				"WHERE \n" +
				"{\n" +
				"?org rdf:type foaf:Organization .\n " +
				"?org foaf:name \"" + text + "\" . \n" +
				"?org foaf:member ?member . \n" +
				"?paper foaf:maker ?member\n" +
				"} GROUP BY ?org \n"
				);
		//ResultSetFormatter.out(rs);
		//System.out.println("====================");
		QuerySolution sol;
		while(rs.hasNext()){
			sol = rs.next();
			paperCount = sol.getLiteral("paperCount").getInt();
			System.out.printf("Paper count for %s:%d\n",text, sol.getLiteral("paperCount").getInt());
		}
		return paperCount;
	}
	
	public static ResultSet getCommonPaperCount(String text){
		return RDFModel.query(
			"SELECT ?otherOrgName (COUNT(?otherMember) as ?coopCount) \n" +
			"WHERE \n" +
			"{\n" +
			"?org rdf:type foaf:Organization .\n " +
			"?org foaf:name \"" + text + "\" . \n" +
			"?org foaf:member ?member . \n" +
			"?paper dc:creator ?member .\n" +
			"?paper dc:creator ?otherMember . \n" +
			"?otherMember swrc:affiliation ?otherOrg . \n" +
			"?otherOrg foaf:name ?otherOrgName . \n" +
			"FILTER (?otherMember != ?member && ?otherOrg != ?org)" +
			"} GROUP BY ?otherOrgName\n"
			);
	}
}
