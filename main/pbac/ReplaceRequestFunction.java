package pbac;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.cond.FunctionBase;

public class ReplaceRequestFunction extends FunctionBase{

	// the name of the function, which will be used publicly
    public static final String NAME = "replace-request-function";

    // the parameter types, in order, and whether or not they're bags
    private static final String params [] = { StringAttribute.identifier,
                                              StringAttribute.identifier};
    private static final boolean bagParams [] = { false, false };

    public ReplaceRequestFunction() {
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
//        BooleanAttribute bool = (BooleanAttribute)(argValues[0]);
//        StringAttribute str = (StringAttribute)(argValues[1]);
        boolean evalResult = false;
        
        // assuming the order of parameters to function
        StringAttribute objectId = (StringAttribute) (argValues[0]);
        StringAttribute depenName = (StringAttribute) (argValues[1]);
//        StringAttribute agentId = (StringAttribute) (argValues[2]);
        
        String objectIdStr = objectId.getValue();
        String depenNameStr = depenName.getValue();
        
        System.out.println(objectIdStr);
        System.out.println(depenNameStr);
        
        
        // obtain Jena model from memory
        // can later provide interface to SDB
        Model hwgsModel = DataGenerator.getModelInstance(); 
//        DataGenerator.printModel(hwgsModel);
        
        String prefix = "PREFIX hw: <http://peac/hwgs#>";
		String qStr = "PREFIX hw: <http://peac/hwgs#>";
		qStr += "\n" + 
				"SELECT ?agent WHERE { hw:" + objectIdStr + " (hw:wasGeneratedByUpload/hw:wasControlledBy) ?agent. }";
		Query q = QueryFactory.create(qStr);
		QueryExecution qexec= QueryExecutionFactory.create( q, hwgsModel );
		ResultSet rs= qexec.execSelect();
		
//		System.out.println("rs: " + rs);
		
		ArrayList<StringAttribute> results = new ArrayList<StringAttribute>();
		
		for ( ; rs.hasNext() ;){
			String entry = rs.next().getResource("?agent").toString();
			
			entry = entry.replaceAll(DataGenerator.hwgs, "");
			System.out.println("entry: "+entry);
			results.add(new StringAttribute(entry));
			
		}
		
		try {
			return (new EvaluationResult(new BagAttribute(new URI(StringAttribute.identifier), results)));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
//		EvaluationResult eval = new EvaluationResult(BagAttribute.createEmptyBag(null));
		
//      return EvaluationResult.getInstance(evalResult);
    }

}
