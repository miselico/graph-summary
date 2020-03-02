/**
 * Copyright (c) 2012 National University of Ireland, Galway. All Rights Reserved.
 */
package org.sindice.core.analytics.testHelper.iotransformation;

import java.util.Arrays;

import org.sindice.core.analytics.cascading.AnalyticsSubAssembly;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.pipe.Pipe;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

/**
 * This {@link Function} is used as a post-processing operation in the testing of an {@link AnalyticsSubAssembly}.
 * It sorts, if necessary, the content of a field, in preparation for JUnit tests assertions.
 */
public class SortFunction
extends BaseOperation<Tuple>
implements Function<Tuple> {

  private static final long serialVersionUID = -4770152649946441953L;
  private final Class[]     types;

  /**
   * Create a new {@link SortFunction} instance with the given input {@link Pipe} name
   * and describing {@link FieldType}s.
   * @param fieldDeclaration of type {@link Fields}
   * @param types the set of {@link FieldType}s
   */
  public SortFunction(Fields fieldDeclaration, Class[] types) {
    super(1, fieldDeclaration);
    if (types == null || types.length != getFieldDeclaration().size()) {
      throw new IllegalArgumentException("The number of describing FieldTypes and output Fields must be equal");
    }
    this.types = types;
  }

  @Override
  public void prepare(final FlowProcess flowProcess,
                      final OperationCall<Tuple> operationCall) {
    operationCall.setContext(new Tuple());
  }

  @Override
  public void operate(final FlowProcess flowProcess,
                      final FunctionCall<Tuple> functionCall) {
    final TupleEntry arguments = functionCall.getArguments();
    final Tuple result = functionCall.getContext();

    result.clear();
    Tuple tuple = arguments.getTuple();

    //Executes the decode and encode implemented methods for every class
    for (int i = 0; i < types.length; i++) {
      try {
        FieldType<?> field = (FieldType<?>) types[i]
        .getConstructor(FlowProcess.class, String.class).newInstance(flowProcess, null);
        result.add(field.sort(tuple.getObject(i)));
      } catch (Exception e) {
        throw new RuntimeException("Exception catched", e);
      }
    }
    functionCall.getOutputCollector().add(result);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof SortFunction)) {
      return false;
    }
    if (!super.equals(object)) {
      return false;
    }

    final SortFunction func = (SortFunction) object;
    return Arrays.equals(types, func.types);
  }

  @Override
  public int hashCode() {
    int hash = super.hashCode();
    for (Class ft : types) {
      hash = hash * 31 + ft.hashCode();
    }
    return hash;
  }

}
