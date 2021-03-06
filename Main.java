package jsontordf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class Main {

	public static Repository repository;
	public static RepositoryConnection connection;
	
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	
	public static void main(String[] args) throws Exception {
		
		//server Graphdb connection
		
		String serverURL= "http://localhost:7200";
		String repositoryId ="smart_home";
		
		
		RemoteRepositoryManager manager = new RemoteRepositoryManager(serverURL);

        
        manager.init();

        try {
            // Get Repository
            Main.repository = manager.getRepository(repositoryId);

            // Initialize Repository
            Main.repository.init();

            // Separate connection to a repository
            Main.connection = Main.repository.getConnection();

        } catch (RepositoryConfigException | RepositoryException e) {
            LOGGER.log(Level.WARNING, "Connecting to remote repository failed!");
        }
		
        loadOntologyFile("sh_new.ttl");
		jsontordf();
	}
	
	
	 
    public static void jsontordf() throws IOException  {

        Gson gson = new Gson();
        File file = new File("example_observations.json");
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();

        String str = new String(data, "UTF-8");
       
        JsonElement json = gson.fromJson(str, JsonElement.class);
        JsonObject jobject = json.getAsJsonObject();
      
        ValueFactory f = SimpleValueFactory.getInstance();
        ModelBuilder builder = new ModelBuilder();
      
        String smho = "http://www.semanticweb.org/user/ontologies/2020/6/untitled-ontology-3#"; 
     
       
            	
                builder.setNamespace("smho", smho );
                
               int counter=0;
                
               //αρχικοποίηση prevActivity σε null για να μην έχει το πρώτο activity previous
               IRI prevActivity = null;
               
               for(int i=0; i<jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().size(); i++) {
            	   String Activity_name = jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("content").getAsString();
            	   IRI activity_ = f.createIRI(smho + Activity_name);  
                   IRI activity = f.createIRI(smho + "Activity");
                   
                   builder.subject(activity_).add(RDF.TYPE, activity);
                   
                   //builder.add(activity_, RDF.TYPE, activity);
                   
                   if(prevActivity != null) {
                	   //builder.add(activity_.toString(), "smho:isNextOf", prevActivity);
                	   //builder.add(prevActivity.toString(), "smho:hasNext", activity_);
                	   builder.subject(prevActivity).add("smho:hasNext", activity_);
                   }
                   
            	   //builder.add(activity_.toString(), "smho:hasStartTime", jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("start").getAsString());
                   builder.subject(activity_).add("smho:hasStartTime", jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("start").getAsString());
	               
                   
                   //builder.add(activity_.toString(), "smho:hasEndTime", jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("end").getAsString());
                   builder.add("smho:hasEndTime", jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("end").getAsString());
                   
	               //builder.add(activity_.toString(), "smho:hasContent", jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("content").getAsString());
                   builder.add("smho:hasContent", jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("content").getAsString());
	              
                   //ορισμός prevActivity σε τρέχων activity για να γίνει το Previous του επόμενου activity
	               prevActivity = activity_;
	               
	               for (int j=0; j<jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("observations").getAsJsonArray().size(); j++) {
	            	   counter++;
	            	   String Observation_name = jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("observations").getAsJsonArray().get(j).getAsJsonObject().get("content").getAsString();
	            	   IRI observation_ = f.createIRI(smho + "Observation_" + counter);
	                   IRI observation = f.createIRI(smho + "Observation");
	                   IRI observation_type = f.createIRI(smho + Observation_name);
	                   
	                   builder.subject(activity_).add("smho:hasObservations", observation_);
	                   //builder.add(activity_.toString(), "smho:hasObservations", observation_);
	                   
	                   builder.subject(observation_).add(RDF.TYPE, observation).add(RDF.TYPE, observation_type);
	                   /*builder.add(observation_, RDF.TYPE, observation);
	                   builder.add(observation_, RDF.TYPE, observation_type);
	                   */
	                   
	                   builder.subject(observation_).add("smho:hasStartTime", jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("observations").getAsJsonArray().get(j).getAsJsonObject().get("start").getAsString());
	                   
	                   //builder.add(observation_.toString(), "smho:hasStartTime", jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("observations").getAsJsonArray().get(j).getAsJsonObject().get("start").getAsString());
	                   
	                   builder.add("smho:hasEndTime", jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("observations").getAsJsonArray().get(j).getAsJsonObject().get("end").getAsString());
		                  
	                   //builder.add(observation_.toString(), "smho:hasEndTime", jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("observations").getAsJsonArray().get(j).getAsJsonObject().get("end").getAsString());
	                  
	                   builder.add("smho:hasContent", jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("observations").getAsJsonArray().get(j).getAsJsonObject().get("content").getAsString());
	                   
	                   //builder.add(observation_.toString(), "smho:hasContent", jobject.get("model").getAsJsonObject().get("activities").getAsJsonArray().get(i).getAsJsonObject().get("observations").getAsJsonArray().get(j).getAsJsonObject().get("content").getAsString());
	                   
	               
	               }
               
               }
               
    
     // When adding data we need to start a transaction
        Main.connection.begin();

        // We're done building, create our Model
        Model m = builder.build();

        Main.connection.add(m);

        Main.connection.commit();
        m.setNamespace(RDF.NS);
		m.setNamespace(RDFS.NS); 
	m.setNamespace("smho", smho);
	FileOutputStream out = new FileOutputStream("C:\\Users\\User\\Desktop\\file.rdf");
	try {
		  Rio.write(m, out, RDFFormat.RDFXML);
		}
			finally {
		  out.close();
		}
		Rio.write(m, System.out, RDFFormat.TURTLE);
        
        System.out.println(m);

               
    }
    
    public static void  loadOntologyFile(String fileName) {

    	System.out.println("Loading ontology file...");

    	try {
	    	// Main.connection.clear();
	
	    	Main.connection.begin();
	
	    	Main.connection.add(Main.class.getResourceAsStream("/" + fileName), "urn:base", RDFFormat.TURTLE);
	
	    	// Committing the transaction persists the data
	    	Main.connection.commit();
    	} catch (RDFParseException | RepositoryException | IOException e) {
	    	LOGGER.log(Level.WARNING, "Loading ontology file failed!");
	    	}
    	}
    
    
    
}