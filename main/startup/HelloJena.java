package startup;

import java.sql.DriverManager;

import java.sql.SQLException;
import java.util.Properties;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model; 
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.DatasetStore;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.vocabulary.DC;
import com.mysql.jdbc.Connection;

public class HelloJena {
	static Store store;
	public static void main(String args[]){
		Model m = ModelFactory.createDefaultModel();

		//		less flexible way to create a store description
		StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash,DatabaseType.MySQL);
		JDBC.loadDriverMySQL();
		String jdbcURL = "jdbc:mysql://10.245.122.49/SDBExample";
		//				+ "user=sdbuser&password=sdbuserpw";
		SDBConnection conn = new SDBConnection(jdbcURL, "sdbuser", "sdbuserpw");
		
		store = SDBFactory.connectStore(conn, storeDesc);

//		try {
//			System.out.println(StoreUtils.isFormatted(store));
			
//		} catch (SQLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		store.getTableFormatter().create();
		
		Model m2 = executeExample2();
		
		
		
        System.out.println("Subjects: ") ;
        query("SELECT DISTINCT ?s { ?s ?p ?o }", storeDesc, conn) ;
        System.out.println("Predicates: ") ;
        query("SELECT DISTINCT ?p { ?s ?p ?o }", storeDesc, conn) ;
        System.out.println("Objects: ") ;
        query("SELECT DISTINCT ?o { ?s ?p ?o }", storeDesc, conn) ;
		
		uploadRDFasDOMtoMYSQL(m2);
		
		
//		String prolog = "PREFIX dc:<" + DC.getURI() + ">";
//		String queryString= prolog + "\n" 
//				+ "SELECT ?title WHERE { ?x dc:description ?y . ?y dc:description?z . ?z dc:title?title }";
//		Query query= QueryFactory.create( queryString);
//		QueryExecution qexec= QueryExecutionFactory.create( query, m2 );
//		ResultSet rs= qexec.execSelect();
//		//			System.out.println(rs.getResultVars());
//		//			System.out.println(rs.hasNext());
//		ResultSetFormatter.out(System.out, (com.hp.hpl.jena.query.ResultSet) rs, query);
//
//
//		System.out.println("\nGLEEN\n");
//
//		/* Testing GLEEN */
//		prolog += "\nPREFIX gleen:<java:edu.washington.sig.gleen.>";
//		queryString = prolog + "\n"
//				+ "SELECT ?title WHERE {  <http://example.org/book#1> gleen:OnPath(\"[dc:description]*/[dc:title]\" ?title ) .}";
//		query= QueryFactory.create(queryString);
//		qexec= QueryExecutionFactory.create( query, m2 );
//		rs= qexec.execSelect();
//
//
//		ResultSetFormatter.out(System.out, (com.hp.hpl.jena.query.ResultSet) rs, query);
//		try {
			//				ResultSet res= qexec.execSelect();
			//System.out.println(rs.getResultVars().toString());
			//				for ( ; rs.hasNext() ; ) {
			//					QuerySolution rb= rs.nextSolution() ;
			//System.out.println(rb);
			//					RDFNode x = rb.get( "title" ) ;
			//					System.out.println(x);
			//					if ( x.isLiteral() ) {
			//						Literal titleStr= (Literal )x ;
			//						System.out.println( " " + titleStr); 
			//
			//					}
			//				}
//		}
//		finally { qexec.close(); }


		//		Store store = SDBFactory.connectStore("sdb.ttl");

	}

	private static void uploadRDFasDOMtoMYSQL(Model m2) {
//		m2.write(System.out, "RDF/XML-ABBREV");
		
		
	}

	private static Model executeExample2() {
		//		Model m = ModelFactory.createDefaultModel();
		//		
		//		Resource r1 = m.createResource("http://example.org/book#1");
		//		
		//		r1.addProperty(DC.title, "SP-the book")
		//			.addProperty(DC.description, "A book")
		//			.addProperty(DC.description, "Advanced tech");

		//		m.write(System.out, "RDF/XML-ABBREV");

//		Model m2 = ModelFactory.createDefaultModel();
		Model m2 = SDBFactory.connectDefaultModel(store);



		Resource r4 = m2.createResource("http://example.org/book#1");
		Resource r2 = m2.createResource( "http://example.org/book#2" ) ;
		Resource r3 = m2.createResource( "http://example.org/book#3" ) ;

		//		r4.addProperty(DC.description, r2);
		//		m2.write(System.out, "RDF/XML-ABBREV");

		String nsResource = "http://example.org/book#";
		m2.setNsPrefix("nsResource", nsResource);


		r4.addProperty(DC.title, "SPARQL-the book" ).addProperty(DC.description, "A book about SPARQL" ).addProperty(DC.description, r2) ;
		r2.addProperty(DC.title, "Advanced techniques for SPARQL" ).addProperty(DC.description, r3).addProperty(DC.description, r4) ;
		r3.addProperty(DC.title, "Jena -an RDF framework for Java" ).addProperty(DC.description, "A book about Jena").addProperty(DC.description, r4) ;
//		m2.write(System.out, "RDF/XML-ABBREV");
		
		


		return m2;

	}
	
	public static void query(String queryString, StoreDesc storeDesc, SDBConnection conn)
    {
        Query query = QueryFactory.create(queryString) ;

        
        Store store = StoreFactory.create(storeDesc, conn) ;
        
        Dataset ds = DatasetStore.create(store) ;
        QueryExecution qe = QueryExecutionFactory.create(query, ds) ;
        try {
            ResultSet rs = qe.execSelect() ;
            ResultSetFormatter.out(rs) ;
        } finally { qe.close() ; }
        // Does not close the JDBC connection.
        // Do not call : store.getConnection().close() , which does close the underlying connection.
        store.close() ;
    }

}

//	public Connection getConnection() throws SQLException {
//
//	    Connection conn = null;
//	    Properties connectionProps = new Properties();
//	    connectionProps.put("user", this.userName);
//	    connectionProps.put("password", this.password);
//
//	    if (this.dbms.equals("mysql")) {
//	        conn = DriverManager.getConnection(
//	                   "jdbc:" + this.dbms + "://" +
//	                   this.serverName +
//	                   ":" + this.portNumber + "/",
//	                   connectionProps);
//	    } else if (this.dbms.equals("derby")) {
//	        conn = DriverManager.getConnection(
//	                   "jdbc:" + this.dbms + ":" +
//	                   this.dbName +
//	                   ";create=true",
//	                   connectionProps);
//	    }
//	    System.out.println("Connected to database");
//	    return conn;
//	}


