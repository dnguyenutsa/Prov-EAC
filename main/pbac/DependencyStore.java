/**
 * 
 */
package pbac;

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
		depList = new HashMap<String,String>();
		return depList;
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
