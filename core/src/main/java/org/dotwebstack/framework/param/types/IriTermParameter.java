package org.dotwebstack.framework.param.types;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class IriTermParameter extends TermParameter<IRI> {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  public IriTermParameter(IRI identifier, String name, boolean required, String defaultValue) {
    super(identifier, name, required, defaultValue);
  }

  @Override
  public Literal getValue(IRI value) {
    return VALUE_FACTORY.createLiteral(value.stringValue());
  }

  @Override
  protected IRI handleInner(String value) {
    if (value != null) {
      return VALUE_FACTORY.createIRI(value);
    } else {
      return null;
    }
  }
}
