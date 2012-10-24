package edu.utsa.cs.pbac;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * This is a factory class for generating models of provenance data
 * for the purpose of executing regular path queries.
 *  
 * @author dnguyen
 *
 */
public class ProvenanceModelFactory {
	/**
	 * This function returns an instance of a provenance models from three different
	 * provenance model classes (current envision, potentially extended in future). 
	 * @param factoryType indicates the type of provenance model to be generated and returned.
	 * @return 
	 */
	public static ProvModel getFactoryInstance(String factoryType){
		if (factoryType.equalsIgnoreCase("RDF"))
			return new RDFProvModel();
		else if (factoryType.equalsIgnoreCase("PROV-DM"))
			return null; // future extension based on PROV-DM implementation.
		else if (factoryType.equalsIgnoreCase("OPM"))
			return null; // unlikely possibility since OPM is integrated into PROV-DM
		else
			throw new IllegalArgumentException("Illegal factory type...");
	}
}
