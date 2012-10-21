package pbac;

import java.util.List;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.cond.FunctionBase;

public class BoolTextCompare extends FunctionBase {
    
    // the name of the function, which will be used publicly
    public static final String NAME = "bool-text-compare";

    // the parameter types, in order, and whether or not they're bags
    private static final String params [] = { BooleanAttribute.identifier,
                                              StringAttribute.identifier };
    private static final boolean bagParams [] = { false, false };

    public BoolTextCompare() {
        // use the constructor that handles mixed argument types
        super(NAME, 0, params, bagParams, BooleanAttribute.identifier,
              false);
    }

    public EvaluationResult evaluate(List inputs, EvaluationCtx context) {
        // Evaluate the arguments using the helper method...this will
        // catch any errors, and return values that can be compared
        AttributeValue [] argValues = new AttributeValue[inputs.size()];
        EvaluationResult result = evalArgs(inputs, context, argValues);
        if (result != null)
            return result;

        // cast the resolved values into specific types
        BooleanAttribute bool = (BooleanAttribute)(argValues[0]);
        StringAttribute str = (StringAttribute)(argValues[1]);
        boolean evalResult;

        // now compare the values
        if (bool.getValue()) {
            // see if the string is "true"
            evalResult = str.getValue().equals("true");
        } else {
            // see if the string is "false"
            evalResult = str.getValue().equals("false");
        }

        // boolean returns are common, so there's a getInstance() for that
        return EvaluationResult.getInstance(evalResult);
    }
}