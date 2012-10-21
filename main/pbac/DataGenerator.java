package pbac;

import java.util.Map;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;

public class DataGenerator {
	static Model model1;
	public static String hwgs = "http://peac/hwgs#";
	// Manual Data Generation
	public static void manualGeneration(){
		model1 = ModelFactory.createDefaultModel();

		Resource o1v1 = model1.createResource(hwgs + "o1v1");
		Resource o1v2 = model1.createResource(hwgs + "o1v2");
		Resource o1v3 = model1.createResource(hwgs + "o1v3");
		Resource o2v1 = model1.createResource(hwgs + "o2v1");
		Resource o3v1 = model1.createResource(hwgs + "o3v1");

		Resource upload1 = model1.createResource(hwgs + "upload1");
		Resource replace1 = model1.createResource(hwgs + "replace1");
		Resource submit1 = model1.createResource(hwgs + "submit1");
		Resource review1 = model1.createResource(hwgs + "review1");
		Resource grade1 = model1.createResource(hwgs + "grade1");

		Resource au1 = model1.createResource(hwgs + "au1");
		Resource au2 = model1.createResource(hwgs + "au2");
		Resource au3 = model1.createResource(hwgs + "au3");

		Property gUpload = model1.createProperty(hwgs, "wasGeneratedByUpload");
		Property gReplace = model1.createProperty(hwgs, "wasGeneratedByReplace");
		Property controlledBy = model1.createProperty(hwgs, "wasControlledBy");
		Property gSubmit = model1.createProperty(hwgs, "wasGeneratedBySubmit");
		Property gReview = model1.createProperty(hwgs, "wasGeneratedByReview");
		Property gGrade = model1.createProperty(hwgs, "wasGeneratedByGrade");
		Property uInput = model1.createProperty(hwgs, "usedInput");

		//		System.out.println(uInput.getURI());

		o1v1.addProperty(gUpload, (upload1));
		upload1.addProperty(controlledBy, au1);
		replace1.addProperty(uInput, o1v1).addProperty(controlledBy, au1);
		o1v2.addProperty(gReplace, replace1);
		submit1.addProperty(uInput, o1v2).addProperty(controlledBy, au1);
		o1v3.addProperty(gSubmit, submit1);
		review1.addProperty(uInput, o1v3).addProperty(controlledBy, au2);
		o2v1.addProperty(gReview, review1);
		grade1.addProperty(uInput, o1v3).addProperty(controlledBy, au3);
		o3v1.addProperty(gGrade, grade1);

		//		o1v1.addProperty(DC.description, upload1);

		//		model1.write(System.out);
//		model1.write(System.out, "RDF/XML-ABBREV");

//		printModel(model1);

		String prefix = "PREFIX hw: <http://peac/hwgs#>";
		String qStr = "PREFIX hw: <http://peac/hwgs#>";
		qStr += "\n" + 
				"SELECT ?process WHERE { ?process hw:wasControlledBy hw:au1. }";
		
		Query q = QueryFactory.create(qStr);
		QueryExecution qexec= QueryExecutionFactory.create( q, model1 );
		ResultSet rs= qexec.execSelect();

		//		ResultSetFormatter.out(System.out, rs, q);
		for ( ; rs.hasNext() ;){
			String entry = rs.next().getResource("?process").toString();
			System.out.println(entry);

		}
		String auid = "au1";
		String queryString = prefix;
		queryString += "\n" +
				"SELECT ?artifact WHERE { ?artifact (hw:wasGeneratedBySubmit/hw:wasControlledBy) hw:"+auid+". }";
		Query query = QueryFactory.create(queryString);
		qexec= QueryExecutionFactory.create( query, model1 );
		rs= qexec.execSelect();

		//		ResultSetFormatter.out(System.out, rs, query);

		for ( ; rs.hasNext() ;){

			System.out.println(rs.next());
		}


	}


	// Data generation from RDF file

	static public void printModel(Model m){

		// list the statements in Model m
		StmtIterator iter = m.listStatements();

		// print out the predicate, subject and object of each statement
		while (iter.hasNext()){
			Statement stmt = iter.nextStatement();
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();

			System.out.print(subject.toString());
			System.out.print(" " + predicate.toString() + " ");
			if (object instanceof Resource) {
				System.out.print(object.toString());
			} else {
				// object is a literal
				System.out.print(" \"" + object.toString() + "\"");
			}

			System.out.println(" .");
		}
	}

	private static void populateSampleDList(Map<String, String> localDList) {

		localDList.put("wasReplacedVof", "gReplace.uInput");
		localDList.put("wasSubmittedVof", "gSubmit.uInput");
		localDList.put("wasReviewedOof", "gReview.uInput");
		localDList.put("wasReviewedOby", "gReview.controlledBy");
		localDList.put("wasGradedOof", "gGrade.uInput");
		localDList.put("wasAuthoredBy", "wasSubmittedVof?wasReplacedVof*.gUpload.controlledBy");
		localDList.put("wasReviewedBy", "wasReviewedOofInverse.wasReviewedOby");
	}

	public static void main(String args[]){

		// generate sample mock data based on grading system scenario
		// simplified scenario from pbac presentation slides
		manualGeneration();

		//		generateModelFromRDFFile("/sample/sample.rdf");

		// generate new dependency list and populate with sample data
		Map<String, String> localDList = DependencyStore.createNewDepList();
		populateSampleDList(localDList);
	}

	public static Model getModelInstance(){
		if (model1 == null)
			manualGeneration();
		return model1;
	}


	private static Model generateModelFromRDFFile(String filename) {
		Model modelFromFile = ModelFactory.createDefaultModel();
		modelFromFile.read(filename);
		modelFromFile.write(System.out, "RDF/XML");

		return modelFromFile;

	}



}
