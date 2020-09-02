package jsontordf;


import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;


public class Activities  {

	
	public static void main(String[] args) throws Exception {
		
		String queryString = "PREFIX smho: <http://www.semanticweb.org/user/ontologies/2020/6/untitled-ontology-3#>\r\n" + 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
				"DELETE {?NextActivity smho:hasStartTime ?NextActivityStartTime}\r\n" + 
				"INSERT {?NextActivity smho:hasStartTime ?UpdatedNextActivityStartTime}\r\n" + 
				"where { \r\n" + 
				"    ?o rdf:type smho:Activity  .\r\n" + 
				"	?o smho:hasStartTime ?Start .\r\n" + 
				"    ?o smho:hasEndTime ?End .\r\n" + 
				"    ?o smho:hasContent ?Activity  .\r\n" + 
				"    ?o smho:hasNext ?NextActivity  .\r\n" + 
				"    ?NextActivity smho:hasStartTime ?NextActivityStartTime  . \r\n" + 
				"    ?NextActivity smho:hasEndTime ?NextActivityEndTime  .\r\n" + 
				"    BIND(if(?NextActivityStartTime < ?End, ?End, ?NextActivityStartTime) AS ?UpdatedNextActivityStartTime)\r\n" + 
				"    FILTER(?NextActivityStartTime < ?End)\r\n" + 
				"}";
        
		
    	System.out.println("Τροποποίηση των activities που αλληλεκαλύπτονται χρονικά (overlap)... "); 
    	sparqlQueries(queryString);
    	System.out.println("Done!"); 
    	
	}
	
  
    public static void executeUpdate(RepositoryConnection repositoryConnection, String update, Binding... bindings)
            throws MalformedQueryException, RepositoryException, UpdateExecutionException {
        Update preparedUpdate = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, update);
        // Setting any potential bindings (query parameters)
        for (Binding b : bindings) {
            preparedUpdate.setBinding(b.getName(), b.getValue());
        }
        preparedUpdate.execute();
    }
    
    

    
    public static void sparqlQueries(String query) throws Exception {
    	
    	// RepositoryConnection connection = EmbeddedGraphDB.openConnectionToTemporaryRepository("smart_home");
    	
    	// Connect to a remote repository
    	// Abstract representation of a remote repository accessible over HTTP
    	HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/smart_home");

        // Separate connection to a repository
        RepositoryConnection connection = repository.getConnection();

    	
    	 

        try {
        	
        	// When adding data we need to start a transaction
            connection.begin();

            // We interpolate the URIs inside the string as INSERT DATA may not contain variables (bindings)
            executeUpdate(connection,
                    String.format(query));

            // Committing the transaction persists the changes
            connection.commit();
            
            
           
        } finally {
            // It is best to close the connection in a finally block
            connection.close();
        }
    }
}