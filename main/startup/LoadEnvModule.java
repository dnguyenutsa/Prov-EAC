package startup;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.finder.AttributeFinderModule;

public class LoadEnvModule extends AttributeFinderModule {

    // always return true, since this is a feature we always support
    public boolean isDesignatorSupported() {
        return true;
    }

    // return a single identifier that shows support for environment attrs
    public Set getSupportedDesignatorTypes() {
        Set set = new HashSet();
        set.add(new Integer(AttributeDesignator.ENVIRONMENT_TARGET));
        return set;
    }

    public EvaluationResult findAttribute(URI attributeType,
                                          URI attributeId,
                                          URI issuer, URI subjectCategory,
                                          EvaluationCtx context,
                                          int designatorType) {
        // make sure this is an Environment attribute
        if (designatorType != AttributeDesignator.ENVIRONMENT_TARGET)
            return new EvaluationResult(BagAttribute.
                                        createEmptyBag(attributeType));
        
        // make sure they're asking for our identifier
        if (! attributeId.toString().equals("processor-load"))
            return new EvaluationResult(BagAttribute.
                                        createEmptyBag(attributeType));
                                        
        // make sure they're asking for an integer return value
        if (! attributeType.toString().equals(IntegerAttribute.identifier))
            return new EvaluationResult(BagAttribute.
                                        createEmptyBag(attributeType));

        // now that everything checks out, get the load...
//        long procLoad = someMethodToCalculateProcessorLoad();
        long procLoad = 0;

        // ... and return that value
        Set set = new HashSet();
        set.add(new IntegerAttribute(procLoad));
        return new EvaluationResult(new BagAttribute(attributeType, set));
    }

}
