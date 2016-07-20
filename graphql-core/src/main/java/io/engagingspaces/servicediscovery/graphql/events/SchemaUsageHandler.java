/*
 * Copyright (c) 2016 The original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.engagingspaces.servicediscovery.graphql.events;

/**
 * Event handler that is invoked after a schema reference has been bound or released.
 *
 * @author Arnold Schrijver
 */
@FunctionalInterface
public interface SchemaUsageHandler {

    /**
     * Event handler that handles service discovery `usage` events for GraphQL services that have been bound
     * or released.
     *
     * @param eventData the service reference state
     */
    void schemaReferenceEvent(SchemaReferenceData eventData);
}
