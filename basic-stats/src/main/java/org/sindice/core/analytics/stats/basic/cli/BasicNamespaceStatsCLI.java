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
package org.sindice.core.analytics.stats.basic.cli;

import java.util.List;
import java.util.Properties;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ToolRunner;
import org.sindice.core.analytics.cascading.AbstractAnalyticsCLI;
import org.sindice.core.analytics.cascading.AnalyticsParameters;
import org.sindice.core.analytics.cascading.CascadeConfYAMLoader;
import org.sindice.core.analytics.stats.basic.assembly.BasicNamespaceStats;

import cascading.flow.Flow;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.management.UnitOfWork;
import cascading.property.AppProps;
import cascading.scheme.hadoop.SequenceFile;
import cascading.scheme.hadoop.WritableSequenceFile;
import cascading.stats.CascadingStats;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@deri.org>
 * @link www.di.unipi.it/~ceccarel
 */
public class BasicNamespaceStatsCLI extends AbstractAnalyticsCLI {

  private static final String CLASS_ATTRIBUTES = "class-attributes";

  @Override
  protected void initializeOptionParser(final OptionParser parser) {
    parser
        .accepts(CLASS_ATTRIBUTES,
            "The list of class attributes to use, for defining the classes")
        .withRequiredArg().ofType(String.class).withValuesSeparatedBy(',');
  }

  @Override
  protected UnitOfWork<? extends CascadingStats> doRun(final OptionSet options)
  throws Exception {
    final Properties properties = cascadeConf.getFlowsConfiguration()
    .get(CascadeConfYAMLoader.DEFAULT_PARAMETERS);

    // Check for input directory
    final Fields fieldSelector = new Fields("value");
    Tap<?, ?, ?> source = new Hfs(new WritableSequenceFile(fieldSelector,
        Text.class), input.get(0));
    // Set the class attributes
    if (options.has(CLASS_ATTRIBUTES)) {
      final List<String> classes = (List<String>) options
          .valuesOf(CLASS_ATTRIBUTES);
      final StringBuilder sb = new StringBuilder();
      for (String c : classes) {
        sb.append(c).append(',');
      }
      properties
          .setProperty(AnalyticsParameters.CLASS_ATTRIBUTES_FIELD.toString(),
              sb.toString());
    } else {
      printMissingOptionError(CLASS_ATTRIBUTES);
    }
    // Set source, sink and fields for job
    AppProps.setApplicationJarClass(properties, BasicNamespaceStatsCLI.class);
    final BasicNamespaceStats pipe = new BasicNamespaceStats();
    final Fields fd = new Fields("namespace", "objectsNumber", "url",
        "domain", "sndDomain");
    final Flow<?> flow = new HadoopFlowConnector(properties).connect(
        "basic-namespace-stats", source, new Hfs(new SequenceFile(fd), output.get(0),
            SinkMode.REPLACE), pipe);

    // Run the job
    flow.start();
    flow.complete();

    return flow;
  }

  public static void main(final String[] args) throws Exception {
    final BasicNamespaceStatsCLI cli = new BasicNamespaceStatsCLI();
    int res = ToolRunner.run(new Configuration(), cli, args);
    System.exit(res);
  }

}
