package org.linkeddatafragments.client;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import org.linkeddatafragments.model.LinkedDataFragmentGraph;
import org.linkeddatafragments.utils.Config;

/**
 * A QueryResultsWriter executes a query on a graph and writes its results to a stream.
 * @author Laurens De Vocht
 * @author Ruben Verborgh
 */
public class QueryResultsWriter {
    private final Model model;
    private final Query query;

    /**
     * Creates a new QueryResultsWriter.
     * @param model The model to query.
     * @param query The query to execute.
     */
    public QueryResultsWriter(final Model model, final Query query) {
        this.model = model;
        this.query = query;
    }

    /**
     * Write the results of the query to the stream.
     * @param outputStream The output stream.
     */
    public void writeResults(final PrintStream outputStream) {
        final QueryExecution executor = QueryExecutionFactory.create(query, model);
        switch (query.getQueryType()) {
        case Query.QueryTypeSelect:
            final ResultSet rs = executor.execSelect();
            while (rs.hasNext())
                outputStream.println(rs.next());
            break;
        case Query.QueryTypeAsk:
            outputStream.println(executor.execAsk());
            break;
        case Query.QueryTypeConstruct:
        case Query.QueryTypeDescribe:
            final Iterator<Triple> triples = executor.execConstructTriples();
            while (triples.hasNext())
                outputStream.println(triples.next().asTriple());
            break;
        default:
            throw new Error("Unsupported query type");
        }
    }

    /**
     * Starts a standalone version of the results writer.
     * @param args The command-line arguments.
     */
    public static void main(final String[] args) {
        // Verify and parse arguments
        if (args.length < 1 || args.length > 2 || args[0].matches("/^--?h(elp)?$/")) {
            System.err.println("usage: java -jar ldf-client.jar [config.json] query");
            System.exit(1);
            return;
        }
        final boolean hasConfig = args.length >= 2;

        // Read the configuration
        final File configFile = hasConfig ? new File(args[0]) : new File("./config-default.json");
        JsonReader configReader;
        try {
            configReader = new JsonReader(new FileReader(configFile));
        } catch (FileNotFoundException e) {
            System.out.println("Config file could not be found.");
            System.exit(1);
            return;
        }
        final Config config = new Gson().fromJson(configReader, Config.class);

        // Read the query
        final String queryFilePath = hasConfig ? args[1] : args[0];
        String queryText;
        try {
            queryText = new String(Files.readAllBytes(Paths.get(queryFilePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Query file could not be read.");
            System.exit(1);
            return;
        }
        final Query query = QueryFactory.create(queryText);
        for (final String prefix : config.prefixes.keySet())
            query.setPrefix(prefix, config.prefixes.get(prefix));

        // Execute the query over the graph and write its results
        final LinkedDataFragmentGraph graph = new LinkedDataFragmentGraph(config.datasource);
        new QueryResultsWriter(ModelFactory.createModelForGraph(graph), query).writeResults(System.out);
        System.exit(0);
    }
}
