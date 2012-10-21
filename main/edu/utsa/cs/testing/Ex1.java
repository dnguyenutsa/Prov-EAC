package edu.utsa.cs.testing;

import java.util.Arrays;  

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;



public class Ex1 {
	public static void main(String [] args){

		Model model1 = ModelFactory.createDefaultModel();
		
		Resource r1 = model1.createResource("http://example.org/book#1");
		Resource r2 = model1.createResource( "http://example.org/book#2" ) ;
		Resource r3 = model1.createResource( "http://example.org/book#3" ) ;
		
//		r4.addProperty(DC.description, r2);
//		m2.write(System.out, "RDF/XML-ABBREV");

		String nsResource = "http://example.org/book#";
		model1.setNsPrefix("nsResource", nsResource);
		
		System.out.println("TEsting\n " + DC.title.getNameSpace());
		
		r1.addProperty(DC.title, "SPARQL-the book" ).addProperty(DC.description, "A book about SPARQL" ).addProperty(DC.description, r2) ;
//		r2.addProperty(DC.title, "Advanced techniques for SPARQL" ).addProperty(DC.description, r3).addProperty(DC.description, r1) ;
//		r3.addProperty(DC.title, "Jena -an RDF framework for Java" ).addProperty(DC.description, "A book about Jena").addProperty(DC.description, r1) ;
		r2.addProperty(DC.title, "Advanced techniques for SPARQL" ).addProperty(DC.description, r1);
		
		System.out.println(DC.description.getURI());
		System.out.println(DC.getURI());
		
		model1.write(System.out,"N-TRIPLE");
		model1.write(System.out, "RDF/XML-ABBREV");


		String prolog = "PREFIX dc:<" + DC.getURI() + ">";
		String queryString= prolog + "\n" 
				+ "SELECT ?title WHERE { ?x dc:description ?y . ?y dc:description?z . ?z dc:title?title }";
		
		Query query= QueryFactory.create( queryString);
		QueryExecution qexec= QueryExecutionFactory.create( query, model1 );
		
		ResultSet rs= qexec.execSelect();
		ResultSetFormatter.out(System.out, rs, query);

		
		System.out.println("\nGLEEN\n");

		/* Testing GLEEN */
		// specify query to execute
		prolog += "\nPREFIX gleen:<java:edu.washington.sig.gleen.>";
		queryString = prolog + "\n"
				+ "SELECT ?title WHERE {  <http://example.org/book#1> gleen:OnPath(\"([dc:description]/[dc:description])*/[dc:title]\" ?title ) .}";
		
		// create and execute an instance of the specified query
		query= QueryFactory.create(queryString);
		qexec= QueryExecutionFactory.create( query, model1 );
		
		// store results in a set
		rs= qexec.execSelect();

		// output query results to the screen
		ResultSetFormatter.out(System.out, rs, query); 
		
		// attempt to access the results one by one
		// so far unable
		try {
			//				ResultSet res= qexec.execSelect();
			//System.out.println(rs.getResultVars().toString());
			//				for ( ; rs.hasNext() ; ) {
			//					QuerySolution rb= rs.nextSolution() ;
			//System.out.println(rb);
			//					RDFNode x = rb.get( "title" ) ;
			//					System.out.println(x);
			//					if ( x.isLiteral() ) {
			//						Literal titleStr= (Literal )x ;
			//						System.out.println( " " + titleStr); 
			//
			//					}
			//				}
		}
		finally { qexec.close(); }

		
	}
}
