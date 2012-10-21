/*
 * Created on Sep 14, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eklavya.accesscontrol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Indenter;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.RFC822NameAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.Subject;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AccessPolicyEnforcementPoint {

	public static Set setupSubjects() throws URISyntaxException {
		HashSet attributes = new HashSet();

		// setup the id and value for the requesting subject
		URI subjectId = new URI("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
		RFC822NameAttribute value = new RFC822NameAttribute("mverma@secf.com");

		// create the subject section with two attributes, the first with
		// the subject's identity...
		attributes.add(new Attribute(subjectId, null, null, value));
		// ...and the second with the subject's group membership
		
		URI groupId = new URI("group");
		StringAttribute stringAttribValue = new StringAttribute("owner");
		
		attributes.add(new Attribute(groupId,null,null,stringAttribValue));
		
		// bundle the attributes in a Subject with the default category
		HashSet subjects = new HashSet();
		subjects.add(new Subject(attributes));

		return subjects;
	}

	public static Set setupResource() throws URISyntaxException {
		HashSet resource = new HashSet();

		// the resource being requested
		AnyURIAttribute value =
			new AnyURIAttribute(new URI("file:///D:/Documents/Administrator/Desktop/ProjectPlan.html"));

		// create the resource using a standard, required identifier for
		// the resource being requested
		resource.add(
			new Attribute(
				new URI(EvaluationCtx.RESOURCE_ID),
				null,
				null,
				value));

		return resource;
	}
	public static Set setupAction() throws URISyntaxException {
		HashSet action = new HashSet();

		// this is a standard URI that can optionally be used to specify
		// the action being requested
		URI actionId = new URI("urn:oasis:names:tc:xacml:1.0:action:action-id");

		// create the action
		action.add(
			new Attribute(actionId, null, null, new StringAttribute("open")));

		return action;
	}

	/**
	 * Command-line interface that creates a new Request by invoking the
	 * static methods in this class. The Request has no Environment section.
	 */
	public static void main(String[] args) throws Exception {
		// create the new Request...note that the Environment must be specified
		// using a valid Set, even if that Set is empty
		RequestCtx request =
			new RequestCtx(
				setupSubjects(),
				setupResource(),
				setupAction(),
				new HashSet());

		// encode the Request and print it to standard out
		request.encode(System.out, new Indenter());
	}

}
