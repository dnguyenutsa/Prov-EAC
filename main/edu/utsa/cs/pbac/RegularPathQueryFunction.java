package edu.utsa.cs.pbac;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.cond.FunctionBase;

public class RegularPathQueryFunction extends FunctionBase{

	// the name of the function, which will be used publicly
	public static final String NAME = "regular-path-query-function";

	// the parameter types, in order, and whether or not they're bags
	private static final String params [] = { StringAttribute.identifier,
		StringAttribute.identifier,	
		StringAttribute.identifier};
	private static final boolean bagParams [] = { false, false, false };

	public RegularPathQueryFunction() {
		// use the constructor that handles mixed argument types
		super(NAME, 0, params, bagParams, StringAttribute.identifier,
				true);
	}

	public EvaluationResult evaluate(List inputs, EvaluationCtx context) {
		// Evaluate the arguments using the helper method...this will
		// catch any errors, and return values that can be compared
		AttributeValue [] argValues = new AttributeValue[inputs.size()];
		EvaluationResult result = evalArgs(inputs, context, argValues);
		if (result != null)
			return result;

		// cast the resolved values into specific types
		// assuming the order of parameters to function
		// starting node is an objectId, on which, the regular path pattern
		// associated with depenName is applied on to obtain a result nodes set. 

		StringAttribute startingNode = (StringAttribute) (argValues[0]);
		StringAttribute depenName = (StringAttribute) (argValues[1]);

		String startingNodeStr = startingNode.getValue();
		String depenNameStr = depenName.getValue();
		String queryType = ((StringAttribute) (argValues[2])).getValue().trim();

		// obtain Jena model from memory
		// can later provide interface to SDB
		Model hwgsModel = DataGenerator.getModelInstance(); 
		
		// obtain dependency list
		Map<String,String> hwgsDepenList = DependencyStore.createNewDepList();
		
		if (queryType.equalsIgnoreCase("UserAuthorization")){
			
			String wasAuthoredBy = hwgsDepenList.get(depenNameStr.trim());

			String qStr = "PREFIX hw: <http://peac/hwgs#>";
			qStr += "\n" + 
					"SELECT ?agent WHERE { hw:" + startingNodeStr + " " + wasAuthoredBy
					+ " ?agent. }";
			System.out.println(qStr);
			Query q = QueryFactory.create(qStr);
			QueryExecution qexec= QueryExecutionFactory.create( q, hwgsModel );
			ResultSet rs= qexec.execSelect();

			ArrayList<StringAttribute> results = new ArrayList<StringAttribute>();

			for ( ; rs.hasNext() ;){
				String entry = rs.next().getResource("?agent").toString();

				entry = entry.replaceAll(DataGenerator.hwgs, "");
				results.add(new StringAttribute(entry));
			}

			try {
				return (new EvaluationResult(new BagAttribute(new URI(StringAttribute.identifier), results)));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		else if (queryType.equalsIgnoreCase("ActionValidation"))
		{
			String wasSubmittedVof = hwgsDepenList.get(depenNameStr.trim());

			String qStr = "PREFIX hw: <http://peac/hwgs#>";
			qStr += "\n" + 
					"SELECT ?artifact WHERE { hw:" + startingNodeStr + " " + wasSubmittedVof
					+ " ?artifact. }";
			System.out.println(qStr);
			Query q = QueryFactory.create(qStr);
			QueryExecution qexec= QueryExecutionFactory.create( q, hwgsModel );
			ResultSet rs= qexec.execSelect();
			
			ArrayList<StringAttribute> results = new ArrayList<StringAttribute>();
			for ( ; rs.hasNext() ;){
				System.out.println("hey");
				results.add(new StringAttribute(rs.next().toString()));
			}

			try {
				return (new EvaluationResult(new BagAttribute(new URI(StringAttribute.identifier), results)));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
