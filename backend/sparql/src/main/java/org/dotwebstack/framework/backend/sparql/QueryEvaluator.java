package org.dotwebstack.framework.backend.sparql;

import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QueryEvaluator {

  private static final Logger LOG = LoggerFactory.getLogger(QueryEvaluator.class);

  public Object evaluate(@NonNull RepositoryConnection repositoryConnection, @NonNull String query,
      @NonNull Map<String, Value> bindings) {
    Query preparedQuery;

    try {
      preparedQuery = repositoryConnection.prepareQuery(QueryLanguage.SPARQL, query);
    } catch (RDF4JException e) {
      throw new BackendException(String.format("Query could not be prepared: %s", query), e);
    }

    bindings.forEach(preparedQuery::setBinding);

    if (preparedQuery instanceof GraphQuery) {
      try {
        return ((GraphQuery) preparedQuery).evaluate();
      } catch (QueryEvaluationException e) {
        throw new BackendException(String.format("Query could not be evaluated: %s", query), e);
      }
    }

    if (preparedQuery instanceof TupleQuery) {
      try {
        return ((TupleQuery) preparedQuery).evaluate();
      } catch (QueryEvaluationException e) {
        throw new BackendException(String.format("Query could not be evaluated: %s", query), e);
      }
    }

    throw new BackendException(
        String.format("Query type '%s' not supported.", preparedQuery.getClass()));
  }

  public void add(@NonNull RepositoryConnection repositoryConnection, @NonNull Model model,
      @NonNull IRI targetGraph) {

    try {
      if (queryContainsBNode(model)) {
        repositoryConnection.prepareGraphQuery(getInsertQuery(model, targetGraph));
      } else {
        repositoryConnection.add(model, targetGraph);
      }
    } catch (RDF4JException e) {
      throw new BackendException(
          String.format("Data could not be added into graph: %s", e.getMessage()), e);
    }
  }

  public void update(@NonNull RepositoryConnection repositoryConnection, @NonNull String query,
      @NonNull Map<String, Value> bindings) {
    Update preparedQuery;

    try {
      preparedQuery = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, query);
    } catch (RDF4JException e) {
      throw new BackendException(String.format("Query could not be prepared: %s", query), e);
    }

    bindings.forEach(preparedQuery::setBinding);

    try {
      preparedQuery.execute();
    } catch (QueryEvaluationException e) {
      throw new BackendException(String.format("Query could not be executed: %s", query), e);
    }
  }

  private String getInsertQuery(Model model, IRI targetGraph) {
    StringBuilder insertQueryBuilder = new StringBuilder();
    insertQueryBuilder.append("INSERT {");
    insertQueryBuilder.append("GRAPH <" + targetGraph.stringValue() + "> {");
    model.stream().forEach(statement -> insertQueryBuilder.append(statement.getSubject() + " "
        + statement.getPredicate() + " " + statement.getObject() + " ."));
    insertQueryBuilder.append("}};");
    final String insertQuery = insertQueryBuilder.toString();
    LOG.debug("Transformed INSERT query: \n{}", insertQuery);

    return insertQuery;
  }

  private boolean queryContainsBNode(Model model) {
    return model.subjects().stream().anyMatch(subject -> subject instanceof BNode);
  }

}
