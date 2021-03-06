/**
 * Copyright (c) 2012 National University of Ireland, Galway. All Rights Reserved.
 */
package org.sindice.core.analytics.cascading.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.sindice.core.analytics.cascading.AnalyticsSubAssembly;

import cascading.pipe.Pipe;

/**
 * This annotation defines the set of tails {@link Pipe} of an {@link AnalyticsSubAssembly}.
 *
 * <p>
 *
 * A tail is defined using an {@link AnalyticsPipe}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface AnalyticsTailPipes {

  /**
   * The set of {@link AnalyticsPipe} that are the tails of the annotated
   * {@link AnalyticsSubAssembly}.
   */
  AnalyticsPipe[] values();

}
