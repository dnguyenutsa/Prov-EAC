package startup;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

public class DpediaStart {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String service = "http://dbpedia.org/sparql";
		String query = "ASK { }";
		QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
		try {
			if (qe.execAsk()) {
				System.out.println(service + " is up");
			}
		} catch (QueryExceptionHTTP e) {
			System.out.println(service + "is down");
		} finally {
			qe.close();
		}

	}

}
