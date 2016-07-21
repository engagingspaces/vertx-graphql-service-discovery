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

package io.engagingspaces.servicediscovery.graphql.discovery;

import io.vertx.core.Vertx;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.List;

/**
 * Interface for registrar implementations.
 *
 * @author Arnold Schrijver
 */
public interface Registrar {

    /**
     * @return the vert.x instance
     */
    Vertx getVertx();

    /**
     * Gets the service discovery of the specified name, if it is managed by this registrar.
     * <p>
     * The returned service discovery is wrapped as a managed service discovery
     * so that a call to {@link ServiceDiscovery#close()} are forwarded to the registrar for handling.
     *
     * @param discoveryName the service discovery name
     * @return the managed service discovery, null otherwise
     */
    ServiceDiscovery getDiscovery(String discoveryName);

    /**
     * @return the list of names of managed service discoveries
     */
    List<String> serviceDiscoveryNames();
}
