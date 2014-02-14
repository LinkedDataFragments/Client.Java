package be.mmlab.ldfjena;

import be.mmlab.ldfjena.model.LinkedDataFragmentGraph;
import be.mmlab.ldfjena.util.LDFTestUtils;
import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class LinkedDataFragmentsClientTest {
    protected Model model;

    @Before
    public void setUp() {
        LinkedDataFragmentGraph ldfg = new LinkedDataFragmentGraph("http://data.linkeddatafragments.org/dbpedia");
        model = ModelFactory.createModelForGraph(ldfg);
    }

    @Test
    public void testSize() {
        assertThat(model.size()).isEqualTo(427670470);
        System.out.println(model.size());
    }

    @Test
    public void testBasicSparql() {
        String queryString = "SELECT ?p ?o WHERE { <http://dbpedia.org/resource/Barack_Obama> ?p ?o }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }

        assertThat(rs.getRowNumber()).isGreaterThan(0);
    }

    @Test
    public void testTypeSparql() {
        String queryString = "SELECT ?o WHERE { <http://dbpedia.org/resource/Barack_Obama> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }

        assertThat(rs.getRowNumber()).isGreaterThan(0);
    }

    @Test
    public void testLiteralSparql() {

        String queryString = "SELECT (COUNT(?o) AS ?count) WHERE { ?o <http://www.w3.org/2000/01/rdf-schema#label> \"Belgium\"@en }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }

        assertThat(rs.getRowNumber()).isGreaterThan(0);

    }

    @Test
    public void testSingleJoinSparql() {

        String queryString = "SELECT ?o ?n WHERE { <http://dbpedia.org/resource/Barack_Obama> <http://dbpedia.org/ontology/almaMater> ?o " +
                ". ?o <http://dbpedia.org/ontology/state> ?n }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();

        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }

        assertThat(rs.getRowNumber()).isGreaterThan(0);

    }

    @Test
    public void testSingleCountPredicateSparql() {

        String queryString = "SELECT (COUNT(DISTINCT ?p) AS ?count) where { ?o ?p <http://dbpedia.org/resource/Barack_Obama> }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();

        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }

        assertThat(rs.getRowNumber()).isGreaterThan(0);

    }

    @Test
    public void testSingleCountSubjectSparql() {

        String queryString = "SELECT (COUNT(DISTINCT ?o) AS ?count) where { ?o ?p <http://dbpedia.org/resource/Barack_Obama> }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();

        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }

        assertThat(rs.getRowNumber()).isGreaterThan(0);

    }

    @Test
    public void testSingleCountObjectSparql() {

        String queryString = "SELECT (COUNT(DISTINCT ?o) AS ?count) where { <http://dbpedia.org/resource/Barack_Obama> ?p ?o }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();

        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }

        assertThat(rs.getRowNumber()).isGreaterThan(0);

    }



    @Test
    public void testSparqlQueries() {

        List<String> queries = LDFTestUtils.readFiles(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        String prefixes = "" +
                "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> " +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "PREFIX dbpedia: <http://dbpedia.org/resource/> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
        for(String query : queries) {

            String queryString = prefixes + query;
            System.out.println(queryString);
            Query qry = QueryFactory.create(queryString);
            Stopwatch sw = Stopwatch.createStarted();
            QueryExecution qe = QueryExecutionFactory.create(qry, model);
            if(qe.getQuery().getQueryType() == Query.QueryTypeConstruct) {
                Iterator<Triple> rm = qe.execConstructTriples();

                assertThat(rm.hasNext()).isTrue();

                while(rm.hasNext()) {
                    System.out.println(rm.next().asTriple().toString());
                }

            } else if(qe.getQuery().getQueryType() == Query.QueryTypeSelect) {
                ResultSet rs = qe.execSelect();

                while (rs.hasNext()) {
                    System.out.println(rs.nextSolution().toString());
                }

                assertThat(rs.getRowNumber()).isGreaterThan(0);
            }
            System.out.println(sw.stop());
        }


    }
}
