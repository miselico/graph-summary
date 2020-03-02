/*******************************************************************************
 * Copyright (c) 2012 National University of Ireland, Galway. All Rights Reserved.
 *
 *
 * This project is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.sindice.core.analytics.stats.domain;

import java.io.IOException;
import java.util.LinkedList;

import org.junit.Test;
import org.sindice.core.analytics.stats.domain.assembly.CountPredicatePerDomain;
import org.sindice.core.analytics.testHelper.AbstractAnalyticsTestCase;

import cascading.flow.Flow;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.pipe.Pipe;
import cascading.scheme.Scheme;
import cascading.scheme.hadoop.TextLine;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;
import cascading.tuple.TupleEntryIterator;

/**
 * @project analytics
 * @author Thomas Perry <thomas.perry@deri.org>
 */
public class DomainPredicateTest extends AbstractAnalyticsTestCase {

  protected final static String jsonPath = "./src/test/resources/basic-input.json";
  protected final static String domain1 = "domain1.com";
  protected final static String domain2 = "domain2.com";
  protected final static String domain3 = "domain3.com";
  protected final static String pred1 = "http://www.predicate.com/#p1";
  protected final static String pred2 = "http://www.predicate.com/#p2";
  protected final static String pred3 = "http://www.predicate.com/#p3";
  protected final static String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

  @Test
  public void Test() throws IOException {
    Scheme sourceScheme = new TextLine(new Fields("value"));
    Tap source = new Hfs(sourceScheme, jsonPath);
    Scheme sinkScheme = new TextLine(new Fields("output"));
    Tap sink = new Hfs(sinkScheme, testOutput.getAbsolutePath(),
        SinkMode.REPLACE);

    Pipe parse = new CountPredicatePerDomain();

    // plan a new Flow from the assembly using the source and sink Taps
    // with the above properties
    Flow flow = new HadoopFlowConnector().connect(source, sink, parse);
    flow.complete();

    LinkedList<String> expectedTuples = new LinkedList<String>();
    expectedTuples.add(domain3 + "\t" + pred3 + "\t1\t1");
    expectedTuples.add(domain1 + "\t" + pred1 + "\t1\t1");
    expectedTuples.add(domain3 + "\t" + pred1 + "\t1\t1");
    expectedTuples.add(domain1 + "\t" + rdfType + "\t1\t1");
    expectedTuples.add(domain2 + "\t" + rdfType + "\t6\t2");
    expectedTuples.add(domain1 + "\t" + pred2 + "\t1\t1");

    final TupleEntryIterator iterator = flow.openSink();

    while (iterator.hasNext()) {
      TupleEntry tuple = iterator.next();
      assertTrue(expectedTuples.contains(tuple.getTuple().toString()));
      expectedTuples.remove(tuple.getTuple().toString());
    }

    // Check the correct number of tuples
    assertTrue(expectedTuples.isEmpty());
  }

}
