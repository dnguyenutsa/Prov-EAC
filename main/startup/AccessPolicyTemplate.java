/*
 * Created on Sep 15, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package startup;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.sun.xacml.Indenter;
import com.sun.xacml.Policy;
import com.sun.xacml.Rule;
import com.sun.xacml.Target;
import com.sun.xacml.TargetMatch;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.combine.CombiningAlgFactory;
import com.sun.xacml.combine.OrderedPermitOverridesRuleAlg;
import com.sun.xacml.combine.RuleCombiningAlgorithm;
import com.sun.xacml.cond.Apply;
import com.sun.xacml.cond.Function;
import com.sun.xacml.cond.FunctionFactory;
import com.sun.xacml.ctx.Result;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AccessPolicyTemplate {

	public static TargetMatch createTargetMatch(
		int type,
		String functionId,
		AttributeDesignator designator,
		AttributeValue value) {
		try {
			// get the factory that handles Target functions and get an
			// instance of the right function
			FunctionFactory factory = FunctionFactory.getTargetInstance();
			Function function = factory.createFunction(functionId);

			// create the TargetMatch
			return new TargetMatch(type, function, designator, value);
		} catch (Exception e) {
			return null;
		}
	}

	public static Target createPolicyTarget() throws URISyntaxException {
		List subjects = new ArrayList();
		List resources = new ArrayList();

		// create the Subject section
		List subject = new ArrayList();

		String subjectMatchId =
			"urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match";

		URI subjectDesignatorType =
			new URI("urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name");
		URI subjectDesignatorId =
			new URI("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
		//new URI("subject-id");
		AttributeDesignator subjectDesignator =
			new AttributeDesignator(
				AttributeDesignator.SUBJECT_TARGET,
				subjectDesignatorType,
				subjectDesignatorId,
				false);

		StringAttribute subjectValue = new StringAttribute("secf.com");

		subject.add(
			createTargetMatch(
				TargetMatch.SUBJECT,
				subjectMatchId,
				subjectDesignator,
				subjectValue));

		// create the Resource section
		List resource = new ArrayList();

		String resourceMatchId =
			"urn:oasis:names:tc:xacml:1.0:function:anyURI-equal";

		URI resourceDesignatorType =
			new URI("http://www.w3.org/2001/XMLSchema#anyURI");
		URI resourceDesignatorId =
			new URI("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
		AttributeDesignator resourceDesignator =
			new AttributeDesignator(
				AttributeDesignator.RESOURCE_TARGET,
				resourceDesignatorType,
				resourceDesignatorId,
				false);

		AnyURIAttribute resourceValue =
			new AnyURIAttribute(
				new URI("file:///D:/Documents/Administrator/Desktop/ProjectPlan.html"));

		resource.add(
			createTargetMatch(
				TargetMatch.RESOURCE,
				resourceMatchId,
				resourceDesignator,
				resourceValue));

		// put the Subject and Resource sections into their lists
		subjects.add(subject);
		resources.add(resource);

		// create & return the new Target
		return new Target(subjects, resources, null);
	}
	public static Target createRuleTarget() throws URISyntaxException {
		List actions = new ArrayList();

		// create the Action section
		List action = new ArrayList();

		String actionMatchId =
			"urn:oasis:names:tc:xacml:1.0:function:string-equal";

		URI actionDesignatorType =
			new URI("http://www.w3.org/2001/XMLSchema#string");
		URI actionDesignatorId =
			new URI("urn:oasis:names:tc:xacml:1.0:action:action-id");
		AttributeDesignator actionDesignator =
			new AttributeDesignator(
				AttributeDesignator.ACTION_TARGET,
				actionDesignatorType,
				actionDesignatorId,
				false);

		StringAttribute actionValue = new StringAttribute("open");

		action.add(
			createTargetMatch(
				TargetMatch.ACTION,
				actionMatchId,
				actionDesignator,
				actionValue));

		// put the Action section in the Actions list
		actions.add(action);

		// create & return the new Target
		return new Target(null, null, actions);
	}
	public static Apply createRuleCondition() throws URISyntaxException {
		List conditionArgs = new ArrayList();

		// get the function that the condition uses
		FunctionFactory factory = FunctionFactory.getConditionInstance();
		Function conditionFunction = null;
		try {
			conditionFunction =
				factory.createFunction(
					"urn:oasis:names:tc:xacml:1.0:function:" + "string-equal");
		} catch (Exception e) {
			// see comment in createTargetMatch()
			return null;
		}

		// now create the apply section that gets the designator value
		List applyArgs = new ArrayList();

		factory = FunctionFactory.getGeneralInstance();
		Function applyFunction = null;
		try {
			applyFunction =
				factory.createFunction(
					"urn:oasis:names:tc:xacml:1.0:function:"
						+ "string-one-and-only");
		} catch (Exception e) {
			// see comment in createTargetMatch()
			return null;
		}

		URI designatorType = new URI("http://www.w3.org/2001/XMLSchema#string");
		URI designatorId = new URI("group");
		AttributeDesignator designator =
			new AttributeDesignator(
				AttributeDesignator.SUBJECT_TARGET,
				designatorType,
				designatorId,
				false,
				null);
		applyArgs.add(designator);

		Apply apply = new Apply(applyFunction, applyArgs, false);

		// add the new apply element to the list of inputs to the condition
		conditionArgs.add(apply);

		// create an AttributeValue and add it to the input list
		StringAttribute value = new StringAttribute("owner");
		conditionArgs.add(value);

		// finally, create & return our Condition
		return new Apply(conditionFunction, conditionArgs, true);
	}
	public static List createRules() throws URISyntaxException {
		// define the identifier for the rule
		URI ruleId = new URI("ProjectPlanAccessRule");

		// define the effect for the Rule
		int effect = Result.DECISION_PERMIT;

		// get the Target for the rule
		Target target = createRuleTarget();

		// get the Condition for the rule
		Apply condition = createRuleCondition();

		Rule openRule = new Rule(ruleId, effect, null, target, condition);


		// create a list for the rules and add the rule to it

		List ruleList = new ArrayList();
		ruleList.add(openRule);

		return ruleList;
	}

	public static void main(String[] args) throws Exception {
		// define the identifier for the AccessPolicy
		URI policyId = new URI("ProjectPlanAccessPolicy");

		// get the combining algorithm for the AccessPolicy
		URI combiningAlgId = new URI(OrderedPermitOverridesRuleAlg.algId);
		CombiningAlgFactory factory = CombiningAlgFactory.getInstance();
		RuleCombiningAlgorithm combiningAlg =
			(RuleCombiningAlgorithm) (factory.createAlgorithm(combiningAlgId));

		// add a description for the AccessPolicy
		String description =
			"This AccessPolicy applies to any account at secf.com "
				+ "accessing file:///D:/Documents/Administrator/Desktop/Project Plan.html.";

		// create the target for the AccessPolicy
		Target policyTarget = createPolicyTarget();

		// create rules
		List ruleList = createRules();

		// create the AccessPolicy
		Policy policy =
			new Policy(
				policyId,
				combiningAlg,
				description,
				policyTarget,
				ruleList);

		// finally, encode the AccessPolicy and print it to standard out
		policy.encode(System.out, new Indenter());
	}

}
