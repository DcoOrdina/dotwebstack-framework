package org.dotwebstack.framework.transaction.flow.step.persistence;

import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.dotwebstack.framework.transaction.flow.step.StepExecutor;
import org.dotwebstack.framework.transaction.flow.step.StepFailureException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceStep implements Step {

  private static final Logger LOG = LoggerFactory.getLogger(PersistenceStep.class);

  private Resource identifier;

  private IRI persistenceStrategy;

  private Backend backend;

  private BackendResourceProvider backendResourceProvider;

  private IRI targetGraph;

  public PersistenceStep(@NonNull Builder builder) {
    this.identifier = builder.identifier;
    this.persistenceStrategy = builder.persistenceStrategy;
    this.backend = builder.backend;
    this.targetGraph = builder.targetGraph;
    this.backendResourceProvider = builder.backendResourceProvider;
  }

  public StepExecutor createStepExecutor(
      @NonNull RepositoryConnection transactionRepositoryConnection) {
    Model transactionModel =
        QueryResults.asModel(transactionRepositoryConnection.getStatements(null, null, null));
    if (!transactionModel.isEmpty()) {
      return backendResourceProvider.get(backend.getIdentifier()).createPersistenceStepExecutor(
          this, transactionModel);
    }
    LOG.debug("Could not create a persistence step {}, cause of an empty transaction repository",
        identifier);
    throw new StepFailureException("Transaction repository is empty");
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public IRI getPersistenceStrategy() {
    return persistenceStrategy;
  }

  public Backend getBackend() {
    return backend;
  }

  public IRI getTargetGraph() {
    return targetGraph;
  }

  public static final class Builder {

    private Resource identifier;

    private IRI persistenceStrategy;

    private Backend backend;

    private BackendResourceProvider backendResourceProvider;

    private IRI targetGraph;

    public Builder(@NonNull Resource identifier,
        @NonNull BackendResourceProvider backendResourceProvider) {
      this.identifier = identifier;
      this.backendResourceProvider = backendResourceProvider;
    }

    public Builder persistenceStrategy(@NonNull IRI persistenceStrategy) {
      this.persistenceStrategy = persistenceStrategy;
      return this;
    }

    public Builder backend(@NonNull Backend backend) {
      this.backend = backend;
      return this;
    }

    public Builder targetGraph(@NonNull IRI targetGraph) {
      this.targetGraph = targetGraph;
      return this;
    }

    public PersistenceStep build() {
      return new PersistenceStep(this);
    }
  }

}
