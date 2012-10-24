package edu.utsa.cs.pbac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.xacml.attr.StringAttribute;

public class RDFProvModel extends ProvModel{
	private String applicationID;
	private String hwgs_prefix = "http://peac/hwgs#";
	private Model model;
	private Map<String,String> depenList;

	/**
	 * Default Constructor.
	 * Homework Grading System.
	 */
	public RDFProvModel(){
		applicationID = "HomeworkGradingSystem";
		model = ModelFactory.createDefaultModel();
		depenList = new HashMap<String,String>();
		
		manualGenerationHWGS();
	}
	
	private void manualGenerationHWGS(){
		
		Resource o1v1 = model.createResource(hwgs_prefix + "o1v1");
		Resource o1v2 = model.createResource(hwgs_prefix + "o1v2");
		Resource o1v3 = model.createResource(hwgs_prefix + "o1v3");
		Resource o2v1 = model.createResource(hwgs_prefix + "o2v1");
		Resource o3v1 = model.createResource(hwgs_prefix + "o3v1");

		Resource upload1 = model.createResource(hwgs_prefix + "upload1");
		Resource replace1 = model.createResource(hwgs_prefix + "replace1");
		Resource submit1 = model.createResource(hwgs_prefix + "submit1");
		Resource review1 = model.createResource(hwgs_prefix + "review1");
		Resource grade1 = model.createResource(hwgs_prefix + "grade1");

		Resource au1 = model.createResource(hwgs_prefix + "au1");
		Resource au2 = model.createResource(hwgs_prefix + "au2");
		Resource au3 = model.createResource(hwgs_prefix + "au3");

		Property gUpload = model.createProperty(hwgs_prefix, "wasGeneratedByUpload");
		Property gReplace = model.createProperty(hwgs_prefix, "wasGeneratedByReplace");
		Property controlledBy = model.createProperty(hwgs_prefix, "wasControlledBy");
		Property gSubmit = model.createProperty(hwgs_prefix, "wasGeneratedBySubmit");
		Property gReview = model.createProperty(hwgs_prefix, "wasGeneratedByReview");
		Property gGrade = model.createProperty(hwgs_prefix, "wasGeneratedByGrade");
		Property uInput = model.createProperty(hwgs_prefix, "usedInput");

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
	}
	
	private void populateSampleDList() {
		depenList.put("wasReplacedVof", "gReplace/uInput");
		depenList.put("wasSubmittedVof", "gSubmit/uInput");
		depenList.put("wasReviewedOof", "gReview/uInput");
		depenList.put("wasReviewedOby", "gReview/controlledBy");
		depenList.put("wasGradedOof", "gGrade/uInput");
		depenList.put("wasAuthoredBy", "wasSubmittedVof?wasReplacedVof*/gUpload/controlledBy");
		depenList.put("wasReviewedBy", "wasReviewedOofInverse/wasReviewedOby");
	}

	public ArrayList<StringAttribute> evaluateRegularPathQuery(String startingNode, String depenName){
		ArrayList<StringAttribute> results = new ArrayList<StringAttribute>();
        
		String pathPattern = resolveDepenName(depenName);
		
		String qStr = "PREFIX hw: "+ hwgs_prefix;
		qStr += "\n" + 
				"SELECT ?agent WHERE { hw:" + startingNode + " (hw:wasGeneratedByUpload/hw:wasControlledBy) ?agent. }";
		Query q = QueryFactory.create(qStr);
		QueryExecution qexec= QueryExecutionFactory.create( q, model );
		ResultSet rs= qexec.execSelect();
		
		for ( ; rs.hasNext() ;){
			String entry = rs.next().getResource("?agent").toString();
			
			entry = entry.replaceAll(DataGenerator.hwgs, "");
//			System.out.println("entry: "+entry);
			results.add(new StringAttribute(entry));
			
		}
		
		return results;
	}

	private String resolveDepenName(String depenName) {
		return null;
	}
}
