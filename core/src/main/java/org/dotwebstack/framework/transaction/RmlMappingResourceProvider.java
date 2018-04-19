package org.dotwebstack.framework.transaction;

import com.taxonic.carml.vocab.Rdf.Carml;
import com.taxonic.carml.vocab.Rml;
import java.util.List;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.dotwebstack.framework.vocabulary.R2RML;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RmlMappingResourceProvider
    extends AbstractResourceProvider<RmlMapping> {

  private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private static final String STREAMNAME = "streamName";

  @Autowired
  public RmlMappingResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s a ?type } WHERE { ?s a ?type }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", R2RML.TRIPLES_MAP);

    return graphQuery;
  }

  @Override
  protected RmlMapping createResource(Model model, Resource identifier) {
    return new RmlMapping.Builder(identifier, getModel(identifier),
        getStreamName(identifier)).build();
  }

  public RmlMapping get(String identifier) {
    IRI resource = valueFactory.createIRI(identifier);
    return get(resource);
  }

  private Model getModel(Resource identifier) {
    final RepositoryConnection repositoryConnection;
    final Model model;

    try {
      repositoryConnection = configurationBackend.getRepository().getConnection();
    } catch (RepositoryException e) {
      throw new ConfigurationException("Error while getting repository connection.", e);
    }

    String query = "CONSTRUCT { ?rmlmapping ?p ?o . ?o ?op ?oo . ?oo ?oop ?ooo } "
        + "WHERE { ?rmlmapping ?p ?o . ?rmlmapping a ?type "
        + "OPTIONAL { ?o ?op ?oo OPTIONAL { ?oo ?oop ?ooo } } } ";
    GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(query);
    graphQuery.setBinding("rmlmapping", identifier);
    graphQuery.setBinding("type", R2RML.TRIPLES_MAP);

    SimpleDataset simpleDataset = new SimpleDataset();
    simpleDataset.addDefaultGraph(applicationProperties.getSystemGraph());
    simpleDataset.addDefaultGraph(ELMO.CONFIG_GRAPHNAME);
    graphQuery.setDataset(simpleDataset);

    try {
      model = QueryResults.asModel(graphQuery.evaluate());
    } catch (QueryEvaluationException e) {
      throw new ConfigurationException("Error while evaluating SPARQL query.", e);
    } finally {
      repositoryConnection.close();
    }

    return model;
  }

  private String getStreamName(Resource identifier) {
    final RepositoryConnection repositoryConnection;
    final String streamName;

    try {
      repositoryConnection = configurationBackend.getRepository().getConnection();
    } catch (RepositoryException e) {
      throw new ConfigurationException("Error while getting repository connection.", e);
    }

    String query = String.format("SELECT ?streamName "
        + "WHERE { ?rmlmapping ?p ?o . ?rmlmapping a ?type "
        + "OPTIONAL { ?o <%s> ?oo OPTIONAL { ?oo <%s> ?streamName } } } ",
        Rml.source, Carml.streamName);
    TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(query);
    tupleQuery.setBinding("rmlmapping", identifier);
    tupleQuery.setBinding("type", R2RML.TRIPLES_MAP);

    SimpleDataset simpleDataset = new SimpleDataset();
    simpleDataset.addDefaultGraph(applicationProperties.getSystemGraph());
    simpleDataset.addDefaultGraph(ELMO.CONFIG_GRAPHNAME);
    tupleQuery.setDataset(simpleDataset);

    List<BindingSet> queryResults;
    try {
      queryResults = QueryResults.asList(tupleQuery.evaluate());
    } catch (QueryEvaluationException e) {
      throw new ConfigurationException("Error while evaluating SPARQL query.", e);
    } finally {
      repositoryConnection.close();
    }

    if (queryResults.isEmpty() || queryResults.get(0).getValue(STREAMNAME) == null) {
      throw new ConfigurationException(
          String.format("%s not set for RmlMapping %s", STREAMNAME, identifier));
    }

    streamName = queryResults.get(0).getValue(STREAMNAME).stringValue();

    return streamName;
  }

}