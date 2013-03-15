package rdf;

import java.io.InputStream;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
}
