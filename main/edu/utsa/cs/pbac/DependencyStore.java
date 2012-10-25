/**
 * 
 */
package edu.utsa.cs.pbac;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dangnguyen
 * This class maintains the list of application-specific predefined dependency names 
 * and the corresponding regular expression based path patterns.
 * The class also provides an interface to an underlying (MySQL) database for storing the list
 * constructs.
 */
public class DependencyStore {

	private static Map<String, String> depList;
	
	public static Map<String, String> createNewDepList(){
		if (depList == null){
			depList = new HashMap<String,String>();
			populateSampleDList();
		}
		return depList;
	}
	
	private static void populateSampleDList() {
		String wasAuthoredBy = "((hw:wasGeneratedBySubmit/hw:usedInput)?" +
				"/(hw:wasGeneratedByReplace/hw:usedInput)*" +
				"/hw:wasGeneratedByUpload/hw:wasControlledBy)";
		String wasReviewedBy = "^(hw:wasGeneratedByReview/hw:usedInput)" +
				"/(hw:wasGeneratedByReview/hw:wasControlledBy)";
		
		depList.put("wasReplacedVof", "(hw:wasGeneratedByReplace/hw:usedInput)");
		depList.put("wasSubmittedVof", "(hw:wasGeneratedBySubmit/hw:usedInput)");
		depList.put("wasReviewedOof", "(hw:wasGeneratedByReview/hw:usedInput)");
		depList.put("wasReviewedOby", "(hw:wasGeneratedByReview/hw:wasControlledBy)");
		depList.put("wasGradedOof", "(hw:wasGeneratedByGrade/hw:usedInput)");
		depList.put("wasAuthoredBy", wasAuthoredBy);
		depList.put("wasReviewedBy", wasReviewedBy);
	}
	
	public static void loadDepListFromDatabase(){
		// to be implemented
	}
	
	public static void addDListEntry(String depName, String pathPattern){
		depList.put(depName, pathPattern);
	}
	
	public static String getDepPathPattern(String depName){
		return depList.get(depName);
	}
	
	public static void removeDListEntry(String depName){
		depList.remove(depName);
	}
}
