
/*
 * @(#)PolicyReference.java
 *
 * Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

package com.sun.xacml;

import com.sun.xacml.combine.CombiningAlgorithm;

import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;

import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderResult;

import java.io.OutputStream;
import java.io.PrintStream;

import java.net.URI;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class is used as a placeholder for the PolicyIdReference and
 * PolicySetIdReference fields in a PolicySetType. When a reference is used
 * in a policy set, it is telling the PDP to use an external policy in
 * the current policy. Each time the PDP needs to evaluate that policy
 * reference, it asks the policy finder for the policy. Typically the policy
 * finder will have cached the referenced policy, so this isn't too slow.
 * <p>
 * NOTE: all of the accessor methods, the match method, and the evaluate method
 * require this class to ask its <code>PolicyFinder</code> for the referenced
 * policy, which can be a slow operation. Care should be taken, therefore in
 * calling these methods too often. Also note that it's not safe to cache the
 * results of these calls, since the referenced policy may change.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class PolicyReference extends AbstractPolicy
{
    
    /**
     * Identifies this as a reference to a <code>Policy</code>
     */
    public static final int POLICY_REFERENCE = 0;

    /**
     * Identifies this as a reference to a <code>PolicySet</code>
     */
    public static final int POLICYSET_REFERENCE = 1;

    // the reference
    private URI reference;

    // the reference type
    private int policyType;

    // the finder to use in finding the referenced policy
    private PolicyFinder finder;

    // the logger we'll use for all messages
    private static final Logger logger =
        Logger.getLogger(PolicyReference.class.getName());

    /**
     * Creates a new <code>PolicyReference</code>.
     *
     * @param reference the reference to the policy
     * @param policyType one of the two fields in this class
     * @param finder the <code>PolicyFinder</code> used to handle the reference
     *
     * @throws IllegalArgumentException if the input policyType isn't valid
     */
    public PolicyReference(URI reference, int policyType, 
                           PolicyFinder finder) 
        throws IllegalArgumentException{

        // check if input policyType is a valid value
        if ((policyType != POLICY_REFERENCE) && 
            (policyType != POLICYSET_REFERENCE))
            throw new IllegalArgumentException("Input policyType is not a" +
                                               "valid value");

        this.reference = reference;
        this.policyType = policyType;
        this.finder = finder;
    }

    /**
     * Creates an instance of a <code>PolicyReference</code> object based on
     * a DOM node.
     *
     * @param root the DOM root of a PolicyIdReference or a 
     *             PolicySetIdReference XML type
     * @param finder the <code>PolicyFinder</code> used to handle the reference
     *
     * @exception ParsingException if the node is invalid
     */
    public static PolicyReference getInstance(Node root, PolicyFinder finder)
        throws ParsingException
    {
        URI reference = null;
        int policyType;
        
        String name = root.getNodeName();
        if (name.equals("PolicyIdReference")) {
            policyType = POLICY_REFERENCE;
        } else if (name.equals("PolicySetIdReference")) {
            policyType = POLICYSET_REFERENCE;
        } else {
            throw new ParsingException("Unknown reference type: " + name);
        }

        try {
            reference = new URI(root.getFirstChild().getNodeValue());
        } catch (Exception e) {
            throw new ParsingException("Invalid URI in Reference", e);
        }

        return new PolicyReference(reference, policyType, finder);
    }

    /**
     * Returns the id of this policy. If the policy is invalid or can't be
     * retrieved, then a runtime exception is thrown.
     *
     * @return the policy id
     *
     * @throws ProcessingException if the referenced policy can't be retrieved
     */
    public URI getId() {
        return resolvePolicy().getId();
    }

    /**
     * Returns the combining algorithm used by this policy. If the policy is
     * invalid or can't be retrieved, then a runtime exception is thrown.
     *
     * @return the combining algorithm
     *
     * @throws ProcessingException if the referenced policy can't be retrieved
     */
    public CombiningAlgorithm getCombiningAlg() {
        return resolvePolicy().getCombiningAlg();
    }

    /**
     * Returns the given description of this policy or null if there is no
     * description. If the policy is invalid or can't be retrieved, then a
     * runtime exception is thrown.
     *
     * @return the description or null
     *
     * @throws ProcessingException if the referenced policy can't be retrieved
     */
    public String getDescription() {
        return resolvePolicy().getDescription();
    }

    /**
     * Returns the target for this policy. If the policy is invalid or can't be
     * retrieved, then a runtime exception is thrown.
     *
     * @return the policy's target
     *
     * @throws ProcessingException if the referenced policy can't be retrieved
     */
    public Target getTarget() {
        return resolvePolicy().getTarget();
    }

    /**
     * Returns the default version for this policy. If the policy is 
     * invalid or can't be retrieved, then a runtime exception is thrown.
     *
     * @return the policy's default version
     *
     * @throws ProcessingException if the referenced policy can't be retrieved
     */
    public String getDefaultVersion() {
        return resolvePolicy().getDefaultVersion();
    }

    /**
     * Returns the child policy nodes under this node in the policy tree. If
     * the policy is invalid or can't be retrieved, then a runtime exception
     * is thrown.
     *
     * @return the <code>List</code> of child policy nodes
     *
     * @throws ProcessingException if the referenced policy can't be retrieved
     */
    public List getChildren() {
        return resolvePolicy().getChildren();
    }

    /**
     * Returns the Set of obligations for this policy, which may be empty if 
     * there are no obligations. If the policy is invalid or can't be 
     * retrieved, then a runtime exception is thrown.
     *
     * @return the policy's obligations
     *
     * @throws ProcessingException if the referenced policy can't be retrieved
     */
    public Set getObligations() {
        return resolvePolicy().getObligations();
    }

    /**
     * Given the input context sees whether or not the request matches this
     * policy. This must be called by combining algorithms before they
     * evaluate a policy. This is also used in the initial policy finding
     * operation to determine which top-level policies might apply to the
     * request. If the policy is invalid or can't be retrieved, then a
     * runtime exception is thrown.
     *
     * @param context the representation of the request
     *
     * @return the result of trying to match the policy and the request
     */
    public MatchResult match(EvaluationCtx context) {
        try {
            return getTarget().match(context);
        } catch (ProcessingException pe) {
            // this means that we couldn't resolve the policy
            ArrayList code = new ArrayList();
            code.add(Status.STATUS_PROCESSING_ERROR);
            Status status = new Status(code, "couldn't resolve policy ref");
            return new MatchResult(MatchResult.INDETERMINATE, status);
        }
    }

    /**
     * Private helper method that tried to resolve the policy
     */
    private AbstractPolicy resolvePolicy() {
        // see if this reference was setup with a finder
        if (finder == null) {
            if (logger.isLoggable(Level.WARNING))
                logger.warning("PolicyReference with id " +
                               reference.toString() + " was queried but was " +
                               "not configured with a PolicyFinder");

            throw new ProcessingException("couldn't find the policy with " +
                                          "a null finder");
        }

        PolicyFinderResult pfr = finder.findPolicy(reference, policyType);
        
        if (pfr.notApplicable())
            throw new ProcessingException("couldn't resolve the policy");
        
        if (pfr.indeterminate())
            throw new ProcessingException("error resolving the policy");

        return pfr.getPolicy();
    }

    /**
     * Tries to evaluate the policy by calling the combining algorithm on
     * the given policies or rules. The <code>match</code> method must always
     * be called first, and must always return MATCH, before this method
     * is called.
     *
     * @param context the representation of the request
     *
     * @return the result of evaluation
     */
    public Result evaluate(EvaluationCtx context) {
        // if there is no finder, then we return NotApplicable
        if (finder == null)
            return new Result(Result.DECISION_NOT_APPLICABLE,
                              context.getResourceId().encode());

        PolicyFinderResult pfr = finder.findPolicy(reference, policyType);

        // if we found nothing, then we return NotApplicable
        if (pfr.notApplicable())
            return new Result(Result.DECISION_NOT_APPLICABLE,
                              context.getResourceId().encode());

        // if there was an error, we return that status data
        if (pfr.indeterminate())
            return new Result(Result.DECISION_INDETERMINATE, pfr.getStatus(),
                              context.getResourceId().encode());

        // we must have found a policy
        return pfr.getPolicy().evaluate(context);
    }

    /**
     * Encodes this <code>PolicyReference</code> into its XML representation
     * and writes this encoding to the given <code>OutputStream</code> with
     * no indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     */
    public void encode(OutputStream output) {
        encode(output, new Indenter(0));
    }

    /**
     * Encodes this <code>PolicyReference</code> into its XML representation
     * and writes this encoding to the given <code>OutputStream</code> with
     * indentation.
     *
     * @param output a stream into which the XML-encoded data is written
     * @param indenter an object that creates indentation strings
     */
    public void encode(OutputStream output, Indenter indenter) {
        PrintStream out = new PrintStream(output);
        String encoded = indenter.makeString();
        
        if (policyType == POLICY_REFERENCE) {
            out.println(encoded + "<PolicyIdReference>" +
                        reference.toString() + "</PolicyIdReference>");
        } else {
            out.println(encoded + "<PolicySetIdReference>" +
                        reference.toString() + "</PolicySetIdReference>");
        }
    }

}
