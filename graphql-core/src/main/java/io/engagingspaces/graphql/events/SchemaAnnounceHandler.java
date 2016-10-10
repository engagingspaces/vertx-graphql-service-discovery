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

package io.engagingspaces.graphql.events;

import io.vertx.servicediscovery.Record;

/**
 * Event handler that is invoked when a schema has been published or un-published.
 *
 * @author <a href="https://github.com/aschrijver/">Arnold Schrijver</a>
 */
@FunctionalInterface
public interface SchemaAnnounceHandler {

    /**
     * Event handler that handles service discovery `announce` events for GraphQL services.
     *
     * @param record the service record of the GraphQL service
     */
    void schemaDiscoveryEvent(Record record);
}
