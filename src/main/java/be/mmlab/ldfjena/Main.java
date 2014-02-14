package be.mmlab.ldfjena;

import be.mmlab.ldfjena.model.LinkedDataFragmentGraph;
import be.mmlab.ldfjena.utils.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Created by ldevocht on 4/29/14.
 *
 * For server use - it is recommended to use Fuseki.
 *
 */

public class Main {
    public static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static void main(String[] args){

        if (args.length < 1 || args.length > 2 || args[0].matches("/^--?h(elp)?$/")) {
            System.out.println("usage: java -jar ldf-client.jar [config.json] query");
            System.exit(1);
        }

        Boolean hasConfig = args.length >= 2;
        File configFile = hasConfig? new File(args[0]) : new File("./config-default.json");
        String queryFilePath = hasConfig? args[1] : args[0];
        Gson gson = new GsonBuilder().create();
        Config config = new Config();
        String queryString = "";
        QueryExecution qe = null;
        Model model = ModelFactory.createDefaultModel();

        //MODEL
        try {
            JsonReader jsonReader = new JsonReader(new FileReader(configFile));
            config = gson.fromJson(jsonReader, Config.class);
            LinkedDataFragmentGraph ldfg = new LinkedDataFragmentGraph(config.datasource);
            model = ModelFactory.createModelForGraph(ldfg);
        } catch (FileNotFoundException e) {
            System.out.println("Config file could not be found!");
            System.exit(1);
        }

        //QUERY
        try {

            queryString = readFile(queryFilePath, StandardCharsets.UTF_8);
            Query qry = QueryFactory.create(queryString);
            for(String prefix : config.prefixes.keySet()) {
                qry.setPrefix(prefix,config.prefixes.get(prefix));
                System.out.println(String.format("setting prefix %s",prefix));
            }
            qe = QueryExecutionFactory.create(qry, model);
        } catch (IOException e) {
            System.out.println("Query file could not be found!");
            System.exit(1);
        }


        //EXECUTE
        if (qe!= null) {

            if(qe.getQuery().getQueryType() == Query.QueryTypeSelect) {
               ResultSet rs = qe.execSelect();
               ResultSetFormatter.outputAsJSON(System.out, rs);
            }
            if(qe.getQuery().getQueryType() == Query.QueryTypeAsk) {
                System.out.println(qe.execAsk());
            }
            if(qe.getQuery().getQueryType() == Query.QueryTypeConstruct) {
                Iterator<Triple> triples = qe.execConstructTriples();
                while(triples.hasNext()) {
                    System.out.println(triples.next().asTriple());
                }
            }
            if (qe.getQuery().getQueryType() == Query.QueryTypeDescribe) {
                Iterator<Triple> triples = qe.execDescribeTriples();
                while(triples.hasNext()) {
                    System.out.println(triples.next().asTriple());
                }
            }
            if (qe.getQuery().getQueryType() == Query.QueryTypeUnknown) {
                System.out.println("Unknown query type");
            }
        } else {
            System.out.println("No query executor found!");
            System.exit(1);
        }
        System.exit(0);
    }
}
