package edu.utsa.cs.pbac;

//public class ProvPDP {

// specify a sample policy in xml
// make mock function to see if it can be invoked with PDP using a sample xml policy
// need two functions for evaluating action validation and user authorization


//}

/*
 * @(#)SimplePDP.java
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


import com.hp.hpl.jena.rdf.model.Model;
import com.sun.xacml.ConfigurationStore; 
import com.sun.xacml.Indenter;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;

import com.sun.xacml.cond.FunctionFactory;
import com.sun.xacml.cond.FunctionFactoryProxy;
import com.sun.xacml.cond.StandardFunctionFactory;

import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.ResponseCtx;

import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.PolicyFinder;

import com.sun.xacml.finder.impl.CurrentEnvModule;
import com.sun.xacml.finder.impl.FilePolicyModule;
import com.sun.xacml.finder.impl.SelectorModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sample.TimeInRangeFunction;
import startup.LoadEnvModule;


/**
 * This is a simple, command-line driven XACML PDP. 
 *
 * @since 1.1
 * @author seth proctor
 */
public class ProvPDP
{

	// this is the actual PDP object we'll use for evaluation
	private PDP pdp = null;
	static Model hwgsLargeModel; 
	static int thrdcount = 0;

	/**
	 * Default constructor. This creates a <code>SimplePDP</code> with a
	 * <code>PDP</code> based on the configuration defined by the runtime
	 * property com.sun.xcaml.PDPConfigFile.
	 */
	public ProvPDP() throws Exception {
		// load the configuration
		ConfigurationStore store = new ConfigurationStore();

		// use the default factories from the configuration
		store.useDefaultFactories();

		// get the PDP configuration's and setup the PDP
		pdp = new PDP(store.getDefaultPDPConfig());
	}

	/**
	 * Constructor that takes an array of filenames, each of which
	 * contains an XACML policy, and sets up a <code>PDP</code> with access
	 * to these policies only. The <code>PDP</code> is configured
	 * programmatically to have only a few specific modules.
	 *
	 * @param policyFiles an array of filenames that specify policies
	 */
	public ProvPDP(String [] policyFiles) throws Exception {
		// Create a PolicyFinderModule and initialize it...in this case,
		// we're using the sample FilePolicyModule that is pre-configured
		// with a set of policies from the filesystem
		FilePolicyModule filePolicyModule = new FilePolicyModule();
		for (int i = 0; i < policyFiles.length; i++)
			filePolicyModule.addPolicy(policyFiles[i]);

		// next, setup the PolicyFinder that this PDP will use
		PolicyFinder policyFinder = new PolicyFinder();
		Set policyModules = new HashSet();
		policyModules.add(filePolicyModule);
		policyFinder.setModules(policyModules);

		// now setup attribute finder modules for the current date/time and
		// AttributeSelectors (selectors are optional, but this project does
		// support a basic implementation)
		CurrentEnvModule envAttributeModule = new CurrentEnvModule();
		SelectorModule selectorAttributeModule = new SelectorModule();
		//		LoadEnvModule aLoadEnvModule = new LoadEnvModule();

		// Setup the AttributeFinder just like we setup the PolicyFinder. Note
		// that unlike with the policy finder, the order matters here. See the
		// the javadocs for more details.
		AttributeFinder attributeFinder = new AttributeFinder();
		List attributeModules = new ArrayList();
		attributeModules.add(envAttributeModule);
		attributeModules.add(selectorAttributeModule);
		//        attributeModules.add(aLoadEnvModule);
		attributeFinder.setModules(attributeModules);

		// Try to load the time-in-range function, which is used by several
		// of the examples...see the documentation for this function to
		// understand why it's provided here instead of in the standard
		// code base.
		FunctionFactoryProxy proxy =
				StandardFunctionFactory.getNewFactoryProxy();
		FunctionFactory factory = proxy.getConditionFactory();
		factory.addFunction(new TimeInRangeFunction());

		//        factory.addFunction(new BoolTextCompare());
		factory.addFunction(new RegularPathQueryFunction());

		FunctionFactory.setDefaultFactory(proxy);

		// finally, initialize our pdp
		pdp = new PDP(new PDPConfig(attributeFinder, policyFinder, null));
	}

	/**
	 * Evaluates the given request and returns the Response that the PDP
	 * will hand back to the PEP.
	 *
	 * @param requestFile the name of a file that contains a Request
	 *
	 * @return the result of the evaluation
	 *
	 * @throws IOException if there is a problem accessing the file
	 * @throws ParsingException if the Request is invalid
	 */
	public ResponseCtx evaluate(String requestFile)
			throws IOException, ParsingException
			{
		// setup the request based on the file
		RequestCtx request =
				RequestCtx.getInstance(new FileInputStream(requestFile));

		// evaluate the request
		return pdp.evaluate(request);
			}

	/**
	 * Main-line driver for this sample code. This method lets you invoke
	 * the PDP directly from the command-line.
	 *
	 * @param args the input arguments to the class. They are either the
	 *             flag "-config" followed by a request file, or a request
	 *             file followed by one or more policy files. In the case
	 *             that the configuration flag is used, the configuration
	 *             file must be specified in the standard java property,
	 *             com.sun.xacml.PDPConfigFile.
	 */
	public static void main(String [] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Usage: -config <request>");
			System.out.println("       <request> <policy> [policies]");
			System.exit(1);
		}

		ProvPDP simplePDP = null;
		String requestFile = null;

		if (args[0].equals("-config")) {
			requestFile = args[1];
			simplePDP = new ProvPDP();
		} else {
			requestFile = args[0];
			String [] policyFiles = new String[args.length - 1];

			for (int i = 1; i < args.length; i++)
				policyFiles[i-1] = args[i];

			simplePDP = new ProvPDP(policyFiles);
		}

		initializeModel();

		// Generate graph model here to avoid overhead in evaluation
		//		Model hwgsModel = DataGenerator.getModelInstance(); 
		Model hwgsLargeModel = DataGenerator.getLargeModelInstance(6000); 
		// warmup run
		ResponseCtx response = simplePDP.evaluate(requestFile);
//		response.encode(System.out, new Indenter());

		
		long startTime, endTime, duration;
		
		// evaluate single request
//		evaluateOneRequest(simplePDP, requestFile);

		// evaluate multiple requests with parallelization

		String currentDir = System.getProperty("user.dir");
		
		// for 50 requests
		File requestFolder = new File(currentDir + "/sample/request-50/");
		
		
//		startTime = System.nanoTime(); // start timer
//		evaluateMultipleRequests(simplePDP, requestFolder);
//		while (thrdcount < 50)
//			Thread.sleep(100);
//		endTime = System.nanoTime(); // end timer
//		duration = endTime - startTime;
//		System.out.println(duration);
		
		// for 100 requests
//		requestFolder = new File(currentDir + "/sample/request-100/");
//		
//		startTime = System.nanoTime(); // start timer
//		evaluateMultipleRequests(simplePDP, requestFolder);
//		while (thrdcount < 100)
//			Thread.sleep(100);
//		endTime = System.nanoTime(); // end timer
//		duration = endTime - startTime;
//		System.out.println(duration);
		
		// for 200 requests
//		requestFolder = new File(currentDir + "/sample/request-200/");
//		
//		startTime = System.nanoTime(); // start timer
//		evaluateMultipleRequests(simplePDP, requestFolder);
//		while (thrdcount < 200)
//			Thread.sleep(1000);
//		endTime = System.nanoTime(); // end timer
//		duration = endTime - startTime;
//		System.out.println(duration);
		
		// for 400 requests
//		requestFolder = new File(currentDir + "/sample/request-400/");
//		
//		startTime = System.nanoTime(); // start timer
//		evaluateMultipleRequests(simplePDP, requestFolder);
//		while (thrdcount < 400)
//			Thread.sleep(100);
//		endTime = System.nanoTime(); // end timer
//		duration = endTime - startTime;
//		System.out.println(duration);
		
		// for 500 requests
//		requestFolder = new File(currentDir + "/sample/request-500-1000/");
//		requestFolder = new File(currentDir + "/sample/request-500-2000/");
//		requestFolder = new File(currentDir + "/sample/request-500-3000/");
//		requestFolder = new File(currentDir + "/sample/request-500-4000/");
//		requestFolder = new File(currentDir + "/sample/request-500-5000/");
//		requestFolder = new File(currentDir + "/sample/request-500-6000/");
		
//		requestFolder = new File(currentDir + "/sample/request-500-1000-width/");
//		requestFolder = new File(currentDir + "/sample/request-500-2000-width/");
//		requestFolder = new File(currentDir + "/sample/request-500-3000-width/");
//		requestFolder = new File(currentDir + "/sample/request-500-4000-width/");
//		requestFolder = new File(currentDir + "/sample/request-500-5000-width/");
//		requestFolder = new File(currentDir + "/sample/request-500-6000-width/");

		requestFolder = new File(currentDir + "/sample/request-500-1000-depth/");
		requestFolder = new File(currentDir + "/sample/request-500-2000-depth/");
		requestFolder = new File(currentDir + "/sample/request-500-3000-depth/");
		requestFolder = new File(currentDir + "/sample/request-500-4000-depth/");
		requestFolder = new File(currentDir + "/sample/request-500-5000-depth/");
		requestFolder = new File(currentDir + "/sample/request-500-6000-depth/");

		System.out.println(requestFolder.list().length);

		// Parallel evaluation
		startTime = System.nanoTime(); // start timer
		evaluateMultipleRequests(simplePDP, requestFolder);
		while (thrdcount < 500)
			Thread.sleep(1000);
		endTime = System.nanoTime(); // end timer
		duration = endTime - startTime;
		System.out.println(duration);

		// Sequential evaluation
//		startTime = System.nanoTime(); // start timer
//		evaluateMultipleRequestsSequentially(simplePDP, requestFolder);
//		endTime = System.nanoTime(); // end timer
//		duration = endTime - startTime;
//		System.out.println("dura: "+duration);
		
		// Evaluate requests without parallelization
		
/*		// for 50 requests
		requestFolder = new File(currentDir + "/sample/request-50/");
		
		startTime = System.nanoTime(); // start timer
		evaluateMultipleRequestsSequentially(simplePDP, requestFolder);
		endTime = System.nanoTime(); // end timer
		duration = endTime - startTime;
		System.out.println(duration);
		
		// for 100 requests
		requestFolder = new File(currentDir + "/sample/request-100/");
		
		startTime = System.nanoTime(); // start timer
		evaluateMultipleRequestsSequentially(simplePDP, requestFolder);
		endTime = System.nanoTime(); // end timer
		duration = endTime - startTime;
		System.out.println(duration);
		
		// for 200 requests
		requestFolder = new File(currentDir + "/sample/request-200/");
		
		startTime = System.nanoTime(); // start timer
		evaluateMultipleRequestsSequentially(simplePDP, requestFolder);
		endTime = System.nanoTime(); // end timer
		duration = endTime - startTime;
		System.out.println(duration);
		
		// for 400 requests
		requestFolder = new File(currentDir + "/sample/request-400/");
		
		startTime = System.nanoTime(); // start timer
		evaluateMultipleRequestsSequentially(simplePDP, requestFolder);
		endTime = System.nanoTime(); // end timer
		duration = endTime - startTime;
		System.out.println(duration);
		
		// for 800 requests
		requestFolder = new File(currentDir + "/sample/request-800/");
		
		startTime = System.nanoTime(); // start timer
		evaluateMultipleRequestsSequentially(simplePDP, requestFolder);
		endTime = System.nanoTime(); // end timer
		duration = endTime - startTime;
		System.out.println(duration);*/
	}
	
	private static void evaluateMultipleRequestsSequentially(ProvPDP simplePDP, File folderName){
		File[] listOfFiles = folderName.listFiles();

		for (File file : listOfFiles) {
			if (file.isFile()) {
				try {
					ResponseCtx response = simplePDP.evaluate(file.getAbsolutePath());
//					response.encode(System.out, new Indenter());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParsingException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void evaluateMultipleRequests(ProvPDP simplePDP, File folderName){
		File[] listOfFiles = folderName.listFiles();

		for (File file : listOfFiles) {
			if (file.isFile()) {
//				System.out.println(file.getAbsolutePath());
				RequestHandleThread reqThread = new RequestHandleThread(simplePDP, file.getAbsolutePath());
				reqThread.start();
			}
		}
		

	}

	private static class RequestHandleThread extends Thread {
		private ProvPDP simplePDP;
		private String requestFile;

		public RequestHandleThread(ProvPDP simplePDP, String requestFile){
			this.simplePDP = simplePDP;
			this.requestFile = requestFile;
		}

		public void run(){
			ResponseCtx response;
			try {
				response = simplePDP.evaluate(requestFile);
//				response.encode(System.out, new Indenter());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParsingException e) {
				e.printStackTrace();
			}
			long threadId = Thread.currentThread().getId();
			System.out.println(threadId + " is done.");
			System.out.println(++thrdcount);
		}
	}

	private static void evaluateOneRequest(ProvPDP simplePDP, String requestFile) throws Exception{
		// evaluate the request
		for (int i = 0; i <= 10; i++){
			long startTime = System.nanoTime(); // start timer
			ResponseCtx response = simplePDP.evaluate(requestFile);
			long endTime = System.nanoTime(); // end timer

			long duration = endTime - startTime;
			//			System.out.println("Evaluation Time Run #" + i + ": " + duration);
			System.out.println(duration);
		}
		// for this sample program, we'll just print out the response
		ResponseCtx response = simplePDP.evaluate(requestFile);
		response.encode(System.out, new Indenter());
	}

	private static void initializeModel() {
		hwgsLargeModel = DataGenerator.getLargeModelInstance(10000);		
	}

	public static Model getLargeModel(){
		if (hwgsLargeModel == null)
			initializeModel();
		return hwgsLargeModel;
	}

}
