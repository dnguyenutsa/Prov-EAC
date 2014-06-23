package edu.utsa.cs.pbac;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import com.sun.xacml.ctx.ResponseCtx;

public class DataGenerator {
	static Model model1, model2;
	public static String hwgs = "http://peac/hwgs#";
	public static String osic = "http://peac/osic#";


	/*
	 * This method is used to generate sample RDF model with large number of triples.
	 *
	 */
	private static void generateLargeModel(int numActions){
		model2 = ModelFactory.createDefaultModel();

		Resource o1v1 = model2.createResource(hwgs + "o1v0");

		Resource upload1 = model2.createResource(hwgs + "upload1");

		Resource submit1 = model2.createResource(hwgs + "submit1");
		Resource review1 = model2.createResource(hwgs + "review1");
		Resource grade1 = model2.createResource(hwgs + "grade1");

		Resource au1 = model2.createResource(hwgs + "au1");
		Resource au2 = model2.createResource(hwgs + "au2");
		Resource au3 = model2.createResource(hwgs + "au3");

		Property gUpload = model2.createProperty(hwgs, "wasGeneratedByUpload");
		Property gReplace = model2.createProperty(hwgs, "wasGeneratedByReplace");
		Property controlledBy = model2.createProperty(hwgs, "wasControlledBy");
		Property gSubmit = model2.createProperty(hwgs, "wasGeneratedBySubmit");
		Property gReview = model2.createProperty(hwgs, "wasGeneratedByReview");
		Property gGrade = model2.createProperty(hwgs, "wasGeneratedByGrade");
		Property uInput = model2.createProperty(hwgs, "usedInput");

		// au1 uploads a homework
		// note here we can generate multiple instances of au_x to simulate
		// disjoint graphs/models
		o1v1.addProperty(gUpload, (upload1));
		upload1.addProperty(controlledBy, au1);

		// au1 replaced it arbitrary number of times
		// here we generate large number of replace to simulate depth
		Resource prevObj = o1v1;
		Resource curObj = null;
		for (int i = 1; i <= numActions; i++){
			StringBuffer objRef = new StringBuffer(hwgs).append("o1v").append(i);
			curObj = model2.createResource(objRef.toString());
			StringBuffer replaceRef = new StringBuffer(hwgs).append("replace").append(i);
			Resource replace = model2.createResource(replaceRef.toString());

			replace.addProperty(uInput, prevObj).addProperty(controlledBy, au1);
			curObj.addProperty(gReplace, replace);
			prevObj = curObj;
		}

		// au1 submitted the latest replaced homework version
		//		if (curObj == null)
		//			System.err.println("curObj is null");
		submit1.addProperty(uInput, prevObj);
		submit1.addProperty(controlledBy, au1);
		StringBuffer objRef = new StringBuffer(hwgs).append("o1v").append(numActions+1);
		curObj = model2.createResource(objRef.toString());
		curObj.addProperty(gSubmit, submit1);

		//		System.out.println("current local: " + curObj.getLocalName());
		// review process here
		// note we can generate large number of reviewers to simulate breadth
		int objId = 2;
		review1.addProperty(uInput, curObj).addProperty(controlledBy, au2);
		StringBuffer objIdRef = new StringBuffer(hwgs).append("o").append(objId)
				.append("v0");
		curObj = model2.createResource(objIdRef.toString());
		curObj.addProperty(gReview, review1);

		numActions = 4000;
		Random rand = new Random();

		for (int i = 2; i <= numActions; i++){
			StringBuffer reviewRef = new StringBuffer(hwgs).append("review").append(i);
			Resource review = model2.createResource(reviewRef.toString());
			StringBuffer reviewObjRef = new StringBuffer(hwgs).append("o").append(i).append("v0");
			Resource resource = model2.createResource(reviewObjRef.toString());

			Resource au = model2.createResource(hwgs + "au" + rand.nextInt(numActions));
			//			System.out.println(au.getURI());
			//			System.out.println(curObj.getURI());
			review.addProperty(uInput, curObj)
			//			.addProperty(controlledBy, au);
			.addProperty(controlledBy, au3).addProperty(controlledBy, au2).addProperty(controlledBy, au1);
			resource.addProperty(gReview, review);
			//			System.out.println("resource " + resource.getURI());
		}

		// grade process here
		grade1.addProperty(uInput, curObj).addProperty(controlledBy, au3);
		objIdRef = new StringBuffer(hwgs).append("o3v0");
		curObj = model2.createResource(objIdRef.toString());
		curObj.addProperty(gGrade, grade1);

		// test queries
		String qStr = "PREFIX hw: <http://peac/hwgs#>";
		qStr += "\n" + 
				"SELECT ?agent WHERE { hw:o2v0 ^hw:usedInput/hw:wasControlledBy ?agent. }";
//		System.out.println(qStr);


//		for (int i = 0; i < 20; i++){
//			long startTime = System.nanoTime(); // start timer
//			Query q = QueryFactory.create(qStr);
//			QueryExecution qexec= QueryExecutionFactory.create( q, model2 );
//			ResultSet rs= qexec.execSelect();
//			long endTime = System.nanoTime(); // end timer
//
//			long duration = endTime - startTime;
//			//			System.out.println("Evaluation Time Run #" + i + ": " + duration);
//			System.out.println(duration);
//		}
		//		while (rs.hasNext()){
		//			System.out.println(rs.next());
		//		}

	}
	
	private static void generateLargeModelForCloud(int numActions){
		model2 = ModelFactory.createDefaultModel();

		Resource o1v1 = model2.createResource(osic + "vmi1v0");

		Resource upload1 = model2.createResource(osic + "upload_image1");

		Resource modify1 = model2.createResource(osic + "modify_image1");
		Resource copyfrom1 = model2.createResource(osic + "copy_from1");
		Resource getImage1 = model2.createResource(osic + "get_image1");
		Resource spawn1 = model2.createResource(osic + "spawn1");
		
		Resource au1 = model2.createResource(osic + "au1");
		Resource au2 = model2.createResource(osic + "au2");
		Resource au3 = model2.createResource(osic + "au3");

		Property gUpload = model2.createProperty(osic, "wasGeneratedByUpload");
		Property gModify = model2.createProperty(osic, "wasGeneratedByModify");
		Property controlledBy = model2.createProperty(osic, "wasControlledBy");
		Property gCopyFrom = model2.createProperty(osic, "wasGeneratedByCopyFrom");
		Property gGetImage = model2.createProperty(osic, "wasGeneratedByGetImage");
		Property uInput = model2.createProperty(osic, "usedInput");
		
		Property gSpawn = model2.createProperty(osic, "wasGeneratedBySpawn");

		// au1 uploads a homework
		// note here we can generate multiple instances of au_x to simulate
		// disjoint graphs/models
		o1v1.addProperty(gUpload, (upload1));
		upload1.addProperty(controlledBy, au1);

		// au1 replaced it arbitrary number of times
		// here we generate large number of replace to simulate depth
		Resource prevObj = o1v1;
		Resource curObj = null;
		for (int i = 1; i <= numActions; i++){
			StringBuffer objRef = new StringBuffer(osic).append("o1v").append(i);
			curObj = model2.createResource(objRef.toString());
			StringBuffer modifyRef = new StringBuffer(osic).append("modify_image").append(i);
			Resource modify = model2.createResource(modifyRef.toString());

			modify.addProperty(uInput, prevObj).addProperty(controlledBy, au1);
			curObj.addProperty(gModify, modify);
			prevObj = curObj;
		}

		// au1 submitted the latest replaced homework version
		//		if (curObj == null)
		//			System.err.println("curObj is null");
		copyfrom1.addProperty(uInput, prevObj);
		copyfrom1.addProperty(controlledBy, au1);
		StringBuffer objRef = new StringBuffer(osic).append("vmi1v").append(numActions+1);
		curObj = model2.createResource(objRef.toString());
		curObj.addProperty(gCopyFrom, copyfrom1);

		//		System.out.println("current local: " + curObj.getLocalName());
		// review process here
		// note we can generate large number of reviewers to simulate breadth
		int objId = 2;
		getImage1.addProperty(uInput, curObj).addProperty(controlledBy, au2);
		StringBuffer objIdRef = new StringBuffer(osic).append("vmi").append(objId)
				.append("v0");
		curObj = model2.createResource(objIdRef.toString());
		curObj.addProperty(gGetImage, getImage1);

		numActions = 4000;
		Random rand = new Random();

		for (int i = 2; i <= numActions; i++){
			StringBuffer getImageRef = new StringBuffer(osic).append("get_image").append(i);
			Resource getImage = model2.createResource(getImageRef.toString());
			StringBuffer getImageObjRef = new StringBuffer(osic).append("vmi").append(i).append("v0");
			Resource resource = model2.createResource(getImageObjRef.toString());

			Resource au = model2.createResource(osic + "au" + rand.nextInt(numActions));
			//			System.out.println(au.getURI());
			//			System.out.println(curObj.getURI());
			getImage.addProperty(uInput, curObj)
			//			.addProperty(controlledBy, au);
			.addProperty(controlledBy, au3).addProperty(controlledBy, au2).addProperty(controlledBy, au1);
			resource.addProperty(gGetImage, getImage);
			//			System.out.println("resource " + resource.getURI());
		}

		// grade process here
		spawn1.addProperty(uInput, curObj).addProperty(controlledBy, au3);
		objIdRef = new StringBuffer(osic).append("o3v0");
		curObj = model2.createResource(objIdRef.toString());
		curObj.addProperty(gSpawn, spawn1);

		// test queries
//		String qStr = "PREFIX hw: <http://peac/hwgs#>";
//		qStr += "\n" + 
//				"SELECT ?agent WHERE { hw:o2v0 ^hw:usedInput/hw:wasControlledBy ?agent. }";
//		System.out.println(qStr);


//		for (int i = 0; i < 20; i++){
//			long startTime = System.nanoTime(); // start timer
//			Query q = QueryFactory.create(qStr);
//			QueryExecution qexec= QueryExecutionFactory.create( q, model2 );
//			ResultSet rs= qexec.execSelect();
//			long endTime = System.nanoTime(); // end timer
//
//			long duration = endTime - startTime;
//			//			System.out.println("Evaluation Time Run #" + i + ": " + duration);
//			System.out.println(duration);
//		}
		//		while (rs.hasNext()){
		//			System.out.println(rs.next());
		//		}

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

	/*
	 * This method is used to generate the RDF model of
	 * the provenance example of a HWGS in PST12 presentation slides.
	 */
	private static void manualGeneration(){
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

		// Sample queries over the generated model
		String prefix = "PREFIX hw: <http://peac/hwgs#>";
		String qStr = "PREFIX hw: <http://peac/hwgs#>";
		qStr += "\n" + 
				"SELECT ?process WHERE { ?process hw:wasControlledBy hw:au1. }";

		Query q = QueryFactory.create(qStr);
		QueryExecution qexec= QueryExecutionFactory.create( q, model1 );
		ResultSet rs= qexec.execSelect();

		for ( ; rs.hasNext() ;){
			String entry = rs.next().getResource("?process").toString();
		}


		String auid = "au1";
		String queryString = prefix;
		queryString += "\n" +
				"SELECT ?artifact WHERE { ?artifact (hw:wasGeneratedBySubmit/hw:wasControlledBy) hw:"+auid+". }";
		Query query = QueryFactory.create(queryString);
		qexec= QueryExecutionFactory.create( query, model1 );
		long startTime = System.nanoTime(); // start timer
		rs= qexec.execSelect();
		long endTime = System.nanoTime(); // end timer

		long duration = endTime - startTime;
		//			System.out.println("Evaluation Time Run #" + i + ": " + duration);
		System.out.println(duration);


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

	public static void main(String args[]) {

		// generate sample mock data based on grading system scenario
		// simplified scenario from pbac presentation slides
		//		manualGeneration();

		// generate sample model to mock large graph
		//generateLargeModel(100000);
		
		generateLargeModelForCloud(10000);
		FileOutputStream fout;
		try {
			fout = new FileOutputStream("osis1.rdf");
			model2.write(fout, "RDF/XML");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		// generate new dependency list and populate with sample data
		Map<String, String> localDList = DependencyStore.createNewDepList();
		populateSampleDList(localDList);

		//		System.out.println(System.getProperty("C:\\Users\\dnguyen\\workspace\\Prov-EAC\\jena.rdf"));
		//		Model rdfmodel = generateModelFromRDFFile("C:\\Users\\dnguyen\\workspace\\Prov-EAC\\jena.rdf");
	}

	public static Model getModelInstance(){
		if (model1 == null)
			manualGeneration();
		return model1;
	}

	public static Model getLargeModelInstance(int numAct){
		if (model2 == null)
			generateLargeModel(numAct);
		return model2;
	}

	private static Model generateModelFromRDFFile(String filename) {
		Model modelFromFile = ModelFactory.createDefaultModel();
		//		modelFromFile.read(filename);
		//		modelFromFile.write(System.out, "RDF/XML");
		try{
			InputStream in =new  FileInputStream(filename);
			if (in == null) {  
				System.out.println("File not found");
			}  
			modelFromFile.read(in," ");
			modelFromFile.write(System.out, "N-TRIPLE");

		}catch(Exception e){}
		return modelFromFile;

	}


	private List<String> generateAgentID(){
		ArrayList<String> retArray = new ArrayList<String>();

		return retArray;
	}

}
