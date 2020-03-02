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
package org.sindice.core.analytics.stats;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;
import org.sindice.core.analytics.cascading.annotation.Analytics;
import org.sindice.core.analytics.stats.entity.assembly.CountTotalTriplePerEntity;
import org.sindice.core.analytics.testHelper.AbstractAnalyticsTestCase;

import cascading.flow.Flow;
import cascading.flow.hadoop.HadoopFlowConnector;
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
public class EntityTripleTest extends AbstractAnalyticsTestCase {

  private final String jsonPath = "./src/test/resources/basic-input.json";

  @Test
  public void Test() throws IOException {
    CountTotalTriplePerEntity parse = new CountTotalTriplePerEntity();
    Scheme sourceScheme = new TextLine(Analytics.getHeadFields(parse));
    Tap source = new Hfs(sourceScheme, jsonPath);
    Scheme sinkScheme = new TextLine(new Fields("output"));
    Tap sink = new Hfs(sinkScheme, testOutput.getAbsolutePath(),
        SinkMode.REPLACE);

    Flow flow = new HadoopFlowConnector().connect(source, sink, parse);
    flow.complete();

    final ArrayList<String> actualTuples = new ArrayList<String>();
    final ArrayList<String> expectedTuples = new ArrayList<String>();
    expectedTuples.add("http://www.domain1.com/~sub2\t1");
    expectedTuples.add("http://www.domain2.com/~sub1\t6");
    expectedTuples.add("bnode1|http://domain2.com/url:stuff2\t1");
    expectedTuples.add("http://www.domain1.com/~sub1\t1");
    expectedTuples.add("bnode1|http://domain3.com/url:stuff\t1");
    expectedTuples.add("bnode1|http://domain2.com/url:stuff\t1");

    final TupleEntryIterator iterator = flow.openSink();
    while (iterator.hasNext()) {
      TupleEntry tuple = iterator.next();
      actualTuples.add(tuple.getTuple().toString());
    }
    Collections.sort(actualTuples);
    Collections.sort(expectedTuples);
    assertArrayEquals(expectedTuples.toArray(new String[0]),
      actualTuples.toArray(new String[0]));
  }

}
