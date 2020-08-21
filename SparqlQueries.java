package jsontordf;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;


public class SparqlQueries  {

	
	public static void main(String[] args) throws Exception {
		String queryString = "PREFIX smho: <http://www.semanticweb.org/user/ontologies/2020/6/untitled-ontology-3#> \n" + 
    			"SELECT ?Start ?End ?Content WHERE {" +
                "?o rdf:type smho:Activity  .\r\n" + 
                "	?o smho:hasStartTime ?Start .\r\n" + 
                "    ?o smho:hasEndTime ?End .\r\n" + 
                "    ?o smho:hasContent ?Content  ." +
                "}";
    	 String queryString2 = "PREFIX smho: <http://www.semanticweb.org/user/ontologies/2020/6/untitled-ontology-3#>\r\n" + 
    	 		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
    	 		"select DISTINCT ?Content \r\n" + 
    	 		"where { \r\n" + 
    	 		"    ?o rdf:type smho:Observation  .\r\n" + 
    	 		"    ?o smho:hasContent ?Content  .\r\n" + 
    	 		"}  ";
    	 
    	 String queryString3 = "PREFIX smho: <http://www.semanticweb.org/user/ontologies/2020/6/untitled-ontology-3#>\r\n" + 
    	 		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
    	 		"select ?Content \r\n" + 
    	 		"where { \r\n" + 
    	 		"    ?o rdf:type smho:Observation  .\r\n" + 
    	 		"    ?o smho:hasStartTime ?Start . " + 
    	 		"    ?o smho:hasEndTime ?End . " + 
    	 		"    ?o smho:hasContent ?Content  .\r\n" + 
    	 		"FILTER ( ?Start >= \"2014-05-05T18:34:54.000\" && ?End <= \"2014-05-05T18:55:40.000\")\r\n" +
    	 		"}  ";
        
		
    	System.out.println("1. Επιστροφή όλων των activities που υπάρχουν: \n"); 
    	sparqlQueries(queryString);
    	System.out.println();
    	System.out.println();
    	System.out.println("2. Επιστροφή όλων των observation types που υπάρχουν: \n"); 
		sparqlQueries(queryString2);
		System.out.println();
		System.out.println();
		System.out.println("3. Επιστροφή όλων των observations που έχουν αναγνωριστεί το διάστημα από \"2014-05-05T18:34:54.000\" έως \"2014-05-05T18:55:40.000\":  \n"); 
		sparqlQueries(queryString3);
	}
	
	
    
    
    public static TupleQueryResult evaluateSelectQuery(RepositoryConnection connection, String query,
		            Binding... bindings)
		throws MalformedQueryException, RepositoryException, QueryEvaluationException {
		// Preparing a new query
		TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
		
		// Setting any potential bindings (query parameters)
		for (Binding b : bindings) {
		tupleQuery.setBinding(b.getName(), b.getValue());
		}
		
		// Sending the query to GraphDB, evaluating it and returning the result
		return tupleQuery.evaluate();
		}
    
    public static void sparqlQueries(String query) throws Exception {
    	
    	// RepositoryConnection connection = EmbeddedGraphDB.openConnectionToTemporaryRepository("smart_home");
    	
    	// Connect to a remote repository
    	// Abstract representation of a remote repository accessible over HTTP
    	HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/smart_home");

        // Separate connection to a repository
        RepositoryConnection connection = repository.getConnection();

    	
    	 

        try {
            // Preparing a SELECT query for later evaluation
            TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL,query);
//                    "PREFIX smho: <http://www.semanticweb.org/user/ontologies/2020/6/untitled-ontology-3#> \n" +
//                    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
//                    		"SELECT ?Start ?End ?Content WHERE {" +
//                            "?o rdf:type sh:Activity  .\r\n" + 
//                            "	?o sh:hasStartTime ?Start .\r\n" + 
//                            "    ?o sh:hasEndTime ?End .\r\n" + 
//                            "    ?o sh:hasContent ?Content  ." +
//                            "}");

            // Evaluating a prepared query returns an iterator-like object
            // that can be traversed with the methods hasNext() and next()
            TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
            while (tupleQueryResult.hasNext()) {
                // Each result is represented by a BindingSet, which corresponds to a result row
                BindingSet bindingSet = tupleQueryResult.next();

                // Each BindingSet contains one or more Bindings
                for (Binding binding : bindingSet) {
                    // Each Binding contains the variable name and the value for this result row
                    String name = binding.getName();
                    Value value = binding.getValue();

                    System.out.println(name + " = " + value);
                }

                // Bindings can also be accessed explicitly by variable name
                //Binding binding = bindingSet.getBinding("x");
            }

            // Once we are done with a particular result we need to close it
            tupleQueryResult.close();

            // Doing more with the same connection object
            // ...
        } finally {
            // It is best to close the connection in a finally block
            connection.close();
        }
    }
}