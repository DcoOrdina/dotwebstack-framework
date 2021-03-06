package org.dotwebstack.framework.frontend.openapi.handlers;

import java.util.Collection;
import java.util.Map;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.informationproduct.AbstractInformationProduct;
import org.dotwebstack.framework.informationproduct.template.TemplateProcessor;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.IRI;

final class TestInformationProduct extends AbstractInformationProduct {

  TestInformationProduct(IRI identifier, String label, ResultType resultType,
      Collection<Parameter> parameters, TemplateProcessor templateProcessor) {
    super(identifier, label, resultType, parameters, templateProcessor);
  }

  @Override
  public Object getResult(Map<String, String> parameterValues) {
    throw new UnsupportedOperationException(
        "getResult() method unsupported, mock the InformationProduct interface instead");
  }

}
